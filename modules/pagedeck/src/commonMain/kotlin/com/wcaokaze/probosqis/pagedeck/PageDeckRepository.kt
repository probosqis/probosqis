/*
 * Copyright 2023-2024 wcaokaze
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

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.wcaokaze.probosqis.capsiqum.deck.Deck
import com.wcaokaze.probosqis.capsiqum.page.PageStack
import com.wcaokaze.probosqis.panoptiqon.Cache
import com.wcaokaze.probosqis.panoptiqon.InternalCacheApi
import com.wcaokaze.probosqis.panoptiqon.WritableCache
import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual

internal class PageStackCacheSerializer(
   private val pageStackRepository: PageStackRepository
) : KSerializer<WritableCache<PageStack>> {
   override val descriptor: SerialDescriptor
      get() = PrimitiveSerialDescriptor("Cache", PrimitiveKind.LONG)

   override fun serialize(encoder: Encoder, value: WritableCache<PageStack>) {
      encoder.encodeLong(value.value.id.value)
   }

   override fun deserialize(decoder: Decoder): WritableCache<PageStack> {
      val id = PageStack.Id(decoder.decodeLong())
      return pageStackRepository.loadPageStack(id)
   }
}

@Serializable
sealed class SerializablePageDeck {
   @Serializable
   class Card(
      val id: PageStack.Id,
      @Contextual
      val pageStackCache: WritableCache<PageStack>
   ) : SerializablePageDeck()

   @Serializable
   class Column(
      val children: List<SerializablePageDeck>
   ) : SerializablePageDeck()

   @Serializable
   class Row(
      val children: List<SerializablePageDeck>
   ) : SerializablePageDeck()
}

private fun PageDeck.toSerializable(): SerializablePageDeck {
   fun Deck.Layout<LazyPageStackState>.toSerializable(): SerializablePageDeck {
      return when (this) {
         is Deck.Card -> SerializablePageDeck.Card(
            content.id, content.pageStackCache
         )
         is Deck.Column -> SerializablePageDeck.Column(
            children.map { it.toSerializable() }
         )
         is Deck.Row -> SerializablePageDeck.Row(
            children.map { it.toSerializable() }
         )
      }
   }

   return rootRow.toSerializable()
}

private fun SerializablePageDeck.toPageDeck(): PageDeck {
   check(this is SerializablePageDeck.Row)

   fun SerializablePageDeck.toLayout(): Deck.Layout<LazyPageStackState> {
      return when (this) {
         is SerializablePageDeck.Card -> Deck.Card(
            LazyPageStackState(id, pageStackCache, initialVisibility = true)
         )
         is SerializablePageDeck.Column -> Deck.Column(
            children.map { it.toLayout() }
         )
         is SerializablePageDeck.Row -> Deck.Row(
            children.map { it.toLayout() }
         )
      }
   }

   return Deck(rootRow = toLayout() as Deck.Row)
}

interface PageDeckRepository {
   fun savePageDeck(pageDeck: PageDeck): WritableCache<PageDeck>
   fun loadPageDeck(): WritableCache<PageDeck>
}

abstract class AbstractPageDeckRepository
   internal constructor(
      private val pageStackRepository: PageStackRepository
   )
   : PageDeckRepository
{
   protected val json = Json {
      serializersModule = SerializersModule {
         contextual(PageStackCacheSerializer(pageStackRepository))
      }
   }

   abstract fun saveSerializableDeck(
      deck: SerializablePageDeck
   ): WritableCache<SerializablePageDeck>

   abstract fun loadSerializableDeck(): WritableCache<SerializablePageDeck>

   override fun savePageDeck(pageDeck: PageDeck): WritableCache<PageDeck> {
      val serializable = pageDeck.toSerializable()
      val serializableCache = saveSerializableDeck(serializable)
      return PageDeckCache(serializableCache)
   }

   override fun loadPageDeck(): WritableCache<PageDeck> {
      val serializableCache = loadSerializableDeck()
      return PageDeckCache(serializableCache)
   }

   private class PageDeckCache(
      private val serializableCache: WritableCache<SerializablePageDeck>
   ) : Cache<PageDeck>, WritableCache<PageDeck> {
      // XXX: 本来、
      //     derivedStateOf { serializableCache.asState().value.toPageDeck() }
      // 等としてキャッシュの変更を検知すべきだが、そうするとvalueのセット時等に
      // キャッシュが変更されて即座にtoPageDeck()が再実行されてしまう
      @InternalCacheApi
      override val state = mutableStateOf(serializableCache.value.toPageDeck())

      @InternalCacheApi
      override val mutableState = object : MutableState<PageDeck> {
         override var value: PageDeck
            get() = state.value
            set(value) {
               serializableCache.value = value.toSerializable()
            }

         override fun component1() = state.value
         override fun component2(): (PageDeck) -> Unit {
            return { serializableCache.value = it.toSerializable() }
         }
      }

      @OptIn(InternalCacheApi::class)
      override var value: PageDeck
         get() = state.value
         set(value) {
            state.value = value
            serializableCache.value = value.toSerializable()
         }

      override fun asCache(): Cache<PageDeck> = this
   }
}
