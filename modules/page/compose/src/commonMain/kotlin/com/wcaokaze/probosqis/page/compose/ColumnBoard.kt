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

package com.wcaokaze.probosqis.page.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.wcaokaze.probosqis.cache.compose.asMutableState
import com.wcaokaze.probosqis.cache.core.WritableCache
import com.wcaokaze.probosqis.page.core.ColumnBoard

@Stable
class ColumnBoardState(
   columnBoardCache: WritableCache<ColumnBoard>,
   allMetadata: List<PageComposable<*>>
) {
   internal var columnBoard by columnBoardCache.asMutableState()
   internal val metadataCollection = PageMetadataCollection(allMetadata)

   internal val pagerState = PagerState()

   /**
    * ユーザーが最後に操作したカラム。
    *
    * ColumnBoardのスクロールによってcurrentColumnが画面外に出るとき、
    * そのカラムに一番近い画面内のカラムが新たなcurrentColumnとなる。
    *
    * であるべきだが、ひとまず今はタブレットUIに対応するまでの間は
    * 常に画面内に1カラムしか表示されないので
    * 画面内にあるカラムがcurrentColumnとなる
    */
   val currentColumn: Int
      get() = pagerState.currentPage

   suspend fun animateScrollTo(column: Int) {
      pagerState.animateScrollToPage(column)
   }
}

@Composable
fun ColumnBoard(
   state: ColumnBoardState,
   modifier: Modifier = Modifier
) {
   val columnBoard = state.columnBoard

   HorizontalPager(
      columnBoard.columnCount,
      state.pagerState,
      modifier
   ) { index ->
      val column = columnBoard[index]
      Box(
         Modifier.background(
            if (state.currentColumn == index) { Color.Cyan } else { Color.White }
         )
      ) {
         Column(column, state.metadataCollection)
      }
   }
}
