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

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.wcaokaze.probosqis.cache.core.WritableCache
import com.wcaokaze.probosqis.page.compose.PageComposableSwitcher
import com.wcaokaze.probosqis.page.compose.pageComposable
import com.wcaokaze.probosqis.page.core.Column
import com.wcaokaze.probosqis.page.core.ColumnBoard
import com.wcaokaze.probosqis.page.perpetuation.ColumnBoardRepository
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Preview
@Composable
private fun ProbosqisPreview() {
   val di = remember {
      object : DI {
         private val clock = object : Clock {
            override fun now() = Instant.parse("2000-01-01T00:00:00.000Z")
         }

         override val pageComposableSwitcher = PageComposableSwitcher(
            allPageComposables = persistentListOf(
               pageComposable<TestPage> { page, columnState -> TestPage(page, columnState) },
            )
         )

         override val columnBoardRepository = object : ColumnBoardRepository {
            override fun saveColumnBoard(columnBoard: ColumnBoard)
                  = throw NotImplementedError()

            override fun loadColumnBoard() = WritableCache(
               ColumnBoard(
                  columns = persistentListOf(
                     Column(TestPage(0), clock),
                  )
               )
            )
         }
      }
   }

   Probosqis(di)
}
