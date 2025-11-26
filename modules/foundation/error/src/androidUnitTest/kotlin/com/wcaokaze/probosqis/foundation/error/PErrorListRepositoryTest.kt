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

package com.wcaokaze.probosqis.foundation.error

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.wcaokaze.probosqis.capsiqum.page.PageId
import com.wcaokaze.probosqis.ext.kotlintest.loadNativeLib
import com.wcaokaze.probosqis.panoptiqon.Repository
import com.wcaokaze.probosqis.panoptiqon.WritableCache
import com.wcaokaze.probosqis.panoptiqon.compose.asMutableState
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.fail

@RunWith(RobolectricTestRunner::class)
class PErrorListRepositoryTest {
   @get:Rule
   val rule = createComposeRule()

   init {
      loadNativeLib()
   }

   @Serializable
   @SerialName("com.wcaokaze.probosqis.foundation.error.PErrorListRepositoryTest\$ErrorImpl")
   private data class ErrorImpl(val i: Int) : PError() {
      override fun restorePage() = fail()
   }

   @Suppress("TestFunctionName")
   private fun RaisedError(
      id: Long,
      error: PError,
      raiserPageId: PageId
   ) = RaisedError(
      RaisedError.Id(id),
      error,
      raiserPageId
   )

   private class PErrorListRepository : AbstractPErrorListRepository(
      allErrorSerializers = listOf(
         errorSerializer<ErrorImpl>(),
      ),
      allPageSerializers = listOf()
   ) {
      private external fun createPanoptiqonRepository(): Repository<Unit, ErrorListJson>

      private val panoptiqonRepository = createPanoptiqonRepository()

      override fun savePanoptiqon(json: ErrorListJson): WritableCache<ErrorListJson> {
         return panoptiqonRepository.save(json)
      }

      override fun loadPanoptiqon(): WritableCache<ErrorListJson> {
         return panoptiqonRepository.load(Unit)
      }
   }

   @Test
   fun saveLoad() {
      val repository = PErrorListRepository()

      repository.saveErrorList(
         listOf(
            RaisedError(0L, ErrorImpl(0), PageId(0L)),
            RaisedError(1L, ErrorImpl(2), PageId(3L)),
         )
      )

      assertEquals(
         listOf(
            RaisedError(0L, ErrorImpl(0), PageId(0L)),
            RaisedError(1L, ErrorImpl(2), PageId(3L)),
         ),
         repository.loadErrorList().value
      )
   }

   @Test
   fun load_notSaved() {
      val repository = PErrorListRepository()
      assertFailsWith<IOException> {
         repository.loadErrorList()
      }
   }

   @Test
   fun save_viaCache() {
      val repository = PErrorListRepository()

      val cache = repository.saveErrorList(
         listOf(
            RaisedError(0L, ErrorImpl(0), PageId(0L)),
         )
      )

      cache.value = listOf(
         RaisedError(1L, ErrorImpl(2), PageId(3L)),
      )

      assertEquals(
         listOf(
            RaisedError(1L, ErrorImpl(2), PageId(3L)),
         ),
         repository.loadErrorList().value
      )
   }

   @Test
   fun state_recompose() {
      val repository = PErrorListRepository()

      val cache = repository.saveErrorList(
         listOf(
            RaisedError(0L, ErrorImpl(0), PageId(0L)),
         )
      )

      rule.setContent {
         val errorList by cache.asMutableState()

         Column {
            for (error in errorList) {
               Text("${error.id}, ${error.error}, ${error.raiserPageId}")
            }
         }
      }

      rule.onNodeWithText("Id(value=0), ErrorImpl(i=0), PageId(value=0)").assertExists()

      repository.saveErrorList(
         listOf(
            RaisedError(1L, ErrorImpl(2), PageId(3L)),
         )
      )

      rule.onNodeWithText("Id(value=0), ErrorImpl(i=0), PageId(value=0)").assertDoesNotExist()
      rule.onNodeWithText("Id(value=1), ErrorImpl(i=2), PageId(value=3)").assertExists()
   }
}
