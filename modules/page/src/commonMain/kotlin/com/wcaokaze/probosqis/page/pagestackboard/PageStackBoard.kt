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

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.wcaokaze.probosqis.cache.compose.asMutableState
import com.wcaokaze.probosqis.cache.core.WritableCache
import com.wcaokaze.probosqis.page.PageStack
import com.wcaokaze.probosqis.page.PageStackState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.math.roundToInt

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

/**
 * PageStackBoard内の位置
 * @see PageStackBoardState.animateScroll
 */
enum class PositionInBoard {
   /** 表示されている領域の一番左（Ltr時） */
   FirstVisible,
   /** 表示されている領域の一番右（Rtl時） */
   LastVisible,
   /**
    * 現在の位置から最も近い表示される位置。つまり、目的のPageStackが現在の表示領域
    * より右にある場合、表示領域の一番右、目的のPageStackが現在の表示領域より左に
    * ある場合、表示領域の一番左、目的のPageStackがすでに現在表示されている場合、
    * 現在の位置。
    */
   NearestVisible,
}

@Stable
sealed class PageStackBoardState(
   pageStackBoardCache: WritableCache<PageStackBoard>,
   private val pageStackRepository: PageStackRepository,
   private val animCoroutineScope: CoroutineScope
) {
   private val pageStackBoardState = pageStackBoardCache.asMutableState()

   internal var pageStackBoard: PageStackBoard
      get() = pageStackBoardState.value
      set(value) {
         pageStackBoardState.value = value
         layout.recreateLayoutState(value)
      }

   internal val scrollState = PageStackBoardScrollState()

   internal val layout = LayoutLogic(
      pageStackBoard,
      pageStackStateConstructor = { pageStackCache,->
         PageStackState(pageStackCache, pageStackBoardState = this)
      }
   )

   abstract val firstVisiblePageStackIndex: Int

   private var pageStackInsertionAnimOffset by mutableStateOf(0.0f)

   internal abstract fun pageStackState(id: PageStack.Id): PageStackState?
   internal abstract fun pageStackState(index: Int): PageStackState

   suspend fun animateScroll(
      pageStackIndex: Int,
      targetPositionInBoard: PositionInBoard = PositionInBoard.NearestVisible,
      animationSpec: AnimationSpec<Float> = spring()
   ) {
      val pageStackLayout = layout.pageStackLayout(pageStackIndex)
      pageStackLayout.awaitInitialized()
      val targetScrollOffset
            = getScrollOffset(pageStackLayout, targetPositionInBoard)

      scrollState.animateScrollBy(
         targetScrollOffset - scrollState.scrollOffset, animationSpec)
   }

   suspend fun animateScroll(
      pageStackId: PageStack.Id,
      targetPositionInBoard: PositionInBoard = PositionInBoard.NearestVisible,
      animationSpec: AnimationSpec<Float> = spring()
   ) {
      val pageStackLayout = layout.pageStackLayout(pageStackId)
         ?: throw IllegalArgumentException("PageStack not found: $pageStackId")
      pageStackLayout.awaitInitialized()
      val targetScrollOffset
            = getScrollOffset(pageStackLayout, targetPositionInBoard)

      scrollState.animateScrollBy(
         targetScrollOffset - scrollState.scrollOffset, animationSpec)
   }

   internal fun getScrollOffset(
      pageStackIndex: Int,
      targetPositionInBoard: PositionInBoard
   ): Int = getScrollOffset(
      pageStackLayoutState = layout.pageStackLayout(pageStackIndex),
      targetPositionInBoard
   )

   internal fun getScrollOffset(
      pageStackId: PageStack.Id,
      targetPositionInBoard: PositionInBoard
   ): Int = getScrollOffset(
      pageStackLayoutState = layout.pageStackLayout(pageStackId)
         ?: throw IllegalArgumentException("PageStack not found: $pageStackId"),
      targetPositionInBoard
   )

   /**
    * 指定したPageStackが指定した位置にあるときの
    * [scrollOffset][PageStackBoardScrollState.scrollOffset]。
    * ただし実際にその位置までスクロールできるとは限らない。
    */
   internal fun getScrollOffset(
      pageStackLayoutState: LayoutLogic.PageStackLayoutState,
      targetPositionInBoard: PositionInBoard
   ): Int {
      when (targetPositionInBoard) {
         PositionInBoard.FirstVisible -> {
            return pageStackLayoutState.position.x - layout.pageStackPadding * 2
         }
         PositionInBoard.LastVisible -> {
            return pageStackLayoutState.position.x - (
                  layout.pageStackBoardWidth - pageStackLayoutState.width
                  - layout.pageStackPadding * 2
            )
         }
         PositionInBoard.NearestVisible -> {
            return scrollState.scrollOffset.roundToInt()
               .coerceIn(
                  getScrollOffset(pageStackLayoutState, PositionInBoard.LastVisible),
                  getScrollOffset(pageStackLayoutState, PositionInBoard.FirstVisible)
               )
         }
      }
   }

   suspend fun addColumn(index: Int, pageStack: PageStack) {
      val pageStackCache = pageStackRepository.savePageStack(pageStack)

      pageStackBoard = PageStackBoard(
         pageStackBoard.rootRow.inserted(
            index,
            PageStackBoard.PageStack(pageStackCache)
         )
      )

      coroutineScope {
         launch {
            animateScroll(pageStack.id, PositionInBoard.NearestVisible,
               layout.pageStackPositionAnimSpec())
         }
         launch {
            layout.pageStackLayout(pageStack.id)
               ?.animateInsertion(pageStackInsertionAnimOffset)
         }
      }
   }

   internal fun layout(
      density: Density,
      pageStackBoardWidth: Int,
      pageStackCount: Int,
      pageStackPadding: Int
   ) {
      with (density) {
         pageStackInsertionAnimOffset = 64.dp.toPx()
      }

      layout.layout(animCoroutineScope, pageStackBoardWidth, pageStackCount,
         pageStackPadding, scrollState)
   }
}
