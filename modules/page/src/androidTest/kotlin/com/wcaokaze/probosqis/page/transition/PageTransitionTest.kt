package com.wcaokaze.probosqis.page.transition

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.test.junit4.createComposeRule
import com.wcaokaze.probosqis.cache.core.WritableCache
import com.wcaokaze.probosqis.page.Page
import com.wcaokaze.probosqis.page.PageComposableSwitcher
import com.wcaokaze.probosqis.page.PageStack
import com.wcaokaze.probosqis.page.PageStackBoard
import com.wcaokaze.probosqis.page.PageStackState
import com.wcaokaze.probosqis.page.PageState
import com.wcaokaze.probosqis.page.PageStateStore
import com.wcaokaze.probosqis.page.SingleColumnPageStackBoardState
import com.wcaokaze.probosqis.page.SpyPage
import com.wcaokaze.probosqis.page.pageComposable
import com.wcaokaze.probosqis.page.pageStateFactory
import com.wcaokaze.probosqis.page.spyPageComposable
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame

@RunWith(RobolectricTestRunner::class)
class PageTransitionTest {
   @get:Rule
   val rule = createComposeRule()

   private fun createPageStackState(initialPage: Page): PageStackState {
      val pageStackCache = WritableCache(
         PageStack(
            PageStack.Id(0L),
            PageStack.SavedPageState(
               PageStack.PageId(0L),
               initialPage
            )
         )
      )

      return PageStackState(
         PageStackBoard.PageStackId(0L),
         pageStackCache,
         pageStackBoardState = mockk()
      )
   }

   @Test
   fun pageLayoutInfoId_autoIncrement() {
      val a = PageLayoutInfo.LayoutId()
      val b = PageLayoutInfo.LayoutId()
      val c = PageLayoutInfo.LayoutId()
      assertEquals(1, b.id - a.id)
      assertEquals(2, c.id - a.id)
   }

   @Test
   fun pageLayoutInfo_getAndSet() {
      val pageLayoutInfo = PageLayoutInfoImpl(
         PageStackBoard.PageStackId(0L), PageStack.PageId(0L))
      val layoutId1 = PageLayoutInfo.LayoutId()
      val layoutId2 = PageLayoutInfo.LayoutId()

      val coordinates = mockk<LayoutCoordinates>()
      pageLayoutInfo[layoutId1] = coordinates

      assertSame(coordinates, pageLayoutInfo[layoutId1])
      assertNull(pageLayoutInfo[layoutId2])
   }

   @Test
   fun pageLayoutInfo_getViaCompositionLocal() {
      var pageALayoutInfo: PageLayoutInfo? = null
      var pageBLayoutInfo: PageLayoutInfo? = null

      class PageA : Page()
      class PageAState : PageState()
      val pageAComposable = pageComposable<PageA, PageAState>(
         pageStateFactory { _, _ -> PageAState() },
         content = { _, _, _ ->
            pageALayoutInfo = LocalPageLayoutInfo.current
         },
         header = { _, _, _ -> },
         footer = null,
         pageTransitions = {}
      )

      class PageB : Page()
      class PageBState : PageState()
      val pageBComposable = pageComposable<PageB, PageBState>(
         pageStateFactory { _, _ -> PageBState() },
         content = { _, _, _ ->
            pageBLayoutInfo = LocalPageLayoutInfo.current
         },
         header = { _, _, _ -> },
         footer = null,
         pageTransitions = {}
      )

      val pageStackState = createPageStackState(PageA())

      lateinit var coroutineScope: CoroutineScope
      rule.setContent {
         coroutineScope = rememberCoroutineScope()

         PageTransition(
            pageStackState,
            PageComposableSwitcher(
               listOf(
                  pageAComposable,
                  pageBComposable,
               )
            ),
            PageStateStore(
               listOf(
                  pageAComposable.pageStateFactory,
                  pageBComposable.pageStateFactory,
               ),
               coroutineScope
            )
         )
      }

      rule.runOnIdle {
         assertNotNull(pageALayoutInfo)
         assertNull   (pageBLayoutInfo)
      }

      coroutineScope.launch {
         pageStackState.startPage(PageB())
      }

      rule.runOnIdle {
         assertNotNull(pageALayoutInfo)
         assertNotNull(pageBLayoutInfo)
         assertNotSame(pageALayoutInfo, pageBLayoutInfo)
      }
   }

   @Test
   fun pageLayoutInfo_getFromOutsideOfPage() {
      assertFails {
         rule.setContent {
            LocalPageLayoutInfo.current
         }
         rule.waitForIdle()
      }
   }

   @Test
   fun onlyForefrontComposableIsCalled() {
      val page1 = SpyPage()
      val page2 = SpyPage()

      var pageStack = PageStack(
         PageStack.Id(0L),
         PageStack.SavedPageState(
            PageStack.PageId(0L),
            page1
         )
      )
      pageStack = pageStack.added(
         PageStack.SavedPageState(
            PageStack.PageId(1L),
            page2
         )
      )

      val pageStackState = PageStackState(
         PageStackBoard.PageStackId(pageStack.id.value),
         WritableCache(pageStack),
         mockk<SingleColumnPageStackBoardState>()
      )

      rule.setContent {
         val pageComposableSwitcher = remember {
            PageComposableSwitcher(
               listOf(
                  spyPageComposable,
               )
            )
         }

         val coroutineScope = rememberCoroutineScope()

         val pageStateStore = remember {
            PageStateStore(
               listOf(
                  spyPageComposable.pageStateFactory,
               ),
               coroutineScope
            )
         }

         PageTransition(pageStackState, pageComposableSwitcher, pageStateStore)
      }

      rule.runOnIdle {
         assertEquals(0, page1.recompositionCount)
         assertEquals(1, page2.recompositionCount)
      }
   }

