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

package com.wcaokaze.probosqis.pagedeck

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wcaokaze.probosqis.capsiqum.deck.cardCount
import com.wcaokaze.probosqis.capsiqum.deck.sequence
import com.wcaokaze.probosqis.capsiqum.page.Page
import com.wcaokaze.probosqis.capsiqum.page.PageComposable
import com.wcaokaze.probosqis.capsiqum.page.PageId
import com.wcaokaze.probosqis.capsiqum.page.PageStack
import com.wcaokaze.probosqis.capsiqum.page.PageState
import com.wcaokaze.probosqis.capsiqum.page.PageStateStore
import com.wcaokaze.probosqis.capsiqum.page.PageSwitcher
import com.wcaokaze.probosqis.capsiqum.page.PageSwitcherState
import com.wcaokaze.probosqis.capsiqum.page.SavedPageState
import com.wcaokaze.probosqis.capsiqum.transition.transitionElement
import com.wcaokaze.probosqis.panoptiqon.WritableCache
import com.wcaokaze.probosqis.panoptiqon.compose.asState
import com.wcaokaze.probosqis.panoptiqon.update

private val pageFooterHeight = 48.dp

@Immutable
data class PageStackColors(
   val background: Color,
   val content: Color,
   val activationAnimColor: Color,
   val footer: Color,
   val footerContent: Color,
)

@Stable
class PageStackState internal constructor(
   val pageStackId: PageStack.Id,
   internal val pageStackCache: WritableCache<PageStack>,
   val pageDeckState: PageDeckState
) {
   internal val pageStack: PageStack by pageStackCache.asState()
   internal val multiColumnActivationAnimState = MultiColumnPageStackActivationAnimState()

   private val activationBackgroundAlphaAnim = Animatable(0.0f)
   val activationBackgroundAlpha by activationBackgroundAlphaAnim.asState()

   internal suspend fun animateActivationBackground() {
      activationBackgroundAlphaAnim.snapTo(1.0f)
      repeat (2) {
         activationBackgroundAlphaAnim.animateTo(0.0f, snap(150))
         activationBackgroundAlphaAnim.animateTo(1.0f, snap(120))
      }
      activationBackgroundAlphaAnim
         .animateTo(0.0f, tween(500, delayMillis = 200, easing = LinearOutSlowInEasing))
   }

   fun startPage(page: Page) {
      pageStackCache.update {
         it.added(
            SavedPageState(
               PageId(),
               page
            )
         )
      }
   }

   fun finishPage() {
      val tail = pageStackCache.value.tailOrNull()

      if (tail == null) {
         removeFromDeck()
         return
      }

      pageStackCache.value = tail
   }

   fun addColumn(page: Page) {
      val pageStack = PageStack(
         SavedPageState(
            PageId(),
            page
         )
      )

      addColumn(pageStack)
   }

   fun addColumn(pageStack: PageStack) {
      val deck = pageDeckState.deck

      val index = deck.sequence().indexOfFirst { it.content.id == pageStackId }

      val insertionIndex = if (index < 0) {
         deck.cardCount
      } else {
         deck.rootRow.asSequence()
            .map { it.cardCount }
            .runningReduce { acc, leafCount -> acc + leafCount }
            .indexOfFirst { it > index }
            .plus(1)
      }

      pageDeckState.addColumn(insertionIndex, pageStack)
   }

   fun removeFromDeck() {
      pageDeckState.removePageStack(pageStackId)
   }
}

internal enum class FooterPaddingType {
   Entire,  // 背景色を含めた全体
   Content, // 内部のComposableのみ。背景色には適用されない
}

private fun <P : Page, S : PageState> extractPageComposable(
   combined: CombinedPageComposable<P, S>,
   pageStackState: State<PageStackState>,
   colors: State<PageStackColors>,
   windowInsets: State<WindowInsets>,
   horizontalContentPadding: State<Dp>,
   footerStartPaddingType: State<FooterPaddingType>,
   footerEndPaddingType:   State<FooterPaddingType>
): PageComposable<P, S> {
   return PageComposable(
      combined.pageClass,
      combined.pageStateClass,
      composable = { page, pageState ->
         PageContentFooter(
            combined, page, pageState, pageStackState.value,
            colors.value, windowInsets.value, horizontalContentPadding.value,
            footerStartPaddingType.value, footerEndPaddingType.value
         )
      }
   )
}

