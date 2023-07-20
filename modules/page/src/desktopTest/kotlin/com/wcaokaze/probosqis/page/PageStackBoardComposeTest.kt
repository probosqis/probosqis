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

package com.wcaokaze.probosqis.page

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.wcaokaze.probosqis.cache.core.WritableCache
import com.wcaokaze.probosqis.ext.kotlin.datetime.MockClock
import com.wcaokaze.probosqis.page.pagestackboard.PageStackBoard
import com.wcaokaze.probosqis.page.pagestackboard.SingleColumnPageStackBoard
import com.wcaokaze.probosqis.page.pagestackboard.SingleColumnPageStackBoardState
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.test.Ignore
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs

@RunWith(JUnit4::class)
class PageStackBoardComposeTest {
   @get:Rule
   val rule = createComposeRule()

   @Ignore
   @Test
   fun addPageStack_viaPageStackState() {
      class TestPage(val i: Int) : Page()

      val pageStackBoardCache = run {
         val children = listOf(
               PageStack(TestPage( 0), MockClock(minute = 0)),
               PageStack(TestPage(10), MockClock(minute = 1)),
            )
            .map { WritableCache(it) }
            .map { PageStackBoard.PageStack(it) }
            .toImmutableList()

         val rootRow = PageStackBoard.Row(children)
         val pageStackBoard = PageStackBoard(rootRow)
         WritableCache(pageStackBoard)
      }

      val pageStackBoardState = SingleColumnPageStackBoardState(pageStackBoardCache)

      val pageComposableSwitcher = PageComposableSwitcher(
         allPageComposables = listOf(
            pageComposable<TestPage>(
               content = { page, pageStackState ->
                  val coroutineScope = rememberCoroutineScope()

                  Button(
                     onClick = {
                        coroutineScope.launch {
                           val newPageStack = PageStack(
                              TestPage(page.i + 1),
                              MockClock(minute = 2)
                           )
                           pageStackState.addColumn(newPageStack)
                        }
                     }
                  ) {
                     Text("${page.i}")
                  }
               },
               header = { _, _ -> },
               footer = null
            ),
         )
      )

      lateinit var coroutineScope: CoroutineScope
      rule.setContent {
         coroutineScope = rememberCoroutineScope()

         SingleColumnPageStackBoard(
            pageStackBoardState,
            pageComposableSwitcher,
            WindowInsets(0, 0, 0, 0)
         )
      }

      fun assertPageNumbers(expected: List<Int>, actual: PageStackBoard) {
         val elements = List(actual.rootRow.childCount) { actual.rootRow[it] }

         val pages = elements
            .map { assertIs<PageStackBoard.PageStack>(it) }
            .map { it.cache.value }
            .map { assertIs<TestPage>(it.head) }

         assertContentEquals(expected, pages.map { it.i })
      }

      rule.runOnIdle {
         assertEquals(2, pageStackBoardCache.value.rootRow.childCount)
      }
      coroutineScope.launch {
         pageStackBoardState.animateScrollTo(1)
         rule.onNodeWithText("10").performClick()
      }
      rule.runOnIdle {
         assertEquals(3, pageStackBoardCache.value.rootRow.childCount)
         assertPageNumbers(listOf(0, 10, 11), pageStackBoardCache.value)
      }
      coroutineScope.launch {
         pageStackBoardState.animateScrollTo(0)
         rule.onNodeWithText("0").performClick()
      }
      rule.runOnIdle {
         assertEquals(4, pageStackBoardCache.value.rootRow.childCount)
         assertPageNumbers(listOf(0, 1, 10, 11), pageStackBoardCache.value)
      }
   }
}
