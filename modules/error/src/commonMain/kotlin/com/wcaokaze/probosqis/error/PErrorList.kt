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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import com.wcaokaze.probosqis.resources.icons.Error
import kotlin.math.roundToInt

@Stable
class PErrorListState {
   internal var buttonBounds by mutableStateOf(Rect.Zero)

   var isShown by mutableStateOf(false)

   fun show() {
      isShown = true
   }

   fun hide() {
      isShown = false
   }
}

@Composable
fun PErrorList(state: PErrorListState) {
   Layout(
      content = {
         val tapDetectorModifier = Modifier.pointerInput(Unit) {
            awaitPointerEventScope {
               awaitFirstDown()
               state.hide()
            }
         }

         Box(
            Modifier
               .fillMaxSize()
               .then(
                  if (state.isShown) { tapDetectorModifier } else { Modifier }
               )
         )

         PErrorListContent(state)
      },
      measurePolicy = { measurables, constraints ->
         val backgroundPlaceable = measurables[0].measure(constraints)

         val startPadding = 36.dp.roundToPx()
         val endPadding = when (layoutDirection) {
            LayoutDirection.Ltr -> (constraints.maxWidth - state.buttonBounds.right).roundToInt()
            LayoutDirection.Rtl -> state.buttonBounds.left.roundToInt()
         }
         val topPadding = state.buttonBounds.top.roundToInt()
         val bottomPadding = 32.dp.roundToPx()

         val contentPlaceable = measurables[1].measure(
            constraints.offset(
               horizontal = -(startPadding + endPadding),
               vertical   = -(topPadding + bottomPadding)
            )
         )

         layout(constraints.maxWidth, constraints.maxHeight) {
            backgroundPlaceable.place(0, 0)

            contentPlaceable.place(
               x = when (layoutDirection) {
                  LayoutDirection.Ltr -> constraints.maxWidth - endPadding - contentPlaceable.width
                  LayoutDirection.Rtl -> endPadding
               },
               y = topPadding
            )
         }
      }
   )
}

private val iconRowHeight = 48.dp
private val iconHorizontalPadding = 16.dp

private fun <T> animSpec(easing: Easing = LinearEasing)
      = tween<T>(300, easing = easing)

@Composable
private fun PErrorListContent(state: PErrorListState) {
   val transition = updateTransition(state.isShown)

   val offset by transition.animateDp(
      label = "offset",
      transitionSpec = { animSpec() }
   ) { isShown -> if (isShown) { -4.dp } else { 0.dp } }

   val elevation by transition.animateDp(
      label = "elevation",
      transitionSpec = { animSpec() }
   ) { isShown -> if (isShown) { 3.dp } else { 0.dp } }

   transition.AnimatedContent(
      transitionSpec = {
         EnterTransition.None togetherWith ExitTransition.None using SizeTransform(
            clip = true,
            sizeAnimationSpec = { _, _ -> animSpec(FastOutSlowInEasing) }
         )
      },
      modifier = Modifier
         .offset(x = offset)
         .shadow(elevation, MaterialTheme.shapes.small)
         .then(
            if (elevation > 0.dp) {
               Modifier.background(MaterialTheme.colorScheme.surface)
            } else {
               Modifier
            }
         )
   ) { isShown ->
      if (!isShown) {
         Spacer(Modifier.size(iconRowHeight))
      } else {
         Column(
            modifier = Modifier
               .clip(MaterialTheme.shapes.small)
               .pointerInput(Unit) {}
         ) {
            Box(
               modifier = Modifier
                  .fillMaxWidth()
                  .height(iconRowHeight)
                  .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
            ) {
               val hiddenIconOffset = with (LocalDensity.current) {
                  val hiddenIconPadding = (iconRowHeight - Icons.Default.Error.defaultWidth) / 2
                  (hiddenIconPadding - iconHorizontalPadding).roundToPx()
               }

               @OptIn(ExperimentalAnimationApi::class)
               Icon(
                  Icons.Default.Error,
                  contentDescription = null,
                  modifier = Modifier
                     .align(Alignment.CenterStart)
                     .padding(horizontal = iconHorizontalPadding)
                     .animateEnterExit(
                        enter = slideInHorizontally (animSpec()) { hiddenIconOffset },
                        exit  = slideOutHorizontally(animSpec()) { hiddenIconOffset }
                     )
               )
            }

            Text(
               """
               Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do
               eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut
               enim ad minim veniam, quis nostrud exercitation ullamco laboris
               nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in
               reprehenderit in voluptate velit esse cillum dolore eu fugiat
               nulla pariatur. Excepteur sint occaecat cupidatat non proident,
               sunt in culpa qui officia deserunt mollit anim id est laborum
               """.trimIndent()
            )
         }
      }
   }
}
