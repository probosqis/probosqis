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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertLeftPositionInRootIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wcaokaze.probosqis.cache.core.WritableCache
import com.wcaokaze.probosqis.page.PageStack
import com.wcaokaze.probosqis.page.Page
import com.wcaokaze.probosqis.page.PageComposableSwitcher
import com.wcaokaze.probosqis.page.pageComposable
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.BeforeTest
import kotlin.test.assertContains
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class PageStackBoardComposeTest {
   @get:Rule
   val rule = createComposeRule()

   private val pageStackBoardTag = "PageStackBoard"
   private val defaultPageStackBoardWidth = 200.dp
   private val defaultPageStackCount = 2

   private class TestPage(val i: Int) : Page()

   private val testPageComposable = pageComposable<TestPage>(
      content = { page, pageStackState ->
         Column {
            val coroutineScope = rememberCoroutineScope()

            Text(
               "${page.i}",
               modifier = Modifier.fillMaxWidth()
            )

            Button(
               onClick = {
                  coroutineScope.launch {
                     val newPageStack = PageStack(
                        PageStack.Id(pageStackState.pageStack.id.value + 100L),
                        TestPage(page.i + 100)
                     )
                     pageStackState.addColumn(newPageStack)
                  }
               }
            ) {
               Text("Add PageStack ${page.i}")
            }
         }
      },
      header = { _, _ -> },
      footer = null
   )

   private val pageComposableSwitcher = PageComposableSwitcher(
      allPageComposables = listOf(
         testPageComposable,
      )
   )

   private lateinit var pageStackRepository: PageStackRepository

   @BeforeTest
   fun beforeTest() {
      pageStackRepository = mockk {
         every { savePageStack(any()) } answers { WritableCache(firstArg()) }
      }
   }

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
      width: Dp = defaultPageStackBoardWidth,
      pageStackCount: Int = defaultPageStackCount
   ) {
      MultiColumnPageStackBoard(
         state,
         pageComposableSwitcher,
         pageStackCount,
         WindowInsets(0, 0, 0, 0),
         modifier = Modifier
            .width(width)
            .testTag(pageStackBoardTag)
      )
   }

   @Stable
   private class RememberedPageStackBoardState<S : PageStackBoardState>(
      val pageStackBoardState: S,
      val coroutineScope: CoroutineScope
   ) {
      operator fun component1() = pageStackBoardState
      operator fun component2() = coroutineScope
   }

   @Composable
   private fun rememberSingleColumnPageStackBoardState(
      pageStackCount: Int
   ): RememberedPageStackBoardState<SingleColumnPageStackBoardState> {
      val animCoroutineScope = rememberCoroutineScope()
      return remember(animCoroutineScope) {
         val pageStackBoardState = createPageStackBoardState(
            ::SingleColumnPageStackBoardState, animCoroutineScope, pageStackCount)
         RememberedPageStackBoardState(pageStackBoardState, animCoroutineScope)
      }
   }

   @Composable
   private fun rememberMultiColumnPageStackBoardState(
      pageStackCount: Int
   ): RememberedPageStackBoardState<MultiColumnPageStackBoardState> {
      val animCoroutineScope = rememberCoroutineScope()
      return remember(animCoroutineScope) {
         val pageStackBoardState = createPageStackBoardState(
            ::MultiColumnPageStackBoardState, animCoroutineScope, pageStackCount)
         RememberedPageStackBoardState(pageStackBoardState, animCoroutineScope)
      }
   }

   private fun <S : PageStackBoardState> createPageStackBoardState(
      constructor: (
         WritableCache<PageStackBoard>, PageStackRepository, CoroutineScope
      ) -> S,
      animCoroutineScope: CoroutineScope,
      pageStackCount: Int
   ): S {
      val rootRow = PageStackBoard.Row(
         List(pageStackCount) { createPageStack(it) }.toImmutableList()
      )

      val pageStackBoard = PageStackBoard(rootRow)
      val pageStackBoardCache = WritableCache(pageStackBoard)
      return constructor(pageStackBoardCache, pageStackRepository, animCoroutineScope)
   }

   private fun createPageStack(i: Int): PageStackBoard.PageStack {
      val page = TestPage(i)
      val pageStack = PageStack(
         PageStack.Id(i.toLong()),
         page
      )
      val cache = pageStackRepository.savePageStack(pageStack)
      return PageStackBoard.PageStack(cache)
   }

   private fun expectedPageStackWidth(
      pageStackBoardWidth: Dp = defaultPageStackBoardWidth,
      pageStackCount: Int = defaultPageStackCount
   ): Dp {
      return (pageStackBoardWidth - 16.dp) / pageStackCount - 16.dp
   }

   private fun expectedPageStackLeftPosition(
      indexInBoard: Int,
      pageStackBoardWidth: Dp = defaultPageStackBoardWidth,
      pageStackCount: Int = defaultPageStackCount
   ): Dp {
      val pageStackWidth = expectedPageStackWidth(
         pageStackBoardWidth, pageStackCount)

      return 16.dp + (pageStackWidth + 16.dp) * indexInBoard
   }

   private fun expectedScrollOffset(
      index: Int,
      pageStackBoardWidth: Dp = defaultPageStackBoardWidth,
      pageStackCount: Int = defaultPageStackCount
   ): Float {
      return with (rule.density) {
         (pageStackBoardWidth - 16.dp).toPx() / pageStackCount * index
      }
   }

   @Test
   fun singleColumnPageStackBoard_layout() {
      rule.setContent {
         val (pageStackBoardState, _)
               = rememberSingleColumnPageStackBoardState(pageStackCount = 2)
         SingleColumnPageStackBoard(pageStackBoardState, width = 100.dp)
      }

      rule.onNodeWithText("0")
         .assertLeftPositionInRootIsEqualTo(0.dp)
         .assertWidthIsEqualTo(100.dp)
   }

   @Test
   fun multiColumnPageStackBoard_layout() {
      rule.setContent {
         val (pageStackBoardState, _)
               = rememberMultiColumnPageStackBoardState(pageStackCount = 2)
         MultiColumnPageStackBoard(pageStackBoardState)
      }

      rule.onNodeWithText("0")
         .assertLeftPositionInRootIsEqualTo(expectedPageStackLeftPosition(0))
         .assertWidthIsEqualTo(expectedPageStackWidth())
      rule.onNodeWithText("1")
         .assertLeftPositionInRootIsEqualTo(expectedPageStackLeftPosition(1))
         .assertWidthIsEqualTo(expectedPageStackWidth())
   }

   @Test
   fun multiColumnPageStackBoard_layout_notEnoughPageStacks() {
      rule.setContent {
         val (pageStackBoardState, _)
               = rememberMultiColumnPageStackBoardState(pageStackCount = 1)
         MultiColumnPageStackBoard(pageStackBoardState)
      }

      rule.onNodeWithText("0")
         .assertLeftPositionInRootIsEqualTo(expectedPageStackLeftPosition(0))
         .assertWidthIsEqualTo(expectedPageStackWidth())
   }

   @Test
   fun multiColumnPageStackBoard_layout_omitComposingInvisibles() {
      val boardWidth = 100.dp

      lateinit var pageStackBoardState: MultiColumnPageStackBoardState
      lateinit var coroutineScope: CoroutineScope
      rule.setContent {
         val remembered = rememberMultiColumnPageStackBoardState(pageStackCount = 5)
         SideEffect {
            pageStackBoardState = remembered.pageStackBoardState
            coroutineScope = remembered.coroutineScope
         }
         MultiColumnPageStackBoard(remembered.pageStackBoardState, boardWidth)
      }

      rule.onNodeWithText("0").assertExists()
      rule.onNodeWithText("1").assertExists()
      rule.onNodeWithText("2").assertExists() // 2はギリギリ存在する（影が見えるため）
      rule.onNodeWithText("3").assertDoesNotExist()
      rule.onNodeWithText("4").assertDoesNotExist()

      coroutineScope.launch {
         pageStackBoardState.animateScroll(1, PositionInBoard.FirstVisible)
      }

      rule.onNodeWithText("0").assertExists()
      rule.onNodeWithText("1").assertExists()
      rule.onNodeWithText("2").assertExists()
      rule.onNodeWithText("3").assertExists()
      rule.onNodeWithText("4").assertDoesNotExist()

      coroutineScope.launch {
         pageStackBoardState.animateScroll(2, PositionInBoard.FirstVisible)
      }

      rule.onNodeWithText("0").assertDoesNotExist()
      rule.onNodeWithText("1").assertExists() // 1はギリギリ存在する
      rule.onNodeWithText("2").assertExists()
      rule.onNodeWithText("3").assertExists()
      rule.onNodeWithText("4").assertExists()

      coroutineScope.launch {
         pageStackBoardState.animateScroll(3, PositionInBoard.FirstVisible)
      }

      rule.onNodeWithText("0").assertDoesNotExist()
      rule.onNodeWithText("1").assertDoesNotExist()
      rule.onNodeWithText("2").assertExists()
      rule.onNodeWithText("3").assertExists()
      rule.onNodeWithText("4").assertExists()
   }

   @Test
   fun multiColumnPageStackBoard_layout_mutatePageStackBoard() {
      fun assertPageNumbers(
         expectedPageNumbers: List<Int>,
         pageStackBoard: PageStackBoard
      ) {
         val pageStackCount = pageStackBoard.pageStackCount
         assertEquals(expectedPageNumbers.size, pageStackCount)

         for (i in 0 until pageStackCount) {
            val page = pageStackBoard[i].cache.value.head
            assertIs<TestPage>(page)
            assertEquals(expectedPageNumbers[i], page.i)
         }
      }

      fun assertPageStackLayoutStatesExist(
         pageStackBoard: PageStackBoard,
         layoutLogic: LayoutLogic
      ) {
         val pageStackCount = pageStackBoard.pageStackCount
         val ids = (0 until pageStackCount)
            .map { pageStackBoard[it].cache.value.id }

         assertEquals(ids, layoutLogic.layoutStateList.map { it.pageStackId })

         assertEquals(pageStackCount, layoutLogic.layoutStateMap.size)
         for (id in ids) {
            assertContains(layoutLogic.layoutStateMap, id)
         }
      }

      lateinit var pageStackBoardState: MultiColumnPageStackBoardState
      rule.setContent {
         val remembered = rememberMultiColumnPageStackBoardState(pageStackCount = 2)
         SideEffect {
            pageStackBoardState = remembered.pageStackBoardState
         }
         MultiColumnPageStackBoard(remembered.pageStackBoardState)
      }

      rule.runOnIdle {
         assertPageNumbers(listOf(0, 1), pageStackBoardState.pageStackBoard)

         assertPageStackLayoutStatesExist(
            pageStackBoardState.pageStackBoard, pageStackBoardState.layout)
      }

      // ---- insert first ----

      pageStackBoardState.pageStackBoard = PageStackBoard(
         pageStackBoardState.pageStackBoard.rootRow.inserted(0, createPageStack(2))
      )

      assertFalse(
         pageStackBoardState.layout.pageStackLayout(0).isInitialized)

      rule.runOnIdle {
         assertPageNumbers(listOf(2, 0, 1), pageStackBoardState.pageStackBoard)

         assertPageStackLayoutStatesExist(
            pageStackBoardState.pageStackBoard, pageStackBoardState.layout)

         assertTrue(
            pageStackBoardState.layout.pageStackLayout(0).isInitialized)
      }

      // ---- insert last ----

      pageStackBoardState.pageStackBoard = PageStackBoard(
         pageStackBoardState.pageStackBoard.rootRow.inserted(3, createPageStack(3))
      )

      assertFalse(
         pageStackBoardState.layout.pageStackLayout(3).isInitialized)

      rule.runOnIdle {
         assertPageNumbers(listOf(2, 0, 1, 3), pageStackBoardState.pageStackBoard)

         assertPageStackLayoutStatesExist(
            pageStackBoardState.pageStackBoard, pageStackBoardState.layout)

         assertTrue(
            pageStackBoardState.layout.pageStackLayout(3).isInitialized)
      }

      // ---- insert middle ----

      pageStackBoardState.pageStackBoard = PageStackBoard(
         pageStackBoardState.pageStackBoard.rootRow.inserted(2, createPageStack(4))
      )

      assertFalse(
         pageStackBoardState.layout.pageStackLayout(2).isInitialized)

      rule.runOnIdle {
         assertPageNumbers(listOf(2, 0, 4, 1, 3), pageStackBoardState.pageStackBoard)

         assertPageStackLayoutStatesExist(
            pageStackBoardState.pageStackBoard, pageStackBoardState.layout)

         assertTrue(
            pageStackBoardState.layout.pageStackLayout(2).isInitialized)
      }

      // ---- replace ----

      pageStackBoardState.pageStackBoard = PageStackBoard(
         pageStackBoardState.pageStackBoard.rootRow.replaced(2, createPageStack(5))
      )

      assertFalse(
         pageStackBoardState.layout.pageStackLayout(2).isInitialized)

      rule.runOnIdle {
         assertPageNumbers(listOf(2, 0, 5, 1, 3), pageStackBoardState.pageStackBoard)

         assertPageStackLayoutStatesExist(
            pageStackBoardState.pageStackBoard, pageStackBoardState.layout)

         assertTrue(
            pageStackBoardState.layout.pageStackLayout(2).isInitialized)
      }

      // ---- remove first ----

      pageStackBoardState.pageStackBoard = PageStackBoard(
         pageStackBoardState.pageStackBoard.rootRow.removed(0)
      )

      rule.runOnIdle {
         assertPageNumbers(listOf(0, 5, 1, 3), pageStackBoardState.pageStackBoard)

         assertPageStackLayoutStatesExist(
            pageStackBoardState.pageStackBoard, pageStackBoardState.layout)
      }

      // ---- remove last ----

      pageStackBoardState.pageStackBoard = PageStackBoard(
         pageStackBoardState.pageStackBoard.rootRow.removed(3)
      )

      rule.runOnIdle {
         assertPageNumbers(listOf(0, 5, 1), pageStackBoardState.pageStackBoard)

         assertPageStackLayoutStatesExist(
            pageStackBoardState.pageStackBoard, pageStackBoardState.layout)
      }

      // ---- remove middle ----

      pageStackBoardState.pageStackBoard = PageStackBoard(
         pageStackBoardState.pageStackBoard.rootRow.removed(1)
      )

      rule.runOnIdle {
         assertPageNumbers(listOf(0, 1), pageStackBoardState.pageStackBoard)

         assertPageStackLayoutStatesExist(
            pageStackBoardState.pageStackBoard, pageStackBoardState.layout)
      }
   }

   @Test
   fun multiColumnPageStackBoard_scroll() {
      lateinit var pageStackBoardState: MultiColumnPageStackBoardState
      rule.setContent {
         val remembered = rememberMultiColumnPageStackBoardState(pageStackCount = 3)
         SideEffect {
            pageStackBoardState = remembered.pageStackBoardState
         }
         MultiColumnPageStackBoard(remembered.pageStackBoardState)
      }

      rule.onNodeWithText("0")
         .assertLeftPositionInRootIsEqualTo(expectedPageStackLeftPosition(0))
      rule.onNodeWithText("1")
         .assertLeftPositionInRootIsEqualTo(expectedPageStackLeftPosition(1))
      rule.runOnIdle {
         assertEquals(0.0f, pageStackBoardState.scrollState.scrollOffset)
      }

      val scrollAmount = 25.dp

      rule.onNodeWithTag(pageStackBoardTag).performTouchInput {
         down(Offset(0.0f, 0.0f))
         moveBy(Offset(-viewConfiguration.touchSlop, 0.0f))
         moveBy(Offset(-scrollAmount.toPx(), 0.0f))
         // upするとsnapする
         // up()
      }

      rule.onNodeWithText("0").assertLeftPositionInRootIsEqualTo(
         expectedPageStackLeftPosition(0) - scrollAmount)
      rule.onNodeWithText("1").assertLeftPositionInRootIsEqualTo(
         expectedPageStackLeftPosition(1) - scrollAmount)
      rule.runOnIdle {
         assertEquals(
            with (rule.density) { scrollAmount.toPx() },
            pageStackBoardState.scrollState.scrollOffset
         )
      }
   }

   @Test
   fun multiColumnPageStackBoard_scrollEdge() {
      lateinit var pageStackBoardState: MultiColumnPageStackBoardState
      rule.setContent {
         val remembered = rememberMultiColumnPageStackBoardState(pageStackCount = 3)
         SideEffect {
            pageStackBoardState = remembered.pageStackBoardState
         }
         MultiColumnPageStackBoard(remembered.pageStackBoardState)
      }

      rule.onNodeWithText("0")
         .assertLeftPositionInRootIsEqualTo(expectedPageStackLeftPosition(0))
      rule.runOnIdle {
         assertEquals(
            expectedScrollOffset(0),
            pageStackBoardState.scrollState.scrollOffset
         )
      }

      rule.onNodeWithTag(pageStackBoardTag).performTouchInput {
         down(Offset(0.0f, 0.0f))
         moveBy(Offset(viewConfiguration.touchSlop, 0.0f))
         moveBy(Offset(100.dp.toPx(), 0.0f))
      }
      rule.onNodeWithText("0")
         .assertLeftPositionInRootIsEqualTo(expectedPageStackLeftPosition(0))
      rule.runOnIdle {
         assertEquals(
            expectedScrollOffset(0),
            pageStackBoardState.scrollState.scrollOffset
         )
      }

      rule.onNodeWithTag(pageStackBoardTag).performTouchInput {
         moveBy(Offset(-viewConfiguration.touchSlop, 0.0f))
         moveBy(Offset(-defaultPageStackBoardWidth.toPx(), 0.0f))
      }
      rule.onNodeWithText("1")
         .assertLeftPositionInRootIsEqualTo(expectedPageStackLeftPosition(0))
      rule.runOnIdle {
         assertEquals(
            expectedScrollOffset(1),
            pageStackBoardState.scrollState.scrollOffset
         )
      }

      rule.onNodeWithTag(pageStackBoardTag).performTouchInput {
         moveBy(Offset(-defaultPageStackBoardWidth.toPx(), 0.0f))
      }
      rule.onNodeWithText("1")
         .assertLeftPositionInRootIsEqualTo(expectedPageStackLeftPosition(0))
      rule.runOnIdle {
         assertEquals(
            expectedScrollOffset(1),
            pageStackBoardState.scrollState.scrollOffset
         )
      }
   }

   @Test
   fun multiColumnPageStackBoard_scrollEdge_afterSizeChanged() {
      var boardWidth by mutableStateOf(200.dp)

      lateinit var pageStackBoardState: MultiColumnPageStackBoardState
      rule.setContent {
         val remembered = rememberMultiColumnPageStackBoardState(pageStackCount = 3)
         SideEffect {
            pageStackBoardState = remembered.pageStackBoardState
         }
         MultiColumnPageStackBoard(remembered.pageStackBoardState, boardWidth)
      }

      rule.onNodeWithText("0").assertLeftPositionInRootIsEqualTo(
         expectedPageStackLeftPosition(0, boardWidth))

      rule.onNodeWithTag(pageStackBoardTag).performTouchInput {
         down(Offset(0.0f, 0.0f))
         moveBy(Offset(-viewConfiguration.touchSlop, 0.0f))
         moveBy(Offset(-boardWidth.toPx(), 0.0f))
      }
      rule.onNodeWithText("1").assertLeftPositionInRootIsEqualTo(
         expectedPageStackLeftPosition(0, boardWidth))
      rule.runOnIdle {
         assertEquals(
            expectedScrollOffset(1, boardWidth),
            pageStackBoardState.scrollState.scrollOffset
         )
      }

      boardWidth = 190.dp
      rule.onNodeWithText("1").assertLeftPositionInRootIsEqualTo(
         expectedPageStackLeftPosition(0, boardWidth))
      rule.runOnIdle {
         assertEquals(
            expectedScrollOffset(1, boardWidth),
            pageStackBoardState.scrollState.scrollOffset
         )
      }

      boardWidth = 200.dp
      rule.runOnIdle {
         assertEquals(
            expectedScrollOffset(1, pageStackBoardWidth = 190.dp),
            pageStackBoardState.scrollState.scrollOffset
         )
      }
   }

   private fun SemanticsNodeInteraction.swipeLeft(
      offset: Dp,
      duration: Long = 200L,
      interpolator: (Float) -> Float = { it }
   ) {
      performTouchInput {
         val start = Offset(viewConfiguration.touchSlop + offset.toPx(), 0.0f)
         val end = Offset.Zero
         swipe(
            curve = {
               val fraction = interpolator(it / duration.toFloat())
               lerp(start, end, fraction)
            },
            duration
         )
      }
   }

   private fun SemanticsNodeInteraction.swipeRight(
      offset: Dp,
      duration: Long = 200L,
      interpolator: (Float) -> Float = { it }
   ) {
      performTouchInput {
         val start = Offset.Zero
         val end = Offset(viewConfiguration.touchSlop + offset.toPx(), 0.0f)
         swipe(
            curve = {
               val fraction = interpolator(it / duration.toFloat())
               lerp(start, end, fraction)
            },
            duration
         )
      }
   }

   private val decayInterpolator = fun (f: Float): Float {
      return 1.0f - (f - 1.0f) * (f - 1.0f)
   }

   @Test
   fun multiColumnPageStackBoard_scroll_snap() {
      lateinit var pageStackBoardState: MultiColumnPageStackBoardState
      rule.setContent {
         val remembered = rememberMultiColumnPageStackBoardState(pageStackCount = 4)
         SideEffect {
            pageStackBoardState = remembered.pageStackBoardState
         }
         MultiColumnPageStackBoard(remembered.pageStackBoardState)
      }

      rule.runOnIdle {
         assertEquals(
            expectedScrollOffset(0),
            pageStackBoardState.scrollState.scrollOffset
         )
      }

      rule.onNodeWithTag(pageStackBoardTag).swipeLeft(20.dp)
      rule.runOnIdle {
         assertEquals(
            expectedScrollOffset(1),
            pageStackBoardState.scrollState.scrollOffset
         )
      }

      rule.onNodeWithTag(pageStackBoardTag).swipeRight(20.dp)
      rule.runOnIdle {
         assertEquals(
            expectedScrollOffset(0),
            pageStackBoardState.scrollState.scrollOffset
         )
      }
   }

   @Test
   fun multiColumnPageStackBoard_scroll_snap_tooFast() {
      lateinit var pageStackBoardState: MultiColumnPageStackBoardState
      rule.setContent {
         val remembered = rememberMultiColumnPageStackBoardState(pageStackCount = 4)
         SideEffect {
            pageStackBoardState = remembered.pageStackBoardState
         }
         MultiColumnPageStackBoard(remembered.pageStackBoardState)
      }

      rule.runOnIdle {
         assertEquals(
            expectedScrollOffset(0),
            pageStackBoardState.scrollState.scrollOffset
         )
      }

      rule.onNodeWithTag(pageStackBoardTag).swipeLeft(32.dp, duration = 100L)
      rule.runOnIdle {
         assertEquals(
            expectedScrollOffset(1),
            pageStackBoardState.scrollState.scrollOffset
         )
      }

      rule.onNodeWithTag(pageStackBoardTag).swipeLeft(32.dp, duration = 100L)
      rule.runOnIdle {
         assertEquals(
            expectedScrollOffset(2),
            pageStackBoardState.scrollState.scrollOffset
         )
      }

      rule.onNodeWithTag(pageStackBoardTag).swipeRight(32.dp, duration = 100L)
      rule.runOnIdle {
         assertEquals(
            expectedScrollOffset(1),
            pageStackBoardState.scrollState.scrollOffset
         )
      }
   }

   @Test
   fun multiColumnPageStackBoard_scroll_snap_edges() {
      lateinit var pageStackBoardState: MultiColumnPageStackBoardState
      rule.setContent {
         val remembered = rememberMultiColumnPageStackBoardState(pageStackCount = 4)
         SideEffect {
            pageStackBoardState = remembered.pageStackBoardState
         }
         MultiColumnPageStackBoard(remembered.pageStackBoardState)
      }

      rule.runOnIdle {
         assertEquals(
            expectedScrollOffset(0),
            pageStackBoardState.scrollState.scrollOffset
         )
      }

      rule.onNodeWithTag(pageStackBoardTag).swipeRight(20.dp)
      rule.runOnIdle {
         assertEquals(
            expectedScrollOffset(0),
            pageStackBoardState.scrollState.scrollOffset
         )
      }

      repeat (2) {
         rule.onNodeWithTag(pageStackBoardTag).swipeLeft(20.dp)
      }
      rule.runOnIdle {
         assertEquals(
            expectedScrollOffset(2),
            pageStackBoardState.scrollState.scrollOffset
         )
      }

      rule.onNodeWithTag(pageStackBoardTag).swipeLeft(20.dp)
      rule.runOnIdle {
         assertEquals(
            expectedScrollOffset(2),
            pageStackBoardState.scrollState.scrollOffset
         )
      }
   }

   @Test
   fun multiColumnPageStackBoard_scroll_snap_afterImmobility() {
      lateinit var pageStackBoardState: MultiColumnPageStackBoardState
      rule.setContent {
         val remembered = rememberMultiColumnPageStackBoardState(pageStackCount = 4)
         SideEffect {
            pageStackBoardState = remembered.pageStackBoardState
         }
         MultiColumnPageStackBoard(remembered.pageStackBoardState)
      }

      rule.onNodeWithTag(pageStackBoardTag).swipeLeft(20.dp)
      rule.runOnIdle {
         assertEquals(
            expectedScrollOffset(1),
            pageStackBoardState.scrollState.scrollOffset
         )
      }

      val halfWidth = (expectedPageStackWidth() + 16.dp) / 2

      rule.onNodeWithTag(pageStackBoardTag)
         .swipeLeft(halfWidth - 2.dp, duration = 1000L, decayInterpolator)
      rule.runOnIdle {
         assertEquals(
            expectedScrollOffset(1),
            pageStackBoardState.scrollState.scrollOffset
         )
      }

      rule.onNodeWithTag(pageStackBoardTag)
         .swipeRight(halfWidth - 2.dp, duration = 1000L, decayInterpolator)
      rule.runOnIdle {
         assertEquals(
            expectedScrollOffset(1),
            pageStackBoardState.scrollState.scrollOffset
         )
      }

      rule.onNodeWithTag(pageStackBoardTag)
         .swipeLeft(halfWidth + 2.dp, duration = 1000L, decayInterpolator)
      rule.runOnIdle {
         assertEquals(
            expectedScrollOffset(2),
            pageStackBoardState.scrollState.scrollOffset
         )
      }

      rule.onNodeWithTag(pageStackBoardTag)
         .swipeRight(halfWidth + 2.dp, duration = 1000L, decayInterpolator)
      rule.runOnIdle {
         assertEquals(
            expectedScrollOffset(1),
            pageStackBoardState.scrollState.scrollOffset
         )
      }
   }

   @Test
   fun multiColumnPageStackBoard_scroll_swipe_overMultiplePageStacks() {
      lateinit var pageStackBoardState: MultiColumnPageStackBoardState
      rule.setContent {
         val remembered = rememberMultiColumnPageStackBoardState(pageStackCount = 4)
         SideEffect {
            pageStackBoardState = remembered.pageStackBoardState
         }
         MultiColumnPageStackBoard(remembered.pageStackBoardState)
      }

      rule.runOnIdle {
         assertEquals(0.0f, pageStackBoardState.scrollState.scrollOffset)
      }


      rule.onNodeWithTag(pageStackBoardTag)
         .swipeLeft(expectedPageStackWidth() + 20.dp, duration = 400L)
      rule.runOnIdle {
         assertEquals(
            expectedScrollOffset(2),
            pageStackBoardState.scrollState.scrollOffset
         )
      }

      rule.onNodeWithTag(pageStackBoardTag)
         .swipeRight(expectedPageStackWidth() + 20.dp, duration = 400L)
      rule.runOnIdle {
         assertEquals(
            expectedScrollOffset(0),
            pageStackBoardState.scrollState.scrollOffset
         )
      }
   }

   @Test
   fun animateScroll() {
      lateinit var pageStackBoardState: MultiColumnPageStackBoardState
      lateinit var coroutineScope: CoroutineScope
      var pageStackCount by mutableStateOf(2)
      rule.setContent {
         val remembered = rememberMultiColumnPageStackBoardState(pageStackCount = 4)
         SideEffect {
            pageStackBoardState = remembered.pageStackBoardState
            coroutineScope = remembered.coroutineScope
         }
         MultiColumnPageStackBoard(
            remembered.pageStackBoardState,
            pageStackCount = pageStackCount
         )
      }

      class ScrollParameterType
      val byIndex = ScrollParameterType()
      val byId = ScrollParameterType()

      suspend fun animateScroll(
         pageStack: Int,
         targetPositionInBoard: PositionInBoard,
         parameterType: ScrollParameterType
      ) {
         when (parameterType) {
            byIndex -> {
               pageStackBoardState
                  .animateScroll(pageStack, targetPositionInBoard)
            }
            byId -> {
               pageStackBoardState.animateScroll(
                  PageStack.Id(pageStack.toLong()),
                  targetPositionInBoard
               )
            }
         }
      }

      fun assertScrollOffset(leftmostPageStackIndex: Int) {
         rule.onNodeWithText("$leftmostPageStackIndex")
            .assertLeftPositionInRootIsEqualTo(
               expectedPageStackLeftPosition(0, pageStackCount = pageStackCount)
            )

         rule.runOnIdle {
            assertEquals(
               expectedScrollOffset(
                  leftmostPageStackIndex, pageStackCount = pageStackCount
               ),
               pageStackBoardState.scrollState.scrollOffset
            )
         }
      }

      for (parameterType in listOf(byIndex, byId)) {
         // -------- pageStackCount = 2 --------
         pageStackCount = 2
         coroutineScope.launch {
            animateScroll(0, PositionInBoard.FirstVisible, byIndex)
         }
         assertScrollOffset(0)

         // ==== FirstVisible ====

         //  0]1 2]3
         coroutineScope.launch {
            animateScroll(1, PositionInBoard.FirstVisible, parameterType)
         }
         assertScrollOffset(1)

         //  0 1[2 3]
         coroutineScope.launch {
            animateScroll(2, PositionInBoard.FirstVisible, parameterType)
         }
         assertScrollOffset(2)

         //  0 1[2 3]
         coroutineScope.launch {
            animateScroll(3, PositionInBoard.FirstVisible, parameterType)
         }
         assertScrollOffset(2)

         //  0 1[2 3]
         coroutineScope.launch {
            animateScroll(2, PositionInBoard.FirstVisible, parameterType)
         }
         assertScrollOffset(2)

         //  0[1 2]3
         coroutineScope.launch {
            animateScroll(1, PositionInBoard.FirstVisible, parameterType)
         }
         assertScrollOffset(1)

         // [0 1]2 3
         coroutineScope.launch {
            animateScroll(0, PositionInBoard.FirstVisible, parameterType)
         }
         assertScrollOffset(0)

         //  0 1[2 3]
         coroutineScope.launch {
            animateScroll(2, PositionInBoard.FirstVisible, parameterType)
         }
         assertScrollOffset(2)

         // [0 1]2 3
         coroutineScope.launch {
            animateScroll(0, PositionInBoard.FirstVisible, parameterType)
         }
         assertScrollOffset(0)

         coroutineScope.launch {
            assertFails {
               animateScroll(4, PositionInBoard.FirstVisible, parameterType)
            }
            assertFails {
               animateScroll(-1, PositionInBoard.FirstVisible, parameterType)
            }
         }

         // ==== LastVisible ====

         //  0[1 2]3
         coroutineScope.launch {
            animateScroll(2, PositionInBoard.LastVisible, parameterType)
         }
         assertScrollOffset(1)

         //  0 1[2 3]
         coroutineScope.launch {
            animateScroll(3, PositionInBoard.LastVisible, parameterType)
         }
         assertScrollOffset(2)

         //  0[1 2]3
         coroutineScope.launch {
            animateScroll(2, PositionInBoard.LastVisible, parameterType)
         }
         assertScrollOffset(1)

         // [0 1]2 3
         coroutineScope.launch {
            animateScroll(1, PositionInBoard.LastVisible, parameterType)
         }
         assertScrollOffset(0)

         // [0 1]2 3
         coroutineScope.launch {
            animateScroll(0, PositionInBoard.LastVisible, parameterType)
         }
         assertScrollOffset(0)

         //  0 1[2 3]
         coroutineScope.launch {
            animateScroll(3, PositionInBoard.LastVisible, parameterType)
         }
         assertScrollOffset(2)

         // [0 1]2 3
         coroutineScope.launch {
            animateScroll(1, PositionInBoard.LastVisible, parameterType)
         }
         assertScrollOffset(0)

         coroutineScope.launch {
            assertFails {
               pageStackBoardState.animateScroll(4, PositionInBoard.LastVisible)
            }
            assertFails {
               pageStackBoardState.animateScroll(-1, PositionInBoard.LastVisible)
            }
         }

         // ==== NearestVisible ====

         // [0 1]2 3
         coroutineScope.launch {
            animateScroll(1, PositionInBoard.NearestVisible, parameterType)
         }
         assertScrollOffset(0)

         //  0[1 2]3
         coroutineScope.launch {
            animateScroll(2, PositionInBoard.NearestVisible, parameterType)
         }
         assertScrollOffset(1)

         //  0 1[2 3]
         coroutineScope.launch {
            animateScroll(3, PositionInBoard.NearestVisible, parameterType)
         }
         assertScrollOffset(2)

         //  0 1[2 3]
         coroutineScope.launch {
            animateScroll(2, PositionInBoard.NearestVisible, parameterType)
         }
         assertScrollOffset(2)

         //  0[1 2]3
         coroutineScope.launch {
            animateScroll(1, PositionInBoard.NearestVisible, parameterType)
         }
         assertScrollOffset(1)

         // [0 1]2 3
         coroutineScope.launch {
            animateScroll(0, PositionInBoard.NearestVisible, parameterType)
         }
         assertScrollOffset(0)

         //  0 1[2 3]
         coroutineScope.launch {
            animateScroll(3, PositionInBoard.NearestVisible, parameterType)
         }
         assertScrollOffset(2)

         // [0 1]2 3
         coroutineScope.launch {
            animateScroll(0, PositionInBoard.NearestVisible, parameterType)
         }
         assertScrollOffset(0)

         coroutineScope.launch {
            assertFails {
               animateScroll(4, PositionInBoard.NearestVisible, parameterType)
            }
            assertFails {
               animateScroll(-1, PositionInBoard.NearestVisible, parameterType)
            }
         }

         // -------- pageStackCount = 1 --------
         pageStackCount = 1
         coroutineScope.launch {
            animateScroll(0, PositionInBoard.FirstVisible, byIndex)
         }
         assertScrollOffset(0)

         // ==== FirstVisible ====

         //  0[1]2 3
         coroutineScope.launch {
            animateScroll(1, PositionInBoard.FirstVisible, parameterType)
         }
         assertScrollOffset(1)

         //  0 1[2]3
         coroutineScope.launch {
            animateScroll(2, PositionInBoard.FirstVisible, parameterType)
         }
         assertScrollOffset(2)

         //  0 1 2[3]
         coroutineScope.launch {
            animateScroll(3, PositionInBoard.FirstVisible, parameterType)
         }
         assertScrollOffset(3)

         //  0 1[2]3
         coroutineScope.launch {
            animateScroll(2, PositionInBoard.FirstVisible, parameterType)
         }
         assertScrollOffset(2)

         //  0[1]2 3
         coroutineScope.launch {
            animateScroll(1, PositionInBoard.FirstVisible, parameterType)
         }
         assertScrollOffset(1)

         // [0]1 2 3
         coroutineScope.launch {
            animateScroll(0, PositionInBoard.FirstVisible, parameterType)
         }
         assertScrollOffset(0)

         //  0 1[2]3
         coroutineScope.launch {
            animateScroll(2, PositionInBoard.FirstVisible, parameterType)
         }
         assertScrollOffset(2)

         // [0]1 2 3
         coroutineScope.launch {
            animateScroll(0, PositionInBoard.FirstVisible, parameterType)
         }
         assertScrollOffset(0)

         coroutineScope.launch {
            assertFails {
               animateScroll(4, PositionInBoard.FirstVisible, parameterType)
            }
            assertFails {
               animateScroll(-1, PositionInBoard.FirstVisible, parameterType)
            }
         }

         // ==== LastVisible ====

         //  0[1]2 3
         coroutineScope.launch {
            animateScroll(1, PositionInBoard.LastVisible, parameterType)
         }
         assertScrollOffset(1)

         //  0 1[2]3
         coroutineScope.launch {
            animateScroll(2, PositionInBoard.LastVisible, parameterType)
         }
         assertScrollOffset(2)

         //  0 1 2[3]
         coroutineScope.launch {
            animateScroll(3, PositionInBoard.LastVisible, parameterType)
         }
         assertScrollOffset(3)

         //  0 1[2]3
         coroutineScope.launch {
            animateScroll(2, PositionInBoard.LastVisible, parameterType)
         }
         assertScrollOffset(2)

         //  0[1]2 3
         coroutineScope.launch {
            animateScroll(1, PositionInBoard.LastVisible, parameterType)
         }
         assertScrollOffset(1)

         // [0]1 2 3
         coroutineScope.launch {
            animateScroll(0, PositionInBoard.LastVisible, parameterType)
         }
         assertScrollOffset(0)

         //  0 1[2]3
         coroutineScope.launch {
            animateScroll(2, PositionInBoard.LastVisible, parameterType)
         }
         assertScrollOffset(2)

         // [0]1 2 3
         coroutineScope.launch {
            animateScroll(0, PositionInBoard.LastVisible, parameterType)
         }
         assertScrollOffset(0)

         coroutineScope.launch {
            assertFails {
               animateScroll(4, PositionInBoard.LastVisible, parameterType)
            }
            assertFails {
               animateScroll(-1, PositionInBoard.LastVisible, parameterType)
            }
         }

         // ==== NearestVisible ====

         //  0[1]2 3
         coroutineScope.launch {
            animateScroll(1, PositionInBoard.NearestVisible, parameterType)
         }
         assertScrollOffset(1)

         //  0 1[2]3
         coroutineScope.launch {
            animateScroll(2, PositionInBoard.NearestVisible, parameterType)
         }
         assertScrollOffset(2)

         //  0 1 2[3]
         coroutineScope.launch {
            animateScroll(3, PositionInBoard.NearestVisible, parameterType)
         }
         assertScrollOffset(3)

         //  0 1[2]3
         coroutineScope.launch {
            animateScroll(2, PositionInBoard.NearestVisible, parameterType)
         }
         assertScrollOffset(2)

         //  0[1]2 3
         coroutineScope.launch {
            animateScroll(1, PositionInBoard.NearestVisible, parameterType)
         }
         assertScrollOffset(1)

         // [0]1 2 3
         coroutineScope.launch {
            animateScroll(0, PositionInBoard.NearestVisible, parameterType)
         }
         assertScrollOffset(0)

         //  0 1[2]3
         coroutineScope.launch {
            animateScroll(2, PositionInBoard.NearestVisible, parameterType)
         }
         assertScrollOffset(2)

         // [0]1 2 3
         coroutineScope.launch {
            animateScroll(0, PositionInBoard.NearestVisible, parameterType)
         }
         assertScrollOffset(0)

         coroutineScope.launch {
            assertFails {
               animateScroll(4, PositionInBoard.NearestVisible, parameterType)
            }
            assertFails {
               animateScroll(-1, PositionInBoard.NearestVisible, parameterType)
            }
         }
      }
   }

   @Test
   fun multiColumnPageStackBoard_firstVisibleIndex() {
      lateinit var pageStackBoardState: MultiColumnPageStackBoardState
      rule.setContent {
         val remembered = rememberMultiColumnPageStackBoardState(pageStackCount = 4)
         SideEffect {
            pageStackBoardState = remembered.pageStackBoardState
         }
         MultiColumnPageStackBoard(remembered.pageStackBoardState)
      }

      rule.runOnIdle {
         assertEquals(0, pageStackBoardState.firstVisiblePageStackIndex)
      }

      val pageStackWidth = expectedScrollOffset(1)

      rule.onNodeWithTag(pageStackBoardTag).performTouchInput {
         down(Offset(0.0f, 0.0f))
         moveBy(Offset(-viewConfiguration.touchSlop, 0.0f))
         moveBy(Offset(-pageStackWidth + 1.0f, 0.0f))
      }
      rule.onNodeWithText("0")
         .fetchSemanticsNode()
         .boundsInRoot
         .let { assertEquals(1.0f, it.left + it.width, absoluteTolerance = 0.05f) }
      rule.runOnIdle {
         assertEquals(0, pageStackBoardState.firstVisiblePageStackIndex)
      }

      rule.onNodeWithTag(pageStackBoardTag).performTouchInput {
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

   // TODO: Row内のPageStackとかでもテストする
   @Test
   fun addPageStack_viaPageStackState() {
      lateinit var pageStackBoardState: MultiColumnPageStackBoardState
      lateinit var coroutineScope: CoroutineScope
      rule.setContent {
         val remembered = rememberMultiColumnPageStackBoardState(pageStackCount = 2)
         SideEffect {
            pageStackBoardState = remembered.pageStackBoardState
            coroutineScope = remembered.coroutineScope
         }
         MultiColumnPageStackBoard(remembered.pageStackBoardState)
      }

      fun assertPageNumbers(expected: List<Int>, actual: PageStackBoard) {
         val pages = actual.sequence()
            .map { assertIs<PageStackBoard.PageStack>(it) }
            .map { it.cache.value }
            .map { assertIs<TestPage>(it.head) }
            .toList()

         assertContentEquals(expected, pages.map { it.i })
      }

      rule.runOnIdle {
         assertPageNumbers(
            listOf(0, 1),
            pageStackBoardState.pageStackBoard
         )

         assertEquals(
            expectedScrollOffset(0),
            pageStackBoardState.scrollState.scrollOffset
         )
      }

      rule.onNodeWithText("Add PageStack 1").performClick()
      rule.runOnIdle {
         assertPageNumbers(
            listOf(0, 1, 101),
            pageStackBoardState.pageStackBoard
         )

         assertEquals(
            expectedScrollOffset(1),
            pageStackBoardState.scrollState.scrollOffset
         )
      }

      coroutineScope.launch {
         pageStackBoardState.animateScroll(PageStack.Id(0L))
      }

      rule.onNodeWithText("Add PageStack 0").performClick()
      rule.runOnIdle {
         assertPageNumbers(
            listOf(0, 100, 1, 101),
            pageStackBoardState.pageStackBoard
         )

         assertEquals(
            expectedScrollOffset(0),
            pageStackBoardState.scrollState.scrollOffset
         )
      }

      rule.onNodeWithText("Add PageStack 100").performClick()
      rule.runOnIdle {
         assertPageNumbers(
            listOf(0, 100, 200, 1, 101),
            pageStackBoardState.pageStackBoard
         )

         assertEquals(
            expectedScrollOffset(1),
            pageStackBoardState.scrollState.scrollOffset
         )
      }
   }
}
