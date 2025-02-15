/*
 * Copyright 2023-2025 wcaokaze
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

package com.wcaokaze.probosqis

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import com.wcaokaze.probosqis.app.MultiColumnProbosqis
import com.wcaokaze.probosqis.app.ProbosqisState
import com.wcaokaze.probosqis.app.SingleColumnProbosqis
import com.wcaokaze.probosqis.app.allVisiblePageStates
import com.wcaokaze.probosqis.mastodon.ui.auth.callbackwaiter.CallbackProcessor
import com.wcaokaze.probosqis.resources.ProbosqisTheme

class MainActivity : ComponentActivity() {
   private val probosqisState = ProbosqisState()

   override fun onCreate(savedInstanceState: Bundle?) {
      initializeEdgeToEdge()
      super.onCreate(savedInstanceState)

      setContent {
         ProbosqisTheme {
            BackHandler {
               probosqisState.pageDeckState.activePageStackState.finishPage()
            }

            Box {
               @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
               val windowSizeClass = calculateWindowSizeClass(activity = this@MainActivity)

               if (windowSizeClass.widthSizeClass > WindowWidthSizeClass.Medium) {
                  MultiColumnProbosqis(probosqisState)
               } else {
                  SingleColumnProbosqis(probosqisState)
               }

               NavigationBarScrim()
            }
         }
      }
   }

   override fun onNewIntent(intent: Intent) {
      super.onNewIntent(intent)

      CallbackProcessor.onNewIntent(intent, probosqisState.allVisiblePageStates)
   }
}
