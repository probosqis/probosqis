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

package com.wcaokaze.probosqis.testpages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.wcaokaze.probosqis.capsiqum.page.PageId
import com.wcaokaze.probosqis.capsiqum.page.PageStack
import com.wcaokaze.probosqis.capsiqum.page.PageStateFactory
import com.wcaokaze.probosqis.capsiqum.page.SavedPageState
import com.wcaokaze.probosqis.page.PPage
import com.wcaokaze.probosqis.page.PPageComposable
import com.wcaokaze.probosqis.page.PPageState
import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("com.wcaokaze.probosqis.testpages.TestPage")
class TestPage(val i: Int) : PPage() {
   override fun clone() = this
}

@Stable
class TestPageState : PPageState()

val testPageComposable = PPageComposable<TestPage, TestPageState>(
   PageStateFactory { _, _, _ -> TestPageState() },
   content = { page, pageState, windowInsets ->
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
               pageState.addColumn(newPageStack)
            }
         ) {
            Text("Add Column")
         }

         Button(
            onClick = {
               pageState.startPage(
                  TestTimelinePage()
               )
            }
         ) {
            Text("Start Timeline")
         }

         Button(
            onClick = {
               val error = TestError(Clock.System.now())
               pageState.raiseError(error)
            }
         ) {
            Text("Raise an error")
         }
      }
   },
   header = { _, _ -> },
   footer = null,
   pageTransitions = {}
)
