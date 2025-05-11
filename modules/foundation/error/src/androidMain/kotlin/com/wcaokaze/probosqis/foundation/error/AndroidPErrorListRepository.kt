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

package com.wcaokaze.probosqis.foundation.error

import android.content.Context
import com.wcaokaze.probosqis.app.pagedeck.PageStackRepository
import com.wcaokaze.probosqis.panoptiqon.TemporaryCacheApi
import com.wcaokaze.probosqis.panoptiqon.WritableCache
import com.wcaokaze.probosqis.panoptiqon.loadCache
import com.wcaokaze.probosqis.panoptiqon.saveCache
import java.io.File
import java.io.IOException

class AndroidPErrorListRepository(
   context: Context,
   allErrorSerializers: List<PErrorListRepository.PErrorSerializer<*>>,
   allPageSerializers: List<PageStackRepository.PageSerializer<*>>
) : AbstractPErrorListRepository(allErrorSerializers, allPageSerializers) {
   private val file = File(context.filesDir, "MrVA3boZqIa78Man")

   /** @throws IOException */
   @TemporaryCacheApi
   override fun saveErrorList(
      errorList: List<RaisedError>
   ): WritableCache<List<RaisedError>> {
      return saveCache(errorList, file, json)
   }

   /** @throws IOException */
   @TemporaryCacheApi
   override fun loadErrorList(): WritableCache<List<RaisedError>> {
      return loadCache(file, json)
   }
}
