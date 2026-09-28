@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
package com.zunawet.clipvault.ui.theme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.sp
val Yellow=Color(0xFFFBBF24)
val Cream=Color(0xFFFFF8E1)
val PaleYellow=Color(0xFFFEF3C7)
val Ink=Color(0xFF111827)
val Muted=Color(0xFF6B7280)
val Line=Color(0xFFE5E7EB)
val Amber=Color(0xFFB45309)
private val family=FontFamily("Segoe UI")
@Composable fun VaultTheme(dark: Boolean=false, content: @Composable () -> Unit) {
 val palette=if(dark) darkColorScheme(primary=Yellow,onPrimary=Ink,background=Color(0xFF0F172A),surface=Color(0xFF1E293B),onSurface=Color(0xFFF8FAFC),surfaceVariant=Color(0xFF334155),onSurfaceVariant=Color(0xFF94A3B8))
 else lightColorScheme(primary=Yellow,onPrimary=Ink,background=Color.White,surface=Color.White,onSurface=Ink,onSurfaceVariant=Muted,surfaceVariant=Color(0xFFF3F4F6),outline=Line,secondary=Amber)
 MaterialTheme(colorScheme=palette,typography=Typography(
  headlineMedium=TextStyle(fontFamily=family,fontSize=24.sp,fontWeight=FontWeight.Bold),
  titleLarge=TextStyle(fontFamily=family,fontSize=20.sp,fontWeight=FontWeight.Bold),
  titleMedium=TextStyle(fontFamily=family,fontSize=16.sp,fontWeight=FontWeight.SemiBold),
  bodyLarge=TextStyle(fontFamily=family,fontSize=14.sp),bodyMedium=TextStyle(fontFamily=family,fontSize=14.sp),bodySmall=TextStyle(fontFamily=family,fontSize=12.sp),labelSmall=TextStyle(fontFamily=family,fontSize=11.sp)),content=content)
}
