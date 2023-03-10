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

import com.wcaokaze.probosqis.page.core.Page
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PageMetadataCollectionTest {
   private class PageA : Page()
   private class PageB : Page()
   private class PageC : Page()

   @Test
   fun getMetadata() {
      val metadataCollection = PageMetadataCollection(
         listOf(
            pageComposable<PageA> {},
            pageComposable<PageB> {},
         )
      )

      val pageA = PageA()
      val metadataA = metadataCollection[pageA]
      assertNotNull(metadataA)
      assertEquals(metadataA.pageClass, PageA::class)

      val pageB = PageB()
      val metadataB = metadataCollection[pageB]
      assertNotNull(metadataB)
      assertEquals(metadataB.pageClass, PageB::class)

      val pageC = PageC()
      val metadataC = metadataCollection[pageC]
      assertNull(metadataC)
   }
}
