package com.wcaokaze.probosqis.page.transition

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import kotlinx.collections.immutable.persistentMapOf
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class TransitionSpecBuilderTest {
   @get:Rule
   val rule = createComposeRule()

   @Test
   fun build() {
      val enterCurrentId = PageLayoutInfo.LayoutId()
      val enterCurrent = CurrentPageTransitionElementAnim { Modifier }

      val enterTargetId = PageLayoutInfo.LayoutId()
      val enterTarget = TargetPageTransitionElementAnim { Modifier }

      val exitCurrentId = PageLayoutInfo.LayoutId()
      val exitCurrent = CurrentPageTransitionElementAnim { Modifier }

      val exitTargetId = PageLayoutInfo.LayoutId()
      val exitTarget = TargetPageTransitionElementAnim { Modifier }

      val transitionSpec = pageTransitionSpec(
         enter = {
            currentPageElement(enterCurrentId, enterCurrent)
            targetPageElement (enterTargetId,  enterTarget)
         },
         exit = {
            currentPageElement(exitCurrentId, exitCurrent)
            targetPageElement (exitTargetId,  exitTarget)
         }
      )

      assertEquals(
         persistentMapOf(enterCurrentId to enterCurrent),
         transitionSpec.enteringCurrentPageElementAnimations
      )
      assertEquals(
         persistentMapOf(enterTargetId to enterTarget),
         transitionSpec.enteringTargetPageElementAnimations
      )
      assertEquals(
         persistentMapOf(exitCurrentId to exitCurrent),
         transitionSpec.exitingCurrentPageElementAnimations
      )
      assertEquals(
         persistentMapOf(exitTargetId to exitTarget),
         transitionSpec.exitingTargetPageElementAnimations
      )
   }
}
