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

import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.animateTo
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wcaokaze.probosqis.app.pagedeck.CombinedPageSwitcherState
import com.wcaokaze.probosqis.app.pagedeck.SingleColumnPageDeck
import com.wcaokaze.probosqis.app.pagedeck.SingleColumnPageDeckAppBar
import com.wcaokaze.probosqis.app.pagedeck.SingleColumnPageDeckState
import com.wcaokaze.probosqis.capsiqum.page.PageStateStore
import com.wcaokaze.probosqis.ext.compose.layout.safeDrawing
import com.wcaokaze.probosqis.resources.Strings

@Composable
internal fun SingleColumnProbosqis(
   state: ProbosqisState,
   safeDrawingWindowInsets: WindowInsets = WindowInsets.safeDrawing
) {
   val pageDeckState = remember(state) {
      val pageDeckCache = state.loadPageDeckOrDefault()

      SingleColumnPageDeckState(
         pageDeckCache, state.pageStackRepository
      ).also { state.pageDeckState = it }
   }

   // 現状Desktopで動作しないため自前実装する
   // val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
   val appBarScrollState = remember { AppBarScrollState() }
   val nestedScrollConnection = remember(appBarScrollState) {
      AppBarNestedScrollConnection(appBarScrollState)
   }

   Column(
      modifier = Modifier
         .nestedScroll(nestedScrollConnection)
   ) {
      AppBar(
         appBarScrollState,
         pageDeckState,
         state.pageComposableSwitcher,
         state.pageStateStore,
         windowInsets = safeDrawingWindowInsets
            .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
      )

      SingleColumnPageDeck(
         pageDeckState,
         state.pageComposableSwitcher,
         state.pageStateStore,
         windowInsets = safeDrawingWindowInsets
            .only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal),
         modifier = Modifier
            .fillMaxSize()
      )
   }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppBar(
   scrollState: AppBarScrollState,
   deckState: SingleColumnPageDeckState,
   pageSwitcher: CombinedPageSwitcherState,
   pageStateStore: PageStateStore,
   windowInsets: WindowInsets
) {
   Surface(
      tonalElevation = 3.dp,
      modifier = Modifier
         .scrollable(rememberScrollState(), Orientation.Vertical)
   ) {
      Column(
         Modifier
            .windowInsetsPadding(windowInsets.only(WindowInsetsSides.Top))
            .clipToBounds()
            .layout { measurable, constraints ->
               val placeable = measurable.measure(constraints)

               layout(
                  placeable.width,
                  placeable.height + scrollState.scrollOffset.toInt()
               ) {
                  placeable.place(0, scrollState.scrollOffset.toInt())
               }
            }
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
            windowInsets = windowInsets.only(WindowInsetsSides.Horizontal),
            modifier = Modifier
               .onSizeChanged { scrollState.updateAppBarHeight(it.height) }
         )

         SingleColumnPageDeckAppBar(
            deckState,
            pageSwitcher,
            pageStateStore,
            windowInsets = windowInsets.only(WindowInsetsSides.Horizontal)
         )
      }
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

@Stable
private class AppBarScrollState {
   var scrollOffset by mutableStateOf(0.0f)
      private set

   private var appBarHeight by mutableStateOf(0)

   fun scroll(offset: Float): Float {
      val oldScrollOffset = scrollOffset
      scrollOffset = (scrollOffset + offset)
         .coerceIn(-appBarHeight.toFloat(), 0.0f)
      return scrollOffset - oldScrollOffset
   }

   fun updateAppBarHeight(height: Int) {
      if (height == appBarHeight) { return }

      appBarHeight = height
      scrollOffset = scrollOffset.coerceIn(-appBarHeight.toFloat(), 0.0f)
   }

   @Suppress("UNUSED")
   suspend fun settle() {
      if (appBarHeight == 0) { return }

      val targetOffset = if (scrollOffset / appBarHeight < -0.5f) {
         -appBarHeight.toFloat()
      } else {
         0.0f
      }

      AnimationState(scrollOffset).animateTo(targetOffset) { scrollOffset = value }
   }
}

private class AppBarNestedScrollConnection(
   private val scrollState: AppBarScrollState
) : NestedScrollConnection {
   override fun onPreScroll(
      available: Offset,
      source: NestedScrollSource
   ): Offset {
      return Offset(0.0f, scrollState.scroll(available.y))
   }
}
