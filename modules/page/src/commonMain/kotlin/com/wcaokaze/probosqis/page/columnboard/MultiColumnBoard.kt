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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MultiColumnBoard(
   columnCount: Int,
   windowInsets: WindowInsets,
   onTopAppBarHeightChanged: (Dp) -> Unit,
   modifier: Modifier = Modifier
) {
   Row(
      modifier
         .windowInsetsPadding(
            windowInsets.only(WindowInsetsSides.Horizontal)
         )
         .padding(horizontal = 8.dp)
   ) {
      repeat (columnCount) { i ->
         MultiColumnColumn(
            isActive = columnCount == 1 || i == 1,
            windowInsets.only(WindowInsetsSides.Bottom),
            onTopAppBarHeightChanged,
            modifier = Modifier
               .weight(1.0f)
               .padding(horizontal = 8.dp)
         )
      }
   }
}

@Composable
private fun MultiColumnColumn(
   isActive: Boolean,
   windowInsets: WindowInsets,
   onTopAppBarHeightChanged: (Dp) -> Unit,
   modifier: Modifier = Modifier
) {
   Surface(
      shape = MaterialTheme.shapes.large,
      tonalElevation = if (isActive) { 3.dp } else { 1.dp },
      shadowElevation = if (isActive) { 4.dp } else { 2.dp },
      modifier = modifier
   ) {
      val density by rememberUpdatedState(LocalDensity.current)

      Column {
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
            windowInsets = WindowInsets(0, 0, 0, 0),
            colors = TopAppBarDefaults.smallTopAppBarColors(
               containerColor = if (isActive) {
                  MaterialTheme.colorScheme.primaryContainer
                  MaterialTheme.colorScheme
                     .surfaceTint.copy(alpha = 0.13f)
                     .compositeOver(MaterialTheme.colorScheme.primaryContainer)
               } else {
                  MaterialTheme.colorScheme
                     .surfaceColorAtElevation(4.dp)
               }
            ),
            modifier = Modifier
               .onSizeChanged {
                  val heightPx = it.height
                  val heightDp = with (density) { heightPx.toDp() }
                  onTopAppBarHeightChanged(heightDp)
               }
         )

         DummyPage(
            windowInsets,
            modifier = Modifier.fillMaxSize()
         )
      }
   }
}

@Composable
private fun DummyPage(
   windowInsets: WindowInsets,
   modifier: Modifier = Modifier
) {
   LazyColumn(
      contentPadding = windowInsets.asPaddingValues(),
      modifier = modifier
   ) {
      items(42) { i ->
         Box(
            Modifier
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
