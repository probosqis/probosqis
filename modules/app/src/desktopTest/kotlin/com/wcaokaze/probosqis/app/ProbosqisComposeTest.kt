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
import com.wcaokaze.probosqis.page.ColumnBoard
import com.wcaokaze.probosqis.page.ColumnBoardRepository
import io.mockk.every
import io.mockk.mockk
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
   fun loadColumnBoard() {
      val columnBoard = ColumnBoard(emptyList())

      val columnBoardRepository = mockk<ColumnBoardRepository> {
         every { loadColumnBoard() } returns WritableCache(columnBoard)
      }

      val loadedCache = loadColumnBoardOrDefault(columnBoardRepository)
      assertSame(columnBoard, loadedCache.value)
   }

   @Test
   fun loadColumnBoard_default() {
      val columnBoardRepository = mockk<ColumnBoardRepository> {
         every { loadColumnBoard() } throws IOException()
         every { saveColumnBoard(any()) } answers { WritableCache(firstArg()) }
      }

      val loadedCache = loadColumnBoardOrDefault(columnBoardRepository)
      assertEquals(2, loadedCache.value.columnCount)
      assertIs<TestPage>(loadedCache.value[0].head)
      assertNull(loadedCache.value[0].tailOrNull())
      assertIs<TestPage>(loadedCache.value[1].head)
      assertNull(loadedCache.value[1].tailOrNull())
   }
}
