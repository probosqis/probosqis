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

package com.wcaokaze.probosqis.page

import com.wcaokaze.probosqis.cache.core.TemporaryCacheApi
import com.wcaokaze.probosqis.cache.core.WritableCache
import com.wcaokaze.probosqis.cache.core.loadCache
import com.wcaokaze.probosqis.cache.core.saveCache
import com.wcaokaze.probosqis.page.pagestackboard.PageStackBoard
import com.wcaokaze.probosqis.page.pagestackboard.PageStackRepository
import java.io.File

class JvmPageStackBoardRepository(
   allPageSerializers: List<PageStackRepository.PageSerializer<*>>,
   directory: File
) : AbstractPageStackBoardRepository(allPageSerializers) {
   private val file = File(directory, "U61Jfjj954X8OrvZ")

   @TemporaryCacheApi
   override fun savePageStackBoard(
      pageStackBoard: PageStackBoard
   ): WritableCache<PageStackBoard> {
      return saveCache(pageStackBoard, file, json)
   }

   @TemporaryCacheApi
   override fun loadPageStackBoard(): WritableCache<PageStackBoard> {
      return loadCache(file, json)
   }
}
