/*
 * Copyright 2023-2025 wcaokaze
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

package com.wcaokaze.probosqis.app.pagedeck

import com.wcaokaze.probosqis.panoptiqon.Repository
import com.wcaokaze.probosqis.panoptiqon.WritableCache
import java.io.File

class AndroidPageDeckRepository(
   appDataDir: File,
   pageStackRepository: PageStackRepository
) : AbstractPageDeckRepository(pageStackRepository) {
   private val panoptiqonRepository
      = createPanoptiqonRepository(appDataDir.absolutePath)

   override fun savePanoptiqon(
      deck: SerializedPageDeck
   ): WritableCache<SerializedPageDeck> {
      return panoptiqonRepository.save(deck)
   }

   override fun loadPanoptiqon(): WritableCache<SerializedPageDeck> {
      return panoptiqonRepository.load(Unit)
   }

   private external fun createPanoptiqonRepository(
      dataDirPath: String
   ): Repository<Unit, SerializedPageDeck>
}
