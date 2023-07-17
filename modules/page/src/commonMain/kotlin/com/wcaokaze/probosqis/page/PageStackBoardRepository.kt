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

import com.wcaokaze.probosqis.cache.core.WritableCache
import com.wcaokaze.probosqis.page.pagestackboard.AbstractPageStackRepository
import com.wcaokaze.probosqis.page.pagestackboard.PageStackBoard
import com.wcaokaze.probosqis.page.pagestackboard.PageStackRepository

interface PageStackBoardRepository {
   fun savePageStackBoard(pageStackBoard: PageStackBoard): WritableCache<PageStackBoard>
   fun loadPageStackBoard(): WritableCache<PageStackBoard>
}

abstract class AbstractPageStackBoardRepository
   internal constructor(
      allPageSerializers: List<PageStackRepository.PageSerializer<*>>
   )
   : PageStackBoardRepository
{
   private val pageRepo = object : AbstractPageStackRepository(allPageSerializers) {
      val _json = json

      override fun loadPageStack(id: PageStack.Id): WritableCache<PageStack> {
         throw NotImplementedError()
      }

      override fun savePageStack(pageStack: PageStack): WritableCache<PageStack> {
         throw NotImplementedError()
      }

      override fun deleteAllPageStacks() {
         throw NotImplementedError()
      }
   }

   protected val json = pageRepo._json
}
