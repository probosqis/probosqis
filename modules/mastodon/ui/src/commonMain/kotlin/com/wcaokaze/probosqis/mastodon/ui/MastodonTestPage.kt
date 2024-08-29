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

package com.wcaokaze.probosqis.mastodon.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.rememberCoroutineScope
import com.wcaokaze.probosqis.capsiqum.page.PageStateFactory
import com.wcaokaze.probosqis.mastodon.repository.AppRepository
import com.wcaokaze.probosqis.page.PPage
import com.wcaokaze.probosqis.page.PPageComposable
import com.wcaokaze.probosqis.page.PPageState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.core.component.inject

@Serializable
@SerialName("com.wcaokaze.probosqis.mastodon.ui.MastodonTestPage")
class MastodonTestPage : PPage()

@Stable
class MastodonTestPageState : PPageState() {
   private val appRepository: AppRepository by inject()

   val snackbarHostState = SnackbarHostState()

   suspend fun getAuthorizeUrl(): String {
      return withContext(Dispatchers.IO) {
         appRepository.getAuthorizeUrl("https://pawoo.net/")
      }
   }
}

val mastodonTestPageComposable = PPageComposable<MastodonTestPage, MastodonTestPageState>(
   PageStateFactory { _, _, _ -> MastodonTestPageState() },
   content = { _, state, _ ->
      MastodonTestPage(state)
   },
   header = { _, _ ->
   },
   footer = { _, state ->
      SnackbarHost(state.snackbarHostState)
   },
   pageTransitions = {}
)

@Composable
private fun MastodonTestPage(state: MastodonTestPageState) {
   Column {
      val coroutineScope = rememberCoroutineScope()
      val browserLauncher = getBrowserLauncher()

      Button(
         onClick = {
            coroutineScope.launch {
               val authorizeUrl = try {
                  state.getAuthorizeUrl()
               } catch (_: Exception) {
                  state.snackbarHostState.showSnackbar("ERROROROR")
                  return@launch
               }

               browserLauncher.launchBrowser(authorizeUrl)
            }
         }
      ) {
         Text("START AUTH")
      }
   }
}
