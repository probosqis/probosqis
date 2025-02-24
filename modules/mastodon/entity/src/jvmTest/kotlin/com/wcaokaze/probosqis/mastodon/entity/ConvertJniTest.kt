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

package com.wcaokaze.probosqis.mastodon.entity

import com.wcaokaze.probosqis.ext.kotlintest.loadNativeLib
import com.wcaokaze.probosqis.panoptiqon.Cache
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals

class ConvertJniTest {
   init {
      loadNativeLib()
   }

   @Test
   fun account_toRust() {
      val instance = `account_toRust$createInstance`()

      val account = Account(
         instance,
         Account.Id(
            instance.value.url,
            Account.LocalId("id"),
         ),
         "username",
         "acct",
         "https://example.com/url",
         "displayName",
         "profileNote",
         "https://example.com/avatar/image/url",
         "https://example.com/avatar/static/image/url",
         "https://example.com/header/image/url",
         "https://example.com/header/static/image/url",
         isLocked = true,
         listOf(
            Account.ProfileField("name1", "value1", LocalDateTime(2000, 1, 1, 0, 0, 0).toInstant(TimeZone.UTC)),
            Account.ProfileField("name2", "value2", LocalDateTime(2000, 1, 2, 0, 0, 0).toInstant(TimeZone.UTC)),
            Account.ProfileField("name3", "value3", LocalDateTime(2000, 1, 3, 0, 0, 0).toInstant(TimeZone.UTC)),
            Account.ProfileField("name4", "value4", LocalDateTime(2000, 1, 4, 0, 0, 0).toInstant(TimeZone.UTC)),
            Account.ProfileField("name5", "value5", LocalDateTime(2000, 1, 5, 0, 0, 0).toInstant(TimeZone.UTC)),
         ),
         listOf(
            CustomEmoji(
               instance,
               "shortcode1",
               "https://example.com/image/url/1",
               "https://example.com/static/image/url/1",
               isVisibleInPicker = true,
               "category1"
            ),
            CustomEmoji(
               instance,
               "shortcode2",
               "https://example.com/image/url/2",
               "https://example.com/static/image/url/2",
               isVisibleInPicker = false,
               "category2"
            ),
         ),
         isBot = true,
         isGroup = true,
         isDiscoverable = true,
         isNoindex = true,
         isSuspended = true,
         isLimited = true,
         createdTime = LocalDateTime(2000, 1, 1, 0, 0, 0).toInstant(TimeZone.UTC),
         lastStatusPostTime = LocalDateTime(2000, 2, 1, 0, 0, 0).toInstant(TimeZone.UTC),
         statusCount = 10000,
         followerCount = 100,
         followeeCount = 200,
         movedTo = `account_toRust$createMovedTo`(),
      )

      `account_toRust$assert`(account)
   }

   private external fun `account_toRust$createInstance`(): Cache<Instance>
   private external fun `account_toRust$createMovedTo`(): Cache<Account>
   private external fun `account_toRust$assert`(account: Account)

   @Test
   fun account_fromRust() {
      val account = `account_fromRust$createAccount`()
      val instance = `account_fromRust$createInstance`()

      assertEquals(
         Account(
            instance,
            Account.Id(
               instance.value.url,
               Account.LocalId("id"),
            ),
            "username",
            "acct",
            "https://example.com/url",
            "displayName",
            "profileNote",
            "https://example.com/avatar/image/url",
            "https://example.com/avatar/static/image/url",
            "https://example.com/header/image/url",
            "https://example.com/header/static/image/url",
            isLocked = true,
            listOf(
               Account.ProfileField("name1", "value1", LocalDateTime(2000, 1, 1, 0, 0, 0).toInstant(TimeZone.UTC)),
               Account.ProfileField("name2", "value2", LocalDateTime(2000, 1, 2, 0, 0, 0).toInstant(TimeZone.UTC)),
               Account.ProfileField("name3", "value3", LocalDateTime(2000, 1, 3, 0, 0, 0).toInstant(TimeZone.UTC)),
               Account.ProfileField("name4", "value4", LocalDateTime(2000, 1, 4, 0, 0, 0).toInstant(TimeZone.UTC)),
               Account.ProfileField("name5", "value5", LocalDateTime(2000, 1, 5, 0, 0, 0).toInstant(TimeZone.UTC)),
            ),
            listOf(
               CustomEmoji(
                  instance,
                  "shortcode1",
                  "https://example.com/image/url/1",
                  "https://example.com/static/image/url/1",
                  isVisibleInPicker = true,
                  "category1"
               ),
               CustomEmoji(
                  instance,
                  "shortcode2",
                  "https://example.com/image/url/2",
                  "https://example.com/static/image/url/2",
                  isVisibleInPicker = false,
                  "category2"
               ),
            ),
            isBot = true,
            isGroup = true,
            isDiscoverable = true,
            isNoindex = true,
            isSuspended = true,
            isLimited = true,
            createdTime = LocalDateTime(2000, 1, 1, 0, 0, 0).toInstant(TimeZone.UTC),
            lastStatusPostTime = LocalDateTime(2000, 2, 1, 0, 0, 0).toInstant(TimeZone.UTC),
            statusCount = 10000,
            followerCount = 100,
            followeeCount = 200,
            movedTo = `account_fromRust$createMovedTo`(),
         ),
         account
      )
   }

