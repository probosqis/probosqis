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

import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.animateTo
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wcaokaze.probosqis.cache.core.WritableCache
import com.wcaokaze.probosqis.ext.compose.layout.safeDrawing
import com.wcaokaze.probosqis.ext.kotlin.datetime.BehindClock
import com.wcaokaze.probosqis.page.Column
import com.wcaokaze.probosqis.page.ColumnBoard
import com.wcaokaze.probosqis.page.ColumnBoardRepository
import kotlinx.collections.immutable.persistentListOf
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Probosqis(di: DI) {
   // 現状Desktopで動作しないため自前実装する
   // val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
   val appBarScrollState = remember { AppBarScrollState() }
   val nestedScrollConnection = remember(appBarScrollState) {
      AppBarNestedScrollConnection(appBarScrollState)
   }

   Scaffold(
      topBar = { AppBar(appBarScrollState) },
      contentWindowInsets = WindowInsets(0, 0, 0, 0),
      modifier = Modifier
         .nestedScroll(nestedScrollConnection)
   ) { paddingValues ->
      LazyColumn(
         contentPadding = WindowInsets.safeDrawing
            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
            .asPaddingValues(),
         modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
            .background(Color.Cyan)
      ) {
         items(42) { i ->
            Box(Modifier.fillMaxWidth().height(48.dp)) {
               Text(
                  "$i",
                  fontSize = 20.sp,
                  modifier = Modifier.align(Alignment.CenterStart)
               )
            }
         }
      }
   }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppBar(state: AppBarScrollState) {
   Column(
      Modifier
         .shadow(4.dp)
         .background(MaterialTheme.colorScheme.primaryContainer)
         .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
         .verticalScroll(rememberScrollState())
   ) {
      Column(
         Modifier
            .layout { measurable, constraints ->
               val placeable = measurable.measure(constraints)

               layout(
                  placeable.width,
                  placeable.height + state.scrollOffset.toInt()
               ) {
                  placeable.place(0, state.scrollOffset.toInt())
               }
            }
      ) {
         TopAppBar(
            title = { Text("Probosqis") },
            navigationIcon = {
               IconButton(
                  onClick = {}
               ) {
                  Icon(Icons.Default.Menu, contentDescription = "Menu")
               }
            },
            windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
            colors = TopAppBarDefaults.smallTopAppBarColors(
               containerColor = Color.Transparent
            ),
            modifier = Modifier
               .onSizeChanged { state.updateAppBarHeight(it.height) },
         )

         Row {
            TopAppBar(
               title = { Text("Home") },
               navigationIcon = {
                  IconButton(
                     onClick ={}
                  ) {
                     Icon(Icons.Default.Close, contentDescription = "Close")
                  }
               },
               windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
               colors = TopAppBarDefaults.smallTopAppBarColors(
                  containerColor = Color.Transparent
               ),
               modifier = Modifier
            )
         }
      }
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

   override suspend fun onPostFling(
      consumed: Velocity,
      available: Velocity
   ): Velocity {
      // AppBarに限って考えればここでavailableがゼロじゃないパターンは
      // ほとんどありえない（AppBarの高さより小さい幅でFlingした場合だけ）
      // ので無視する
      scrollState.settle()
      return Velocity.Zero
   }
}

internal fun loadColumnBoardOrDefault(
   columnBoardRepository: ColumnBoardRepository
): WritableCache<ColumnBoard> {
   return try {
      columnBoardRepository.loadColumnBoard()
   } catch (e: Exception) {
      val columnBoard = ColumnBoard(
         columns = createDefaultColumns()
      )
      columnBoardRepository.saveColumnBoard(columnBoard)
   }
}

private fun createDefaultColumns(): List<Column> {
   return persistentListOf(
      Column(TestPage(0), BehindClock(Duration.ZERO)),
      Column(TestPage(1), BehindClock(1.milliseconds)),
   )
}
