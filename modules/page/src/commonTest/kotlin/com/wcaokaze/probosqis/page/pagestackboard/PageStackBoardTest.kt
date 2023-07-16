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

import com.wcaokaze.probosqis.cache.core.WritableCache
import com.wcaokaze.probosqis.page.Page
import com.wcaokaze.probosqis.page.PageStack
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertIs
import kotlin.test.assertNotSame
import kotlin.test.assertSame

class PageStackBoardTest {
   @Serializable
   private class PageImpl(val i: Int) : Page()

   private fun createPageStackElement(i: Int): PageStackBoard.PageStack {
      val page = PageImpl(i)
      val pageStack = PageStack(page)
      val cache = WritableCache(pageStack)
      return PageStackBoard.PageStack(cache)
   }

   private fun createColumn(pageStackCount: Int): PageStackBoard.Column {
      val children = List(pageStackCount) { createPageStackElement(it) }
         .toImmutableList()

      return PageStackBoard.Column(children)
   }

   private fun createRow(pageStackCount: Int): PageStackBoard.Row {
      val children = List(pageStackCount) { createPageStackElement(it) }
         .toImmutableList()

      return PageStackBoard.Row(children)
   }

   private fun assertPage(expected: Int, element: PageStackBoard.LayoutElement) {
      assertIs<PageStackBoard.PageStack>(element)
      val page = assertIs<PageImpl>(element.cache.value.head)
      assertEquals(expected, page.i)
   }

   @Test
   fun column_insert() {
      var column = createColumn(2)
      val inserted = createColumn(0)
      column = column.inserted(1, inserted)

      assertEquals(3, column.childCount)
      assertPage(0, column[0])
      assertSame(inserted, column[1])
      assertPage(1, column[2])
   }

   @Test
   fun column_insertFirst() {
      var column = createColumn(2)
      val inserted = createColumn(0)
      column = column.inserted(0, inserted)

      assertEquals(3, column.childCount)
      assertSame(inserted, column[0])
      assertPage(0, column[1])
      assertPage(1, column[2])
   }

   @Test
   fun column_insertLast() {
      var column = createColumn(2)
      val inserted = createColumn(0)
      column = column.inserted(2, inserted)

      assertEquals(3, column.childCount)
      assertPage(0, column[0])
      assertPage(1, column[1])
      assertSame(inserted, column[2])
   }

   @Test
   fun column_insertOutOfBounds() {
      val column = createColumn(2)
      val inserted = createColumn(0)

      assertFails {
         column.inserted(3, inserted)
      }
   }

   @Test
   fun column_insertNegativeIndex() {
      val column = createColumn(2)
      val inserted = createColumn(0)

      assertFails {
         column.inserted(-1, inserted)
      }
   }

   @Test
   fun column_remove() {
      var column = createColumn(3)
      column = column.removed(1)

      assertEquals(2, column.childCount)
      assertPage(0, column[0])
      assertPage(2, column[1])
   }

   @Test
   fun column_removeFirst() {
      var column = createColumn(3)
      column = column.removed(0)

      assertEquals(2, column.childCount)
      assertPage(1, column[0])
      assertPage(2, column[1])
   }

   @Test
   fun column_removeLast() {
      var column = createColumn(3)
      column = column.removed(2)

      assertEquals(2, column.childCount)
      assertPage(0, column[0])
      assertPage(1, column[1])
   }

   @Test
   fun column_removeOutOfBounds() {
      val column = createColumn(3)

      assertFails {
         column.removed(3)
      }
   }

   @Test
   fun column_removeNegativeIndex() {
      val column = createColumn(3)

      assertFails {
         column.removed(-1)
      }
   }

   @Test
   fun column_replace() {
      var column = createColumn(3)
      val replaced = createColumn(0)
      column = column.replaced(1, replaced)

      assertEquals(3, column.childCount)
      assertPage(0, column[0])
      assertSame(replaced, column[1])
      assertPage(2, column[2])
   }

   @Test
   fun column_replaceFirst() {
      var column = createColumn(3)
      val replaced = createColumn(0)
      column = column.replaced(0, replaced)

      assertEquals(3, column.childCount)
      assertSame(replaced, column[0])
      assertPage(1, column[1])
      assertPage(2, column[2])
   }

   @Test
   fun column_replaceLast() {
      var column = createColumn(3)
      val replaced = createColumn(0)
      column = column.replaced(2, replaced)

      assertEquals(3, column.childCount)
      assertPage(0, column[0])
      assertPage(1, column[1])
      assertSame(replaced, column[2])
   }

   @Test
   fun column_replaceOutOfBounds() {
      val column = createColumn(3)
      val replaced = createColumn(0)

      assertFails {
         column.replaced(3, replaced)
      }
   }

   @Test
   fun column_replaceNegativeIndex() {
      val column = createColumn(3)
      val replaced = createColumn(0)

      assertFails {
         column.replaced(-1, replaced)
      }
   }

   @Test
   fun column_insertImmutability() {
      val column = createColumn(2)

      val insertedColumn = column.inserted(0, createPageStackElement(2))
      assertNotSame(column, insertedColumn)
      assertEquals(2, column.childCount)
      assertPage(0, column[0])
      assertPage(1, column[1])
      assertEquals(3, insertedColumn.childCount)
   }

