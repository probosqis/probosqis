package com.wcaokaze.probosqis.page.transition

import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.test.junit4.createComposeRule
import com.wcaokaze.probosqis.page.PageStack
import com.wcaokaze.probosqis.page.PageStackBoard
import io.mockk.mockk
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class PageLayoutInfoTest {
   @get:Rule
   val rule = createComposeRule()

   @Test
   fun id_autoIncrement() {
      val a = PageLayoutInfo.LayoutId()
      val b = PageLayoutInfo.LayoutId()
      val c = PageLayoutInfo.LayoutId()
      assertEquals(1, b.id - a.id)
      assertEquals(2, c.id - a.id)
   }

   @Test
   fun globalIds_companionObjectEqualsSubclasses() {
      val someLayoutIds = object : PageLayoutIds() {}

      assertEquals(someLayoutIds.root,       PageLayoutIds.root)
      assertEquals(someLayoutIds.background, PageLayoutIds.background)
      assertEquals(someLayoutIds.content,    PageLayoutIds.content)
   }

   @Test
   fun getAndSet() {
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
   fun isEmpty() {
      val pageLayoutInfo = PageLayoutInfoImpl(
         PageStackBoard.PageStackId(0L), PageStack.PageId(0L))

      assertTrue(pageLayoutInfo.isEmpty())

      val layoutId = PageLayoutInfo.LayoutId()
      pageLayoutInfo[layoutId] = mockk()

      assertFalse(pageLayoutInfo.isEmpty())
   }
}
