/*
 * Copyright 2024-2025 wcaokaze
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

import com.wcaokaze.probosqis.capsiqum.deck.Deck
import com.wcaokaze.probosqis.capsiqum.page.PageId
import com.wcaokaze.probosqis.capsiqum.page.PageStack
import com.wcaokaze.probosqis.capsiqum.page.SavedPageState
import com.wcaokaze.probosqis.pagedeck.LazyPageStackState
import com.wcaokaze.probosqis.pagedeck.PageDeck
import com.wcaokaze.probosqis.pagedeck.PageDeckRepository
import com.wcaokaze.probosqis.pagedeck.PageStackRepository
import com.wcaokaze.probosqis.panoptiqon.WritableCache
import com.wcaokaze.probosqis.testpages.TestPage
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

fun loadPageDeckOrDefault(
   pageDeckRepository: PageDeckRepository,
   pageStackRepository: PageStackRepository
): WritableCache<PageDeck> {
   return try {
      pageDeckRepository.loadPageDeck().also {
         if (it.value.rootRow.childCount == 0) {
            it.value = createDefaultPageDeck(pageStackRepository)
         }
      }
   } catch (_: Exception) {
      pageStackRepository.deleteAllPageStacks()

      val pageDeck = createDefaultPageDeck(pageStackRepository)
      pageDeckRepository.savePageDeck(pageDeck)
   }
}

private fun createDefaultPageDeck(
   pageStackRepository: PageStackRepository
): PageDeck {
   val rootRow = Deck.Row(
      createDefaultPageStacks(pageStackRepository)
   )
   return Deck(rootRow)
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
