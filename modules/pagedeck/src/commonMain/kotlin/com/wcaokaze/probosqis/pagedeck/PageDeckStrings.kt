package com.wcaokaze.probosqis.pagedeck

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import com.wcaokaze.probosqis.resources.LocalLanguage
import com.wcaokaze.probosqis.resources.Strings

interface PageDeckStrings {
   val pageStackAppBarCloseDescription: String
}

val Strings.Companion.PageDeck: PageDeckStrings
   @Composable
   @ReadOnlyComposable
   get() = when (LocalLanguage.current) {
      Strings.Language.ENGLISH -> object : PageDeckStrings {
         override val pageStackAppBarCloseDescription = "Close"

      }

      Strings.Language.JAPANESE -> object : PageDeckStrings {
         override val pageStackAppBarCloseDescription = "閉じる"
      }
   }
