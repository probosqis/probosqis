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
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import com.wcaokaze.probosqis.capsiqum.page.PageId
import com.wcaokaze.probosqis.panoptiqon.WritableCache
import com.wcaokaze.probosqis.panoptiqon.compose.asMutableState
import com.wcaokaze.probosqis.resources.icons.Error
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt
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

@Serializable
class RaisedError(
   val error: PError,
   val raisedIn: PageId
) {
   override fun equals(other: Any?): Boolean {
      return other is RaisedError
            && error    == other.error
            && raisedIn == other.raisedIn
   }

   override fun hashCode(): Int {
      var h =      error .hashCode()
      h = h * 31 + raisedIn.hashCode()
      return h
   }

   override fun toString() = "$error (raised in $raisedIn)"
}

@Stable
class PErrorListState(
   errorListCache: WritableCache<List<RaisedError>>,
   itemComposables: List<PErrorItemComposable<*>>
) {
   private val itemComposables = itemComposables.associateBy { it.errorClass }

   var raisedTime: Instant? by mutableStateOf(null)
      private set

   internal var buttonBounds by mutableStateOf(Rect.Zero)

   var errors: List<RaisedError> by errorListCache.asMutableState()

   var isShown by mutableStateOf(false)

   fun show() {
      isShown = true
   }

   fun hide() {
      isShown = false
   }

   fun raise(error: PError, raisedIn: PageId) {
      check(errors.none { it.error.id == error.id })

      errors += RaisedError(error, raisedIn)
      raisedTime = Clock.System.now()
   }

   fun dismiss(error: PError) {
      errors = errors.filterNot { it.error.id == error.id }

      if (errors.isEmpty()) {
         hide()
      }
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
            modifier = Modifier
               .windowInsetsPadding(
                  // PErrorListContentはTopEndがPErrorActionButtonと合う位置に
                  // 配置される（measurePolicy内に該当処理がある）のでWindowInsetsの
                  // EndとTopは無視する
                  windowInsets
                     .only(WindowInsetsSides.Start + WindowInsetsSides.Bottom)
               )
               .widthIn(max = 400.dp)
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
            PErrorListHeader(state, colors.header, colors.headerContent)

            PErrorListContent(state, colors.itemBackground, colors.content)
         }
      }
   }
}

@Composable
private fun AnimatedContentScope.PErrorListHeader(
   state: PErrorListState,
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

         val slideIn  = slideInHorizontally (animSpec()) { hiddenIconOffset }
         val slideOut = slideOutHorizontally(animSpec()) { hiddenIconOffset }

         val isEmpty by derivedStateOf { state.errors.isEmpty() }

         @OptIn(ExperimentalAnimationApi::class)
         Icon(
            Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier
               .align(Alignment.CenterStart)
               .padding(horizontal = iconHorizontalPadding)
               .animateEnterExit(
                  enter = slideIn,
                  exit = if (isEmpty) { slideOut + fadeOut() } else { slideOut }
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
         itemsIndexed(
            errors,
            key = { _, error -> error.error.id.value }
         ) { index, raisedError ->
            val error = raisedError.error
            val composable = state.getComposableFor(error)?.composable ?: fallback
            PErrorListItem(
               error, composable, itemBackgroundColor,
               onDismiss = { state.dismiss(error) }
            )

            if (index < errors.lastIndex) {
               Divider()
            }
         }
      }
   }
}
