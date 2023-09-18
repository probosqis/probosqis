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

class PageStackTest {
   @Serializable
   @SerialName("com.wcaokaze.probosqis.page.IntPage")
   class IntPage(val i: Int) : Page()

   @Serializable
   @SerialName("com.wcaokaze.probosqis.page.StringPage")
   class StringPage(val s: String) : Page()

   @Test
   fun add_pop() {
      var pageStack = PageStack(IntPage(0), MockClock())
      pageStack = pageStack.added(StringPage("1"))
      pageStack = pageStack.added(StringPage("2"))
      pageStack = pageStack.added(IntPage(3))

      var head = pageStack.head
      assertIs<IntPage>(head)
      assertEquals(3, head.i)

      var tail = pageStack.tailOrNull()
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
      var pageStack = PageStack(IntPage(0), MockClock())
      pageStack = pageStack.added(StringPage("1"))

      val tail = pageStack.tailOrNull()
      assertNotNull(tail)
      val tailHead = tail.head
      assertIs<IntPage>(tailHead)
      assertEquals(0, tailHead.i)

      val pageStackHead = pageStack.head
      assertIs<StringPage>(pageStackHead)
      assertEquals("1", pageStackHead.s)
   }
}
