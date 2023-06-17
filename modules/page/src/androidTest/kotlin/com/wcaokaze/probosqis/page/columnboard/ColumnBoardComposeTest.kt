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

package com.wcaokaze.probosqis.page.columnboard

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertLeftPositionInRootIsEqualTo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.wcaokaze.probosqis.cache.core.WritableCache
import com.wcaokaze.probosqis.ext.kotlin.datetime.MockClock
import com.wcaokaze.probosqis.page.Column
import com.wcaokaze.probosqis.page.ColumnBoard
import com.wcaokaze.probosqis.page.ColumnBoardState
import com.wcaokaze.probosqis.page.Page
import com.wcaokaze.probosqis.page.PageComposableSwitcher
import com.wcaokaze.probosqis.page.pageComposable
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ColumnBoardComposeTest {
   @get:Rule
   val rule = createComposeRule()

   private class TestPage(val i: Int) : Page()

   private val testPageComposable = pageComposable<TestPage>(
      content = { page, _ ->
         Text("${page.i}")
      },
      header = { _, _ -> },
      footer = null
   )

   private val pageComposableSwitcher = PageComposableSwitcher(
      allPageComposables = listOf(
         testPageComposable,
      )
   )

   private fun createColumnBoardState(vararg columns: Column): ColumnBoardState {
      val columnBoard = ColumnBoard(columns.toList())
      val columnBoardCache = WritableCache(columnBoard)
      return ColumnBoardState(columnBoardCache)
   }

   @Test
   fun singleColumnBoard_layout() {
      rule.setContent {
         val columnBoardState = remember {
            createColumnBoardState(
               Column(TestPage(0), MockClock(minute = 0)),
               Column(TestPage(1), MockClock(minute = 1)),
            )
         }

         SingleColumnBoard(
            columnBoardState,
            pageComposableSwitcher,
            WindowInsets(0, 0, 0, 0),
            modifier = Modifier
               .width(100.dp)
         )
      }

      rule.onNodeWithText("0")
         .assertLeftPositionInRootIsEqualTo(0.dp)
   }

   @Test
   fun multiColumnBoard_layout() {
      rule.setContent {
         val columnBoardState = remember {
            createColumnBoardState(
               Column(TestPage(0), MockClock(minute = 0)),
               Column(TestPage(1), MockClock(minute = 1)),
            )
         }

         MultiColumnBoard(
            columnBoardState,
            pageComposableSwitcher,
            columnCount = 2,
            WindowInsets(0, 0, 0, 0),
            modifier = Modifier
               .width(100.dp)
         )
      }

      rule.onNodeWithText("0")
         .assertLeftPositionInRootIsEqualTo(0.dp)
      rule.onNodeWithText("1")
         .assertLeftPositionInRootIsEqualTo(50.dp)
   }
}
