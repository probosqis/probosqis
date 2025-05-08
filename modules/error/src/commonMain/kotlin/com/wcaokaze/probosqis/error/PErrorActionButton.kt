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

package com.wcaokaze.probosqis.error

import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import com.wcaokaze.probosqis.foundation.resources.Strings
import com.wcaokaze.probosqis.foundation.resources.icons.Error

@Composable
fun PErrorActionButton(
   state: PErrorListState,
   onClick: () -> Unit,
   modifier: Modifier = Modifier
) {
   if (state.errors.isNotEmpty()) {
      IconButton(
         onClick,
         modifier = modifier
            .onGloballyPositioned { state.buttonBounds = it.boundsInRoot() }
      ) {
         Icon(
            Icons.Outlined.Error,
            contentDescription = Strings.PError.pErrorActionButtonContentDescription
         )
      }
   }
}
