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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wcaokaze.probosqis.page.Page
import com.wcaokaze.probosqis.page.PageState
import com.wcaokaze.probosqis.page.pageComposable
import com.wcaokaze.probosqis.page.pageStateFactory
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("com.wcaokaze.probosqis.app.TestNotePage")
class TestNotePage(val i: Int) : Page()

@Stable
class TestNotePageState : PageState()

val testNotePageComposable = pageComposable<TestNotePage, TestNotePageState>(
   pageStateFactory { _, _ -> TestNotePageState() },
   content = { page, _, _ ->
      Note(page.i, Modifier.fillMaxSize())
   },
   header = { _, _, _ -> },
   footer = null
)

@Composable
private fun Note(
   i: Int,
   modifier: Modifier = Modifier
) {
   Column(modifier) {
      Account(i, Modifier.fillMaxWidth())

      Box(
         Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .fillMaxWidth()
            .height(12.dp)
            .background(Color.Gray)
      )

      Box(
         Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .fillMaxWidth()
            .height(12.dp)
            .background(Color.Gray)
      )
   }
}

@Composable
private fun Account(
   i: Int,
   modifier: Modifier = Modifier
) {
   Row(modifier.padding(8.dp)) {
      Box(
         modifier = Modifier
            .padding(8.dp)
            .size(56.dp)
            .background(Color.Gray)
      ) {
         Text(
            "$i",
            color = Color.White,
            fontSize = 32.sp,
            modifier = Modifier.align(Alignment.Center)
         )
      }

      Column(Modifier.padding(horizontal = 8.dp)) {
         Box(
            Modifier
               .padding(vertical = 8.dp)
               .fillMaxWidth()
               .height(16.dp)
               .background(Color.Gray)
         )

         Box(
            Modifier
               .padding(vertical = 8.dp)
               .fillMaxWidth()
               .height(16.dp)
               .background(Color.Gray)
         )
      }
   }
}
