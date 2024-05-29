/*
 * Copyright 2024 wcaokaze
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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.wcaokaze.probosqis.capsiqum.deck.Deck
import com.wcaokaze.probosqis.capsiqum.page.Page
import com.wcaokaze.probosqis.capsiqum.page.PageId
import com.wcaokaze.probosqis.capsiqum.page.PageStack
import com.wcaokaze.probosqis.capsiqum.page.PageState
import com.wcaokaze.probosqis.capsiqum.page.PageStateFactory
import com.wcaokaze.probosqis.capsiqum.page.PageStateStore
import com.wcaokaze.probosqis.capsiqum.page.SavedPageState
import com.wcaokaze.probosqis.pagedeck.CombinedPageComposable
import com.wcaokaze.probosqis.pagedeck.CombinedPageSwitcherState
import com.wcaokaze.probosqis.pagedeck.LazyPageStackState
import com.wcaokaze.probosqis.pagedeck.MultiColumnPageDeckState
import com.wcaokaze.probosqis.pagedeck.PageDeck
import com.wcaokaze.probosqis.panoptiqon.WritableCache
import io.mockk.every
import io.mockk.mockk
import org.junit.Rule
import org.junit.runner.RunWith
import org.koin.compose.KoinIsolatedContext
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
class ProbosqisTest {
   @get:Rule
   val rule = createComposeRule()

   @Test
   fun composeTooNarrowMultiColumn() {
      class PageImpl(val i: Int) : Page()
      class PageStateImpl : PageState()

      val allPageComposables = listOf(
         CombinedPageComposable<PageImpl, PageStateImpl>(
            PageStateFactory { _, _ -> PageStateImpl() },
            content = { page, _, _, _ ->
               Text(
                  "content${page.i}",
                  modifier = Modifier.fillMaxWidth()
               )
            },
            header = { _, _, _ -> },
            footer = { _, _, _ -> },
            pageTransitions = {}
         )
      )

      val probosqisState = ProbosqisState(
         CombinedPageSwitcherState(allPageComposables),
         PageStateStore(
            allPageStateFactories = allPageComposables.map { it.pageStateFactory },
            appCoroutineScope = mockk()
         ),
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
                        val pageDeck = PageDeck(
                           children = List(2) { i ->
                              val pageStack = PageStack(
                                 PageStack.Id(i.toLong()),
                                 SavedPageState(
                                    PageId(i.toLong()),
                                    PageImpl(i)
                                 )
                              )

                              Deck.Card(
                                 LazyPageStackState(
                                    pageStack.id, WritableCache(pageStack),
                                    initialVisibility = true
                                 )
                              )
                           }
                        )

                        MultiColumnPageDeckState(
                           WritableCache(pageDeck),
                           pageStackRepository = mockk()
                        )
                     }
                  }
               )
            }
         ) {
            Box(
               Modifier
                  .requiredWidth(60.dp)
                  .requiredHeight(800.dp)
            ) {
               MultiColumnProbosqis(probosqisState)
            }
         }
      }

      rule.onNodeWithText("content0").assertExists()
   }
}
