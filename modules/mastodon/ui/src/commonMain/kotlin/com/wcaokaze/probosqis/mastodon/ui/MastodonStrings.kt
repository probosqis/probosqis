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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import com.wcaokaze.probosqis.resources.LocalLanguage
import com.wcaokaze.probosqis.resources.Strings
import com.wcaokaze.probosqis.resources.appName

interface MastodonStrings {
   class AuthUrlInput(
      val appBar: String,
      val description: String,
      val serverUrlTextFieldLabel: String,
      val startAuthButton: String,
      val serverUrlGettingError: String,
   )

   val authUrlInput: AuthUrlInput
}

val Strings.Companion.Mastodon: MastodonStrings
   @Composable
   @ReadOnlyComposable
   get() = when (LocalLanguage.current) {
      Strings.Language.ENGLISH -> object : MastodonStrings {
         override val authUrlInput = MastodonStrings.AuthUrlInput(
            appBar = "Add an account",
            description = "Add an existing account to $appName.",
            serverUrlTextFieldLabel = "Server URL",
            startAuthButton = "GO",
            serverUrlGettingError = "Cannot connect to server.",
         )
      }

      Strings.Language.JAPANESE -> object : MastodonStrings {
         override val authUrlInput = MastodonStrings.AuthUrlInput(
            appBar = "アカウントを追加",
            description = "作成済みのアカウントを${appName}に追加します",
            serverUrlTextFieldLabel = "サーバーURL",
            startAuthButton = "GO",
            serverUrlGettingError = "サーバーに接続できません",
         )
      }
   }
