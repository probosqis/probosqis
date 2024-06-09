/*
 * Copyright 2024 wcaokaze
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

package com.wcaokaze.probosqis.error

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.wcaokaze.probosqis.resources.Strings
import com.wcaokaze.probosqis.resources.icons.Error

@Composable
fun PErrorActionButton(
   state: PErrorListState,
   onClick: () -> Unit
) {
   val anim = remember { Animatable(0.dp, Dp.VectorConverter) }

   LaunchedEffect(state.raisedTime) {
      if (state.raisedTime == null) { return@LaunchedEffect }

      anim.animateTo(-12.dp, animationSpec = tween(32, easing = LinearEasing))
      anim.animateTo(  0.dp, animationSpec = spring(Spring.DampingRatioHighBouncy))
   }

   IconButton(
      onClick,
      modifier = Modifier
         .onGloballyPositioned { state.buttonBounds = it.boundsInRoot() }
         .offset { IntOffset(anim.value.roundToPx(), 0) }
   ) {
      Icon(
         Icons.Default.Error,
         contentDescription = Strings.PError.pErrorActionButtonContentDescription
      )
   }
}
