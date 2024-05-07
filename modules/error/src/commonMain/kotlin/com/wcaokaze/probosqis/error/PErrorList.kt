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

package com.wcaokaze.probosqis.error

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.wcaokaze.probosqis.resources.icons.Error

@Stable
class PErrorListState {
   var isShown by mutableStateOf(false)

   fun show() {
      isShown = true
   }

   fun hide() {
      isShown = false
   }
}

@Composable
fun PErrorList(
   state: PErrorListState,
   safeDrawingWindowInsets: WindowInsets
) {
   Box {
      val tapDetectorModifier = Modifier.pointerInput(Unit) {
         awaitPointerEventScope {
            awaitFirstDown()
            state.hide()
         }
      }

      Box(
         Modifier
            .fillMaxSize()
            .then(
               if (state.isShown) { tapDetectorModifier } else { Modifier }
            )
      )

      val density = LocalDensity.current

      PErrorListContent(
         state,
         modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(
               start = 32.dp,
               top = with (density) {
                  safeDrawingWindowInsets.getTop(density).toDp() + 8.dp
               },
               end = 8.dp,
            )
      )
   }
}

@Composable
private fun PErrorListContent(
   state: PErrorListState,
   modifier: Modifier = Modifier
) {
   if (state.isShown) {
      Column(
         modifier = modifier
            .shadow(3.dp, MaterialTheme.shapes.small)
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surface)
            .pointerInput(Unit) {}
      ) {
         Box(
            modifier = Modifier
               .fillMaxWidth()
               .height(48.dp)
               .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
         ) {
            Icon(
               Icons.Default.Error,
               contentDescription = null,
               modifier = Modifier
                  .align(Alignment.CenterStart)
                  .padding(horizontal = 16.dp)
            )
         }

         Text(
            """
            Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do
            eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut
            enim ad minim veniam, quis nostrud exercitation ullamco laboris
            nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in
            reprehenderit in voluptate velit esse cillum dolore eu fugiat
            nulla pariatur. Excepteur sint occaecat cupidatat non proident,
            sunt in culpa qui officia deserunt mollit anim id est laborum
            """.trimIndent()
         )
      }
   }
}
