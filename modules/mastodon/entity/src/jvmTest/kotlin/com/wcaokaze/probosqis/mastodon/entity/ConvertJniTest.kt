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
                  Status.Id(
                     Url("https://example.com/instance/url"),
                     Status.LocalId("status id1")
                  ),
               ),
               Filter.FilterStatus(
                  Filter.FilterStatus.Id("filter status id2"),
                  Status.Id(
                     Url("https://example.com/instance/url"),
                     Status.LocalId("status id2")
                  ),
               ),
            ),
         ),
         keywordMatches = listOf(
            "keyword1",
         ),
         statusMatches = listOf(
            Status.Id(
               Url("https://example.com/instance/url"),
               Status.LocalId("status id1")
            ),
            Status.Id(
               Url("https://example.com/instance/url"),
               Status.LocalId("status id2")
            ),
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
                     Status.Id(
                        Url("https://example.com/instance/url"),
                        Status.LocalId("status id1")
                     ),
                  ),
                  Filter.FilterStatus(
                     Filter.FilterStatus.Id("filter status id2"),
                     Status.Id(
                        Url("https://example.com/instance/url"),
                        Status.LocalId("status id2")
                     ),
                  ),
               ),
            ),
            keywordMatches = listOf(
               "keyword1",
            ),
            statusMatches = listOf(
               Status.Id(
                  Url("https://example.com/instance/url"),
                  Status.LocalId("status id1")
               ),
               Status.Id(
                  Url("https://example.com/instance/url"),
                  Status.LocalId("status id2")
               ),
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

   // ==== Poll ================================================================

   @Test
   fun poll_toRust() {
      val instance = `poll_toRust$createInstance`()

      val noCredential = Poll.NoCredential(
         Poll.Id(instance.value.url, Poll.LocalId("poll id")),
         expireTime = LocalDateTime(2000, 1, 1, 0, 0, 0).toInstant(TimeZone.UTC),
         isExpired = true,
         allowsMultipleChoices = false,
         voteCount = 123L,
         voterCount = 45L,
         pollOptions = listOf(
            Poll.Option("title1", 1L),
            Poll.Option("title2", 2L),
            Poll.Option("title3", 3L),
         ),
         emojis = listOf(
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
      )

      `poll_toRust$assertNoCredential`(noCredential)

      val noCredentialCache = `poll_toRust$saveNoCredential`(noCredential)

      val poll = Poll(
         Poll.Id(instance.value.url, Poll.LocalId("poll id")),
         noCredentialCache,
         isVoted = true,
         votedOptions = listOf(0),
      )

      `poll_toRust$assert`(poll)
   }

   private external fun `poll_toRust$createInstance`(): Cache<Instance>

   private external fun `poll_toRust$assertNoCredential`(
      noCredentialPoll: Poll.NoCredential
   )

   private external fun `poll_toRust$saveNoCredential`(
      noCredentialPoll: Poll.NoCredential
   ): Cache<Poll.NoCredential>

   private external fun `poll_toRust$assert`(poll: Poll)

   @Test
   fun poll_nulls_toRust() {
      val instance = `poll_nulls_toRust$createInstance`()

      val noCredential = Poll.NoCredential(
         Poll.Id(instance.value.url, Poll.LocalId("poll id")),
         expireTime = null,
         isExpired = null,
         allowsMultipleChoices = null,
         voteCount = null,
         voterCount = null,
         pollOptions = listOf(
            Poll.Option(null, null),
         ),
         emojis = emptyList(),
      )

      `poll_nulls_toRust$assertNoCredential`(noCredential)

      val noCredentialCache = `poll_nulls_toRust$saveNoCredential`(noCredential)

      val poll = Poll(
         Poll.Id(instance.value.url, Poll.LocalId("poll id")),
         noCredentialCache,
         isVoted = null,
         votedOptions = listOf(),
      )

      `poll_nulls_toRust$assert`(poll)
   }

   private external fun `poll_nulls_toRust$createInstance`(): Cache<Instance>

   private external fun `poll_nulls_toRust$assertNoCredential`(
      noCredentialPoll: Poll.NoCredential
   )

   private external fun `poll_nulls_toRust$saveNoCredential`(
      noCredentialPoll: Poll.NoCredential
   ): Cache<Poll.NoCredential>

   private external fun `poll_nulls_toRust$assert`(poll: Poll)

   @Test
   fun poll_fromRust() {
      val poll = `poll_fromRust$createPoll`()
      val instance = `poll_fromRust$getInstanceCache`()

      assertEquals(
         Poll.NoCredential(
            Poll.Id(instance.value.url, Poll.LocalId("poll id")),
            expireTime = LocalDateTime(2000, 1, 1, 0, 0, 0).toInstant(TimeZone.UTC),
            isExpired = true,
            allowsMultipleChoices = false,
            voteCount = 123L,
            voterCount = 45L,
            pollOptions = listOf(
               Poll.Option("title1", 1L),
               Poll.Option("title2", 2L),
               Poll.Option("title3", 3L),
            ),
            emojis = listOf(
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
         ),
         poll.noCredential.value
      )

      assertEquals(
         Poll(
            Poll.Id(instance.value.url, Poll.LocalId("poll id")),
            poll.noCredential,
            isVoted = true,
            votedOptions = listOf(0),
         ),
         poll
      )
   }

   private external fun `poll_fromRust$createPoll`(): Poll
   private external fun `poll_fromRust$getInstanceCache`(): Cache<Instance>

   @Test
   fun poll_nulls_fromRust() {
      val poll = `poll_nulls_fromRust$createPoll`()
      val instance = `poll_nulls_fromRust$getInstanceCache`()

      assertEquals(
         Poll.NoCredential(
            Poll.Id(instance.value.url, Poll.LocalId("poll id")),
            expireTime = null,
            isExpired = null,
            allowsMultipleChoices = null,
            voteCount = null,
            voterCount = null,
            pollOptions = listOf(
               Poll.Option(null, null),
            ),
            emojis = emptyList(),
         ),
         poll.noCredential.value
      )

      assertEquals(
         Poll(
            Poll.Id(instance.value.url, Poll.LocalId("poll id")),
            poll.noCredential,
            isVoted = null,
            votedOptions = listOf(),
         ),
         poll
      )
   }

   private external fun `poll_nulls_fromRust$createPoll`(): Poll
   private external fun `poll_nulls_fromRust$getInstanceCache`(): Cache<Instance>

   // ==== PreviewCard =========================================================

   @Test
   fun previewCard_toRust() {
      val instance = `previewCard_toRust$createInstance`()

      val previewCard = PreviewCard(
         Url("https://example.com/preview/card/url"),
         "title",
         "description",
         PreviewCard.Type.LINK,
         authors = List(2) { i ->
            val account = Account(
               instance,
               Account.Id(
                  instance.value.url,
                  Account.LocalId("account id$i"),
               ),
               username = null,
               acct = null,
               url = null,
               displayName = null,
               profileNote = null,
               avatarImageUrl = null,
               avatarStaticImageUrl = null,
               headerImageUrl = null,
               headerStaticImageUrl = null,
               isLocked = null,
               profileFields = emptyList(),
               emojisInProfile = emptyList(),
               isBot = null,
               isGroup = null,
               isDiscoverable = null,
               isNoindex = null,
               isSuspended = null,
               isLimited = null,
               createdTime = null,
               lastStatusPostTime = null,
               statusCount = null,
               followerCount = null,
               followeeCount = null,
               movedTo = null,
            )

            val accountCache = `previewCard_toRust$saveAccount`(account)

            PreviewCard.Author(
               "author name",
               Url("https://example.com/author"),
               accountCache,
            )
         },
         "provider name",
         Url("https://example.com/provider/url"),
         "html",
         width = 123L,
         height = 456L,
         Url("https://example.com/image/url/3"),
         Url("https://example.com/embed/url"),
         "blurhash",
      )

      `previewCard_toRust$assert`(previewCard)
   }

   private external fun `previewCard_toRust$createInstance`(): Cache<Instance>
   private external fun `previewCard_toRust$saveAccount`(account: Account): Cache<Account>
   private external fun `previewCard_toRust$assert`(previewCard: PreviewCard)

   @Test
   fun previewCard_nulls_toRust() {
      val previewCard = PreviewCard(
         url = null,
         title = null,
         description = null,
         cardType = null,
         authors = emptyList(),
         providerName = null,
         providerUrl = null,
         html = null,
         width = null,
         height = null,
         imageUrl = null,
         embedUrl = null,
         blurhash = null,
      )

      `previewCard_nulls_toRust$assert`(previewCard)
   }

   private external fun `previewCard_nulls_toRust$assert`(previewCard: PreviewCard)

   @Test
   fun previewCard_fromRust() {
      val previewCard = `previewCard_fromRust$createPreviewCard`()

      assertEquals(
         PreviewCard(
            Url("https://example.com/preview/card/url"),
            "title",
            "description",
            PreviewCard.Type.LINK,
            previewCard.authors,
            "provider name",
            Url("https://example.com/provider/url"),
            "html",
            width = 123L,
            height = 456L,
            Url("https://example.com/image/url/3"),
            Url("https://example.com/embed/url"),
            "blurhash",
         ),
         previewCard
      )
   }

   private external fun `previewCard_fromRust$createPreviewCard`(): PreviewCard

   @Test
   fun previewCard_nulls_fromRust() {
      val previewCard = `previewCard_nulls_fromRust$createPreviewCard`()

      assertEquals(
         PreviewCard(
            url = null,
            title = null,
            description = null,
            cardType = null,
            authors = emptyList(),
            providerName = null,
            providerUrl = null,
            html = null,
            width = null,
            height = null,
            imageUrl = null,
            embedUrl = null,
            blurhash = null,
         ),
         previewCard
      )
   }

   private external fun `previewCard_nulls_fromRust$createPreviewCard`(): PreviewCard

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
