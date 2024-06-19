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

import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.core.tween
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.awaitDragOrCancellation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign

@Composable
internal fun <E : PError> PErrorListItem(
   pError: E,
   itemComposable: @Composable (E) -> Unit,
   backgroundColor: Color
) {
   Box(
      contentAlignment = Alignment.CenterStart,
      modifier = Modifier
         .fillMaxWidth()
         .heightIn(min = 48.dp)
         .swipeDismiss()
         .background(backgroundColor)
   ) {
      itemComposable(pError)
   }
}

@Composable
private fun Modifier.swipeDismiss(): Modifier {
   val scrollingLogic = remember { ScrollingLogic() }
   val decaySpec = rememberSplineBasedDecay<Float>()
   val coroutineScope = rememberCoroutineScope()

   return pointerInput(Unit) {
         detectHorizontalDragGesture(
            shouldStartDragImmediately = { scrollingLogic.shouldStartDragImmediately() },
            onDrag = { dragAmount ->
               coroutineScope.launch {
                  scrollingLogic.scrollBy(dragAmount)
               }
            },
            onDragEnd = { velocity ->
               coroutineScope.launch {
                  scrollingLogic.settle(velocity, decaySpec, size.width.toFloat())
               }
            }
         )
      }
      .offset { IntOffset(scrollingLogic.offset.roundToInt(), 0) }
}

@Stable
private class ScrollingLogic {
   var offset by mutableFloatStateOf(0.0f)
      private set

   private val scrollState = ScrollableState {
      offset += it
      it
   }

   fun shouldStartDragImmediately() = scrollState.isScrollInProgress

   suspend fun scrollBy(offset: Float) {
      scrollState.scrollBy(offset)
   }

   suspend fun settle(
      initialVelocity: Float,
      decaySpec: DecayAnimationSpec<Float>,
      listWidth: Float
   ) {
      val settledOffset = decaySpec.calculateTargetValue(offset, initialVelocity)

      when {
         settledOffset < -(listWidth * 0.6f) -> settleTo(-listWidth, initialVelocity)
         settledOffset >   listWidth * 0.6f  -> settleTo( listWidth, initialVelocity)
         else                                -> settleToZero(initialVelocity)
      }
   }

   private suspend fun settleToZero(initialVelocity: Float) {
      scrollState.scroll {
         AnimationState(offset, initialVelocity).animateTo(0.0f) {
            offset = value
         }
      }
   }

   private suspend fun settleTo(
      targetOffset: Float,
      initialVelocity: Float
   ) {
      val velocityPxPerMillis = initialVelocity / 1000.0f
      val scrollOffset = targetOffset - offset
      val durationMillis = scrollOffset / velocityPxPerMillis
      scrollState.animateScrollBy(
         scrollOffset,
         tween(
            when {
               // scrollOffsetとvelocityPxPerMillisの向きが逆。
               // フリックと反対向きへスクロールするため算出したdurationは使えない
               durationMillis <   0.0f -> 500
               // フリックがおそすぎる
               durationMillis > 500.0f -> 500
               // 通常
               else                    -> durationMillis.roundToInt()
            },
            easing = LinearEasing
         )
      )
   }
}

private suspend fun PointerInputScope.detectHorizontalDragGesture(
   shouldStartDragImmediately: () -> Boolean,
   onDrag: (dragAmount: Float) -> Unit,
   onDragEnd: (velocity: Float) -> Unit
) {
   val velocityTracker = VelocityTracker()

   awaitEachGesture {
      val down = awaitFirstDown(requireUnconsumed = false)
      if (down.type == PointerType.Mouse) { return@awaitEachGesture }
      velocityTracker.addPointerInputChange(down)

      val pointerId = if (shouldStartDragImmediately()) {
         onDrag(0.0f)
         down.id
      } else {
         awaitPointerSlop(down, velocityTracker, onDrag)
               ?: return@awaitEachGesture
      }

      detectDrag(pointerId, velocityTracker, onDrag, onDragEnd)
   }
}

private suspend fun AwaitPointerEventScope.awaitPointerSlop(
   firstDown: PointerInputChange,
   velocityTracker: VelocityTracker,
   onDrag: (Float) -> Unit
): PointerId? {
   var down = firstDown

   val touchSlop = viewConfiguration.touchSlop
   while (true) {
      val event = awaitPointerEvent()
      val dragEvent = event.changes.firstOrNull { it.id == down.id }
      if (dragEvent == null || dragEvent.isConsumed) {
         velocityTracker.resetTracking()
         return null
      }

      velocityTracker.addPointerInputChange(dragEvent)

      if (dragEvent.changedToUpIgnoreConsumed()) {
         val alternativeDown = event.changes
            .firstOrNull { it.pressed && it.type != PointerType.Mouse }
         if (alternativeDown == null) {
            velocityTracker.resetTracking()
            return null
         }

         down = alternativeDown
         velocityTracker.resetTracking()
         velocityTracker.addPointerInputChange(alternativeDown)

         continue
      }

      val xDragOffset = dragEvent.position.x - down.position.x
      val yDragOffset = dragEvent.position.y - down.position.y

      when {
         abs(xDragOffset) > touchSlop -> {
            dragEvent.consume()
            onDrag(xDragOffset - sign(xDragOffset) * touchSlop)
            return down.id
         }
         abs(yDragOffset) > touchSlop -> return null
         else -> continue
      }
   }
}

private suspend fun AwaitPointerEventScope.detectDrag(
   sloppedPointerId: PointerId,
   velocityTracker: VelocityTracker,
   onDrag: (Float) -> Unit,
   onDragEnd: (Float) -> Unit
) {
   var pointerId = sloppedPointerId

   while (true) {
      val change = awaitDragOrCancellation(pointerId)
      if (change == null) {
         val velocity = velocityTracker.calculateVelocity()
         velocityTracker.resetTracking()
         onDragEnd(velocity.x)
         return
      }

      velocityTracker.addPointerInputChange(change)

      if (change.changedToUpIgnoreConsumed()) {
         val velocity = velocityTracker.calculateVelocity()
         velocityTracker.resetTracking()
         onDragEnd(velocity.x)
         return
      }

      onDrag(change.positionChange().x)
      pointerId = change.id
   }
}
