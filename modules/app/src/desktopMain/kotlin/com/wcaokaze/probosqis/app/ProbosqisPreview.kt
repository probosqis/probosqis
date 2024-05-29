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

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.wcaokaze.probosqis.capsiqum.deck.Deck
import com.wcaokaze.probosqis.capsiqum.page.PageStack
import com.wcaokaze.probosqis.capsiqum.page.PageId
import com.wcaokaze.probosqis.capsiqum.page.SavedPageState
import com.wcaokaze.probosqis.error.PError
import com.wcaokaze.probosqis.error.PErrorItemComposable
import com.wcaokaze.probosqis.error.PErrorListRepository
import com.wcaokaze.probosqis.pagedeck.CombinedPageSwitcherState
import com.wcaokaze.probosqis.pagedeck.LazyPageStackState
import com.wcaokaze.probosqis.pagedeck.PageDeck
import com.wcaokaze.probosqis.pagedeck.PageDeckRepository
import com.wcaokaze.probosqis.pagedeck.PageStackRepository
import com.wcaokaze.probosqis.panoptiqon.WritableCache
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Preview
@Composable
private fun ProbosqisPreview() {
   val allPageComposables = persistentListOf(
      testPageComposable,
   )

   val pageDeckRepository = object : PageDeckRepository {
      override fun savePageDeck(pageDeck: PageDeck): WritableCache<PageDeck>
            = throw NotImplementedError()

      override fun loadPageDeck(): WritableCache<PageDeck> {
         val children = List(4) { pageStackId ->
            val pageStack = PageStack(
               PageStack.Id(pageStackId.toLong()),
               SavedPageState(
                  PageId(0L),
                  TestPage(0)
               )
            )
            val lazyPageStackState = LazyPageStackState(
               pageStack.id,
               WritableCache(pageStack),
               initialVisibility = true
            )
            Deck.Card(lazyPageStackState)
         } .toImmutableList()

         val rootRow = Deck.Row(children)
         val deck = Deck(rootRow)
         return WritableCache(deck)
      }
   }

   val pageStackRepository = object : PageStackRepository {
      override fun savePageStack(pageStack: PageStack): WritableCache<PageStack>
            = throw NotImplementedError()
      override fun loadPageStack(id: PageStack.Id): WritableCache<PageStack>
            = throw NotImplementedError()
      override fun deleteAllPageStacks()
            = throw NotImplementedError()
   }

   val allErrorItemComposables = persistentListOf<PErrorItemComposable<*>>()

   val errorListRepository = object : PErrorListRepository {
      override fun saveErrorList(errorList: List<PError>): WritableCache<List<PError>>
            = throw NotImplementedError()
      override fun loadErrorList(): WritableCache<List<PError>>
            = throw NotImplementedError()
   }

   val coroutineScope = rememberCoroutineScope()

   val probosqisState = remember {
      ProbosqisState(
         allPageComposables, CombinedPageSwitcherState(allPageComposables),
         pageDeckRepository, pageStackRepository, allErrorItemComposables,
         errorListRepository, coroutineScope
      )
   }

   MultiColumnProbosqis(probosqisState)
}
