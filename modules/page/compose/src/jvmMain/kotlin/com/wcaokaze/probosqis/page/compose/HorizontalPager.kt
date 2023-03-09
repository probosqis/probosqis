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

import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier

@Stable
actual class PagerState {
   internal val lazyListState = LazyListState()

   actual val currentPage: Int
      get() {
         val layoutInfo = lazyListState.layoutInfo
         return layoutInfo.visibleItemsInfo
            .maxByOrNull {
               val start = maxOf(it.offset, 0)
               val end = minOf(
                  it.offset + it.size,
                  layoutInfo.viewportEndOffset - layoutInfo.afterContentPadding
               )
               end - start
            }
            ?.index ?: 0
      }

   actual suspend fun animateScrollToPage(page: Int) {
      lazyListState.animateScrollToItem(page)
   }
}

@Composable
internal actual fun HorizontalPager(
   count: Int,
   state: PagerState,
   modifier: Modifier,
   content: @Composable (Int) -> Unit
) {
   Column(modifier) {
      LazyRow(
         state = state.lazyListState,
         modifier = Modifier
            .fillMaxWidth()
            .weight(1.0f)
      ) {
         items(count) { index ->
            Box(Modifier.fillParentMaxSize()) {
               content(index)
            }
         }
      }

      HorizontalScrollbar(
         rememberScrollbarAdapter(state.lazyListState)
      )
   }
}
