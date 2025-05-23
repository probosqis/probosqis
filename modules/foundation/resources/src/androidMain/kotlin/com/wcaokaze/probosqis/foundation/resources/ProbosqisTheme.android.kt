/*
 * Copyright 2023-2025 wcaokaze
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

package com.wcaokaze.probosqis.foundation.resources

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun colorScheme(): ColorScheme {
   val isDarkTheme = isSystemInDarkTheme()

   return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      val context = LocalContext.current

      if (isDarkTheme) {
         dynamicDarkColorScheme(context)
      } else {
         dynamicLightColorScheme(context)
      }
   } else {
      if (isDarkTheme) {
         darkColorScheme()
      } else {
         lightColorScheme()
      }
   }
}
