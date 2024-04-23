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

package com.wcaokaze.probosqis

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.wcaokaze.probosqis.app.MultiColumnProbosqis
import com.wcaokaze.probosqis.app.ProbosqisState
import com.wcaokaze.probosqis.app.SingleColumnProbosqis
import com.wcaokaze.probosqis.app.TestNotePage
import com.wcaokaze.probosqis.app.TestPage
import com.wcaokaze.probosqis.app.TestTimelinePage
import com.wcaokaze.probosqis.app.pagedeck.AndroidPageDeckRepository
import com.wcaokaze.probosqis.app.pagedeck.AndroidPageStackRepository
import com.wcaokaze.probosqis.app.pagedeck.pageSerializer
import com.wcaokaze.probosqis.app.testNotePageComposable
import com.wcaokaze.probosqis.app.testPageComposable
import com.wcaokaze.probosqis.app.testTimelinePageComposable
import com.wcaokaze.probosqis.resources.ProbosqisTheme
import kotlinx.collections.immutable.persistentListOf

class MainActivity : ComponentActivity() {
   override fun onCreate(savedInstanceState: Bundle?) {
      initializeEdgeToEdge()
      super.onCreate(savedInstanceState)

      setContent {
         ProbosqisTheme {
            val context = LocalContext.current

            val coroutineScope = rememberCoroutineScope()

            val probosqisState = remember(context) {
               val allPageComposables = persistentListOf(
                  testPageComposable,
                  testTimelinePageComposable,
                  testNotePageComposable,
               )
               val pageStackRepository = AndroidPageStackRepository(
                  context,
                  allPageSerializers = listOf(
                     pageSerializer<TestPage>(),
                     pageSerializer<TestTimelinePage>(),
                     pageSerializer<TestNotePage>(),
                  )
               )
               val pageDeckRepository = AndroidPageDeckRepository(
                  context, pageStackRepository
               )

               ProbosqisState(allPageComposables, pageDeckRepository,
                  pageStackRepository, coroutineScope)
            }

            BackHandler {
               probosqisState.pageDeckState.activePageStackState.finishPage()
            }

            NavigationBarController()

            @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
            val windowSizeClass = calculateWindowSizeClass(activity = this)

            if (windowSizeClass.widthSizeClass > WindowWidthSizeClass.Medium) {
               MultiColumnProbosqis(probosqisState)
            } else {
               SingleColumnProbosqis(probosqisState)
            }
         }
      }
   }

   @Composable
   private fun NavigationBarController() {
      val colorScheme = MaterialTheme.colorScheme
      val navigationBarsInsets = WindowInsets.navigationBars
      val density = LocalDensity.current
      val layoutDirection = LocalLayoutDirection.current

      LaunchedEffect(colorScheme, navigationBarsInsets, layoutDirection) {
         updateSystemBarColors(colorScheme, navigationBarsInsets, density, layoutDirection)
      }
   }

   private fun isDarkTheme() = (
         resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
      ) == Configuration.UI_MODE_NIGHT_YES

   private val lightNavigationBarBackground = Color.White
   private val darkNavigationBarBackground = Color(0x1B, 0x1B, 0x1B)

   private fun initializeEdgeToEdge() {
      val lightScrim = lightNavigationBarBackground.copy(alpha = 0.9f)
      val darkScrim = darkNavigationBarBackground.copy(alpha = 0.5f)

      enableEdgeToEdge(
         navigationBarStyle = if (isDarkTheme()) {
            SystemBarStyle.dark(darkScrim.toArgb())
         } else {
            SystemBarStyle.light(lightScrim.toArgb(), darkScrim.toArgb())
         }
      )
   }

   private fun updateSystemBarColors(
      colorScheme: ColorScheme,
      navigationBarsInsets: WindowInsets,
      density: Density,
      layoutDirection: LayoutDirection
   ) {
      val lightNavigationBarScrim: Color
      val darkNavigationBarScrim:  Color

      // ナビゲーションバーが画面下部にあって十分に低い場合透明にする
      // 具体的には2ボタン、3ボタンでは半透明、ジェスチャーナビゲーションでは透明
      val shouldTransparentNavigationBar =
         navigationBarsInsets.getBottom(density) <= with (density) { 32.dp.toPx() }
               && navigationBarsInsets.getLeft (density, layoutDirection) <= 0
               && navigationBarsInsets.getTop  (density)                  <= 0
               && navigationBarsInsets.getRight(density, layoutDirection) <= 0

      if (shouldTransparentNavigationBar) {
         lightNavigationBarScrim = Color.Transparent
         darkNavigationBarScrim  = Color.Transparent
      } else {
         lightNavigationBarScrim = lightNavigationBarBackground.copy(alpha = 0.9f)
            .compositeOver(colorScheme.primaryContainer)
            .copy(alpha = 0.9f)

         darkNavigationBarScrim = darkNavigationBarBackground.copy(alpha = 0.9f)
            .compositeOver(colorScheme.primaryContainer)
            .copy(alpha = 0.8f)
      }

      enableEdgeToEdge(
         navigationBarStyle = if (isDarkTheme()) {
            SystemBarStyle.dark(darkNavigationBarScrim.toArgb())
         } else {
            SystemBarStyle.light(
               lightNavigationBarScrim.toArgb(), darkNavigationBarScrim.toArgb())
         }
      )
   }
}
