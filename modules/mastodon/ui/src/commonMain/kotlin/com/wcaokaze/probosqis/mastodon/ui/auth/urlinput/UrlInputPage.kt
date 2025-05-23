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
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wcaokaze.probosqis.capsiqum.page.PageStateFactory
import com.wcaokaze.probosqis.ext.compose.LoadState
import com.wcaokaze.probosqis.ext.compose.LocalBrowserLauncher
import com.wcaokaze.probosqis.ext.kotlin.Url
import com.wcaokaze.probosqis.foundation.resources.Strings
import com.wcaokaze.probosqis.foundation.resources.icons.Error
import com.wcaokaze.probosqis.mastodon.repository.AppRepository
import com.wcaokaze.probosqis.mastodon.ui.Mastodon
import com.wcaokaze.probosqis.mastodon.ui.auth.callbackwaiter.CallbackWaiterPage
import com.wcaokaze.probosqis.nodeinfo.entity.FediverseSoftware
import com.wcaokaze.probosqis.nodeinfo.repository.NodeInfoRepository
import com.wcaokaze.probosqis.foundation.page.PPage
import com.wcaokaze.probosqis.foundation.page.PPageComposable
import com.wcaokaze.probosqis.foundation.page.PPageState
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

internal class UnsupportedServerSoftwareException(
   val software: FediverseSoftware.Unsupported
) : Exception()

@Stable
class UrlInputPageState : PPageState<UrlInputPage>() {
   private val appRepository: AppRepository by inject()
   private val nodeInfoRepository: NodeInfoRepository by inject()

   var authorizeUrlLoadState: LoadState<Unit> by mutableStateOf(LoadState.Success(Unit))
      internal set

   var hasKeyboardShown by save(
      "has_keyboard_shown", Boolean.serializer(),
      init = { false }, recover = { true }
   )

   var inputUrl: TextFieldValue by save("inputUrl", TextFieldValue.Saver) {
      TextFieldValue("https://mastodon.social/", selection = TextRange(8, 24))
   }

