package com.zunawet.clipvault.ui.settings
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import com.zunawet.clipvault.data.repository.VaultRepository
import com.zunawet.clipvault.platform.windows.GlobalHotkeyManager
import com.zunawet.clipvault.domain.model.*
import com.zunawet.clipvault.ui.theme.*
import com.zunawet.clipvault.ui.components.*
import com.zunawet.clipvault.utils.AppPaths
import kotlinx.coroutines.*
import java.awt.Desktop

@Composable fun SettingsScreen(repo: VaultRepository,hotkeys: GlobalHotkeyManager,initialPage: String,clear: () -> Unit) {
 val scope=rememberCoroutineScope()
 var page by remember(initialPage) { mutableStateOf(initialPage) }
 val prefs by repo.settings.collectAsState();val clips by repo.clips.collectAsState()
 val bindings by repo.keybinds.collectAsState();val recording by hotkeys.recording.collectAsState()
 val errors by hotkeys.error.collectAsState();val backend by hotkeys.backend.collectAsState()
 val reduced=repo.enabled("reducedMotion")
 val shake=remember { Animatable(0f) }
 LaunchedEffect(errors) { if(errors>0 && !reduced) shake.animateTo(0f,keyframes { durationMillis=200;0f at 0;8f at 35;-6f at 75;4f at 115;-2f at 155;0f at 200 }) }
 fun setting(key: String,value: String) { scope.launch { repo.setting(key,value) } }
 Row(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
  Column(Modifier.width(240.dp).fillMaxHeight().background(if(prefs["theme"]=="dark") MaterialTheme.colorScheme.surface else Cream)) {
   Row(Modifier.fillMaxWidth().padding(horizontal=28.dp,vertical=32.dp),verticalAlignment=Alignment.CenterVertically) {
    Brand();Spacer(Modifier.width(12.dp));Text("ClipVault",fontSize=22.sp,fontWeight=FontWeight.Bold)
   }
   HorizontalDivider(color=Color(0xFFF5E6A8));Spacer(Modifier.height(18.dp))
   val nav=listOf("Home" to Icons.Outlined.Home,"Keybinds" to Icons.Outlined.Keyboard,"Appearance" to Icons.Outlined.Palette,"Notifications" to Icons.Outlined.Notifications,"Cloud Sync" to Icons.Outlined.CloudQueue,"About" to Icons.Outlined.Info)
   nav.forEach { (label,icon) ->
    val active=page==label
    Row(Modifier.fillMaxWidth().heightIn(min=52.dp).background(if(active) Yellow.copy(alpha=.3f) else Color.Transparent).clickable { if(page!=label) hotkeys.cancel();page=label },verticalAlignment=Alignment.CenterVertically) {
     Box(Modifier.width(3.dp).height(36.dp).background(if(active) Yellow else Color.Transparent))
     Spacer(Modifier.width(25.dp));Icon(icon,null,Modifier.size(20.dp),tint=if(active) Amber else Muted);Spacer(Modifier.width(14.dp))
     Text(label,fontSize=14.sp,fontWeight=if(active) FontWeight.SemiBold else FontWeight.Normal)
    }
   }
   Spacer(Modifier.weight(1f))
   Column(Modifier.padding(28.dp)) { Text("Made by Zunawet",fontSize=12.sp,color=Muted);Spacer(Modifier.height(5.dp));Caption("v1.0.0 · Build 001") }
  }
  VerticalDivider(color=MaterialTheme.colorScheme.outline.copy(alpha=.4f))
  Column(Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(horizontal=40.dp,vertical=32.dp)) {
   Text(page,style=MaterialTheme.typography.headlineMedium)
   Spacer(Modifier.height(7.dp))
   Text(when(page) { "Keybinds" -> "Customize shortcuts for ClipVault actions";"Home" -> "Everything you copied. Forever. Searchable.";"Appearance" -> "Make a little space feel like yours.";"Notifications" -> "Stay informed, without the interruption.";"Cloud Sync" -> "Your clips belong to you.";else -> "A thoughtful home for your clipboard." },color=Muted)
   Spacer(Modifier.height(30.dp))
   when(page) {
    "Keybinds" -> {
     if(recording!=null) Surface(color=PaleYellow,shape=RoundedCornerShape(10.dp)) {
      Row(Modifier.fillMaxWidth().padding(16.dp),verticalAlignment=Alignment.CenterVertically) {
       Text("Press keys… Esc to cancel",Modifier.weight(1f),color=Ink)
       TextButton({hotkeys.cancel()}) { Text("Cancel",color=Amber) }
      }
     }
     val names=mapOf("panel" to "Open ClipVault","overlay" to "Overlay picker","pause" to "Pause / Resume capture","plain" to "Paste as plain text","recent1" to "Paste 1st recent","recent2" to "Paste 2nd recent","recent3" to "Paste 3rd recent")
     listOf("GENERAL" to listOf("panel","overlay","pause","plain"),"QUICK PASTE" to listOf("recent1","recent2","recent3")).forEach { (section,actions) ->
      SectionTitle(section)
      SettingsCard {
       actions.forEachIndexed { i,action ->
        val b=bindings.first { it.action==action };val active=recording==action
        val pulse=rememberInfiniteTransition()
        val glow by pulse.animateFloat(.35f,.95f,infiniteRepeatable(tween(600),RepeatMode.Reverse))
        Row(Modifier.fillMaxWidth().heightIn(min=64.dp).padding(horizontal=20.dp,vertical=8.dp),verticalAlignment=Alignment.CenterVertically) {
         Text(names[action]!!,Modifier.weight(1f),fontWeight=FontWeight.SemiBold)
         Row(Modifier.graphicsLayer { translationX=if(active) shake.value else 0f }.clipShape().background(if(active) Yellow.copy(alpha=if(reduced) 1f else glow) else MaterialTheme.colorScheme.surfaceVariant).clickable { hotkeys.record(action) }.padding(horizontal=14.dp,vertical=12.dp),verticalAlignment=Alignment.CenterVertically) {
          Text(if(active) "Press keys…" else b.label,fontSize=13.sp,fontFamily=FontFamily.Monospace,color=if(active) Ink else MaterialTheme.colorScheme.onSurface)
         }
         IconButton({hotkeys.record(action)},Modifier.size(44.dp)) { Icon(Icons.Outlined.Edit,"Record ${names[action]}",Modifier.size(18.dp),tint=Muted) }
        }
        if(i<actions.lastIndex) HorizontalDivider(color=MaterialTheme.colorScheme.outline.copy(alpha=.5f))
       }
      }
     }
     SectionTitle("ADVANCED")
     SettingsCard {
      ToggleRow("Double-tap Ctrl to open",repo.enabled("doubleCtrl")) { setting("doubleCtrl",it.toString()) }
      ToggleRow("Open at cursor position",repo.enabled("cursor",true)) { setting("cursor",it.toString()) }
      ToggleRow("Play sound on capture",repo.enabled("sound")) { setting("sound",it.toString()) }
     }
     Spacer(Modifier.height(16.dp))
     TextButton({hotkeys.cancel();scope.launch { repo.resetBindings() }}) { Text("Reset to Defaults",color=Amber,fontWeight=FontWeight.SemiBold) }
     Caption("Hotkey backend: $backend · Recorder times out after 10 seconds.")
    }
    "Home" -> {
     Row(horizontalArrangement=Arrangement.spacedBy(16.dp)) {
      Metric("${clips.size}","Saved clips",Modifier.weight(1f));Metric("${clips.count { it.pinned }}","Pinned",Modifier.weight(1f));Metric("Offline","Private by default",Modifier.weight(1f))
     }
     SectionTitle("CAPTURE & PRIVACY")
     SettingsCard {
      Row(Modifier.padding(20.dp)) { Column { Text("Sensitive content is skipped",fontWeight=FontWeight.SemiBold);Spacer(Modifier.height(6.dp));Text("Suspected passwords and payment cards are never saved. Detection is best-effort: exclude password managers and sensitive apps below.",color=Muted,fontSize=12.sp) } }
      ToggleRow("Bengali OCR (with English)",repo.enabled("bengali")) { setting("bengali",it.toString()) }
     }
     SectionTitle("EXCLUDED APPLICATIONS")
     val excluded by repo.excluded.collectAsState()
     var text by remember(excluded) { mutableStateOf(excluded.joinToString("\n")) }
     OutlinedTextField(text,{text=it},Modifier.fillMaxWidth(),label={Text("Process names · one per line")},placeholder={Text("1Password.exe\nKeePass.exe")},minLines=3,shape=RoundedCornerShape(12.dp))
     Spacer(Modifier.height(8.dp));Button({scope.launch { repo.exclusions(text.lines()) }}) { Text("Save exclusions") }
     SectionTitle("LOCAL STORAGE")
     Text(AppPaths.root.toString(),fontFamily=FontFamily.Monospace,fontSize=12.sp,color=Muted)
     Row { TextButton({Desktop.getDesktop().open(AppPaths.root.toFile())}) { Text("Open data folder",color=Amber) };TextButton(clear) { Text("Clear unpinned history…",color=Color(0xFFEF4444)) } }
     Caption("History is not encrypted. Protect your Windows account and use BitLocker. No network services run in this app.")
    }
    "Appearance" -> {
     SectionTitle("THEME")
     Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
      listOf("light","dark").forEach { theme ->
       OutlinedButton({setting("theme",theme)},border=BorderStroke(if((prefs["theme"] ?: "light")==theme) 2.dp else 1.dp,if((prefs["theme"] ?: "light")==theme) Yellow else Line),shape=RoundedCornerShape(12.dp)) { Text(theme.replaceFirstChar(Char::uppercase),color=MaterialTheme.colorScheme.onSurface) }
      }
     }
     SectionTitle("ACCESSIBILITY")
     SettingsCard { ToggleRow("Reduce motion",repo.enabled("reducedMotion")) { setting("reducedMotion",it.toString()) } }
     Spacer(Modifier.height(12.dp));Caption("The Windows reduced-motion preference is also respected by popup transitions.")
     Spacer(Modifier.height(24.dp));Text("Designed to feel familiar.",fontWeight=FontWeight.SemiBold);Spacer(Modifier.height(8.dp));Text("Warm cream surfaces, yellow accents, and Segoe UI. Native Windows display scaling is supported.",color=Muted)
    }
    "Notifications" -> {
     SectionTitle("CAPTURE FEEDBACK")
     SettingsCard {
      ToggleRow("Desktop capture notifications",repo.enabled("notifications")) { setting("notifications",it.toString()) }
      ToggleRow("Play sound on capture",repo.enabled("sound")) { setting("sound",it.toString()) }
     }
     Spacer(Modifier.height(14.dp));Text("Notifications are off by default to keep copied content private. Enabling them may expose clip previews on screen. Windows controls delivery through its shell notification settings.",color=Muted,fontSize=12.sp)
    }
    "Cloud Sync" -> {
     SettingsCard { Column(Modifier.padding(24.dp)) {
      Icon(Icons.Outlined.CloudOff,null,Modifier.size(36.dp),tint=Yellow);Spacer(Modifier.height(16.dp));Text("Local first. Local only.",style=MaterialTheme.typography.titleLarge)
      Spacer(Modifier.height(12.dp));Text("Cloud sync and automatic updates are not included in v1.0. No account, analytics, or server is required.",color=Muted)
      Spacer(Modifier.height(16.dp));Caption("Planned for v1.1: encrypted backups to your own server.")
     } }
    }
    "About" -> {
     SettingsCard { Column(Modifier.padding(28.dp)) {
      Brand(56);Spacer(Modifier.height(20.dp));Text("ClipVault",style=MaterialTheme.typography.headlineMedium);Spacer(Modifier.height(6.dp));Text("Everything you copied. Forever. Searchable.",color=Muted)
      Spacer(Modifier.height(24.dp));Text("Made by Zunawet",color=Amber,fontWeight=FontWeight.SemiBold);Caption("Version 1.0.0 · Build 001 · Windows 10 / 11")
      Spacer(Modifier.height(20.dp));Text("Built with Kotlin, JetBrains Compose, SQLDelight, JNativeHook, JNA and Tesseract.",fontSize=12.sp,color=Muted)
     } }
    }
   }
  }
 }
}
private fun Modifier.clipShape()=this.clip(RoundedCornerShape(8.dp))
@Composable private fun SectionTitle(text: String) { Text(text,Modifier.padding(top=20.dp,bottom=10.dp),fontSize=11.sp,fontWeight=FontWeight.Bold,letterSpacing=1.sp,color=Muted) }
@Composable private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) { Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(12.dp),border=BorderStroke(1.dp,MaterialTheme.colorScheme.outline.copy(alpha=.7f))) { Column(content=content) } }
@Composable private fun ToggleRow(label: String,value: Boolean,change: (Boolean) -> Unit) {
 Row(Modifier.fillMaxWidth().heightIn(min=64.dp).padding(horizontal=20.dp,vertical=8.dp),verticalAlignment=Alignment.CenterVertically) {
  Text(label,Modifier.weight(1f),fontWeight=FontWeight.Medium)
  Switch(value,change,colors=SwitchDefaults.colors(checkedTrackColor=Yellow,checkedThumbColor=Color.White,uncheckedTrackColor=Color(0xFF9CA3AF),uncheckedThumbColor=Color.White,uncheckedBorderColor=Color.Transparent))
 }
}
@Composable private fun Metric(value: String,label: String,modifier: Modifier) { Surface(modifier,shape=RoundedCornerShape(12.dp),color=Yellow.copy(alpha=.13f)) { Column(Modifier.padding(20.dp)) { Text(value,style=MaterialTheme.typography.headlineMedium);Spacer(Modifier.height(6.dp));Caption(label) } } }
