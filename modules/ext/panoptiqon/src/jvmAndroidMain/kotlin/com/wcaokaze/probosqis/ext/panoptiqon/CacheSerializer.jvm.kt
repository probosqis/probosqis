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

package com.wcaokaze.probosqis.ext.panoptiqon

import com.wcaokaze.probosqis.panoptiqon.AbstractCacheSerializer
import com.wcaokaze.probosqis.panoptiqon.AbstractWritableCacheSerializer
import com.wcaokaze.probosqis.panoptiqon.Cache
import com.wcaokaze.probosqis.panoptiqon.CacheId
import com.wcaokaze.probosqis.panoptiqon.WritableCache

actual class CacheSerializer<T> : AbstractCacheSerializer<T>() {
   override fun loadCache(cacheId: CacheId): Cache<T> {
      @Suppress("UNCHECKED_CAST")
      return Panoptiqon.loadById(cacheId).asCache() as Cache<T>
   }
}

actual class WritableCacheSerializer<T> : AbstractWritableCacheSerializer<T>() {
   override fun loadCache(cacheId: CacheId): WritableCache<T> {
      @Suppress("UNCHECKED_CAST")
      return Panoptiqon.loadById(cacheId) as WritableCache<T>
   }
}
