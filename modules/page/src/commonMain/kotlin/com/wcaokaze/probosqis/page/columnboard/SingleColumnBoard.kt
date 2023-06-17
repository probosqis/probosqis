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

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import com.wcaokaze.probosqis.ext.compose.layout.safeDrawing
import com.wcaokaze.probosqis.page.ColumnBoardState
import com.wcaokaze.probosqis.page.ColumnContent
import com.wcaokaze.probosqis.page.ColumnState
import com.wcaokaze.probosqis.page.PageComposableSwitcher

@Composable
fun SingleColumnBoardAppBar(
   modifier: Modifier = Modifier,
   safeDrawingWindowInsets: WindowInsets = WindowInsets.safeDrawing
) {
   Row(modifier) {
      @OptIn(ExperimentalMaterial3Api::class)
      TopAppBar(
         title = {
            Text(
               "Home",
               maxLines = 1,
               overflow = TextOverflow.Ellipsis
            )
         },
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
   state: ColumnBoardState,
   pageComposableSwitcher: PageComposableSwitcher,
   windowInsets: WindowInsets,
   modifier: Modifier = Modifier,
) {
   Layout(
      content = {
         val columnState by remember(state) {
            derivedStateOf {
               val column = state.columnBoard[0]
               ColumnState(column, state)
            }
         }

         ColumnContent(
            columnState,
            pageComposableSwitcher
         )
      },
      measurePolicy = rememberSingleColumnBoardMeasurePolicy(),
      modifier = modifier
         .scrollable(rememberScrollState(), Orientation.Vertical)
   )
}

@Composable
private fun rememberSingleColumnBoardMeasurePolicy() = remember {
   MeasurePolicy { measurables, constraints ->
      val columnBoardWidth = constraints.maxWidth
      val columnBoardHeight = constraints.maxHeight

      val columnConstraints = Constraints
         .fixed(columnBoardWidth, columnBoardHeight)

      val placeables = measurables.map { it.measure(columnConstraints) }

      layout(columnBoardWidth, columnBoardHeight) {
         var x = 0

         for (placeable in placeables) {
            placeable.placeRelative(x, 0)
            x += placeable.width
         }
      }
   }
}
