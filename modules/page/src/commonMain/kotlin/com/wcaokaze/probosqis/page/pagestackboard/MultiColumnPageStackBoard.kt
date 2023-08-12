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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.wcaokaze.probosqis.cache.core.WritableCache
import com.wcaokaze.probosqis.page.PageComposableSwitcher
import com.wcaokaze.probosqis.page.PageStack
import com.wcaokaze.probosqis.page.PageStackContent
import com.wcaokaze.probosqis.page.PageStackState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import org.jetbrains.annotations.TestOnly

private const val PAGE_STACK_PADDING_DP = 8

@Stable
class MultiColumnPageStackBoardState(
   pageStackBoardCache: WritableCache<PageStackBoard>,
   pageStackRepository: PageStackRepository
) : PageStackBoardState(pageStackBoardCache, pageStackRepository) {
   override var firstVisiblePageStackIndex by mutableStateOf(0)
      internal set

   internal fun layout(
      pageStackBoardWidth: Int,
      pageStackCount: Int,
      pageStackPadding: Int,
   ) {
      layout.layout(pageStackBoard, pageStackBoardWidth, pageStackCount,
         pageStackPadding, scrollState)
   }
}

@Stable
internal class LayoutState : Iterable<LayoutState.PageStackLayoutState> {
   @Stable
   class PageStackLayoutState(
      val pageStackCache: WritableCache<PageStack>,
      initialPosition: IntOffset,
      initialWidth: Int
   ) {
      val pageStackId = pageStackCache.value.id
      var position by mutableStateOf(initialPosition)
      var width by mutableStateOf(initialWidth)
   }

   internal var pageStackPadding by mutableStateOf(0)
      private set

   /** [MultiColumnPageStackBoardState.pageStackBoard]と同じ順 */
   private var list: ImmutableList<PageStackLayoutState>
         by mutableStateOf(persistentListOf())

   private var map: ImmutableMap<PageStack.Id, PageStackLayoutState>
         by mutableStateOf(persistentMapOf())

   internal val layoutStateList
      @TestOnly get() = list

   internal val layoutStateMap
      @TestOnly get() = map

   fun pageStackLayout(id: PageStack.Id): PageStackLayoutState? = map[id]

   fun pageStackLayout(index: Int): PageStackLayoutState = list[index]

   override operator fun iterator(): Iterator<PageStackLayoutState>
         = list.iterator()

   internal fun layout(
      pageStackBoard: PageStackBoard,
      pageStackBoardWidth: Int,
      pageStackCount: Int,
      pageStackPadding: Int,
      scrollState: PageStackBoardScrollState
   ) {
      val prevLayoutList = list
      val prevLayoutMap = map

      /*
       * ウィンドウサイズの変更やPageStackサイズの変更等によって
       * 再レイアウトする場合、PageStackの順番は変わっておらず、
       * prevLayoutListとprevLayoutMapをそのまま再利用できる。
       * そのため、まずはpageStackBoardを0から順にレイアウトしていくが、
       * prevLayoutList[i]のPageStackIdがpageStackBoard[i]のPageStackIdと
       * 一致しなくなった時点でPageStackの並び替えや挿入があったことが確定し、
       * prevLayoutListを再利用できなくなる。その時点でiに-1が入り、resultList,
       * resultMapにi以前の要素がコピーされる。それ以降はprevLayoutMapから
       * レイアウトを探してresultList, resultMapに格納していく。
       */
      lateinit var resultList: MutableList<PageStackLayoutState>
      lateinit var resultMap: MutableMap<PageStack.Id, PageStackLayoutState>
      var i = 0

      fun prepareResults() {
         resultList = prevLayoutList.subList(0, i).toMutableList()
         resultMap = mutableMapOf()
         for (l in resultList) {
            resultMap[l.pageStackId] = l
         }
         i = -1
      }

      val pageStackWidth = (
         (pageStackBoardWidth - pageStackPadding * 2) / pageStackCount
         - pageStackPadding * 2
      )

      var x = pageStackPadding

      fun layout(parent: PageStackBoard.LayoutElementParent) {
         for (element in parent) {
            when (element) {
               is PageStackBoard.PageStack -> {
                  val pageStack = element.cache.value

                  x += pageStackPadding
                  val position = IntOffset(x, 0)
                  x += pageStackWidth + pageStackPadding

                  var layoutState = if (i >= 0) {
                     if (prevLayoutList.getOrNull(i)?.pageStackId == pageStack.id) {
                        prevLayoutList[i++]
                     } else {
                        prepareResults()
                        prevLayoutMap[pageStack.id]
                     }
                  } else {
                     prevLayoutMap[pageStack.id]
                  }

                  if (layoutState != null) {
                     layoutState.position = position
                     layoutState.width = pageStackWidth
                  } else {
                     layoutState = PageStackLayoutState(
                        element.cache, position, pageStackWidth)
                  }

                  if (i < 0) {
                     resultList += layoutState
                     resultMap[pageStack.id] = layoutState
                  }
               }

               is PageStackBoard.LayoutElementParent -> {
                  layout(element)
               }
            }
         }
      }

      layout(pageStackBoard.rootRow)

      x += pageStackPadding
      this.pageStackPadding = pageStackPadding

      when {
         i < 0 -> {
            map = resultMap.toImmutableMap()
            list = resultList.toImmutableList()
         }
         i < prevLayoutList.size -> {
            prepareResults()
            map = resultMap.toImmutableMap()
            list = resultList.toImmutableList()
         }
      }

      scrollState.setMaxScrollOffset(
         (x - pageStackBoardWidth).toFloat().coerceAtLeast(0f))
   }
}

