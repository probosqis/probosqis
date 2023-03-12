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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlin.test.*

class ColumnTest {
   private val columnCreatedTime = LocalDateTime(2000, Month.JANUARY, 1, 0, 0)

   @Serializable
   @SerialName("com.wcaokaze.probosqis.page.IntPage")
   class IntPage(val i: Int) : Page()

   @Serializable
   @SerialName("com.wcaokaze.probosqis.page.StringPage")
   class StringPage(val s: String) : Page()

   @Test
   fun add_pop() {
      var column = Column(IntPage(0), columnCreatedTime)
      column = column.added(StringPage("1"))
      column = column.added(StringPage("2"))
      column = column.added(IntPage(3))

      var head = column.head
      assertIs<IntPage>(head)
      assertEquals(3, head.i)

      var tail = column.tailOrNull()
      assertNotNull(tail)
      head = tail.head
      assertIs<StringPage>(head)
      assertEquals("2", head.s)

      tail = tail.tailOrNull() ?: fail()
      head = tail.head
      assertIs<StringPage>(head)
      assertEquals("1", head.s)

      tail = tail.tailOrNull() ?: fail()
      head = tail.head
      assertIs<IntPage>(head)
      assertEquals(0, head.i)

      assertNull(tail.tailOrNull())
   }

   @Test
   fun immutability() {
      var column = Column(IntPage(0), columnCreatedTime)
      column = column.added(StringPage("1"))

      val tail = column.tailOrNull()
      assertNotNull(tail)
      val tailHead = tail.head
      assertIs<IntPage>(tailHead)
      assertEquals(0, tailHead.i)

      val columnHead = column.head
      assertIs<StringPage>(columnHead)
      assertEquals("1", columnHead.s)
   }
}
