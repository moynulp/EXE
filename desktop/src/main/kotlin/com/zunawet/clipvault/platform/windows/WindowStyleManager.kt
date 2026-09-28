package com.zunawet.clipvault.platform.windows
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.*
import com.sun.jna.ptr.IntByReference
import java.awt.Window

object WindowStyleManager {
 val windows=System.getProperty("os.name").startsWith("Windows")
 private const val NOACTIVATE=0x08000000
 private const val TOOLWINDOW=0x00000080
 fun handle(window: Window): WinDef.HWND = WinDef.HWND(Native.getWindowPointer(window))
 fun prepare(window: Window, activate: Boolean=false) {
  window.focusableWindowState=activate
  window.isAlwaysOnTop=true
  if(!windows) return
  val h=handle(window);val u=User32.INSTANCE
  val style=u.GetWindowLong(h,WinUser.GWL_EXSTYLE)
  u.SetWindowLong(h,WinUser.GWL_EXSTYLE,(if(activate) style and NOACTIVATE.inv() else style or NOACTIVATE) or TOOLWINDOW)
  check(u.SetWindowPos(h,WinDef.HWND(Pointer.createConstant(-1)),0,0,0,0,0x0001 or 0x0002 or 0x0010 or 0x0020)) { "Cannot set non-activating window style" }
 }
 fun foreground(): WinDef.HWND? = if(windows) User32.INSTANCE.GetForegroundWindow() else null
 fun restore(target: WinDef.HWND?): Boolean {
  if(!windows) return true
  if(target==null || !User32.INSTANCE.IsWindow(target)) return false
  User32.INSTANCE.SetForegroundWindow(target)
  return User32.INSTANCE.GetForegroundWindow()==target
 }
 fun sourceApp(): String? {
  if(!windows) return null
  val pid=IntByReference();User32.INSTANCE.GetWindowThreadProcessId(foreground(),pid)
  val process=Kernel32.INSTANCE.OpenProcess(0x1000,false,pid.value) ?: return null
  return try {
   val size=IntByReference(32768);val buffer=CharArray(size.value)
   if(Kernel32.INSTANCE.QueryFullProcessImageName(process,0,buffer,size)) String(buffer,0,size.value).substringAfterLast('\\') else null
  } finally { Kernel32.INSTANCE.CloseHandle(process) }
 }
 fun reducedMotion(): Boolean {
  if(!windows) return false
  return runCatching { !Advapi32Util.registryGetIntValue(WinReg.HKEY_CURRENT_USER,"Control Panel\\Desktop\\WindowMetrics","MinAnimate").let { it!=0 } }.getOrElse {
   runCatching { Advapi32Util.registryGetStringValue(WinReg.HKEY_CURRENT_USER,"Control Panel\\Desktop\\WindowMetrics","MinAnimate")=="0" }.getOrDefault(false)
  }
 }
}
