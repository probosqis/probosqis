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
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList

class PageStackBoard(val column: Column) {
   sealed class LayoutElement

   class PageStack(
      val cache: WritableCache<com.wcaokaze.probosqis.page.PageStack>
   ) : LayoutElement()

   class Column(
      private val children: ImmutableList<LayoutElement>
   ) : LayoutElement() {
      val childCount: Int get() = children.size

      operator fun get(index: Int): LayoutElement = children[index]

      fun inserted(index: Int, element: LayoutElement) = Column(
         children.toPersistentList().add(index, element)
      )

      fun removed(index: Int) = Column(
         children.toPersistentList().removeAt(index)
      )

      fun replaced(index: Int, element: LayoutElement) = Column(
         children.toPersistentList().set(index, element)
      )
   }

   class Row(
      private val children: ImmutableList<LayoutElement>
   ) : LayoutElement() {
      val childCount: Int get() = children.size

      operator fun get(index: Int): LayoutElement = children[index]

      fun inserted(index: Int, element: LayoutElement) = Row(
         children.toPersistentList().add(index, element)
      )

      fun removed(index: Int) = Row(
         children.toPersistentList().removeAt(index)
      )

      fun replaced(index: Int, element: LayoutElement) = Row(
         children.toPersistentList().set(index, element)
      )
   }
}
