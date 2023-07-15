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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.wcaokaze.probosqis.cache.compose.asMutableState
import com.wcaokaze.probosqis.cache.core.WritableCache
import kotlinx.serialization.Serializable

@Serializable
class PageStackBoard(private val pageStacks: List<PageStack>) {
   val pageStackCount: Int
      get() = pageStacks.size

   operator fun get(index: Int): PageStack = pageStacks[index]

   fun indexOf(pageStack: PageStack): Int
      = pageStacks.indexOfFirst { it === pageStack }

   fun inserted(index: Int, pageStack: PageStack): PageStackBoard {
      return PageStackBoard(
         buildList {
            addAll(pageStacks)
            add(index, pageStack)
         }
      )
   }

   fun removed(index: Int): PageStackBoard {
      return PageStackBoard(
         buildList {
            addAll(pageStacks)
            removeAt(index)
         }
      )
   }
}

@Stable
class PageStackBoardState(pageStackBoardCache: WritableCache<PageStackBoard>) {
   internal var pageStackBoard by pageStackBoardCache.asMutableState()

   internal val pagerState = PagerState()

   /**
    * ユーザーが最後に操作したPageStack。
    *
    * PageStackBoardのスクロールによってcurrentPageStackが画面外に出るとき、
    * そのカラムに一番近い画面内のPageStackが新たなcurrentPageStackとなる。
    *
    * であるべきだが、ひとまず今はタブレットUIに対応するまでの間は
    * 常に画面内に1カラムしか表示されないので
    * 画面内にあるカラムがcurrentPageStackとなる
    */
   val currentPageStack: Int
      get() = pagerState.currentPage

   suspend fun animateScrollTo(pageStack: Int) {
      pagerState.animateScrollToPage(pageStack)
   }

   suspend fun addPageStack(index: Int, pageStack: PageStack) {
      pageStackBoard = pageStackBoard.inserted(index, pageStack)
   }
}

@Composable
fun PageStackBoard(
   state: PageStackBoardState,
   pageComposableSwitcher: PageComposableSwitcher,
   modifier: Modifier = Modifier
) {
   val pageStackBoard = state.pageStackBoard

   Column(modifier) {
      HorizontalPager(
         pageStackBoard.pageStackCount,
         state.pagerState,
         key = { pageStackBoard[it].id },
         modifier = Modifier
            .fillMaxWidth()
            .weight(1.0f)
      ) { index ->
         val pageStack = pageStackBoard[index]
         Box(
            Modifier.background(
               if (state.currentPageStack == index) { Color.Cyan } else { Color.White }
            )
         ) {
            val pageStackState = remember {
               PageStackState(pageStack, pageStackBoardState = state)
            }
            PageStackContent(pageStackState, pageComposableSwitcher)
         }
      }

      PageStackBoardScrollBar(state.pagerState)
   }
}
