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

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.wcaokaze.probosqis.ext.kotlin.Url

@Composable
actual fun rememberBrowserLauncher(): BrowserLauncher {
   val context = LocalContext.current
   return remember(context) {
      AndroidBrowserLauncher(context)
   }
}

class AndroidBrowserLauncher(private val context: Context) : BrowserLauncher {
   override fun launchBrowser(url: Url) {
      val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url.raw))
      context.startActivity(intent)
   }
}
