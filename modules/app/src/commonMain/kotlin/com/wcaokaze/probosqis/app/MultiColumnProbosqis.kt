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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wcaokaze.probosqis.app.pagedeck.MultiColumnPageDeck
import com.wcaokaze.probosqis.app.pagedeck.MultiColumnPageDeckState
import com.wcaokaze.probosqis.error.PErrorActionButton
import com.wcaokaze.probosqis.error.PErrorList
import com.wcaokaze.probosqis.error.PErrorListState
import com.wcaokaze.probosqis.ext.compose.layout.safeDrawing
import com.wcaokaze.probosqis.resources.Strings

@Composable
fun MultiColumnProbosqis(
   state: ProbosqisState,
   colorScheme: MultiColumnProbosqisColorScheme = rememberMultiColumnProbosqisColorScheme(),
   safeDrawingWindowInsets: WindowInsets = WindowInsets.safeDrawing
) {
   BoxWithConstraints(
      Modifier
         .background(colorScheme.background)
   ) {
      val errorListState = remember { PErrorListState() }

      val pageStackCount = (maxWidth / 330.dp).toInt().coerceAtLeast(1)

      Column {
         AppBar(
            safeDrawingWindowInsets,
            onErrorButtonClick = { errorListState.show() }
         )

         val pageDeckState = remember(state) {
            val pageDeckCache = state.loadPageDeckOrDefault()

            MultiColumnPageDeckState(
               pageDeckCache, state.pageStackRepository
            ).also { state.pageDeckState = it }
         }

         @OptIn(ExperimentalMaterial3Api::class)
         MultiColumnPageDeck(
            pageDeckState,
            state.pageComposableSwitcher,
            state.pageStateStore,
            pageStackCount,
            colorScheme.activePageStackAppBar,
            colorScheme.inactivePageStackAppBar,
            colorScheme.pageStackBackground,
            colorScheme.pageStackFooter,
            windowInsets = safeDrawingWindowInsets
               .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
            modifier = Modifier
               .fillMaxSize()
         )
      }

      PErrorList(errorListState, safeDrawingWindowInsets)
   }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppBar(
   safeDrawingWindowInsets: WindowInsets,
   onErrorButtonClick: () -> Unit
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
      actions = {
         PErrorActionButton(onClick = onErrorButtonClick)
      },
      colors = TopAppBarDefaults.topAppBarColors(
         containerColor = Color.Transparent
      ),
      windowInsets = safeDrawingWindowInsets
         .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
   )
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
