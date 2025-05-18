/*
 * Copyright 2024-2025 wcaokaze
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
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

private val lightNavigationBarBackground = Color.White
private val darkNavigationBarBackground = Color(0x1B, 0x1B, 0x1B)

internal fun ComponentActivity.initializeEdgeToEdge() {
   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
      enableEdgeToEdge(
         navigationBarStyle = if (resources.isDarkTheme()) {
            SystemBarStyle.dark(0)
         } else {
            SystemBarStyle.light(0, 0)
         }
      )
   } else {
      val lightScrim = lightNavigationBarBackground.copy(alpha = 0.9f)
      val darkScrim  = darkNavigationBarBackground .copy(alpha = 0.5f)

      enableEdgeToEdge(
         navigationBarStyle = if (resources.isDarkTheme()) {
            SystemBarStyle.dark(darkScrim.toArgb())
         } else {
            SystemBarStyle.light(lightScrim.toArgb(), darkScrim.toArgb())
         }
      )
   }
}

private fun Resources.isDarkTheme(): Boolean {
   return configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
       Configuration.UI_MODE_NIGHT_YES
}

@Immutable
private sealed class NavigationBarStyle {
   abstract val scrim: Color

   @Immutable
   data class LightTheme(
      override val scrim: Color,
      val darkScrim: Color
   ) : NavigationBarStyle()

   @Immutable
   data class DarkTheme(
      override val scrim: Color
   ) : NavigationBarStyle()
}

@Composable
internal fun NavigationBarScrim() {
   val activity = LocalActivity.current as? ComponentActivity

   if (activity != null) {
      val density = LocalDensity.current
      val layoutDirection = LocalLayoutDirection.current
      val navigationBarInsets = WindowInsets.navigationBars

      @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
      val navigationBarStyle = getNavigationBarStyle(
         density, layoutDirection, navigationBarInsets,
         primaryContainerColor = MaterialTheme.colorScheme.primaryContainer,
         isSystemInDarkTheme(),
         windowSizeClass = calculateWindowSizeClass(activity)
      )

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
         if (navigationBarStyle.scrim != Color.Transparent) {
            val left   = navigationBarInsets.getLeft  (density, layoutDirection)
            val top    = navigationBarInsets.getTop   (density)
            val right  = navigationBarInsets.getRight (density, layoutDirection)
            val bottom = navigationBarInsets.getBottom(density)

            Box(Modifier.fillMaxSize()) {
               if (left > 0) {
                  Box(
                     Modifier
                        .align(Alignment.CenterStart)
                        .width(with (density) { left.toDp() })
                        .fillMaxHeight()
                        .background(navigationBarStyle.scrim)
                  )
               }

               if (top > 0) {
                  Box(
                     Modifier
                        .align(Alignment.TopStart)
                        .fillMaxWidth()
                        .height(with (density) { top.toDp() })
                        .background(navigationBarStyle.scrim)
                  )
               }

               if (right > 0) {
                  Box(
                     Modifier
                        .align(Alignment.CenterEnd)
                        .width(with (density) { right.toDp() })
                        .fillMaxHeight()
                        .background(navigationBarStyle.scrim)
                  )
               }

               if (bottom > 0) {
                  Box(
                     Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(with (density) { bottom.toDp() })
                        .background(navigationBarStyle.scrim)
                  )
               }
            }
         }
      } else {
         LaunchedEffect(activity, navigationBarStyle) {
            activity.enableEdgeToEdge(
               navigationBarStyle = when (navigationBarStyle) {
                  is NavigationBarStyle.LightTheme -> SystemBarStyle.light(
                     navigationBarStyle.scrim.toArgb(),
                     navigationBarStyle.darkScrim.toArgb()
                  )

                  is NavigationBarStyle.DarkTheme -> SystemBarStyle.dark(
                     navigationBarStyle.scrim.toArgb()
                  )
               }
            )
         }
      }
   }
}

private fun getNavigationBarStyle(
   density: Density,
   layoutDirection: LayoutDirection,
   navigationBarInsets: WindowInsets,
   primaryContainerColor: Color,
   isDarkTheme: Boolean,
   windowSizeClass: WindowSizeClass
): NavigationBarStyle {
   val isNavigationBarBottom
       =  navigationBarInsets.getBottom(density)                  >  0
       && navigationBarInsets.getLeft  (density, layoutDirection) <= 0
       && navigationBarInsets.getTop   (density)                  <= 0
       && navigationBarInsets.getRight (density, layoutDirection) <= 0

   val windowWidthClass = windowSizeClass.widthSizeClass
   val isSingleColumn = windowWidthClass <= WindowWidthSizeClass.Medium

   // ナビゲーションバーが十分に薄いときtrue
   // 具体的にはジェスチャーナビゲーションのときtrue、2ボタン、3ボタンのときfalse
   val isNavigationBarThin
       = navigationBarInsets.getBottom(density) <= with (density) { 32.dp.toPx() }

   val shouldTransparentNavigationBar
       = isNavigationBarBottom && (isNavigationBarThin || isSingleColumn)

   val lightNavigationBarScrim: Color
   val darkNavigationBarScrim: Color

   if (shouldTransparentNavigationBar) {
      lightNavigationBarScrim = Color.Transparent
      darkNavigationBarScrim  = Color.Transparent
   } else {
      lightNavigationBarScrim = lightNavigationBarBackground.copy(alpha = 0.9f)
         .compositeOver(primaryContainerColor)
         .copy(alpha = 0.9f)

      darkNavigationBarScrim = darkNavigationBarBackground.copy(alpha = 0.9f)
         .compositeOver(primaryContainerColor)
         .copy(alpha = 0.8f)
   }

   return if (isDarkTheme) {
      NavigationBarStyle.DarkTheme(darkNavigationBarScrim)
   } else {
      NavigationBarStyle.LightTheme(lightNavigationBarScrim, darkNavigationBarScrim)
   }
}
