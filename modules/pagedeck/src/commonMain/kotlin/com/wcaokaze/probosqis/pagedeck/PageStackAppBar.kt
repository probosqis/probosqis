/*
 * Copyright 2024 wcaokaze
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

package com.wcaokaze.probosqis.pagedeck

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wcaokaze.probosqis.capsiqum.page.Page
import com.wcaokaze.probosqis.capsiqum.page.PageComposable
import com.wcaokaze.probosqis.capsiqum.page.PageState
import com.wcaokaze.probosqis.capsiqum.page.PageSwitcher
import com.wcaokaze.probosqis.resources.Strings
import kotlinx.collections.immutable.toImmutableList

@ExperimentalMaterial3Api
private fun <P : Page, S : PageState> extractPageComposable(
   combined: CombinedPageComposable<P, S>,
   pageStackState: State<PPageStackState>,
   colors: State<TopAppBarColors>,
   windowInsets: State<WindowInsets>,
   horizontalContentPadding: State<Dp>
): PageComposable<P, S> {
   return PageComposable(
      combined.pageClass,
      combined.pageStateClass,
      composable = { page, pageState ->
         PageStackAppBar(
            combined, page, pageState,
            pageStackState.value, colors.value, windowInsets.value,
            horizontalContentPadding = horizontalContentPadding.value
         )
      }
   )
}

@ExperimentalMaterial3Api
@Composable
internal fun PageStackAppBar(
   pageStackState: PPageStackState,
   pageSwitcher: CombinedPageSwitcherState,
   colors: TopAppBarColors,
   windowInsets: WindowInsets,
   modifier: Modifier = Modifier,
   horizontalContentPadding: Dp = 0.dp
) {
   val updatedPageStackState = rememberUpdatedState(pageStackState)
   val updatedColors = rememberUpdatedState(colors)
   val updatedWindowInsets = rememberUpdatedState(windowInsets)
   val updatedHorizontalContentPadding = rememberUpdatedState(horizontalContentPadding)

   val pageComposables = remember(pageSwitcher) {
      pageSwitcher.allPageComposables
         .map {
            extractPageComposable(
               it, updatedPageStackState, updatedColors, updatedWindowInsets,
               updatedHorizontalContentPadding
            )
         }
         .toImmutableList()
   }

   Box(modifier) {
      PageSwitcher(pageStackState.rawState, pageComposables)
   }
}

@ExperimentalMaterial3Api
@Composable
internal fun <P : Page, S : PageState> PageStackAppBar(
   combined: CombinedPageComposable<P, S>,
   page: P,
   pageState: S,
   pageStackState: PPageStackState,
   colors: TopAppBarColors,
   windowInsets: WindowInsets,
   modifier: Modifier = Modifier,
   horizontalContentPadding: Dp = 0.dp,
) {
   TopAppBar(
      title = {
         combined.headerComposable(
            page,
            pageState,
            pageStackState
         )
      },
      navigationIcon = {
         IconButton(
            onClick = { pageStackState.finishPage() }
         ) {
            val icon = if (pageStackState.pageStack.tailOrNull() != null) {
               Icons.Default.ArrowBack
            } else {
               Icons.Default.Close
            }

            Icon(
               icon,
               contentDescription = Strings.PageDeck.pageStackAppBarCloseDescription
            )
         }
      },
      actions = {
         val headerActionsComposable = combined.headerActionsComposable
         if (headerActionsComposable != null) {
            headerActionsComposable(
               page,
               pageState,
               pageStackState
            )
         }
      },
      windowInsets = windowInsets.add(
         WindowInsets(
            left  = horizontalContentPadding,
            right = horizontalContentPadding
         )
      ),
      colors = colors,
      modifier = modifier
   )
}
