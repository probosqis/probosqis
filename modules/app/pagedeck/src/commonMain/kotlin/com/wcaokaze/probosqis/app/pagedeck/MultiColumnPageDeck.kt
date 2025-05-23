/*
 * Copyright 2023-2025 wcaokaze
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

package com.wcaokaze.probosqis.app.pagedeck

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.changedToDownIgnoreConsumed
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wcaokaze.probosqis.capsiqum.deck.MultiColumnDeck
import com.wcaokaze.probosqis.capsiqum.deck.MultiColumnDeckState
import com.wcaokaze.probosqis.capsiqum.deck.get
import com.wcaokaze.probosqis.capsiqum.transition.PageTransition
import com.wcaokaze.probosqis.panoptiqon.WritableCache

@Stable
class MultiColumnPageDeckState(
   pageDeckCache: WritableCache<PageDeck>,
   pageStackRepository: PageStackRepository
) : PageDeckState(pageDeckCache, pageStackRepository) {
   override val deckState = MultiColumnDeckState<LazyPageStackState>(
      key = { it.id }
   )

   override var activeCardIndex by mutableIntStateOf(0)
      internal set

   override suspend fun activate(cardIndex: Int, animate: Boolean) {
      deckState.animateScroll(cardIndex)
      activeCardIndex = cardIndex

      if (animate) {
         deck[cardIndex].content.getIfInitialized()
            ?.multiColumnActivationAnimState?.animate()
      }
   }
}

@ExperimentalMaterial3Api
@Composable
fun MultiColumnPageDeck(
   state: MultiColumnPageDeckState,
   pageSwitcherState: CombinedPageSwitcherState,
   pageStackCount: Int,
   activeAppBarColors: TopAppBarColors,
   inactiveAppBarColors: TopAppBarColors,
   colors: PageStackColors,
   windowInsets: WindowInsets,
   modifier: Modifier = Modifier
) {
   ActiveCardCorrector(state)

   val coroutineScope = rememberCoroutineScope()
   LaunchedEffect(coroutineScope) {
      state.setCoroutineScope(coroutineScope)
   }

   MultiColumnDeck(
      state.deck,
      state.deckState,
      pageStackCount,
      windowInsets = windowInsets,
      cardPadding = 8.dp,
      modifier = modifier
   ) { index, lazyPageStackState ->
      val density = LocalDensity.current

      AnimatedVisibility(
         lazyPageStackState.isVisible,
         enter = remember(density) { cardAnimEnterTransitionSpec(density) },
         exit  = remember(density) { cardAnimExitTransitionSpec (density) }
      ) {
         val pageStackState = lazyPageStackState.get(state)

         PageStack(
            pageStackState,
            pageSwitcherState,
            appBarColors = if (state.activeCardIndex == index) {
               activeAppBarColors
            } else {
               inactiveAppBarColors
            },
            colors,
            windowInsets.only(WindowInsetsSides.Bottom),
            modifier = Modifier
               .detectTouch(
                  onTouch = remember(state) {{ state.activeCardIndex = index }}
               )
         )
      }
   }
}

@Composable
private fun ActiveCardCorrector(state: MultiColumnPageDeckState) {
   LaunchedEffect(
      state.deckState.firstContentCardIndex,
      state.deckState.lastContentCardIndex,
   ) {
      state.activeCardIndex = state.activeCardIndex.coerceIn(
         state.deckState.firstContentCardIndex,
         state.deckState.lastContentCardIndex
      )
   }
}

@Stable
internal class MultiColumnPageStackActivationAnimState {
   private val scaleOffsetAnim = Animatable(0.dp, Dp.VectorConverter)
   val scaleOffset by scaleOffsetAnim.asState()

   suspend fun animate() {
      scaleOffsetAnim.animateTo(12.dp, tween(32, easing = LinearEasing))
      scaleOffsetAnim.animateTo(0.dp)
   }
}

@ExperimentalMaterial3Api
@Composable
private fun PageStack(
   state: PPageStackState,
   pageSwitcherState: CombinedPageSwitcherState,
   appBarColors: TopAppBarColors,
   colors: PageStackColors,
   windowInsets: WindowInsets,
   modifier: Modifier = Modifier
) {
   Column(
      modifier
         .graphicsLayer {
            val anim = state.multiColumnActivationAnimState
            val scale = (size.width + anim.scaleOffset.toPx()) / size.width
            scaleX = scale
            scaleY = scale
         }
         .clip(MaterialTheme.shapes.large)
         .background(colors.background)
   ) {
      MultiColumnPageStackAppBar(state, pageSwitcherState, appBarColors)

      val transitionState = remember(pageSwitcherState) {
         PageTransitionStateImpl(pageSwitcherState)
      }

      PageTransition(
         transitionState,
         state.pageStack
      ) { pageStack ->
         PageContentFooter(
            pageStack.head, state, pageSwitcherState, colors, windowInsets
         )
      }
   }
}

@ExperimentalMaterial3Api
@Composable
private fun MultiColumnPageStackAppBar(
   pageStackState: PPageStackState,
   pageSwitcherState: CombinedPageSwitcherState,
   colors: TopAppBarColors,
   modifier: Modifier = Modifier
) {
   @OptIn(ExperimentalMaterial3Api::class)
   PageStackAppBar(
      pageStackState,
      pageSwitcherState,
      colors,
      WindowInsets(0),
      modifier = modifier
   )
}

@Stable
private fun Modifier.detectTouch(onTouch: () -> Unit): Modifier {
   return pointerInput(onTouch) {
      awaitEachGesture {
         val event = awaitPointerEvent(PointerEventPass.Initial)

         val isDownEvent = event.changes.any {
            if (it.type == PointerType.Mouse) {
               event.buttons.isPrimaryPressed && it.changedToDownIgnoreConsumed()
            } else {
               it.changedToDownIgnoreConsumed()
            }
         }

         if (isDownEvent) {
            onTouch()
         }
      }
   }
}
