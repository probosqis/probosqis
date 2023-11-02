package com.wcaokaze.probosqis.page

import androidx.annotation.VisibleForTesting
import androidx.compose.animation.core.Transition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onPlaced
import kotlinx.collections.immutable.ImmutableMap

internal val LocalPageTransitionAnimations
   = compositionLocalOf<PageTransitionAnimations> {
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

internal typealias PageTransitionAnimations
      = ImmutableMap<PageLayoutInfo.LayoutId, PageTransitionElementAnim>

@Stable
internal class PageTransitionSpec(
   val enteringCurrentPageAnimations: PageTransitionAnimations,
   val enteringTargetPageAnimations:  PageTransitionAnimations,
   val exitingCurrentPageAnimations:  PageTransitionAnimations,
   val exitingTargetPageAnimations:   PageTransitionAnimations
) {
   override fun equals(other: Any?): Boolean {
      if (other !is PageTransitionSpec) { return false }
      if (enteringCurrentPageAnimations != other.enteringCurrentPageAnimations) { return false }
      if (enteringTargetPageAnimations  != other.enteringTargetPageAnimations ) { return false }
      if (exitingCurrentPageAnimations  != other.exitingCurrentPageAnimations ) { return false }
      if (exitingTargetPageAnimations   != other.exitingTargetPageAnimations  ) { return false }
      return true
   }

   override fun hashCode(): Int {
      var h = 1
      h = h * 31 + enteringCurrentPageAnimations.hashCode()
      h = h * 31 + enteringTargetPageAnimations .hashCode()
      h = h * 31 + exitingCurrentPageAnimations .hashCode()
      h = h * 31 + exitingTargetPageAnimations  .hashCode()
      return h
   }
}

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
   val root       = PageLayoutInfo.LayoutId()
   val background = PageLayoutInfo.LayoutId()
   val content    = PageLayoutInfo.LayoutId()

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

   return onPlaced { pageLayoutInfo[layoutId] = it }
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
