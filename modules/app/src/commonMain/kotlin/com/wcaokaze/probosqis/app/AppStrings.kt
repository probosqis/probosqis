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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import com.wcaokaze.probosqis.resources.LocalLanguage
import com.wcaokaze.probosqis.resources.Strings

interface AppStrings {
   val topAppBar: String
   val topAppBarNavigationContentDescription: String
}

val Strings.Companion.App: AppStrings
   @Composable
   @ReadOnlyComposable
   get() = when (LocalLanguage.current) {
      Strings.Language.ENGLISH -> object : AppStrings {
         override val topAppBar = "Probosqis"
         override val topAppBarNavigationContentDescription = "Menu"
      }

      Strings.Language.JAPANESE -> object : AppStrings {
         override val topAppBar = "Probosqis"
         override val topAppBarNavigationContentDescription = "メニュー"
      }
   }
