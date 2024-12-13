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

package com.wcaokaze.probosqis.mastodon.ui.auth.callbackwaiter

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wcaokaze.probosqis.capsiqum.page.PageStateFactory
import com.wcaokaze.probosqis.mastodon.entity.Token
import com.wcaokaze.probosqis.mastodon.repository.AppRepository
import com.wcaokaze.probosqis.mastodon.ui.Mastodon
import com.wcaokaze.probosqis.page.PPage
import com.wcaokaze.probosqis.page.PPageComposable
import com.wcaokaze.probosqis.page.PPageState
import com.wcaokaze.probosqis.resources.Strings
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.core.component.inject

@Serializable
@SerialName("com.wcaokaze.probosqis.mastodon.ui.auth.callbackwaiter.CallbackWaiterPage")
class CallbackWaiterPage(
   val instanceBaseUrl: String
) : PPage()

@Stable
class CallbackWaiterPageState : PPageState<CallbackWaiterPage>() {
   private val appRepository: AppRepository by inject()

   var token by mutableStateOf<Token?>(null)
      private set

   fun saveAuthorizedAccountByCode(code: String) {
      pageStateScope.launch {
         val application = appRepository.loadAppCache(page.instanceBaseUrl)
         token = appRepository.getToken(application.value, code)
      }
   }
}

val callbackWaiterPageComposable = PPageComposable<CallbackWaiterPage, CallbackWaiterPageState>(
   PageStateFactory { _, _ -> CallbackWaiterPageState() },
   header = { _, _ ->
      Text(
         Strings.Mastodon.callbackWaiter.appBar,
         maxLines = 1,
         overflow = TextOverflow.Ellipsis
      )
   },
   content = { _, state, windowInsets ->
      Box(
         modifier = Modifier
            .verticalScroll(rememberScrollState())
            .windowInsetsPadding(windowInsets)
      ) {
         val token = state.token
         if (token == null) {
            Text(
               Strings.Mastodon.callbackWaiter.message,
               modifier = Modifier.padding(16.dp)
            )
         } else {
            val format = remember(token) {
               buildString {
                  append("instance url: ")
                  append(token.instance.value.url)
                  appendLine()
                  append("token type: ")
                  append(token.tokenType)
                  appendLine()
                  append("scope: ")
                  append(token.scope)
                  appendLine()
                  append("created at: ")
                  append(token.createdAt)
                  appendLine()
                  append("access token: ")
                  repeat(token.accessToken.length) {
                     append("x")
                  }
               }
            }

            Text(
               format,
               modifier = Modifier.padding(16.dp)
            )
         }
      }
   },
   footer = null,
   pageTransitions = {
   }
)
