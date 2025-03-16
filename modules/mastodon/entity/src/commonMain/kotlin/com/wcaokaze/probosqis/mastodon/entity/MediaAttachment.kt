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
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

data class MediaAttachment(
   val id: Id,
   val url: Url?,
   val previewUrl: Url?,
   val remoteUrl: Url?,
   val metadata: Metadata?,
   val description: String?,
   val blurhash: String?,
) {
   constructor(
      rawId: String,
      rawUrl: String?,
      rawPreviewUrl: String?,
      rawRemoteUrl: String?,
      metadata: Metadata?,
      description: String?,
      blurhash: String?,
      @Suppress("UNUSED_PARAMETER")
      dummy: Unit?,
   ) : this(
      Id(rawId),
      rawUrl?.let(::Url),
      rawPreviewUrl?.let(::Url),
      rawRemoteUrl?.let(::Url),
      metadata,
      description,
      blurhash,
   )

   val rawId: String
      get() = id.raw

   val rawUrl: String?
      get() = url?.raw

   val rawPreviewUrl: String?
      get() = previewUrl?.raw

   val rawRemoteUrl: String?
      get() = remoteUrl?.raw

   val dummy: Unit?
      get() = null

   @JvmInline
   value class Id(val raw: String)

   sealed class Metadata {
      data class Image(
         val originalSize: ImageSize?,
         val smallSize: ImageSize?,
         val focus: ImageFocus?,
      ) : Metadata()

      data class Video(
         val originalSize: VideoSize?,
         val smallSize: ImageSize?,
         val length: String?,
         val fps: Long?,
         val audioEncode: String?,
         val audioBitrate: String?,
         val audioChannels: String?,
      ) : Metadata()

      data class Gifv(
         val originalSize: VideoSize?,
         val smallSize: ImageSize?,
         val length: String?,
         val fps: Long?,
      ) : Metadata()

      data class Audio(
         val originalSize: AudioSize?,
         val length: String?,
         val audioEncode: String?,
         val audioBitrate: String?,
         val audioChannels: String?,
      ) : Metadata()
   }

   data class ImageSize(
      val width: Long,
      val height: Long,
   )

   data class ImageFocus(
      val x: Double,
      val y: Double,
   )

   data class VideoSize(
      val width: Long?,
      val height: Long?,
      val frameRate: String?,
      val duration: Duration?,
      val bitrate: Long?,
   ) {
      constructor(
         width: Long?,
         height: Long?,
         frameRate: String?,
         durationSecs: Double?,
         bitrate: Long?,
         @Suppress("UNUSED_PARAMETER")
         dummy: Unit?,
      ) : this(
         width,
         height,
         frameRate,
         durationSecs?.toDuration(DurationUnit.SECONDS),
         bitrate,
      )

      val durationSecs: Double?
         get() = duration?.toDouble(DurationUnit.SECONDS)

      val dummy: Unit?
         get() = null
   }

   data class AudioSize(
      val duration: Duration?,
      val bitrate: Long?,
   ) {
      constructor(
         durationSecs: Double?,
         bitrate: Long?,
         @Suppress("UNUSED_PARAMETER")
         dummy: Unit?,
      ) : this(
         durationSecs?.toDuration(DurationUnit.SECONDS),
         bitrate,
      )

      val durationSecs: Double?
         get() = duration?.toDouble(DurationUnit.SECONDS)

      val dummy: Unit?
         get() = null
   }
}
