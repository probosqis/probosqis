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
import androidx.compose.ui.tooling.preview.Preview
import com.wcaokaze.probosqis.cache.core.WritableCache
import com.wcaokaze.probosqis.page.core.Column
import com.wcaokaze.probosqis.page.core.ColumnBoard

@Preview
@Composable
fun ColumnBoardPreview() {
   val columnBoard = ColumnBoard(
      List(3) { i ->
         Column(PreviewPage("$i - 0"))
            .added(PreviewPage("$i - 1"))
      }
   )
   val pageComposableSwitcher = PageComposableSwitcher(
      allPageComposables = listOf(
         pageComposable<PreviewPage> { PreviewPage(it) },
      )
   )

   val columnBoardState = ColumnBoardState(
      WritableCache(columnBoard),
      pageComposableSwitcher
   )

   ColumnBoard(columnBoardState)
}
