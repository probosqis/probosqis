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
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateTo
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.wcaokaze.probosqis.app.pagedeck.CombinedPageSwitcherState
import com.wcaokaze.probosqis.app.pagedeck.SingleColumnPageDeck
import com.wcaokaze.probosqis.app.pagedeck.SingleColumnPageDeckAppBar
import com.wcaokaze.probosqis.app.pagedeck.SingleColumnPageDeckState
import com.wcaokaze.probosqis.app.pagedeck.navigateToPage
import com.wcaokaze.probosqis.app.setting.account.list.AccountListPage
import com.wcaokaze.probosqis.ext.compose.layout.safeDrawing
import com.wcaokaze.probosqis.foundation.error.PErrorActionButton
import com.wcaokaze.probosqis.foundation.error.PErrorList
import com.wcaokaze.probosqis.foundation.error.PErrorListState
import com.wcaokaze.probosqis.foundation.resources.Strings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun SingleColumnProbosqis(
   state: ProbosqisState,
   onRequestCloseWindow: () -> Unit,
   colorScheme: SingleColumnProbosqisColorScheme = rememberSingleColumnProbosqisColorScheme(),
   safeDrawingWindowInsets: WindowInsets = WindowInsets.safeDrawing
) {
   val pageDeckState = koinInject<SingleColumnPageDeckState>()
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
            onSettingItemClick = {
               coroutineScope.launch {
                  pageDeckState.addColumn(AccountListPage())
                  drawerState.close()
               }
            }
         )
      },
      drawerState = drawerState
   ) {
      // 現状Desktopで動作しないため自前実装する
      // val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
      val appBarScrollState = remember { AppBarScrollState() }
      val nestedScrollConnection = remember(appBarScrollState) {
         AppBarNestedScrollConnection(appBarScrollState)
      }

      Box {
         val errorListState: PErrorListState = koinInject()
         val pageSwitcherState: CombinedPageSwitcherState = koinInject()

         Column(
            modifier = Modifier
               .background(colorScheme.background)
               .nestedScroll(nestedScrollConnection)
         ) {
            AppBar(
               appBarScrollState,
               errorListState,
               pageDeckState,
               pageSwitcherState,
               backgroundColor = colorScheme.appBar,
               windowInsets = safeDrawingWindowInsets
                  .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
               onHamburgerButtonClick = {
                  coroutineScope.launch {
                     drawerState.open()
                  }
               },
               onErrorButtonClick = {
                  coroutineScope.launch {
                     appBarScrollState.show()
                     errorListState.show()
                  }
               }
            )

            SingleColumnPageDeck(
               pageDeckState,
               pageSwitcherState,
               colorScheme.pageStack,
               windowInsets = safeDrawingWindowInsets
                  .only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal),
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
   scrollState: AppBarScrollState,
   errorListState: PErrorListState,
   deckState: SingleColumnPageDeckState,
   pageSwitcherState: CombinedPageSwitcherState,
   backgroundColor: Color,
   windowInsets: WindowInsets,
   onHamburgerButtonClick: () -> Unit,
   onErrorButtonClick: () -> Unit
) {
   val anim = remember { Animatable(0.dp, Dp.VectorConverter) }

   LaunchedEffect(errorListState.raisedTime) {
      if (errorListState.raisedTime == null) { return@LaunchedEffect }

      if (scrollState.isShown) {
         anim.animateErrorNotifier()
      } else {
         launch {
            scrollState.show()
         }
         launch {
            delay(100.milliseconds)
            anim.animateErrorNotifier()
         }
      }
   }

   Column(
      Modifier
         .scrollable(rememberScrollState(), Orientation.Vertical)
         .background(backgroundColor)
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
            containerColor = Color.Transparent,
         ),
         windowInsets = windowInsets.only(WindowInsetsSides.Horizontal),
         modifier = Modifier
            .onSizeChanged { scrollState.updateAppBarHeight(it.height) }
      )

      SingleColumnPageDeckAppBar(
         deckState,
         pageSwitcherState,
         windowInsets = windowInsets.only(WindowInsetsSides.Horizontal)
      )
   }
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

@Stable
private class AppBarScrollState {
   private val scrollMutex = MutatorMutex()

   var scrollOffset by mutableFloatStateOf(0.0f)
      private set

   var appBarHeight by mutableIntStateOf(0)

   val isShown: Boolean get() = scrollOffset / appBarHeight > -0.5f

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

   suspend fun show() {
      scrollMutex.mutate {
         AnimationState(scrollOffset).animateTo(0.0f) { scrollOffset = value }
      }
   }

   @Suppress("UNUSED")
   suspend fun settle() {
      if (appBarHeight == 0) { return }

      scrollMutex.mutate {
         val targetOffset = if (isShown) { 0.0f } else { -appBarHeight.toFloat() }

         AnimationState(scrollOffset).animateTo(targetOffset) { scrollOffset = value }
      }
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
