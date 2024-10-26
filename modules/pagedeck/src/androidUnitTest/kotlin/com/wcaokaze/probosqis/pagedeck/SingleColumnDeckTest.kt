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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.wcaokaze.probosqis.capsiqum.deck.Deck
import com.wcaokaze.probosqis.capsiqum.deck.sequence
import com.wcaokaze.probosqis.capsiqum.page.Page
import com.wcaokaze.probosqis.capsiqum.page.PageId
import com.wcaokaze.probosqis.capsiqum.page.PageStack
import com.wcaokaze.probosqis.capsiqum.page.PageState
import com.wcaokaze.probosqis.capsiqum.page.PageStateFactory
import com.wcaokaze.probosqis.capsiqum.page.SavedPageState
import com.wcaokaze.probosqis.panoptiqon.WritableCache
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentMapOf
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
import org.robolectric.annotation.GraphicsMode
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SingleColumnDeckTest {
   @get:Rule
   val rule = createComposeRule()

   @AfterTest
   fun after() {
      stopKoin()
   }

   @Composable
   private fun KoinIsolatedContext(
      pageSwitcherState: CombinedPageSwitcherState,
      coroutineScope: CoroutineScope = rememberCoroutineScope(),
      content: @Composable () -> Unit
   ) {
      val koinApplication = remember {
         koinApplication {
            modules(
               module {
                  single { coroutineScope }
                  single { pageSwitcherState }
               }
            )
         }
      }

      LaunchedEffect(Unit) {
         startKoin(koinApplication)
      }

      KoinIsolatedContext(koinApplication, content)
   }

   private class PageImpl(val i: Int) : Page()
   private class PageStateImpl : PageState<PageImpl>()

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

            Button(
               onClick = {
                  pageStackState.removeFromDeck()
               }
            ) {
               Text("Remove Card ${page.i}")
            }
         }
      },
      header = { _, _, _ -> },
      footer = null,
      outgoingTransitions = persistentMapOf(),
      incomingTransitions = persistentMapOf()
   )

   @Composable
   fun SingleColumnPageDeck(
      state: SingleColumnPageDeckState,
      pageSwitcherState: CombinedPageSwitcherState,
      modifier: Modifier = Modifier,
   ) {
      SingleColumnPageDeck(
         state, pageSwitcherState,
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
   fun activeCard() {
      val deckTestTag = "deck"
      val deckWidth = 300.dp

      lateinit var coroutineScope: CoroutineScope
      lateinit var deckState: SingleColumnPageDeckState
      rule.setContent {
         val pageSwitcherState = rememberPageSwitcherState()

         KoinIsolatedContext(
            pageSwitcherState = pageSwitcherState
         ) {
            coroutineScope = rememberCoroutineScope()
            deckState = remember {
               SingleColumnPageDeckState(
                  pageDeckCache = WritableCache(createPageDeck()),
                  pageStackRepository = mockk()
               )
            }

            SingleColumnPageDeck(
               deckState, pageSwitcherState,
               modifier = Modifier
                  .testTag(deckTestTag)
                  .width(deckWidth)
                  .fillMaxHeight()
            )
         }
      }

      rule.mainClock.autoAdvance = false

      rule.runOnIdle {
         assertEquals(0, deckState.activeCardIndex)
      }

      rule.onNodeWithTag(deckTestTag).performTouchInput {
         down(Offset(0.0f, 0.0f))
         moveBy(Offset(-viewConfiguration.touchSlop, 0.0f))
         moveBy(Offset(-(deckWidth / 2 + cardPadding).toPx() + 1.0f, 0.0f))
      }
      rule.runOnIdle {
         assertEquals(0, deckState.activeCardIndex)
      }

      rule.onNodeWithTag(deckTestTag).performTouchInput {
         moveBy(Offset(-2.0f, 0.0f))
      }
      rule.runOnIdle {
         assertEquals(1, deckState.activeCardIndex)
      }

      rule.onNodeWithTag(deckTestTag).performTouchInput {
         up()
      }
      rule.runOnIdle {
         assertEquals(1, deckState.activeCardIndex)
      }

      rule.mainClock.autoAdvance = true

      coroutineScope.launch {
         deckState.deckState.animateScroll(0)
      }
      rule.runOnIdle {
         assertEquals(0, deckState.activeCardIndex)
      }
   }

   @Test
   fun activateCard() {
      lateinit var coroutineScope: CoroutineScope
      lateinit var deckState: SingleColumnPageDeckState
      rule.setContent {
         val pageSwitcherState = rememberPageSwitcherState()

         KoinIsolatedContext(
            pageSwitcherState = pageSwitcherState
         ) {
            coroutineScope = rememberCoroutineScope()
            deckState = remember {
               SingleColumnPageDeckState(
                  pageDeckCache = WritableCache(createPageDeck()),
                  pageStackRepository = mockk()
               )
            }

            SingleColumnPageDeck(
               deckState, pageSwitcherState,
               modifier = Modifier.fillMaxSize()
            )
         }
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
         assertEquals(3, deckState.deckState.firstContentCardIndex)
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
      lateinit var deckState: SingleColumnPageDeckState
      rule.setContent {
         val pageSwitcherState = rememberPageSwitcherState()

         KoinIsolatedContext(
            pageSwitcherState = pageSwitcherState
         ) {
            coroutineScope = rememberCoroutineScope()
            deckState = remember {
               SingleColumnPageDeckState(
                  pageDeckCache = WritableCache(createPageDeck()),
                  pageStackRepository = mockk()
               )
            }

            SingleColumnPageDeck(
               deckState, pageSwitcherState,
               modifier = Modifier.fillMaxSize()
            )
         }
      }

      rule.mainClock.autoAdvance = false

      coroutineScope.launch {
         deckState.activate(3)
      }

      repeat (40) { i ->
         rule.onRoot().captureRoboImage("test/singleColumnDeck_activate/scrolling$i.png")
         rule.mainClock.advanceTimeBy(16L)
      }

      rule.mainClock.autoAdvance = true
      rule.waitForIdle()
      rule.mainClock.autoAdvance = false

      coroutineScope.launch {
         deckState.activate(3)
      }

      repeat (20) { i ->
         rule.onRoot().captureRoboImage("test/singleColumnDeck_activate/nonscrolling$i.png")
         rule.mainClock.advanceTimeBy(16L)
      }
   }

   @Test
   fun addCard_viaDeckState() {
      lateinit var coroutineScope: CoroutineScope
      lateinit var deckState: SingleColumnPageDeckState
      rule.setContent {
         val pageSwitcherState = rememberPageSwitcherState()

         KoinIsolatedContext(
            pageSwitcherState = pageSwitcherState
         ) {
            coroutineScope = rememberCoroutineScope()

            val deck = createPageDeck(cardCount = 2)
            deckState = remember {
               SingleColumnPageDeckState(
                  pageDeckCache = WritableCache(deck),
                  pageStackRepository = mockk {
                     every { savePageStack(any()) } answers { WritableCache(firstArg()) }
                  }
               )
            }

            SingleColumnPageDeck(
               deckState, pageSwitcherState,
               modifier = Modifier.fillMaxSize()
            )
         }
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

      rule.onNodeWithText("Add Card 0").performClick()
      rule.runOnIdle {
         assertCardNumbers(listOf(0, 100, 1), deckState.deck)
         assertEquals(1, deckState.deckState.firstContentCardIndex)
      }
   }

   @Test
   fun removeCard_viaDeckState() {
      lateinit var coroutineScope: CoroutineScope
      lateinit var deckState: SingleColumnPageDeckState
      rule.setContent {
         val pageSwitcherState = rememberPageSwitcherState()

         KoinIsolatedContext(
            pageSwitcherState = pageSwitcherState
         ) {
            coroutineScope = rememberCoroutineScope()

            val deck = createPageDeck(cardCount = 4)
            deckState = remember {
               SingleColumnPageDeckState(
                  pageDeckCache = WritableCache(deck),
                  pageStackRepository = mockk()
               )
            }

            SingleColumnPageDeck(
               deckState, pageSwitcherState,
               modifier = Modifier.fillMaxSize()
            )
         }
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
         assertCardNumbers(listOf(0, 1, 2, 3), deckState.deck)
         assertEquals(0, deckState.deckState.firstContentCardIndex)
      }

      rule.onNodeWithText("Remove Card 0").performClick()
      rule.runOnIdle {
         assertCardNumbers(listOf(1, 2, 3), deckState.deck)
         assertEquals(0, deckState.deckState.firstContentCardIndex)
      }

      coroutineScope.launch {
         deckState.deckState.animateScroll(1)
      }

      rule.onNodeWithText("Remove Card 3").performClick()
      rule.runOnIdle {
         assertCardNumbers(listOf(1, 2), deckState.deck)
         assertEquals(1, deckState.deckState.firstContentCardIndex)
      }
   }
}
