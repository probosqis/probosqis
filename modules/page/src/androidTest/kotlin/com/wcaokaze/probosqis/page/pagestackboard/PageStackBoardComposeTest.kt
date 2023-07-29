/*
 * Copyright 2023 wcaokaze
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

package com.wcaokaze.probosqis.page.pagestackboard

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertLeftPositionInRootIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import com.wcaokaze.probosqis.cache.core.WritableCache
import com.wcaokaze.probosqis.ext.kotlin.datetime.MockClock
import com.wcaokaze.probosqis.page.PageStack
import com.wcaokaze.probosqis.page.Page
import com.wcaokaze.probosqis.page.PageComposableSwitcher
import com.wcaokaze.probosqis.page.pageComposable
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertFails

@RunWith(RobolectricTestRunner::class)
class PageStackBoardComposeTest {
   @get:Rule
   val rule = createComposeRule()

   private class TestPage(val i: Int) : Page()

   private val testPageComposable = pageComposable<TestPage>(
      content = { page, _ ->
         Text(
            "${page.i}",
            modifier = Modifier.fillMaxWidth()
         )
      },
      header = { _, _ -> },
      footer = null
   )

   private val pageComposableSwitcher = PageComposableSwitcher(
      allPageComposables = listOf(
         testPageComposable,
      )
   )

   private fun createSingleColumnPageStackBoardState(
      vararg pageStacks: PageStack
   ): SingleColumnPageStackBoardState {
      return createPageStackBoardState(
         ::SingleColumnPageStackBoardState, *pageStacks)
   }

   private fun createMultiColumnPageStackBoardState(
      vararg pageStacks: PageStack
   ): MultiColumnPageStackBoardState {
      return createPageStackBoardState(
         ::MultiColumnPageStackBoardState, *pageStacks)
   }

   private fun <S : PageStackBoardState> createPageStackBoardState(
      constructor: (WritableCache<PageStackBoard>) -> S,
      vararg pageStacks: PageStack
   ): S {
      val rootRow = PageStackBoard.Row(
         pageStacks
            .map { PageStackBoard.PageStack(WritableCache(it)) }
            .toImmutableList()
      )

      val pageStackBoard = PageStackBoard(rootRow)
      val pageStackBoardCache = WritableCache(pageStackBoard)
      return constructor(pageStackBoardCache)
   }

   @Test
   fun singleColumnPageStackBoard_layout() {
      rule.setContent {
         val pageStackBoardState = remember {
            createSingleColumnPageStackBoardState(
               PageStack(TestPage(0), MockClock(minute = 0)),
               PageStack(TestPage(1), MockClock(minute = 1)),
            )
         }

         SingleColumnPageStackBoard(
            pageStackBoardState,
            pageComposableSwitcher,
            WindowInsets(0, 0, 0, 0),
            modifier = Modifier
               .width(100.dp)
         )
      }

      rule.onNodeWithText("0")
         .assertLeftPositionInRootIsEqualTo(0.dp)
         .assertWidthIsEqualTo(100.dp)
   }

   @Test
   fun multiColumnPageStackBoard_layout() {
      val boardWidth = 100.dp

      rule.setContent {
         val pageStackBoardState = remember {
            createMultiColumnPageStackBoardState(
               PageStack(TestPage(0), MockClock(minute = 0)),
               PageStack(TestPage(1), MockClock(minute = 1)),
            )
         }

         MultiColumnPageStackBoard(
            pageStackBoardState,
            pageComposableSwitcher,
            pageStackCount = 2,
            WindowInsets(0, 0, 0, 0),
            modifier = Modifier
               .width(boardWidth)
         )
      }

      val expectedPageStackWidth = (boardWidth - 16.dp) / 2 - 16.dp
      rule.onNodeWithText("0")
         .assertLeftPositionInRootIsEqualTo(16.dp)
         .assertWidthIsEqualTo(expectedPageStackWidth)
      rule.onNodeWithText("1")
         .assertLeftPositionInRootIsEqualTo(boardWidth / 2 + 8.dp)
         .assertWidthIsEqualTo(expectedPageStackWidth)
   }

   @Test
   fun multiColumnPageStackBoard_layout_notEnoughPageStacks() {
      val boardWidth = 100.dp

      rule.setContent {
         val pageStackBoardState = remember {
            createMultiColumnPageStackBoardState(
               PageStack(TestPage(0), MockClock(minute = 0)),
            )
         }

         MultiColumnPageStackBoard(
            pageStackBoardState,
            pageComposableSwitcher,
            pageStackCount = 2,
            WindowInsets(0, 0, 0, 0),
            modifier = Modifier
               .width(boardWidth)
         )
      }

      val expectedPageStackWidth = (boardWidth - 16.dp) / 2 - 16.dp
      rule.onNodeWithText("0")
         .assertLeftPositionInRootIsEqualTo(16.dp)
         .assertWidthIsEqualTo(expectedPageStackWidth)
   }

   @Test
   fun multiColumnPageStackBoard_scroll() {
      val boardWidth = 100.dp
      val boardTestTag = "PageStackBoard"

      val pageStackBoardState = createMultiColumnPageStackBoardState(
         PageStack(TestPage(0), MockClock(minute = 0)),
         PageStack(TestPage(1), MockClock(minute = 1)),
         PageStack(TestPage(2), MockClock(minute = 2)),
      )

      rule.setContent {
         MultiColumnPageStackBoard(
            pageStackBoardState,
            pageComposableSwitcher,
            pageStackCount = 2,
            WindowInsets(0, 0, 0, 0),
            modifier = Modifier
               .width(boardWidth)
               .testTag(boardTestTag)
         )
      }

      rule.onNodeWithText("0")
         .assertLeftPositionInRootIsEqualTo(16.dp)
      rule.onNodeWithText("1")
         .assertLeftPositionInRootIsEqualTo(boardWidth / 2 + 8.dp)
      rule.runOnIdle {
         assertEquals(0f, pageStackBoardState.scrollState.scrollOffset)
      }

      val scrollAmount = 32.dp

      rule.onNodeWithTag(boardTestTag)
         .performTouchInput {
            down(Offset(0.0f, 0.0f))
            moveBy(Offset(-viewConfiguration.touchSlop, 0.0f))
            moveBy(Offset(-scrollAmount.toPx(), 0.0f))
            // upするとsnapする
            // up()
         }

      rule.onNodeWithText("0")
         .assertLeftPositionInRootIsEqualTo(16.dp - scrollAmount)
      rule.onNodeWithText("1")
         .assertLeftPositionInRootIsEqualTo(boardWidth / 2 + 8.dp - scrollAmount)
      rule.runOnIdle {
         assertEquals(
            with (rule.density) { 32.dp.toPx() },
            pageStackBoardState.scrollState.scrollOffset
         )
      }
   }

   @Test
   fun multiColumnPageStackBoard_scrollEdge() {
      val boardWidth = 100.dp
      val boardTestTag = "PageStackBoard"

      val pageStackBoardState = createMultiColumnPageStackBoardState(
         PageStack(TestPage(0), MockClock(minute = 0)),
         PageStack(TestPage(1), MockClock(minute = 1)),
         PageStack(TestPage(2), MockClock(minute = 2)),
      )

      rule.setContent {
         MultiColumnPageStackBoard(
            pageStackBoardState,
            pageComposableSwitcher,
            pageStackCount = 2,
            WindowInsets(0, 0, 0, 0),
            modifier = Modifier
               .width(boardWidth)
               .testTag(boardTestTag)
         )
      }

      rule.onNodeWithText("0").assertLeftPositionInRootIsEqualTo(16.dp)
      rule.runOnIdle {
         assertEquals(0f, pageStackBoardState.scrollState.scrollOffset)
      }

      rule.onNodeWithTag(boardTestTag)
         .performTouchInput {
            down(Offset(0.0f, 0.0f))
            moveBy(Offset(viewConfiguration.touchSlop, 0.0f))
            moveBy(Offset(100.dp.toPx(), 0.0f))
         }

      rule.onNodeWithText("0").assertLeftPositionInRootIsEqualTo(16.dp)
      rule.runOnIdle {
         assertEquals(0f, pageStackBoardState.scrollState.scrollOffset)
      }

      rule.onNodeWithTag(boardTestTag)
         .performTouchInput {
            moveBy(Offset(-viewConfiguration.touchSlop, 0.0f))
            moveBy(Offset(-100.dp.toPx(), 0.0f))
         }

      rule.onNodeWithText("1").assertLeftPositionInRootIsEqualTo(16.dp)
      rule.runOnIdle {
         assertEquals(
            with (rule.density) { (boardWidth - 16.dp).toPx() / 2 },
            pageStackBoardState.scrollState.scrollOffset
         )
      }
   }

   @Test
   fun multiColumnPageStackBoard_scrollEdge_afterSizeChanged() {
      var boardWidth by mutableStateOf(100.dp)
      val boardTestTag = "PageStackBoard"

      val pageStackBoardState = createMultiColumnPageStackBoardState(
         PageStack(TestPage(0), MockClock(minute = 0)),
         PageStack(TestPage(1), MockClock(minute = 1)),
         PageStack(TestPage(2), MockClock(minute = 2)),
      )

      rule.setContent {
         MultiColumnPageStackBoard(
            pageStackBoardState,
            pageComposableSwitcher,
            pageStackCount = 2,
            WindowInsets(0, 0, 0, 0),
            modifier = Modifier
               .width(boardWidth)
               .testTag(boardTestTag)
         )
      }

      rule.onNodeWithText("0").assertLeftPositionInRootIsEqualTo(16.dp)

      rule.onNodeWithTag(boardTestTag)
         .performTouchInput {
            down(Offset(0.0f, 0.0f))
            moveBy(Offset(-viewConfiguration.touchSlop, 0.0f))
            moveBy(Offset(-100.dp.toPx(), 0.0f))
         }

      rule.onNodeWithText("1").assertLeftPositionInRootIsEqualTo(16.dp)
      rule.runOnIdle {
         assertEquals(
            with (rule.density) { (boardWidth - 16.dp).toPx() / 2 },
            pageStackBoardState.scrollState.scrollOffset
         )
      }

      boardWidth = 90.dp

      rule.onNodeWithText("1").assertLeftPositionInRootIsEqualTo(16.dp)
      rule.runOnIdle {
         assertEquals(
            with (rule.density) { (boardWidth - 16.dp).toPx() / 2 },
            pageStackBoardState.scrollState.scrollOffset
         )
      }

      boardWidth = 100.dp

      rule.runOnIdle {
         assertEquals(
            with (rule.density) { (90.dp - 16.dp).toPx() / 2 },
            pageStackBoardState.scrollState.scrollOffset
         )
      }
   }

   @Test
   fun animateScrollTo() {
      val boardWidth = 100.dp
      val boardTestTag = "PageStackBoard"

      val pageStackBoardState = createMultiColumnPageStackBoardState(
         PageStack(TestPage(0), MockClock(minute = 0)),
         PageStack(TestPage(1), MockClock(minute = 1)),
         PageStack(TestPage(2), MockClock(minute = 2)),
         PageStack(TestPage(3), MockClock(minute = 3)),
      )

      lateinit var coroutineScope: CoroutineScope

      rule.setContent {
         coroutineScope = rememberCoroutineScope()

         MultiColumnPageStackBoard(
            pageStackBoardState,
            pageComposableSwitcher,
            pageStackCount = 2,
            WindowInsets(0, 0, 0, 0),
            modifier = Modifier
               .width(boardWidth)
               .testTag(boardTestTag)
         )
      }

      fun ComposeContentTestRule.assertScrollOffset(index: Int) {
         runOnIdle {
            val pageStackWidth = with (density) {
               (boardWidth - 16.dp).toPx() / 2
            }
            assertEquals(
               pageStackWidth * index,
               pageStackBoardState.scrollState.scrollOffset
            )
         }
      }

      rule.onNodeWithText("0").assertLeftPositionInRootIsEqualTo(16.dp)
      rule.assertScrollOffset(0)

      coroutineScope.launch {
         pageStackBoardState.animateScrollTo(1)
      }
      rule.onNodeWithText("1").assertLeftPositionInRootIsEqualTo(16.dp)
      rule.assertScrollOffset(1)

      coroutineScope.launch {
         pageStackBoardState.animateScrollTo(2)
      }
      rule.onNodeWithText("2").assertLeftPositionInRootIsEqualTo(16.dp)
      rule.assertScrollOffset(2)

      coroutineScope.launch {
         pageStackBoardState.animateScrollTo(3)
      }
      // 3が右端でBoardには2つのPageStackが入るため、
      // 3までスクロールしても2までしかスクロールされない
      rule.onNodeWithText("2").assertLeftPositionInRootIsEqualTo(16.dp)
      rule.assertScrollOffset(2)

      coroutineScope.launch {
         pageStackBoardState.animateScrollTo(2)
      }
      rule.onNodeWithText("2").assertLeftPositionInRootIsEqualTo(16.dp)
      rule.assertScrollOffset(2)

      coroutineScope.launch {
         pageStackBoardState.animateScrollTo(1)
      }
      rule.onNodeWithText("1").assertLeftPositionInRootIsEqualTo(16.dp)
      rule.assertScrollOffset(1)

      coroutineScope.launch {
         pageStackBoardState.animateScrollTo(0)
      }
      rule.onNodeWithText("0").assertLeftPositionInRootIsEqualTo(16.dp)
      rule.assertScrollOffset(0)

      coroutineScope.launch {
         assertFails {
            pageStackBoardState.animateScrollTo(4)
         }
         assertFails {
            pageStackBoardState.animateScrollTo(-1)
         }
      }
   }
}
