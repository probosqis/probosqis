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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.TouchInjectionScope
import androidx.compose.ui.test.assertLeftPositionInRootIsEqualTo
import androidx.compose.ui.test.assertTopPositionInRootIsEqualTo
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.wcaokaze.probosqis.panoptiqon.WritableCache
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PErrorListTest {
   @get:Rule
   val rule = createComposeRule()

   private data class ErrorImpl(val i: Int) : PError(Id(i.toLong()))

   private val errorItemComposableImpl = PErrorItemComposable<ErrorImpl> { error ->
      Text(
         "Error message ${error.i}",
         modifier = Modifier.fillMaxWidth().height(48.dp)
      )
   }

   @Composable
   private fun PErrorList(state: PErrorListState) {
      PErrorList(
         state,
         PErrorListColors(
            listBackground = Color.LightGray,
            itemBackground = Color.White,
            content = Color.Black,
            header = Color.DarkGray,
            headerContent = Color.Black,
         )
      )
   }

   @Test
   fun enterExitAnim() {
      val errorList = List(4) { ErrorImpl(it) } .toImmutableList()
      val state = PErrorListState(
         WritableCache(errorList),
         itemComposables = listOf(errorItemComposableImpl)
      )

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
      val errorList = persistentListOf(ErrorImpl(0))
      val state = PErrorListState(
         WritableCache(errorList),
         itemComposables = listOf(errorItemComposableImpl)
      )

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
      val errorList = persistentListOf(ErrorImpl(0))
      val state = PErrorListState(
         WritableCache(errorList),
         itemComposables = listOf(errorItemComposableImpl)
      )

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
      val errorList = persistentListOf(ErrorImpl(0))
      val state = PErrorListState(
         WritableCache(errorList),
         itemComposables = listOf(errorItemComposableImpl)
      )

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

   @Test
   fun pErrorListState_raise() {
      val errorList = persistentListOf(ErrorImpl(0))
      val cache = WritableCache<List<PError>>(errorList)

      val state = PErrorListState(
         cache,
         itemComposables = listOf(errorItemComposableImpl)
      )

      assertContentEquals(listOf(ErrorImpl(0)), cache.value)
      state.raise(ErrorImpl(1))
      assertContentEquals(listOf(ErrorImpl(0), ErrorImpl(1)), cache.value)
   }

   @Test
   fun pErrorList_verticalScroll() {
      val errorList = List(50) { ErrorImpl(it) }
      val state = PErrorListState(
         WritableCache(errorList),
         itemComposables = listOf(errorItemComposableImpl)
      )

      rule.setContent {
         Box {
            PErrorActionButton(
               state,
               onClick = {},
               modifier = Modifier.align(Alignment.TopEnd)
            )

            PErrorList(state)
         }
      }

      state.show()

      rule.onNodeWithText("Error message 0")
         .assertTopPositionInRootIsEqualTo(48.dp)

      rule.onNodeWithText("Error message 0").performTouchInput {
         down(center)
         moveBy(Offset(0.0f, -viewConfiguration.touchSlop))
         moveBy(Offset(0.0f, -32.dp.toPx()))
      }

      rule.onNodeWithText("Error message 0")
         .assertTopPositionInRootIsEqualTo(16.dp)
   }

   @Test
   fun listItem_horizontalDrag() {
      val errorList = List(50) { ErrorImpl(it) }
      val state = PErrorListState(
         WritableCache(errorList),
         itemComposables = listOf(errorItemComposableImpl)
      )

      rule.setContent {
         Box {
            PErrorActionButton(
               state,
               onClick = {},
               modifier = Modifier.align(Alignment.TopEnd)
            )

            PErrorList(state)
         }
      }

      state.show()

      rule.onNodeWithText("Error message 0")
         .assertLeftPositionInRootIsEqualTo(32.dp)

      rule.onNodeWithText("Error message 0").performTouchInput {
         down(center)
         moveBy(Offset(-viewConfiguration.touchSlop, 0.0f))
         moveBy(Offset(-32.dp.toPx(), 0.0f))
      }

      rule.onNodeWithText("Error message 0")
         .assertLeftPositionInRootIsEqualTo(0.dp)

      rule.onNodeWithText("Error message 1")
         .assertLeftPositionInRootIsEqualTo(32.dp)

      rule.onNodeWithText("Error message 0").performTouchInput {
         up()
      }

      rule.onNodeWithText("Error message 1").performTouchInput {
         down(center)
         moveBy(Offset(viewConfiguration.touchSlop, 0.0f))
         moveBy(Offset(64.dp.toPx(), 0.0f))
      }

      rule.onNodeWithText("Error message 1")
         .assertLeftPositionInRootIsEqualTo(96.dp)
   }

   @Test
   fun listItem_horizontalFling() {
      val errorList = List(50) { ErrorImpl(it) }
      val state = PErrorListState(
         WritableCache(errorList),
         itemComposables = listOf(errorItemComposableImpl)
      )

      rule.setContent {
         Box {
            PErrorActionButton(
               state,
               onClick = {},
               modifier = Modifier.align(Alignment.TopEnd)
            )

            PErrorList(state)
         }
      }

      state.show()

      rule.onNodeWithText("Error message 0")
         .assertLeftPositionInRootIsEqualTo(32.dp)

      rule.onNodeWithText("Error message 0").performTouchInput {
         swipeLeft(
            centerX,
            centerX - viewConfiguration.touchSlop - 32.dp.toPx(),
            durationMillis = 50
         )
      }

      rule.onNodeWithText("Error message 0")
         .fetchSemanticsNode()
         .let {
            with (it.layoutInfo.density) {
               // スワイプが終わる位置はx = 0だがFlingによってさらに16dpくらいは動く
               assertTrue(it.positionInRoot.x < (0 - 16).dp.toPx())
            }
         }

      rule.onNodeWithText("Error message 1")
         .assertLeftPositionInRootIsEqualTo(32.dp)

      rule.onNodeWithText("Error message 1").performTouchInput {
         swipeRight(
            centerX,
            centerX + viewConfiguration.touchSlop + 32.dp.toPx(),
            durationMillis = 50
         )
      }

      rule.onNodeWithText("Error message 1")
         .fetchSemanticsNode()
         .let {
            with (it.layoutInfo.density) {
               assertTrue(it.positionInRoot.x > (96 + 16).dp.toPx())
            }
         }
   }

   @Test
   fun listItem_stopFlinging() {
      val errorList = List(50) { ErrorImpl(it) }
      val state = PErrorListState(
         WritableCache(errorList),
         itemComposables = listOf(errorItemComposableImpl)
      )

      rule.setContent {
         Box {
            PErrorActionButton(
               state,
               onClick = {},
               modifier = Modifier.align(Alignment.TopEnd)
            )

            PErrorList(state)
         }
      }

      state.show()

      rule.onNodeWithText("Error message 0")
         .assertLeftPositionInRootIsEqualTo(32.dp)

      rule.onNodeWithText("Error message 0").performTouchInput {
         swipeLeft(
            centerX,
            centerX - viewConfiguration.touchSlop - 32.dp.toPx(),
            durationMillis = 50
         )
         down(center)
      }

      rule.onNodeWithText("Error message 0")
         .assertLeftPositionInRootIsEqualTo(0.dp)
   }

   @Test
   fun errorItemComposable_touchChildren() {
      val buttonTag = "button"
      var isButtonClicked by mutableStateOf(false)
      class ButtonError : PError(Id(0L))
      val buttonErrorComposable = PErrorItemComposable<ButtonError> {
         Button(
            onClick = { isButtonClicked = true },
            modifier = Modifier.testTag(buttonTag)
         ) {
            Text("")
         }
      }

      val sliderTag = "slider"
      var sliderValue by mutableStateOf(0.0f)
      class SliderError : PError(Id(1L))
      val sliderErrorComposable = PErrorItemComposable<SliderError> {
         Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            steps = 10,
            modifier = Modifier.testTag(sliderTag)
         )
      }

      val errorList = listOf(
         ButtonError(),
         SliderError(),
      )
      val state = PErrorListState(
         WritableCache(errorList),
         itemComposables = listOf(
            buttonErrorComposable,
            sliderErrorComposable,
         )
      )

      rule.setContent {
         Box {
            PErrorActionButton(
               state,
               onClick = {},
               modifier = Modifier.align(Alignment.TopEnd)
            )

            PErrorList(state)
         }
      }

      state.show()

      rule.runOnIdle {
         assertFalse(isButtonClicked)
      }
      rule.onNodeWithTag(buttonTag).performClick()
      rule.runOnIdle {
         assertTrue(isButtonClicked)
      }

      rule.runOnIdle {
         assertEquals(0.0f, sliderValue)
      }
      rule.onNodeWithTag(sliderTag).performTouchInput {
         swipeRight()
      }
      rule.runOnIdle {
         assertEquals(1.0f, sliderValue)
      }
   }

   private fun assertFlingAnim(
      outputFileName: String,
      itemTouchInput: TouchInjectionScope.() -> Unit
   ) {
      val errorList = List(50) { ErrorImpl(it) }
      val state = PErrorListState(
         WritableCache(errorList),
         itemComposables = listOf(errorItemComposableImpl)
      )

      rule.setContent {
         Box {
            PErrorActionButton(
               state,
               onClick = {},
               modifier = Modifier.align(Alignment.TopEnd)
            )

            PErrorList(state)
         }
      }

      state.show()
      rule.waitForIdle()
      rule.mainClock.autoAdvance = false

      rule.onNodeWithText("Error message 1").performTouchInput(itemTouchInput)

      repeat (20) { i ->
         rule.onRoot().captureRoboImage("test/$outputFileName$i.png")
         rule.mainClock.advanceTimeBy(16L)
      }
   }

   @Test
   fun listItem_flingAnim_notEnoughVelocity_left() {
      assertFlingAnim("flingAnim/notEnoughVelocity_left") {
         swipeLeft(
            centerX,
            centerX - viewConfiguration.touchSlop - 16.dp.toPx(),
            durationMillis = 40
         )
      }
   }

   @Test
   fun listItem_flingAnim_notEnoughVelocity_right() {
      assertFlingAnim("flingAnim/notEnoughVelocity_right") {
         swipeRight(
            centerX,
            centerX + viewConfiguration.touchSlop + 16.dp.toPx(),
            durationMillis = 40
         )
      }
   }

   @Test
   fun listItem_flingAnim_enoughVelocity_left() {
      assertFlingAnim("flingAnim/enoughVelocity_left") {
         swipeLeft(
            centerX,
            centerX - viewConfiguration.touchSlop - 20.dp.toPx(),
            durationMillis = 40
         )
      }
   }

   @Test
   fun listItem_flingAnim_enoughVelocity_right() {
      assertFlingAnim("flingAnim/enoughVelocity_right") {
         swipeRight(
            centerX,
            centerX + viewConfiguration.touchSlop + 20.dp.toPx(),
            durationMillis = 40
         )
      }
   }

   @Test
   fun listItem_flingAnim_tooFast_left() {
      assertFlingAnim("flingAnim/tooFast_left") {
         swipeLeft(
            centerX,
            centerX - viewConfiguration.touchSlop - 64.dp.toPx(),
            durationMillis = 30
         )
      }
   }

   @Test
   fun listItem_flingAnim_tooFast_right() {
      assertFlingAnim("flingAnim/tooFast_right") {
         swipeRight(
            centerX,
            centerX + viewConfiguration.touchSlop + 64.dp.toPx(),
            durationMillis = 30
         )
      }
   }

   @Test
   fun listItem_flingAnim_backHome_left() {
      assertFlingAnim("flingAnim/backHome_left") {
         down(center)
         moveBy(Offset(-viewConfiguration.touchSlop - 256.dp.toPx(), 0.0f))
         moveBy(Offset(1.0f, 0.0f), delayMillis = 3000L)
         repeat (2) { moveBy(Offset(20.dp.toPx(), 0.0f)) }
         up()
      }
   }

   @Test
   fun listItem_flingAnim_backHome_right() {
      assertFlingAnim("flingAnim/backHome_right") {
         down(center)
         moveBy(Offset(viewConfiguration.touchSlop + 256.dp.toPx(), 0.0f))
         moveBy(Offset(-1.0f, 0.0f), delayMillis = 3000L)
         repeat (2) { moveBy(Offset(-20.dp.toPx(), 0.0f)) }
         up()
      }
   }

   @Test
   fun listItem_flingAnim_removeAfterRest_left() {
      assertFlingAnim("flingAnim/removeAfterRest_left") {
         down(center)
         moveBy(Offset(-viewConfiguration.touchSlop - 216.dp.toPx(), 0.0f))
         moveBy(Offset(1.0f, 0.0f), delayMillis = 3000L)
         up()
      }
   }

   @Test
   fun listItem_flingAnim_removeAfterRest_right() {
      assertFlingAnim("flingAnim/removeAfterRest_right") {
         down(center)
         moveBy(Offset(viewConfiguration.touchSlop + 216.dp.toPx(), 0.0f))
         moveBy(Offset(-1.0f, 0.0f), delayMillis = 3000L)
         up()
      }
   }

   @Test
   fun listItem_flingAnim_flingAfterRest_left() {
      assertFlingAnim("flingAnim/flingAfterRest_left") {
         down(center)
         moveBy(Offset(-viewConfiguration.touchSlop - 184.dp.toPx(), 0.0f))
         moveBy(Offset(-1.0f, 0.0f), delayMillis = 3000L)
         repeat (2) { moveBy(Offset(-20.dp.toPx(), 0.0f)) }
         up()
      }
   }

   @Test
   fun listItem_flingAnim_flingAfterRest_right() {
      assertFlingAnim("flingAnim/flingAfterRest_right") {
         down(center)
         moveBy(Offset(viewConfiguration.touchSlop + 184.dp.toPx(), 0.0f))
         moveBy(Offset(1.0f, 0.0f), delayMillis = 3000L)
         repeat (2) { moveBy(Offset(20.dp.toPx(), 0.0f)) }
         up()
      }
   }

   @Test
   fun listItem_flingAnim_flingToAnotherSide_left() {
      assertFlingAnim("flingAnim/flingToAnotherSide_left") {
         down(center)
         moveBy(Offset(viewConfiguration.touchSlop + 288.dp.toPx(), 0.0f))
         moveBy(Offset(-1.0f, 0.0f), delayMillis = 3000L)
         repeat (2) { moveBy(Offset(-40.dp.toPx(), 0.0f)) }
         up()
      }
   }

   @Test
   fun listItem_flingAnim_flingToAnotherSide_right() {
      assertFlingAnim("flingAnim/flingToAnotherSide_right") {
         down(center)
         moveBy(Offset(-viewConfiguration.touchSlop - 288.dp.toPx(), 0.0f))
         moveBy(Offset(1.0f, 0.0f), delayMillis = 3000L)
         repeat (2) { moveBy(Offset(40.dp.toPx(), 0.0f)) }
         up()
      }
   }

   @Test
   fun errorItem_dismissByFlinging() {
      val errorList = List(3) { ErrorImpl(it) }
      val state = PErrorListState(
         WritableCache(errorList),
         itemComposables = listOf(errorItemComposableImpl)
      )

      rule.setContent {
         Box {
            PErrorActionButton(
               state,
               onClick = {},
               modifier = Modifier.align(Alignment.TopEnd)
            )

            PErrorList(state)
         }
      }

      state.show()

      rule.onNodeWithText("Error message 1").performTouchInput {
         swipeLeft(
            centerX,
            centerX - viewConfiguration.touchSlop - 20.dp.toPx(),
            durationMillis = 40
         )
      }

      rule.runOnIdle {
         assertContentEquals(
            listOf(ErrorImpl(0), ErrorImpl(2)),
            state.errors
         )
      }
   }

   @OptIn(ExperimentalTestApi::class)
   @Test
   fun errorItem_showDismissButton() {
      val errorList = List(3) { ErrorImpl(it) }
      val state = PErrorListState(
         WritableCache(errorList),
         itemComposables = listOf(errorItemComposableImpl)
      )

      rule.setContent {
         Box {
            PErrorActionButton(
               state,
               onClick = {},
               modifier = Modifier.align(Alignment.TopEnd)
            )

            PErrorList(state)
         }
      }

      state.show()

      rule.onNodeWithContentDescription("Dismiss").assertDoesNotExist()

      rule.onNodeWithText("Error message 1").performMouseInput {
         enter(center)
      }

      rule.onNodeWithContentDescription("Dismiss").assertExists()
   }

   @OptIn(ExperimentalTestApi::class)
   @Test
   fun errorItem_dismissAnim() {
      val errorList = List(50) { ErrorImpl(it) }
      val state = PErrorListState(
         WritableCache(errorList),
         itemComposables = listOf(errorItemComposableImpl)
      )

      rule.setContent {
         Box {
            PErrorActionButton(
               state,
               onClick = {},
               modifier = Modifier.align(Alignment.TopEnd)
            )

            PErrorList(state)
         }
      }

      state.show()

      rule.onNodeWithText("Error message 1").performMouseInput {
         enter(center)
      }

      rule.waitForIdle()
      rule.mainClock.autoAdvance = false

      rule.onNodeWithContentDescription("Dismiss").performMouseInput {
         click()
      }
      rule.onNodeWithText("Error message 1").performMouseInput {
         exit()
      }

      repeat (20) { i ->
         rule.onRoot().captureRoboImage("test/dismissAnim/$i.png")
         rule.mainClock.advanceTimeBy(16L)
      }
   }

   @OptIn(ExperimentalTestApi::class)
   @Test
   fun errorItem_dismissByButton() {
      val errorList = List(3) { ErrorImpl(it) }
      val state = PErrorListState(
         WritableCache(errorList),
         itemComposables = listOf(errorItemComposableImpl)
      )

      rule.setContent {
         Box {
            PErrorActionButton(
               state,
               onClick = {},
               modifier = Modifier.align(Alignment.TopEnd)
            )

            PErrorList(state)
         }
      }

      state.show()

      rule.onNodeWithText("Error message 1").performMouseInput {
         enter(center)
      }
      rule.onNodeWithContentDescription("Dismiss").performMouseInput {
         click()
      }
      rule.onNodeWithText("Error message 1").performMouseInput {
         exit()
      }

      rule.runOnIdle {
         assertContentEquals(
            listOf(ErrorImpl(0), ErrorImpl(2)),
            state.errors
         )
      }
   }
}
