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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wcaokaze.probosqis.capsiqum.page.PageStateFactory
import com.wcaokaze.probosqis.ext.compose.LoadState
import com.wcaokaze.probosqis.mastodon.ui.Mastodon
import com.wcaokaze.probosqis.page.PPageComposable
import com.wcaokaze.probosqis.resources.Strings
import kotlinx.coroutines.delay
import kotlinx.serialization.builtins.serializer

@Stable
actual class CallbackWaiterPageState : AbstractCallbackWaiterPageState() {
   var hasKeyboardShown by save(
      "has_keyboard_shown", Boolean.serializer(),
      init = { false }, recover = { true }
   )

   var inputCode: TextFieldValue by save("inputCode", TextFieldValue.Saver) {
      TextFieldValue()
   }

   fun saveAuthorizedAccount() {
      saveAuthorizedAccountByCode(inputCode.text)
   }
}


actual val callbackWaiterPageComposable = PPageComposable<CallbackWaiterPage, CallbackWaiterPageState>(
   PageStateFactory { _, _ -> CallbackWaiterPageState() },
   header = { _, _ ->
      Text(
         Strings.Mastodon.callbackWaiter.desktop.appBar,
         maxLines = 1,
         overflow = TextOverflow.Ellipsis
      )
   },
   content = { _, state, windowInsets ->
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

      Box(
         modifier = Modifier
            .verticalScroll(rememberScrollState())
            .windowInsetsPadding(windowInsets)
      ) {
         when (val tokenLoadState = state.tokenLoadState) {
            is LoadState.Success -> {
               val token = tokenLoadState.data
               if (token == null) {
                  CallbackWaiterPageContent(
                     state.inputCode,
                     onInputCodeChange = { newValue ->
                        state.inputCode = newValue
                     },
                     onAuthorizationCodeTextFieldKeyboardActionGo = {
                        state.saveAuthorizedAccount()
                     },
                     onVerifyButtonClick = {
                        state.saveAuthorizedAccount()
                     },
                     focusRequester
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
            is LoadState.Loading -> {}
            is LoadState.Error -> {}
         }
      }
   },
   footer = null,
   pageTransitions = {
   }
)

@Composable
private fun CallbackWaiterPageContent(
   inputCode: TextFieldValue,
   onInputCodeChange: (TextFieldValue) -> Unit,
   onAuthorizationCodeTextFieldKeyboardActionGo: KeyboardActionScope.() -> Unit,
   onVerifyButtonClick: () -> Unit,
   focusRequester: FocusRequester
) {
   Column(
      modifier = Modifier
         .padding(16.dp)
   ) {
      Text(
         Strings.Mastodon.callbackWaiter.desktop.message,
         fontSize = 15.sp,
         modifier = Modifier.padding(8.dp)
      )

      Spacer(Modifier.height(24.dp))

      AuthorizationCodeInputField(
         inputCode, onInputCodeChange,
         onAuthorizationCodeTextFieldKeyboardActionGo, focusRequester
      )

      Button(
         onClick = onVerifyButtonClick,
         enabled = inputCode.text.isNotEmpty(),
         shape = ButtonDefaults.filledTonalShape,
         colors = ButtonDefaults.filledTonalButtonColors(),
         elevation = ButtonDefaults.filledTonalButtonElevation(),
         modifier = Modifier
            .align(Alignment.End)
            .padding(horizontal = 8.dp, vertical = 16.dp)
      ) {
         Text(Strings.Mastodon.callbackWaiter.desktop.verifyButton)
      }
   }
}

@Composable
private fun AuthorizationCodeInputField(
   inputCode: TextFieldValue,
   onInputCodeChange: (TextFieldValue) -> Unit,
   onKeyboardActionGo: KeyboardActionScope.() -> Unit,
   focusRequester: FocusRequester,
) {
   OutlinedTextField(
      inputCode,
      onInputCodeChange,
      label = {
         Text(Strings.Mastodon.callbackWaiter.desktop.authorizationCodeInputFieldLabel)
      },
      singleLine = true,
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
