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

import com.wcaokaze.probosqis.capsiqum.deck.Deck
import com.wcaokaze.probosqis.capsiqum.page.Page
import com.wcaokaze.probosqis.capsiqum.page.PageId
import com.wcaokaze.probosqis.capsiqum.page.PageStack
import com.wcaokaze.probosqis.capsiqum.page.SavedPageState
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PageDeckRepositoryTest {
   @Serializable
   @SerialName("com.wcaokaze.probosqis.capsiqum.IntPage")
   class IntPage(val i: Int) : Page()

   @Serializable
   @SerialName("com.wcaokaze.probosqis.capsiqum.StringPage")
   class StringPage(val s: String) : Page()

   private lateinit var pageStackRepository: PageStackRepository
   private lateinit var pageDeckRepository: PageDeckRepository

   @BeforeTest
   fun beforeTest() {
      pageStackRepository = createPageStackRepository(
         listOf(
            pageSerializer<IntPage>(),
            pageSerializer<StringPage>(),
         )
      )

      pageDeckRepository = createPageDeckRepository(
         pageStackRepository
      )
   }

   @AfterTest
   fun afterTest() {
      deleteRepositories()
   }

   @Test
   fun readWrite() {
      val intPage = IntPage(42)
      val stringPage = StringPage("wcaokaze")
      var pageStack = PageStack(
         PageStack.Id(0L),
         SavedPageState(
            PageId(0L),
            intPage
         )
      )
      pageStack = pageStack.added(
         SavedPageState(
            PageId(1L),
            stringPage
         )
      )

      val pageStackCache = pageStackRepository.savePageStack(pageStack)
      val children = persistentListOf(
         Deck.Card(
            LazyPageStackState(
               PageStack.Id(pageStackCache.value.id.value),
               pageStackCache,
               initialVisibility = true
            )
         ),
      )
      val pageDeck = Deck(
         rootRow = Deck.Row(children)
      )

      pageDeckRepository.savePageDeck(pageDeck)

      val loadedCache = pageDeckRepository.loadPageDeck()

      assertEquals(loadedCache.value.rootRow.childCount, 1)

      val loadedPageStack = loadedCache.value
         .let { assertIs<Deck.Card<*>>(it.rootRow[0]) }
         .let { assertIs<LazyPageStackState>(it.content) }
         .pageStackCache.value

      val pageId1 = loadedPageStack.head.id
      val page1 = loadedPageStack.head.page
      assertEquals(PageId(1L), pageId1)
      assertIs<StringPage>(page1)
      assertEquals(stringPage.s, page1.s)

      var tail = loadedPageStack.tailOrNull()
      assertNotNull(tail)
      val pageId2 = tail.head.id
      val page2 = tail.head.page
      assertEquals(PageId(0L), pageId2)
      assertIs<IntPage>(page2)
      assertEquals(intPage.i, page2.i)

      tail = tail.tailOrNull()
      assertNull(tail)
   }
}
