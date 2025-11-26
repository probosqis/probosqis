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

import com.wcaokaze.probosqis.ext.panoptiqon.CacheSerializer
import com.wcaokaze.probosqis.panoptiqon.Cache
import kotlinx.serialization.Serializable

@Serializable
data class Role(
   @Serializable(CacheSerializer::class)
   val instance: Cache<Instance>,
   val id: Id?,
   val name: String?,
   val color: String?,
   val permissions: String?,
   val isHighlighted: Boolean?,
) {
   @Serializable
   @JvmInline
   value class Id(val value: String)

   val rawId: String?
      get() = id?.value
}
