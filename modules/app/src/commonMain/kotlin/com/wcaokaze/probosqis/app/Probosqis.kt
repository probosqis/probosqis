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

package com.wcaokaze.probosqis.app

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.dp
import com.wcaokaze.probosqis.app.pagedeck.CombinedPageComposable
import com.wcaokaze.probosqis.app.pagedeck.CombinedPageSwitcherState
import com.wcaokaze.probosqis.app.pagedeck.LazyPageStackState
import com.wcaokaze.probosqis.app.pagedeck.PageDeck
import com.wcaokaze.probosqis.app.pagedeck.PageDeckRepository
import com.wcaokaze.probosqis.app.pagedeck.PageDeckState
import com.wcaokaze.probosqis.app.pagedeck.PageStackRepository
import com.wcaokaze.probosqis.capsiqum.deck.Deck
import com.wcaokaze.probosqis.capsiqum.page.PageStack
import com.wcaokaze.probosqis.capsiqum.page.PageId
import com.wcaokaze.probosqis.capsiqum.page.PageStateStore
import com.wcaokaze.probosqis.capsiqum.page.SavedPageState
import com.wcaokaze.probosqis.ext.compose.layout.safeDrawing
import com.wcaokaze.probosqis.panoptiqon.WritableCache
import com.wcaokaze.probosqis.resources.ProbosqisTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope

@Stable
class ProbosqisState(
   allPageComposables: List<CombinedPageComposable<*, *>>,
   val pageDeckRepository: PageDeckRepository,
   val pageStackRepository: PageStackRepository,
   coroutineScope: CoroutineScope
) {
   val pageComposableSwitcher = CombinedPageSwitcherState(allPageComposables)
   val pageStateStore = PageStateStore(
      allPageComposables.map { it.pageStateFactory },
      coroutineScope
   )

   private var _pageDeckState: PageDeckState? = null
   var pageDeckState: PageDeckState
      get() {
         return _pageDeckState ?: throw IllegalStateException(
            "attempt to get pageDeckState before the first Composition")
      }
      internal set(value) {
         _pageDeckState = value
      }

   internal fun loadPageDeckOrDefault(): WritableCache<PageDeck> {
      return try {
         pageDeckRepository.loadPageDeck()
      } catch (e: Exception) {
         pageStackRepository.deleteAllPageStacks()

         val rootRow = Deck.Row(
            createDefaultPageStacks(pageStackRepository)
         )
         val pageDeck = Deck(rootRow)
         pageDeckRepository.savePageDeck(pageDeck)
      }
   }

   private fun createDefaultPageStacks(
      pageStackRepository: PageStackRepository
   ): ImmutableList<Deck.Layout<LazyPageStackState>> {
      return sequenceOf(
            PageStack(
               PageStack.Id(0L),
               SavedPageState(
                  PageId(0L),
                  TestPage(0)
               )
            ),
            PageStack(
               PageStack.Id(1L),
               SavedPageState(
                  PageId(1L),
                  TestPage(1)
               )
            ),
         )
         .map { pageStackRepository.savePageStack(it) }
         .map { LazyPageStackState(it.value.id, it, initialVisibility = true) }
         .map { Deck.Card(it) }
         .toImmutableList()
   }
}
