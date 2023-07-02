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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wcaokaze.probosqis.ext.compose.layout.safeDrawing
import com.wcaokaze.probosqis.page.PageStackBoardState
import com.wcaokaze.probosqis.page.pagestackboard.MultiColumnPageStackBoard
import com.wcaokaze.probosqis.resources.Strings

@Composable
internal fun MultiColumnProbosqis(
   di: DI,
   safeDrawingWindowInsets: WindowInsets = WindowInsets.safeDrawing
) {
   BoxWithConstraints(
      Modifier
         .background(MaterialTheme.colorScheme.background)
   ) {
      val density = LocalDensity.current
      var appBarHeight by remember(density, safeDrawingWindowInsets) {
         val initialHeight = with (density) {
            safeDrawingWindowInsets.getTop(density).toDp() + 64.dp
         }
         mutableStateOf(initialHeight)
      }
      var pageStackTopAppBarHeight by remember { mutableStateOf(64.dp) }

      AppBar(
         safeDrawingWindowInsets,
         pageStackTopAppBarHeight,
         onHeightChanged = { appBarHeight = it }
      )

      val pageStackBoardState = remember(di) {
         PageStackBoardState(
            pageStackBoardCache = loadPageStackBoardOrDefault(di.pageStackBoardRepository)
         )
      }

      val pageComposableSwitcher = remember(di) {
         di.pageComposableSwitcher
      }

      MultiColumnPageStackBoard(
         pageStackBoardState,
         pageComposableSwitcher,
         pageStackCount = (maxWidth / 330.dp).toInt(),
         windowInsets = safeDrawingWindowInsets
            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
         onTopAppBarHeightChanged = { pageStackTopAppBarHeight = it },
         modifier = Modifier
            .padding(top = appBarHeight)
      )
   }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppBar(
   safeDrawingWindowInsets: WindowInsets,
   pageStackTopAppBarHeight: Dp,
   onHeightChanged: (Dp) -> Unit
) {
   val density by rememberUpdatedState(LocalDensity.current)

   Column(
      Modifier
         .background(MaterialTheme.colorScheme.primaryContainer)
   ) {
      TopAppBar(
         title = {
            Text(
               text = Strings.App.topAppBar,
               maxLines = 1,
               overflow = TextOverflow.Ellipsis
            )
         },
         navigationIcon = {
            MenuButton(
               onClick = {}
            )
         },
         colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = Color.Transparent
         ),
         windowInsets = safeDrawingWindowInsets
            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
         modifier = Modifier
            .onSizeChanged {
               val heightPx = it.height
               val heightDp = with (density) { heightPx.toDp() }
               onHeightChanged(heightDp)
            }
      )

      Spacer(Modifier.height(pageStackTopAppBarHeight))
   }
}

@Composable
private fun MenuButton(
   onClick: () -> Unit
) {
   IconButton(onClick) {
      Icon(
         Icons.Default.Menu,
         contentDescription = Strings.App.topAppBarNavigationContentDescription
      )
   }
}
