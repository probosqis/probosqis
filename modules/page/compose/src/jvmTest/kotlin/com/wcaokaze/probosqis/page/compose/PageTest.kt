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

import androidx.compose.runtime.SideEffect
import androidx.compose.ui.test.junit4.createComposeRule
import com.wcaokaze.probosqis.page.core.Page
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.test.assertEquals
import kotlin.test.assertSame

@RunWith(JUnit4::class)
class PageTest {
   @get:Rule
   val rule = createComposeRule()

   private class PageA : Page()
   private class PageB : Page()

   @Test
   fun composableCalled() {
      var pageARecompositionCount = 0
      var pageBRecompositionCount = 0

      val pageA = PageA()

      val pageComposableSwitcher = PageComposableSwitcher(
         listOf(
            pageComposable<PageA> {
               SideEffect {
                  pageARecompositionCount++
               }
            },
            pageComposable<PageB> {
               SideEffect {
                  pageBRecompositionCount++
               }
            },
         )
      )

      rule.setContent {
         Page(pageA, pageComposableSwitcher)
      }
      rule.waitForIdle()

      assertEquals(1, pageARecompositionCount)
      assertEquals(0, pageBRecompositionCount)
   }

   @Test
   fun argument() {
      val page = PageA()

      val pageComposableSwitcher = PageComposableSwitcher(
         listOf(
            pageComposable<PageA> { actual ->
               assertSame(page, actual)
            },
         )
      )

      rule.setContent {
         Page(page, pageComposableSwitcher)
      }
      rule.waitForIdle()
   }
}
