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

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.wcaokaze.probosqis.cache.compose.asMutableState
import com.wcaokaze.probosqis.cache.core.WritableCache
import com.wcaokaze.probosqis.page.core.ColumnBoard

@Stable
class ColumnBoardState(
   columnBoardCache: WritableCache<ColumnBoard>,
   allMetadata: List<PageMetadata<*>>
) {
   internal var columnBoard by columnBoardCache.asMutableState()
   internal val metadataCollection = PageMetadataCollection(allMetadata)
}

@Composable
fun ColumnBoard(
   state: ColumnBoardState,
   modifier: Modifier = Modifier
) {
   val columnBoard = state.columnBoard

   HorizontalPager(
      columnBoard.columnCount,
      modifier
   ) { index ->
      val column = columnBoard[index]
      Column(column, state.metadataCollection)
   }
}
