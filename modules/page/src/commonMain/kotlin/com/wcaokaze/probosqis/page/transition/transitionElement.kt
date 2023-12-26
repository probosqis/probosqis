package com.wcaokaze.probosqis.page.transition

import androidx.compose.animation.core.ExperimentalTransitionApi
import androidx.compose.animation.core.createChildTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned

/**
 * Page遷移時アニメーションを可能にする。
 *
 * 遷移前/先のPageを元に適切な[PageTransitionElementAnim.createAnimModifier]が
 * 実行され、返却されたModifierがこのModifierに統合される。
 *
 * このModifierが付与されたComposableの位置は[PageLayoutInfo]に収集され、
 * Page遷移時に[createAnimModifier][PageTransitionElementAnim.createAnimModifier]に
 * 渡されるため、[createAnimModifier][PageTransitionElementAnim.createAnimModifier]は
 * 遷移前/先のPageの各Composableの位置を元にアニメーションを生成できる。
 *
 * @see PageTransitionSpec
 */
@Composable
fun Modifier.transitionElement(
   layoutId: PageLayoutInfo.LayoutId,
   enabled: Boolean = true
): Modifier {
   if (!enabled) { return this }

   val pageLayoutInfo = LocalPageLayoutInfo.current
   val pageTransitionAnimations = LocalPageTransitionAnimations.current
   val transition = LocalPageTransition.current

   val transitionAnimationModifier = when (
      val elementAnim = pageTransitionAnimations[layoutId]
   ) {
      null -> Modifier
      is CurrentPageTransitionElementAnim -> with (elementAnim) {
         @OptIn(ExperimentalTransitionApi::class)
         val elementTransition = transition
            .createChildTransition(label = "transitionElement$layoutId") { it }

         CurrentPageTransitionElementAnimScope(elementTransition)
            .createAnimModifier()
      }
      is TargetPageTransitionElementAnim -> with (elementAnim) {
         @OptIn(ExperimentalTransitionApi::class)
         val elementTransition = transition
            .createChildTransition(label = "transitionElement$layoutId") { it }

         TargetPageTransitionElementAnimScope(elementTransition)
            .createAnimModifier()
      }
   }

   return onGloballyPositioned { pageLayoutInfo[layoutId] = it }
      .then(transitionAnimationModifier)
}
