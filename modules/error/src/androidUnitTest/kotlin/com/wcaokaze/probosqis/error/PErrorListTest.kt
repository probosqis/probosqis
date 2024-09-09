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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
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
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.wcaokaze.probosqis.capsiqum.page.Page
import com.wcaokaze.probosqis.capsiqum.page.PageId
import com.wcaokaze.probosqis.panoptiqon.WritableCache
import com.wcaokaze.probosqis.panoptiqon.compose.asMutableState
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.test.fail

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PErrorListTest {
   @get:Rule
   val rule = createComposeRule()

   private val ViewConfiguration.errorItemTouchSlop get() = touchSlop * 1.25f

   private val SemanticsNodeInteraction.dpPositionInRoot: DpOffset
      get() {
         val node = fetchSemanticsNode()
         return with (node.layoutInfo.density) {
            DpOffset(
               node.positionInRoot.x.toDp(),
               node.positionInRoot.y.toDp()
            )
         }
      }

   private data class ErrorImpl(val i: Int) : PError() {
      override fun restorePage() = fail()
   }

   private val errorItemComposableImpl = PErrorItemComposable<ErrorImpl>(
      composable = { error ->
         Text(
            "Error message ${error.i}",
            modifier = Modifier.fillMaxWidth().height(48.dp)
         )
      },
      onClick = { fail() }
   )

   private fun RaisedError(
      id: Long,
      error: PError,
      raiserPageId: PageId
   ) = RaisedError(
      RaisedError.Id(id),
      error,
      raiserPageId
   )

   private fun createRaisedErrorList(errorCount: Int): List<RaisedError> {
      return List(errorCount) {
         RaisedError(
            id = it.toLong(),
            ErrorImpl(it),
            raiserPageId = PageId(it.toLong()),
         )
      }
   }

   @Composable
   private fun PErrorList(
      state: PErrorListState,
      onRequestNavigateToPage: (PageId, () -> Page) -> Unit = { _, _ -> fail() },
      windowInsets: WindowInsets = WindowInsets(0)
   ) {
      PErrorList(
         state,
         PErrorListColors(
            listBackground = Color.LightGray,
            itemBackground = Color.White,
            content = Color.Black,
            header = Color.DarkGray,
            headerContent = Color.Black,
         ),
         onRequestNavigateToPage,
         windowInsets
      )
   }

   @Test
   fun pErrorActionButton_showIfErrorExists() {
      val state = PErrorListState(
         WritableCache(emptyList()),
         itemComposables = listOf(errorItemComposableImpl)
      )

      rule.setContent {
         PErrorActionButton(
            state,
            onClick = {}
         )
      }

      rule.onNodeWithContentDescription("Errors").assertDoesNotExist()

      state.raise(ErrorImpl(0), raiserPageId = PageId(0L))

      rule.onNodeWithContentDescription("Errors").assertExists()
   }

   @Config(qualifiers = "w600dp")
   @Test
   fun pErrorList_layout() {
      val widthList = listOf(350.dp, 600.dp)
      val windowInsetsList = listOf(
         "narrowInsets" to WindowInsets(0),
         "wideInsets"   to WindowInsets(64.dp, 48.dp, 56.dp, 16.dp)
      )
      val errorsList = listOf(
         createRaisedErrorList( 1),
         createRaisedErrorList(50),
      )

      val itemsCache = WritableCache(errorsList.first())
      val state = PErrorListState(
         itemsCache,
         itemComposables = listOf(errorItemComposableImpl)
      )

      var width by mutableStateOf(widthList.first())
      var windowInsets by mutableStateOf(windowInsetsList.first().second)
      var errors by itemsCache.asMutableState()

      rule.setContent {
         Box(
            modifier = Modifier
               .width(width)
         ) {
            Box(
               modifier = Modifier
                  .align(Alignment.TopEnd)
                  .windowInsetsPadding(windowInsets)
                  .padding(8.dp)
            ) {
               PErrorActionButton(
                  state,
                  onClick = {}
               )
            }

            PErrorList(state, windowInsets = windowInsets)
         }
      }

      state.show()

      for (w in widthList) {
         width = w

         for ((windowInsetsStr, wi) in windowInsetsList) {
            windowInsets = wi

            for (e in errorsList) {
               errors = e

               val widthStr = "${w.value.toInt()}dp"
               val errorCountStr = when {
                  e.size == 1 -> "1error"
                  else        -> "${e.size}errors"
               }

               rule.onRoot().captureRoboImage(
                  "test/errorList/$widthStr-$windowInsetsStr-$errorCountStr.png"
               )
            }
         }
      }
   }

   @Test
   fun enterExitAnim() {
      val errorList = createRaisedErrorList(4)
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
      val errorList = createRaisedErrorList(1)
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
      val errorList = createRaisedErrorList(1)
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
         click(Offset(right - 24.dp.toPx(), 24.dp.toPx()))
      }

      rule.runOnIdle {
         assertTrue(state.isShown)
      }
   }

   @Test
   fun dismissHandler_consumeTouchEvent() {
      val errorList = createRaisedErrorList(1)
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
      val errorList = createRaisedErrorList(1)
      val cache = WritableCache(errorList)

      val state = PErrorListState(
         cache,
         itemComposables = listOf(errorItemComposableImpl)
      )

      assertContentEquals(
         listOf(
            RaisedError(0L, ErrorImpl(0), raiserPageId = PageId(0L)),
         ),
         cache.value
      )

      state.raise(RaisedError.Id(1L), ErrorImpl(1), raiserPageId = PageId(1L))

      assertContentEquals(
         listOf(
            RaisedError(0L, ErrorImpl(0), raiserPageId = PageId(0L)),
            RaisedError(1L, ErrorImpl(1), raiserPageId = PageId(1L)),
         ),
         cache.value
      )
   }

   @Test
   fun pErrorList_verticalScroll() {
      val errorList = createRaisedErrorList(50)
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
      val errorList = createRaisedErrorList(50)
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
         moveBy(Offset(-viewConfiguration.errorItemTouchSlop, 0.0f))
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
         moveBy(Offset(viewConfiguration.errorItemTouchSlop, 0.0f))
         moveBy(Offset(64.dp.toPx(), 0.0f))
      }

      rule.onNodeWithText("Error message 1")
         .assertLeftPositionInRootIsEqualTo(96.dp)
   }

   @Test
   fun listItem_horizontalFling() {
      val errorList = createRaisedErrorList(50)
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

      rule.onNodeWithText("Error message 0")
         .assertLeftPositionInRootIsEqualTo(32.dp)

      rule.onNodeWithText("Error message 0").performTouchInput {
         swipeLeft(
            centerX,
            centerX - viewConfiguration.errorItemTouchSlop - 32.dp.toPx(),
            durationMillis = 50
         )
      }

      rule.waitUntil {
         rule.waitForIdle()
         rule.mainClock.advanceTimeByFrame()

         // スワイプが終わる位置はx = 0だがFlingによってさらに16dpくらいは動く
         rule.onNodeWithText("Error message 0").dpPositionInRoot.x > (-16).dp
      }

      rule.onNodeWithText("Error message 1")
         .assertLeftPositionInRootIsEqualTo(32.dp)

      rule.onNodeWithText("Error message 1").performTouchInput {
         swipeRight(
            centerX,
            centerX + viewConfiguration.errorItemTouchSlop + 32.dp.toPx(),
            durationMillis = 50
         )
      }

      rule.waitUntil {
         rule.waitForIdle()
         rule.mainClock.advanceTimeByFrame()

         rule.onNodeWithText("Error message 1").dpPositionInRoot.x < (64 + 16).dp
      }
   }

   @Test
   fun listItem_stopFlinging() {
      val errorList = createRaisedErrorList(50)
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
            centerX - viewConfiguration.errorItemTouchSlop - 32.dp.toPx(),
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
      class ButtonError : PError() {
         override fun restorePage() = fail()
      }
      val buttonErrorComposable = PErrorItemComposable<ButtonError>(
         composable = {
            Button(
               onClick = { isButtonClicked = true },
               modifier = Modifier.testTag(buttonTag)
            ) {
               Text("")
            }
         },
         onClick = {}
      )

      val sliderTag = "slider"
      var sliderValue by mutableStateOf(0.0f)
      class SliderError : PError() {
         override fun restorePage() = fail()
      }
      val sliderErrorComposable = PErrorItemComposable<SliderError>(
         composable = {
            Slider(
               value = sliderValue,
               onValueChange = { sliderValue = it },
               steps = 10,
               modifier = Modifier.testTag(sliderTag)
            )
         },
         onClick = {}
      )

      val errorList = listOf(
         RaisedError(0L, ButtonError(), raiserPageId = PageId(0L)),
         RaisedError(1L, SliderError(), raiserPageId = PageId(1L)),
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

   @Test
   fun errorItem_removedAfterClicked() {
      val errorList = createRaisedErrorList(2)
      val state = PErrorListState(
         WritableCache(errorList),
         itemComposables = listOf(
            PErrorItemComposable<ErrorImpl>(
               composable = { error ->
                  Text("Error message ${error.i}")
               },
               onClick = { navigateToPage() }
            )
         )
      )

      rule.setContent {
         Box {
            PErrorActionButton(
               state,
               onClick = {},
               modifier = Modifier.align(Alignment.TopEnd)
            )

            PErrorList(state, onRequestNavigateToPage = { _, _ -> })
         }
      }

      state.show()

      rule.onNodeWithText("Error message 0").performClick()

      rule.runOnIdle {
         assertContentEquals(
            listOf(
               RaisedError(1L, ErrorImpl(1), raiserPageId = PageId(1L)),
            ),
            state.errors
         )

         assertFalse(state.isShown)
      }
   }

   @Test
   fun errorItemComposableScope_navigateToPage() {
      class RaiserPage : Page()
      class Error(val raiserPage: RaiserPage) : PError() {
         override fun restorePage() = raiserPage
      }
      val errorComposable = PErrorItemComposable<Error>(
         composable = {
            Text("Error message")
         },
         onClick = { navigateToPage() }
      )

      val raiserPage = RaiserPage()
      val errorList = listOf(
         RaisedError(0L, Error(raiserPage), raiserPageId = PageId(42L))
      )
      val state = PErrorListState(
         WritableCache(errorList),
         itemComposables = listOf(errorComposable)
      )

      val fallbackPageSlot = slot<() -> Page>()
      val spyRequestNavigateToPage: (PageId, () -> Page) -> Unit = mockk {
         every { this@mockk.invoke(any(), capture(fallbackPageSlot)) } returns Unit
      }

      rule.setContent {
         Box {
            PErrorActionButton(
               state,
               onClick = {},
               modifier = Modifier.align(Alignment.TopEnd)
            )

            PErrorList(state, spyRequestNavigateToPage)
         }
      }

      state.show()

      rule.onNodeWithText("Error message").performClick()

      rule.runOnIdle {
         verify { spyRequestNavigateToPage(PageId(42L), any()) }
         assertSame(raiserPage, fallbackPageSlot.captured.invoke())
      }
   }

   private fun assertFlingAnim(
      outputFileName: String,
      itemTouchInput: TouchInjectionScope.() -> Unit
   ) {
      val errorList = createRaisedErrorList(50)
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
            centerX - viewConfiguration.errorItemTouchSlop - 16.dp.toPx(),
            durationMillis = 50
         )
      }
   }

   @Test
   fun listItem_flingAnim_notEnoughVelocity_right() {
      assertFlingAnim("flingAnim/notEnoughVelocity_right") {
         swipeRight(
            centerX,
            centerX + viewConfiguration.errorItemTouchSlop + 16.dp.toPx(),
            durationMillis = 50
         )
      }
   }

   @Test
   fun listItem_flingAnim_enoughVelocity_left() {
      assertFlingAnim("flingAnim/enoughVelocity_left") {
         swipeLeft(
            centerX,
            centerX - viewConfiguration.errorItemTouchSlop - 20.dp.toPx(),
            durationMillis = 40
         )
      }
   }

   @Test
   fun listItem_flingAnim_enoughVelocity_right() {
      assertFlingAnim("flingAnim/enoughVelocity_right") {
         swipeRight(
            centerX,
            centerX + viewConfiguration.errorItemTouchSlop + 20.dp.toPx(),
            durationMillis = 40
         )
      }
   }

   @Test
   fun listItem_flingAnim_tooFast_left() {
      assertFlingAnim("flingAnim/tooFast_left") {
         swipeLeft(
            centerX,
            centerX - viewConfiguration.errorItemTouchSlop - 64.dp.toPx(),
            durationMillis = 30
         )
      }
   }

   @Test
   fun listItem_flingAnim_tooFast_right() {
      assertFlingAnim("flingAnim/tooFast_right") {
         swipeRight(
            centerX,
            centerX + viewConfiguration.errorItemTouchSlop + 64.dp.toPx(),
            durationMillis = 30
         )
      }
   }

   @Test
   fun listItem_flingAnim_backHome_left() {
      assertFlingAnim("flingAnim/backHome_left") {
         down(center)
         moveBy(Offset(-viewConfiguration.errorItemTouchSlop - 256.dp.toPx(), 0.0f))
         moveBy(Offset(1.0f, 0.0f), delayMillis = 3000L)
         repeat (2) { moveBy(Offset(20.dp.toPx(), 0.0f)) }
         up()
      }
   }

   @Test
   fun listItem_flingAnim_backHome_right() {
      assertFlingAnim("flingAnim/backHome_right") {
         down(center)
         moveBy(Offset(viewConfiguration.errorItemTouchSlop + 256.dp.toPx(), 0.0f))
         moveBy(Offset(-1.0f, 0.0f), delayMillis = 3000L)
         repeat (2) { moveBy(Offset(-20.dp.toPx(), 0.0f)) }
         up()
      }
   }

   @Test
   fun listItem_flingAnim_removeAfterRest_left() {
      assertFlingAnim("flingAnim/removeAfterRest_left") {
         down(center)
         moveBy(Offset(-viewConfiguration.errorItemTouchSlop - 216.dp.toPx(), 0.0f))
         moveBy(Offset(1.0f, 0.0f), delayMillis = 3000L)
         up()
      }
   }

   @Test
   fun listItem_flingAnim_removeAfterRest_right() {
      assertFlingAnim("flingAnim/removeAfterRest_right") {
         down(center)
         moveBy(Offset(viewConfiguration.errorItemTouchSlop + 216.dp.toPx(), 0.0f))
         moveBy(Offset(-1.0f, 0.0f), delayMillis = 3000L)
         up()
      }
   }

   @Test
   fun listItem_flingAnim_flingAfterRest_left() {
      assertFlingAnim("flingAnim/flingAfterRest_left") {
         down(center)
         moveBy(Offset(-viewConfiguration.errorItemTouchSlop - 184.dp.toPx(), 0.0f))
         moveBy(Offset(-1.0f, 0.0f), delayMillis = 3000L)
         repeat (2) { moveBy(Offset(-20.dp.toPx(), 0.0f)) }
         up()
      }
   }

   @Test
   fun listItem_flingAnim_flingAfterRest_right() {
      assertFlingAnim("flingAnim/flingAfterRest_right") {
         down(center)
         moveBy(Offset(viewConfiguration.errorItemTouchSlop + 184.dp.toPx(), 0.0f))
         moveBy(Offset(1.0f, 0.0f), delayMillis = 3000L)
         repeat (2) { moveBy(Offset(20.dp.toPx(), 0.0f)) }
         up()
      }
   }

   @Test
   fun listItem_flingAnim_flingToAnotherSide_left() {
      assertFlingAnim("flingAnim/flingToAnotherSide_left") {
         down(center)
         moveBy(Offset(viewConfiguration.errorItemTouchSlop + 288.dp.toPx(), 0.0f))
         moveBy(Offset(-1.0f, 0.0f), delayMillis = 3000L)
         repeat (2) { moveBy(Offset(-40.dp.toPx(), 0.0f)) }
         up()
      }
   }

   @Test
   fun listItem_flingAnim_flingToAnotherSide_right() {
      assertFlingAnim("flingAnim/flingToAnotherSide_right") {
         down(center)
         moveBy(Offset(-viewConfiguration.errorItemTouchSlop - 288.dp.toPx(), 0.0f))
         moveBy(Offset(1.0f, 0.0f), delayMillis = 3000L)
         repeat (2) { moveBy(Offset(40.dp.toPx(), 0.0f)) }
         up()
      }
   }

   @Test
   fun errorItem_dismissByFlinging() {
      val errorList = createRaisedErrorList(3)
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
            centerX - viewConfiguration.errorItemTouchSlop - 20.dp.toPx(),
            durationMillis = 40
         )
      }

      rule.runOnIdle {
         assertContentEquals(
            listOf(
               RaisedError(0L, ErrorImpl(0), raiserPageId = PageId(0L)),
               RaisedError(2L, ErrorImpl(2), raiserPageId = PageId(2L)),
            ),
            state.errors
         )
      }
   }

   @OptIn(ExperimentalTestApi::class)
   @Test
   fun errorItem_showDismissButton() {
      val errorList = createRaisedErrorList(3)
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
      val errorList = createRaisedErrorList(50)
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
      val errorList = createRaisedErrorList(3)
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

      rule.runOnIdle {
         assertContentEquals(
            listOf(
               RaisedError(0L, ErrorImpl(0), raiserPageId = PageId(0L)),
               RaisedError(2L, ErrorImpl(2), raiserPageId = PageId(2L)),
            ),
            state.errors
         )
      }
   }

   @Test
   fun errorItemList_hideAfterLastItemDismissed() {
      val errorList = createRaisedErrorList(1)
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

      rule.onNodeWithText("Error message 0").performTouchInput {
         swipeLeft(
            centerX,
            centerX - viewConfiguration.errorItemTouchSlop - 20.dp.toPx(),
            durationMillis = 40
         )
      }

      rule.mainClock.advanceTimeBy(350L)

      repeat (35) { i ->
         rule.onRoot().captureRoboImage("test/hideAfterLastItemDismissed/$i.png")
         rule.mainClock.advanceTimeBy(16L)
      }

      rule.runOnIdle {
         assertFalse(state.isShown)
      }
   }
}
