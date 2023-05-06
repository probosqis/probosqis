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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.wcaokaze.probosqis.page.Column
import com.wcaokaze.probosqis.page.ColumnState
import com.wcaokaze.probosqis.page.Page
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("com.wcaokaze.probosqis.app.TestPage")
class TestPage(val i: Int) : Page()

@Composable
fun TestPage(page: TestPage, columnState: ColumnState) {
   Column(Modifier.fillMaxSize()) {
      Text(
         "${page.i}",
         fontSize = 48.sp
      )

      val coroutineScope = rememberCoroutineScope()

      Button(
         onClick = {
            coroutineScope.launch {
               val newColumn = Column(TestPage(page.i + 1))
               columnState.addColumn(newColumn)
            }
         }
      ) {
         Text("Add Column")
      }
   }
}
