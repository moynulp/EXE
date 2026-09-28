package com.zunawet.clipvault.ui.panel
import androidx.compose.runtime.*
import androidx.compose.ui.awt.ComposePanel
import com.zunawet.clipvault.data.clipboard.ClipboardWatcher
import com.zunawet.clipvault.domain.model.Clip
import com.zunawet.clipvault.platform.windows.WindowStyleManager
import com.zunawet.clipvault.ui.theme.VaultTheme
import kotlinx.coroutines.*
import java.awt.*
import javax.swing.JWindow
import javax.swing.JFrame

class PanelWindow(private val vm: PanelViewModel,private val watcher: ClipboardWatcher,private val scope: CoroutineScope,
 private val paste: (Clip) -> Unit,private val settings: () -> Unit): AutoCloseable {
 // A focusable JWindow needs a showing Frame owner. This transparent, off-screen
 // tool owner has no taskbar entry and never activates. A hidden shared owner would
 // prevent search from obtaining keyboard focus even after clearing NOACTIVATE.
 private val owner=JFrame().apply {
  isUndecorated=true;type=Window.Type.UTILITY;background=Color(0,0,0,0)
  focusableWindowState=false;setSize(1,1);setLocation(-32000,-32000)
  addNotify();WindowStyleManager.prepare(this);isVisible=true
 }
 private val window=JWindow(owner)
 private var shown by mutableStateOf(false)
 private var overlay by mutableStateOf(false)
 @Volatile var visible=false;private set
 @Volatile var searchFocused=false;private set
 var target: com.sun.jna.platform.win32.WinDef.HWND?=null;private set
 private var closing: Job?=null
 private val reduced get()=WindowStyleManager.reducedMotion() || vm.repo.enabled("reducedMotion")
 init {
  window.name="ClipVault picker";window.background=Color(0,0,0,0);window.focusableWindowState=false
  window.isAlwaysOnTop=true;window.type=Window.Type.POPUP
  val panel=ComposePanel()
  panel.background=Color(0,0,0,0)
  window.contentPane.add(panel);window.setSize(424,584);window.addNotify()
  WindowStyleManager.prepare(window)
  panel.setContent {
   val prefs by vm.repo.settings.collectAsState();val processing by watcher.processing.collectAsState()
   VaultTheme(prefs["theme"]=="dark") {
    PanelScreen(vm,shown,overlay,reduced,processing,::focusSearch,paste,{scope.launch { vm.repo.pin(it) }},{scope.launch { vm.repo.delete(it) }},::hide,settings)
   }
  }
 }
 fun show(compact: Boolean=false) {
  closing?.cancel()
  if(!visible) target=WindowStyleManager.foreground()
  shown=false;searchFocused=false;overlay=compact;vm.reset()
  WindowStyleManager.prepare(window,false)
  val pointer=MouseInfo.getPointerInfo();val gc=pointer.device.defaultConfiguration
  val insets=Toolkit.getDefaultToolkit().getScreenInsets(gc)
  val bounds=Rectangle(gc.bounds).apply { x+=insets.left;y+=insets.top;width-=insets.left+insets.right;height-=insets.top+insets.bottom }
  window.setSize(if(compact) 404 else 424,if(compact) 484 else 584)
  val cursor=compact || vm.repo.enabled("cursor",true)
  val x=if(cursor) pointer.location.x+4 else bounds.x+(bounds.width-window.width)/2
  val y=if(cursor) pointer.location.y+4 else bounds.y+(bounds.height-window.height)/2
  window.setLocation(x.coerceIn(bounds.x,(bounds.x+bounds.width-window.width).coerceAtLeast(bounds.x)),y.coerceIn(bounds.y,(bounds.y+bounds.height-window.height).coerceAtLeast(bounds.y)))
  visible=true;window.isVisible=true
  scope.launch { delay(16);shown=true }
 }
 private fun focusSearch() {
  if(searchFocused) return
  searchFocused=true;WindowStyleManager.prepare(window,true);window.requestFocus()
 }
 fun outsideClick() {
  if(!visible) return
  val p=MouseInfo.getPointerInfo().location
  if(!window.bounds.contains(p)) hide(false)
 }
 fun hide(restore: Boolean=true) {
  if(!visible) return
  shown=false;visible=false
  if(restore && searchFocused) WindowStyleManager.restore(target)
  searchFocused=false
  closing=scope.launch { delay(if(reduced) 100 else 180);window.isVisible=false;WindowStyleManager.prepare(window,false) }
 }
 fun nav(vk: Int) { when(vk) { 0x26 -> vm.move(-1);0x28 -> vm.move(1);0x1B -> hide();0x0D -> vm.results.value.getOrNull(vm.selected.value)?.let(paste) } }
 override fun close() { closing?.cancel();window.dispose();owner.dispose() }
}
