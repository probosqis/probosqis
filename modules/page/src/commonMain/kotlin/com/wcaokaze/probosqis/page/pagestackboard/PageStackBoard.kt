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

import androidx.compose.foundation.gestures.animateScrollBy
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
   sealed class LayoutElementParent : LayoutElement(), Iterable<LayoutElement> {
      abstract val children: List<LayoutElement>

      /** 葉ノードの数。すなわちこのサブツリーの子孫にある[PageStack]の数。 */
      internal abstract val leafCount: Int

      val childCount: Int get() = children.size

      protected fun countLeafs(children: List<LayoutElement>): Int {
         return children.sumOf {
            when (it) {
               is PageStack -> 1
               is LayoutElementParent -> it.leafCount
            }
         }
      }

      operator fun get(index: Int): LayoutElement = children[index]

      override operator fun iterator(): Iterator<LayoutElement>
            = children.iterator()
   }

   @Serializable
   class Column(
      override val children: List<LayoutElement>
   ) : LayoutElementParent() {
      override val leafCount: Int = countLeafs(children)

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
      override val children: List<LayoutElement>
   ) : LayoutElementParent() {
      override val leafCount: Int = countLeafs(children)

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

   val pageStackCount = rootRow.leafCount
}

operator fun PageStackBoard.get(index: Int): PageStackBoard.PageStack {
   fun PageStackBoard.LayoutElementParent
         .findSubtree(indexInSubtree: Int): PageStackBoard.PageStack
   {
      assert(indexInSubtree >= 0)

      var nodeIndex = 0
      for (node in this) {
         when (node) {
            is PageStackBoard.PageStack -> {
               if (nodeIndex == indexInSubtree) { return node }
               nodeIndex++
            }
            is PageStackBoard.LayoutElementParent -> {
               if (nodeIndex + node.leafCount > indexInSubtree) {
                  return node.findSubtree(indexInSubtree - nodeIndex)
               }
               nodeIndex += node.leafCount
            }
         }
      }

      throw IndexOutOfBoundsException(
         "pageStack count: $nodeIndex, specified index: $index")
   }

   if (index < 0) { throw IndexOutOfBoundsException("specified index: $index") }

   return rootRow.findSubtree(index)
}

fun PageStackBoard.sequence(): Sequence<PageStackBoard.PageStack> {
   suspend fun SequenceScope<PageStackBoard.PageStack>
         .yieldChildren(parent: PageStackBoard.LayoutElementParent)
   {
      for (node in parent) {
         when (node) {
            is PageStackBoard.PageStack           -> yield(node)
            is PageStackBoard.LayoutElementParent -> yieldChildren(node)
         }
      }
   }

   return sequence {
      yieldChildren(rootRow)
   }
}

@Stable
sealed class PageStackBoardState(
   pageStackBoardCache: WritableCache<PageStackBoard>,
   private val pageStackRepository: PageStackRepository
) {
   internal var pageStackBoard: PageStackBoard
         by pageStackBoardCache.asMutableState()

   internal val scrollState = PageStackBoardScrollState()
   internal val layout = LayoutState()

   abstract val firstVisiblePageStackIndex: Int

   suspend fun animateScrollTo(index: Int) {
      val targetScrollOffset = getScrollOffsetForPageStack(index)
      scrollState.animateScrollBy(targetScrollOffset - scrollState.scrollOffset)
   }

   internal fun getScrollOffsetForPageStack(index: Int): Int {
      val pageStackLayout = layout.pageStackLayout(index)
      return pageStackLayout.position.x - layout.pageStackPadding * 2
   }

   suspend fun addColumn(index: Int, pageStack: PageStack) {
      val pageStackCache = pageStackRepository.savePageStack(pageStack)

      pageStackBoard = PageStackBoard(
         pageStackBoard.rootRow.inserted(
            index,
            PageStackBoard.PageStack(pageStackCache)
         )
      )
   }
}
