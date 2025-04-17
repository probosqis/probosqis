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

package com.wcaokaze.probosqis.mastodon.ui.auth.callbackwaiter

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wcaokaze.probosqis.capsiqum.page.PageStateFactory
import com.wcaokaze.probosqis.ext.compose.CircularProgressCompleteIcon
import com.wcaokaze.probosqis.mastodon.entity.Account
import com.wcaokaze.probosqis.mastodon.ui.Mastodon
import com.wcaokaze.probosqis.page.PPageComposable
import com.wcaokaze.probosqis.resources.Strings
import com.wcaokaze.probosqis.resources.icons.Error
import kotlinx.coroutines.delay
import kotlinx.serialization.builtins.serializer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.time.Duration.Companion.milliseconds

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
      Box {
         val slideOffset = with (LocalDensity.current) { 64.dp.roundToPx() }

         AnimatedContent(
            state.credentialAccountLoadState as? CredentialAccountLoadState.Success,
            transitionSpec = {
               val halfPi = PI.toFloat() / 2.0f

               val enter =
                  fadeIn(tween(300, delayMillis = 100)) +
                  slideInVertically(
                     tween(300, delayMillis = 100, easing = { sin(it * halfPi) }),
                     initialOffsetY = { slideOffset }
                  )
               val exit =
                  fadeOut(tween(300)) +
                  slideOutVertically(
                     tween(300, easing = { 1.0f - cos(it * halfPi) }),
                     targetOffsetY = { -slideOffset }
                  )

               enter togetherWith exit
            }
         ) { credentialAccountLoadState ->
            if (credentialAccountLoadState == null) {
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

               CallbackWaiterPageContent(
                  state.inputCode,
                  state.credentialAccountLoadState,
                  onInputCodeChange = { newValue ->
                     state.inputCode = newValue
                  },
                  onAuthorizationCodeTextFieldKeyboardActionGo = {
                     state.saveAuthorizedAccount()
                  },
                  onVerifyButtonClick = {
                     state.saveAuthorizedAccount()
                  },
                  focusRequester,
                  windowInsets
               )
            } else {
               val verifiedAccount = credentialAccountLoadState
                  .credentialAccount.account.value
               val verifiedAccountIcon = credentialAccountLoadState
                  .credentialAccountIcon.value?.composeImageBitmap

               VerifiedAccount(
                  verifiedAccount, verifiedAccountIcon, windowInsets
               )
            }
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
   tokenLoadState: CredentialAccountLoadState,
   onInputCodeChange: (TextFieldValue) -> Unit,
   onAuthorizationCodeTextFieldKeyboardActionGo: KeyboardActionScope.() -> Unit,
   onVerifyButtonClick: () -> Unit,
   focusRequester: FocusRequester,
   windowInsets: WindowInsets
) {
   Column(
      modifier = Modifier
         .fillMaxSize()
         .verticalScroll(rememberScrollState())
         .windowInsetsPadding(windowInsets)
         .padding(16.dp)
   ) {
      Text(
         Strings.Mastodon.callbackWaiter.desktop.message,
         fontSize = 15.sp,
         modifier = Modifier.padding(8.dp)
      )

      Spacer(Modifier.height(24.dp))

      AuthorizationCodeInputField(
         inputCode, showError = tokenLoadState is CredentialAccountLoadState.Error,
         onInputCodeChange, onAuthorizationCodeTextFieldKeyboardActionGo,
         focusRequester
      )

      Row(
         verticalAlignment = Alignment.CenterVertically,
         modifier = Modifier
            .align(Alignment.End)
            .padding(horizontal = 8.dp)
      ) {
         val isLoading = tokenLoadState is CredentialAccountLoadState.Loading

         if (isLoading) {
            CircularProgressIndicator(
               strokeWidth = 2.dp,
               modifier = Modifier.size(16.dp)
            )

            Spacer(Modifier.width(16.dp))
         }

         Button(
            onClick = onVerifyButtonClick,
            enabled = !isLoading && inputCode.text.isNotEmpty(),
            shape = ButtonDefaults.filledTonalShape,
            colors = ButtonDefaults.filledTonalButtonColors(),
            elevation = ButtonDefaults.filledTonalButtonElevation()
         ) {
            Text(Strings.Mastodon.callbackWaiter.desktop.verifyButton)
         }
      }
   }
}

@Composable
private fun VerifiedAccount(
   verifiedAccount: Account,
   verifiedAccountIcon: ImageBitmap?,
   windowInsets: WindowInsets
) {
   Column(
      modifier = Modifier
         .fillMaxSize()
         .verticalScroll(rememberScrollState())
         .windowInsetsPadding(windowInsets)
         .padding(16.dp)
   ) {
      Row(
         verticalAlignment = Alignment.CenterVertically,
         modifier = Modifier.padding(horizontal = 8.dp)
      ) {
         CompleteIcon(
            modifier = Modifier
               .size(with (LocalDensity.current) { 22.sp.toDp() })
         )

         Text(
            Strings.Mastodon.callbackWaiter.desktop.verifySucceedMessage,
            fontSize = 15.sp,
            modifier = Modifier.padding(8.dp)
         )
      }

      VerifiedAccount(
         verifiedAccount,
         verifiedAccountIcon,
         modifier = Modifier
            .padding(vertical = 16.dp)
            .fillMaxWidth()
      )
   }
}

@Composable
private fun AuthorizationCodeInputField(
   inputCode: TextFieldValue,
   showError: Boolean,
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
      supportingText = {
         Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = if (showError) { Modifier } else { Modifier.alpha(0.0f) }
         ) {
            Icon(
               Icons.Default.Error,
               contentDescription = null,
               modifier = Modifier.size(16.dp)
            )

            Text(
               Strings.Mastodon.callbackWaiter.desktop.errorMessage,
               modifier = Modifier.padding(horizontal = 4.dp)
            )
         }
      },
      isError = showError,
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

@Composable
private fun CompleteIcon(
   modifier: Modifier = Modifier
) {
   Box(
      contentAlignment = Alignment.Center,
      modifier = modifier
   ) {
      CircularProgressIndicator(
         progress = { 1.0f },
         strokeWidth = 1.5.dp
      )

      CircularProgressCompleteIcon(
         animDelay = 700.milliseconds,
         strokeWidth = 1.5.dp
      )
   }
}
