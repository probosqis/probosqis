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
import androidx.compose.runtime.Stable
import com.wcaokaze.probosqis.resources.LocalLanguage
import com.wcaokaze.probosqis.resources.Strings
import com.wcaokaze.probosqis.resources.appName

interface MastodonStrings {
   interface AuthUrlInput {
      val appBar: String
      val description: String
      val serverUrlTextFieldLabel: String
      val startAuthButton: String
      val serverUrlGettingError: String

      @Stable
      fun unsupportedServerSoftwareError(softwareName: String): String
   }

   class CallbackWaiter(
      val android: Android,
      val desktop: Desktop,
   ) {
      class Android(
         val appBar: String,
         val initialMessage: String,
         val tokenLoadingMessage: String,
         val errorMessage: String,
      )

      class Desktop(
         val appBar: String,
         val message: String,
         val authorizationCodeInputFieldLabel: String,
         val verifyButton: String,
      )
   }

   val authUrlInput: AuthUrlInput
   val callbackWaiter: CallbackWaiter
}

val Strings.Companion.Mastodon: MastodonStrings
   @Composable
   @ReadOnlyComposable
   get() = when (LocalLanguage.current) {
      Strings.Language.ENGLISH -> object : MastodonStrings {
         override val authUrlInput = object : MastodonStrings.AuthUrlInput {
            override val appBar = "Add an account"
            override val description = "Add an existing account to $appName."
            override val serverUrlTextFieldLabel = "Server URL"
            override val startAuthButton = "GO"
            override val serverUrlGettingError = "Cannot connect to server."

            override fun unsupportedServerSoftwareError(softwareName: String)
               = "Unsupported server: $softwareName"
         }

         override val callbackWaiter = MastodonStrings.CallbackWaiter(
            MastodonStrings.CallbackWaiter.Android(
               appBar = "Add an account",
               initialMessage = "Please authorize $appName to access your account on your browser app.",
               tokenLoadingMessage = "Verifying your credential…",
               errorMessage = "Unfortunately, $appName failed to verify your account. Please try again.",
            ),
            MastodonStrings.CallbackWaiter.Desktop(
               appBar = "Add an account",
               message = "Please authorize $appName to access your account. And paste the authorization code.",
               authorizationCodeInputFieldLabel = "Authorization Code",
               verifyButton = "Verify the Code",
            ),
         )
      }

      Strings.Language.JAPANESE -> object : MastodonStrings {
         override val authUrlInput = object : MastodonStrings.AuthUrlInput {
            override val appBar = "アカウントを追加"
            override val description = "作成済みのアカウントを${appName}に追加します"
            override val serverUrlTextFieldLabel = "サーバーURL"
            override val startAuthButton = "GO"
            override val serverUrlGettingError = "サーバーに接続できません"

            override fun unsupportedServerSoftwareError(softwareName: String)
                = "サポートされていないサーバーです: $softwareName"
         }

         override val callbackWaiter = MastodonStrings.CallbackWaiter(
            MastodonStrings.CallbackWaiter.Android(
               appBar = "アカウントを追加",
               initialMessage = "ブラウザアプリで${appName}からのアカウントへのアクセスを許可してください。",
               tokenLoadingMessage = "認証情報を確認しています…",
               errorMessage = "認証に失敗しました。お手数ですが、もう一度お試しください。",
            ),
            MastodonStrings.CallbackWaiter.Desktop(
               appBar = "アカウントを追加",
               message = "${appName}からのアカウントへのアクセスを許可し、発行された認証コードを貼り付けてください。",
               authorizationCodeInputFieldLabel = "認証コード",
               verifyButton = "確認",
            ),
         )
      }
   }
