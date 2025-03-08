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

data class CustomEmoji(
   val instance: Cache<Instance>,
   val shortcode: String,
   val imageUrl: Url,
   val staticImageUrl: Url?,
   val isVisibleInPicker: Boolean?,
   val category: String?,
) {
   constructor(
      instance: Cache<Instance>,
      shortcode: String,
      rawImageUrl: String,
      rawStaticImageUrl: String?,
      isVisibleInPicker: Boolean?,
      category: String?,
      @Suppress("UNUSED_PARAMETER")
      dummy: Unit?,
   ) : this(
      instance,
      shortcode,
      Url(rawImageUrl),
      rawStaticImageUrl?.let(::Url),
      isVisibleInPicker,
      category,
   )

   val rawImageUrl: String
      get() = imageUrl.raw

   val rawStaticImageUrl: String?
      get() = staticImageUrl?.raw

   val dummy: Unit?
      get() = null
}
