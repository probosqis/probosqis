/*
 * Copyright 2023 wcaokaze
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

package com.wcaokaze.probosqis.page.compose

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import com.wcaokaze.probosqis.cache.core.Cache
import com.wcaokaze.probosqis.cache.core.InternalCacheApi
import com.wcaokaze.probosqis.cache.core.WritableCache
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

fun <T> WritableCache<JsonObject>.elementAsWritableCache(
   key: String,
   serializer: KSerializer<T>,
   init: () -> T
): WritableCache<T> {
   return ElementWritableCache(this, key, serializer)
}

private class ElementWritableCache<T>(
   private val origin: WritableCache<JsonObject>,
   private val key: String,
   private val serializer: KSerializer<T>
) : Cache<T>, WritableCache<T> {
   @InternalCacheApi
   override val state: State<T>
      get() = mutableState

   @InternalCacheApi
   override val mutableState = object : MutableState<T> {
      override var value: T
         get() = this@ElementWritableCache.value
         set(newValue) {
            this@ElementWritableCache.value = newValue
         }

      override fun component1(): T = value
      override fun component2(): (T) -> Unit = { value = it }
   }

   override var value: T
      get() {
         val element = origin.value[key] ?: TODO()
         return Json.decodeFromJsonElement(serializer, element)
      }
      set(_) {
         TODO()
      }

   override fun asCache() = this
}
