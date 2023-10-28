package com.wcaokaze.probosqis.page

import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.layout.LayoutCoordinates

val LocalPageLayoutInfo = compositionLocalOf<MutablePageLayoutInfo> {
   throw IllegalStateException(
      "Attempt to get a PageLayoutInfo from outside a Page")
}

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

interface MutablePageLayoutInfo : PageLayoutInfo {
   operator fun set(id: PageLayoutInfo.LayoutId, coordinates: LayoutCoordinates)
}

internal class PageLayoutInfoImpl : MutablePageLayoutInfo {
   private val map = mutableMapOf<PageLayoutInfo.LayoutId, LayoutCoordinates>()

   override fun get(id: PageLayoutInfo.LayoutId): LayoutCoordinates? = map[id]

   override fun set(id: PageLayoutInfo.LayoutId, coordinates: LayoutCoordinates) {
      map[id] = coordinates
   }
}
