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

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.Orientation
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.jetbrains.annotations.TestOnly
import kotlin.time.Duration.Companion.milliseconds

private const val PAGE_STACK_PADDING_DP = 8

@Stable
class MultiColumnPageStackBoardState(
   pageStackBoardCache: WritableCache<PageStackBoard>,
   pageStackRepository: PageStackRepository,
   animCoroutineScope: CoroutineScope
) : PageStackBoardState(
   pageStackBoardCache,
   pageStackRepository,
   animCoroutineScope
) {
   override var firstVisiblePageStackIndex by mutableStateOf(0)
      internal set

   override fun pageStackState(id: PageStack.Id): PageStackState?
         = layout.pageStackLayout(id)?.pageStackState

   override fun pageStackState(index: Int): PageStackState
         = layout.pageStackLayout(index).pageStackState
}

@Stable
internal class LayoutLogic(
   pageStackBoard: PageStackBoard,
   private val pageStackStateConstructor: (WritableCache<PageStack>) -> PageStackState
) : Iterable<LayoutLogic.PageStackLayoutState> {
   @Stable
   internal class PageStackLayoutState(val pageStackState: PageStackState) {
      val pageStackId = pageStackState.pageStack.id

      /**
       * [PageStackBoardState.pageStackBoard]をセットして生成された直後の
       * インスタンスの場合 `false`。[layout]が呼ばれて位置とサイズが決まったあと
       * `true` になる
       */
      var isInitialized by mutableStateOf(false)
         internal set

      private val yOffsetAnimatable = Animatable(0.0f)
      internal suspend fun animateInsertion(yOffset: Float) {
         coroutineScope {
            alphaAnimatable.snapTo(0.0f)
            yOffsetAnimatable.snapTo(yOffset)

            delay(200.milliseconds)

            launch {
               alphaAnimatable.animateTo(1.0f, tween(durationMillis = 200))
            }
            launch {
               yOffsetAnimatable.animateTo(0.0f, tween(durationMillis = 200))
            }
         }
      }
      internal suspend fun animateRemoving(yOffset: Float) {
         coroutineScope {
            launch {
               alphaAnimatable.animateTo(
                  0.0f, tween(durationMillis = 200, easing = LinearEasing))
            }
            launch {
               yOffsetAnimatable.animateTo(
                  yOffset, tween(durationMillis = 200, easing = LinearEasing))
            }
         }
      }

      private lateinit var positionAnimatable: Animatable<IntOffset, *>
      val position: IntOffset get() {
         require(isInitialized)
         val (x, y) = positionAnimatable.value
         return IntOffset(x, y + yOffsetAnimatable.value.toInt())
      }
      internal val targetPosition: IntOffset get() {
         assert(isInitialized)
         return positionAnimatable.targetValue
      }
      internal suspend fun animatePosition(
         targetPosition: IntOffset,
         animationSpec: AnimationSpec<IntOffset>
      ) {
         positionAnimatable.animateTo(targetPosition, animationSpec)
      }

      private lateinit var widthAnimatable: Animatable<Int, *>
      val width: Int get() {
         require(isInitialized)
         return widthAnimatable.value
      }
      internal val targetWidth: Int get() {
         assert(isInitialized)
         return widthAnimatable.targetValue
      }
      internal suspend fun animateWidth(targetWidth: Int) {
         widthAnimatable.animateTo(targetWidth)
      }

      private val alphaAnimatable = Animatable(1.0f)
      val alpha: Float get() = alphaAnimatable.value

      internal fun initialize(position: IntOffset, width: Int) {
         require(!isInitialized)
         positionAnimatable = Animatable(position, IntOffset.VectorConverter)
         widthAnimatable = Animatable(width, Int.VectorConverter)
         isInitialized = true
      }

      internal suspend fun awaitInitialized() {
         snapshotFlow { isInitialized }
            .filter { it }
            .first()
      }
   }

   internal var pageStackBoardWidth by mutableStateOf(0)
      private set

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

   private var maxScrollOffsetAnimJob: Job
         by mutableStateOf(Job().apply { complete() })
   private var maxScrollOffsetAnimTarget by mutableStateOf(0.0f)

   init {
      recreateLayoutState(pageStackBoard)
   }

   fun pageStackLayout(id: PageStack.Id): PageStackLayoutState? = map[id]

   fun pageStackLayout(index: Int): PageStackLayoutState = list[index]

   override operator fun iterator(): Iterator<PageStackLayoutState>
         = list.iterator()

   internal fun <T> pageStackPositionAnimSpec() = spring<T>()

   internal fun recreateLayoutState(pageStackBoard: PageStackBoard) {
      val prevLayoutList = list
      val prevLayoutMap = map

      // 再生成が不要な可能性もあるため、必要になるまでは生成しないまま進める
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

      for (pageStackElement in pageStackBoard.sequence()) {
         val pageStack = pageStackElement.cache.value

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

         if (i < 0) {
            if (layoutState == null) {
               val pageStackState = pageStackStateConstructor(pageStackElement.cache)
               layoutState = PageStackLayoutState(pageStackState)
            }

            resultList += layoutState
            resultMap[pageStack.id] = layoutState
         }
      }

      when {
         i < 0 -> {
            list = resultList.toImmutableList()
            map = resultMap.toImmutableMap()
         }
         i < prevLayoutList.size -> {
            prepareResults()
            list = resultList.toImmutableList()
            map = resultMap.toImmutableMap()
         }
      }
   }

   /**
    * @param animCoroutineScope
    *   PageStackの移動や幅変更があったときのアニメーションを再生するための
    *   CoroutineScope
    */
   internal fun layout(
      animCoroutineScope: CoroutineScope,
      pageStackBoardWidth: Int,
      pageStackCount: Int,
      pageStackPadding: Int,
      scrollState: PageStackBoardScrollState
   ) {
      // ---- 各PageStackの位置計算

      val pageStackWidth = (
         (pageStackBoardWidth - pageStackPadding * 2) / pageStackCount
         - pageStackPadding * 2
      )

      var x = pageStackPadding

      for (layoutState in list) {
         x += pageStackPadding
         val position = IntOffset(x, 0)
         x += pageStackWidth + pageStackPadding

         if (!layoutState.isInitialized) {
            // 初回コンポジション。アニメーション不要
            layoutState.initialize(position, pageStackWidth)
         } else {
            // リコンポジション。位置か幅が変化してる場合アニメーションする
            if (layoutState.targetPosition != position) {
               animCoroutineScope.launch {
                  layoutState.animatePosition(position, pageStackPositionAnimSpec())
               }
            }

            if (layoutState.targetWidth != pageStackWidth) {
               animCoroutineScope.launch {
                  layoutState.animateWidth(pageStackWidth)
               }
            }
         }
      }

      this.pageStackBoardWidth = pageStackBoardWidth

      x += pageStackPadding
      this.pageStackPadding = pageStackPadding

      // ---- maxScrollOffset調整

      val maxScrollOffset = (x - pageStackBoardWidth).toFloat().coerceAtLeast(0f)
      if (!maxScrollOffsetAnimJob.isActive
         || maxScrollOffsetAnimTarget != maxScrollOffset)
      {
         if (scrollState.scrollOffset <= maxScrollOffset) {
            // スクロール位置の調整不要なためmaxScrollOffsetのセットだけ行う
            scrollState.setMaxScrollOffset(maxScrollOffset)
         } else {
            // maxScrollOffsetまでスクロールアニメーションする
            maxScrollOffsetAnimTarget = maxScrollOffset
            maxScrollOffsetAnimJob = animCoroutineScope.launch {
               scrollState.scroll(enableOverscroll = true) {
                  scrollState.setMaxScrollOffset(maxScrollOffset)

                  var prevValue = scrollState.scrollOffset
                  animate(
                     initialValue = prevValue,
                     targetValue = maxScrollOffset,
                     animationSpec = pageStackPositionAnimSpec()
                  ) { value, _ ->
                     prevValue += scrollBy(value - prevValue)
                  }
               }
            }
         }
      }
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

         state.layout(density = this, pageStackBoardWidth, pageStackCount,
            pageStackPadding)

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
               PageStack(
                  pageStackLayout.pageStackState,
                  isActive = pageStackCount == 1,
                  windowInsets.only(WindowInsetsSides.Bottom),
                  pageComposableSwitcher,
                  onTopAppBarHeightChanged,
                  modifier = Modifier
                     .alpha(pageStackLayout.alpha)
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

      val coroutineScope = rememberCoroutineScope()

      Column {
         @OptIn(ExperimentalMaterial3Api::class)
         TopAppBar(
            title = { Text("Home") },
            navigationIcon = {
               IconButton(
                  onClick = {
                     coroutineScope.launch {
                        state.removeFromBoard()
                     }
                  }
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
