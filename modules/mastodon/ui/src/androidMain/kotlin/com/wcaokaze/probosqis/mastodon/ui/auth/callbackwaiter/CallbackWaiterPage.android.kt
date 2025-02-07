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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wcaokaze.probosqis.capsiqum.page.PageStateFactory
import com.wcaokaze.probosqis.ext.compose.LoadState
import com.wcaokaze.probosqis.mastodon.ui.Mastodon
import com.wcaokaze.probosqis.page.PPageComposable
import com.wcaokaze.probosqis.resources.Strings
import com.wcaokaze.probosqis.resources.icons.Error

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
            .verticalScroll(rememberScrollState())
            .windowInsetsPadding(windowInsets)
      ) {
         when (val credentialAccountLoadState = state.credentialAccountLoadState) {
            is LoadState.Loading -> {
               Text(
                  Strings.Mastodon.callbackWaiter.android.tokenLoadingMessage,
                  modifier = Modifier.padding(16.dp)
               )
            }
            is LoadState.Error -> {
               Row(
                  modifier = Modifier.padding(16.dp)
               ) {
                  Icon(
                     Icons.Outlined.Error,
                     contentDescription = null,
                     modifier = Modifier
                        .padding(vertical = 4.dp)
                        .size(20.dp)
                  )

                  Spacer(Modifier.width(8.dp))

                  Text(
                     Strings.Mastodon.callbackWaiter.android.errorMessage,
                     fontSize = 15.sp
                  )
               }
            }
            is LoadState.Success -> {
               val credentialAccount = credentialAccountLoadState.data
               if (credentialAccount == null) {
                  Text(
                     Strings.Mastodon.callbackWaiter.android.initialMessage,
                     fontSize = 15.sp,
                     modifier = Modifier.padding(16.dp)
                  )
               } else {
                  Text(
                     credentialAccount.account.value.username ?: "",
                     fontSize = 15.sp,
                     modifier = Modifier.padding(8.dp)
                  )
               }
            }
         }
      }
   },
   footer = null,
   pageTransitions = {
   }
)
