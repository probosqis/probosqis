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

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
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

@Composable
internal fun rememberMultiColumnProbosqisColorScheme(): MultiColumnProbosqisColorScheme {
   return remember {
      MultiColumnProbosqisColorScheme(
      )
   }
}

@Immutable
internal class MultiColumnProbosqisColorScheme(
)
