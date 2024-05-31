/*
 * Copyright 2023-2024 wcaokaze
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.wcaokaze.probosqis.capsiqum.page.Page
import com.wcaokaze.probosqis.capsiqum.page.PageId
import com.wcaokaze.probosqis.capsiqum.page.PageStack
import com.wcaokaze.probosqis.capsiqum.page.PageState
import com.wcaokaze.probosqis.capsiqum.page.PageStateFactory
import com.wcaokaze.probosqis.capsiqum.page.SavedPageState
import com.wcaokaze.probosqis.page.CombinedPageComposable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("com.wcaokaze.probosqis.app.TestPage")
class TestPage(val i: Int) : Page()

@Stable
class TestPageState : PageState()

val testPageComposable = CombinedPageComposable<TestPage, TestPageState>(
   PageStateFactory { _, _ -> TestPageState() },
   content = { page, _, pageStackState, windowInsets ->
      Column(
         Modifier
            .fillMaxSize()
            .windowInsetsPadding(windowInsets)
      ) {
         Text(
            "${page.i}",
            fontSize = 48.sp
         )

         Button(
            onClick = {
               val newPageStack = PageStack(
                  SavedPageState(
                     PageId(),
                     TestPage(page.i + 1)
                  )
               )
               pageStackState.addColumn(newPageStack)
            }
         ) {
            Text("Add Column")
         }

         Button(
            onClick = {
               pageStackState.startPage(
                  TestTimelinePage()
               )
            }
         ) {
            Text("Start Timeline")
         }
      }
   },
   header = { _, _, _ -> },
   footer = null,
   pageTransitions = {}
)
