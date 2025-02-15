/*
 * Copyright 2024-2025 wcaokaze
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

package com.wcaokaze.probosqis.mastodon.ui.auth.callbackwaiter

import android.content.Intent
import com.wcaokaze.probosqis.page.PPageState

object CallbackProcessor {
   fun onNewIntent(
      intent: Intent,
      allVisiblePageStates: Sequence<PPageState<*>>
   ) {
      val uri = intent.data ?: return
      if (uri.host != "probosqis.wcaokaze.com") { return }
      if (uri.path != "/auth/callback") { return }

      val code = uri.getQueryParameter("code") ?: return

      // TODO: 同時に2つ以上の認可プロレスが走らないようにする必要がある
      val pageState = allVisiblePageStates
         .filterIsInstance<CallbackWaiterPageState>()
         .firstOrNull() ?: return

      pageState.saveAuthorizedAccountByCode(code)
   }
}
