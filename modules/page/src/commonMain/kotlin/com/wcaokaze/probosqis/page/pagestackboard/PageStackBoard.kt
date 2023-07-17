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

import androidx.compose.runtime.Stable
import com.wcaokaze.probosqis.cache.compose.asMutableState
import com.wcaokaze.probosqis.cache.core.WritableCache
import com.wcaokaze.probosqis.page.PageStack
import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal class PageStackCacheSerializer(
   private val pageStackRepository: PageStackRepository
) : KSerializer<WritableCache<PageStack>> {
   override val descriptor: SerialDescriptor
      get() = PrimitiveSerialDescriptor("Cache", PrimitiveKind.LONG)

   override fun serialize(encoder: Encoder, value: WritableCache<PageStack>) {
      encoder.encodeLong(value.value.id.value)
   }

   override fun deserialize(decoder: Decoder): WritableCache<PageStack> {
      val id = PageStack.Id(decoder.decodeLong())
      return pageStackRepository.loadPageStack(id)
   }
}

@Serializable
class PageStackBoard(val rootRow: Row) {
   @Serializable
   sealed class LayoutElement

   @Serializable
   class PageStack(
      @Contextual
      val cache: WritableCache<com.wcaokaze.probosqis.page.PageStack>
   ) : LayoutElement()

   @Serializable
   class Column(
      private val children: List<LayoutElement>
   ) : LayoutElement() {
      val childCount: Int get() = children.size

      operator fun get(index: Int): LayoutElement = children[index]

      fun inserted(index: Int, element: LayoutElement) = Column(
         buildList(capacity = children.size + 1) {
            addAll(children.subList(0, index))
            add(element)
            addAll(children.subList(index, children.size))
         }
      )

      fun removed(index: Int) = Column(
         buildList(capacity = children.size) {
            addAll(children.subList(0, index))
            addAll(children.subList(index + 1, children.size))
         }
      )

      fun replaced(index: Int, element: LayoutElement) = Column(
         buildList(capacity = children.size) {
            addAll(children)
            set(index, element)
         }
      )
   }

   @Serializable
   class Row(
      private val children: List<LayoutElement>
   ) : LayoutElement() {
      val childCount: Int get() = children.size

      operator fun get(index: Int): LayoutElement = children[index]

      fun inserted(index: Int, element: LayoutElement) = Row(
         buildList(capacity = children.size + 1) {
            addAll(children.subList(0, index))
            add(element)
            addAll(children.subList(index, children.size))
         }
      )

      fun removed(index: Int) = Row(
         buildList(capacity = children.size) {
            addAll(children.subList(0, index))
            addAll(children.subList(index + 1, children.size))
         }
      )

      fun replaced(index: Int, element: LayoutElement) = Row(
         buildList(capacity = children.size) {
            addAll(children)
            set(index, element)
         }
      )
   }
}

@Stable
class PageStackBoardState(pageStackBoardCache: WritableCache<PageStackBoard>) {
   internal var pageStackBoard: PageStackBoard
         by pageStackBoardCache.asMutableState()

   suspend fun animateScrollTo(pageStack: Int) {
      TODO()
   }
}
