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

class Status {
   data class Id(
      val instanceUrl: Url,
      val local: LocalId
   ) {
      constructor(
         rawInstanceUrl: String,
         rawLocalId: String,
         @Suppress("UNUSED_PARAMETER")
         dummy: Unit?
      ) : this(
         Url(rawInstanceUrl),
         LocalId(rawLocalId)
      )

      val rawInstanceUrl: String
         get() = instanceUrl.raw

      val rawLocalId: String
         get() = local.raw

      val dummy: Unit?
         get() = null
   }

   @JvmInline
   value class LocalId(val raw: String)

   enum class Visibility(val value: Int) {
      PUBLIC  (0),
      UNLISTED(1),
      PRIVATE (2),
      DIRECT  (3),
      ;
      companion object {
         @JvmStatic
         fun fromInt(value: Int) = entries.first { it.value == value }
      }
   }
}
