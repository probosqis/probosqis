/*
 * Copyright 2025 wcaokaze
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

package com.wcaokaze.probosqis.app.setting.account.list

import androidx.compose.runtime.Stable
import com.wcaokaze.probosqis.capsiqum.page.PageStateFactory
import com.wcaokaze.probosqis.foundation.page.PPage
import com.wcaokaze.probosqis.foundation.page.PPageComposable
import com.wcaokaze.probosqis.foundation.page.PPageState
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("com.wcaokaze.probosqis.app.setting.account.list.AccountListPage")
class AccountListPage : PPage()

@Stable
class AccountListPageState : PPageState<AccountListPage>() {
}

val accountListPageComposable = PPageComposable<AccountListPage, AccountListPageState>(
   PageStateFactory { _, _ -> AccountListPageState() },
   header = { _, _ -> },
   content = { _, _, _ -> },
   footer = null,
   pageTransitions = {
   }
)
