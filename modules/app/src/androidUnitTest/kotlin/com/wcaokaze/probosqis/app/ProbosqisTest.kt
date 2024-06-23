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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertTopPositionInRootIsEqualTo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.wcaokaze.probosqis.capsiqum.deck.Deck
import com.wcaokaze.probosqis.capsiqum.page.Page
import com.wcaokaze.probosqis.capsiqum.page.PageId
import com.wcaokaze.probosqis.capsiqum.page.PageStack
import com.wcaokaze.probosqis.capsiqum.page.PageState
import com.wcaokaze.probosqis.capsiqum.page.PageStateFactory
import com.wcaokaze.probosqis.capsiqum.page.PageStateStore
import com.wcaokaze.probosqis.capsiqum.page.SavedPageState
import com.wcaokaze.probosqis.error.PError
import com.wcaokaze.probosqis.error.PErrorListState
import com.wcaokaze.probosqis.error.RaisedError
import com.wcaokaze.probosqis.pagedeck.CombinedPageComposable
import com.wcaokaze.probosqis.pagedeck.CombinedPageSwitcherState
import com.wcaokaze.probosqis.pagedeck.LazyPageStackState
import com.wcaokaze.probosqis.pagedeck.MultiColumnPageDeckState
import com.wcaokaze.probosqis.pagedeck.PageDeck
import com.wcaokaze.probosqis.pagedeck.SingleColumnPageDeckState
import com.wcaokaze.probosqis.panoptiqon.WritableCache
import com.wcaokaze.probosqis.resources.Strings
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import org.junit.Rule
import org.junit.runner.RunWith
import org.koin.compose.KoinIsolatedContext
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ProbosqisTest {
   @get:Rule
   val rule = createComposeRule()

   private class PageImpl(val i: Int) : Page()
   private class PageStateImpl : PageState()

   private class ErrorImpl : PError()

   private val allPageComposables = listOf(
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
         outgoingTransitions = persistentMapOf(),
         incomingTransitions = persistentMapOf()
      )
   )

   private fun koinApplication(
      pageStackCount: Int = 2,
      errorListState: PErrorListState = PErrorListState(
         errorListCache = WritableCache(emptyList()),
         itemComposables = emptyList()
      )
   ) = koinApplication {
      val pageDeck = PageDeck(
         children = List(pageStackCount) { i ->
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

      modules(
         module {
            single { CombinedPageSwitcherState(allPageComposables) }

            single {
               PageStateStore(
                  allPageStateFactories = allPageComposables.map { it.pageStateFactory },
                  appCoroutineScope = mockk()
               )
            }

            single {
               MultiColumnPageDeckState(
                  WritableCache(pageDeck),
                  pageStackRepository = mockk()
               )
            }

            single {
               SingleColumnPageDeckState(
                  WritableCache(pageDeck),
                  pageStackRepository = mockk()
               )
            }

            single { errorListState }
         }
      )
   }

   @Test
   fun composeTooNarrowMultiColumn() {
      val probosqisState = ProbosqisState()

      rule.setContent {
         KoinIsolatedContext(koinApplication()) {
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

   @Test
   fun multiColumn_errorNotifierAnim() {
      val errorListState = PErrorListState(
         WritableCache(
            persistentListOf(
               RaisedError(ErrorImpl(), PageId(0L)),
            )
         ),
         itemComposables = listOf(errorItemComposableImpl)
      )

      rule.setContent {
         KoinIsolatedContext(
            koinApplication(errorListState = errorListState)
         ) {
            val probosqisState = remember { ProbosqisState() }
            MultiColumnProbosqis(probosqisState)
         }
      }

      rule.mainClock.autoAdvance = false

      errorListState.raise(ErrorImpl())
      rule.waitForIdle()

      repeat (20) { i ->
         rule.onRoot().captureRoboImage("test/errorNotifierAnim/multiColumn$i.png")
         rule.mainClock.advanceTimeBy(16L)
      }
   }

   @Test
   fun singleColumn_errorNotifierAnim() {
      val errorListState = PErrorListState(
         WritableCache(
            persistentListOf(
               RaisedError(ErrorImpl(), PageId(0L)),
            )
         ),
         itemComposables = listOf(errorItemComposableImpl)
      )

      rule.setContent {
         KoinIsolatedContext(
            koinApplication(errorListState = errorListState)
         ) {
            val probosqisState = remember { ProbosqisState() }
            SingleColumnProbosqis(probosqisState)
         }
      }

      rule.mainClock.autoAdvance = false

      errorListState.raise(ErrorImpl())
      rule.waitForIdle()

      repeat (20) { i ->
         rule.onRoot().captureRoboImage("test/errorNotifierAnim/singleColumn$i.png")
         rule.mainClock.advanceTimeBy(16L)
      }
   }

   @Test
   fun singleColumn_errorNotifierAnim_showAppBar() {
      val errorListState = PErrorListState(
         WritableCache(
            persistentListOf(
               RaisedError(ErrorImpl(), PageId(0L)),
            )
         ),
         itemComposables = listOf(errorItemComposableImpl)
      )

      lateinit var topAppBarText: String

      rule.setContent {
         KoinIsolatedContext(
            koinApplication(errorListState = errorListState)
         ) {
            topAppBarText = Strings.App.topAppBar

            val probosqisState = remember { ProbosqisState() }
            SingleColumnProbosqis(probosqisState)
         }
      }

      val initialTop = rule.runOnIdle {
         val node = rule.onNodeWithText(topAppBarText)
            .fetchSemanticsNode()

         with (node.layoutInfo.density) {
            node.positionInRoot.y.toDp()
         }
      }

      rule.onNodeWithText(topAppBarText).performTouchInput {
         swipeUp(
            startY = centerY,
            endY = centerY - viewConfiguration.touchSlop - 64.dp.toPx()
         )
      }

      rule.onNodeWithText(topAppBarText)
         .assertTopPositionInRootIsEqualTo(initialTop - 64.dp)

      errorListState.raise(ErrorImpl())

      rule.onNodeWithText(topAppBarText)
         .assertTopPositionInRootIsEqualTo(initialTop)
   }

   @Test
   fun singleColumn_showErrorList_showAppBar() {
      val errorListState = PErrorListState(
         WritableCache(
            persistentListOf(
               RaisedError(ErrorImpl(), PageId(0L)),
            )
         ),
         itemComposables = listOf(errorItemComposableImpl)
      )

      lateinit var topAppBarText: String
      lateinit var errorActionButtonContentDescription: String

      rule.setContent {
         KoinIsolatedContext(
            koinApplication(errorListState = errorListState)
         ) {
            topAppBarText = Strings.App.topAppBar
            errorActionButtonContentDescription =
                  Strings.PError.pErrorActionButtonContentDescription

            val probosqisState = remember { ProbosqisState() }
            SingleColumnProbosqis(probosqisState)
         }
      }

      val initialTop = rule.runOnIdle {
         val node = rule.onNodeWithText(topAppBarText)
            .fetchSemanticsNode()

         with (node.layoutInfo.density) {
            node.positionInRoot.y.toDp()
         }
      }

      rule.onNodeWithText(topAppBarText).performTouchInput {
         down(center)
         moveBy(Offset(0.0f, -(viewConfiguration.touchSlop + 32.dp.toPx())))
         moveBy(Offset(0.1f, 0.0f), delayMillis = 3000L)
         up()
      }

      rule.onNodeWithText(topAppBarText)
         .assertTopPositionInRootIsEqualTo(initialTop - 32.dp)

      rule.onNodeWithContentDescription(errorActionButtonContentDescription)
         .performClick()

      rule.onNodeWithText(topAppBarText)
         .assertTopPositionInRootIsEqualTo(initialTop)
   }
}
