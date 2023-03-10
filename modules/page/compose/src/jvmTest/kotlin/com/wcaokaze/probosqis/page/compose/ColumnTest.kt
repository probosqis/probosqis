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

import androidx.compose.runtime.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.wcaokaze.probosqis.page.core.Column
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(JUnit4::class)
class ColumnTest {
   @get:Rule
   val rule = createComposeRule()

   @Test
   fun onlyForefrontComposableIsCalled() {
      val page1 = SpyPage()
      val page2 = SpyPage()

      var column = Column(page1)
      column = column.added(page2)

      val metadataCollection = PageMetadataCollection(
         listOf(
            pageComposable<SpyPage> { SpyPage(it) },
         )
      )

      rule.setContent {
         Column(column, metadataCollection)
      }
      rule.waitForIdle()

      assertEquals(0, page1.recompositionCount)
      assertEquals(1, page2.recompositionCount)
   }

   @Test
   fun pageTransition() {
      val page1 = SpyPage()
      val page2 = SpyPage()
      val page3 = SpyPage()

      var column by mutableStateOf(Column(page1))
      column = column.added(page2)

      val metadataCollection = PageMetadataCollection(
         listOf(
            pageComposable<SpyPage> { SpyPage(it) },
         )
      )

      rule.setContent {
         Column(column, metadataCollection)
      }
      rule.waitForIdle()

      assertEquals(0, page1.recompositionCount)
      assertEquals(1, page2.recompositionCount)
      assertEquals(0, page3.recompositionCount)

      column = assertNotNull(column.tailOrNull())
      rule.waitForIdle()

      assertEquals(1, page1.recompositionCount)
      assertEquals(1, page2.recompositionCount)
      assertEquals(0, page3.recompositionCount)

      column = column.added(page3)
      rule.waitForIdle()

      assertEquals(1, page1.recompositionCount)
      assertEquals(1, page2.recompositionCount)
      assertEquals(1, page3.recompositionCount)
   }
}
