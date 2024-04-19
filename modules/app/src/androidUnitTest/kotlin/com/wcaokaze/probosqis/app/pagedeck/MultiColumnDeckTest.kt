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

package com.wcaokaze.probosqis.app.pagedeck

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.wcaokaze.probosqis.capsiqum.deck.Deck
import com.wcaokaze.probosqis.capsiqum.deck.PositionInDeck
import com.wcaokaze.probosqis.capsiqum.deck.sequence
import com.wcaokaze.probosqis.capsiqum.page.Page
import com.wcaokaze.probosqis.capsiqum.page.PageId
import com.wcaokaze.probosqis.capsiqum.page.PageStack
import com.wcaokaze.probosqis.capsiqum.page.PageState
import com.wcaokaze.probosqis.capsiqum.page.PageStateFactory
import com.wcaokaze.probosqis.capsiqum.page.PageStateStore
import com.wcaokaze.probosqis.capsiqum.page.SavedPageState
import com.wcaokaze.probosqis.panoptiqon.WritableCache
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class MultiColumnDeckTest {
   @get:Rule
   val rule = createComposeRule()

   private class PageImpl(val i: Int) : Page()
   private class PageStateImpl : PageState()

   private val pageComposable = CombinedPageComposable<PageImpl, PageStateImpl>(
      PageStateFactory { _, _ -> PageStateImpl() },
      content = { page, _, pageStackState, _ ->
         Column {
            Text("${page.i}")

            Button(
               onClick = {
                  val newPageStack = PageStack(
                     SavedPageState(
                        PageId(page.i + 100L),
                        PageImpl(page.i + 100)
                     )
                  )
                  pageStackState.addColumn(newPageStack)
               }
            ) {
               Text("Add Card ${page.i}")
            }
         }
      },
      header = { _, _, _ -> },
      footer = null,
      pageTransitions = {}
   )

   @Composable
   private fun rememberPageSwitcherState() = remember {
      CombinedPageSwitcherState(
         listOf(pageComposable)
      )
   }

   @Composable
   private fun rememberPageStateStore(coroutineScope: CoroutineScope) = remember {
      PageStateStore(
         listOf(pageComposable.pageStateFactory),
         coroutineScope
      )
   }

   private fun createPageDeck(cardCount: Int = 4): PageDeck {
      return Deck(
         List(cardCount) { i ->
            val pageStackId = PageStack.Id(i.toLong())
            val pageStack = PageStack(
               pageStackId,
               SavedPageState(
                  PageId(i.toLong()),
                  PageImpl(i)
               )
            )
            val lazyPageStackState = LazyPageStackState(
               pageStackId, WritableCache(pageStack), initialVisibility = true
            )
            Deck.Card(lazyPageStackState)
         }
      )
   }

   @Test
   fun activeCard_detectClick() {
      lateinit var coroutineScope: CoroutineScope
      lateinit var deckState: MultiColumnPageDeckState
      rule.setContent {
         coroutineScope = rememberCoroutineScope()
         deckState = remember {
            MultiColumnPageDeckState(
               pageDeckCache = WritableCache(createPageDeck()),
               pageStackRepository = mockk()
            )
         }

         @OptIn(ExperimentalMaterial3Api::class)
         MultiColumnPageDeck(
            deckState, rememberPageSwitcherState(),
            rememberPageStateStore(coroutineScope), pageStackCount = 2,
            activeAppBarColors = TopAppBarDefaults.topAppBarColors(),
            inactiveAppBarColors = TopAppBarDefaults.topAppBarColors(),
            pageStackBackgroundColor = Color.Transparent,
            pageStackFooterBackgroundColor = Color.Transparent, WindowInsets(0),
            modifier = Modifier.fillMaxSize()
         )
      }

      rule.onNodeWithText("1").performClick()

      rule.runOnIdle {
         assertEquals(1, deckState.activeCardIndex)
      }

      coroutineScope.launch {
         deckState.deckState.animateScroll(2)
      }

      rule.onNodeWithText("2").performClick()

      rule.runOnIdle {
         assertEquals(2, deckState.activeCardIndex)
      }

      rule.onNodeWithText("1").performClick()

      rule.runOnIdle {
         assertEquals(1, deckState.activeCardIndex)
      }
   }

   @Test
   fun activeCard_scrolling() {
      lateinit var coroutineScope: CoroutineScope
      lateinit var deckState: MultiColumnPageDeckState
      rule.setContent {
         coroutineScope = rememberCoroutineScope()
         deckState = remember {
            MultiColumnPageDeckState(
               pageDeckCache = WritableCache(createPageDeck()),
               pageStackRepository = mockk()
            )
         }

         @OptIn(ExperimentalMaterial3Api::class)
         MultiColumnPageDeck(
            deckState, rememberPageSwitcherState(),
            rememberPageStateStore(coroutineScope), pageStackCount = 2,
            activeAppBarColors = TopAppBarDefaults.topAppBarColors(),
            inactiveAppBarColors = TopAppBarDefaults.topAppBarColors(),
            pageStackBackgroundColor = Color.Transparent,
            pageStackFooterBackgroundColor = Color.Transparent, WindowInsets(0),
            modifier = Modifier.fillMaxSize()
         )
      }

      rule.runOnIdle {
         assertEquals(0, deckState.activeCardIndex)
      }

      coroutineScope.launch {
         deckState.deckState.animateScroll(1, PositionInDeck.FirstVisible)
      }
      rule.runOnIdle {
         assertEquals(1, deckState.activeCardIndex)
      }

      rule.onNodeWithText("2").performClick()
      rule.runOnIdle {
         assertEquals(2, deckState.activeCardIndex)
      }

      coroutineScope.launch {
         deckState.deckState.animateScroll(1, PositionInDeck.LastVisible)
      }
      rule.runOnIdle {
         assertEquals(1, deckState.activeCardIndex)
      }
   }

   @Test
   fun activeCard_scrolling_windowInsets() {
      lateinit var coroutineScope: CoroutineScope
      lateinit var deckState: MultiColumnPageDeckState
      rule.setContent {
         coroutineScope = rememberCoroutineScope()
         deckState = remember {
            MultiColumnPageDeckState(
               pageDeckCache = WritableCache(createPageDeck()),
               pageStackRepository = mockk()
            )
         }

         @OptIn(ExperimentalMaterial3Api::class)
         MultiColumnPageDeck(
            deckState, rememberPageSwitcherState(),
            rememberPageStateStore(coroutineScope), pageStackCount = 2,
            activeAppBarColors = TopAppBarDefaults.topAppBarColors(),
            inactiveAppBarColors = TopAppBarDefaults.topAppBarColors(),
            pageStackBackgroundColor = Color.Transparent,
            pageStackFooterBackgroundColor = Color.Transparent,
            WindowInsets(left = 32.dp, right = 32.dp),
            modifier = Modifier.fillMaxSize()
         )
      }

      coroutineScope.launch {
         deckState.deckState.animateScroll(1, PositionInDeck.FirstVisible)
      }
      rule.runOnIdle {
         assertEquals(1, deckState.activeCardIndex)
      }

      coroutineScope.launch {
         deckState.deckState.animateScroll(2, PositionInDeck.FirstVisible)
      }
      rule.runOnIdle {
         assertEquals(2, deckState.activeCardIndex)
      }

      rule.onNodeWithText("3").performClick()
      rule.runOnIdle {
         assertEquals(3, deckState.activeCardIndex)
      }

      coroutineScope.launch {
         deckState.deckState.animateScroll(2, PositionInDeck.LastVisible)
      }
      rule.runOnIdle {
         assertEquals(2, deckState.activeCardIndex)
      }
   }

   @Test
   fun addCard_viaDeckState() {
      lateinit var coroutineScope: CoroutineScope
      lateinit var deckState: MultiColumnPageDeckState
      rule.setContent {
         coroutineScope = rememberCoroutineScope()

         val deck = createPageDeck(cardCount = 2)
         deckState = remember {
            MultiColumnPageDeckState(
               pageDeckCache = WritableCache(deck),
               pageStackRepository = mockk {
                  every { savePageStack(any()) } answers { WritableCache(firstArg()) }
               }
            )
         }

         @OptIn(ExperimentalMaterial3Api::class)
         MultiColumnPageDeck(
            deckState, rememberPageSwitcherState(),
            rememberPageStateStore(coroutineScope), pageStackCount = 2,
            activeAppBarColors = TopAppBarDefaults.topAppBarColors(),
            inactiveAppBarColors = TopAppBarDefaults.topAppBarColors(),
            pageStackBackgroundColor = Color.Transparent,
            pageStackFooterBackgroundColor = Color.Transparent, WindowInsets(0),
            modifier = Modifier.fillMaxSize()
         )
      }

      fun assertCardNumbers(expected: List<Int>, actual: PageDeck) {
         val contents = actual.sequence()
            .map { card ->
               val pageStack = card.content.pageStackCache.value
               val pageId = pageStack.head.id
               pageId.value.toInt()
            }
            .toList()

         assertContentEquals(expected, contents)
      }

      rule.runOnIdle {
         assertCardNumbers(listOf(0, 1), deckState.deck)
         assertEquals(0, deckState.deckState.firstContentCardIndex)
      }

      rule.onNodeWithText("Add Card 1").performClick()
      rule.runOnIdle {
         assertCardNumbers(listOf(0, 1, 101), deckState.deck)
         assertEquals(1, deckState.deckState.firstContentCardIndex)
      }

      coroutineScope.launch {
         deckState.deckState.animateScroll(0)
      }

      rule.onNodeWithText("Add Card 0").performClick()
      rule.runOnIdle {
         assertCardNumbers(listOf(0, 100, 1, 101), deckState.deck)
         assertEquals(0, deckState.deckState.firstContentCardIndex)
      }

      rule.onNodeWithText("Add Card 100").performClick()
      rule.runOnIdle {
         assertCardNumbers(listOf(0, 100, 200, 1, 101), deckState.deck)
         assertEquals(1, deckState.deckState.firstContentCardIndex)
      }
   }
}
