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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wcaokaze.probosqis.capsiqum.page.PageStateFactory
import com.wcaokaze.probosqis.mastodon.ui.Mastodon
import com.wcaokaze.probosqis.page.PPage
import com.wcaokaze.probosqis.page.PPageComposable
import com.wcaokaze.probosqis.page.PPageState
import com.wcaokaze.probosqis.resources.Strings
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("com.wcaokaze.probosqis.mastodon.ui.auth.callbackwaiter.CallbackWaiterPage")
class CallbackWaiterPage : PPage()

@Stable
class CallbackWaiterPageState : PPageState<CallbackWaiterPage>()

val callbackWaiterPageComposable = PPageComposable<CallbackWaiterPage, CallbackWaiterPageState>(
   PageStateFactory { _, _ -> CallbackWaiterPageState() },
   header = { _, _ ->
      Text(
         Strings.Mastodon.callbackWaiter.appBar,
         maxLines = 1,
         overflow = TextOverflow.Ellipsis
      )
   },
   content = { _, _, windowInsets ->
      Box(
         modifier = Modifier
            .verticalScroll(rememberScrollState())
            .windowInsetsPadding(windowInsets)
      ) {
         Text(
            Strings.Mastodon.callbackWaiter.message,
            modifier = Modifier.padding(16.dp)
         )
      }
   },
   footer = null,
   pageTransitions = {
   }
)
