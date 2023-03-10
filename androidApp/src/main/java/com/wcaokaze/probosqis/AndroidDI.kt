/*
 * Copyright 2023 wcaokaze
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

import android.content.Context
import androidx.compose.runtime.Stable
import com.wcaokaze.probosqis.app.DI
import com.wcaokaze.probosqis.app.TestPage
import com.wcaokaze.probosqis.page.compose.PageComposableSwitch
import com.wcaokaze.probosqis.page.compose.pageComposable
import com.wcaokaze.probosqis.page.perpetuation.JvmColumnBoardRepository
import com.wcaokaze.probosqis.page.perpetuation.pageSerializer
import kotlinx.collections.immutable.persistentListOf
import java.io.File

@Stable
class AndroidDI(context: Context) : DI {
   override val pageComposableSwitch = PageComposableSwitch(
      allPageComposables = persistentListOf(
         pageComposable<TestPage> { TestPage(it) },
      )
   )

   override val columnBoardRepository = JvmColumnBoardRepository(
      allPageSerializers = listOf(
         pageSerializer<TestPage>(),
      ),
      File(context.filesDir, "probosqisData/columnBoardCache")
   )
}
