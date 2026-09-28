@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
package com.zunawet.clipvault.ui.panel
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.zunawet.clipvault.domain.model.Clip
import com.zunawet.clipvault.ui.theme.*
import com.zunawet.clipvault.ui.components.*
import com.zunawet.clipvault.ui.animations.AnimationSpecs

@Composable fun PanelScreen(vm: PanelViewModel, visible: Boolean, overlay: Boolean, reduced: Boolean,
 busy: Set<String>, onSearchFocus: () -> Unit, onPaste: (Clip) -> Unit, onPin: (Clip) -> Unit,
 onDelete: (Clip) -> Unit, onHide: () -> Unit, settings: () -> Unit) {
 val query by vm.query.collectAsState();val category by vm.category.collectAsState()
 val clips by vm.results.collectAsState();val selected by vm.selected.collectAsState();val limit by vm.limit.collectAsState()
 val focus=remember { FocusRequester() };var focused by remember { mutableStateOf(false) }
 val list=rememberLazyListState()
 val duration=AnimationSpecs.duration(reduced,if(visible) 320 else 180)
 val alpha by animateFloatAsState(if(visible) 1f else 0f,tween(duration,easing=if(visible) AnimationSpecs.appear else AnimationSpecs.disappear))
 val scale by animateFloatAsState(if(visible||reduced) 1f else .92f,tween(duration,easing=AnimationSpecs.appear))
 val translation by animateFloatAsState(if(visible||reduced) 0f else 12f,tween(duration))
 LaunchedEffect(selected) { if(clips.isNotEmpty()) list.animateScrollToItem(selected.coerceAtMost(clips.lastIndex)) }
 val dark=MaterialTheme.colorScheme.surface!=Color.White
 val bg=if(dark) MaterialTheme.colorScheme.surface else Cream
 Surface(Modifier.padding(12.dp).fillMaxSize().graphicsLayer { this.alpha=alpha;scaleX=scale;scaleY=scale;translationY=translation }.onPreviewKeyEvent {
  if(it.type!=KeyEventType.KeyDown) false else when(it.key) {
   Key.Escape -> { onHide();true };Key.DirectionDown -> { vm.move(1);true };Key.DirectionUp -> { vm.move(-1);true }
   Key.Enter -> { clips.getOrNull(selected)?.let(onPaste);true };else -> false
  }
 },shape=RoundedCornerShape(14.dp),color=bg,shadowElevation=12.dp,border=BorderStroke(1.dp,if(dark) MaterialTheme.colorScheme.outline else Color(0xFFF5E6A8))) {
  Column {
   Box(Modifier.padding(14.dp).fillMaxWidth().heightIn(min=44.dp).shadow(if(focused) 4.dp else 0.dp,RoundedCornerShape(12.dp),ambientColor=Yellow,spotColor=Yellow).background(MaterialTheme.colorScheme.surface,RoundedCornerShape(12.dp)).border(if(focused) 1.5.dp else 1.dp,if(focused) Yellow else Line,RoundedCornerShape(12.dp)).onPointerEvent(PointerEventType.Press) { onSearchFocus();focus.requestFocus() }.padding(horizontal=12.dp,vertical=12.dp)) {
    Row(verticalAlignment=Alignment.CenterVertically) {
     Icon(Icons.Outlined.Search,null,Modifier.size(20.dp),tint=Muted);Spacer(Modifier.width(10.dp))
     BasicTextField(query,{vm.query.value=it},Modifier.weight(1f).focusRequester(focus).onFocusChanged { focused=it.isFocused },singleLine=true,textStyle=TextStyle(color=MaterialTheme.colorScheme.onSurface,fontSize=14.sp),cursorBrush=SolidColor(Yellow),decorationBox={ inner -> if(query.isEmpty()) Text("Search clips…",color=Muted,fontSize=14.sp);inner() })
     Text("/",Modifier.background(MaterialTheme.colorScheme.surfaceVariant,RoundedCornerShape(4.dp)).padding(horizontal=6.dp),color=Muted,fontSize=11.sp)
    }
   }
   if(!overlay) {
    Row(Modifier.fillMaxWidth().background(if(dark) MaterialTheme.colorScheme.surfaceVariant else PaleYellow).horizontalScroll(rememberScrollState()).padding(horizontal=10.dp,vertical=4.dp),horizontalArrangement=Arrangement.spacedBy(3.dp)) {
     listOf("Recent","Pinned","Text","Code","Links","Images").forEach { tab ->
      val active=category==tab
      Column(Modifier.clip(RoundedCornerShape(7.dp)).background(if(active) Yellow.copy(alpha=.35f) else Color.Transparent).clickable { vm.category.value=tab }.padding(horizontal=10.dp),horizontalAlignment=Alignment.CenterHorizontally) {
       Box(Modifier.height(42.dp),contentAlignment=Alignment.Center) { Text(tab,fontSize=12.sp,color=if(active) Amber else MaterialTheme.colorScheme.onSurfaceVariant,fontWeight=if(active) FontWeight.SemiBold else FontWeight.Normal) }
       Box(Modifier.fillMaxWidth().height(2.dp).background(if(active) Yellow else Color.Transparent))
      }
     }
    }
   }
   if(clips.isEmpty()) Column(Modifier.weight(1f).fillMaxWidth().padding(24.dp),verticalArrangement=Arrangement.Center,horizontalAlignment=Alignment.CenterHorizontally) {
    Icon(Icons.Outlined.ContentPaste,null,Modifier.size(36.dp),tint=Yellow);Spacer(Modifier.height(12.dp))
    Text(if(query.isBlank()) "A little space for everything." else "No clips found",fontWeight=FontWeight.SemiBold)
    Spacer(Modifier.height(8.dp));Caption(if(query.isBlank()) "Copy something to start your collection." else "Try another word or category.")
   } else LazyColumn(Modifier.weight(1f).padding(8.dp),state=list,verticalArrangement=Arrangement.spacedBy(4.dp)) {
    items(clips.take(limit),key={it.id}) { c ->
     Box(Modifier.animateItemPlacement(if(reduced) snap() else spring(dampingRatio=.75f,stiffness=400f))) {
      ClipRow(c,c.id==clips.getOrNull(selected)?.id,query,c.id in busy,{onPaste(c)},{onPin(c)},{onDelete(c)})
     }
    }
    if(clips.size>limit) item { TextButton({vm.limit.value+=50},Modifier.fillMaxWidth()) { Text("Load 50 more",color=Amber) } }
   }
   Row(Modifier.fillMaxWidth().background(if(dark) MaterialTheme.colorScheme.surfaceVariant else PaleYellow).padding(start=14.dp,end=6.dp,top=6.dp,bottom=6.dp),verticalAlignment=Alignment.CenterVertically) {
    Column(Modifier.weight(1f)) {
     Caption("↑↓ Navigate · Enter Paste · Esc Close")
     Text("Made by Zunawet",fontSize=11.sp,color=if(dark) Yellow else Amber,fontWeight=FontWeight.SemiBold)
    }
    IconButton(settings,Modifier.size(44.dp)) { Icon(Icons.Outlined.Settings,"Open settings",Modifier.size(18.dp),tint=Muted) }
   }
  }
 }
}
