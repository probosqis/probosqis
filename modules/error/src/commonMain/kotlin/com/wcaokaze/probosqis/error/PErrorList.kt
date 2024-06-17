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
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.awaitDragOrCancellation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
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
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import com.wcaokaze.probosqis.panoptiqon.WritableCache
import com.wcaokaze.probosqis.panoptiqon.compose.asMutableState
import com.wcaokaze.probosqis.resources.icons.Error
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlin.reflect.KClass

inline fun <reified E : PError> PErrorItemComposable(
   noinline composable: @Composable (E) -> Unit
) = PErrorItemComposable(E::class, composable)

@Immutable
class PErrorItemComposable<E : PError>(
   val errorClass: KClass<E>,
   val composable: @Composable (E) -> Unit
)

@Immutable
data class PErrorListColors(
   val listBackground: Color,
   val itemBackground: Color,
   val content: Color,
   val header: Color,
   val headerContent: Color,
)

@Stable
class PErrorListState(
   errorListCache: WritableCache<List<PError>>,
   itemComposables: List<PErrorItemComposable<*>>
) {
   private val itemComposables = itemComposables.associateBy { it.errorClass }

   var raisedTime: Instant? by mutableStateOf(null)
      private set

   internal var buttonBounds by mutableStateOf(Rect.Zero)

   var errors: List<PError> by errorListCache.asMutableState()

   var isShown by mutableStateOf(false)

   fun show() {
      isShown = true
   }

   fun hide() {
      isShown = false
   }

   fun raise(error: PError) {
      errors += error
      raisedTime = Clock.System.now()
   }

   @Stable
   internal fun <E : PError> getComposableFor(error: E): PErrorItemComposable<E>? {
      @Suppress("UNCHECKED_CAST")
      return itemComposables[error::class] as PErrorItemComposable<E>?
   }
}

@Composable
internal expect fun DismissHandler(onDismissRequest: () -> Unit)

@Composable
fun PErrorList(
   state: PErrorListState,
   colors: PErrorListColors,
   windowInsets: WindowInsets = WindowInsets.safeDrawing
) {
   Layout(
      content = {
         if (state.isShown) {
            DismissHandler(
               onDismissRequest = { state.hide() }
            )
         } else {
            Spacer(Modifier.fillMaxSize())
         }

         PErrorListSheet(
            state,
            colors,
            modifier = Modifier.windowInsetsPadding(
               // PErrorListContentはTopEndがPErrorActionButtonと合う位置に
               // 配置される（measurePolicy内に該当処理がある）のでWindowInsetsの
               // EndとTopは無視する
               windowInsets
                  .only(WindowInsetsSides.Start + WindowInsetsSides.Bottom)
            )
         )
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

private val headerHeight = 48.dp
private val iconHorizontalPadding = 16.dp

private fun <T> animSpec(easing: Easing = LinearEasing)
      = tween<T>(300, easing = easing)

@Composable
private fun PErrorListSheet(
   state: PErrorListState,
   colors: PErrorListColors,
   modifier: Modifier = Modifier
) {
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
      modifier = modifier
         .offset(x = offset)
         .shadow(elevation, MaterialTheme.shapes.small)
         .then(
            if (elevation > 0.dp) {
               Modifier.background(colors.listBackground)
            } else {
               Modifier
            }
         )
   ) { isShown ->
      if (!isShown) {
         Spacer(Modifier.size(headerHeight))
      } else {
         Column(
            modifier = Modifier
               .clip(MaterialTheme.shapes.small)
               .pointerInput(Unit) {}
         ) {
            PErrorListHeader(colors.header, colors.headerContent)

            PErrorListContent(state, colors.itemBackground, colors.content)
         }
      }
   }
}

@Composable
private fun AnimatedContentScope.PErrorListHeader(
   backgroundColor: Color,
   contentColor: Color
) {
   Box(
      modifier = Modifier
         .fillMaxWidth()
         .height(headerHeight)
         .background(backgroundColor)
   ) {
      CompositionLocalProvider(LocalContentColor provides contentColor) {
         val hiddenIconOffset = with (LocalDensity.current) {
            val hiddenIconPadding = (headerHeight - Icons.Default.Error.defaultWidth) / 2
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
   }
}

@Composable
private fun PErrorListContent(
   state: PErrorListState,
   itemBackgroundColor: Color,
   contentColor: Color,
   fallback: @Composable (PError) -> Unit = {}
) {
   CompositionLocalProvider(LocalContentColor provides contentColor) {
      val errors = state.errors

      LazyColumn {
         itemsIndexed(errors) { index, error ->
            Box(
               contentAlignment = Alignment.CenterStart,
               modifier = Modifier
                  .fillMaxWidth()
                  .heightIn(min = 48.dp)
                  .swipeDismiss()
                  .background(itemBackgroundColor)
            ) {
               val composable = state.getComposableFor(error)?.composable ?: fallback
               composable(error)
            }

            if (index < errors.lastIndex) {
               Divider()
            }
         }
      }
   }
}

@Composable
private fun Modifier.swipeDismiss(): Modifier {
   var offset by remember { mutableFloatStateOf(0.0f) }
   val scrollState = remember {
      ScrollableState {
         offset += it
         it
      }
   }

   val decaySpec = rememberSplineBasedDecay<Float>()
   val coroutineScope = rememberCoroutineScope()

   return pointerInput(Unit) {
         detectHorizontalDragGesture(
            shouldStartDragImmediately = { scrollState.isScrollInProgress },
            onDrag = { dragAmount ->
               coroutineScope.launch {
                  scrollState.scrollBy(dragAmount)
               }
            },
            onDragEnd = { velocity ->
               coroutineScope.launch {
                  val settledOffset = decaySpec.calculateTargetValue(offset, velocity)

                  val targetOffset = when {
                     settledOffset < -(size.width * 0.6f) -> -size.width.toFloat()
                     settledOffset >   size.width * 0.6f  ->  size.width.toFloat()
                     else                                 -> 0.0f
                  }
                  scrollState.scrollBy(targetOffset - offset)
               }
            }
         )
      }
      .offset { IntOffset(offset.roundToInt(), 0) }
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
