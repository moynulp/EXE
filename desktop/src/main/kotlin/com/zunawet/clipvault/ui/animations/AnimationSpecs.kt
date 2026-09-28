package com.zunawet.clipvault.ui.animations
import androidx.compose.animation.core.*
object AnimationSpecs {
 val appear=CubicBezierEasing(0f,0f,.2f,1f)
 val disappear=CubicBezierEasing(.4f,0f,1f,1f)
 val standard=CubicBezierEasing(.4f,0f,.2f,1f)
 fun duration(reduced: Boolean,normal: Int)=if(reduced) 100 else normal
}
