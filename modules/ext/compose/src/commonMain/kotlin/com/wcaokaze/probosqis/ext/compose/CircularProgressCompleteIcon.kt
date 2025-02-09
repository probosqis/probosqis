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

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CircularProgressCompleteIcon(
   modifier: Modifier = Modifier,
   tint: Color = ProgressIndicatorDefaults.circularColor,
   strokeWidth: Dp = ProgressIndicatorDefaults.CircularStrokeWidth,
) {
   Canvas(
      modifier = modifier
         .size(40.dp)
   ) {
      val xs = size.width  / 48.0f
      val ys = size.height / 48.0f

      val p1 = Offset(16.83f * xs, 25.410f * ys)
      val p2 = Offset(21.00f * xs, 29.585f * ys)
      val p3 = Offset(33.00f * xs, 19.000f * ys)

      val strokeWidthPx = strokeWidth.toPx()
      drawLine(tint, p1, p2, strokeWidthPx, cap = StrokeCap.Square)
      drawLine(tint, p2, p3, strokeWidthPx, cap = StrokeCap.Square)
   }
}
