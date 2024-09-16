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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import kotlinx.serialization.builtins.serializer

@Serializable
@SerialName("com.wcaokaze.probosqis.mastodon.ui.auth.urlinput")
class UrlInputPage : PPage()

@Stable
class UrlInputPageState(stateSaver: StateSaver) : PPageState() {
   var inputUrl: String by stateSaver.save("inputUrl", String.serializer()) { "" }
}

val urlInputPageComposable = PPageComposable<UrlInputPage, UrlInputPageState>(
   PageStateFactory { _, _, stateSaver -> UrlInputPageState(stateSaver) },
   header = { _, _ ->
      Text(
         Strings.Mastodon.authUrlInput.appBar,
         maxLines = 1,
         overflow = TextOverflow.Ellipsis
      )
   },
   content = { _, state, _ ->
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

         TextField(
            state.inputUrl,
            onValueChange = { newValue ->
               state.inputUrl = newValue
            },
            placeholder = {
               Text("https://mastodon.social/")
            },
            singleLine = true,
            modifier = Modifier
               .padding(vertical = 8.dp)
               .fillMaxWidth()
         )

         Button(
            onClick = {},
            shape = ButtonDefaults.filledTonalShape,
            colors = ButtonDefaults.filledTonalButtonColors(),
            elevation = ButtonDefaults.filledTonalButtonElevation(),
            modifier = Modifier.align(Alignment.End)
         ) {
            Text(Strings.Mastodon.authUrlInput.startAuthButton)
         }
      }
   },
   footer = null,
   pageTransitions = {
   }
)
