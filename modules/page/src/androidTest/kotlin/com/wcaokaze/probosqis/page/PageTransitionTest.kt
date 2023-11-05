package com.wcaokaze.probosqis.page

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.test.junit4.createComposeRule
import com.wcaokaze.probosqis.cache.core.WritableCache
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
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
      val pageLayoutInfo = PageLayoutInfoImpl()
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

         PageStackContent(
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
}
