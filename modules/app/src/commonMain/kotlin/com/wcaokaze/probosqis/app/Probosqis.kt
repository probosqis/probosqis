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

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.wcaokaze.probosqis.page.compose.ColumnBoard
import com.wcaokaze.probosqis.page.compose.ColumnBoardState
import com.wcaokaze.probosqis.page.core.Column
import com.wcaokaze.probosqis.page.core.ColumnBoard
import kotlinx.collections.immutable.persistentListOf

@Composable
fun Probosqis(di: DI) {
   val columnBoardState = remember {
      val columnBoardRepository = di.columnBoardRepository
      val columnBoardCache = try {
         columnBoardRepository.loadColumnBoard()
      } catch (e: Exception) {
         val columnBoard = ColumnBoard(
            columns = createDefaultColumns()
         )
         columnBoardRepository.saveColumnBoard(columnBoard)
      }

      ColumnBoardState(columnBoardCache, di.allPageMetadata)
   }

   ColumnBoard(
      columnBoardState,
      modifier = Modifier.fillMaxSize()
   )
}

private fun createDefaultColumns(): List<Column> {
   return persistentListOf(
      Column(TestPage()),
      Column(TestPage()),
   )
}
