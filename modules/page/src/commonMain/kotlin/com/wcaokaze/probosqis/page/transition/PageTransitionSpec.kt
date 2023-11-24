package com.wcaokaze.probosqis.page.transition

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.toImmutableMap

inline fun pageTransitionSpec(
   enter: PageTransitionSpec.Builder.() -> Unit,
   exit:  PageTransitionSpec.Builder.() -> Unit
): PageTransitionSpec {
   val enterTransitionBuilder = PageTransitionSpec.Builder()
   val exitTransitionBuilder  = PageTransitionSpec.Builder()
   enterTransitionBuilder.enter()
   exitTransitionBuilder.exit()
   return PageTransitionSpec(enterTransitionBuilder, exitTransitionBuilder)
}

@Stable
class PageTransitionSpec(
   val enteringCurrentPageElementAnimations: CurrentPageTransitionElementAnimSet,
   val enteringTargetPageElementAnimations: TargetPageTransitionElementAnimSet,
   val exitingCurrentPageElementAnimations: CurrentPageTransitionElementAnimSet,
   val exitingTargetPageElementAnimations: TargetPageTransitionElementAnimSet
) {
   override fun equals(other: Any?): Boolean {
      if (other !is PageTransitionSpec) { return false }
      if (enteringCurrentPageElementAnimations != other.enteringCurrentPageElementAnimations) { return false }
      if (enteringTargetPageElementAnimations  != other.enteringTargetPageElementAnimations ) { return false }
      if (exitingCurrentPageElementAnimations  != other.exitingCurrentPageElementAnimations ) { return false }
      if (exitingTargetPageElementAnimations   != other.exitingTargetPageElementAnimations  ) { return false }
      return true
   }

   override fun hashCode(): Int {
      var h = 1
      h = h * 31 + enteringCurrentPageElementAnimations.hashCode()
      h = h * 31 + enteringTargetPageElementAnimations .hashCode()
      h = h * 31 + exitingCurrentPageElementAnimations .hashCode()
      h = h * 31 + exitingTargetPageElementAnimations  .hashCode()
      return h
   }

   constructor(enter: Builder, exit: Builder) : this(
      enter.currentPageAnimations.toImmutableMap(),
      enter.targetPageAnimations .toImmutableMap(),
      exit .currentPageAnimations.toImmutableMap(),
      exit .targetPageAnimations .toImmutableMap()
   )

   class Builder {
      internal val currentPageAnimations
            = mutableMapOf<PageLayoutInfo.LayoutId, CurrentPageTransitionElementAnim>()
      internal val targetPageAnimations
            = mutableMapOf<PageLayoutInfo.LayoutId, TargetPageTransitionElementAnim>()

      fun currentPageElement(
         id: PageLayoutInfo.LayoutId,
         animation: CurrentPageTransitionElementAnim
      ) {
         currentPageAnimations[id] = animation
      }

      fun targetPageElement(
         id: PageLayoutInfo.LayoutId,
         animation: TargetPageTransitionElementAnim
      ) {
         targetPageAnimations[id] = animation
      }

      fun currentPageElement(
         id: PageLayoutInfo.LayoutId,
         animationModifier:
               @Composable CurrentPageTransitionElementAnimScope.() -> Modifier
      ) {
         val anim = CurrentPageTransitionElementAnim(animationModifier)
         currentPageElement(id, anim)
      }

      fun targetPageElement(
         id: PageLayoutInfo.LayoutId,
         animationModifier:
               @Composable TargetPageTransitionElementAnimScope.() -> Modifier
      ) {
         val anim = TargetPageTransitionElementAnim(animationModifier)
         targetPageElement(id, anim)
      }
   }
}
