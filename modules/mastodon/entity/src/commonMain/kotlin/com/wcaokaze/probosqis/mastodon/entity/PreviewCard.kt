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
import com.wcaokaze.probosqis.panoptiqon.Cache

data class PreviewCard(
   val url: Url?,
   val title: String?,
   val description: String?,
   val cardType: Type?,
   val authors: List<Author>,
   val providerName: String?,
   val providerUrl: Url?,
   val html: String?,
   val width: Long?,
   val height: Long?,
   val imageUrl: Url?,
   val embedUrl: Url?,
   val blurhash: String?,
) {
   constructor(
      rawUrl: String?,
      title: String?,
      description: String?,
      rawCardType: String?,
      authors: List<Author>,
      providerName: String?,
      rawProviderUrl: String?,
      html: String?,
      width: Long?,
      height: Long?,
      rawImageUrl: String?,
      rawEmbedUrl: String?,
      blurhash: String?,
      @Suppress("UNUSED_PARAMETER")
      dummy: Unit?
   ) : this(
      rawUrl?.let(::Url),
      title,
      description,
      rawCardType?.let(::Type),
      authors,
      providerName,
      rawProviderUrl?.let(::Url),
      html,
      width,
      height,
      rawImageUrl?.let(::Url),
      rawEmbedUrl?.let(::Url),
      blurhash,
   )

   val rawUrl: String?
      get() = url?.raw

   val rawCardType: String?
      get() = cardType?.raw

   val rawProviderUrl: String?
      get() = providerUrl?.raw

   val rawImageUrl: String?
      get() = imageUrl?.raw

   val rawEmbedUrl: String?
      get() = embedUrl?.raw

   val dummy: Unit?
      get() = null

   @JvmInline
   value class Type(val raw: String) {
      companion object {
         val LINK  = Type("link")
         val PHOTO = Type("photo")
         val VIDEO = Type("video")
         val RICH  = Type("rich")
      }
   }

   data class Author(
      val name: String?,
      val url: Url?,
      val account: Cache<Account>?,
   ) {
      constructor(
         name: String?,
         rawUrl: String?,
         account: Cache<Account>?,
         @Suppress("UNUSED_PARAMETER")
         dummy: Unit?
      ) : this(
         name,
         rawUrl?.let(::Url),
         account,
      )

      val rawUrl: String?
         get() = url?.raw

      val dummy: Unit?
         get() = null
   }
}
