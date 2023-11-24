package com.wcaokaze.probosqis.page

import androidx.annotation.VisibleForTesting
import androidx.compose.animation.core.ExperimentalTransitionApi
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.createChildTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
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
   = compositionLocalOf<Transition<PageLayoutInfo>> {
      throw IllegalStateException(
         "Attempt to get a PageTransition from outside a Page")
   }

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

internal typealias PageTransitionElementAnimSet
      = ImmutableMap<PageLayoutInfo.LayoutId, PageTransitionElementAnim<*>>
internal typealias CurrentPageTransitionElementAnimSet
      = ImmutableMap<PageLayoutInfo.LayoutId, CurrentPageTransitionElementAnim>
internal typealias TargetPageTransitionElementAnimSet
      = ImmutableMap<PageLayoutInfo.LayoutId, TargetPageTransitionElementAnim>

@Stable
class PageTransitionSpec(
   val enteringCurrentPageElementAnimations: CurrentPageTransitionElementAnimSet,
   val enteringTargetPageElementAnimations:  TargetPageTransitionElementAnimSet,
   val exitingCurrentPageElementAnimations:  CurrentPageTransitionElementAnimSet,
   val exitingTargetPageElementAnimations:   TargetPageTransitionElementAnimSet
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
      targetPageElement(PageLayoutIds.root) {
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
      currentPageElement(PageLayoutIds.root) {
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

   val pageStackId: PageStackBoard.PageStackId
   val pageId: PageStack.PageId

   operator fun get(id: LayoutId): LayoutCoordinates?
}

@Stable
interface MutablePageLayoutInfo : PageLayoutInfo {
   operator fun set(id: PageLayoutInfo.LayoutId, coordinates: LayoutCoordinates)
}

@Stable
internal class PageLayoutInfoImpl(
   override val pageStackId: PageStackBoard.PageStackId,
   override val pageId: PageStack.PageId
) : MutablePageLayoutInfo {
   private val map = mutableStateMapOf<PageLayoutInfo.LayoutId, LayoutCoordinates>()

   override fun get(id: PageLayoutInfo.LayoutId): LayoutCoordinates? = map[id]

   override fun set(id: PageLayoutInfo.LayoutId, coordinates: LayoutCoordinates) {
      map[id] = coordinates
   }

   internal fun isEmpty(): Boolean = map.isEmpty()
}

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

private typealias PageComposableArguments
      = Triple<PageStack.SavedPageState, MutablePageLayoutInfo, PageTransitionElementAnimSet>

@Stable
private class PageTransitionState(
   private val pageStackState: PageStackState,
   private val pageComposableSwitcher: PageComposableSwitcher
) {
   private val layoutInfoMap = mutableMapOf<PageStack.PageId, PageLayoutInfoImpl>()

   private var currentPageIndex = -1
   private var targetPageIndex  = -1
   private var isTargetFirstComposition = false
   private var targetPageState: IndexedValue<PageStack.SavedPageState>? = null

   var visiblePageStates by mutableStateOf(emptyList<PageComposableArguments>())

   private val emptyPageTransitionAnimSet: PageTransitionElementAnimSet
         = persistentMapOf()

   @Composable
   fun updateTransition(): Transition<PageLayoutInfo> {
      /*
       * pageStackState.pageStack.headが変化した際、直前に表示されていたPageを
       * 表示したまま一度裏で遷移先のPageをコンポーズし、PageLayoutInfoが
       * 収集できてから遷移先のPageを表にして遷移アニメーションを開始する。
       * そのため、pageが変化した直後のリコンポジションではTransitionには
       * まだ遷移後のPageは渡さず、PageLayoutInfoが収集できてからTransitionに
       * 遷移後のPageを渡すことになる。
       *
       * | current | target | transitionTarget | isTargetFirstComposition | visiblePages |
       * |---------|--------|------------------|--------------------------|--------------|
       * |       0 |      0 |                0 | false                    | [0]          |
       * |       0 |      1 |                0 | true                     | [1, 0]       |
       * |       0 |      1 |                1 | false                    | [0, 1]       |
       * |       1 |      1 |                1 | false                    | [1]          |
       *
       */

      val pageStack = pageStackState.pageStack

      val targetPageState = pageStack.indexedHead

      val isTargetFirstComposition = getLayoutInfo(targetPageState.value.id).isEmpty()

      val transitionTargetPageState = if (isTargetFirstComposition) {
         this.targetPageState ?: targetPageState
      } else {
         targetPageState
      }

      this.targetPageState = transitionTargetPageState

      val transition = updateTransition(
         transitionTargetPageState,
         label = "PageStackContentTransition"
      )

      val currentPageState = transition.currentState

      if (currentPageState.index   != currentPageIndex ||
          targetPageState .index   != targetPageIndex  ||
          isTargetFirstComposition != this.isTargetFirstComposition)
      {
         updateVisiblePageStates(
            currentPageState, targetPageState, isTargetFirstComposition)

         currentPageIndex = currentPageState.index
         targetPageIndex  = targetPageState .index
         this.isTargetFirstComposition = isTargetFirstComposition
      }

      @OptIn(ExperimentalTransitionApi::class)
      val pageTransition: Transition<PageLayoutInfo>
            = transition.createChildTransition(label = "PageTransition") {
               getLayoutInfo(it.value.id)
            }

      return pageTransition
   }

   private fun getLayoutInfo(pageId: PageStack.PageId): PageLayoutInfoImpl {
      return layoutInfoMap.getOrPut(pageId) {
         PageLayoutInfoImpl(pageStackState.pageStackId, pageId)
      }
   }

   private fun updateVisiblePageStates(
      currentState: IndexedValue<PageStack.SavedPageState>,
      targetState:  IndexedValue<PageStack.SavedPageState>,
      isTargetFirstComposition: Boolean
   ) {
      val (currentIndex, currentPage) = currentState
      val (targetIndex,  targetPage ) = targetState

      visiblePageStates = when {
         currentIndex < targetIndex -> {
            val currentPageComposable = pageComposableSwitcher[currentPage.page] ?: TODO()
            val targetPageComposable  = pageComposableSwitcher[targetPage .page] ?: TODO()

            val transitionSpec
                  =  currentPageComposable.pageTransitionSet.getTransitionTo  (targetPage .page::class)
                  ?: targetPageComposable .pageTransitionSet.getTransitionFrom(currentPage.page::class)
                  ?: defaultPageTransitionSpec

            if (isTargetFirstComposition) {
               listOf(
                  Triple(targetPage,  getLayoutInfo(targetPage .id), emptyPageTransitionAnimSet),
                  Triple(currentPage, getLayoutInfo(currentPage.id), emptyPageTransitionAnimSet)
               )
            } else {
               listOf(
                  Triple(currentPage, getLayoutInfo(currentPage.id), transitionSpec.enteringCurrentPageElementAnimations),
                  Triple(targetPage,  getLayoutInfo(targetPage .id), transitionSpec.enteringTargetPageElementAnimations)
               )
            }
         }
         currentIndex > targetIndex -> {
            val currentPageComposable = pageComposableSwitcher[currentPage.page] ?: TODO()
            val targetPageComposable  = pageComposableSwitcher[targetPage .page] ?: TODO()

            val transitionSpec
                  =  targetPageComposable .pageTransitionSet.getTransitionTo  (currentPage.page::class)
                  ?: currentPageComposable.pageTransitionSet.getTransitionFrom(targetPage .page::class)
                  ?: defaultPageTransitionSpec

            if (isTargetFirstComposition) {
               listOf(
                  Triple(targetPage,  getLayoutInfo(targetPage .id), emptyPageTransitionAnimSet),
                  Triple(currentPage, getLayoutInfo(currentPage.id), emptyPageTransitionAnimSet)
               )
            } else {
               listOf(
                  Triple(targetPage,  getLayoutInfo(targetPage .id), transitionSpec.exitingTargetPageElementAnimations),
                  Triple(currentPage, getLayoutInfo(currentPage.id), transitionSpec.exitingCurrentPageElementAnimations)
               )
            }
         }
         else -> {
            listOf(
               Triple(targetPage, getLayoutInfo(targetPage.id), emptyPageTransitionAnimSet)
            )
         }
      }

      val iter = layoutInfoMap.keys.iterator()
      while (iter.hasNext()) {
         val pageId = iter.next()

         if (visiblePageStates.none { it.first.id == pageId }) {
            iter.remove()
         }
      }
   }
}

@Composable
internal fun PageTransition(
   pageStackState: PageStackState,
   pageComposableSwitcher: PageComposableSwitcher,
   pageStateStore: PageStateStore
) {
   val transitionState = remember(pageStackState, pageComposableSwitcher) {
      PageTransitionState(pageStackState, pageComposableSwitcher)
   }

   val transition = transitionState.updateTransition()

   Box {
      val backgroundColor = MaterialTheme.colorScheme
         .surfaceColorAtElevation(LocalAbsoluteTonalElevation.current)

      for ((savedPageState, layoutInfo, transitionAnimations)
         in transitionState.visiblePageStates)
      {
         key(savedPageState.id) {
            CompositionLocalProvider(
               LocalPageLayoutInfo provides layoutInfo,
               LocalPageTransitionAnimations provides transitionAnimations,
               LocalPageTransition provides transition,
            ) {
               Box(Modifier.transitionElement(PageLayoutIds.root)) {
                  Box(
                     Modifier
                        .transitionElement(PageLayoutIds.background)
                        .fillMaxSize()
                        .background(backgroundColor)
                  )

                  Box(Modifier.transitionElement(PageLayoutIds.content)) {
                     PageContent(
                        savedPageState,
                        pageComposableSwitcher,
                        pageStateStore,
                        pageStackState
                     )
                  }
               }
            }
         }
      }
   }
}
