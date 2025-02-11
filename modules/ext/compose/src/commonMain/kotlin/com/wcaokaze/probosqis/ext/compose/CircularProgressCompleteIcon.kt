/*
 * Copyright 2025 wcaokaze
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wcaokaze.probosqis.ext.compose

import androidx.compose.animation.core.ExperimentalTransitionApi
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.time.Duration
import kotlin.time.DurationUnit

private val p1 = Offset(16.83f, 25.410f)
private val p2 = Offset(21.00f, 29.585f)
private val p3 = Offset(33.00f, 19.000f)

private val d1 = (p2 - p1).getDistance()
private val d2 = (p3 - p2).getDistance()
private val totalD = d1 + d2

@Composable
fun CircularProgressCompleteIcon(
   modifier: Modifier = Modifier,
   tint: Color = ProgressIndicatorDefaults.circularColor,
   strokeWidth: Dp = ProgressIndicatorDefaults.CircularStrokeWidth,
   animDelay: Duration = Duration.ZERO,
) {
   val transitionState = remember {
      MutableTransitionState(false).also { it.targetState = true }
   }

   @OptIn(ExperimentalTransitionApi::class)
   val transition = rememberTransition(transitionState, label = "CircularProgressCompleteIcon")

   val f by transition.animateFloat(
      transitionSpec = {
         tween(
            durationMillis = 500,
            delayMillis = animDelay.toInt(DurationUnit.MILLISECONDS),
            easing = LinearOutSlowInEasing
         )
      },
      targetValueByState = { if (it) { totalD } else { 0.0f } }
   )

   Canvas(
      modifier = modifier
         .size(40.dp)
   ) {
      val xs = size.width  / 48.0f
      val ys = size.height / 48.0f

      val strokeWidthPx = strokeWidth.toPx()

      when {
         f <= 0.0f -> {
         }

         f <= d1 -> {
            val p1 = Offset(p1.x * xs, p1.y * ys)
            val p2 = Offset(p2.x * xs, p2.y * ys)

            drawLine(
               color = tint,
               start = p1,
               end = p1 + (p2 - p1) * (f / d1),
               strokeWidthPx,
               StrokeCap.Square
            )
         }

         f < totalD -> {
            val p1 = Offset(p1.x * xs, p1.y * ys)
            val p2 = Offset(p2.x * xs, p2.y * ys)
            val p3 = Offset(p3.x * xs, p3.y * ys)

            drawLine(color = tint, start = p1, end = p2, strokeWidthPx, StrokeCap.Square)

            drawLine(
               color = tint,
               start = p2,
               end = p2 + (p3 - p2) * ((f - d1) / d2),
               strokeWidthPx,
               StrokeCap.Square
            )
         }

         else -> {
            val p1 = Offset(p1.x * xs, p1.y * ys)
            val p2 = Offset(p2.x * xs, p2.y * ys)
            val p3 = Offset(p3.x * xs, p3.y * ys)

            drawLine(color = tint, start = p1, end = p2, strokeWidthPx, StrokeCap.Square)
            drawLine(color = tint, start = p2, end = p3, strokeWidthPx, StrokeCap.Square)
         }
      }
   }
}
