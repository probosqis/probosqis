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
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.Dp
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
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertIs

@RunWith(RobolectricTestRunner::class)
class PageStackBoardComposeTest {
   @get:Rule
   val rule = createComposeRule()

   private val pageStackBoardTag = "PageStackBoard"

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

   @Composable
   private fun SingleColumnPageStackBoard(
      state: SingleColumnPageStackBoardState,
      width: Dp
   ) {
      SingleColumnPageStackBoard(
         state,
         pageComposableSwitcher,
         WindowInsets(0, 0, 0, 0),
         modifier = Modifier
            .width(width)
            .testTag(pageStackBoardTag)
      )
   }

   @Composable
   private fun MultiColumnPageStackBoard(
      state: MultiColumnPageStackBoardState,
      width: Dp
   ) {
      MultiColumnPageStackBoard(
         state,
         pageComposableSwitcher,
         pageStackCount = 2,
         WindowInsets(0, 0, 0, 0),
         modifier = Modifier
            .width(width)
            .testTag(pageStackBoardTag)
      )
   }

   private fun createSingleColumnPageStackBoardState(
      pageStackCount: Int
   ): SingleColumnPageStackBoardState {
      return createPageStackBoardState(
         ::SingleColumnPageStackBoardState, pageStackCount)
   }

   private fun createMultiColumnPageStackBoardState(
      pageStackCount: Int
   ): MultiColumnPageStackBoardState {
      return createPageStackBoardState(
         ::MultiColumnPageStackBoardState, pageStackCount)
   }

   private fun <S : PageStackBoardState> createPageStackBoardState(
      constructor: (WritableCache<PageStackBoard>) -> S,
      pageStackCount: Int
   ): S {
      val rootRow = PageStackBoard.Row(
         List(pageStackCount) { createPageStack(it) }.toImmutableList()
      )

      val pageStackBoard = PageStackBoard(rootRow)
      val pageStackBoardCache = WritableCache(pageStackBoard)
      return constructor(pageStackBoardCache)
   }

   private fun createPageStack(i: Int): PageStackBoard.PageStack {
      val page = TestPage(i)
      val pageStack = PageStack(page, MockClock(minute = i))
      val cache = WritableCache(pageStack)
      return PageStackBoard.PageStack(cache)
   }

   @Test
   fun singleColumnPageStackBoard_layout() {
      rule.setContent {
         val pageStackBoardState = remember {
            createSingleColumnPageStackBoardState(pageStackCount = 2)
         }

         SingleColumnPageStackBoard(pageStackBoardState, width = 100.dp)
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
            createMultiColumnPageStackBoardState(pageStackCount = 2)
         }

         MultiColumnPageStackBoard(pageStackBoardState, boardWidth)
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
            createMultiColumnPageStackBoardState(pageStackCount = 1)
         }

