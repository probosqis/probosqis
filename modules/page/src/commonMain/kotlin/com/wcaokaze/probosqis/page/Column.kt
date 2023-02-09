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

import kotlinx.serialization.Serializable

@Serializable
class Column private constructor(private val pages: List<Page>) {
   constructor(page: Page) : this(listOf(page))

   /** このColumnの一番上の[Page] */
   val head: Page get() = pages.last()

   /**
    * @return
    * このColumnの一番上の[Page]を取り除いたColumn。
    * このColumnにPageがひとつしかない場合はnull
    */
   fun tailOrNull(): Column? {
      val tailPages = pages.dropLast(1)
      return if (tailPages.isEmpty()) { null } else { Column(tailPages) }
   }

   fun added(page: Page) = Column(pages + page)
}
