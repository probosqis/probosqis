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

import androidx.compose.runtime.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.wcaokaze.probosqis.cache.core.WritableCache
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Job
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(JUnit4::class)
class PageStackComposeTest {
   @get:Rule
   val rule = createComposeRule()

   private val pageComposableSwitcher = PageComposableSwitcher(
      listOf(
         spyPageComposable,
      )
   )

   private val pageStateStore = PageStateStore(
      listOf(
         spyPageComposable.pageStateFactory,
      )
   )

   @Test
   fun onlyForefrontComposableIsCalled() {
      val page1 = SpyPage()
      val page2 = SpyPage()

      var pageStack = PageStack(
         PageStack.Id(0L),
         PageStack.SavedPageState(
            PageStack.PageId(0L),
            page1
         )
      )
      pageStack = pageStack.added(
         PageStack.SavedPageState(
            PageStack.PageId(1L),
            page2
         )
      )

      val pageStackState = PageStackState(
         PageStackBoard.PageStackId(pageStack.id.value),
         WritableCache(pageStack),
         mockk<SingleColumnPageStackBoardState>()
      )

      rule.setContent {
         PageStackContent(pageStackState, pageComposableSwitcher, pageStateStore)
      }

      rule.runOnIdle {
         assertEquals(0, page1.recompositionCount)
         assertEquals(1, page2.recompositionCount)
      }
   }

   @Test
   fun pageTransition() {
      val page1 = SpyPage()
      val page2 = SpyPage()
      val page3 = SpyPage()

      val pageStackState by derivedStateOf {
         var pageStack = PageStack(
            PageStack.Id(0L),
            PageStack.SavedPageState(
               PageStack.PageId(0L),
               page1
            )
         )
         pageStack = pageStack.added(
            PageStack.SavedPageState(
               PageStack.PageId(1L),
               page2
            )
         )

         PageStackState(
            PageStackBoard.PageStackId(pageStack.id.value),
            WritableCache(pageStack),
            mockk<SingleColumnPageStackBoardState>()
         )
      }

      rule.setContent {
         PageStackContent(pageStackState, pageComposableSwitcher, pageStateStore)
      }

      rule.runOnIdle {
         assertEquals(0, page1.recompositionCount)
         assertEquals(1, page2.recompositionCount)
         assertEquals(0, page3.recompositionCount)
      }

      pageStackState.pageStackCache.value =
         assertNotNull(pageStackState.pageStack.tailOrNull())

      rule.runOnIdle {
         assertEquals(1, page1.recompositionCount)
         assertEquals(1, page2.recompositionCount)
         assertEquals(0, page3.recompositionCount)
      }

      pageStackState.pageStackCache.value = pageStackState.pageStack.added(
         PageStack.SavedPageState(
            PageStack.PageId(2L),
            page3
         )
      )

      rule.runOnIdle {
         assertEquals(1, page1.recompositionCount)
         assertEquals(1, page2.recompositionCount)
         assertEquals(1, page3.recompositionCount)
      }
   }

   @Test
   fun pageTransition_viaPageStackState() {
      val page1 = SpyPage()
      val page2 = SpyPage()
      val page3 = SpyPage()

      val pageStackId = PageStackBoard.PageStackId(0L)

      val initialPageStack = PageStack(
         PageStack.Id(pageStackId.value),
         PageStack.SavedPageState(
            PageStack.PageId(0L),
            page1
         )
      )

      val pageStackBoardState = mockk<SingleColumnPageStackBoardState> {
         every { removePageStack(any()) } returns Job().apply { complete() }
      }

      val pageStackState = PageStackState(
         pageStackId,
         WritableCache(initialPageStack),
         pageStackBoardState
      )

      rule.setContent {
         PageStackContent(pageStackState, pageComposableSwitcher, pageStateStore)
      }

      rule.runOnIdle {
         assertEquals(1, page1.recompositionCount)
         assertEquals(0, page2.recompositionCount)
         assertEquals(0, page3.recompositionCount)
         verify(inverse = true) { pageStackBoardState.removePageStack(pageStackId) }
      }

      pageStackState.startPage(page2)

      rule.runOnIdle {
         assertEquals(1, page1.recompositionCount)
         assertEquals(1, page2.recompositionCount)
         assertEquals(0, page3.recompositionCount)
         verify(inverse = true) { pageStackBoardState.removePageStack(pageStackId) }
      }

      pageStackState.finishPage()

      rule.runOnIdle {
         assertEquals(2, page1.recompositionCount)
         assertEquals(1, page2.recompositionCount)
         assertEquals(0, page3.recompositionCount)
         verify(inverse = true) { pageStackBoardState.removePageStack(pageStackId) }
      }

      pageStackState.startPage(page3)

      rule.runOnIdle {
         assertEquals(2, page1.recompositionCount)
         assertEquals(1, page2.recompositionCount)
         assertEquals(1, page3.recompositionCount)
         verify(inverse = true) { pageStackBoardState.removePageStack(pageStackId) }
      }

      pageStackState.finishPage()

      rule.runOnIdle {
         assertEquals(3, page1.recompositionCount)
         assertEquals(1, page2.recompositionCount)
         assertEquals(1, page3.recompositionCount)
         verify(inverse = true) { pageStackBoardState.removePageStack(pageStackId) }
      }

      pageStackState.finishPage()

      rule.runOnIdle {
         assertEquals(3, page1.recompositionCount)
         assertEquals(1, page2.recompositionCount)
         assertEquals(1, page3.recompositionCount)
         verify { pageStackBoardState.removePageStack(pageStackId) }
      }
   }
}
