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

import com.wcaokaze.probosqis.ext.kotlin.Url
import com.wcaokaze.probosqis.ext.kotlintest.loadNativeLib
import com.wcaokaze.probosqis.panoptiqon.Cache
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class ConvertJniTest {
   init {
      loadNativeLib()
   }

   // ==== Account =============================================================

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
         Url("https://example.com/url"),
         "displayName",
         "profileNote",
         Url("https://example.com/avatar/image/url"),
         Url("https://example.com/avatar/static/image/url"),
         Url("https://example.com/header/image/url"),
         Url("https://example.com/header/static/image/url"),
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
               Url("https://example.com/image/url/1"),
               Url("https://example.com/static/image/url/1"),
               isVisibleInPicker = true,
               "category1"
            ),
            CustomEmoji(
               instance,
               "shortcode2",
               Url("https://example.com/image/url/2"),
               Url("https://example.com/static/image/url/2"),
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
            Url("https://example.com/url"),
            "displayName",
            "profileNote",
            Url("https://example.com/avatar/image/url"),
            Url("https://example.com/avatar/static/image/url"),
            Url("https://example.com/header/image/url"),
            Url("https://example.com/header/static/image/url"),
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
                  Url("https://example.com/image/url/1"),
                  Url("https://example.com/static/image/url/1"),
                  isVisibleInPicker = true,
                  "category1"
               ),
               CustomEmoji(
                  instance,
                  "shortcode2",
                  Url("https://example.com/image/url/2"),
                  Url("https://example.com/static/image/url/2"),
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

   // ==== Status.Visibility ===================================================

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

   // ==== CustomEmoji =========================================================

   @Test
   fun customEmoji_toRust() {
      val customEmoji = CustomEmoji(
         instance = `customEmoji_toRust$createInstance`(),
         "shortcode",
         Url("https://example.com/image/url"),
         Url("https://example.com/static/image/url"),
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
         Url("https://example.com/image/url"),
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
            Url("https://example.com/image/url"),
            Url("https://example.com/static/image/url"),
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
            Url("https://example.com/image/url"),
            staticImageUrl = null,
            isVisibleInPicker = null,
            category = null,
         ),
         customEmoji
      )
   }

   private external fun `customEmoji_nulls_fromRust$createCustomEmoji`(): CustomEmoji
   private external fun `customEmoji_nulls_fromRust$createInstance`(): Cache<Instance>

   // ==== FilterResult ========================================================

   @Test
   fun filterResult_toRust() {
      val filterResult = FilterResult(
         Filter(
            Filter.Id("filter id"),
            "title",
            listOf(
               Filter.Context.HOME,
               Filter.Context.PUBLIC,
               Filter.Context("illegal context"),
            ),
            expireTime = LocalDateTime(2000, 1, 1, 0, 0, 0).toInstant(TimeZone.UTC),
            Filter.Action.HIDE,
            keywords = listOf(
               Filter.Keyword(
                  Filter.Keyword.Id("filter keyword id1"),
                  "keyword1",
                  wholeWord = false,
               ),
               Filter.Keyword(
                  Filter.Keyword.Id("filter keyword id2"),
                  "keyword2",
                  wholeWord = true,
               ),
            ),
            statuses = listOf(
               Filter.FilterStatus(
                  Filter.FilterStatus.Id("filter status id1"),
                  Status.Id("status id1"),
               ),
               Filter.FilterStatus(
                  Filter.FilterStatus.Id("filter status id2"),
                  Status.Id("status id2"),
               ),
            ),
         ),
         keywordMatches = listOf(
            "keyword1",
         ),
         statusMatches = listOf(
            Status.Id("status id1"),
            Status.Id("status id2"),
         )
      )

      `filterResult_toRust$assert`(filterResult)
   }

   private external fun `filterResult_toRust$assert`(filterResult: FilterResult)

   @Test
   fun filterResult_nulls_toRust() {
      val filterResult = FilterResult(
         Filter(
            Filter.Id("filter id"),
            title = null,
            context = emptyList(),
            expireTime = null,
            filterAction = null,
            keywords = listOf(
               Filter.Keyword(
                  Filter.Keyword.Id("filter keyword id1"),
                  keyword = null,
                  wholeWord = null,
               ),
            ),
            statuses = emptyList(),
         ),
         keywordMatches = emptyList(),
         statusMatches = emptyList(),
      )

      `filterResult_nulls_toRust$assert`(filterResult)
   }

   private external fun `filterResult_nulls_toRust$assert`(filterResult: FilterResult)

   @Test
   fun filterResult_fromRust() {
      val filterResult = `filterResult_fromRust$createFilterResult`()

      assertEquals(
         FilterResult(
            Filter(
               Filter.Id("filter id"),
               "title",
               listOf(
                  Filter.Context.HOME,
                  Filter.Context.PUBLIC,
                  Filter.Context("illegal context"),
               ),
               expireTime = LocalDateTime(2000, 1, 1, 0, 0, 0).toInstant(TimeZone.UTC),
               Filter.Action.HIDE,
               keywords = listOf(
                  Filter.Keyword(
                     Filter.Keyword.Id("filter keyword id1"),
                     "keyword1",
                     wholeWord = false,
                  ),
                  Filter.Keyword(
                     Filter.Keyword.Id("filter keyword id2"),
                     "keyword2",
                     wholeWord = true,
                  ),
               ),
               statuses = listOf(
                  Filter.FilterStatus(
                     Filter.FilterStatus.Id("filter status id1"),
                     Status.Id("status id1"),
                  ),
                  Filter.FilterStatus(
                     Filter.FilterStatus.Id("filter status id2"),
                     Status.Id("status id2"),
                  ),
               ),
            ),
            keywordMatches = listOf(
               "keyword1",
            ),
            statusMatches = listOf(
               Status.Id("status id1"),
               Status.Id("status id2"),
            )
         ),
         filterResult
      )
   }

   private external fun `filterResult_fromRust$createFilterResult`(): FilterResult

   @Test
   fun filterResult_nulls_fromRust() {
      val filterResult = `filterResult_nulls_fromRust$createFilterResult`()

      assertEquals(
         FilterResult(
            Filter(
               Filter.Id("filter id"),
               title = null,
               context = emptyList(),
               expireTime = null,
               filterAction = null,
               keywords = listOf(
                  Filter.Keyword(
                     Filter.Keyword.Id("filter keyword id1"),
                     keyword = null,
                     wholeWord = null,
                  ),
               ),
               statuses = emptyList(),
            ),
            keywordMatches = emptyList(),
            statusMatches = emptyList(),
         ),
         filterResult
      )
   }

   private external fun `filterResult_nulls_fromRust$createFilterResult`(): FilterResult

   // ==== MediaAttachment =====================================================

   @Test
   fun mediaAttachment_image_toRust() {
      val mediaAttachment = MediaAttachment(
         MediaAttachment.Id("media attachment id"),
         Url("https://example.com/"),
         Url("https://example.com/preview"),
         Url("https://example.com/remote"),
         MediaAttachment.Metadata.Image(
            originalSize = MediaAttachment.ImageSize(1000L, 2000L),
            smallSize = MediaAttachment.ImageSize(100L, 200L),
            focus = MediaAttachment.ImageFocus(0.1, 0.2),
         ),
         "description",
         "blurhash",
      )

      `mediaAttachment_image_toRust$assert`(mediaAttachment)
   }

   private external fun `mediaAttachment_image_toRust$assert`(mediaAttachment: MediaAttachment)

   @Test
   fun mediaAttachment_video_toRust() {
      val mediaAttachment = MediaAttachment(
         MediaAttachment.Id("media attachment id"),
         Url("https://example.com/"),
         Url("https://example.com/preview"),
         Url("https://example.com/remote"),
         MediaAttachment.Metadata.Video(
            originalSize = MediaAttachment.VideoSize(
               width = 1000L,
               height = 2000L,
               "frameRate",
               duration = 1.23.seconds,
               bitrate = 123L,
            ),
            smallSize = MediaAttachment.ImageSize(100L, 200L),
            "length",
            fps = 42L,
            "audioEncode",
            "audioBitrate",
            "audioChannels",
         ),
         "description",
         "blurhash",
      )

      `mediaAttachment_video_toRust$assert`(mediaAttachment)
   }

   private external fun `mediaAttachment_video_toRust$assert`(mediaAttachment: MediaAttachment)

   @Test
   fun mediaAttachment_gifv_toRust() {
      val mediaAttachment = MediaAttachment(
         MediaAttachment.Id("media attachment id"),
         Url("https://example.com/"),
         Url("https://example.com/preview"),
         Url("https://example.com/remote"),
         MediaAttachment.Metadata.Gifv(
            originalSize = MediaAttachment.VideoSize(
               width = 1000L,
               height = 2000L,
               "frameRate",
               duration = 1.23.seconds,
               bitrate = 123L,
            ),
            smallSize = MediaAttachment.ImageSize(100L, 200L),
            "length",
            fps = 42L,
         ),
         "description",
         "blurhash",
      )

      `mediaAttachment_gifv_toRust$assert`(mediaAttachment)
   }

   private external fun `mediaAttachment_gifv_toRust$assert`(mediaAttachment: MediaAttachment)

   @Test
   fun mediaAttachment_audio_toRust() {
      val mediaAttachment = MediaAttachment(
         MediaAttachment.Id("media attachment id"),
         Url("https://example.com/"),
         Url("https://example.com/preview"),
         Url("https://example.com/remote"),
         MediaAttachment.Metadata.Audio(
            originalSize = MediaAttachment.AudioSize(
               duration = 1.23.seconds,
               bitrate = 123L,
            ),
            "length",
            "audioEncode",
            "audioBitrate",
            "audioChannels",
         ),
         "description",
         "blurhash",
      )

      `mediaAttachment_audio_toRust$assert`(mediaAttachment)
   }

   private external fun `mediaAttachment_audio_toRust$assert`(mediaAttachment: MediaAttachment)

   @Test
   fun mediaAttachment_nulls_toRust() {
      val mediaAttachment = MediaAttachment(
         MediaAttachment.Id("media attachment id"),
         url = null,
         previewUrl = null,
         remoteUrl = null,
         metadata = null,
         description = null,
         blurhash = null,
      )

      `mediaAttachment_nulls_toRust$assert`(mediaAttachment)
   }

   private external fun `mediaAttachment_nulls_toRust$assert`(mediaAttachment: MediaAttachment)

   @Test
   fun mediaAttachment_image_nulls_toRust() {
      val mediaAttachment = MediaAttachment(
         MediaAttachment.Id("media attachment id"),
         url = null,
         previewUrl = null,
         remoteUrl = null,
         metadata = MediaAttachment.Metadata.Image(
            originalSize = null,
            smallSize = null,
            focus = null,
         ),
         description = null,
         blurhash = null,
      )

      `mediaAttachment_image_nulls_toRust$assert`(mediaAttachment)
   }

   private external fun `mediaAttachment_image_nulls_toRust$assert`(mediaAttachment: MediaAttachment)

   @Test
   fun mediaAttachment_video_nulls_toRust() {
      val mediaAttachment = MediaAttachment(
         MediaAttachment.Id("media attachment id"),
         url = null,
         previewUrl = null,
         remoteUrl = null,
         metadata = MediaAttachment.Metadata.Video(
            originalSize = null,
            smallSize = null,
            length = null,
            fps = null,
            audioEncode = null,
            audioBitrate = null,
            audioChannels = null,
         ),
         description = null,
         blurhash = null,
      )

      `mediaAttachment_video_nulls_toRust$assert`(mediaAttachment)
   }

   private external fun `mediaAttachment_video_nulls_toRust$assert`(mediaAttachment: MediaAttachment)

   @Test
   fun mediaAttachment_gifv_nulls_toRust() {
      val mediaAttachment = MediaAttachment(
         MediaAttachment.Id("media attachment id"),
         url = null,
         previewUrl = null,
         remoteUrl = null,
         metadata = MediaAttachment.Metadata.Gifv(
            originalSize = null,
            smallSize = null,
            length = null,
            fps = null,
         ),
         description = null,
         blurhash = null,
      )

      `mediaAttachment_gifv_nulls_toRust$assert`(mediaAttachment)
   }

   private external fun `mediaAttachment_gifv_nulls_toRust$assert`(mediaAttachment: MediaAttachment)

   @Test
   fun mediaAttachment_audio_nulls_toRust() {
      val mediaAttachment = MediaAttachment(
         MediaAttachment.Id("media attachment id"),
         url = null,
         previewUrl = null,
         remoteUrl = null,
         metadata = MediaAttachment.Metadata.Audio(
            originalSize = null,
            length = null,
            audioEncode = null,
            audioBitrate = null,
            audioChannels = null,
         ),
         description = null,
         blurhash = null,
      )

      `mediaAttachment_audio_nulls_toRust$assert`(mediaAttachment)
   }

   private external fun `mediaAttachment_audio_nulls_toRust$assert`(mediaAttachment: MediaAttachment)

   @Test
   fun mediaAttachment_image_fromRust() {
      val mediaAttachment = `mediaAttachment_image_fromRust$createMediaAttachment`()

      assertEquals(
         MediaAttachment(
            MediaAttachment.Id("media attachment id"),
            Url("https://example.com/"),
            Url("https://example.com/preview"),
            Url("https://example.com/remote"),
            MediaAttachment.Metadata.Image(
               originalSize = MediaAttachment.ImageSize(1000L, 2000L),
               smallSize = MediaAttachment.ImageSize(100L, 200L),
               focus = MediaAttachment.ImageFocus(0.1, 0.2),
            ),
            "description",
            "blurhash",
         ),
         mediaAttachment
      )
   }

   private external fun `mediaAttachment_image_fromRust$createMediaAttachment`(): MediaAttachment

   @Test
   fun mediaAttachment_video_fromRust() {
      val mediaAttachment = `mediaAttachment_video_fromRust$createMediaAttachment`()

      assertEquals(
         MediaAttachment(
            MediaAttachment.Id("media attachment id"),
            Url("https://example.com/"),
            Url("https://example.com/preview"),
            Url("https://example.com/remote"),
            MediaAttachment.Metadata.Video(
               originalSize = MediaAttachment.VideoSize(
                  width = 1000L,
                  height = 2000L,
                  "frameRate",
                  duration = 1.23.seconds,
                  bitrate = 123L,
               ),
               smallSize = MediaAttachment.ImageSize(100L, 200L),
               "length",
               fps = 42L,
               "audioEncode",
               "audioBitrate",
               "audioChannels",
            ),
            "description",
            "blurhash",
         ),
         mediaAttachment
      )
   }

   private external fun `mediaAttachment_video_fromRust$createMediaAttachment`(): MediaAttachment

   @Test
   fun mediaAttachment_gifv_fromRust() {
      val mediaAttachment = `mediaAttachment_gifv_fromRust$createMediaAttachment`()

      assertEquals(
         MediaAttachment(
            MediaAttachment.Id("media attachment id"),
            Url("https://example.com/"),
            Url("https://example.com/preview"),
            Url("https://example.com/remote"),
            MediaAttachment.Metadata.Gifv(
               originalSize = MediaAttachment.VideoSize(
                  width = 1000L,
                  height = 2000L,
                  "frameRate",
                  duration = 1.23.seconds,
                  bitrate = 123L,
               ),
               smallSize = MediaAttachment.ImageSize(100L, 200L),
               "length",
               fps = 42L,
            ),
            "description",
            "blurhash",
         ),
         mediaAttachment
      )
   }

   private external fun `mediaAttachment_gifv_fromRust$createMediaAttachment`(): MediaAttachment

   @Test
   fun mediaAttachment_audio_fromRust() {
      val mediaAttachment = `mediaAttachment_audio_fromRust$createMediaAttachment`()

      assertEquals(
         MediaAttachment(
            MediaAttachment.Id("media attachment id"),
            Url("https://example.com/"),
            Url("https://example.com/preview"),
            Url("https://example.com/remote"),
            MediaAttachment.Metadata.Audio(
               originalSize = MediaAttachment.AudioSize(
                  duration = 1.23.seconds,
                  bitrate = 123L,
               ),
               "length",
               "audioEncode",
               "audioBitrate",
               "audioChannels",
            ),
            "description",
            "blurhash",
         ),
         mediaAttachment
      )
   }

   private external fun `mediaAttachment_audio_fromRust$createMediaAttachment`(): MediaAttachment

   @Test
   fun mediaAttachment_nulls_fromRust() {
      val mediaAttachment = `mediaAttachment_nulls_fromRust$createMediaAttachment`()

      assertEquals(
         MediaAttachment(
            MediaAttachment.Id("media attachment id"),
            url = null,
            previewUrl = null,
            remoteUrl = null,
            metadata = null,
            description = null,
            blurhash = null,
         ),
         mediaAttachment
      )
   }

   private external fun `mediaAttachment_nulls_fromRust$createMediaAttachment`(): MediaAttachment

   @Test
   fun mediaAttachment_image_nulls_fromRust() {
      val mediaAttachment = `mediaAttachment_image_nulls_fromRust$createMediaAttachment`()

      assertEquals(
         MediaAttachment(
            MediaAttachment.Id("media attachment id"),
            url = null,
            previewUrl = null,
            remoteUrl = null,
            metadata = MediaAttachment.Metadata.Image(
               originalSize = null,
               smallSize = null,
               focus = null,
            ),
            description = null,
            blurhash = null,
         ),
         mediaAttachment
      )
   }

   private external fun `mediaAttachment_image_nulls_fromRust$createMediaAttachment`(): MediaAttachment

   @Test
   fun mediaAttachment_video_nulls_fromRust() {
      val mediaAttachment = `mediaAttachment_video_nulls_fromRust$createMediaAttachment`()

      assertEquals(
         MediaAttachment(
            MediaAttachment.Id("media attachment id"),
            url = null,
            previewUrl = null,
            remoteUrl = null,
            metadata = MediaAttachment.Metadata.Video(
               originalSize = null,
               smallSize = null,
               length = null,
               fps = null,
               audioEncode = null,
               audioBitrate = null,
               audioChannels = null,
            ),
            description = null,
            blurhash = null,
         ),
         mediaAttachment
      )
   }

   private external fun `mediaAttachment_video_nulls_fromRust$createMediaAttachment`(): MediaAttachment

   @Test
   fun mediaAttachment_gifv_nulls_fromRust() {
      val mediaAttachment = `mediaAttachment_gifv_nulls_fromRust$createMediaAttachment`()

      assertEquals(
         MediaAttachment(
            MediaAttachment.Id("media attachment id"),
            url = null,
            previewUrl = null,
            remoteUrl = null,
            metadata = MediaAttachment.Metadata.Gifv(
               originalSize = null,
               smallSize = null,
               length = null,
               fps = null,
            ),
            description = null,
            blurhash = null,
         ),
         mediaAttachment
      )
   }

   private external fun `mediaAttachment_gifv_nulls_fromRust$createMediaAttachment`(): MediaAttachment

   @Test
   fun mediaAttachment_audio_nulls_fromRust() {
      val mediaAttachment = `mediaAttachment_audio_nulls_fromRust$createMediaAttachment`()

      assertEquals(
         MediaAttachment(
            MediaAttachment.Id("media attachment id"),
            url = null,
            previewUrl = null,
            remoteUrl = null,
            metadata = MediaAttachment.Metadata.Audio(
               originalSize = null,
               length = null,
               audioEncode = null,
               audioBitrate = null,
               audioChannels = null,
            ),
            description = null,
            blurhash = null,
         ),
         mediaAttachment
      )
   }

   private external fun `mediaAttachment_audio_nulls_fromRust$createMediaAttachment`(): MediaAttachment

   // ==== Role ================================================================

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