@Composable
fun MultiColumnPageStackBoard(
   state: MultiColumnPageStackBoardState,
   pageComposableSwitcher: PageComposableSwitcher,
   pageStackCount: Int,
   windowInsets: WindowInsets,
   modifier: Modifier = Modifier,
   onTopAppBarHeightChanged: (Dp) -> Unit = {}
) {
   SubcomposeLayout(
      modifier = modifier
         .scrollable(
            state.scrollState,
            Orientation.Horizontal,
            // scrollableで検知する指の動きは右に動いたとき正の値となる。
            // ScrollScope.scrollByは正のとき「右が見える」方向へスクロールする。
            // よってこの2つは符号が逆であるため、ここで反転する
            reverseDirection = true,
            flingBehavior = remember(state) {
               PageStackBoardFlingBehavior.Standard(state)
            }
         ),
      measurePolicy = remember(state, pageStackCount) {{ constraints ->
         val pageStackBoardWidth = constraints.maxWidth
         val pageStackBoardHeight = constraints.maxHeight
         val pageStackPadding = PAGE_STACK_PADDING_DP.dp.roundToPx()

         state.layout(pageStackBoardWidth, pageStackCount, pageStackPadding)

         val scrollOffset = state.scrollState.scrollOffset.toInt()

         var firstVisibleIndex = -1

         val placeables = state.layout.mapIndexedNotNull { index, pageStackLayout ->
            val pageStackPosition = pageStackLayout.position
            val pageStackWidth = pageStackLayout.width

            if (firstVisibleIndex < 0) {
               if (pageStackPosition.x + pageStackWidth > scrollOffset) {
                  firstVisibleIndex = index
               }
            }

            // TODO: PageStackに影がつくかつかないか未定のためギリギリ範囲外の
            //       PageStackもコンポーズしている。影の件が決まり次第変更する
            if (pageStackPosition.x + pageStackWidth + pageStackPadding < scrollOffset ||
                pageStackPosition.x - pageStackPadding > scrollOffset + pageStackBoardWidth)
            {
               return@mapIndexedNotNull null
            }

            val measurable = subcompose(pageStackLayout.pageStackId) {
               val pageStackState = remember {
                  PageStackState(pageStackLayout.pageStackCache, state)
               }

               PageStack(
                  pageStackState,
                  isActive = pageStackCount == 1,
                  windowInsets.only(WindowInsetsSides.Bottom),
                  pageComposableSwitcher,
                  onTopAppBarHeightChanged
               )
            } .single()

            val pageStackConstraints = Constraints.fixed(
               pageStackWidth, pageStackBoardHeight)

            val placeable = measurable.measure(pageStackConstraints)
            Pair(pageStackLayout, placeable)
         }

         state.firstVisiblePageStackIndex = firstVisibleIndex

         layout(pageStackBoardWidth, pageStackBoardHeight) {
            for ((layout, placeable) in placeables) {
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
