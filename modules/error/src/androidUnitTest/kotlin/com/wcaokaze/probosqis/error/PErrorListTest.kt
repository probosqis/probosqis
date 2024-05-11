/*
 * Copyright 2024 wcaokaze
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

package com.wcaokaze.probosqis.error

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PErrorListTest {
   @get:Rule
   val rule = createComposeRule()

   @Test
   fun enterExitAnim() {
      val state = PErrorListState()

      rule.setContent {
         Box {
            Box(
               modifier = Modifier
                  .align(Alignment.TopEnd)
                  .padding(8.dp)
            ) {
               PErrorActionButton(
                  state,
                  onClick = {}
               )
            }

            PErrorList(state)
         }
      }

      rule.mainClock.autoAdvance = false

      state.show()
      rule.waitForIdle()

      repeat (20) { i ->
         rule.onRoot().captureRoboImage("test/errorListAnim/enter$i.png")
         rule.mainClock.advanceTimeBy(16L)
      }

      state.hide()
      rule.waitForIdle()

      repeat (20) { i ->
         rule.onRoot().captureRoboImage("test/errorListAnim/exit$i.png")
         rule.mainClock.advanceTimeBy(16L)
      }
   }

   @Test
   fun dismissHandler() {
      val state = PErrorListState()

      rule.setContent {
         Box {
            Box(
               modifier = Modifier
                  .align(Alignment.TopEnd)
                  .padding(8.dp)
            ) {
               PErrorActionButton(
                  state,
                  onClick = {}
               )
            }

            PErrorList(state)
         }
      }

      state.show()
      rule.waitForIdle()

      rule.onRoot().performTouchInput {
         click(Offset(4.dp.toPx(), 4.dp.toPx()))
      }

      rule.runOnIdle {
         assertFalse(state.isShown)
      }
   }

   @Test
   fun dismissHandler_doNotDismissTouchingErrorList() {
      val state = PErrorListState()

      rule.setContent {
         Box {
            Box(
               modifier = Modifier
                  .align(Alignment.TopEnd)
                  .padding(8.dp)
            ) {
               PErrorActionButton(
                  state,
                  onClick = {}
               )
            }

            PErrorList(state)
         }
      }

      state.show()
      rule.waitForIdle()

      rule.onRoot().performTouchInput {
         click(Offset(centerX, 12.dp.toPx()))
      }

      rule.runOnIdle {
         assertTrue(state.isShown)
      }
   }

   @Test
   fun dismissHandler_consumeTouchEvent() {
      val state = PErrorListState()
      var isRootTouched by mutableStateOf(false)

      rule.setContent {
         Box {
            Box(
               modifier = Modifier
                  .align(Alignment.TopStart)
                  .size(8.dp)
                  .pointerInput(Unit) {
                     detectTapGestures(
                        onPress = { isRootTouched = true }
                     )
                  }
            )

            Box(
               modifier = Modifier
                  .align(Alignment.TopEnd)
                  .padding(8.dp)
            ) {
               PErrorActionButton(
                  state,
                  onClick = {}
               )
            }

            PErrorList(state)
         }
      }

      state.show()
      rule.waitForIdle()

      rule.onRoot().performTouchInput {
         click(Offset(4.dp.toPx(), 4.dp.toPx()))
      }

      rule.runOnIdle {
         assertFalse(isRootTouched)
      }

      rule.onRoot().performTouchInput {
         click(Offset(4.dp.toPx(), 4.dp.toPx()))
      }

      rule.runOnIdle {
         assertTrue(isRootTouched)
      }
   }
}
