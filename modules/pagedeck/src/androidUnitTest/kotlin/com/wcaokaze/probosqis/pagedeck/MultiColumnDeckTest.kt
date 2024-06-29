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

package com.wcaokaze.probosqis.pagedeck

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
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import com.github.takahirom.roborazzi.captureRoboImage
import com.wcaokaze.probosqis.capsiqum.deck.Deck
import com.wcaokaze.probosqis.capsiqum.deck.PositionInDeck
import com.wcaokaze.probosqis.capsiqum.deck.get
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
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.fail

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MultiColumnDeckTest {
   @get:Rule
   val rule = createComposeRule()

   private class PageImpl(val i: Int) : Page()
   private class PageStateImpl : PageState()

   private val pageComposable = CombinedPageComposable<PageImpl, PageStateImpl>(
      PageStateFactory { _, _, _ -> PageStateImpl() },
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
      outgoingTransitions = persistentMapOf(),
      incomingTransitions = persistentMapOf()
   )

   @OptIn(ExperimentalMaterial3Api::class)
   @Composable
   private fun MultiColumnPageDeck(
      state: MultiColumnPageDeckState,
      pageSwitcherState: CombinedPageSwitcherState,
      pageStateStore: PageStateStore,
      pageStackCount: Int,
      modifier: Modifier = Modifier
   ) {
      MultiColumnPageDeck(
         state, pageSwitcherState, pageStateStore, pageStackCount,
         activeAppBarColors = TopAppBarDefaults.topAppBarColors(),
         inactiveAppBarColors = TopAppBarDefaults.topAppBarColors(),
         PageStackColors(
            background = Color.Transparent,
            content = Color.Black,
            activationAnimColor = Color.DarkGray,
            footer = Color.Transparent,
            footerContent = Color.Black,
         ),
         WindowInsets(0),
         modifier = modifier
      )
   }

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

   private fun createPageDeck(
      vararg pageStacks: Pair<Long, List<Int>>
   ): PageDeck {
      return Deck(
         pageStacks.map { (id, pages) ->
            val savedPageStates
               = pages.map { SavedPageState(PageId(it.toLong()), PageImpl(it)) }

            val pageStackId = PageStack.Id(id)
            val bottom = savedPageStates.first()
            val pageStack = savedPageStates.drop(1)
               .fold(PageStack(pageStackId, bottom), PageStack::added)

            val lazyPageStackState = LazyPageStackState(
               PageStack.Id(id), WritableCache(pageStack), initialVisibility = true
            )
            Deck.Card(lazyPageStackState)
         }
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

         MultiColumnPageDeck(
            deckState, rememberPageSwitcherState(),
            rememberPageStateStore(coroutineScope), pageStackCount = 2,
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

         MultiColumnPageDeck(
            deckState, rememberPageSwitcherState(),
            rememberPageStateStore(coroutineScope), pageStackCount = 2,
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

         MultiColumnPageDeck(
            deckState, rememberPageSwitcherState(),
            rememberPageStateStore(coroutineScope), pageStackCount = 2,
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
   fun activateCard() {
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

         MultiColumnPageDeck(
            deckState, rememberPageSwitcherState(),
            rememberPageStateStore(coroutineScope), pageStackCount = 2,
            modifier = Modifier.fillMaxSize()
         )
      }

      rule.runOnIdle {
         assertEquals(0, deckState.activeCardIndex)
         assertEquals(0, deckState.deckState.firstContentCardIndex)
      }

      coroutineScope.launch {
         deckState.activate(3)
      }

      rule.runOnIdle {
         assertEquals(3, deckState.activeCardIndex)
         assertEquals(2, deckState.deckState.firstContentCardIndex)
      }

      coroutineScope.launch {
         deckState.activate(1)
      }

      rule.runOnIdle {
         assertEquals(1, deckState.activeCardIndex)
         assertEquals(1, deckState.deckState.firstContentCardIndex)
      }
   }

   @Test
   fun activateCard_anim() {
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

         MultiColumnPageDeck(
            deckState, rememberPageSwitcherState(),
            rememberPageStateStore(coroutineScope), pageStackCount = 2,
            modifier = Modifier.fillMaxSize()
         )
      }

      rule.mainClock.autoAdvance = false

      coroutineScope.launch {
         deckState.activate(3)
      }

      repeat (40) { i ->
         rule.onRoot().captureRoboImage("test/multiColumnDeck_activate/scrolling$i.png")
         rule.mainClock.advanceTimeBy(16L)
      }

      rule.mainClock.autoAdvance = true
      rule.waitForIdle()
      rule.mainClock.autoAdvance = false

      coroutineScope.launch {
         deckState.activate(3)
      }

      repeat (20) { i ->
         rule.onRoot().captureRoboImage("test/multiColumnDeck_activate/nonscrolling$i.png")
         rule.mainClock.advanceTimeBy(16L)
      }
   }

   @Test
   fun navigateToPage() {
      val pageDeck = createPageDeck(
         0L to listOf(100),
         1L to listOf(101),
         2L to listOf(102),
         3L to listOf(103),
      )

      lateinit var coroutineScope: CoroutineScope
      lateinit var deckState: MultiColumnPageDeckState
      rule.setContent {
         coroutineScope = rememberCoroutineScope()
         deckState = remember {
            MultiColumnPageDeckState(
               pageDeckCache = WritableCache(pageDeck),
               pageStackRepository = mockk()
            )
         }

         MultiColumnPageDeck(
            deckState, rememberPageSwitcherState(),
            rememberPageStateStore(coroutineScope), pageStackCount = 2,
            modifier = Modifier.fillMaxSize()
         )
      }

      rule.runOnIdle {
         assertEquals(0, deckState.activeCardIndex)
         assertEquals(0, deckState.deckState.firstContentCardIndex)
      }

      coroutineScope.launch {
         deckState.navigateToPage(
            PageId(103),
            fallbackPage = fun (): SavedPageState { fail() }
         )
      }

      rule.runOnIdle {
         assertEquals(3, deckState.activeCardIndex)
         assertEquals(2, deckState.deckState.firstContentCardIndex)
      }

      coroutineScope.launch {
         deckState.navigateToPage(
            PageId(101),
            fallbackPage = fun (): SavedPageState { fail() }
         )
      }

      rule.runOnIdle {
         assertEquals(1, deckState.activeCardIndex)
         assertEquals(1, deckState.deckState.firstContentCardIndex)
      }
   }

   @Test
   fun navigateToPage_fallback() {
      val pageDeck = createPageDeck(
         0L to listOf(100),
         1L to listOf(101),
         2L to listOf(102),
         3L to listOf(103),
      )

      lateinit var coroutineScope: CoroutineScope
      lateinit var deckState: MultiColumnPageDeckState
      rule.setContent {
         coroutineScope = rememberCoroutineScope()
         deckState = remember {
            MultiColumnPageDeckState(
               pageDeckCache = WritableCache(pageDeck),
               pageStackRepository = mockk {
                  every { savePageStack(any()) } answers { WritableCache(firstArg()) }
               }
            )
         }

         MultiColumnPageDeck(
            deckState, rememberPageSwitcherState(),
            rememberPageStateStore(coroutineScope), pageStackCount = 2,
            modifier = Modifier.fillMaxSize()
         )
      }

      rule.runOnIdle {
         assertEquals(0, deckState.activeCardIndex)
         assertEquals(0, deckState.deckState.firstContentCardIndex)
      }

      coroutineScope.launch {
         deckState.navigateToPage(
            PageId(104),
            fallbackPage = { SavedPageState(PageId(204L), PageImpl(4)) }
         )
      }

      rule.runOnIdle {
         assertEquals(1, deckState.activeCardIndex)
         assertEquals(0, deckState.deckState.firstContentCardIndex)

         val pageStack = deckState.deck[1].content.pageStackCache.value
         assertNull(pageStack.tailOrNull())
         assertEquals(PageId(204L), pageStack.head.id)
         val page = pageStack.head.page
         assertIs<PageImpl>(page)
         assertEquals(4, page.i)
      }

      coroutineScope.launch {
         deckState.activate(1)

         deckState.navigateToPage(
            PageId(104),
            fallbackPage = { SavedPageState(PageId(205L), PageImpl(5)) }
         )
      }

      rule.runOnIdle {
         assertEquals(2, deckState.activeCardIndex)
         assertEquals(1, deckState.deckState.firstContentCardIndex)

         val pageStack = deckState.deck[2].content.pageStackCache.value
         assertNull(pageStack.tailOrNull())
         assertEquals(PageId(205L), pageStack.head.id)
         val page = pageStack.head.page
         assertIs<PageImpl>(page)
         assertEquals(5, page.i)
      }

      coroutineScope.launch {
         deckState.activate(5)

         deckState.navigateToPage(
            PageId(104),
            fallbackPage = { SavedPageState(PageId(206L), PageImpl(6)) }
         )
      }

      rule.runOnIdle {
         assertEquals(6, deckState.activeCardIndex)
         assertEquals(5, deckState.deckState.firstContentCardIndex)

         val pageStack = deckState.deck[6].content.pageStackCache.value
         assertNull(pageStack.tailOrNull())
         assertEquals(PageId(206L), pageStack.head.id)
         val page = pageStack.head.page
         assertIs<PageImpl>(page)
         assertEquals(6, page.i)
      }
   }

   @Test
   fun navigateToPage_fallback_ifTargetPageIsBuried() {
      val pageDeck = createPageDeck(
         0L to listOf(100),
         1L to listOf(101),
         2L to listOf(102, 202),
         3L to listOf(103),
      )

      lateinit var coroutineScope: CoroutineScope
      lateinit var deckState: MultiColumnPageDeckState
      rule.setContent {
         coroutineScope = rememberCoroutineScope()
         deckState = remember {
            MultiColumnPageDeckState(
               pageDeckCache = WritableCache(pageDeck),
               pageStackRepository = mockk {
                  every { savePageStack(any()) } answers { WritableCache(firstArg()) }
               }
            )
         }

         MultiColumnPageDeck(
            deckState, rememberPageSwitcherState(),
            rememberPageStateStore(coroutineScope), pageStackCount = 2,
            modifier = Modifier.fillMaxSize()
         )
      }

      rule.runOnIdle {
         assertEquals(0, deckState.activeCardIndex)
         assertEquals(0, deckState.deckState.firstContentCardIndex)
      }

      coroutineScope.launch {
         deckState.navigateToPage(
            PageId(102),
            fallbackPage = { SavedPageState(PageId(204L), PageImpl(4)) }
         )
      }

      rule.runOnIdle {
         assertEquals(1, deckState.activeCardIndex)
         assertEquals(0, deckState.deckState.firstContentCardIndex)

         val actual = deckState.deck.sequence()
            .map { it.content.pageStackCache.value }
            .map { it.head.id.value }
            .toList()

         assertContentEquals(
            listOf(100L, 204L, 101L, 202L, 103L),
            actual
         )
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

         MultiColumnPageDeck(
            deckState, rememberPageSwitcherState(),
            rememberPageStateStore(coroutineScope), pageStackCount = 2,
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
         assertEquals(0, deckState.activeCardIndex)
      }

      rule.onNodeWithText("Add Card 1").performClick()
      rule.runOnIdle {
         assertCardNumbers(listOf(0, 1, 101), deckState.deck)
         assertEquals(1, deckState.deckState.firstContentCardIndex)
         assertEquals(2, deckState.activeCardIndex)
      }

      coroutineScope.launch {
         deckState.deckState.animateScroll(0)
      }

      rule.onNodeWithText("Add Card 0").performClick()
      rule.runOnIdle {
         assertCardNumbers(listOf(0, 100, 1, 101), deckState.deck)
         assertEquals(0, deckState.deckState.firstContentCardIndex)
         assertEquals(1, deckState.activeCardIndex)
      }

      rule.onNodeWithText("Add Card 100").performClick()
      rule.runOnIdle {
         assertCardNumbers(listOf(0, 100, 200, 1, 101), deckState.deck)
         assertEquals(1, deckState.deckState.firstContentCardIndex)
         assertEquals(2, deckState.activeCardIndex)
      }
   }
}
