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

import androidx.compose.ui.test.junit4.createComposeRule
import com.wcaokaze.probosqis.cache.core.WritableCache
import com.wcaokaze.probosqis.page.PageStackBoard
import com.wcaokaze.probosqis.page.PageStackBoardRepository
import com.wcaokaze.probosqis.page.PageStackRepository
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

      val pageStackRepository = mockk<PageStackRepository>()

      val loadedCache = loadPageStackBoardOrDefault(
         pageStackBoardRepository, pageStackRepository)

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

      val loadedCache = loadPageStackBoardOrDefault(
         pageStackBoardRepository, pageStackRepository)

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
}
