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

import java.io.File
import java.io.IOException

private val testDir = File(".pageStackRepositoryTest")

actual fun createPageStackRepository(
   allPageSerializers: List<PageStackRepository.PageSerializer<*>>
): PageStackRepository {
   if (testDir.exists()) {
      if (!testDir.deleteRecursively()) { throw IOException() }
   }
   if (!testDir.mkdir()) { throw IOException() }

   return JvmPageStackRepository(allPageSerializers, testDir)
}

actual fun createPageStackBoardRepository(
   pageStackRepository: PageStackRepository
): PageStackBoardRepository {
   return JvmPageStackBoardRepository(pageStackRepository, testDir)
}

actual fun deletePageStackRepository(
   pageStackRepository: PageStackRepository
) {
   testDir.deleteRecursively()
}

actual fun deleteRepositories(
   pageStackRepository: PageStackRepository,
   pageStackBoardRepository: PageStackBoardRepository
) {
   testDir.deleteRecursively()
}