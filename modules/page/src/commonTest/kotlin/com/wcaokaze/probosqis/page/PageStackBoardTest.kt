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
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFails

class PageStackBoardTest {
   @Serializable
   class PageImpl(val i: Int) : Page()

   private val PageStackBoard.pageStacks: List<PageStack>
      get() = (0 until pageStackCount).map { this[it] }

   @Test
   fun insert() {
      val clock = MockClock()
      var pageStackBoard = PageStackBoard(emptyList())
      pageStackBoard = pageStackBoard.inserted(0, PageStack(PageImpl(0), clock))
      pageStackBoard = pageStackBoard.inserted(0, PageStack(PageImpl(1), clock))
      pageStackBoard = pageStackBoard.inserted(1, PageStack(PageImpl(2), clock))
      pageStackBoard = pageStackBoard.inserted(3, PageStack(PageImpl(3), clock))

      assertEquals(4, pageStackBoard.pageStackCount)

      assertContentEquals(
         listOf(1, 2, 0, 3),
         pageStackBoard.pageStacks.map { (it.head as PageImpl).i }
      )
   }

   @Test
   fun insert_outOfBounds() {
      val clock = MockClock()

      assertFails {
         val pageStackBoard = PageStackBoard(emptyList())
         pageStackBoard.inserted(1, PageStack(PageImpl(0), clock))
      }

      assertFails {
         val pageStackBoard = PageStackBoard(emptyList())
         pageStackBoard.inserted(-1, PageStack(PageImpl(0), clock))
      }

      assertFails {
         val pageStackBoard = PageStackBoard(
            listOf(PageStack(PageImpl(0), clock))
         )
         pageStackBoard.inserted(2, PageStack(PageImpl(1), clock))
      }
   }

   @Test
   fun remove() {
      var pageStackBoard = PageStackBoard(
         List(5) { i ->
            PageStack(PageImpl(i), MockClock())
         }
      )

      pageStackBoard = pageStackBoard.removed(1)

      assertEquals(4, pageStackBoard.pageStackCount)

      assertContentEquals(
         listOf(0, 2, 3, 4),
         pageStackBoard.pageStacks.map { (it.head as PageImpl).i }
      )
   }

   @Test
   fun remove_outOfBounds() {
      val pageStackBoard = PageStackBoard(
         List(5) { i ->
            PageStack(PageImpl(i), MockClock())
         }
      )

      assertFails {
         pageStackBoard.removed(6)
      }

      assertFails {
         pageStackBoard.removed(-1)
      }
   }

   @Test
   fun immutability() {
      val pageStackBoard = PageStackBoard(emptyList())

      val addedPageStackBoard = pageStackBoard
         .inserted(0, PageStack(PageImpl(0), MockClock()))
      assertEquals(0, pageStackBoard.pageStackCount)
      assertEquals(1, addedPageStackBoard.pageStackCount)

      val removedPageStackBoard = addedPageStackBoard.removed(0)
      assertEquals(0, pageStackBoard.pageStackCount)
      assertEquals(1, addedPageStackBoard.pageStackCount)
      assertEquals(0, removedPageStackBoard.pageStackCount)
   }
}
