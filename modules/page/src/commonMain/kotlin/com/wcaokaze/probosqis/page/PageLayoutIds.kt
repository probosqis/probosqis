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

package com.wcaokaze.probosqis.page

import com.wcaokaze.probosqis.capsiqum.transition.PageLayoutInfo
import com.wcaokaze.probosqis.pagedeck.GlobalLayoutIds

/**
 * [LayoutId][PageLayoutInfo.LayoutId]をまとめたもの。
 *
 * [PPageComposable]ひとつに対してこのclassを継承したobjectをひとつ用意する。
 *
 * ```kotlin
 * object AccountPageLayoutIds : PageLayoutIds() {
 *    val accountIcon = PageLayoutInfo.LayoutId()
 *    val accountName = PageLayoutInfo.LayoutId()
 * }
 *
 * @Composable
 * fun AccountPage(state: AccountPageState) {
 *    Row {
 *       Image(
 *          state.account.icon,
 *          Modifier
 *             .transitionElement(AccountPageLayoutIds.accountIcon)
 *       )
 *
 *       Text(
 *          state.account.name,
 *          Modifier
 *             .transitionElement(AccountPageLayoutIds.accountName)
 *       )
 *    }
 * }
 * ```
 */
open class PageLayoutIds {
   /**
    * Page内の最も親のComposable。
    * [background]と[content]を子に持つ。
    */
   val root = GlobalLayoutIds.root

   /**
    * Pageの背景。遷移前のPageより手前、[content]よりは奥にある。
    * 遷移アニメーション中でなければPage全体に広がっている。
    */
   val background = GlobalLayoutIds.background

   /**
    * Page本体。[PPageComposable.contentComposable]の親。
    */
   val content = GlobalLayoutIds.content

   /**
    * フッター。[PPageComposable.footerComposable]の親。
    * ヘッダーと異なり、フッターは各Pageごとに別々にコンポーズされ、[content]等と
    * 同様に遷移アニメーションを付与することが可能。
    */
   val footer = GlobalLayoutIds.footer

   companion object : PageLayoutIds()
}
