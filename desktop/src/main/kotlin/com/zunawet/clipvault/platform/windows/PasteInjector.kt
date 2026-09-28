package com.zunawet.clipvault.platform.windows
import com.zunawet.clipvault.domain.model.*
import com.zunawet.clipvault.data.clipboard.ClipboardWatcher
import com.sun.jna.platform.win32.*
import kotlinx.coroutines.*
import java.awt.*
import java.awt.datatransfer.*
import java.awt.event.KeyEvent
import java.io.File
import javax.imageio.ImageIO

class PasteInjector(private val watcher: ClipboardWatcher) {
 suspend fun paste(clip: Clip, target: WinDef.HWND?, plain: Boolean=false): Boolean = withContext(Dispatchers.IO) {
  val transferable: Transferable=when {
   plain && clip.type==ClipType.IMAGE -> StringSelection(clip.ocr ?: return@withContext false)
   clip.type==ClipType.IMAGE -> SingleTransfer(DataFlavor.imageFlavor,ImageIO.read(File(clip.content)))
   clip.type==ClipType.FILE && !plain -> SingleTransfer(DataFlavor.javaFileListFlavor,clip.content.lines().map(::File))
   else -> StringSelection(clip.content)
  }
  // Never inject into the wrong application. Fail closed when foreground restoration is denied.
  val focused=withContext(Dispatchers.Main) { WindowStyleManager.restore(target) }
  if(!focused) return@withContext false
  watcher.ignoreNext(clip.hash)
  var copied=false
  repeat(5) {
   if(!copied) try { Toolkit.getDefaultToolkit().systemClipboard.setContents(transferable,null);copied=true }
   catch(_: IllegalStateException) { delay(35) }
  }
  if(!copied) return@withContext false
  // Do not synthesize Ctrl+V while the invocation's Shift/Alt/Win keys are held.
  if(WindowStyleManager.windows) {
   var wait=0
   while(listOf(0x10,0x11,0x12,0x5B,0x5C).any { User32.INSTANCE.GetAsyncKeyState(it).toInt() and 0x8000 != 0 }) {
    delay(10);wait+=10;if(wait>2000) return@withContext false
   }
   if(WindowStyleManager.foreground()!=target) return@withContext false
  }
  val robot=Robot()
  try { robot.keyPress(KeyEvent.VK_CONTROL);robot.keyPress(KeyEvent.VK_V);robot.delay(20) }
  finally { robot.keyRelease(KeyEvent.VK_V);robot.keyRelease(KeyEvent.VK_CONTROL) }
  true
 }
 private class SingleTransfer(private val flavor: DataFlavor,private val value: Any): Transferable {
  override fun getTransferDataFlavors()=arrayOf(flavor)
  override fun isDataFlavorSupported(f: DataFlavor)=f==flavor
  override fun getTransferData(f: DataFlavor): Any { if(f!=flavor) throw UnsupportedFlavorException(f);return value }
 }
}
