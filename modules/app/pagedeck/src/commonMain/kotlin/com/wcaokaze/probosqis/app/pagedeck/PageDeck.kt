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

import androidx.annotation.VisibleForTesting
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.wcaokaze.probosqis.capsiqum.deck.Deck
import com.wcaokaze.probosqis.capsiqum.deck.DeckState
import com.wcaokaze.probosqis.capsiqum.deck.get
import com.wcaokaze.probosqis.capsiqum.deck.inserted
import com.wcaokaze.probosqis.capsiqum.deck.removed
import com.wcaokaze.probosqis.capsiqum.deck.sequence
import com.wcaokaze.probosqis.capsiqum.page.Page
import com.wcaokaze.probosqis.capsiqum.page.PageId
import com.wcaokaze.probosqis.capsiqum.page.PageStack
import com.wcaokaze.probosqis.capsiqum.page.SavedPageState
import com.wcaokaze.probosqis.panoptiqon.WritableCache
import com.wcaokaze.probosqis.panoptiqon.compose.asMutableState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

typealias PageDeck = Deck<LazyPageStackState>

@Stable
class LazyPageStackState(
   val id: PageStack.Id,
   internal val pageStackCache: WritableCache<PageStack>,
   initialVisibility: Boolean
) {
   private var pageStackState: PPageStackState? = null

   @VisibleForTesting(otherwise = VisibleForTesting.NONE)
   val pageStack get() = pageStackCache.value

   var _isVisible = mutableStateOf(initialVisibility)
   var isVisible by _isVisible

   fun get(deckState: PageDeckState): PPageStackState {
      var s = pageStackState
      if (s != null) { return s }

      s = PPageStackState(id, pageStackCache, deckState)
      pageStackState = s
      return s
   }

   fun getIfInitialized(): PPageStackState? = pageStackState
}

internal val CARD_ANIM_DURATION = 200.milliseconds

internal fun cardAnimEnterTransitionSpec(density: Density): EnterTransition {
   val yOffset = with (density) { 64.dp.roundToPx() }
   val duration = CARD_ANIM_DURATION.toInt(DurationUnit.MILLISECONDS)

   return fadeIn(tween(duration)) + slideInVertically(tween(duration)) { yOffset }
}

internal fun cardAnimExitTransitionSpec(density: Density): ExitTransition {
   val yOffset = with (density) { 64.dp.roundToPx() }
   val duration = CARD_ANIM_DURATION.toInt(DurationUnit.MILLISECONDS)

   return fadeOut(tween(duration)) + slideOutVertically(tween(duration)) { yOffset }
}

@Stable
sealed class PageDeckState(
   pageDeckCache: WritableCache<PageDeck>,
   private val pageStackRepository: PageStackRepository
) {
   var deck: PageDeck by pageDeckCache.asMutableState()

   abstract val deckState: DeckState<LazyPageStackState>

   abstract val activeCardIndex: Int

   protected abstract suspend fun activate(cardIndex: Int, animate: Boolean)

   suspend fun activate(cardIndex: Int) {
      activate(cardIndex, animate = true)
   }
   
   private var coroutineScope: CoroutineScope? = null
   internal fun setCoroutineScope(coroutineScope: CoroutineScope) {
      this.coroutineScope = coroutineScope
   }

   val activePageStackState: PPageStackState get() {
      val lazy = deck[activeCardIndex].content
      return lazy.get(this)
   }

   fun addColumn(page: Page) {
      val pageStack = PageStack(
         SavedPageState(
            PageId(),
            page
         )
      )

      addColumn(activeCardIndex + 1, pageStack)
   }

   fun addColumn(index: Int, pageStack: PageStack): Job {
      val pageStackCache = pageStackRepository.savePageStack(pageStack)

      val coroutineScope = coroutineScope
      if (coroutineScope == null) {
         val lazyPageStackState = LazyPageStackState(
            pageStack.id, pageStackCache, initialVisibility = true)

         deck = Deck(
            rootRow = deck.rootRow.inserted(index, lazyPageStackState)
         )

         return Job().apply { complete() }
      } else {
         return coroutineScope.launch {
            val lazyPageStackState = LazyPageStackState(
               pageStack.id, pageStackCache, initialVisibility = false)

            try {
               deck = Deck(
                  rootRow = deck.rootRow.inserted(index, lazyPageStackState)
               )
               launch {
                  // リコンポジションを待機する
                  snapshotFlow { deckState.layoutInfo.cardsInfo }
                     .first { cards -> cards.any { it.key == pageStack.id } }

                  activate(index, animate = false)
               }
               delay(50.milliseconds)
            } finally {
               lazyPageStackState.isVisible = true
            }
         }
      }
   }

   fun removePageStack(id: PageStack.Id): Job {
      val lazyPageStackState = deck.sequence()
         .map { it.content }
         .find { it.id == id }
         ?: return Job().apply { complete() }

      val coroutineScope = coroutineScope
      return if (coroutineScope == null) {
         lazyPageStackState.isVisible = false
         val index = deck.sequence().indexOfFirst { it.content.id == id }
         if (index >= 0) {
            deck = deck.removed(index)
         }
         Job().apply { complete() }
      } else {
         coroutineScope.launch {
            lazyPageStackState.isVisible = false
            delay(CARD_ANIM_DURATION)

            val index = deck.sequence().indexOfFirst { it.content.id == id }
            if (index >= 0) {
               deck = deck.removed(index)
            }
         }
      }
   }
}
