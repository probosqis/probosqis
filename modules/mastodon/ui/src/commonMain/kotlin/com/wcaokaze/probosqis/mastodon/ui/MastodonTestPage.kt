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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.wcaokaze.probosqis.capsiqum.page.PageStateFactory
import com.wcaokaze.probosqis.mastodon.entity.Application
import com.wcaokaze.probosqis.mastodon.repository.AppRepository
import com.wcaokaze.probosqis.page.PPage
import com.wcaokaze.probosqis.page.PPageComposable
import com.wcaokaze.probosqis.page.PPageState
import com.wcaokaze.probosqis.panoptiqon.Cache
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

   var application: Cache<Application>? by mutableStateOf(null)
   val applicationCache = appRepository.loadAppCache("https://pawoo.net/")

   suspend fun createApplication() {
      withContext(Dispatchers.IO) {
         application = appRepository.createApp("https://pawoo.net/")
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
   footer = { _, _ -> },
   pageTransitions = {}
)

@Composable
private fun MastodonTestPage(state: MastodonTestPageState) {
   Column {
      Text(
         state.application?.value.toString()
      )

      Text(
         "cache: ${state.applicationCache?.value.toString()}"
      )

      val coroutineScope = rememberCoroutineScope()
      Button(
         onClick = {
            coroutineScope.launch {
               state.createApplication()
            }
         }
      ) {
         Text("CREATE APP")
      }
   }
}