@Composable
internal fun PageContentFooter(
   savedPageState: SavedPageState,
   pageStackState: PageStackState,
   pageSwitcher: CombinedPageSwitcherState,
   pageStateStore: PageStateStore,
   colors: PageStackColors,
   windowInsets: WindowInsets,
   horizontalContentPadding: Dp = 0.dp,
   footerStartPaddingType: FooterPaddingType = FooterPaddingType.Content,
   footerEndPaddingType:   FooterPaddingType = FooterPaddingType.Content
) {
   val updatedPageStackState           = rememberUpdatedState(pageStackState)
   val updatedColors                   = rememberUpdatedState(colors)
   val updatedWindowInsets             = rememberUpdatedState(windowInsets)
   val updatedHorizontalContentPadding = rememberUpdatedState(horizontalContentPadding)
   val updatedFooterStartPaddingType   = rememberUpdatedState(footerStartPaddingType)
   val updatedFooterEndPaddingType     = rememberUpdatedState(footerEndPaddingType)

   val switcherState = remember(pageSwitcher, pageStateStore) {
      PageSwitcherState(
         pageSwitcher.allPageComposables.map {
            extractPageComposable(
               it, updatedPageStackState,
               updatedColors, updatedWindowInsets, updatedHorizontalContentPadding,
               updatedFooterStartPaddingType, updatedFooterEndPaddingType
            )
         },
         pageStateStore
      )
   }

   PageSwitcher(switcherState, savedPageState)
}

@Composable
private fun <P : Page, S : PageState> PageContentFooter(
   combined: CombinedPageComposable<P, S>,
   page: P,
   pageState: S,
   pageStackState: PageStackState,
   colors: PageStackColors,
   windowInsets: WindowInsets,
   horizontalContentPadding: Dp,
   footerStartPaddingType: FooterPaddingType,
   footerEndPaddingType:   FooterPaddingType
) {
   Box(Modifier.transitionElement(GlobalLayoutIds.root)) {
      PageContentBackground(
         pageStackState, colors,
         modifier = Modifier
            .transitionElement(GlobalLayoutIds.background)
            .fillMaxSize()
      )

      val footerComposable = combined.footerComposable

      PageContent(
         combined.contentComposable, page, pageState,
         pageStackState,
         isFooterShown = footerComposable != null,
         colors.content,
         windowInsets,
         horizontalContentPadding = horizontalContentPadding,
         modifier = Modifier.transitionElement(GlobalLayoutIds.content)
      )

      if (footerComposable != null) {
         PageFooter(
            footerComposable, page, pageState, pageStackState,
            colors.footer, colors.footerContent, windowInsets,
            startContentPadding = when (footerStartPaddingType) {
               FooterPaddingType.Entire  -> 0.dp
               FooterPaddingType.Content -> horizontalContentPadding
            },
            endContentPadding = when (footerEndPaddingType) {
               FooterPaddingType.Entire  -> 0.dp
               FooterPaddingType.Content -> horizontalContentPadding
            },
            modifier = Modifier
               .padding(
                  start = when (footerStartPaddingType) {
                     FooterPaddingType.Entire  -> horizontalContentPadding
                     FooterPaddingType.Content -> 0.dp
                  },
                  end = when (footerEndPaddingType) {
                     FooterPaddingType.Entire  -> horizontalContentPadding
                     FooterPaddingType.Content -> 0.dp
                  }
               )
               .transitionElement(GlobalLayoutIds.footer)
               .align(Alignment.BottomCenter)
         )
      }
   }
}

@Composable
private fun PageContentBackground(
   pageStackState: PageStackState,
   colors: PageStackColors,
   modifier: Modifier = Modifier
) {
   val background = colors.activationAnimColor
      .copy(alpha = colors.activationAnimColor.alpha * pageStackState.activationBackgroundAlpha)
      .compositeOver(colors.background)

   Box(modifier.background(background))
}

@Composable
internal fun <P : Page, S : PageState> PageContent(
   contentComposable: @Composable (P, S, PageStackState, WindowInsets) -> Unit,
   page: P,
   pageState: S,
   pageStackState: PageStackState,
   isFooterShown: Boolean,
   contentColor: Color,
   windowInsets: WindowInsets,
   modifier: Modifier = Modifier,
   horizontalContentPadding: Dp = 0.dp
) {
   Box(
      modifier.padding(horizontal = horizontalContentPadding)
   ) {
      CompositionLocalProvider(
         LocalContentColor provides contentColor,
      ) {
         val contentWindowInsets = if (isFooterShown) {
            windowInsets.add(WindowInsets(bottom = pageFooterHeight))
         } else {
            windowInsets
         }

         contentComposable(page, pageState, pageStackState, contentWindowInsets)
      }
   }
}

@Composable
internal fun <P : Page, S : PageState> PageFooter(
   footerComposable: @Composable (P, S, PageStackState) -> Unit,
   page: P,
   pageState: S,
   pageStackState: PageStackState,
   backgroundColor: Color,
   contentColor: Color,
   windowInsets: WindowInsets,
   modifier: Modifier = Modifier,
   startContentPadding: Dp = 0.dp,
   endContentPadding:   Dp = 0.dp,
) {
   val footerWindowInsets = windowInsets
      .only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)

   Box(
      modifier = modifier
         .background(backgroundColor)
         .pointerInput(Unit) {}
         .windowInsetsPadding(footerWindowInsets)
         .padding(start = startContentPadding, end = endContentPadding)
         .fillMaxWidth()
         .requiredHeight(pageFooterHeight)
   ) {
      CompositionLocalProvider(
         LocalContentColor provides contentColor,
      ) {
         footerComposable(page, pageState, pageStackState)
      }
   }
}
