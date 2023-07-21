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

      layoutStates = layoutResult.toImmutableMap()
   }

   override suspend fun animateScrollTo(pageStack: Int) {
      TODO()
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
      modifier = modifier,
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
            for ((_, layout, placeable) in placeables) {
               placeable.placeRelative(layout.position)
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
