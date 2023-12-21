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

package com.wcaokaze.probosqis.app

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.dp
import com.wcaokaze.probosqis.cache.core.WritableCache
import com.wcaokaze.probosqis.ext.compose.layout.safeDrawing
import com.wcaokaze.probosqis.page.PageComposable
import com.wcaokaze.probosqis.page.PageComposableSwitcher
import com.wcaokaze.probosqis.page.PageStack
import com.wcaokaze.probosqis.page.PageStackBoard
import com.wcaokaze.probosqis.page.PageStackBoardRepository
import com.wcaokaze.probosqis.page.PageStackBoardState
import com.wcaokaze.probosqis.page.PageStackRepository
import com.wcaokaze.probosqis.page.PageStateStore
import com.wcaokaze.probosqis.resources.ProbosqisTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope

@Stable
class ProbosqisState(
   allPageComposables: List<PageComposable<*, *>>,
   val pageStackBoardRepository: PageStackBoardRepository,
   val pageStackRepository: PageStackRepository,
   coroutineScope: CoroutineScope
) {
   val pageComposableSwitcher = PageComposableSwitcher(allPageComposables)
   val pageStateStore = PageStateStore(
      allPageComposables.map { it.pageStateFactory },
      coroutineScope
   )

   private var _pageStackBoardState: PageStackBoardState? = null
   var pageStackBoardState: PageStackBoardState
      get() {
         return _pageStackBoardState ?: throw IllegalStateException(
            "attempt to get pageStackBoardState before the first Composition")
      }
      internal set(value) {
         _pageStackBoardState = value
      }

   internal fun loadPageStackBoardOrDefault(): WritableCache<PageStackBoard> {
      return try {
         pageStackBoardRepository.loadPageStackBoard()
      } catch (e: Exception) {
         pageStackRepository.deleteAllPageStacks()

         val rootRow = PageStackBoard.Row(
            createDefaultPageStacks(pageStackRepository)
         )
         val pageStackBoard = PageStackBoard(rootRow)
         pageStackBoardRepository.savePageStackBoard(pageStackBoard)
      }
   }

   private fun createDefaultPageStacks(
      pageStackRepository: PageStackRepository
   ): ImmutableList<PageStackBoard.LayoutElement> {
      return sequenceOf(
            PageStack(
               PageStack.Id(0L),
               PageStack.SavedPageState(
                  PageStack.PageId(0L),
                  TestPage(0)
               )
            ),
            PageStack(
               PageStack.Id(1L),
               PageStack.SavedPageState(
                  PageStack.PageId(1L),
                  TestPage(1)
               )
            ),
         )
         .map { pageStackRepository.savePageStack(it) }
         .map { pageStackCache ->
            PageStackBoard.PageStack(
               PageStackBoard.PageStackId(pageStackCache.value.id.value),
               pageStackCache
            )
         }
         .toImmutableList()
   }
}

@Composable
fun Probosqis(
   state: ProbosqisState,
   safeDrawingWindowInsets: WindowInsets = WindowInsets.safeDrawing
) {
   ProbosqisTheme {
      MaterialTheme(colorScheme()) {
         BoxWithConstraints {
            if (maxWidth < 512.dp) {
               SingleColumnProbosqis(state, safeDrawingWindowInsets)
            } else {
               MultiColumnProbosqis(state, safeDrawingWindowInsets)
            }
         }
      }
   }
}

@Composable
expect fun colorScheme(): ColorScheme
