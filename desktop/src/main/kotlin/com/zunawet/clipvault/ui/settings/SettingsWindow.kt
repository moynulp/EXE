package com.zunawet.clipvault.ui.settings
import androidx.compose.runtime.*
import androidx.compose.ui.awt.ComposePanel
import com.zunawet.clipvault.data.repository.VaultRepository
import com.zunawet.clipvault.platform.windows.GlobalHotkeyManager
import com.zunawet.clipvault.ui.theme.VaultTheme
import java.awt.Dimension
import java.awt.event.*
import javax.swing.JFrame
import javax.imageio.ImageIO
class SettingsWindow(repo: VaultRepository,private val hotkeys: GlobalHotkeyManager,clear: () -> Unit): AutoCloseable {
 private val window=JFrame("ClipVault · Made by Zunawet")
 private var page by mutableStateOf("Home")
 init {
  window.defaultCloseOperation=JFrame.HIDE_ON_CLOSE;window.setSize(1040,680);window.minimumSize=Dimension(900,600)
  window.iconImage=ImageIO.read(javaClass.getResource("/icons/clipvault-256.png"))
  window.setLocationRelativeTo(null)
  window.addWindowListener(object: WindowAdapter() { override fun windowClosing(e: WindowEvent) { hotkeys.cancel() } })
  val panel=ComposePanel();window.contentPane.add(panel)
  panel.setContent {
   val prefs by repo.settings.collectAsState()
   VaultTheme(prefs["theme"]=="dark") { SettingsScreen(repo,hotkeys,page,clear) }
  }
 }
 fun show(initial: String="Home") { page=initial;window.isVisible=true;window.toFront();window.requestFocus() }
 override fun close() { hotkeys.cancel();window.dispose() }
}