   /**
    * @return (authorizeUrl, instanceBaseUrl)
    */
   fun getAuthorizeUrl(): Deferred<Result<Pair<Url, Url>>> {
      if (authorizeUrlLoadState is LoadState.Loading) {
         val e = IllegalStateException(
            "attempt to get authorize url but an old job is running yet."
         )

         return CompletableDeferred(Result.failure(e))
      }

      authorizeUrlLoadState = LoadState.Loading

      return pageStateScope.async {
         try {
            val result = withContext(Dispatchers.IO) {
               val software = nodeInfoRepository
                  .getServerSoftware(Url(inputUrl.text))

               when (software) {
                  is FediverseSoftware.Mastodon -> {
                     val authorizeUrl = appRepository.getAuthorizeUrl(software.instance)
                     Pair(authorizeUrl, software.instance.url)
                  }
                  is FediverseSoftware.Unsupported -> {
                     throw UnsupportedServerSoftwareException(software)
                  }
               }
            }
            authorizeUrlLoadState = LoadState.Success(Unit)
            Result.success(result)
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

      val browserLauncher by rememberUpdatedState(LocalBrowserLauncher.current)

      fun launchBrowserForAuthorize() {
         // Composableが非表示になってもPageStateが生きているのであれば
         // 続行すべき処理なのでpageStateScopeでlaunchする
         state.pageStateScope.launch {
            val (authorizeUrl, instanceBaseUrl) = state.getAuthorizeUrl().await()
               .getOrElse { return@launch }

            browserLauncher.launchBrowser(authorizeUrl)

            state.startPage(CallbackWaiterPage(instanceBaseUrl))
         }
      }

      UrlInputPageContent(
         state.inputUrl,
         state.authorizeUrlLoadState,
         onInputUrlChange = { newValue ->
            state.inputUrl = newValue
         },
         onUrlTextFieldKeyboardActionGo = {
            launchBrowserForAuthorize()
         },
         onGoButtonClick = {
            launchBrowserForAuthorize()
         },
         focusRequester
      )
   },
   footer = null,
   pageTransitions = {
   }
)

@Composable
private fun UrlInputPageContent(
   inputUrl: TextFieldValue,
   authorizeUrlLoadState: LoadState<Unit>,
   onInputUrlChange: (TextFieldValue) -> Unit,
   onUrlTextFieldKeyboardActionGo: KeyboardActionScope.() -> Unit,
   onGoButtonClick: () -> Unit,
   focusRequester: FocusRequester
) {
   Column(
      modifier = Modifier
         .verticalScroll(rememberScrollState())
         .padding(16.dp)
   ) {
      Text(
         Strings.Mastodon.authUrlInput.description,
         fontSize = 15.sp,
         modifier = Modifier.padding(8.dp)
      )

      Spacer(Modifier.height(24.dp))

      UrlTextField(
         inputUrl, authorizeUrlLoadState, onInputUrlChange,
         onUrlTextFieldKeyboardActionGo, focusRequester
      )

      Row(
         verticalAlignment = Alignment.CenterVertically,
         modifier = Modifier
            .align(Alignment.End)
            .padding(horizontal = 8.dp)
      ) {
         val isLoading = authorizeUrlLoadState is LoadState.Loading

         if (isLoading) {
            CircularProgressIndicator(
               strokeWidth = 2.dp,
               modifier = Modifier.size(16.dp)
            )

            Spacer(Modifier.width(16.dp))
         }

         Button(
            onClick = onGoButtonClick,
            enabled = !isLoading,
            shape = ButtonDefaults.filledTonalShape,
            colors = ButtonDefaults.filledTonalButtonColors(),
            elevation = ButtonDefaults.filledTonalButtonElevation()
         ) {
            Text(Strings.Mastodon.authUrlInput.startAuthButton)
         }
      }
   }
}

@Composable
private fun UrlTextField(
   inputUrl: TextFieldValue,
   authorizeUrlLoadState: LoadState<Unit>,
   onInputUrlChange: (TextFieldValue) -> Unit,
   onKeyboardActionGo: KeyboardActionScope.() -> Unit,
   focusRequester: FocusRequester,
) {
   val errorMessage = if (authorizeUrlLoadState !is LoadState.Error) {
      null
   } else {
      val exception = authorizeUrlLoadState.exception
      if (exception is UnsupportedServerSoftwareException) {
         Strings.Mastodon.authUrlInput.unsupportedServerSoftwareError(
            exception.software.name
         )
      } else {
         Strings.Mastodon.authUrlInput.serverUrlGettingError
      }
   }

   OutlinedTextField(
      inputUrl,
      onValueChange = onInputUrlChange,
      enabled = authorizeUrlLoadState !is LoadState.Loading,
      label = {
         Text(Strings.Mastodon.authUrlInput.serverUrlTextFieldLabel)
      },
      placeholder = {
         Text("https://mastodon.social/")
      },
      singleLine = true,
      supportingText = {
         Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = if (errorMessage != null) {
               Modifier
            } else {
               Modifier.alpha(0.0f)
            }
         ) {
            Icon(
               Icons.Default.Error,
               contentDescription = null,
               modifier = Modifier.size(16.dp)
            )

            Text(
               errorMessage ?: "",
               modifier = Modifier.padding(horizontal = 4.dp)
            )
         }
      },
      isError = authorizeUrlLoadState is LoadState.Error,
      keyboardOptions = KeyboardOptions(
         keyboardType = KeyboardType.Uri,
         imeAction = ImeAction.Go,
      ),
      keyboardActions = KeyboardActions(
         onGo = onKeyboardActionGo,
      ),
      modifier = Modifier
         .fillMaxWidth()
         .focusRequester(focusRequester)
   )
}
