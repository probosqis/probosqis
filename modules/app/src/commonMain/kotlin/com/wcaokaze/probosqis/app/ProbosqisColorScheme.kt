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

package com.wcaokaze.probosqis.app

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.dp

@Composable
internal fun rememberSingleColumnProbosqisColorScheme(): SingleColumnProbosqisColorScheme {
   val materialColorScheme = MaterialTheme.colorScheme

   return remember(materialColorScheme) {
      SingleColumnProbosqisColorScheme(
         background = materialColorScheme.background,
         appBar = materialColorScheme.surfaceColorAtElevation(3.dp),
         pageStackBackground = materialColorScheme.surface,
      )
   }
}

@Immutable
internal class SingleColumnProbosqisColorScheme(
   val background: Color,
   val appBar: Color,
   val pageStackBackground: Color,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun rememberMultiColumnProbosqisColorScheme(): MultiColumnProbosqisColorScheme {
   val materialColorScheme = MaterialTheme.colorScheme

   val activePageStackAppBar = TopAppBarDefaults.topAppBarColors(
      containerColor = materialColorScheme.surfaceTint.copy(alpha = 0.13f)
         .compositeOver(materialColorScheme.primaryContainer),
      navigationIconContentColor = materialColorScheme.onPrimaryContainer,
      titleContentColor = materialColorScheme.onPrimaryContainer,
      actionIconContentColor = materialColorScheme.onPrimaryContainer,
   )

   val inactivePageStackAppBar = TopAppBarDefaults.topAppBarColors(
      containerColor = materialColorScheme.surfaceColorAtElevation(3.dp),
   )

   return remember(materialColorScheme) {
      MultiColumnProbosqisColorScheme(
         background = materialColorScheme.background,
         activePageStackAppBar,
         inactivePageStackAppBar,
         pageStackBackground = materialColorScheme.surfaceColorAtElevation(1.dp),
      )
   }
}

@OptIn(ExperimentalMaterial3Api::class)
@Immutable
internal class MultiColumnProbosqisColorScheme(
   val background: Color,
   val activePageStackAppBar: TopAppBarColors,
   val inactivePageStackAppBar: TopAppBarColors,
   val pageStackBackground: Color,
)
