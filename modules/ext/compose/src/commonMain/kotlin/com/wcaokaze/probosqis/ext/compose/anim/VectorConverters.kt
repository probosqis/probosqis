package com.wcaokaze.probosqis.ext.compose.anim

import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.ui.layout.ScaleFactor

val ScaleFactor.Companion.VectorConverter: TwoWayConverter<ScaleFactor, AnimationVector2D>
   get() = TwoWayConverter(
      convertToVector = { AnimationVector2D(it.scaleX, it.scaleY) },
      convertFromVector = { ScaleFactor(it.v1, it.v2) }
   )
