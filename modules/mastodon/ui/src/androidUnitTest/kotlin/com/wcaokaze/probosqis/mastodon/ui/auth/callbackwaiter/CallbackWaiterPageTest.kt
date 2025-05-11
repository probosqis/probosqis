/*
 * Copyright 2025 wcaokaze
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

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.wcaokaze.probosqis.capsiqum.page.test.rememberTestPageState
import com.wcaokaze.probosqis.entity.Image
import com.wcaokaze.probosqis.ext.kotlin.Url
import com.wcaokaze.probosqis.mastodon.entity.Account
import com.wcaokaze.probosqis.mastodon.entity.CredentialAccount
import com.wcaokaze.probosqis.mastodon.entity.Instance
import com.wcaokaze.probosqis.mastodon.entity.Status
import com.wcaokaze.probosqis.foundation.page.PPageState
import com.wcaokaze.probosqis.panoptiqon.Cache
import io.mockk.mockk
import kotlinx.datetime.Instant
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CallbackWaiterPageTest {
   @get:Rule
   val rule = createComposeRule()

   @Composable
   private fun rememberPageState(
      page: CallbackWaiterPage = CallbackWaiterPage(Url("https://example.com/")),
      pageStateBase: PPageState.Interface = mockk()
   ): CallbackWaiterPageState {
      val pageState = callbackWaiterPageComposable.pageStateFactory.rememberTestPageState(page)
      pageState.injectTestable(pageStateBase)
      return pageState
   }

   @Composable
   private fun CallbackWaiterPage(pageState: CallbackWaiterPageState) {
      callbackWaiterPageComposable.contentComposable(
         pageState.page, pageState, WindowInsets(0)
      )
   }

   @Test
   fun screenshot_initial() {
      rule.setContent {
         val state = rememberPageState()
         CallbackWaiterPage(state)
      }

      rule.onRoot().captureRoboImage("callbackWaiterPage/initial.png")
   }

   @Test
   fun screenshot_verifying() {
      rule.setContent {
         val state = rememberPageState().also {
            it.credentialAccountLoadState = CredentialAccountLoadState.Loading
         }

         CallbackWaiterPage(state)
      }

      rule.onRoot().captureRoboImage("callbackWaiterPage/verifying.png")
   }

   @Test
   fun screenshot_error() {
      rule.setContent {
         val state = rememberPageState().also {
            it.credentialAccountLoadState = CredentialAccountLoadState.Error
         }

         CallbackWaiterPage(state)
      }

      rule.onRoot().captureRoboImage("callbackWaiterPage/error.png")
   }

   @Test
   fun screenshot_success() {
      rule.setContent {
         val state = rememberPageState().also {
            it.credentialAccountLoadState = CredentialAccountLoadState.Success(
               CredentialAccount(
                  id = Account.Id(
                     instanceUrl = Url("https://example.com/"),
                     Account.LocalId("account id"),
                  ),
                  Cache(Account(
                     instance = Cache(Instance(
                        Url("https://example.com/"),
                        version = "0.0.0",
                        versionCheckedTime = Instant.fromEpochMilliseconds(
                           0L
                        ),
                     )),
                     id = Account.Id(
                        instanceUrl = Url("https://example.com/"),
                        Account.LocalId("account id"),
                     ),
                     "username",
                     "acct",
                     Url("https://example.com/account_id"),
                     "displayName",
                     "profileNote",
                     Url("https://example.com/avatarImageUrl"),
                     Url("https://example.com/avatarStaticImageUrl"),
                     Url("https://example.com/headerImageUrl"),
                     Url("https://example.com/headerStaticImageUrl"),
                     isLocked = false,
                     profileFields = emptyList(),
                     emojisInProfile = emptyList(),
                     isBot = false,
                     isGroup = false,
                     isDiscoverable = false,
                     isNoindex = false,
                     movedTo = null,
                     isSuspended = false,
                     isLimited = false,
                     createdTime = Instant.fromEpochMilliseconds(0L),
                     lastStatusPostTime = Instant.fromEpochMilliseconds(0L),
                     statusCount = 0L,
                     followerCount = 0L,
                     followeeCount = 0L,
                  )),
                  rawProfileNote = "profileNote",
                  rawProfileFields = emptyList(),
                  defaultPostVisibility = Status.Visibility.PUBLIC,
                  defaultPostSensitivity = false,
                  defaultPostLanguage = "ja",
                  followRequestCount = 0L,
                  role = null,
               ),
               credentialAccountIcon = Cache(Image(
                  Url("https://example.com/avatarImageUrl"),
                  ImageBitmap(100, 100),
               )),
            )
         }

         CallbackWaiterPage(state)
      }

      rule.onRoot().captureRoboImage("callbackWaiterPage/success.png")
   }

   @Test
   fun screenshot_success_tooLongName() {
      rule.setContent {
         val state = rememberPageState().also {
            it.credentialAccountLoadState = CredentialAccountLoadState.Success(
               CredentialAccount(
                  id = Account.Id(
                     instanceUrl = Url("https://example.com/"),
                     Account.LocalId("account id"),
                  ),
                  Cache(Account(
                     instance = Cache(Instance(
                        Url("https://example.com/"),
                        version = "0.0.0",
                        versionCheckedTime = Instant.fromEpochMilliseconds(
                           0L
                        ),
                     )),
                     id = Account.Id(
                        instanceUrl = Url("https://example.com/"),
                        Account.LocalId("account id"),
                     ),
                     "username".repeat(10),
                     "acct".repeat(10),
                     Url("https://example.com/account_id"),
                     "displayName".repeat(10),
                     "profileNote",
                     Url("https://example.com/avatarImageUrl"),
                     Url("https://example.com/avatarStaticImageUrl"),
                     Url("https://example.com/headerImageUrl"),
                     Url("https://example.com/headerStaticImageUrl"),
                     isLocked = false,
                     profileFields = emptyList(),
                     emojisInProfile = emptyList(),
                     isBot = false,
                     isGroup = false,
                     isDiscoverable = false,
                     isNoindex = false,
                     movedTo = null,
                     isSuspended = false,
                     isLimited = false,
                     createdTime = Instant.fromEpochMilliseconds(0L),
                     lastStatusPostTime = Instant.fromEpochMilliseconds(0L),
                     statusCount = 0L,
                     followerCount = 0L,
                     followeeCount = 0L,
                  )),
                  rawProfileNote = "profileNote",
                  rawProfileFields = emptyList(),
                  defaultPostVisibility = Status.Visibility.PUBLIC,
                  defaultPostSensitivity = false,
                  defaultPostLanguage = "ja",
                  followRequestCount = 0L,
                  role = null,
               ),
               credentialAccountIcon = Cache(Image(
                  Url("https://example.com/avatarImageUrl"),
                  ImageBitmap(100, 100),
               )),
            )
         }

         CallbackWaiterPage(state)
      }

      rule.onRoot().captureRoboImage("callbackWaiterPage/success_tooLongName.png")
   }

   @Test
   fun screenshot_anim_verifyingToSuccess() {
      lateinit var state: CallbackWaiterPageState

      rule.setContent {
         state = rememberPageState().also {
            it.credentialAccountLoadState = CredentialAccountLoadState.Loading
         }

         CallbackWaiterPage(state)
      }

      rule.mainClock.autoAdvance = false

      state.credentialAccountLoadState = CredentialAccountLoadState.Success(
         CredentialAccount(
            id = Account.Id(
               instanceUrl = Url("https://example.com/"),
               Account.LocalId("account id"),
            ),
            Cache(Account(
               instance = Cache(Instance(
                  Url("https://example.com/"),
                  version = "0.0.0",
                  versionCheckedTime = Instant.fromEpochMilliseconds(
                     0L
                  ),
               )),
               id = Account.Id(
                  instanceUrl = Url("https://example.com/"),
                  Account.LocalId("account id"),
               ),
               "username",
               "acct",
               Url("https://example.com/account_id"),
               "displayName",
               "profileNote",
               Url("https://example.com/avatarImageUrl"),
               Url("https://example.com/avatarStaticImageUrl"),
               Url("https://example.com/headerImageUrl"),
               Url("https://example.com/headerStaticImageUrl"),
               isLocked = false,
               profileFields = emptyList(),
               emojisInProfile = emptyList(),
               isBot = false,
               isGroup = false,
               isDiscoverable = false,
               isNoindex = false,
               movedTo = null,
               isSuspended = false,
               isLimited = false,
               createdTime = Instant.fromEpochMilliseconds(0L),
               lastStatusPostTime = Instant.fromEpochMilliseconds(0L),
               statusCount = 0L,
               followerCount = 0L,
               followeeCount = 0L,
            )),
            rawProfileNote = "profileNote",
            rawProfileFields = emptyList(),
            defaultPostVisibility = Status.Visibility.PUBLIC,
            defaultPostSensitivity = false,
            defaultPostLanguage = "ja",
            followRequestCount = 0L,
            role = null,
         ),
         credentialAccountIcon = Cache(Image(
            Url("https://example.com/avatarImageUrl"),
            ImageBitmap(100, 100),
         )),
      )

      rule.waitForIdle()

      repeat (30) { i ->
         rule.onRoot().captureRoboImage(
            "callbackWaiterPage/verifyingToSuccess$i.png"
         )
         rule.mainClock.advanceTimeBy(32L)
      }
   }
}
