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

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertLeftPositionInRootIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wcaokaze.probosqis.cache.core.WritableCache
import io.mockk.every
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.BeforeTest

@RunWith(RobolectricTestRunner::class)
class SingleColumnPageStackBoardComposeTest : PageStackBoardComposeTestBase() {
   @get:Rule
   val rule = createComposeRule()

   private val defaultPageStackBoardWidth = 100.dp

   override lateinit var pageStackRepository: PageStackRepository

   @BeforeTest
   fun beforeTest() {
      pageStackRepository = mockk {
         every { savePageStack(any()) } answers { WritableCache(firstArg()) }
      }
   }

   @Composable
   private fun SingleColumnPageStackBoard(
      state: SingleColumnPageStackBoardState,
      width: Dp = defaultPageStackBoardWidth
   ) {
      SingleColumnPageStackBoard(
         state,
         pageComposableSwitcher,
         WindowInsets(0, 0, 0, 0),
         modifier = Modifier
            .width(width)
            .testTag(pageStackBoardTag)
      )
   }

   @Composable
   private fun rememberSingleColumnPageStackBoardState(
      pageStackCount: Int
   ): RememberedPageStackBoardState<SingleColumnPageStackBoardState> {
      val animCoroutineScope = rememberCoroutineScope()
      return remember(animCoroutineScope) {
         val pageStackBoardCache = createPageStackBoard(pageStackCount)
         val pageStackBoardState = SingleColumnPageStackBoardState(
            pageStackBoardCache, pageStackRepository, animCoroutineScope)
         RememberedPageStackBoardState(pageStackBoardState, animCoroutineScope)
      }
   }

   private fun expectedPageStackLeftPosition(
      indexInBoard: Int,
      pageStackBoardWidth: Dp = defaultPageStackBoardWidth
   ): Dp {
      return (pageStackBoardWidth + 16.dp) * indexInBoard
   }

   @Test
   fun layout() {
      rule.setContent {
         val (pageStackBoardState, _)
               = rememberSingleColumnPageStackBoardState(pageStackCount = 2)
         SingleColumnPageStackBoard(pageStackBoardState)
      }

      rule.onNodeWithText("0")
         .assertLeftPositionInRootIsEqualTo(expectedPageStackLeftPosition(0))
         .assertWidthIsEqualTo(defaultPageStackBoardWidth)
   }
}
