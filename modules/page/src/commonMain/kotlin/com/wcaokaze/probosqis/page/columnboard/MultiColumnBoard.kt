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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wcaokaze.probosqis.page.ColumnBoardState
import com.wcaokaze.probosqis.page.ColumnContent
import com.wcaokaze.probosqis.page.ColumnState
import com.wcaokaze.probosqis.page.PageComposableSwitcher

@Composable
fun MultiColumnBoard(
   state: ColumnBoardState,
   pageComposableSwitcher: PageComposableSwitcher,
   columnCount: Int,
   windowInsets: WindowInsets,
   modifier: Modifier = Modifier,
   onTopAppBarHeightChanged: (Dp) -> Unit = {},
) {
   Layout(
      content = {
         repeat (columnCount) { index ->
            val columnState = remember {
               val column = state.columnBoard[index]
               ColumnState(column, state)
            }

            MultiColumnColumn(
               columnState,
               isActive = columnCount == 1 || index == 1,
               windowInsets.only(WindowInsetsSides.Bottom),
               pageComposableSwitcher,
               onTopAppBarHeightChanged,
            )
         }
      },
      measurePolicy = rememberMultiColumnBoardMeasurePolicy(columnCount),
      modifier = modifier
   )
}

@Composable
private fun rememberMultiColumnBoardMeasurePolicy(
   columnCount: Int
) = remember(columnCount) {
   MeasurePolicy { measurables, constraints ->
      val columnBoardWidth = constraints.maxWidth
      val columnBoardHeight = constraints.maxHeight

      val columnConstraints = Constraints.fixed(
         columnBoardWidth / columnCount,
         columnBoardHeight
      )

      val placeables = measurables.map { it.measure(columnConstraints) }

      layout(constraints.minWidth, constraints.minHeight) {
         var x = 0

         for (placeable in placeables) {
            placeable.placeRelative(x, 0)
            x += placeable.width
         }
      }
   }
}

@Composable
private fun MultiColumnColumn(
   state: ColumnState,
   isActive: Boolean,
   windowInsets: WindowInsets,
   pageComposableSwitcher: PageComposableSwitcher,
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

         ColumnContent(
            state,
            pageComposableSwitcher
         )
      }
   }
}
