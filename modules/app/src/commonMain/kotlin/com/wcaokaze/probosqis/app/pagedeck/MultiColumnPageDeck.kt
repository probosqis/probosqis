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
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.changedToDownIgnoreConsumed
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wcaokaze.probosqis.capsiqum.deck.MultiColumnDeck
import com.wcaokaze.probosqis.capsiqum.deck.MultiColumnDeckState
import com.wcaokaze.probosqis.capsiqum.page.PageStateStore
import com.wcaokaze.probosqis.capsiqum.transition.PageTransition
import com.wcaokaze.probosqis.panoptiqon.WritableCache

object MultiColumnPageDeckDefaults {
   @ExperimentalMaterial3Api
   @Composable
   fun pageStackAppBarColors(): MultiColumnPageStackAppBarColors {
      val colorScheme = MaterialTheme.colorScheme

      return MultiColumnPageStackAppBarColors(
         active = TopAppBarDefaults.topAppBarColors(
            containerColor = colorScheme.surfaceTint.copy(alpha = 0.13f)
               .compositeOver(MaterialTheme.colorScheme.primaryContainer),
            navigationIconContentColor = colorScheme.onPrimaryContainer,
            titleContentColor = colorScheme.onPrimaryContainer,
            actionIconContentColor = colorScheme.onPrimaryContainer,
         ),
         inactive = TopAppBarDefaults.topAppBarColors(
            containerColor = colorScheme.surfaceColorAtElevation(4.dp),
         )
      )
   }
}

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
}

@ExperimentalMaterial3Api
@Immutable
data class MultiColumnPageStackAppBarColors(
   val active: TopAppBarColors,
   val inactive: TopAppBarColors
)

@ExperimentalMaterial3Api
@Composable
fun MultiColumnPageDeck(
   state: MultiColumnPageDeckState,
   pageSwitcherState: CombinedPageSwitcherState,
   pageStateStore: PageStateStore,
   pageStackCount: Int,
   windowInsets: WindowInsets,
   modifier: Modifier = Modifier,
   pageStackAppBarColors: MultiColumnPageStackAppBarColors
         = MultiColumnPageDeckDefaults.pageStackAppBarColors(),
   onTopAppBarHeightChanged: (Dp) -> Unit = {}
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
            isActive = state.activeCardIndex == index,
            windowInsets.only(WindowInsetsSides.Bottom),
            pageStackAppBarColors,
            pageSwitcherState,
            pageStateStore,
            onTopAppBarHeightChanged,
            modifier = Modifier
               .detectTouch(
                  onTouch = { state.activeCardIndex = index }
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

@ExperimentalMaterial3Api
@Composable
private fun PageStack(
   state: PageStackState,
   isActive: Boolean,
   windowInsets: WindowInsets,
   appBarColors: MultiColumnPageStackAppBarColors,
   pageSwitcherState: CombinedPageSwitcherState,
   pageStateStore: PageStateStore,
   onTopAppBarHeightChanged: (Dp) -> Unit,
   modifier: Modifier = Modifier
) {
   Surface(
      shape = MaterialTheme.shapes.large,
      tonalElevation = if (isActive) { 3.dp } else { 1.dp },
      shadowElevation = if (isActive) { 4.dp } else { 2.dp },
      modifier = modifier
   ) {
      Column {
         val density by rememberUpdatedState(LocalDensity.current)

         MultiColumnPageStackAppBar(
            state,
            pageSwitcherState,
            pageStateStore,
            isActive,
            appBarColors,
            modifier = Modifier
               .onSizeChanged {
                  val heightPx = it.height
                  val heightDp = with (density) { heightPx.toDp() }
                  onTopAppBarHeightChanged(heightDp)
               }
         )

         val transitionState = remember(pageSwitcherState) {
            PageTransitionStateImpl(pageSwitcherState)
         }

         PageTransition(
            transitionState,
            state.pageStack
         ) { pageStack ->
            PageContentFooter(pageStack.head, state, pageSwitcherState,
               pageStateStore, windowInsets)
         }
      }
   }
}

@ExperimentalMaterial3Api
@Composable
private fun MultiColumnPageStackAppBar(
   pageStackState: PageStackState,
   pageSwitcherState: CombinedPageSwitcherState,
   pageStateStore: PageStateStore,
   isActive: Boolean,
   colors: MultiColumnPageStackAppBarColors,
   modifier: Modifier = Modifier
) {
   @OptIn(ExperimentalMaterial3Api::class)
   PageStackAppBar(
      pageStackState,
      pageSwitcherState,
      pageStateStore,
      windowInsets = WindowInsets(0),
      colors = if (isActive) { colors.active } else { colors.inactive },
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