   private external fun `account_fromRust$createAccount`(): Account
   private external fun `account_fromRust$createInstance`(): Cache<Instance>
   private external fun `account_fromRust$createMovedTo`(): Cache<Account>

   @Test
   fun statusVisibility_toRust() {
      val statusVisibility = Status.Visibility.UNLISTED
      `statusVisibility_toRust$assert`(statusVisibility)
   }

   private external fun `statusVisibility_toRust$assert`(statusVisibility: Status.Visibility)

   @Test
   fun statusVisibility_fromRust() {
      val statusVisibility = `statusVisibility_fromRust$createStatusVisibility`()

      assertEquals(
         Status.Visibility.UNLISTED,
         statusVisibility
      )
   }

   private external fun `statusVisibility_fromRust$createStatusVisibility`(): Status.Visibility

   @Test
   fun customEmoji_toRust() {
      val customEmoji = CustomEmoji(
         instance = `customEmoji_toRust$createInstance`(),
         "shortcode",
         "https://example.com/image/url",
         "https://example.com/static/image/url",
         isVisibleInPicker = true,
         "category",
      )

      `customEmoji_toRust$assert`(customEmoji)
   }

   private external fun `customEmoji_toRust$createInstance`(): Cache<Instance>
   private external fun `customEmoji_toRust$assert`(customEmoji: CustomEmoji)

   @Test
   fun customEmoji_nulls_toRust() {
      val customEmoji = CustomEmoji(
         instance = `customEmoji_nulls_toRust$createInstance`(),
         "shortcode",
         "https://example.com/image/url",
         staticImageUrl = null,
         isVisibleInPicker = null,
         category = null,
      )

      `customEmoji_nulls_toRust$assert`(customEmoji)
   }

   private external fun `customEmoji_nulls_toRust$createInstance`(): Cache<Instance>
   private external fun `customEmoji_nulls_toRust$assert`(customEmoji: CustomEmoji)

   @Test
   fun customEmoji_fromRust() {
      val customEmoji = `customEmoji_fromRust$createCustomEmoji`()

      assertEquals(
         CustomEmoji(
            instance = `customEmoji_fromRust$createInstance`(),
            "shortcode",
            "https://example.com/image/url",
            "https://example.com/static/image/url",
            isVisibleInPicker = true,
            "category",
         ),
         customEmoji
      )
   }

   private external fun `customEmoji_fromRust$createCustomEmoji`(): CustomEmoji
   private external fun `customEmoji_fromRust$createInstance`(): Cache<Instance>

   @Test
   fun customEmoji_nulls_fromRust() {
      val customEmoji = `customEmoji_nulls_fromRust$createCustomEmoji`()

      assertEquals(
         CustomEmoji(
            instance = `customEmoji_nulls_fromRust$createInstance`(),
            "shortcode",
            "https://example.com/image/url",
            staticImageUrl = null,
            isVisibleInPicker = null,
            category = null,
         ),
         customEmoji
      )
   }

   private external fun `customEmoji_nulls_fromRust$createCustomEmoji`(): CustomEmoji
   private external fun `customEmoji_nulls_fromRust$createInstance`(): Cache<Instance>

   @Test
   fun role_toRust() {
      val role = Role(
         instance = `role_toRust$createInstance`(),
         Role.Id("id"),
         "name",
         "color",
         "permissions",
         isHighlighted = true,
      )

      `role_toRust$assert`(role)
   }

   private external fun `role_toRust$createInstance`(): Cache<Instance>
   private external fun `role_toRust$assert`(role: Role)

   @Test
   fun role_nulls_toRust() {
      val role = Role(
         instance = `role_nulls_toRust$createInstance`(),
         id = null,
         name = null,
         color = null,
         permissions = null,
         isHighlighted = null,
      )

      `role_nulls_toRust$assert`(role)
   }

   private external fun `role_nulls_toRust$createInstance`(): Cache<Instance>
   private external fun `role_nulls_toRust$assert`(role: Role)

   @Test
   fun role_fromRust() {
      val role = `role_fromRust$createRole`()

      assertEquals(
         Role(
            instance = `role_fromRust$createInstance`(),
            Role.Id("id"),
            "name",
            "color",
            "permissions",
            isHighlighted = true,
         ),
         role
      )
   }

   private external fun `role_fromRust$createRole`(): Role
   private external fun `role_fromRust$createInstance`(): Cache<Instance>

   @Test
   fun role_nulls_fromRust() {
      val role = `role_nulls_fromRust$createRole`()

      assertEquals(
         Role(
            instance = `role_nulls_fromRust$createInstance`(),
            id = null,
            name = null,
            color = null,
            permissions = null,
            isHighlighted = null,
         ),
         role
      )
   }

   private external fun `role_nulls_fromRust$createRole`(): Role
   private external fun `role_nulls_fromRust$createInstance`(): Cache<Instance>
}
