/*
 * Copyright 2023-2024 wcaokaze
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.dp
import com.wcaokaze.probosqis.capsiqum.deck.SingleColumnDeck
import com.wcaokaze.probosqis.capsiqum.deck.SingleColumnDeckState
import com.wcaokaze.probosqis.capsiqum.page.PageStateStore
import com.wcaokaze.probosqis.capsiqum.transition.PageTransition
import com.wcaokaze.probosqis.panoptiqon.WritableCache
import kotlin.time.DurationUnit

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
}

@ExperimentalMaterial3Api
@Composable
fun SingleColumnPageDeckAppBar(
   state: SingleColumnPageDeckState,
   pageSwitcherState: CombinedPageSwitcherState,
   pageStateStore: PageStateStore,
   windowInsets: WindowInsets = WindowInsets
      .safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
) {
   Box {
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
         cardPadding = 8.dp,
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

            SingleColumnPageStackAppBar(pageStackState, pageSwitcherState,
               pageStateStore, windowInsets)
         }
      }
   }
}

@Composable
fun SingleColumnPageDeck(
   state: SingleColumnPageDeckState,
   pageSwitcherState: CombinedPageSwitcherState,
   pageStateStore: PageStateStore,
   pageStackBackgroundColor: Color,
   pageStackFooterBackgroundColor: Color,
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
      cardPadding = 8.dp,
      modifier = modifier
   ) { _, lazyPageStackState ->
      val density = LocalDensity.current

      AnimatedVisibility(
         lazyPageStackState.isVisible,
         enter = remember(density) { cardAnimEnterTransitionSpec(density) },
         exit  = remember(density) { cardAnimExitTransitionSpec (density) }
      ) {
         val pageStackState = lazyPageStackState.get(state)

         PageStackContent(
            pageStackState,
            pageSwitcherState,
            pageStateStore,
            pageStackBackgroundColor,
            pageStackFooterBackgroundColor,
            windowInsets = windowInsets,
         )
      }
   }
}

@ExperimentalMaterial3Api
@Composable
private fun SingleColumnPageStackAppBar(
   pageStackState: PageStackState,
   pageSwitcher: CombinedPageSwitcherState,
   pageStateStore: PageStateStore,
   windowInsets: WindowInsets,
   modifier: Modifier = Modifier
) {
   PageStackAppBar(
      pageStackState,
      pageSwitcher,
      pageStateStore,
      windowInsets,
      colors = TopAppBarDefaults.topAppBarColors(
         containerColor = Color.Transparent,
      ),
      modifier = modifier
   )
}

@Composable
private fun PageStackContent(
   state: PageStackState,
   pageSwitcher: CombinedPageSwitcherState,
   pageStateStore: PageStateStore,
   backgroundColor: Color,
   footerBackgroundColor: Color,
   windowInsets: WindowInsets
) {
   val transitionState = remember(pageSwitcher) {
      PageTransitionStateImpl(pageSwitcher)
   }

   PageTransition(
      transitionState,
      state.pageStack
   ) { pageStack ->
      PageContentFooter(pageStack.head, state, pageSwitcher,
         pageStateStore, backgroundColor, footerBackgroundColor,windowInsets)
   }
}
