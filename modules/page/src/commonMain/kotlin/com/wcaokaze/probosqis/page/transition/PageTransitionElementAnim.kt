package com.wcaokaze.probosqis.page.transition

import androidx.compose.animation.core.Transition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.wcaokaze.probosqis.page.PageComposable
import kotlinx.collections.immutable.ImmutableMap

/**
 * ページ遷移中、[PageComposable.contentComposable]内のComposableに
 * 適用されるModifier。
 *
 * [PageTransitionElementAnimScope.transition]から現在の遷移状況や
 * 遷移元、遷移先の[PageLayoutInfo]を取得できるため、それを使ってアニメーションを
 * 生成し、Modifierに適用する。
 *
 * @see PageTransitionSpec
 */
sealed class PageTransitionElementAnim<S : PageTransitionElementAnimScope> {
   abstract val createAnimModifier: @Composable S.() -> Modifier
}

/**
 * ページ遷移中、遷移元の[PageComposable.contentComposable]内のComposableに
 * 適用されるModifier。
 *
 * @see PageTransitionSpec
 */
class CurrentPageTransitionElementAnim(
   override val createAnimModifier:
         @Composable CurrentPageTransitionElementAnimScope.() -> Modifier
) : PageTransitionElementAnim<CurrentPageTransitionElementAnimScope>()

/**
 * ページ遷移中、遷移先の[PageComposable.contentComposable]内のComposableに
 * 適用されるModifier。
 *
 * @see PageTransitionSpec
 */
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
