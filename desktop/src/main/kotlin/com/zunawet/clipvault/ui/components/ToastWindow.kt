package com.zunawet.clipvault.ui.components
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.*
import com.zunawet.clipvault.platform.windows.WindowStyleManager
import com.zunawet.clipvault.ui.theme.*
import kotlinx.coroutines.*
import java.awt.*
import javax.swing.JWindow
class ToastWindow(private val scope: CoroutineScope): AutoCloseable {
 private val window=JWindow()
 private var text by mutableStateOf("")
 private var visible by mutableStateOf(false)
 private var job: Job?=null
 init {
  window.background=Color(0,0,0,0);window.focusableWindowState=false;window.isAlwaysOnTop=true
  window.setSize(520,72);val panel=ComposePanel();panel.background=Color(0,0,0,0);window.contentPane.add(panel);window.addNotify();WindowStyleManager.prepare(window)
  panel.setContent { VaultTheme {
   val alpha by animateFloatAsState(if(visible) 1f else 0f,tween(if(WindowStyleManager.reducedMotion()) 100 else 200))
   Box(Modifier.fillMaxSize().graphicsLayer { this.alpha=alpha },contentAlignment=Alignment.Center) {
    Surface(shape=RoundedCornerShape(999.dp),color=Yellow,shadowElevation=8.dp) { Text(text,Modifier.padding(horizontal=20.dp,vertical=12.dp),color=Ink,fontSize=13.sp) }
   }
  } }
 }
 fun show(message: String) {
  job?.cancel();text=message
  val gc=MouseInfo.getPointerInfo().device.defaultConfiguration;val b=gc.bounds;val inset=Toolkit.getDefaultToolkit().getScreenInsets(gc)
  window.setLocation(b.x+(b.width-window.width)/2,b.y+b.height-inset.bottom-96)
  WindowStyleManager.prepare(window);window.isVisible=true;visible=true
  job=scope.launch { delay(1800);visible=false;delay(220);window.isVisible=false }
 }
 override fun close() { job?.cancel();window.dispose() }
}
