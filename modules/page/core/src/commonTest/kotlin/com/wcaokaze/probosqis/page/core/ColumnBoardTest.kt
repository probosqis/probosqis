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

package com.wcaokaze.probosqis.page.core

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFails

class ColumnBoardTest {
   private val clock = object : Clock {
      override fun now() = Instant.parse("2000-01-01T00:00:00.000Z")
   }

   @Serializable
   class PageImpl(val i: Int) : Page()

   private val ColumnBoard.columns: List<Column>
      get() = (0 until columnCount).map { this[it] }

   @Test
   fun insert() {
      var columnBoard = ColumnBoard(emptyList())
      columnBoard = columnBoard.inserted(0, Column(PageImpl(0), clock))
      columnBoard = columnBoard.inserted(0, Column(PageImpl(1), clock))
      columnBoard = columnBoard.inserted(1, Column(PageImpl(2), clock))
      columnBoard = columnBoard.inserted(3, Column(PageImpl(3), clock))

      assertEquals(4, columnBoard.columnCount)

      assertContentEquals(
         listOf(1, 2, 0, 3),
         columnBoard.columns.map { (it.head as PageImpl).i }
      )
   }

   @Test
   fun insert_outOfBounds() {
      assertFails {
         val columnBoard = ColumnBoard(emptyList())
         columnBoard.inserted(1, Column(PageImpl(0), clock))
      }

      assertFails {
         val columnBoard = ColumnBoard(emptyList())
         columnBoard.inserted(-1, Column(PageImpl(0), clock))
      }

      assertFails {
         val columnBoard = ColumnBoard(
            listOf(Column(PageImpl(0), clock))
         )
         columnBoard.inserted(2, Column(PageImpl(1), clock))
      }
   }

   @Test
   fun remove() {
      var columnBoard = ColumnBoard(
         List(5) { i ->
            Column(PageImpl(i), clock)
         }
      )

      columnBoard = columnBoard.removed(1)

      assertEquals(4, columnBoard.columnCount)

      assertContentEquals(
         listOf(0, 2, 3, 4),
         columnBoard.columns.map { (it.head as PageImpl).i }
      )
   }

   @Test
   fun remove_outOfBounds() {
      val columnBoard = ColumnBoard(
         List(5) { i ->
            Column(PageImpl(i), clock)
         }
      )

      assertFails {
         columnBoard.removed(6)
      }

      assertFails {
         columnBoard.removed(-1)
      }
   }

   @Test
   fun immutability() {
      val columnBoard = ColumnBoard(emptyList())

      val addedColumnBoard = columnBoard.inserted(0, Column(PageImpl(0), clock))
      assertEquals(0, columnBoard.columnCount)
      assertEquals(1, addedColumnBoard.columnCount)

      val removedColumnBoard = addedColumnBoard.removed(0)
      assertEquals(0, columnBoard.columnCount)
      assertEquals(1, addedColumnBoard.columnCount)
      assertEquals(0, removedColumnBoard.columnCount)
   }
}
