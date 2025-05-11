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
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import com.wcaokaze.probosqis.capsiqum.deck.SingleColumnDeck
import com.wcaokaze.probosqis.capsiqum.deck.SingleColumnDeckState
import com.wcaokaze.probosqis.capsiqum.deck.get
import com.wcaokaze.probosqis.capsiqum.transition.PageTransition
import com.wcaokaze.probosqis.panoptiqon.WritableCache
import kotlin.time.DurationUnit

internal val cardPadding = 8.dp

@Stable
class SingleColumnPageDeckState(
   pageDeckCache: WritableCache<PageDeck>,
   pageStackRepository: PageStackRepository
) : PageDeckState(pageDeckCache, pageStackRepository) {
   override val deckState = SingleColumnDeckState<LazyPageStackState>(
      key = { it.id }
   )

   override val activeCardIndex: Int by derivedStateOf {
      val firstVisibleIndex = deckState.firstVisibleCardIndex
      val lastVisibleIndex  = deckState.lastVisibleCardIndex
      if (firstVisibleIndex == lastVisibleIndex) {
         return@derivedStateOf firstVisibleIndex
      }

      val cardsInfo = deckState.layoutInfo.cardsInfo
      val firstVisibleLayout = cardsInfo[firstVisibleIndex]
      val lastVisibleLayout  = cardsInfo[lastVisibleIndex]
      val deckWidth = firstVisibleLayout.width

      if (
         deckState.scrollOffset + deckWidth / 2.0f
            < (firstVisibleLayout.position.x
               + lastVisibleLayout.position.x + lastVisibleLayout.width) / 2.0f
      ) {
         firstVisibleIndex
      } else {
         firstVisibleIndex + 1
      }
   }

   override suspend fun activate(cardIndex: Int, animate: Boolean) {
      if (activeCardIndex == cardIndex) {
         if (animate) {
            deck[cardIndex].content.getIfInitialized()
               ?.animateActivationBackground()
         }
      } else {
         deckState.animateScroll(cardIndex)
      }

   }
}

@ExperimentalMaterial3Api
@Composable
fun SingleColumnPageDeckAppBar(
   state: SingleColumnPageDeckState,
   pageSwitcherState: CombinedPageSwitcherState,
   windowInsets: WindowInsets = WindowInsets
      .safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
) {
   Box(Modifier.inflateWidth(cardPadding)) {
      // PageDeckState.removePageStack等によって一瞬PageStackが画面内に
      // ひとつもないことがある
      var appBarHeight by remember { mutableIntStateOf(0) }

      Layout(
         content = {},
         measurePolicy = { _, constraints ->
            val height = constraints.constrainHeight(appBarHeight)
            layout(0, height) {}
         }
      )

      SingleColumnDeck(
         state.deck,
         state.deckState,
         cardPadding = 0.dp,
         modifier = Modifier
            .fillMaxWidth()
            .onSizeChanged {
               if (it.height > 0) {
                  appBarHeight = it.height
               }
            }
      ) { _, lazyPageStackState ->
         val duration = CARD_ANIM_DURATION.toInt(DurationUnit.MILLISECONDS)

         AnimatedVisibility(
            lazyPageStackState.isVisible,
            enter = fadeIn (tween(duration)),
            exit  = fadeOut(tween(duration))
         ) {
            val pageStackState = lazyPageStackState.get(state)

            SingleColumnPageStackAppBar(
               pageStackState, pageSwitcherState, windowInsets
            )
         }
      }
   }
}

@Composable
fun SingleColumnPageDeck(
   state: SingleColumnPageDeckState,
   pageSwitcherState: CombinedPageSwitcherState,
   colors: PageStackColors,
   windowInsets: WindowInsets,
   modifier: Modifier = Modifier,
) {
   val coroutineScope = rememberCoroutineScope()
   LaunchedEffect(coroutineScope) {
      state.setCoroutineScope(coroutineScope)
   }

   SingleColumnDeck(
      state.deck,
      state.deckState,
      cardPadding = 0.dp,
      modifier = modifier.inflateWidth(cardPadding)
   ) { index, lazyPageStackState ->
      val density = LocalDensity.current

      AnimatedVisibility(
         lazyPageStackState.isVisible,
         enter = remember(density) { cardAnimEnterTransitionSpec(density) },
         exit  = remember(density) { cardAnimExitTransitionSpec (density) }
      ) {
         val pageStackState = lazyPageStackState.get(state)

         val cardsInfo = state.deckState.layoutInfo.cardsInfo

         val prevPageStackHasFooter = cardsInfo.getOrNull(index - 1)?.let {
            val page = it.card.content.pageStackCache.value.head.page
            pageSwitcherState[page]?.footerComposable != null
         } ?: false

         val nextPageStackHasFooter = cardsInfo.getOrNull(index + 1)?.let {
            val page = it.card.content.pageStackCache.value.head.page
            pageSwitcherState[page]?.footerComposable != null
         } ?: false

         PageStackContent(
            pageStackState, pageSwitcherState, colors,
            prevPageStackHasFooter, nextPageStackHasFooter,
            windowInsets
         )
      }
   }
}

@ExperimentalMaterial3Api
@Composable
private fun SingleColumnPageStackAppBar(
   pageStackState: PPageStackState,
   pageSwitcher: CombinedPageSwitcherState,
   windowInsets: WindowInsets,
   modifier: Modifier = Modifier
) {
   PageStackAppBar(
      pageStackState,
      pageSwitcher,
      colors = TopAppBarDefaults.topAppBarColors(
         containerColor = Color.Transparent,
      ),
      windowInsets,
      horizontalContentPadding = cardPadding,
      modifier = modifier
   )
}

@Composable
private fun PageStackContent(
   state: PPageStackState,
   pageSwitcher: CombinedPageSwitcherState,
   colors: PageStackColors,
   prevPageStackHasFooter: Boolean,
   nextPageStackHasFooter: Boolean,
   windowInsets: WindowInsets
) {
   val transitionState = remember(pageSwitcher) {
      PageTransitionStateImpl(pageSwitcher)
   }

   PageTransition(
      transitionState,
      state.pageStack
   ) { pageStack ->
      PageContentFooter(
         pageStack.head, state, pageSwitcher,
         colors, windowInsets, horizontalContentPadding = cardPadding,
         footerStartPaddingType = if (prevPageStackHasFooter) {
            FooterPaddingType.Content
         } else {
            FooterPaddingType.Entire
         },
         footerEndPaddingType = if (nextPageStackHasFooter) {
            FooterPaddingType.Content
         } else {
            FooterPaddingType.Entire
         }
      )
   }
}

@Stable
private fun Modifier.inflateWidth(delta: Dp): Modifier {
   return layout { measurable, constraints ->
      val inflatedConstraints = if (constraints.hasFixedWidth) {
         constraints.copy(
            minWidth = constraints.minWidth + (delta * 2).roundToPx(),
            maxWidth = constraints.maxWidth + (delta * 2).roundToPx()
         )
      } else {
         constraints.copy(
            maxWidth = constraints.maxWidth + (delta * 2).roundToPx()
         )
      }

      val placeable = measurable.measure(inflatedConstraints)

      layout(
         constraints.constrainWidth (placeable.width),
         constraints.constrainHeight(placeable.height)
      ) {
         placeable.place(-delta.roundToPx(), 0)
      }
   }
}
