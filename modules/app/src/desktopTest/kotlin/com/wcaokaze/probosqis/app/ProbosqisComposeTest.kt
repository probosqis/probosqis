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

package com.wcaokaze.probosqis.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import com.wcaokaze.probosqis.cache.core.WritableCache
import com.wcaokaze.probosqis.page.MultiColumnPageStackBoardState
import com.wcaokaze.probosqis.page.PageStackBoard
import com.wcaokaze.probosqis.page.PageStackBoardRepository
import com.wcaokaze.probosqis.page.PageStackRepository
import com.wcaokaze.probosqis.page.SingleColumnPageStackBoardState
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.IOException
import kotlin.test.*

@RunWith(JUnit4::class)
class ProbosqisComposeTest {
   @get:Rule
   val rule = createComposeRule()

   @Test
   fun loadPageStackBoard() {
      val rootRow = PageStackBoard.Row(persistentListOf())
      val pageStackBoard = PageStackBoard(rootRow)

      val pageStackBoardRepository = mockk<PageStackBoardRepository> {
         every { loadPageStackBoard() } returns WritableCache(pageStackBoard)
      }

      val probosqisState = ProbosqisState(
         allPageComposables = emptyList(),
         pageStackBoardRepository,
         pageStackRepository = mockk(),
         coroutineScope = mockk()
      )

      val loadedCache = probosqisState.loadPageStackBoardOrDefault()

      assertSame(pageStackBoard, loadedCache.value)
   }

   @Test
   fun loadPageStackBoard_default() {
      val pageStackBoardRepository = mockk<PageStackBoardRepository> {
         every { loadPageStackBoard() } throws IOException()
         every { savePageStackBoard(any()) } answers { WritableCache(firstArg()) }
      }

      val pageStackRepository = mockk<PageStackRepository> {
         every { savePageStack(any()) } answers { WritableCache(firstArg()) }
         every { deleteAllPageStacks() } returns Unit
      }

      val probosqisState = ProbosqisState(
         allPageComposables = emptyList(),
         pageStackBoardRepository,
         pageStackRepository,
         coroutineScope = mockk()
      )

      val loadedCache = probosqisState.loadPageStackBoardOrDefault()

      verify { pageStackRepository.deleteAllPageStacks() }
      assertEquals(2, loadedCache.value.rootRow.childCount)

      val pageStack1
         = assertIs<PageStackBoard.PageStack>(loadedCache.value.rootRow[0])
         .pageStackCache.value
      assertIs<TestPage>(pageStack1.head.page)
      assertNull(pageStack1.tailOrNull())
      verify { pageStackRepository.savePageStack(pageStack1) }

      val pageStack2
         = assertIs<PageStackBoard.PageStack>(loadedCache.value.rootRow[1])
         .pageStackCache.value
      assertIs<TestPage>(pageStack2.head.page)
      assertNull(pageStack2.tailOrNull())
      verify { pageStackRepository.savePageStack(pageStack2) }
   }

   @Test
   fun getBoardState_singleColumn() {
      val rootRow = PageStackBoard.Row(persistentListOf())
      val pageStackBoard = PageStackBoard(rootRow)

      val probosqisState = ProbosqisState(
         allPageComposables = emptyList(),
         pageStackBoardRepository = mockk {
            every { loadPageStackBoard() } returns WritableCache(pageStackBoard)
         },
         pageStackRepository = mockk(),
         coroutineScope = mockk()
      )

      rule.setContent {
         SingleColumnProbosqis(probosqisState)
      }

      rule.runOnIdle {
         assertIs<SingleColumnPageStackBoardState>(
            probosqisState.pageStackBoardState
         )
      }
   }

   @Test
   fun getBoardState_multiColumn() {
      val rootRow = PageStackBoard.Row(persistentListOf())
      val pageStackBoard = PageStackBoard(rootRow)

      val probosqisState = ProbosqisState(
         allPageComposables = emptyList(),
         pageStackBoardRepository = mockk {
            every { loadPageStackBoard() } returns WritableCache(pageStackBoard)
         },
         pageStackRepository = mockk(),
         coroutineScope = mockk()
      )

      rule.setContent {
         MultiColumnProbosqis(probosqisState)
      }

      rule.runOnIdle {
         assertIs<MultiColumnPageStackBoardState>(
            probosqisState.pageStackBoardState
         )
      }
   }

   @Test
   fun getBoardState_switchingSingleColumnMultiColumn() {
      val rootRow = PageStackBoard.Row(persistentListOf())
      val pageStackBoard = PageStackBoard(rootRow)

      val probosqisState = ProbosqisState(
         allPageComposables = emptyList(),
         pageStackBoardRepository = mockk {
            every { loadPageStackBoard() } returns WritableCache(pageStackBoard)
         },
         pageStackRepository = mockk(),
         coroutineScope = mockk()
      )

      var isMultiColumn by mutableStateOf(false)

      rule.setContent {
         if (isMultiColumn) {
            MultiColumnProbosqis(probosqisState)
         } else {
            SingleColumnProbosqis(probosqisState)
         }
      }

      rule.runOnIdle {
         assertIs<SingleColumnPageStackBoardState>(
            probosqisState.pageStackBoardState
         )
      }

      isMultiColumn = true

      rule.runOnIdle {
         assertIs<MultiColumnPageStackBoardState>(
            probosqisState.pageStackBoardState
         )
      }

      isMultiColumn = false

      rule.runOnIdle {
         assertIs<SingleColumnPageStackBoardState>(
            probosqisState.pageStackBoardState
         )
      }
   }

   @Test
   fun getBoardState_beforeComposition() {
      val probosqisState = ProbosqisState(
         allPageComposables = emptyList(),
         pageStackBoardRepository = mockk(),
         pageStackRepository = mockk(),
         coroutineScope = mockk()
      )

      assertFails {
         probosqisState.pageStackBoardState
      }
   }
}
