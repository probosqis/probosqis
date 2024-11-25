/*
 * Copyright 2024 wcaokaze
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
import android.content.res.Resources
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp

private val lightNavigationBarBackground = Color.White
private val darkNavigationBarBackground = Color(0x1B, 0x1B, 0x1B)

internal fun ComponentActivity.initializeEdgeToEdge() {
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

@Immutable
private sealed class NavigationBarStyle {
   @Immutable
   data class LightTheme(val scrim: Color, val darkScrim: Color) : NavigationBarStyle()

   @Immutable
   data class DarkTheme(val scrim: Color) : NavigationBarStyle()
}

@Composable
internal fun NavigationBarController() {
   val colorScheme = MaterialTheme.colorScheme
   val navigationBarsInsets = WindowInsets.navigationBars
   val density = LocalDensity.current
   val layoutDirection = LocalLayoutDirection.current

   val activity = LocalContext.current as? ComponentActivity

   if (activity != null) {
      @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
      val windowSizeClass = calculateWindowSizeClass(activity)

      val navigationBarStyle = remember(
         colorScheme, navigationBarsInsets, density, layoutDirection, activity,
         windowSizeClass
      ) {
         val windowWidthClass = windowSizeClass.widthSizeClass
         val resources = activity.resources

         val lightNavigationBarScrim: Color
         val darkNavigationBarScrim: Color

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

         if (resources.isDarkTheme()) {
            NavigationBarStyle.DarkTheme(darkNavigationBarScrim)
         } else {
            NavigationBarStyle.LightTheme(lightNavigationBarScrim, darkNavigationBarScrim)
         }
      }

      LaunchedEffect(activity, navigationBarStyle) {
         activity.enableEdgeToEdge(
            navigationBarStyle = when (val style = navigationBarStyle) {
               is NavigationBarStyle.LightTheme -> SystemBarStyle.light(
                  style.scrim.toArgb(),
                  style.darkScrim.toArgb()
               )
               is NavigationBarStyle.DarkTheme -> SystemBarStyle.dark(
                  style.scrim.toArgb()
               )
            }
         )
      }
   }
}
