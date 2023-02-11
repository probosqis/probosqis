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

package com.wcaokaze.probosqis.page.perpetuation

import com.wcaokaze.probosqis.page.core.Column
import com.wcaokaze.probosqis.page.core.Page
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.test.*

expect fun createColumnRepository(
   allPageClasses: List<ColumnRepository.PageSerializer<*>>
): ColumnRepository

expect fun deleteColumnRepository(columnRepository: ColumnRepository)

class ColumnRepositoryTest {
   @Serializable
   @SerialName("com.wcaokaze.probosqis.perpetuation.page.IntPage")
   class IntPage(val i: Int) : Page()

   @Serializable
   @SerialName("com.wcaokaze.probosqis.perpetuation.page.StringPage")
   class StringPage(val s: String) : Page()

   private lateinit var columnRepository: ColumnRepository

   @BeforeTest
   fun beforeTest() {
      columnRepository = createColumnRepository(
         listOf(
            pageSerializer<IntPage>(),
            pageSerializer<StringPage>(),
         )
      )
   }

   @AfterTest
   fun afterTest() {
      deleteColumnRepository(columnRepository)
   }

   @Test
   fun readWrite() {
      val intPage = IntPage(42)
      val stringPage = StringPage("wcaokaze")
      var column = Column(intPage)
      column = column.added(stringPage)

      columnRepository.writeColumn(column)

      val deserializedColumn = columnRepository.loadColumn()

      val page1 = deserializedColumn.head
      assertIs<StringPage>(page1)
      assertEquals(stringPage.s, page1.s)

      var tail = deserializedColumn.tailOrNull()
      assertNotNull(tail)
      val page2 = tail.head
      assertIs<IntPage>(page2)
      assertEquals(intPage.i, page2.i)

      tail = tail.tailOrNull()
      assertNull(tail)
   }
}