   @Test
   fun pageTransition() {
      val page1 = SpyPage()
      val page2 = SpyPage()
      val page3 = SpyPage()

      val pageStackState by derivedStateOf {
         var pageStack = PageStack(
            PageStack.Id(0L),
            PageStack.SavedPageState(
               PageStack.PageId(0L),
               page1
            )
         )
         pageStack = pageStack.added(
            PageStack.SavedPageState(
               PageStack.PageId(1L),
               page2
            )
         )

         PageStackState(
            PageStackBoard.PageStackId(pageStack.id.value),
            WritableCache(pageStack),
            mockk<SingleColumnPageStackBoardState>()
         )
      }

      rule.setContent {
         val pageComposableSwitcher = remember {
            PageComposableSwitcher(
               listOf(
                  spyPageComposable,
               )
            )
         }

         val coroutineScope = rememberCoroutineScope()

         val pageStateStore = remember {
            PageStateStore(
               listOf(
                  spyPageComposable.pageStateFactory,
               ),
               coroutineScope
            )
         }

         PageTransition(pageStackState, pageComposableSwitcher, pageStateStore)
      }

      rule.runOnIdle {
         assertEquals(0, page1.recompositionCount)
         assertEquals(1, page2.recompositionCount)
         assertEquals(0, page3.recompositionCount)
      }

      pageStackState.pageStackCache.value =
         assertNotNull(pageStackState.pageStack.tailOrNull())

      rule.runOnIdle {
         assertEquals(1, page1.recompositionCount)
         assertEquals(1, page2.recompositionCount)
         assertEquals(0, page3.recompositionCount)
      }

      pageStackState.pageStackCache.value = pageStackState.pageStack.added(
         PageStack.SavedPageState(
            PageStack.PageId(2L),
            page3
         )
      )

      rule.runOnIdle {
         assertEquals(1, page1.recompositionCount)
         assertEquals(1, page2.recompositionCount)
         assertEquals(1, page3.recompositionCount)
      }
   }

   @Test
   fun pageTransition_viaPageStackState() {
      val page1 = SpyPage()
      val page2 = SpyPage()
      val page3 = SpyPage()

      val pageStackId = PageStackBoard.PageStackId(0L)

      val initialPageStack = PageStack(
         PageStack.Id(pageStackId.value),
         PageStack.SavedPageState(
            PageStack.PageId(0L),
            page1
         )
      )

      val pageStackBoardState = mockk<SingleColumnPageStackBoardState> {
         every { removePageStack(any()) } returns Job().apply { complete() }
      }

      val pageStackState = PageStackState(
         pageStackId,
         WritableCache(initialPageStack),
         pageStackBoardState
      )

      rule.setContent {
         val pageComposableSwitcher = remember {
            PageComposableSwitcher(
               listOf(
                  spyPageComposable,
               )
            )
         }

         val coroutineScope = rememberCoroutineScope()

         val pageStateStore = remember {
            PageStateStore(
               listOf(
                  spyPageComposable.pageStateFactory,
               ),
               coroutineScope
            )
         }

         PageTransition(pageStackState, pageComposableSwitcher, pageStateStore)
      }

      rule.runOnIdle {
         assertEquals(1, page1.recompositionCount)
         assertEquals(0, page2.recompositionCount)
         assertEquals(0, page3.recompositionCount)
         verify(inverse = true) { pageStackBoardState.removePageStack(pageStackId) }
      }

      pageStackState.startPage(page2)

      rule.runOnIdle {
         assertEquals(1, page1.recompositionCount)
         assertEquals(1, page2.recompositionCount)
         assertEquals(0, page3.recompositionCount)
         verify(inverse = true) { pageStackBoardState.removePageStack(pageStackId) }
      }

      pageStackState.finishPage()

      rule.runOnIdle {
         assertEquals(2, page1.recompositionCount)
         assertEquals(1, page2.recompositionCount)
         assertEquals(0, page3.recompositionCount)
         verify(inverse = true) { pageStackBoardState.removePageStack(pageStackId) }
      }

      pageStackState.startPage(page3)

      rule.runOnIdle {
         assertEquals(2, page1.recompositionCount)
         assertEquals(1, page2.recompositionCount)
         assertEquals(1, page3.recompositionCount)
         verify(inverse = true) { pageStackBoardState.removePageStack(pageStackId) }
      }

      pageStackState.finishPage()

      rule.runOnIdle {
         assertEquals(3, page1.recompositionCount)
         assertEquals(1, page2.recompositionCount)
         assertEquals(1, page3.recompositionCount)
         verify(inverse = true) { pageStackBoardState.removePageStack(pageStackId) }
      }

      pageStackState.finishPage()

      rule.runOnIdle {
         assertEquals(3, page1.recompositionCount)
         assertEquals(1, page2.recompositionCount)
         assertEquals(1, page3.recompositionCount)
         verify { pageStackBoardState.removePageStack(pageStackId) }
      }
   }
}
