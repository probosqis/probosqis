/*
 * Copyright 2023-2025 wcaokaze
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

package com.wcaokaze.probosqis.app.core

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.wcaokaze.probosqis.app.pagedeck.MultiColumnPageDeck
import com.wcaokaze.probosqis.app.pagedeck.MultiColumnPageDeckState
import com.wcaokaze.probosqis.app.pagedeck.navigateToPage
import com.wcaokaze.probosqis.app.setting.account.list.AccountListPage
import com.wcaokaze.probosqis.ext.compose.layout.safeDrawing
import com.wcaokaze.probosqis.foundation.error.PErrorActionButton
import com.wcaokaze.probosqis.foundation.error.PErrorList
import com.wcaokaze.probosqis.foundation.error.PErrorListState
import com.wcaokaze.probosqis.foundation.resources.Strings
import com.wcaokaze.probosqis.mastodon.ui.timeline.home.HomeTimelinePage
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun MultiColumnProbosqis(
   state: ProbosqisState,
   onRequestCloseWindow: () -> Unit,
   colorScheme: MultiColumnProbosqisColorScheme = rememberMultiColumnProbosqisColorScheme(),
   safeDrawingWindowInsets: WindowInsets = WindowInsets.safeDrawing
) {
   val coroutineScope = rememberCoroutineScope()
   val drawerState = rememberDrawerState(DrawerValue.Closed)

   if (drawerState.isOpen) {
      BackHandler {
         coroutineScope.launch {
            drawerState.close()
         }
      }
   }

   ModalNavigationDrawer(
      drawerContent = {
         HamburgerMenu(
            state.hamburgerMenuState,
            onHomeTimelineItemClick = { token ->
               coroutineScope.launch {
                  state.pageDeckState.addColumn(HomeTimelinePage(token))
                  drawerState.close()
               }
            },
            onSettingItemClick = {
               coroutineScope.launch {
                  state.pageDeckState.addColumn(AccountListPage())
                  drawerState.close()
               }
            }
         )
      },
      drawerState = drawerState
   ) {
      BoxWithConstraints(
         modifier = Modifier
            .background(colorScheme.background)
      ) {
         val errorListState: PErrorListState = koinInject()

         val pageStackCount = (maxWidth / 330.dp).toInt().coerceAtLeast(1)

         Column {
            AppBar(
               errorListState,
               safeDrawingWindowInsets,
               onHamburgerButtonClick = {
                  coroutineScope.launch {
                     drawerState.open()
                  }
               },
               onErrorButtonClick = { errorListState.show() }
            )

            val pageDeckState = koinInject<MultiColumnPageDeckState>()
               .also { state.pageDeckState = it }

            val isDeckEmpty by remember {
               derivedStateOf {
                  pageDeckState.deck.rootRow.childCount == 0
               }
            }

            LaunchedEffect(isDeckEmpty) {
               if (isDeckEmpty) {
                  onRequestCloseWindow()
               }
            }

            @OptIn(ExperimentalMaterial3Api::class)
            MultiColumnPageDeck(
               pageDeckState,
               pageSwitcherState = koinInject(),
               pageStackCount,
               colorScheme.activePageStackAppBar,
               colorScheme.inactivePageStackAppBar,
               colorScheme.pageStack,
               windowInsets = safeDrawingWindowInsets
                  .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
               modifier = Modifier
                  .fillMaxSize()
            )
         }

         PErrorList(
            errorListState,
            colorScheme.errorListColors,
            onRequestNavigateToPage = { pageId, fallbackPage ->
               coroutineScope.launch {
                  state.pageDeckState.navigateToPage(
                     pageId,
                     fallbackPage
                  )
               }
            }
         )
      }
   }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppBar(
   errorListState: PErrorListState,
   safeDrawingWindowInsets: WindowInsets,
   onHamburgerButtonClick: () -> Unit,
   onErrorButtonClick: () -> Unit
) {
   val anim = remember { Animatable(0.dp, Dp.VectorConverter) }

   LaunchedEffect(errorListState.raisedTime) {
      if (errorListState.raisedTime == null) { return@LaunchedEffect }
      anim.animateErrorNotifier()
   }

   TopAppBar(
      title = {
         Text(
            text = Strings.App.topAppBar,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
         )
      },
      navigationIcon = {
         HamburgerButton(
            onClick = onHamburgerButtonClick
         )
      },
      actions = {
         PErrorActionButton(
            errorListState,
            onClick = onErrorButtonClick,
            modifier = Modifier
               .offset { IntOffset(anim.value.roundToPx(), 0) }
         )
      },
      colors = TopAppBarDefaults.topAppBarColors(
         containerColor = Color.Transparent
      ),
      windowInsets = safeDrawingWindowInsets
         .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
   )
}

@Composable
private fun HamburgerButton(
   onClick: () -> Unit
) {
   IconButton(onClick) {
      Icon(
         Icons.Default.Menu,
         contentDescription = Strings.App.topAppBarNavigationContentDescription
      )
   }
}
