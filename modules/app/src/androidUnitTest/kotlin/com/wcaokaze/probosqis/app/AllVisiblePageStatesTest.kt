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

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.wcaokaze.probosqis.capsiqum.deck.Deck
import com.wcaokaze.probosqis.capsiqum.deck.get
import com.wcaokaze.probosqis.capsiqum.deck.sequence
import com.wcaokaze.probosqis.capsiqum.page.PageId
import com.wcaokaze.probosqis.capsiqum.page.PageStack
import com.wcaokaze.probosqis.capsiqum.page.PageStateFactory
import com.wcaokaze.probosqis.capsiqum.page.SavedPageState
import com.wcaokaze.probosqis.error.PErrorListState
import com.wcaokaze.probosqis.page.PPage
import com.wcaokaze.probosqis.page.PPageComposable
import com.wcaokaze.probosqis.page.PPageState
import com.wcaokaze.probosqis.page.PPageSwitcherState
import com.wcaokaze.probosqis.pagedeck.LazyPageStackState
import com.wcaokaze.probosqis.pagedeck.PageDeck
import com.wcaokaze.probosqis.pagedeck.SingleColumnPageDeckState
import com.wcaokaze.probosqis.panoptiqon.WritableCache
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.junit.Rule
import org.junit.runner.RunWith
import org.koin.compose.KoinIsolatedContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals

@RunWith(RobolectricTestRunner::class)
class AllVisiblePageStatesTest {
   @get:Rule
   val rule = createComposeRule()

   private class PageImpl(val id: Long) : PPage()
   private class PageStateImpl : PPageState<PageImpl>()

   private val allPageComposables = listOf(
      PPageComposable<PageImpl, PageStateImpl>(
         PageStateFactory { _, _ -> PageStateImpl() },
         content = { page, state, _ ->
            Column {
               Button(
                  onClick = {
                     state.startPage(
                        PageImpl(page.id + 1)
                     )
                  }
               ) {
                  Text("Start Page ${page.id}")
               }

               Button(
                  onClick = {
                     state.addColumn(
                        PageImpl(page.id + 1)
                     )
                  }
               ) {
                  Text("Add PageStack ${page.id}")
               }
            }
         },
         header = { _, _ -> },
         footer = null,
         pageTransitions = {}
      )
   )

   @AfterTest
   fun afterTest() {
      stopKoin()
   }

   @Composable
   private fun KoinIsolatedContext(
      initialPageDeck: PageDeck,
      content: @Composable () -> Unit
   ) {
      val coroutineScope = rememberCoroutineScope()

      val koinApplication = remember {
         koinApplication {
            modules(
               module {
                  single { coroutineScope }

                  single { PPageSwitcherState(allPageComposables) }

                  single {
                     SingleColumnPageDeckState(
                        WritableCache(initialPageDeck),
                        pageStackRepository = mockk {
                           every { savePageStack(any()) } answers { WritableCache(firstArg()) }
                        }
                     )
                  }

                  single {
                     PErrorListState(
                        errorListCache = WritableCache(emptyList()),
                        itemComposables = emptyList()
                     )
                  }
               }
            )
         }
      }

      LaunchedEffect(Unit) {
         startKoin(koinApplication)
      }

      KoinIsolatedContext(koinApplication, content)
   }

   private fun createPageDeck(pageStackCount: Int) = PageDeck(
      List(pageStackCount) { i ->
         val pageStackId = PageStack.Id(i.toLong())
         val pageStack = PageStack(
            pageStackId,
            SavedPageState(
               PageId(i.toLong()),
               PageImpl(i.toLong())
            )
         )

         Deck.Card(
            LazyPageStackState(
               pageStack.id,
               WritableCache(pageStack),
               initialVisibility = true
            )
         )
      }
   )

   private val Deck.Card<LazyPageStackState>.forefrontPageState
      get() = content.getIfInitialized()?.forefrontPageState

   @Test
   fun allVisiblePageStates_onlyForefront() {
      val probosqisState = ProbosqisState()

      rule.setContent {
         KoinIsolatedContext(createPageDeck(1)) {
            SingleColumnProbosqis(
               probosqisState,
               onRequestCloseWindow = {}
            )
         }
      }

      // 全てのPageに対応するPageStateをインスタンス化させるためひとつずつ追加する
      rule.onNodeWithText("Start Page 0")   .performClick() // [[1, 0]]
      rule.onNodeWithText("Add PageStack 1").performClick() // [[1, 0], [2]]
      rule.onNodeWithText("Start Page 2")   .performClick() // [[1, 0], [2, 3]]
      rule.onNodeWithText("Start Page 3")   .performClick() // [[1, 0], [2, 3, 4]]
      rule.onNodeWithText("Add PageStack 4").performClick() // [[1, 0], [2, 3, 4], [5]]

      rule.runOnIdle {
         val deck = probosqisState.pageDeckState.deck

         assertContentEquals(
            deck.sequence().map { it.forefrontPageState } .toList(),
            probosqisState.allVisiblePageStates.toList()
         )
      }
   }

   @Test
   fun allVisiblePageStates_onlyInstantiated() {
      lateinit var coroutineScope: CoroutineScope
      val probosqisState = ProbosqisState()

      rule.setContent {
         coroutineScope = rememberCoroutineScope()

         KoinIsolatedContext(createPageDeck(5)) {
            SingleColumnProbosqis(
               probosqisState,
               onRequestCloseWindow = {}
            )
         }
      }

      coroutineScope.launch {
         probosqisState.pageDeckState.deckState.animateScroll(1)
      }

      rule.runOnIdle {
         val deck = probosqisState.pageDeckState.deck

         assertContentEquals(
            listOf(
               deck[0].forefrontPageState,
               deck[1].forefrontPageState,
               deck[2].forefrontPageState, // SingleColumnProbosqisは左右のPageStackもコンポーズされる
            ),
            probosqisState.allVisiblePageStates.toList()
         )
      }
   }
}
