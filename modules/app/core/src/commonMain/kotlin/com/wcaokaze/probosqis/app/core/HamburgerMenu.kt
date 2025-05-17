/*
 * Copyright 2025 wcaokaze
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

package com.wcaokaze.probosqis.app.core

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wcaokaze.probosqis.foundation.resources.Strings

@Stable
internal class HamburgerMenuState

@Composable
internal fun HamburgerMenu(
   state: HamburgerMenuState,
   onSettingItemClick: () -> Unit
) {
   ModalDrawerSheet {
      Spacer(Modifier.weight(1f))

      HorizontalDivider()

      DropdownMenuItem(
         leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
         text = { Text(Strings.App.hamburgerMenuSettingItem) },
         onClick = onSettingItemClick
      )

      Spacer(Modifier.height(40.dp))
   }
}
