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

package com.wcaokaze.probosqis.ext.compose

import androidx.compose.runtime.Composable
import com.wcaokaze.probosqis.ext.kotlin.Url
import java.awt.Desktop
import java.net.URI

@Composable
actual fun rememberBrowserLauncher(): BrowserLauncher {
   return object : BrowserLauncher {
      fun canLaunchBrowser(): Boolean {
         return Desktop.isDesktopSupported()
             && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)
      }

      override fun launchBrowser(url: Url) {
         if (!canLaunchBrowser()) { TODO() }

         Desktop.getDesktop().browse(URI(url.raw))
      }
   }
}
