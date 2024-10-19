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

package com.wcaokaze.probosqis.mastodon.ui.auth.urlinput

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wcaokaze.probosqis.capsiqum.page.PageStateFactory
import com.wcaokaze.probosqis.ext.compose.LoadState
import com.wcaokaze.probosqis.ext.compose.LocalBrowserLauncher
import com.wcaokaze.probosqis.mastodon.repository.AppRepository
import com.wcaokaze.probosqis.mastodon.ui.Mastodon
import com.wcaokaze.probosqis.page.PPage
import com.wcaokaze.probosqis.page.PPageComposable
import com.wcaokaze.probosqis.page.PPageState
import com.wcaokaze.probosqis.resources.Strings
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import org.koin.core.component.inject

@Serializable
@SerialName("com.wcaokaze.probosqis.mastodon.ui.auth.urlinput.UrlInputPage")
class UrlInputPage : PPage()

@Stable
class UrlInputPageState : PPageState<UrlInputPage>() {
   private val appRepository: AppRepository by inject()

   private var authorizeUrlLoadState: LoadState<Unit>
       by mutableStateOf(LoadState.Success(Unit))

   val isLoading: Boolean by derivedStateOf {
      authorizeUrlLoadState is LoadState.Loading
   }

   var hasKeyboardShown by save(
      "has_keyboard_shown", Boolean.serializer(),
      init = { false }, recover = { true }
   )

   var inputUrl: TextFieldValue by save("inputUrl", TextFieldValue.Saver) {
      TextFieldValue("https://mastodon.social/", selection = TextRange(8, 24))
   }

   fun getAuthorizeUrl(): Deferred<Result<String>> {
      if (authorizeUrlLoadState is LoadState.Loading) {
         val e = IllegalStateException(
            "attempt to get authorize url but an old job is running yet."
         )

         return CompletableDeferred(Result.failure(e))
      }

      authorizeUrlLoadState = LoadState.Loading

      return pageStateScope.async {
         try {
            val instanceBaseUrl = inputUrl.text
            val authorizeUrl = withContext(Dispatchers.IO) {
               appRepository.getAuthorizeUrl(instanceBaseUrl)
            }
            authorizeUrlLoadState = LoadState.Success(Unit)
            Result.success(authorizeUrl)
         } catch (e: Exception) {
            authorizeUrlLoadState = LoadState.Error(e)
            Result.failure(e)
         }
      }
   }
}

val urlInputPageComposable = PPageComposable<UrlInputPage, UrlInputPageState>(
   PageStateFactory { _, _ -> UrlInputPageState() },
   header = { _, _ ->
      Text(
         Strings.Mastodon.authUrlInput.appBar,
         maxLines = 1,
         overflow = TextOverflow.Ellipsis
      )
   },
   content = { _, state, _ ->
      val focusRequester = remember { FocusRequester() }
      val keyboardController = LocalSoftwareKeyboardController.current

      LaunchedEffect(Unit) {
         if (!state.hasKeyboardShown) {
            state.hasKeyboardShown = true

            focusRequester.requestFocus()
            delay(100L)
            keyboardController?.show()
         }
      }

      Column(
         modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
      ) {
         Text(
            Strings.Mastodon.authUrlInput.description,
            modifier = Modifier.padding(vertical = 8.dp)
         )

         Spacer(modifier = Modifier.height(16.dp))

         OutlinedTextField(
            state.inputUrl,
            onValueChange = { newValue ->
               state.inputUrl = newValue
            },
            enabled = !state.isLoading,
            label = {
               Text(Strings.Mastodon.authUrlInput.serverUrlTextFieldLabel)
            },
            placeholder = {
               Text("https://mastodon.social/")
            },
            singleLine = true,
            modifier = Modifier
               .padding(vertical = 8.dp)
               .fillMaxWidth()
               .focusRequester(focusRequester)
         )

         val browserLauncher = LocalBrowserLauncher.current

         Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.align(Alignment.End)
         ) {
            if (state.isLoading) {
               CircularProgressIndicator(
                  strokeWidth = 2.dp,
                  modifier = Modifier.size(16.dp)
               )

               Spacer(Modifier.width(16.dp))
            }

            Button(
               onClick = {
                  // Composableが非表示になってもPageStateが生きているのであれば
                  // 続行すべき処理なのでpageStateScopeでlaunchする
                  state.pageStateScope.launch {
                     val authorizeUrl = state.getAuthorizeUrl().await()
                        .getOrThrow() // TODO

                     browserLauncher.launchBrowser(authorizeUrl)
                  }
               },
               enabled = !state.isLoading,
               shape = ButtonDefaults.filledTonalShape,
               colors = ButtonDefaults.filledTonalButtonColors(),
               elevation = ButtonDefaults.filledTonalButtonElevation()
            ) {
               Text(Strings.Mastodon.authUrlInput.startAuthButton)
            }
         }
      }
   },
   footer = null,
   pageTransitions = {
   }
)
