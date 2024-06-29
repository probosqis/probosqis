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

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.dp
import com.wcaokaze.probosqis.error.PErrorListColors
import com.wcaokaze.probosqis.pagedeck.PageStackColors

@Composable
fun rememberSingleColumnProbosqisColorScheme(): SingleColumnProbosqisColorScheme {
   val materialColorScheme = MaterialTheme.colorScheme

   val pageStackBackground = materialColorScheme.surface

   val pageStackActivationAnimColor: Color
   val errorListBackgroundColor: Color
   val errorListItemBackgroundColor: Color

   if (isSystemInDarkTheme()) {
      pageStackActivationAnimColor = materialColorScheme.surfaceTint.copy(alpha = 0.09f)
      errorListBackgroundColor = pageStackBackground
      errorListItemBackgroundColor = materialColorScheme.surfaceColorAtElevation(1.dp)
   } else {
      pageStackActivationAnimColor = materialColorScheme.surfaceTint.copy(alpha = 0.04f)
      errorListBackgroundColor = materialColorScheme.surfaceColorAtElevation(1.dp)
      errorListItemBackgroundColor = pageStackBackground
   }

   val appBar = materialColorScheme.surfaceColorAtElevation(3.dp)

   return SingleColumnProbosqisColorScheme(
      background = materialColorScheme.background,
      appBar,
      pageStack = PageStackColors(
         pageStackBackground,
         content = materialColorScheme.onSurface,
         pageStackActivationAnimColor,
         footer = materialColorScheme.surfaceColorAtElevation(3.dp),
         footerContent = materialColorScheme.onSurface,
      ),
      PErrorListColors(
         errorListBackgroundColor,
         errorListItemBackgroundColor,
         content = materialColorScheme.onSurface,
         header = appBar,
         headerContent = materialColorScheme.onSurface,
      )
   )
}

@Immutable
class SingleColumnProbosqisColorScheme(
   val background: Color,
   val appBar: Color,
   val pageStack: PageStackColors,
   val errorListColors: PErrorListColors,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberMultiColumnProbosqisColorScheme(): MultiColumnProbosqisColorScheme {
   val materialColorScheme = MaterialTheme.colorScheme

   val background: Color
   val pageStackBackground: Color
   val pageStackActivationAnimColor: Color
   val activePageStackAppBarContainer: Color

   if (isSystemInDarkTheme()) {
      activePageStackAppBarContainer = materialColorScheme.primaryContainer
         .let { materialColorScheme.surface    .copy(alpha = 0.75f).compositeOver(it) }
         .let { materialColorScheme.surfaceTint.copy(alpha = 0.15f).compositeOver(it) }

      background = materialColorScheme.surface
      pageStackBackground = materialColorScheme.surfaceColorAtElevation(1.dp)
      pageStackActivationAnimColor = materialColorScheme.surfaceTint.copy(alpha = 0.09f)
   } else {
      activePageStackAppBarContainer = materialColorScheme.primaryContainer
         .let { materialColorScheme.surface.copy(alpha = 0.5f).compositeOver(it) }

      background = materialColorScheme.surfaceColorAtElevation(1.dp)
      pageStackBackground = materialColorScheme.surface
      pageStackActivationAnimColor = materialColorScheme.surfaceTint.copy(alpha = 0.04f)
   }

   val activePageStackAppBar = TopAppBarDefaults.topAppBarColors(
      containerColor = activePageStackAppBarContainer,
      navigationIconContentColor = materialColorScheme.onPrimaryContainer,
      titleContentColor = materialColorScheme.onPrimaryContainer,
      actionIconContentColor = materialColorScheme.onPrimaryContainer,
   )

   val inactivePageStackAppBar = TopAppBarDefaults.topAppBarColors(
      containerColor = materialColorScheme.surfaceColorAtElevation(6.dp),
   )

   return MultiColumnProbosqisColorScheme(
      background,
      PageStackColors(
         pageStackBackground,
         content = materialColorScheme.onSurface,
         pageStackActivationAnimColor,
         footer = materialColorScheme.surfaceColorAtElevation(6.dp),
         footerContent = materialColorScheme.onSurface,
      ),
      activePageStackAppBar,
      inactivePageStackAppBar,
      PErrorListColors(
         listBackground = background,
         itemBackground = pageStackBackground,
         content = materialColorScheme.onSurface,
         header = background,
         headerContent = materialColorScheme.onSurface,
      )
   )
}

@OptIn(ExperimentalMaterial3Api::class)
@Immutable
class MultiColumnProbosqisColorScheme(
   val background: Color,
   val pageStack: PageStackColors,
   val activePageStackAppBar: TopAppBarColors,
   val inactivePageStackAppBar: TopAppBarColors,
   val errorListColors: PErrorListColors,
)
