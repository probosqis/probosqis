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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import com.wcaokaze.probosqis.app.pagedeck.LazyPageStackState
import com.wcaokaze.probosqis.app.pagedeck.MultiColumnPageDeckState
import com.wcaokaze.probosqis.app.pagedeck.PageDeck
import com.wcaokaze.probosqis.app.pagedeck.PageDeckRepository
import com.wcaokaze.probosqis.app.pagedeck.PageStackRepository
import com.wcaokaze.probosqis.app.pagedeck.SingleColumnPageDeckState
import com.wcaokaze.probosqis.capsiqum.deck.Deck
import com.wcaokaze.probosqis.panoptiqon.WritableCache
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.IOException
import kotlin.test.*

@RunWith(JUnit4::class)
class ProbosqisComposeTest {
   @get:Rule
   val rule = createComposeRule()

   @Test
   fun loadPageDeck() {
      val pageDeck = PageDeck()

      val pageDeckRepository = mockk<PageDeckRepository> {
         every { loadPageDeck() } returns WritableCache(pageDeck)
      }

      val probosqisState = ProbosqisState(
         allPageComposables = emptyList(),
         pageDeckRepository,
         pageStackRepository = mockk(),
         coroutineScope = mockk()
      )

      val loadedCache = probosqisState.loadPageDeckOrDefault()

      assertSame(pageDeck, loadedCache.value)
   }

   @Test
   fun loadPageDeck_default() {
      val pageDeckRepository = mockk<PageDeckRepository> {
         every { loadPageDeck() } throws IOException()
         every { savePageDeck(any()) } answers { WritableCache(firstArg()) }
      }

      val pageStackRepository = mockk<PageStackRepository> {
         every { savePageStack(any()) } answers { WritableCache(firstArg()) }
         every { deleteAllPageStacks() } returns Unit
      }

      val probosqisState = ProbosqisState(
         allPageComposables = emptyList(),
         pageDeckRepository,
         pageStackRepository,
         coroutineScope = mockk()
      )

      val loadedCache = probosqisState.loadPageDeckOrDefault()

      verify { pageStackRepository.deleteAllPageStacks() }
      assertEquals(2, loadedCache.value.rootRow.childCount)

      val pageStack1 = loadedCache.value
         .let { assertIs<Deck.Card<*>>(it.rootRow[0]) }
         .let { assertIs<LazyPageStackState>(it.content) }
         .pageStackCache.value
      assertIs<TestPage>(pageStack1.head.page)
      assertNull(pageStack1.tailOrNull())
      verify { pageStackRepository.savePageStack(pageStack1) }

      val pageStack2 = loadedCache.value
         .let { assertIs<Deck.Card<*>>(it.rootRow[1]) }
         .let { assertIs<LazyPageStackState>(it.content) }
         .pageStackCache.value
      assertIs<TestPage>(pageStack2.head.page)
      assertNull(pageStack2.tailOrNull())
      verify { pageStackRepository.savePageStack(pageStack2) }
   }

   @Test
   fun getDeckState_singleColumn() {
      val pageDeck = PageDeck()

      val probosqisState = ProbosqisState(
         allPageComposables = emptyList(),
         pageDeckRepository = mockk {
            every { loadPageDeck() } returns WritableCache(pageDeck)
         },
         pageStackRepository = mockk(),
         coroutineScope = mockk()
      )

      rule.setContent {
         SingleColumnProbosqis(probosqisState)
      }

      rule.runOnIdle {
         assertIs<SingleColumnPageDeckState>(probosqisState.pageDeckState)
      }
   }

   @Test
   fun getDeckState_multiColumn() {
      val pageDeck = PageDeck()

      val probosqisState = ProbosqisState(
         allPageComposables = emptyList(),
         pageDeckRepository = mockk {
            every { loadPageDeck() } returns WritableCache(pageDeck)
         },
         pageStackRepository = mockk(),
         coroutineScope = mockk()
      )

      rule.setContent {
         MultiColumnProbosqis(probosqisState)
      }

      rule.runOnIdle {
         assertIs<MultiColumnPageDeckState>(probosqisState.pageDeckState)
      }
   }

   @Test
   fun getDeckState_switchingSingleColumnMultiColumn() {
      val pageDeck = PageDeck()

      val probosqisState = ProbosqisState(
         allPageComposables = emptyList(),
         pageDeckRepository = mockk {
            every { loadPageDeck() } returns WritableCache(pageDeck)
         },
         pageStackRepository = mockk(),
         coroutineScope = mockk()
      )

      var isMultiColumn by mutableStateOf(false)

      rule.setContent {
         if (isMultiColumn) {
            MultiColumnProbosqis(probosqisState)
         } else {
            SingleColumnProbosqis(probosqisState)
         }
      }

      rule.runOnIdle {
         assertIs<SingleColumnPageDeckState>(probosqisState.pageDeckState)
      }

      isMultiColumn = true

      rule.runOnIdle {
         assertIs<MultiColumnPageDeckState>(probosqisState.pageDeckState)
      }

      isMultiColumn = false

      rule.runOnIdle {
         assertIs<SingleColumnPageDeckState>(probosqisState.pageDeckState)
      }
   }

   @Test
   fun getDeckState_beforeComposition() {
      val probosqisState = ProbosqisState(
         allPageComposables = emptyList(),
         pageDeckRepository = mockk(),
         pageStackRepository = mockk(),
         coroutineScope = mockk()
      )

      assertFails {
         probosqisState.pageDeckState
      }
   }
}
