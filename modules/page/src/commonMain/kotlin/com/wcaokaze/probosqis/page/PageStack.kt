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

import androidx.compose.animation.core.ExperimentalTransitionApi
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.createChildTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.wcaokaze.probosqis.cache.compose.asState
import com.wcaokaze.probosqis.cache.core.WritableCache
import com.wcaokaze.probosqis.cache.core.update
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

fun PageStack(
   savedPageState: PageStack.SavedPageState,
   clock: Clock = Clock.System
) = PageStack(
   PageStack.Id(clock.now()),
   savedPageState
)

/**
 * 画面遷移した[Page]をStack状に積んだもの。
 *
 * 必ず1つ以上の[Page]を持つ（空のPageStackという概念はない）。
 *
 * UI上はカラムと呼ばれるが列ではない（列は[PageStackBoard.Column]）ので
 * 型としての名称はPageStack
 */
@Serializable
class PageStack private constructor(
   val id: Id, // TODO: キャッシュシステムの完成後Cache自体が識別子となるため削除する
   private val savedPageStates: List<SavedPageState>
) {
   @Serializable
   @JvmInline
   value class Id(val value: Long) {
      constructor(createdTime: Instant) : this(createdTime.toEpochMilliseconds())
   }

   @Stable
   @Serializable
   class SavedPageState(
      val id: PageId,
      val page: Page
   )

   @Serializable
   @JvmInline
   value class PageId(val value: Long) {
      companion object {
         operator fun invoke(clock: Clock = Clock.System) = PageId(
            clock.now().toEpochMilliseconds()
         )
      }
   }

   constructor(id: Id, savedPageState: SavedPageState) : this(
      id, listOf(savedPageState)
   )

   /** このPageStackの一番上の[SavedPageState] */
   val head: SavedPageState get() = savedPageStates.last()

   internal val indexedHead: IndexedValue<SavedPageState> get() {
      val list = savedPageStates
      val idx = list.lastIndex
      return IndexedValue(idx, list[idx])
   }

   /**
    * @return
    * このPageStackの一番上の[Page]を取り除いたPageStack。
    * このPageStackにPageがひとつしかない場合はnull
    */
   fun tailOrNull(): PageStack? {
      val tailPages = savedPageStates.dropLast(1)
      return if (tailPages.isEmpty()) {
         null
      } else {
         PageStack(id, tailPages)
      }
   }

   fun added(savedPageState: SavedPageState) = PageStack(
      id, savedPageStates + savedPageState
   )
}

@Stable
class PageStackState internal constructor(
   val pageStackId: PageStackBoard.PageStackId,
   internal val pageStackCache: WritableCache<PageStack>,
   val pageStackBoardState: PageStackBoardState
) {
   internal val pageStack: PageStack by pageStackCache.asState()

   fun startPage(page: Page) {
      pageStackCache.update {
         it.added(
            PageStack.SavedPageState(
               PageStack.PageId(),
               page
            )
         )
      }
   }

   fun finishPage() {
      val tail = pageStackCache.value.tailOrNull()

      if (tail == null) {
         removeFromBoard()
         return
      }

      pageStackCache.value = tail
   }

   fun addColumn(pageStack: PageStack) {
      val board = pageStackBoardState.pageStackBoard

      val index = board.sequence().indexOfFirst { it.id == pageStackId }

      val insertionIndex = if (index < 0) {
         board.pageStackCount
      } else {
         board.rootRow.asSequence()
            .map { node ->
               when (node) {
                  is PageStackBoard.PageStack           -> 1
                  is PageStackBoard.LayoutElementParent -> node.leafCount
               }
            }
            .runningReduce { acc, leafCount -> acc + leafCount }
            .indexOfFirst { it > index }
            .plus(1)
      }

      pageStackBoardState.addColumn(insertionIndex, pageStack)
   }

   fun removeFromBoard() {
      pageStackBoardState.removePageStack(pageStackId)
   }
}

