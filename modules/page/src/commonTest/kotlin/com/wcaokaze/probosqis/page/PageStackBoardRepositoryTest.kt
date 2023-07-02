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

import com.wcaokaze.probosqis.ext.kotlin.datetime.MockClock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.test.*

expect fun createPageStackBoardRepository(
   allPageClasses: List<PageStackBoardRepository.PageSerializer<*>>
): PageStackBoardRepository

expect fun deletePageStackBoardRepository(
   pageStackBoardRepository: PageStackBoardRepository
)

class PageStackBoardRepositoryTest {
   @Serializable
   @SerialName("com.wcaokaze.probosqis.page.IntPage")
   class IntPage(val i: Int) : Page()

   @Serializable
   @SerialName("com.wcaokaze.probosqis.page.StringPage")
   class StringPage(val s: String) : Page()

   private lateinit var pageStackBoardRepository: PageStackBoardRepository

   @BeforeTest
   fun beforeTest() {
      pageStackBoardRepository = createPageStackBoardRepository(
         listOf(
            pageSerializer<IntPage>(),
            pageSerializer<StringPage>(),
         )
      )
   }

   @AfterTest
   fun afterTest() {
      deletePageStackBoardRepository(pageStackBoardRepository)
   }

   @Test
   fun readWrite() {
      val intPage = IntPage(42)
      val stringPage = StringPage("wcaokaze")
      var pageStack = PageStack(intPage, MockClock())
      pageStack = pageStack.added(stringPage)
      val pageStackBoard = PageStackBoard(listOf(pageStack))

      pageStackBoardRepository.savePageStackBoard(pageStackBoard)

      val loadedCache = pageStackBoardRepository.loadPageStackBoard()

      assertEquals(loadedCache.value.pageStackCount, 1)

      val page1 = loadedCache.value[0].head
      assertIs<StringPage>(page1)
      assertEquals(stringPage.s, page1.s)

      var tail = loadedCache.value[0].tailOrNull()
      assertNotNull(tail)
      val page2 = tail.head
      assertIs<IntPage>(page2)
      assertEquals(intPage.i, page2.i)

      tail = tail.tailOrNull()
      assertNull(tail)
   }
}
