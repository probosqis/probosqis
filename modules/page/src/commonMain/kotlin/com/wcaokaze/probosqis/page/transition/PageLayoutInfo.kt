package com.wcaokaze.probosqis.page.transition

import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.layout.LayoutCoordinates
import com.wcaokaze.probosqis.page.PageStack
import com.wcaokaze.probosqis.page.PageStackBoard

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
