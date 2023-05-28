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

package com.wcaokaze.probosqis

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.wcaokaze.probosqis.app.Probosqis

class MainActivity : ComponentActivity() {
   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)

      setUpSystemBars()

      setContent {
         val context = LocalContext.current
         val di = remember(context) { AndroidDI(context) }
         Probosqis(di)
      }
   }

   private fun isDarkMode(): Boolean {
      return (resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
   }

   private fun setUpSystemBars() {
      WindowCompat.setDecorFitsSystemWindows(window, false)

      val view = findViewById<View>(android.R.id.content)
      val windowInsetsController = WindowInsetsControllerCompat(window, view)

      val isDarkMode = isDarkMode()
      setUpStatusBar(windowInsetsController, isDarkMode)
      setUpNavigationBar(windowInsetsController, isDarkMode)
   }

   private fun setUpStatusBar(
      windowInsetsController: WindowInsetsControllerCompat,
      isDarkMode: Boolean
   ) {
      if (Build.VERSION.SDK_INT >= 23) {
         window.statusBarColor = 0
         windowInsetsController.isAppearanceLightStatusBars = !isDarkMode
      } else {
         // 22以前は透明にはできるもののアイコンの色を変更できないため半透明にする
         window.addFlags(
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
         )
      }
   }

   private fun setUpNavigationBar(
      windowInsetsController: WindowInsetsControllerCompat,
      isDarkMode: Boolean
   ) {
      when {
         Build.VERSION.SDK_INT >= 29 -> {
            // 29以降はジェスチャーナビゲーションのとき透明、
            // 3ボタンナビゲーションのときはいい感じの半透明にしてくれる
            window.navigationBarColor = 0
            windowInsetsController.isAppearanceLightNavigationBars = !isDarkMode
         }
         Build.VERSION.SDK_INT >= 26 -> {
            window.navigationBarColor =
               if (isDarkMode) { 0x80000000.toInt() } else { 0xccffffff.toInt() }
            windowInsetsController.isAppearanceLightNavigationBars = !isDarkMode
         }
         else -> {
            // 25以前は透明にはできるもののアイコンの色を変更できないため半透明にする
            window.addFlags(
               @Suppress("DEPRECATION")
               WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
            )
         }
      }
   }
}
