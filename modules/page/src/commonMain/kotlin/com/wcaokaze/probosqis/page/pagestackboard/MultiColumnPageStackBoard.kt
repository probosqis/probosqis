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
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollable
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
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.wcaokaze.probosqis.cache.core.WritableCache
import com.wcaokaze.probosqis.page.PageComposableSwitcher
import com.wcaokaze.probosqis.page.PageStack
import com.wcaokaze.probosqis.page.PageStackContent
import com.wcaokaze.probosqis.page.PageStackState
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableMap

private const val PAGE_STACK_PADDING_DP = 8

@Stable
class MultiColumnPageStackBoardState(
   pageStackBoardCache: WritableCache<PageStackBoard>
) : PageStackBoardState(pageStackBoardCache) {
   @Stable
   class LayoutState(
      val pageStackCache: WritableCache<PageStack>,
      initialPosition: IntOffset,
      initialWidth: Int,
   ) {
      var position by mutableStateOf(initialPosition)
      var width by mutableStateOf(initialWidth)
   }

   var layoutStates: ImmutableMap<PageStack.Id, LayoutState>
         by mutableStateOf(persistentMapOf())
      private set

   private var pageStackPadding by mutableStateOf(0)

   internal val scrollState = PageStackBoardScrollState()

   internal fun layout(
      pageStackBoardWidth: Int,
      pageStackCount: Int,
      density: Density,
   ) {
      val layoutStateMap = layoutStates
      val layoutResult = mutableMapOf<PageStack.Id, LayoutState>()

      val pageStackPadding = with (density) { PAGE_STACK_PADDING_DP.dp.roundToPx() }

      val pageStackWidth = (
         (pageStackBoardWidth - pageStackPadding * 2) / pageStackCount
         - pageStackPadding * 2
      )

      var x = pageStackPadding

      for (element in pageStackBoard.rootRow) {
         if (element !is PageStackBoard.PageStack) { continue }

         val pageStack = element.cache.value

         x += pageStackPadding
         val position = IntOffset(x, 0)
         x += pageStackWidth + pageStackPadding

         var layoutState = layoutStateMap[pageStack.id]
         if (layoutState != null) {
            layoutState.position = position
            layoutState.width = pageStackWidth
         } else {
            layoutState = LayoutState(element.cache, position, pageStackWidth)
         }
         layoutResult[pageStack.id] = layoutState
      }

      x += pageStackPadding

      layoutStates = layoutResult.toImmutableMap()
      this.pageStackPadding = pageStackPadding

      scrollState.setMaxScrollOffset(
         (x - pageStackBoardWidth).toFloat().coerceAtLeast(0f))
   }

   override suspend fun animateScrollTo(index: Int) {
      val pageStack = pageStackBoard[index].cache.value
      animateScrollTo(pageStack.id)
   }

   suspend fun animateScrollTo(pageStack: PageStack.Id) {
      val layoutState = layoutStates[pageStack]
         ?: throw NoSuchElementException("pageStack for ID $pageStack not found")

      val targetScrollOffset = layoutState.position.x - pageStackPadding * 2
      scrollState.animateScrollBy(targetScrollOffset - scrollState.scrollOffset)
   }
}

@Composable
fun MultiColumnPageStackBoard(
   state: MultiColumnPageStackBoardState,
   pageComposableSwitcher: PageComposableSwitcher,
   pageStackCount: Int,
   windowInsets: WindowInsets,
   modifier: Modifier = Modifier,
   onTopAppBarHeightChanged: (Dp) -> Unit = {},
) {
   SubcomposeLayout(
      modifier = modifier
         .scrollable(
            state.scrollState,
            Orientation.Horizontal,
            // scrollableで検知する指の動きは右に動いたとき正の値となる。
            // ScrollScope.scrollByは正のとき「右が見える」方向へスクロールする。
            // よってこの2つは符号が逆であるため、ここで反転する
            reverseDirection = true
         ),
      measurePolicy = remember(state, pageStackCount) {{ constraints ->
         val pageStackBoardWidth = constraints.maxWidth
         val pageStackBoardHeight = constraints.maxHeight

         state.layout(
            pageStackBoardWidth,
            pageStackCount,
            density = this
         )

         val placeables = state.layoutStates.map { (pageStackId, layoutState) ->
            val measurable = subcompose(pageStackId) {
               val pageStackState = remember {
                  PageStackState(layoutState.pageStackCache, state)
               }

               PageStack(
                  pageStackState,
                  isActive = pageStackCount == 1,
                  windowInsets.only(WindowInsetsSides.Bottom),
                  pageComposableSwitcher,
                  onTopAppBarHeightChanged,
               )
            } .single()

            val pageStackConstraints = Constraints.fixed(
               layoutState.width, pageStackBoardHeight)

            val placeable = measurable.measure(pageStackConstraints)
            Triple(pageStackId, layoutState, placeable)
         }

         layout(pageStackBoardWidth, pageStackBoardHeight) {
            val scrollOffset = state.scrollState.scrollOffset.toInt()

            for ((_, layout, placeable) in placeables) {
               // scrollOffsetが大きいほど右のPageStackが表示される
               // つまりscrollOffsetが大きいほどPageStackの位置は左となるため
               // 符号が逆となる
               placeable.placeRelative(
                  -scrollOffset + layout.position.x, layout.position.y)
            }
         }
      }}
   )
}

@Composable
private fun PageStack(
   state: PageStackState,
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

         PageStackContent(
            state,
            pageComposableSwitcher
         )
      }
   }
}