         MultiColumnPageStackBoard(pageStackBoardState, boardWidth)
      }

      val expectedPageStackWidth = (boardWidth - 16.dp) / 2 - 16.dp
      rule.onNodeWithText("0")
         .assertLeftPositionInRootIsEqualTo(16.dp)
         .assertWidthIsEqualTo(expectedPageStackWidth)
   }

   @Test
   fun multiColumnPageStackBoard_layout_omitComposingInvisibles() {
      val boardWidth = 100.dp

      val pageStackBoardState
            = createMultiColumnPageStackBoardState(pageStackCount = 5)

      lateinit var coroutineScope: CoroutineScope
      rule.setContent {
         coroutineScope = rememberCoroutineScope()
         MultiColumnPageStackBoard(pageStackBoardState, boardWidth)
      }

      rule.onNodeWithText("0").assertExists()
      rule.onNodeWithText("1").assertExists()
      rule.onNodeWithText("2").assertExists() // 2はギリギリ存在する（影が見えるため）
      rule.onNodeWithText("3").assertDoesNotExist()
      rule.onNodeWithText("4").assertDoesNotExist()

      coroutineScope.launch {
         pageStackBoardState.animateScrollTo(1)
      }

      rule.onNodeWithText("0").assertExists()
      rule.onNodeWithText("1").assertExists()
      rule.onNodeWithText("2").assertExists()
      rule.onNodeWithText("3").assertExists()
      rule.onNodeWithText("4").assertDoesNotExist()

      coroutineScope.launch {
         pageStackBoardState.animateScrollTo(2)
      }

      rule.onNodeWithText("0").assertDoesNotExist()
      rule.onNodeWithText("1").assertExists() // 1はギリギリ存在する
      rule.onNodeWithText("2").assertExists()
      rule.onNodeWithText("3").assertExists()
      rule.onNodeWithText("4").assertExists()

      coroutineScope.launch {
         pageStackBoardState.animateScrollTo(3)
      }

      rule.onNodeWithText("0").assertDoesNotExist()
      rule.onNodeWithText("1").assertDoesNotExist()
      rule.onNodeWithText("2").assertExists()
      rule.onNodeWithText("3").assertExists()
      rule.onNodeWithText("4").assertExists()
   }

   @Test
   fun multiColumnPageStackBoard_layout_mutatePageStackBoard() {
      fun assertPageStacks(
         expectedPageNumbers: List<Int>,
         pageStackBoard: PageStackBoard
      ) {
         val pageStackCount = pageStackBoard.rootRow.leafCount
         assertEquals(expectedPageNumbers.size, pageStackCount)

         for (i in 0 until pageStackCount) {
            val page = pageStackBoard[i].cache.value.head
            assertIs<TestPage>(page)
            assertEquals(expectedPageNumbers[i], page.i)
         }
      }

      fun assertPageStackLayoutStates(
         pageStackBoard: PageStackBoard,
         layoutState: LayoutState
      ) {
         val pageStackCount = pageStackBoard.rootRow.leafCount
         val ids = (0 until pageStackCount)
            .map { pageStackBoard[it].cache.value.id }

         assertEquals(ids, layoutState.layoutStateList.map { it.pageStackId })

         assertEquals(pageStackCount, layoutState.layoutStateMap.size)
         for (id in ids) {
            assertContains(layoutState.layoutStateMap, id)
         }
      }

      val boardWidth = 100.dp

      val pageStackBoardState
            = createMultiColumnPageStackBoardState(pageStackCount = 2)

      rule.setContent {
         MultiColumnPageStackBoard(pageStackBoardState, boardWidth)
      }

      rule.runOnIdle {
         assertPageStacks(listOf(0, 1), pageStackBoardState.pageStackBoard)

         assertPageStackLayoutStates(
            pageStackBoardState.pageStackBoard, pageStackBoardState.layout)
      }

      // ---- insert first ----

      pageStackBoardState.pageStackBoard = PageStackBoard(
         pageStackBoardState.pageStackBoard.rootRow.inserted(0, createPageStack(2))
      )

      rule.runOnIdle {
         assertPageStacks(listOf(2, 0, 1), pageStackBoardState.pageStackBoard)

         assertPageStackLayoutStates(
            pageStackBoardState.pageStackBoard, pageStackBoardState.layout)
      }

      // ---- insert last ----

      pageStackBoardState.pageStackBoard = PageStackBoard(
         pageStackBoardState.pageStackBoard.rootRow.inserted(3, createPageStack(3))
      )

      rule.runOnIdle {
         assertPageStacks(listOf(2, 0, 1, 3), pageStackBoardState.pageStackBoard)

         assertPageStackLayoutStates(
            pageStackBoardState.pageStackBoard, pageStackBoardState.layout)
      }

      // ---- insert middle ----

      pageStackBoardState.pageStackBoard = PageStackBoard(
         pageStackBoardState.pageStackBoard.rootRow.inserted(2, createPageStack(4))
      )

      rule.runOnIdle {
         assertPageStacks(listOf(2, 0, 4, 1, 3), pageStackBoardState.pageStackBoard)

         assertPageStackLayoutStates(
            pageStackBoardState.pageStackBoard, pageStackBoardState.layout)
      }

      // ---- replace ----

      pageStackBoardState.pageStackBoard = PageStackBoard(
         pageStackBoardState.pageStackBoard.rootRow.replaced(2, createPageStack(5))
      )

      rule.runOnIdle {
         assertPageStacks(listOf(2, 0, 5, 1, 3), pageStackBoardState.pageStackBoard)

         assertPageStackLayoutStates(
            pageStackBoardState.pageStackBoard, pageStackBoardState.layout)
      }

      // ---- remove first ----

      pageStackBoardState.pageStackBoard = PageStackBoard(
         pageStackBoardState.pageStackBoard.rootRow.removed(0)
      )

      rule.runOnIdle {
         assertPageStacks(listOf(0, 5, 1, 3), pageStackBoardState.pageStackBoard)

         assertPageStackLayoutStates(
            pageStackBoardState.pageStackBoard, pageStackBoardState.layout)
      }

      // ---- remove last ----

      pageStackBoardState.pageStackBoard = PageStackBoard(
         pageStackBoardState.pageStackBoard.rootRow.removed(3)
      )

      rule.runOnIdle {
         assertPageStacks(listOf(0, 5, 1), pageStackBoardState.pageStackBoard)

         assertPageStackLayoutStates(
            pageStackBoardState.pageStackBoard, pageStackBoardState.layout)
      }

      // ---- remove middle ----

      pageStackBoardState.pageStackBoard = PageStackBoard(
         pageStackBoardState.pageStackBoard.rootRow.removed(1)
      )

      rule.runOnIdle {
         assertPageStacks(listOf(0, 1), pageStackBoardState.pageStackBoard)

         assertPageStackLayoutStates(
            pageStackBoardState.pageStackBoard, pageStackBoardState.layout)
      }
   }

   @Test
   fun multiColumnPageStackBoard_scroll() {
      val boardWidth = 100.dp

      val pageStackBoardState
            = createMultiColumnPageStackBoardState(pageStackCount = 3)

      rule.setContent {
         MultiColumnPageStackBoard(pageStackBoardState, boardWidth)
      }

      rule.onNodeWithText("0")
         .assertLeftPositionInRootIsEqualTo(16.dp)
      rule.onNodeWithText("1")
         .assertLeftPositionInRootIsEqualTo(boardWidth / 2 + 8.dp)
      rule.runOnIdle {
         assertEquals(0f, pageStackBoardState.scrollState.scrollOffset)
      }

      val scrollAmount = 32.dp

      rule.onNodeWithTag(pageStackBoardTag)
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

      val pageStackBoardState
            = createMultiColumnPageStackBoardState(pageStackCount = 3)

      rule.setContent {
         MultiColumnPageStackBoard(pageStackBoardState, boardWidth)
      }

      rule.onNodeWithText("0").assertLeftPositionInRootIsEqualTo(16.dp)
      rule.runOnIdle {
         assertEquals(0f, pageStackBoardState.scrollState.scrollOffset)
      }

      rule.onNodeWithTag(pageStackBoardTag)
         .performTouchInput {
            down(Offset(0.0f, 0.0f))
            moveBy(Offset(viewConfiguration.touchSlop, 0.0f))
            moveBy(Offset(boardWidth.toPx(), 0.0f))
         }

      rule.onNodeWithText("0").assertLeftPositionInRootIsEqualTo(16.dp)
      rule.runOnIdle {
         assertEquals(0f, pageStackBoardState.scrollState.scrollOffset)
      }

      rule.onNodeWithTag(pageStackBoardTag)
         .performTouchInput {
            moveBy(Offset(-viewConfiguration.touchSlop, 0.0f))
            moveBy(Offset(-boardWidth.toPx(), 0.0f))
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

      val pageStackBoardState
            = createMultiColumnPageStackBoardState(pageStackCount = 3)

      rule.setContent {
         MultiColumnPageStackBoard(pageStackBoardState, boardWidth)
      }

      rule.onNodeWithText("0").assertLeftPositionInRootIsEqualTo(16.dp)

      rule.onNodeWithTag(pageStackBoardTag)
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

      val pageStackBoardState
            = createMultiColumnPageStackBoardState(pageStackCount = 4)

      lateinit var coroutineScope: CoroutineScope
      rule.setContent {
         coroutineScope = rememberCoroutineScope()
         MultiColumnPageStackBoard(pageStackBoardState, boardWidth)
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

   @Test
   fun multiColumnPageStackBoard_firstVisibleIndex() {
      val boardWidth = 100.dp

      val pageStackBoardState
            = createMultiColumnPageStackBoardState(pageStackCount = 4)

      rule.setContent {
         MultiColumnPageStackBoard(pageStackBoardState, boardWidth)
      }

      rule.runOnIdle {
         assertEquals(0, pageStackBoardState.firstVisiblePageStackIndex)
      }

      val pageStackWidth = (boardWidth - 16.dp) / 2

      rule.onNodeWithTag(pageStackBoardTag)
         .performTouchInput {
            down(Offset(0.0f, 0.0f))
            moveBy(Offset(-viewConfiguration.touchSlop, 0.0f))
            moveBy(Offset(-pageStackWidth.toPx() + 1.0f, 0.0f))
         }

      rule.onNodeWithText("0")
         .fetchSemanticsNode()
         .boundsInRoot
         .let { assertEquals(1.0f, it.left + it.width, absoluteTolerance = 0.05f) }

      rule.runOnIdle {
         assertEquals(0, pageStackBoardState.firstVisiblePageStackIndex)
      }

      rule.onNodeWithTag(pageStackBoardTag)
         .performTouchInput {
            moveBy(Offset(-2.0f, 0.0f))
         }

      rule.onNodeWithText("0")
         .fetchSemanticsNode()
         .boundsInRoot
         .let { assertEquals(-1.0f, it.left + it.width, absoluteTolerance = 0.05f) }

      rule.runOnIdle {
         assertEquals(1, pageStackBoardState.firstVisiblePageStackIndex)
      }
   }
}
