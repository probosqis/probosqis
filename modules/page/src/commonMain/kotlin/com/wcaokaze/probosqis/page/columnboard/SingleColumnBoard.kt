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

package com.wcaokaze.probosqis.page.columnboard

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wcaokaze.probosqis.ext.compose.layout.safeDrawing

@Composable
fun SingleColumnBoardAppBar(
   modifier: Modifier = Modifier,
   safeDrawingWindowInsets: WindowInsets = WindowInsets.safeDrawing
) {
   Row(modifier) {
      @OptIn(ExperimentalMaterial3Api::class)
      TopAppBar(
         title = { Text("Home") },
         navigationIcon = {
            IconButton(
               onClick = {}
            ) {
               Icon(Icons.Default.Close, contentDescription = "Close")
            }
         },
         windowInsets = safeDrawingWindowInsets.only(WindowInsetsSides.Horizontal),
         colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = Color.Transparent
         ),
         modifier = Modifier
      )
   }
}

@Composable
fun SingleColumnBoard(
   modifier: Modifier = Modifier,
   safeDrawingWindowInsets: WindowInsets = WindowInsets.safeDrawing
) {
   Row(
      modifier
         .scrollable(rememberScrollState(), Orientation.Vertical)
   ) {
      LazyColumn(
         contentPadding = safeDrawingWindowInsets
            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
            .asPaddingValues(),
         modifier = Modifier
            .fillMaxSize()
            .background(Color.Cyan)
      ) {
         items(42) { i ->
            Box(Modifier
               .fillMaxWidth()
               .height(48.dp)
               .padding(horizontal = 16.dp)
            ) {
               Text(
                  "$i",
                  fontSize = 20.sp,
                  modifier = Modifier.align(Alignment.CenterStart)
               )
            }
         }
      }
   }
}
