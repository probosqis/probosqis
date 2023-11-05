package com.wcaokaze.probosqis.page

import androidx.annotation.VisibleForTesting
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap

internal val LocalPageTransitionAnimations
   = compositionLocalOf<PageTransitionElementAnimSet> {
      throw IllegalStateException(
         "Attempt to get PageTransitionAnimations from outside a Page")
   }

val LocalPageLayoutInfo
   = compositionLocalOf<MutablePageLayoutInfo> {
      throw IllegalStateException(
         "Attempt to get a PageLayoutInfo from outside a Page")
   }

internal val LocalPageTransition
   = compositionLocalOf<Transition<PageTransitionState>> {
      throw IllegalStateException(
         "Attempt to get a PageTransition from outside a Page")
   }

internal typealias PageTransitionElementAnim
      = @Composable (Transition<PageTransitionState>) -> Modifier

internal typealias PageTransitionElementAnimSet
      = ImmutableMap<PageLayoutInfo.LayoutId, PageTransitionElementAnim>

@Stable
class PageTransitionSpec(
   val enteringCurrentPageElementAnimations: PageTransitionElementAnimSet,
   val enteringTargetPageElementAnimations:  PageTransitionElementAnimSet,
   val exitingCurrentPageElementAnimations:  PageTransitionElementAnimSet,
   val exitingTargetPageElementAnimations:   PageTransitionElementAnimSet
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

   constructor(enter: Builder, exit:  Builder) : this(
      enter.currentPageAnimations.toImmutableMap(),
      enter.targetPageAnimations .toImmutableMap(),
      exit .currentPageAnimations.toImmutableMap(),
      exit .targetPageAnimations .toImmutableMap()
   )

   class Builder {
      internal val currentPageAnimations = mutableMapOf<PageLayoutInfo.LayoutId, PageTransitionElementAnim>()
      internal val targetPageAnimations  = mutableMapOf<PageLayoutInfo.LayoutId, PageTransitionElementAnim>()

      fun currentPageElement(
         id: PageLayoutInfo.LayoutId,
         animationModifier: PageTransitionElementAnim
      ) {
         currentPageAnimations[id] = animationModifier
      }

      fun targetPageElement(
         id: PageLayoutInfo.LayoutId,
         animationModifier: PageTransitionElementAnim
      ) {
         targetPageAnimations[id] = animationModifier
      }
   }
}

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

internal val defaultPageTransitionSpec = pageTransitionSpec(
   enter = {
      targetPageElement(PageLayoutIds.root) { transition ->
         val alpha by transition.animateFloat(
            transitionSpec = { tween() }
         ) {
            if (it.pageId == transition.targetState.pageId) {
               1.0f
            } else {
               0.0f
            }
         }

         val translation by transition.animateFloat(
            transitionSpec = { tween() }
         ) {
            if (it.pageId == transition.targetState.pageId) {
               0.0f
            } else {
               with (LocalDensity.current) { 32.dp.toPx() }
            }
         }

         Modifier.graphicsLayer {
            this.alpha = alpha
            this.translationY = translation
         }
      }
   },
   exit = {
      currentPageElement(PageLayoutIds.root) { transition ->
         val alpha by transition.animateFloat(
            transitionSpec = { tween() }
         ) {
            if (it.pageId == transition.targetState.pageId) {
               0.0f
            } else {
               1.0f
            }
         }

         val translation by transition.animateFloat(
            transitionSpec = { tween() }
         ) {
            if (it.pageId == transition.targetState.pageId) {
               with (LocalDensity.current) { 32.dp.toPx() }
            } else {
               0.0f
            }
         }

         Modifier.graphicsLayer {
            this.alpha = alpha
            this.translationY = translation
         }
      }
   }
)

@Stable
interface PageLayoutInfo {
   @JvmInline
   value class LayoutId private constructor(
      @VisibleForTesting
      internal val id: Long
   ) {
      companion object {
         private var nextId = 0L

         operator fun invoke(): LayoutId = synchronized (this) {
            LayoutId(nextId++)
         }
      }
   }

   operator fun get(id: LayoutId): LayoutCoordinates?
}

open class PageLayoutIds {
   private object GlobalIds {
      val root       = PageLayoutInfo.LayoutId()
      val background = PageLayoutInfo.LayoutId()
      val content    = PageLayoutInfo.LayoutId()
   }

   val root       = GlobalIds.root
   val background = GlobalIds.background
   val content    = GlobalIds.content

   companion object : PageLayoutIds()
}

@Composable
fun Modifier.transitionElement(
   layoutId: PageLayoutInfo.LayoutId
): Modifier {
   val pageLayoutInfo = LocalPageLayoutInfo.current
   val pageTransitionAnimations = LocalPageTransitionAnimations.current
   val transition = LocalPageTransition.current

   val transitionAnimationModifier
      = pageTransitionAnimations[layoutId]?.invoke(transition) ?: Modifier

   return onGloballyPositioned { pageLayoutInfo[layoutId] = it }
      .then(transitionAnimationModifier)
}

@Stable
interface MutablePageLayoutInfo : PageLayoutInfo {
   operator fun set(id: PageLayoutInfo.LayoutId, coordinates: LayoutCoordinates)
}

@Stable
internal class PageLayoutInfoImpl : MutablePageLayoutInfo {
   private val map = mutableStateMapOf<PageLayoutInfo.LayoutId, LayoutCoordinates>()

   override fun get(id: PageLayoutInfo.LayoutId): LayoutCoordinates? = map[id]

   override fun set(id: PageLayoutInfo.LayoutId, coordinates: LayoutCoordinates) {
      map[id] = coordinates
   }
}
