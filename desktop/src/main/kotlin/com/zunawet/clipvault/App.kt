package com.zunawet.clipvault
import com.zunawet.clipvault.data.repository.VaultRepository
import com.zunawet.clipvault.data.clipboard.ClipboardWatcher
import com.zunawet.clipvault.data.ocr.OcrService
import com.zunawet.clipvault.platform.windows.*
import com.zunawet.clipvault.platform.common.SingleInstance
import com.zunawet.clipvault.ui.panel.*
import com.zunawet.clipvault.ui.settings.SettingsWindow
import com.zunawet.clipvault.ui.components.ToastWindow
import com.zunawet.clipvault.domain.model.*
import kotlinx.coroutines.*
import org.koin.core.component.*
import javax.swing.*
class App(private val instance: SingleInstance): KoinComponent {
 private val scope: CoroutineScope by inject()
 private val repo: VaultRepository by inject()
 private val ocr: OcrService by inject()
 private val vm: PanelViewModel by inject()
 private lateinit var watcher: ClipboardWatcher
 private lateinit var panel: PanelWindow
 private lateinit var keys: GlobalHotkeyManager
 private lateinit var tray: TrayIconManager
 private lateinit var settings: SettingsWindow
 private lateinit var toast: ToastWindow
 fun start() { scope.launch {
  repo.initialize()
  toast=ToastWindow(scope)
  tray=TrayIconManager { a -> SwingUtilities.invokeLater { action(a) } }
  watcher=ClipboardWatcher(repo,ocr,scope,WindowStyleManager::sourceApp) { text -> SwingUtilities.invokeLater { if(!panel.visible) tray.notification(text) } }
  panel=PanelWindow(vm,watcher,scope,{ paste(it) },{ panel.hide();settings.show() })
  keys=GlobalHotkeyManager(scope,::action,panel::nav,{_,_ -> panel.outsideClick() },{panel.visible && !panel.searchFocused},{repo.enabled("doubleCtrl")},{ b -> scope.launch { repo.bind(b) } },toast::show)
  settings=SettingsWindow(repo,keys,::clear)
  tray.install();keys.update(repo.keybinds.value);keys.start();watcher.start()
  scope.launch { repo.keybinds.collect { keys.update(it) } }
  scope.launch { watcher.paused.collect { tray.paused(it) } }
  instance.listen(scope) { settings.show() }
  // Intentionally no visible window at startup.
 } }
 private fun paste(c: Clip,plain: Boolean=false) {
  val target=if(panel.visible) panel.target else WindowStyleManager.foreground()
  if(panel.visible) panel.hide()
  scope.launch {
   val ok=runCatching { PasteInjector(watcher).paste(c,target,plain) }.getOrDefault(false)
   toast.show(if(ok) "✓ Copied to clipboard" else "Could not paste — check the target window")
  }
 }
 private fun action(a: String) {
  if(keys.recording.value!=null && a !in setOf("quit","settings")) return
  when(a) {
   "panel" -> panel.show();"overlay" -> panel.show(true)
   "pause" -> { watcher.paused.value=!watcher.paused.value;toast.show(if(watcher.paused.value) "Capture paused" else "Capture resumed") }
   "plain" -> repo.clips.value.maxByOrNull { it.createdAt }?.let { paste(it,true) }
   "recent1","recent2","recent3" -> repo.clips.value.sortedByDescending { it.createdAt }.getOrNull(a.last().digitToInt()-1)?.let { paste(it,false) }
   "settings" -> {panel.hide();settings.show("Keybinds")};"about" -> {panel.hide();settings.show("About")}
   "clear" -> clear()
   "quit" -> close()
  }
 }
 private fun clear() {
  panel.hide()
  if(JOptionPane.showConfirmDialog(null,"Delete all unpinned clips? This cannot be undone.","Clear ClipVault history",JOptionPane.OK_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE)==JOptionPane.OK_OPTION) scope.launch { repo.clear();toast.show("Unpinned history cleared") }
 }
 private fun close() {
  watcher.close();keys.close();panel.close();settings.close();tray.close();toast.close()
  scope.cancel();repo.close();instance.close();kotlin.system.exitProcess(0)
 }
}
