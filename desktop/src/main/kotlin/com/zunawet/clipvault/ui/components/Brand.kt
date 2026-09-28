package com.zunawet.clipvault.ui.components
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.zunawet.clipvault.ui.theme.*
@Composable fun Brand(size: Int=32) {
 Box(Modifier.size(size.dp).background(Yellow,RoundedCornerShape((size/4).dp)),contentAlignment=Alignment.Center) {
  Text("✓",color=Color.White,fontSize=(size*.67).sp,fontWeight=FontWeight.Bold)
  Text("Z",Modifier.align(Alignment.BottomEnd).padding(end=3.dp,bottom=1.dp),color=Color.White,fontSize=(size*.25).sp,fontWeight=FontWeight.Bold)
 }
}
@Composable fun Caption(text: String,modifier: Modifier=Modifier) { Text(text,modifier,color=MaterialTheme.colorScheme.onSurfaceVariant,fontSize=11.sp) }
