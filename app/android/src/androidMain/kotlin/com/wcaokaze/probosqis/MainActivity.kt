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

import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
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
import com.wcaokaze.probosqis.app.allVisiblePageStates
import com.wcaokaze.probosqis.mastodon.ui.auth.callbackwaiter.CallbackProcessor
import com.wcaokaze.probosqis.resources.ProbosqisTheme

class MainActivity : ComponentActivity() {
   private val probosqisState = ProbosqisState()

   override fun onCreate(savedInstanceState: Bundle?) {
      initializeEdgeToEdge()
      super.onCreate(savedInstanceState)

      setContent {
         ProbosqisTheme {
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

   override fun onNewIntent(intent: Intent?) {
      super.onNewIntent(intent)

      CallbackProcessor.onNewIntent(intent, probosqisState.allVisiblePageStates)
   }
}

private val lightNavigationBarBackground = Color.White
private val darkNavigationBarBackground = Color(0x1B, 0x1B, 0x1B)

private fun ComponentActivity.initializeEdgeToEdge() {
   val lightScrim = lightNavigationBarBackground.copy(alpha = 0.9f)
   val darkScrim = darkNavigationBarBackground.copy(alpha = 0.5f)

   enableEdgeToEdge(
      navigationBarStyle = if (resources.isDarkTheme()) {
         SystemBarStyle.dark(darkScrim.toArgb())
      } else {
         SystemBarStyle.light(lightScrim.toArgb(), darkScrim.toArgb())
      }
   )
}

private fun Resources.isDarkTheme(): Boolean {
   return configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
       Configuration.UI_MODE_NIGHT_YES
}

@Composable
private fun NavigationBarController() {
   val colorScheme = MaterialTheme.colorScheme
   val navigationBarsInsets = WindowInsets.navigationBars
   val density = LocalDensity.current
   val layoutDirection = LocalLayoutDirection.current

   val activity = LocalContext.current as? ComponentActivity

   if (activity != null) {
      @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
      val windowSizeClass = calculateWindowSizeClass(activity)

      LaunchedEffect(
         activity, colorScheme, navigationBarsInsets, density, layoutDirection,
         windowSizeClass
      ) {
         activity.updateSystemBarColors(
            colorScheme, windowSizeClass.widthSizeClass, navigationBarsInsets,
            density, layoutDirection
         )
      }
   }
}

private fun ComponentActivity.updateSystemBarColors(
   colorScheme: ColorScheme,
   windowWidthClass: WindowWidthSizeClass,
   navigationBarsInsets: WindowInsets,
   density: Density,
   layoutDirection: LayoutDirection
) {
   val lightNavigationBarScrim: Color
   val darkNavigationBarScrim:  Color

   val isNavigationBarBottom
       =  navigationBarsInsets.getBottom(density)                  >  0
       && navigationBarsInsets.getLeft  (density, layoutDirection) <= 0
       && navigationBarsInsets.getTop   (density)                  <= 0
       && navigationBarsInsets.getRight (density, layoutDirection) <= 0

   // ナビゲーションバーが十分に薄いときtrue
   // 具体的にはジェスチャーナビゲーションのときtrue、2ボタン、3ボタンのときfalse
   val isNavigationBarThin
       = navigationBarsInsets.getBottom(density) <= with (density) { 32.dp.toPx() }

   val isSingleColumn = windowWidthClass <= WindowWidthSizeClass.Medium

   val shouldTransparentNavigationBar
       = isNavigationBarBottom && (isNavigationBarThin || isSingleColumn)

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
      navigationBarStyle = if (resources.isDarkTheme()) {
         SystemBarStyle.dark(darkNavigationBarScrim.toArgb())
      } else {
         SystemBarStyle.light(
            lightNavigationBarScrim.toArgb(), darkNavigationBarScrim.toArgb())
      }
   )
}
