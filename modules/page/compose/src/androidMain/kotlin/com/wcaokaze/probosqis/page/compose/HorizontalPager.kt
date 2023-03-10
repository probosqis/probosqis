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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager

import com.google.accompanist.pager.PagerState as AccompanistPagerState

@OptIn(ExperimentalPagerApi::class)
@Stable
actual class PagerState {
   internal val accompanist = AccompanistPagerState()

   actual val currentPage: Int
      get() = accompanist.currentPage

   actual suspend fun animateScrollToPage(page: Int) {
      accompanist.animateScrollToPage(page)
   }
}

@Composable
internal actual fun HorizontalPager(
   count: Int,
   state: PagerState,
   modifier: Modifier,
   content: @Composable (Int) -> Unit
) {
   @OptIn(ExperimentalPagerApi::class)
   HorizontalPager(
      count,
      state = state.accompanist,
      modifier = modifier
   ) { page ->
      content(page)
   }
}
