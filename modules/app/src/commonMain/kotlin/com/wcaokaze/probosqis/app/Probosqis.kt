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

import androidx.compose.runtime.Stable
import com.wcaokaze.probosqis.capsiqum.deck.Deck
import com.wcaokaze.probosqis.capsiqum.page.PageId
import com.wcaokaze.probosqis.capsiqum.page.PageStack
import com.wcaokaze.probosqis.capsiqum.page.PageStateStore
import com.wcaokaze.probosqis.capsiqum.page.SavedPageState
import com.wcaokaze.probosqis.error.PError
import com.wcaokaze.probosqis.error.PErrorItemComposable
import com.wcaokaze.probosqis.error.PErrorListRepository
import com.wcaokaze.probosqis.error.PErrorListState
import com.wcaokaze.probosqis.pagedeck.CombinedPageComposable
import com.wcaokaze.probosqis.pagedeck.CombinedPageSwitcherState
import com.wcaokaze.probosqis.pagedeck.LazyPageStackState
import com.wcaokaze.probosqis.pagedeck.PageDeck
import com.wcaokaze.probosqis.pagedeck.PageDeckRepository
import com.wcaokaze.probosqis.pagedeck.PageDeckState
import com.wcaokaze.probosqis.pagedeck.PageStackRepository
import com.wcaokaze.probosqis.panoptiqon.WritableCache
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope

@Stable
class ProbosqisState(
   allPageComposables: List<CombinedPageComposable<*, *>>,
   val pageComposableSwitcher: CombinedPageSwitcherState,
   val pageDeckRepository: PageDeckRepository,
   val pageStackRepository: PageStackRepository,
   val allErrorItemComposables: List<PErrorItemComposable<*>>,
   val errorListRepository: PErrorListRepository,
   coroutineScope: CoroutineScope
) {
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

   val errorListState = PErrorListState(
      loadErrorListOrDefault(),
      allErrorItemComposables
   )

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

   internal fun loadErrorListOrDefault(): WritableCache<List<PError>> {
      return try {
         errorListRepository.loadErrorList()
      } catch (e: Exception) {
         val defaultErrors = persistentListOf(
            PErrorImpl("Lorem ipsum dolor sit amet"),
            PErrorImpl("consectetur adipiscing elit"),
            PErrorImpl("sed do eiusmod tempor incididunt ut labore et dolore magna aliqua"),
            PErrorImpl("Ut enim ad minim veniam"),
            PErrorImpl("quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat"),
            PErrorImpl("Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur"),
            PErrorImpl("Excepteur sint occaecat cupidatat non proident"),
            PErrorImpl("sunt in culpa qui officia deserunt mollit anim id est laborum"),
         )
         errorListRepository.saveErrorList(defaultErrors)
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
