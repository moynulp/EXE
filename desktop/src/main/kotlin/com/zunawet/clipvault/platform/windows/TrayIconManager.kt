package com.zunawet.clipvault.platform.windows
import java.awt.*
import javax.imageio.ImageIO
class TrayIconManager(private val action: (String) -> Unit): AutoCloseable {
 private var icon: TrayIcon?=null
 private var pause: CheckboxMenuItem?=null
 fun install() {
  check(SystemTray.isSupported()) { "A Windows desktop session with a system tray is required" }
  val image=ImageIO.read(javaClass.getResource("/icons/clipvault-32.png"))
  val menu=PopupMenu()
  fun item(label: String,id: String) { menu.add(MenuItem(label).apply { addActionListener { action(id) } }) }
  item("Open ClipVault","panel");item("Open Overlay","overlay");menu.addSeparator()
  pause=CheckboxMenuItem("Pause capture").apply { addItemListener { action("pause") } };menu.add(pause)
  item("Clear unpinned history…","clear");menu.addSeparator();item("Settings…","settings");item("About · Made by Zunawet","about");menu.addSeparator();item("Quit","quit")
  icon=TrayIcon(image,"ClipVault · Made by Zunawet",menu).apply { isImageAutoSize=true;addActionListener { action("panel") } }
  SystemTray.getSystemTray().add(icon)
 }
 fun paused(value: Boolean) {
  pause?.state=value
  icon?.toolTip=if(value) "ClipVault · Capture paused" else "ClipVault · Made by Zunawet"
  val image=ImageIO.read(javaClass.getResource("/icons/clipvault-${if(value) "paused" else "32"}.png"))
  icon?.image=image
 }
 /** AWT maps this to shell notification UI. Not a WinRT toast activation implementation. */
 fun notification(text: String) { icon?.displayMessage("ClipVault", "Copied: $text",TrayIcon.MessageType.NONE) }
 override fun close() { icon?.let { SystemTray.getSystemTray().remove(it) } }
}
