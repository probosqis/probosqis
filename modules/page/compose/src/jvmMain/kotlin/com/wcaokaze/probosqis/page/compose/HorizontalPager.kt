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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal actual fun HorizontalPager(
   count: Int,
   modifier: Modifier,
   content: @Composable (Int) -> Unit
) {
   Column(modifier) {
      val lazyListState = rememberLazyListState()

      LazyRow(
         state = lazyListState,
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
         rememberScrollbarAdapter(lazyListState)
      )
   }
}