@OptIn(ExperimentalTransitionApi::class)
@Composable
internal fun PageStackContent(
   state: PageStackState,
   pageComposableSwitcher: PageComposableSwitcher,
   pageStateStore: PageStateStore
) {
   val pageStack = state.pageStack

   val transition = updateTransition(
      pageStack.indexedHead,
      label = "PageStackContentTransition"
   )

   val layoutInfoMap = remember {
      mutableMapOf<PageStack.PageId, MutablePageLayoutInfo>()
   }

   val visiblePages = remember(
      transition.currentState.index,
      transition.targetState .index
   ) {
      val (currentIndex, currentPage) = transition.currentState
      val (targetIndex,  targetPage ) = transition.targetState

      val visiblePages = when {
         currentIndex < targetIndex -> {
            val currentPageComposable = pageComposableSwitcher[currentPage.page] ?: TODO()
            val targetPageComposable  = pageComposableSwitcher[targetPage .page] ?: TODO()

            val transitionSpec
               =  currentPageComposable.pageTransitionSet.getTransitionTo  (targetPage .page::class)
               ?: targetPageComposable .pageTransitionSet.getTransitionFrom(currentPage.page::class)
               ?: defaultPageTransitionSpec

            listOf(
               Pair(currentPage, transitionSpec.enteringCurrentPageElementAnimations),
               Pair(targetPage,  transitionSpec.enteringTargetPageElementAnimations)
            )
         }
         currentIndex > targetIndex -> {
            val currentPageComposable = pageComposableSwitcher[currentPage.page] ?: TODO()
            val targetPageComposable  = pageComposableSwitcher[targetPage .page] ?: TODO()

            val transitionSpec
               =  targetPageComposable .pageTransitionSet.getTransitionFrom(currentPage.page::class)
               ?: currentPageComposable.pageTransitionSet.getTransitionTo  (targetPage .page::class)
               ?: defaultPageTransitionSpec

            listOf(
               Pair(targetPage,  transitionSpec.exitingTargetPageElementAnimations),
               Pair(currentPage, transitionSpec.exitingCurrentPageElementAnimations)
            )
         }
         else -> {
            listOf(
               Pair(targetPage, persistentMapOf())
            )
         }
      }

      val iter = layoutInfoMap.keys.iterator()
      while (iter.hasNext()) {
         val pageId = iter.next()

         if (visiblePages.none { it.first.id == pageId }) {
            iter.remove()
         }
      }

      visiblePages
   }

   val pageTransition: Transition<PageLayoutInfo>
         = transition.createChildTransition(label = "PageTransition") {
            layoutInfoMap.getOrInstantiate(it.value.id)
         }

   Box {
      val backgroundColor = MaterialTheme.colorScheme
         .surfaceColorAtElevation(LocalAbsoluteTonalElevation.current)

      for ((savedPageState, transitionAnimations) in visiblePages) {
         key(savedPageState.id) {
            val layoutInfo = layoutInfoMap.getOrInstantiate(savedPageState.id)

            CompositionLocalProvider(
               LocalPageLayoutInfo provides layoutInfo,
               LocalPageTransitionAnimations provides transitionAnimations,
               LocalPageTransition provides pageTransition,
            ) {
               Box(Modifier.transitionElement(PageLayoutIds.root)) {
                  Box(
                     Modifier
                        .fillMaxSize()
                        .background(backgroundColor)
                        .transitionElement(PageLayoutIds.background)
                  )

                  Box(Modifier.transitionElement(PageLayoutIds.content)) {
                     PageContent(
                        savedPageState,
                        pageComposableSwitcher,
                        pageStateStore,
                        pageStackState = state
                     )
                  }
               }
            }
         }
      }
   }
}

private fun MutableMap<PageStack.PageId, MutablePageLayoutInfo>
      .getOrInstantiate(pageId: PageStack.PageId): MutablePageLayoutInfo
{
   return getOrPut(pageId) { PageLayoutInfoImpl(pageId) }
}
