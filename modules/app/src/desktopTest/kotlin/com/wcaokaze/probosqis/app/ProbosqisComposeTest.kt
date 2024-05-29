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
import com.wcaokaze.probosqis.capsiqum.deck.Deck
import com.wcaokaze.probosqis.capsiqum.page.PageStateStore
import com.wcaokaze.probosqis.pagedeck.CombinedPageSwitcherState
import com.wcaokaze.probosqis.pagedeck.LazyPageStackState
import com.wcaokaze.probosqis.pagedeck.MultiColumnPageDeckState
import com.wcaokaze.probosqis.pagedeck.PageDeck
import com.wcaokaze.probosqis.pagedeck.PageDeckRepository
import com.wcaokaze.probosqis.pagedeck.PageStackRepository
import com.wcaokaze.probosqis.pagedeck.SingleColumnPageDeckState
import com.wcaokaze.probosqis.panoptiqon.WritableCache
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.koin.compose.KoinIsolatedContext
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame

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

      val loadedCache = loadPageDeckOrDefault(
         pageDeckRepository, pageStackRepository = mockk()
      )

      assertSame(pageDeck, loadedCache.value)
   }

   @Suppress("INVISIBLE_MEMBER")
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

      val loadedCache = loadPageDeckOrDefault(pageDeckRepository, pageStackRepository)

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
      val probosqisState = ProbosqisState(
         CombinedPageSwitcherState(allPageComposables = emptyList()),
         PageStateStore(allPageStateFactories = emptyList(), appCoroutineScope = mockk()),
         allErrorItemComposables = emptyList(),
         errorListRepository = mockk {
            every { loadErrorList() } returns WritableCache(emptyList())
         }
      )

      rule.setContent {
         KoinIsolatedContext(
            koinApplication {
               modules(
                  module {
                     single {
                        SingleColumnPageDeckState(
                           WritableCache(PageDeck()),
                           pageStackRepository = mockk()
                        )
                     }
                  }
               )
            }
         ) {
            SingleColumnProbosqis(probosqisState)
         }
      }

      rule.runOnIdle {
         assertIs<SingleColumnPageDeckState>(probosqisState.pageDeckState)
      }
   }

   @Test
   fun getDeckState_multiColumn() {
      val probosqisState = ProbosqisState(
         CombinedPageSwitcherState(allPageComposables = emptyList()),
         PageStateStore(allPageStateFactories = emptyList(), appCoroutineScope = mockk()),
         allErrorItemComposables = emptyList(),
         errorListRepository = mockk {
            every { loadErrorList() } returns WritableCache(emptyList())
         }
      )

      rule.setContent {
         KoinIsolatedContext(
            koinApplication {
               modules(
                  module {
                     single {
                        MultiColumnPageDeckState(
                           WritableCache(PageDeck()),
                           pageStackRepository = mockk()
                        )
                     }
                  }
               )
            }
         ) {
            MultiColumnProbosqis(probosqisState)
         }
      }

      rule.runOnIdle {
         assertIs<MultiColumnPageDeckState>(probosqisState.pageDeckState)
      }
   }

   @Test
   fun getDeckState_switchingSingleColumnMultiColumn() {
      val probosqisState = ProbosqisState(
         CombinedPageSwitcherState(allPageComposables = emptyList()),
         PageStateStore(allPageStateFactories = emptyList(), appCoroutineScope = mockk()),
         allErrorItemComposables = emptyList(),
         errorListRepository = mockk {
            every { loadErrorList() } returns WritableCache(emptyList())
         }
      )

      var isMultiColumn by mutableStateOf(false)

      rule.setContent {
         KoinIsolatedContext(
            koinApplication {
               modules(
                  module {
                     single {
                        MultiColumnPageDeckState(
                           WritableCache(PageDeck()),
                           pageStackRepository = mockk()
                        )
                     }

                     single {
                        SingleColumnPageDeckState(
                           WritableCache(PageDeck()),
                           pageStackRepository = mockk()
                        )
                     }
                  }
               )
            }
         ) {
            if (isMultiColumn) {
               MultiColumnProbosqis(probosqisState)
            } else {
               SingleColumnProbosqis(probosqisState)
            }
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
         CombinedPageSwitcherState(allPageComposables = emptyList()),
         PageStateStore(allPageStateFactories = emptyList(), appCoroutineScope = mockk()),
         allErrorItemComposables = emptyList(),
         errorListRepository = mockk {
            every { loadErrorList() } returns WritableCache(emptyList())
         }
      )

      assertFails {
         probosqisState.pageDeckState
      }
   }
}
