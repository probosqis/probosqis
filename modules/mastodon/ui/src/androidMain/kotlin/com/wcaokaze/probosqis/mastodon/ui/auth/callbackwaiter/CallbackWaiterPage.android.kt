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
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.ExperimentalTransitionApi
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wcaokaze.probosqis.capsiqum.page.PageStateFactory
import com.wcaokaze.probosqis.ext.compose.CircularProgressCompleteIcon
import com.wcaokaze.probosqis.foundation.resources.Strings
import com.wcaokaze.probosqis.foundation.resources.icons.Error
import com.wcaokaze.probosqis.mastodon.ui.Mastodon
import com.wcaokaze.probosqis.foundation.page.PPageComposable
import kotlin.time.Duration.Companion.milliseconds

@Stable
actual class CallbackWaiterPageState : AbstractCallbackWaiterPageState()

actual val callbackWaiterPageComposable = PPageComposable<CallbackWaiterPage, CallbackWaiterPageState>(
   PageStateFactory { _, _ -> CallbackWaiterPageState() },
   header = { _, _ ->
      Text(
         Strings.Mastodon.callbackWaiter.android.appBar,
         maxLines = 1,
         overflow = TextOverflow.Ellipsis
      )
   },
   content = { _, state, windowInsets ->
      Box(
         modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .windowInsetsPadding(windowInsets)
      ) {
         AnimatedContent(
            state.credentialAccountLoadState,
            transitionSpec = {
               EnterTransition.None togetherWith ExitTransition.None using null
            }
         ) { credentialAccountLoadState ->
            val density = LocalDensity.current
            val progressIndicatorSize = with (density) { 22.sp.toDp() }

            when (credentialAccountLoadState) {
               is CredentialAccountLoadState.Loading -> {
                  Row(
                     verticalAlignment = Alignment.CenterVertically,
                     modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .padding(horizontal = 8.dp)
                  ) {
                     @OptIn(ExperimentalAnimationApi::class)
                     CircularProgressIndicator(
                        strokeWidth = 1.5.dp,
                        modifier = Modifier
                           .size(progressIndicatorSize)
                           .animateEnterExit(
                              enter = EnterTransition.None,
                              exit = fadeOut(snap(600))
                           )
                     )

                     @OptIn(ExperimentalAnimationApi::class)
                     Text(
                        Strings.Mastodon.callbackWaiter.android.tokenLoadingMessage,
                        fontSize = 15.sp,
                        modifier = Modifier
                           .padding(8.dp)
                           .animateEnterExit(
                              enter = EnterTransition.None,
                              exit = fadeOut(snap())
                           )
                     )
                  }
               }
               is CredentialAccountLoadState.Error -> {
                  Row(
                     modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                  ) {
                     Icon(
                        Icons.Outlined.Error,
                        contentDescription = null,
                        modifier = Modifier
                           .padding(vertical = 4.dp)
                           .size(progressIndicatorSize)
                     )

                     Spacer(Modifier.width(8.dp))

                     Text(
                        Strings.Mastodon.callbackWaiter.android.errorMessage,
                        fontSize = 15.sp
                     )
                  }
               }
               is CredentialAccountLoadState.Unloading -> {
                  Text(
                     Strings.Mastodon.callbackWaiter.android.initialMessage,
                     fontSize = 15.sp,
                     modifier = Modifier.fillMaxWidth()
                        .padding(16.dp)
                  )
               }
               is CredentialAccountLoadState.Success -> {
                  Column(
                     modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                  ) {
                     Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp)
                     ) {
                        CompleteIcon(
                           modifier = Modifier
                              .size(progressIndicatorSize)
                        )

                        Text(
                           Strings.Mastodon.callbackWaiter.desktop.verifySucceedMessage,
                           fontSize = 15.sp,
                           modifier = Modifier.padding(8.dp)
                        )
                     }

                     val verifiedAccount = credentialAccountLoadState
                        .credentialAccount.account.value
                     val verifiedAccountIcon = credentialAccountLoadState
                        .credentialAccountIcon.value?.composeImageBitmap

                     val slideInOffset = with (density) { -32.dp.roundToPx() }

                     VerifiedAccount(
                        verifiedAccount, verifiedAccountIcon,
                        modifier = Modifier
                           .padding(vertical = 16.dp)
                           .fillMaxWidth()
                           .animateEnterExit(
                              enter = fadeIn() + slideInVertically { slideInOffset },
                              exit = ExitTransition.None
                           )
                     )
                  }
               }
            }
         }
      }
   },
   footer = null,
   pageTransitions = {
   }
)

@Composable
private fun CompleteIcon(
   modifier: Modifier = Modifier
) {
   Box(
      contentAlignment = Alignment.Center,
      modifier = modifier
   ) {
      val transitionState = remember {
         MutableTransitionState(false).also { it.targetState = true }
      }
      @OptIn(ExperimentalTransitionApi::class)
      val transition = rememberTransition(transitionState)
      val alpha by transition.animateFloat(
         transitionSpec = { tween(500) },
         targetValueByState = { if (it) { 1f } else { 0f } }
      )

      CircularProgressIndicator(
         progress = { 1.0f },
         strokeWidth = 1.5.dp,
         modifier = Modifier.alpha(alpha)
      )

      CircularProgressCompleteIcon(
         animDelay = 500.milliseconds,
         strokeWidth = 1.5.dp
      )
   }
}
