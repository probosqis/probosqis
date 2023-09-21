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

import androidx.compose.runtime.SideEffect
import androidx.compose.ui.test.junit4.createComposeRule
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.test.assertEquals
import kotlin.test.assertSame

@RunWith(JUnit4::class)
class PageComposeTest {
   @get:Rule
   val rule = createComposeRule()

   private class PageA : Page()
   private class PageAState : PageState()
   private class PageB : Page()
   private class PageBState : PageState()

   @Test
   fun composableCalled() {
      var pageARecompositionCount = 0
      var pageBRecompositionCount = 0

      val pageA = PageA()

      val pageComposableSwitcher = PageComposableSwitcher(
         listOf(
            pageComposable<PageA, PageAState>(
               pageStateFactory { PageAState() },
               content = { _, _, _ ->
                  SideEffect {
                     pageARecompositionCount++
                  }
               },
               header = { _, _, _ -> },
               footer = null
            ),
            pageComposable<PageB, PageBState>(
               pageStateFactory { PageBState() },
               content = { _, _, _ ->
                  SideEffect {
                     pageBRecompositionCount++
                  }
               },
               header = { _, _, _ -> },
               footer = null
            ),
         )
      )

      val pageStateStore = PageStateStore(
         listOf(
            pageStateFactory<PageA, PageAState> { PageAState() },
            pageStateFactory<PageB, PageBState> { PageBState() },
         )
      )

      rule.setContent {
         PageContent(
            PageStack.SavedPageState(
               PageStack.PageId(0L),
               pageA,
            ),
            pageComposableSwitcher,
            pageStateStore,
            pageStackState = mockk()
         )
      }

      rule.runOnIdle {
         assertEquals(1, pageARecompositionCount)
         assertEquals(0, pageBRecompositionCount)
      }
   }

   @Test
   fun argument() {
      val page = PageA()

      var argumentPage: PageA? = null
      var argumentPageState: PageA? = null

      class PageStateImpl : PageState() {
         fun spy(page: PageA) {
            argumentPageState = page
         }
      }

      val pageComposableSwitcher = PageComposableSwitcher(
         listOf(
            pageComposable<PageA, PageStateImpl>(
               pageStateFactory { PageStateImpl() },
               content = { p, s, _ ->
                  SideEffect {
                     argumentPage = p
                     s.spy(p)
                  }
               },
               header = { _, _, _ -> },
               footer = null
            ),
         )
      )

      val pageStateStore = PageStateStore(
         listOf(
            pageStateFactory<PageA, PageStateImpl> { PageStateImpl() },
         )
      )

      rule.setContent {
         PageContent(
            PageStack.SavedPageState(
               PageStack.PageId(0L),
               page
            ),
            pageComposableSwitcher,
            pageStateStore,
            pageStackState = mockk()
         )
      }

      rule.runOnIdle {
         assertSame(page, argumentPage)
         assertSame(page, argumentPageState)
      }
   }
}
