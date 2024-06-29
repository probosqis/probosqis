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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wcaokaze.probosqis.capsiqum.page.PageStateFactory
import com.wcaokaze.probosqis.capsiqum.transition.PageLayoutInfo
import com.wcaokaze.probosqis.capsiqum.transition.transitionElement
import com.wcaokaze.probosqis.page.PPage
import com.wcaokaze.probosqis.page.PPageComposable
import com.wcaokaze.probosqis.page.PPageState
import com.wcaokaze.probosqis.page.PageLayoutIds
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("com.wcaokaze.probosqis.testpages.TestNotePage")
class TestNotePage(val i: Int) : PPage() {
   override fun clone() = this
}

@Stable
class TestNotePageState : PPageState()

val testNotePageComposable = PPageComposable<TestNotePage, TestNotePageState>(
   PageStateFactory { _, _, _ -> TestNotePageState() },
   content = { page, _, windowInsets ->
      Note(page.i, windowInsets, Modifier.fillMaxSize())
   },
   header = { _, _, ->
      Text(
         "Note",
         maxLines = 1,
         overflow = TextOverflow.Ellipsis
      )
   },
   footer = { _, _, -> },
   pageTransitions = {}
)

object TestNotePageLayoutIds : PageLayoutIds() {
   val account           = PageLayoutInfo.LayoutId()
   val accountIcon       = PageLayoutInfo.LayoutId()
   val accountNameColumn = PageLayoutInfo.LayoutId()
   val accountName       = PageLayoutInfo.LayoutId()
   val accountScreenName = PageLayoutInfo.LayoutId()
   val contentText       = PageLayoutInfo.LayoutId()
}

@Composable
private fun Note(
   i: Int,
   windowInsets: WindowInsets,
   modifier: Modifier = Modifier
) {
   Column(
      modifier = modifier
         .windowInsetsPadding(windowInsets)
         .verticalScroll(rememberScrollState())
   ) {
      Account(
         i,
         Modifier
            .fillMaxWidth()
            .transitionElement(TestNotePageLayoutIds.account)
      )

      Column(
         Modifier
            .transitionElement(TestNotePageLayoutIds.contentText)
      ) {
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
            .transitionElement(TestNotePageLayoutIds.accountIcon)
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

      Column(
         Modifier
            .padding(horizontal = 8.dp)
            .transitionElement(TestNotePageLayoutIds.accountNameColumn)
      ) {
         Box(
            Modifier
               .padding(vertical = 8.dp)
               .transitionElement(TestNotePageLayoutIds.accountName)
               .fillMaxWidth()
               .height(16.dp)
               .background(Color.Gray)
         )

         Box(
            Modifier
               .padding(vertical = 8.dp)
               .transitionElement(TestNotePageLayoutIds.accountScreenName)
               .fillMaxWidth()
               .height(16.dp)
               .background(Color.Gray)
         )
      }
   }
}
