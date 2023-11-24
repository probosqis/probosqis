package com.wcaokaze.probosqis.page.transition

import androidx.compose.animation.core.Transition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableMap

sealed class PageTransitionElementAnim<S : PageTransitionElementAnimScope> {
   abstract val createAnimModifier: @Composable S.() -> Modifier
}

class CurrentPageTransitionElementAnim(
   override val createAnimModifier:
         @Composable CurrentPageTransitionElementAnimScope.() -> Modifier
) : PageTransitionElementAnim<CurrentPageTransitionElementAnimScope>()

class TargetPageTransitionElementAnim(
   override val createAnimModifier:
         @Composable TargetPageTransitionElementAnimScope.() -> Modifier
) : PageTransitionElementAnim<TargetPageTransitionElementAnimScope>()

sealed class PageTransitionElementAnimScope {
   abstract val transition: Transition<PageLayoutInfo>

   val PageLayoutInfo.isCurrentPage: Boolean
      get() = pageId != transition.targetState.pageId
   val PageLayoutInfo.isTargetPage: Boolean
      get() = pageId == transition.targetState.pageId
}

class CurrentPageTransitionElementAnimScope(
   override val transition: Transition<PageLayoutInfo>
) : PageTransitionElementAnimScope()

class TargetPageTransitionElementAnimScope(
   override val transition: Transition<PageLayoutInfo>
) : PageTransitionElementAnimScope()

internal typealias PageTransitionElementAnimSet
      = ImmutableMap<PageLayoutInfo.LayoutId, PageTransitionElementAnim<*>>
internal typealias CurrentPageTransitionElementAnimSet
      = ImmutableMap<PageLayoutInfo.LayoutId, CurrentPageTransitionElementAnim>
internal typealias TargetPageTransitionElementAnimSet
      = ImmutableMap<PageLayoutInfo.LayoutId, TargetPageTransitionElementAnim>
