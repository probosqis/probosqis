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

package com.wcaokaze.probosqis.page.pagestackboard

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
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import com.wcaokaze.probosqis.cache.core.WritableCache
import com.wcaokaze.probosqis.ext.compose.layout.safeDrawing
import com.wcaokaze.probosqis.page.PageStackContent
import com.wcaokaze.probosqis.page.PageStackState
import com.wcaokaze.probosqis.page.PageComposableSwitcher

@Stable
class SingleColumnPageStackBoardState(
   pageStackBoardCache: WritableCache<PageStackBoard>
) : PageStackBoardState(pageStackBoardCache) {
   override suspend fun animateScrollTo(index: Int) {
      TODO()
   }
}

@Composable
fun SingleColumnPageStackBoardAppBar(
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
fun SingleColumnPageStackBoard(
   state: SingleColumnPageStackBoardState,
   pageComposableSwitcher: PageComposableSwitcher,
   windowInsets: WindowInsets,
   modifier: Modifier = Modifier,
) {
   Layout(
      content = {
         val pageStackState by remember(state) {
            derivedStateOf {
               val pageStack = state.pageStackBoard.rootRow[0]
                     as PageStackBoard.PageStack
               PageStackState(pageStack.cache, state)
            }
         }

         PageStackContent(
            pageStackState,
            pageComposableSwitcher
         )
      },
      measurePolicy = rememberSingleColumnPageStackBoardMeasurePolicy(),
      modifier = modifier
         .scrollable(rememberScrollState(), Orientation.Vertical)
   )
}

@Composable
private fun rememberSingleColumnPageStackBoardMeasurePolicy() = remember {
   MeasurePolicy { measurables, constraints ->
      val pageStackBoardWidth = constraints.maxWidth
      val pageStackBoardHeight = constraints.maxHeight

      val pageStackConstraints = Constraints
         .fixed(pageStackBoardWidth, pageStackBoardHeight)

      val placeables = measurables.map { it.measure(pageStackConstraints) }

      layout(pageStackBoardWidth, pageStackBoardHeight) {
         var x = 0

         for (placeable in placeables) {
            placeable.placeRelative(x, 0)
            x += placeable.width
         }
      }
   }
}
