/*
 * Copyright 2024-2025 wcaokaze
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
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class Application(
   @Contextual
   val instance: Cache<Instance>,
   val name: String,
   val website: Url?,
   val clientId: String?,
   val clientSecret: String?,
) {
   constructor(
      instance: Cache<Instance>,
      name: String,
      rawWebsite: String?,
      clientId: String?,
      clientSecret: String?,
      @Suppress("UNUSED_PARAMETER")
      dummy: Unit?,
   ) : this(
      instance,
      name,
      rawWebsite?.let(::Url),
      clientId,
      clientSecret,
   )

   val rawWebsite: String?
      get() = website?.raw

   val dummy: Unit?
      get() = null
}
