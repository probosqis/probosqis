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
import com.wcaokaze.probosqis.page.core.Column
import com.wcaokaze.probosqis.page.core.Page
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Stable
class ColumnState internal constructor(
   internal val column: Column,
   private val columnBoardState: ColumnBoardState
) {
   suspend fun addColumn(page: Page) {
      val createdTime = Clock.System.now()
         .toLocalDateTime(TimeZone.currentSystemDefault())
      val column = Column(page, createdTime)
      addColumn(column)
   }

   suspend fun addColumn(column: Column) {
      val index = columnBoardState.columnBoard.indexOf(this.column)
      columnBoardState.addColumn(index + 1, column)
   }
}

@Composable
internal fun Column(
   state: ColumnState,
   pageComposableSwitcher: PageComposableSwitcher
) {
   Page(
      page = state.column.head,
      pageComposableSwitcher,
      columnState = state
   )
}
