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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.wcaokaze.probosqis.cache.core.WritableCache
import com.wcaokaze.probosqis.ext.kotlin.datetime.MockClock

@Preview
@Composable
private fun PagePreview() {
   val pageComposableSwitcher = remember {
      PageComposableSwitcher(
         listOf(
            pageComposable<PreviewPage>(
               content = { page, _ -> PreviewPage(page) },
               header = { _, _ -> },
               footer = null
            ),
         )
      )
   }

   val page = remember { PreviewPage("PreviewPage") }
   val column = remember { Column(page, MockClock()) }
   val columnBoardState = remember {
      val columnBoard = ColumnBoard(listOf(column))
      ColumnBoardState(
         WritableCache(columnBoard)
      )
   }
   val columnState = remember { ColumnState(column, columnBoardState) }

   PageContent(page, pageComposableSwitcher, columnState)
}
