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

package com.wcaokaze.probosqis.app.core

import com.wcaokaze.probosqis.app.pagedeck.LazyPageStackState
import com.wcaokaze.probosqis.app.pagedeck.PageDeck
import com.wcaokaze.probosqis.app.pagedeck.PageDeckRepository
import com.wcaokaze.probosqis.app.pagedeck.PageStackRepository
import com.wcaokaze.probosqis.capsiqum.deck.Deck
import com.wcaokaze.probosqis.capsiqum.page.Page
import com.wcaokaze.probosqis.capsiqum.page.PageId
import com.wcaokaze.probosqis.capsiqum.page.PageStack
import com.wcaokaze.probosqis.capsiqum.page.SavedPageState
import com.wcaokaze.probosqis.foundation.credential.CredentialRepository
import com.wcaokaze.probosqis.mastodon.entity.Token
import com.wcaokaze.probosqis.mastodon.ui.auth.urlinput.UrlInputPage
import com.wcaokaze.probosqis.mastodon.ui.timeline.home.HomeTimelinePage
import com.wcaokaze.probosqis.panoptiqon.WritableCache
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

fun loadPageDeckOrDefault(
   pageDeckRepository: PageDeckRepository,
   pageStackRepository: PageStackRepository,
   credentialRepository: CredentialRepository
): WritableCache<PageDeck> {
   return try {
      pageDeckRepository.loadPageDeck().also {
         if (it.value.rootRow.childCount == 0) {
            it.value = createDefaultPageDeck(
               pageStackRepository, credentialRepository
            )
         }
      }
   } catch (_: Exception) {
      pageStackRepository.deleteAllPageStacks()

      val pageDeck = createDefaultPageDeck(
         pageStackRepository, credentialRepository
      )
      pageDeckRepository.savePageDeck(pageDeck)
   }
}

internal fun createDefaultPageDeck(
   pageStackRepository: PageStackRepository,
   credentialRepository: CredentialRepository
): PageDeck {
   val allCredentials = try {
      credentialRepository.loadAllCredentials()
   } catch (_: Exception) {
      emptyList()
   }

   val defaultCredential = allCredentials.firstOrNull()

   val defaultPageStacks = if (defaultCredential != null) {
      createPageStacks(
         pages = sequenceOf(HomeTimelinePage(defaultCredential.value as Token)),
         pageStackRepository
      )
   } else {
      createPageStacks(
         pages = sequenceOf(UrlInputPage()),
         pageStackRepository
      )
   }

   val rootRow = Deck.Row(defaultPageStacks)
   return Deck(rootRow)
}

private fun createPageStacks(
   pages: Sequence<Page>,
   pageStackRepository: PageStackRepository
): ImmutableList<Deck.Layout<LazyPageStackState>> {
   return pages
      .mapIndexed { index, page ->
         PageStack(
            PageStack.Id(index.toLong()),
            SavedPageState(
               PageId(index.toLong()),
               page
            )
         )
      }
      .map { pageStackRepository.savePageStack(it) }
      .map { LazyPageStackState(it.value.id, it, initialVisibility = true) }
      .map { Deck.Card(it) }
      .toImmutableList()
}
