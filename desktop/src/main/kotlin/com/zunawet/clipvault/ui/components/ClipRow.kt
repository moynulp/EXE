package com.zunawet.clipvault.ui.components
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.zunawet.clipvault.domain.model.*
import com.zunawet.clipvault.ui.theme.*
import com.zunawet.clipvault.utils.relativeTime
import kotlinx.coroutines.*
import org.jetbrains.skia.Image
import java.io.File

@Composable fun ClipRow(c: Clip, selected: Boolean, query: String, busy: Boolean, paste: () -> Unit, pin: () -> Unit, delete: () -> Unit) {
 val interaction=remember { MutableInteractionSource() };val hover by interaction.collectIsHoveredAsState()
 val bg by animateColorAsState(if(selected) Yellow else if(hover) PaleYellow else Color.Transparent,tween(120))
 val icon: ImageVector=when(c.type) {
  ClipType.CODE -> Icons.Outlined.Code;ClipType.LINK -> Icons.Outlined.Link;ClipType.IMAGE -> Icons.Outlined.Image
  ClipType.COLOR -> Icons.Outlined.Palette;ClipType.EMAIL -> Icons.Outlined.AlternateEmail;ClipType.FILE -> Icons.Outlined.Folder;else -> Icons.Outlined.Description
 }
 val tint=when(c.type) { ClipType.CODE -> Color(0xFF8B5CF6);ClipType.IMAGE -> Color(0xFFF59E0B);ClipType.COLOR -> Color(0xFF10B981);ClipType.EMAIL -> Color(0xFFEC4899);ClipType.FILE -> Muted;else -> Color(0xFF3B82F6) }
 val thumb by produceState<ImageBitmap?>(null,c.id) {
  if(c.type==ClipType.IMAGE) value=withContext(Dispatchers.IO) { runCatching { Image.makeFromEncoded(File(c.content.replace(".png","-thumb.png")).readBytes()).toComposeImageBitmap() }.getOrNull() }
 }
 Row(Modifier.fillMaxWidth().heightIn(min=52.dp).clip(RoundedCornerShape(10.dp)).background(bg).hoverable(interaction).clickable(interactionSource=interaction,indication=null,onClick=paste).padding(start=10.dp,end=4.dp,top=4.dp,bottom=4.dp).semantics { contentDescription="${c.type.name}: ${c.preview}. ${if(c.pinned) "Pinned" else ""}" },verticalAlignment=Alignment.CenterVertically) {
  Box(Modifier.size(32.dp).background(tint.copy(alpha=.15f),RoundedCornerShape(8.dp)),contentAlignment=Alignment.Center) {
   if(thumb!=null) Image(thumb!!,"Image thumbnail",Modifier.size(32.dp).clip(RoundedCornerShape(8.dp))) else Icon(icon,null,Modifier.size(17.dp),tint=tint)
  }
  Spacer(Modifier.width(10.dp))
  Column(Modifier.weight(1f)) {
   val annotated=remember(c.preview,query) {
    buildAnnotatedString {
     append(c.preview)
     if(query.isNotBlank()) { var start=c.preview.indexOf(query,ignoreCase=true)
      while(start>=0) { addStyle(SpanStyle(background=Yellow),start,start+query.length);start=c.preview.indexOf(query,start+query.length,ignoreCase=true) }
     }
    }
   }
   Text(annotated,maxLines=1,overflow=TextOverflow.Ellipsis,color=if(selected||hover) Ink else MaterialTheme.colorScheme.onSurface,fontSize=14.sp)
   if(busy) LinearProgressIndicator(Modifier.fillMaxWidth().height(2.dp),color=Color(0xFF10B981))
   else if(c.type==ClipType.IMAGE && c.ocr!=null) Text("✓ Text recognized",fontSize=10.sp,color=Color(0xFF059669))
  }
  if(hover) {
   IconButton(pin,Modifier.size(44.dp)) { Icon(Icons.Outlined.PushPin,if(c.pinned) "Unpin clip" else "Pin clip",Modifier.size(15.dp),tint=if(c.pinned) Amber else Muted) }
   IconButton(delete,Modifier.size(44.dp)) { Icon(Icons.Outlined.Delete,"Delete clip",Modifier.size(15.dp),tint=Muted) }
  } else {
   if(c.pinned) Icon(Icons.Outlined.PushPin,"Pinned",Modifier.padding(horizontal=4.dp).size(12.dp),tint=Amber)
   Text(relativeTime(c.createdAt),Modifier.padding(horizontal=8.dp),fontSize=11.sp,color=Muted)
  }
 }
}
