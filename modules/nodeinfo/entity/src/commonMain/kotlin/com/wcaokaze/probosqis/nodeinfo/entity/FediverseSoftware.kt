/*
 * Copyright 2024 wcaokaze
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

package com.wcaokaze.probosqis.nodeinfo.entity

import com.wcaokaze.probosqis.mastodon.entity.Instance
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
sealed class FediverseSoftware {
   @Serializable
   data class Unsupported(val name: String, val version: String) : FediverseSoftware()

   @Serializable
   data class Mastodon(val instance: Instance) : FediverseSoftware() {
      constructor(
         instanceBaseUrl: String,
         version: String
      ) : this(
         Instance(
            instanceBaseUrl, version,
            versionCheckedTime = Clock.System.now()
         )
      )
   }
}