   @Test
   fun column_removeImmutability() {
      val column = createColumn(2)

      val removedColumn = column.removed(0)
      assertNotSame(column, removedColumn)
      assertEquals(2, column.childCount)
      assertPage(0, column[0])
      assertPage(1, column[1])
      assertEquals(1, removedColumn.childCount)
   }

   @Test
   fun column_replaceImmutability() {
      val column = createColumn(2)

      val replacedColumn = column.replaced(0, createPageStackElement(2))
      assertNotSame(column, replacedColumn)
      assertEquals(2, column.childCount)
      assertPage(0, column[0])
      assertPage(1, column[1])
      assertEquals(2, replacedColumn.childCount)
   }

   @Test
   fun row_insert() {
      var row = createRow(2)
      val inserted = createColumn(0)
      row = row.inserted(1, inserted)

      assertEquals(3, row.childCount)
      assertPage(0, row[0])
      assertSame(inserted, row[1])
      assertPage(1, row[2])
   }

   @Test
   fun row_insertFirst() {
      var row = createRow(2)
      val inserted = createColumn(0)
      row = row.inserted(0, inserted)

      assertEquals(3, row.childCount)
      assertSame(inserted, row[0])
      assertPage(0, row[1])
      assertPage(1, row[2])
   }

   @Test
   fun row_insertLast() {
      var row = createRow(2)
      val inserted = createColumn(0)
      row = row.inserted(2, inserted)

      assertEquals(3, row.childCount)
      assertPage(0, row[0])
      assertPage(1, row[1])
      assertSame(inserted, row[2])
   }

   @Test
   fun row_insertOutOfBounds() {
      val row = createRow(2)
      val inserted = createColumn(0)

      assertFails {
         row.inserted(3, inserted)
      }
   }

   @Test
   fun row_insertNegativeIndex() {
      val row = createRow(2)
      val inserted = createColumn(0)

      assertFails {
         row.inserted(-1, inserted)
      }
   }

   @Test
   fun row_remove() {
      var row = createRow(3)
      row = row.removed(1)

      assertEquals(2, row.childCount)
      assertPage(0, row[0])
      assertPage(2, row[1])
   }

   @Test
   fun row_removeFirst() {
      var row = createRow(3)
      row = row.removed(0)

      assertEquals(2, row.childCount)
      assertPage(1, row[0])
      assertPage(2, row[1])
   }

   @Test
   fun row_removeLast() {
      var row = createRow(3)
      row = row.removed(2)

      assertEquals(2, row.childCount)
      assertPage(0, row[0])
      assertPage(1, row[1])
   }

   @Test
   fun row_removeOutOfBounds() {
      val row = createRow(3)

      assertFails {
         row.removed(3)
      }
   }

   @Test
   fun row_removeNegativeIndex() {
      val row = createRow(3)

      assertFails {
         row.removed(-1)
      }
   }

   @Test
   fun row_replace() {
      var row = createRow(3)
      val replaced = createColumn(0)
      row = row.replaced(1, replaced)

      assertEquals(3, row.childCount)
      assertPage(0, row[0])
      assertSame(replaced, row[1])
      assertPage(2, row[2])
   }

   @Test
   fun row_replaceFirst() {
      var row = createRow(3)
      val replaced = createColumn(0)
      row = row.replaced(0, replaced)

      assertEquals(3, row.childCount)
      assertSame(replaced, row[0])
      assertPage(1, row[1])
      assertPage(2, row[2])
   }

   @Test
   fun row_replaceLast() {
      var row = createRow(3)
      val replaced = createColumn(0)
      row = row.replaced(2, replaced)

      assertEquals(3, row.childCount)
      assertPage(0, row[0])
      assertPage(1, row[1])
      assertSame(replaced, row[2])
   }

   @Test
   fun row_replaceOutOfBounds() {
      val row = createRow(3)
      val replaced = createColumn(0)

      assertFails {
         row.replaced(3, replaced)
      }
   }

   @Test
   fun row_replaceNegativeIndex() {
      val row = createRow(3)
      val replaced = createColumn(0)

      assertFails {
         row.replaced(-1, replaced)
      }
   }

   @Test
   fun row_insertImmutability() {
      val row = createRow(2)

      val insertedRow = row.inserted(0, createPageStackElement(2))
      assertNotSame(row, insertedRow)
      assertEquals(2, row.childCount)
      assertPage(0, row[0])
      assertPage(1, row[1])
      assertEquals(3, insertedRow.childCount)
   }

   @Test
   fun row_removeImmutability() {
      val row = createRow(2)

      val removedRow = row.removed(0)
      assertNotSame(row, removedRow)
      assertEquals(2, row.childCount)
      assertPage(0, row[0])
      assertPage(1, row[1])
      assertEquals(1, removedRow.childCount)
   }

   @Test
   fun row_replaceImmutability() {
      val row = createRow(2)

      val replacedRow = row.replaced(0, createPageStackElement(2))
      assertNotSame(row, replacedRow)
      assertEquals(2, row.childCount)
      assertPage(0, row[0])
      assertPage(1, row[1])
      assertEquals(2, replacedRow.childCount)
   }
}
