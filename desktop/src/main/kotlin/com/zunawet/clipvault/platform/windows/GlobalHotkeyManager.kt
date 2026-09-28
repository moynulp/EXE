package com.zunawet.clipvault.platform.windows
import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.keyboard.*
import com.github.kwhat.jnativehook.mouse.*
import com.github.kwhat.jnativehook.NativeInputEvent
import com.sun.jna.*
import com.sun.jna.platform.win32.*
import com.sun.jna.win32.StdCallLibrary
import com.zunawet.clipvault.domain.model.Keybind
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.logging.*
import javax.swing.SwingUtilities

/** Native callback threads never touch Swing or Compose state directly. */
class GlobalHotkeyManager(private val scope: CoroutineScope, private val action: (String) -> Unit,
 private val nav: (Int) -> Unit, private val outsideClick: (Int,Int) -> Unit,
 private val panelUnfocused: () -> Boolean, private val doubleCtrl: () -> Boolean,
 private val save: (Keybind) -> Unit, private val message: (String) -> Unit): AutoCloseable {
 @Volatile var bindings=Keybind.defaults
 val recording=MutableStateFlow<String?>(null)
 val error=MutableStateFlow(0)
 val backend=MutableStateFlow("Starting")
 @Volatile private var nativeAvailable=false
 @Volatile private var threadId=0
 @Volatile private var hook: WinUser.HHOOK?=null
 private var timer: Job?=null
 private var lastCtrl=0L
 private var ctrlAlone=false
 private val pressed=mutableSetOf<Int>()
 private val registered=mutableListOf<Int>()
 private val keyboard=object: NativeKeyListener {
  override fun nativeKeyPressed(e: NativeKeyEvent) {
   if(!pressed.add(e.keyCode) || recording.value!=null) return
   val mods=buildSet { if(e.modifiers and NativeInputEvent.CTRL_MASK!=0) add("ctrl");if(e.modifiers and NativeInputEvent.SHIFT_MASK!=0) add("shift");if(e.modifiers and NativeInputEvent.ALT_MASK!=0) add("alt");if(e.modifiers and NativeInputEvent.META_MASK!=0) add("win") }
   val key=NativeKeyEvent.getKeyText(e.keyCode).uppercase()
   bindings.firstOrNull { it.modifiers==mods && it.key==key }?.let { dispatch { action(it.action) } }
  }
  override fun nativeKeyReleased(e: NativeKeyEvent) { pressed.remove(e.keyCode) }
 }
 private val mouse=object: NativeMouseListener { override fun nativeMousePressed(e: NativeMouseEvent) { dispatch { outsideClick(e.x,e.y) } } }
 private fun dispatch(block: () -> Unit) = SwingUtilities.invokeLater(block)
 fun start() {
  Logger.getLogger(GlobalScreen::class.java.`package`.name).apply { level=Level.OFF;useParentHandlers=false }
  try { GlobalScreen.registerNativeHook();GlobalScreen.addNativeKeyListener(keyboard);GlobalScreen.addNativeMouseListener(mouse);nativeAvailable=true;backend.value="JNativeHook" }
  catch(e: Exception) { backend.value="RegisterHotKey fallback";System.err.println("JNativeHook unavailable: ${e.message}") }
  if(WindowStyleManager.windows) Thread({ nativeLoop() },"ClipVault-Win32-hooks").apply { isDaemon=true;start() }
  else if(!nativeAvailable) backend.value="Unavailable"
 }
 fun update(b: List<Keybind>) { bindings=b; if(threadId!=0) User32.INSTANCE.PostThreadMessage(threadId,0x8001,null,null) }
 fun record(action: String) {
  cancel();recording.value=action
  if(threadId!=0) User32.INSTANCE.PostThreadMessage(threadId,0x8001,null,null)
  timer=scope.launch { delay(10_000);cancel();message("Recording cancelled — timed out") }
 }
 fun cancel() {
  recording.value=null;timer?.cancel();timer=null
  if(threadId!=0) User32.INSTANCE.PostThreadMessage(threadId,0x8001,null,null)
 }
 private fun accept(vk: Int, mods: Set<String>) {
  val action=recording.value ?: return
  if(vk==0x1B) { cancel();return }
  if(mods.isEmpty()) { error.value++;message("Add at least one modifier key");return }
  val key=when(vk) { in 0x41..0x5A,in 0x30..0x39 -> vk.toChar().toString();in 0x70..0x7B -> "F${vk-0x6F}";else -> null }
  if(key==null) { error.value++;message("Use a letter, number, or F1–F12");return }
  val b=Keybind(action,mods,key)
  val conflict=bindings.any { it.action!=action && it.modifiers==mods && it.key==key }
  val reserved="win" in mods || ("alt" in mods && key=="F4") || (mods==setOf("ctrl") && key in setOf("C","V","X","A","Z","T","W","L","N","S","P")) || (mods==setOf("ctrl","shift") && key in setOf("P","N","T","I","J"))
  if(conflict || reserved) { error.value++;message(if(conflict) "Already used by another ClipVault action" else "Likely reserved by Windows, Chrome, or VS Code");return }
  cancel();save(b);message("✓ Saved: ${b.label}")
 }
 private fun down(vk: Int)=User32.INSTANCE.GetAsyncKeyState(vk).toInt() and 0x8000!=0
 private val modifierKeys=setOf(0x10,0x11,0x12,0x5B,0x5C,0xA0,0xA1,0xA2,0xA3,0xA4,0xA5)
 // Hold callbacks strongly for the complete hook lifetime; injected Robot events pass through.
 private val lowLevel=WinUser.LowLevelKeyboardProc { code,wParam,info ->
  if(code>=0 && info.flags and 0x10==0) {
   val vk=info.vkCode;val isDown=wParam.toInt() in setOf(0x100,0x104)
   if(recording.value!=null && vk !in modifierKeys) {
    if(isDown) {
     val mods=buildSet { if(down(0x11)) add("ctrl");if(down(0x10)) add("shift");if(down(0x12)) add("alt");if(down(0x5B)||down(0x5C)) add("win") }
     dispatch { accept(vk,mods) }
    }
    return@LowLevelKeyboardProc WinDef.LRESULT(1)
   }
   if(recording.value==null && panelUnfocused() && vk in setOf(0x26,0x28,0x0D,0x1B)) {
    if(isDown) dispatch { nav(vk) }
    return@LowLevelKeyboardProc WinDef.LRESULT(1)
   }
   if(vk in setOf(0x11,0xA2,0xA3)) {
    if(isDown) ctrlAlone=true else if(ctrlAlone) {
     val now=System.currentTimeMillis()
     if(now-lastCtrl in 50..350 && doubleCtrl() && recording.value==null) { dispatch { action("panel") };lastCtrl=0 } else lastCtrl=now
     ctrlAlone=false
    }
   } else if(isDown) { ctrlAlone=false;lastCtrl=0 }
  }
  User32.INSTANCE.CallNextHookEx(hook,code,wParam,WinDef.LPARAM(Pointer.nativeValue(info.pointer)))
 }
 private val mouseHookProc=WinUser.LowLevelMouseProc { code,w,info ->
  if(code>=0 && w.toInt() in setOf(0x201,0x204,0x207)) dispatch { outsideClick(info.pt.x,info.pt.y) }
  User32.INSTANCE.CallNextHookEx(mouseHook,code,w,WinDef.LPARAM(Pointer.nativeValue(info.pointer)))
 }
 private var mouseHook: WinUser.HHOOK?=null
 private fun nativeLoop() {
  threadId=Kernel32.INSTANCE.GetCurrentThreadId()
  val u=User32.INSTANCE;val module=Kernel32.INSTANCE.GetModuleHandle(null)
  hook=u.SetWindowsHookEx(WinUser.WH_KEYBOARD_LL,lowLevel,module,0)
  if(!nativeAvailable) mouseHook=u.SetWindowsHookEx(WinUser.WH_MOUSE_LL,mouseHookProc,module,0)
  if(hook==null) dispatch { message("Native keyboard hook unavailable; navigation requires search focus") }
  fun register() {
   registered.forEach { u.UnregisterHotKey(null,it) };registered.clear()
   if(!nativeAvailable && recording.value==null) bindings.forEachIndexed { i,b ->
    val mods=b.modifiers.map { when(it) { "ctrl" -> 2;"shift" -> 4;"alt" -> 1;"win" -> 8;else -> 0 } }.sum() or 0x4000
    val vk=if(b.key.startsWith("F")&&b.key.length>1) 0x6F+(b.key.drop(1).toIntOrNull() ?: 1) else b.key.first().code
    if(u.RegisterHotKey(null,i+1,mods,vk)) registered.add(i+1) else dispatch { message("Could not register ${b.label}") }
   }
  }
  register()
  val msg=WinUser.MSG()
  while(u.GetMessage(msg,null,0,0)>0) {
   when(msg.message) {
    0x0312 -> if(recording.value==null) bindings.getOrNull(msg.wParam.toInt()-1)?.let { b -> dispatch { action(b.action) } }
    0x8001 -> register()
   }
   u.TranslateMessage(msg);u.DispatchMessage(msg)
  }
  registered.forEach { u.UnregisterHotKey(null,it) }
  hook?.let { u.UnhookWindowsHookEx(it) };mouseHook?.let { u.UnhookWindowsHookEx(it) }
 }
 override fun close() {
  cancel()
  if(nativeAvailable) { GlobalScreen.removeNativeKeyListener(keyboard);GlobalScreen.removeNativeMouseListener(mouse);runCatching { GlobalScreen.unregisterNativeHook() } }
  if(threadId!=0) User32.INSTANCE.PostThreadMessage(threadId,0x0012,null,null)
 }
}
