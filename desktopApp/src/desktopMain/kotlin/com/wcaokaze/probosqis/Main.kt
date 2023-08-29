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

import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.wcaokaze.probosqis.app.App
import com.wcaokaze.probosqis.app.Probosqis
import com.wcaokaze.probosqis.resources.Strings

fun main() {
   application {
      Window(
         title = Strings.App.topAppBar,
         onCloseRequest = { exitApplication() }
      ) {
         val di = remember { DesktopDI() }
         Probosqis(di)
      }
   }
}
