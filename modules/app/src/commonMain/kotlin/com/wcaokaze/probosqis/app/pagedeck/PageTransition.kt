/*
 * Copyright 2024 wcaokaze
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wcaokaze.probosqis.app.pagedeck

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.wcaokaze.probosqis.capsiqum.page.PageStack
import com.wcaokaze.probosqis.capsiqum.transition.PageTransitionSpec
import com.wcaokaze.probosqis.capsiqum.transition.PageTransitionState
import com.wcaokaze.probosqis.capsiqum.transition.pageTransitionSpec

internal val defaultPageTransitionSpec = pageTransitionSpec(
   enter = {
      targetPageElement(PageLayoutIds.background) {
         val alpha by transition.animateFloat(
            transitionSpec = { tween() }
         ) {
            if (it.isTargetPage) { 1.0f } else { 0.0f }
         }

         val translation by transition.animateFloat(
            transitionSpec = { tween() }
         ) {
            if (it.isTargetPage) {
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

      targetPageElement(PageLayoutIds.content) {
         val alpha by transition.animateFloat(
            transitionSpec = { tween() }
         ) {
            if (it.isTargetPage) { 1.0f } else { 0.0f }
         }

         val translation by transition.animateFloat(
            transitionSpec = { tween() }
         ) {
            if (it.isTargetPage) {
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

      targetPageElement(PageLayoutIds.footer) {
         val alpha by transition.animateFloat(
            transitionSpec = { tween() }
         ) {
            if (it.isTargetPage) { 1.0f } else { 0.0f }
         }

         Modifier.graphicsLayer {
            this.alpha = alpha
         }
      }
   },
   exit = {
      currentPageElement(PageLayoutIds.background) {
         val alpha by transition.animateFloat(
            transitionSpec = { tween() }
         ) {
            if (it.isCurrentPage) { 1.0f } else { 0.0f }
         }

         val translation by transition.animateFloat(
            transitionSpec = { tween() }
         ) {
            if (it.isCurrentPage) {
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

      currentPageElement(PageLayoutIds.content) {
         val alpha by transition.animateFloat(
            transitionSpec = { tween() }
         ) {
            if (it.isCurrentPage) { 1.0f } else { 0.0f }
         }

         val translation by transition.animateFloat(
            transitionSpec = { tween() }
         ) {
            if (it.isCurrentPage) {
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

      currentPageElement(PageLayoutIds.footer) {
         val alpha by transition.animateFloat(
            transitionSpec = { tween() }
         ) {
            if (it.isCurrentPage) { 1.0f } else { 0.0f }
         }

         Modifier.graphicsLayer {
            this.alpha = alpha
         }
      }
   }
)

@Stable
class PageTransitionStateImpl(
   private val pageSwitcher: CombinedPageSwitcherState
) : PageTransitionState<PageStack>() {
   override fun getKey(state: PageStack) = state.head.id

   override fun PageStack.compareTransitionTo(other: PageStack): Int {
      return this.pageCount.compareTo(other.pageCount)
   }

   override fun getTransitionSpec(
      frontState: PageStack,
      backState: PageStack
   ): PageTransitionSpec {
      val frontPage = frontState.head.page
      val backPage  = backState .head.page

      val frontPageComposable = pageSwitcher[frontPage] ?: TODO()
      val backPageComposable  = pageSwitcher[backPage ] ?: TODO()

      return backPageComposable .pageTransitionSet.getTransitionTo  (frontPage::class)
         ?:  frontPageComposable.pageTransitionSet.getTransitionFrom(backPage ::class)
         ?:  defaultPageTransitionSpec
   }
}
