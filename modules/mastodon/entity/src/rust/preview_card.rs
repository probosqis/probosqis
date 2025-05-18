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

use serde::Deserialize;
use url::Url;
use panoptiqon::cache::Cache;
use crate::account::Account;

#[cfg(feature = "jvm")]
use {
   ext_panoptiqon::convert_jvm_helper,
   jni::JNIEnv,
   panoptiqon::convert_jvm::{CloneFromJvm, CloneIntoJvm},
   panoptiqon::jvm_types::{
      JvmCache, JvmList, JvmLong, JvmNullable, JvmString, JvmUnit,
   },
   crate::jvm_types::{JvmAccount, JvmPreviewCard, JvmPreviewCardAuthor},
};

#[derive(Debug, Eq, PartialEq, Clone, Deserialize)]
pub struct PreviewCard {
   pub url: Option<Url>,
   pub title: Option<String>,
   pub description: Option<String>,
   pub card_type: Option<String>,
   pub authors: Vec<PreviewCardAuthor>,
   pub provider_name: Option<String>,
   pub provider_url: Option<Url>,
   pub html: Option<String>,
   pub width: Option<i64>,
   pub height: Option<i64>,
   pub image_url: Option<Url>,
   pub embed_url: Option<Url>,
   pub blurhash: Option<String>,
}

#[derive(Debug, Eq, PartialEq, Clone, Deserialize)]
pub struct PreviewCardAuthor {
   pub name: Option<String>,
   pub url: Option<Url>,
   pub account: Option<Cache<Account>>,
}

#[cfg(feature = "jvm")]
convert_jvm_helper! {
   static PREVIEW_CARD_HELPER = impl struct PreviewCardHelper
      where jvm_class: "com/wcaokaze/probosqis/mastodon/entity/PreviewCard"
   {
      fn clone_into_jvm<'local>(..) -> JvmPreviewCard<'local>
         where jvm_constructor: "(\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Ljava/util/List;\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Ljava/lang/Long;\
            Ljava/lang/Long;\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Lkotlin/Unit;\
         )V";

      fn raw_url<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getRawUrl",
               jvm_return_type: "Ljava/lang/String;";

      fn title<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getTitle",
               jvm_return_type: "Ljava/lang/String;";

      fn description<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getDescription",
               jvm_return_type: "Ljava/lang/String;";

      fn raw_card_type<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getRawCardType",
               jvm_return_type: "Ljava/lang/String;";

      fn authors<'local>(..) -> Vec<PreviewCardAuthor>
         where jvm_type: JvmList<'local, JvmPreviewCardAuthor<'local>>,
               jvm_getter_method: "getAuthors",
               jvm_return_type: "Ljava/util/List;";

      fn provider_name<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getProviderName",
               jvm_return_type: "Ljava/lang/String;";

      fn raw_provider_url<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getRawProviderUrl",
               jvm_return_type: "Ljava/lang/String;";

      fn html<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getHtml",
               jvm_return_type: "Ljava/lang/String;";

      fn width<'local>(..) -> Option<i64>
         where jvm_type: JvmNullable<'local, JvmLong<'local>>,
               jvm_getter_method: "getWidth",
               jvm_return_type: "Ljava/lang/Long;";

      fn height<'local>(..) -> Option<i64>
         where jvm_type: JvmNullable<'local, JvmLong<'local>>,
               jvm_getter_method: "getHeight",
               jvm_return_type: "Ljava/lang/Long;";

      fn raw_image_url<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getRawImageUrl",
               jvm_return_type: "Ljava/lang/String;";

      fn raw_embed_url<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getRawEmbedUrl",
               jvm_return_type: "Ljava/lang/String;";

      fn blurhash<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getBlurhash",
               jvm_return_type: "Ljava/lang/String;";

      fn dummy<'local>(..) -> Option<()>
         where jvm_type: JvmNullable<'local, JvmUnit<'local>>,
               jvm_getter_method: "getDummy",
               jvm_return_type: "Lkotlin/Unit;";
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmPreviewCard<'local>> for PreviewCard {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmPreviewCard<'local> {
      PREVIEW_CARD_HELPER.clone_into_jvm(
         env,
         &self.url.as_ref().map(|u| u.as_str()),
         &self.title,
         &self.description,
         &self.card_type,
         &self.authors,
         &self.provider_name,
         &self.provider_url.as_ref().map(|u| u.as_str()),
         &self.html,
         &self.width,
         &self.height,
         &self.image_url.as_ref().map(|u| u.as_str()),
         &self.embed_url.as_ref().map(|u| u.as_str()),
         &self.blurhash,
         &None::<()>,
      )
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmPreviewCard<'local>> for PreviewCard {
   fn clone_from_jvm(
      env: &mut JNIEnv<'local>,
      jvm_instance: &JvmPreviewCard<'local>
   ) -> PreviewCard {
      let raw_url          = PREVIEW_CARD_HELPER.raw_url         (env, jvm_instance);
      let title            = PREVIEW_CARD_HELPER.title           (env, jvm_instance);
      let description      = PREVIEW_CARD_HELPER.description     (env, jvm_instance);
      let raw_card_type    = PREVIEW_CARD_HELPER.raw_card_type   (env, jvm_instance);
      let authors          = PREVIEW_CARD_HELPER.authors         (env, jvm_instance);
      let provider_name    = PREVIEW_CARD_HELPER.provider_name   (env, jvm_instance);
      let raw_provider_url = PREVIEW_CARD_HELPER.raw_provider_url(env, jvm_instance);
      let html             = PREVIEW_CARD_HELPER.html            (env, jvm_instance);
      let width            = PREVIEW_CARD_HELPER.width           (env, jvm_instance);
      let height           = PREVIEW_CARD_HELPER.height          (env, jvm_instance);
      let raw_image_url    = PREVIEW_CARD_HELPER.raw_image_url   (env, jvm_instance);
      let raw_embed_url    = PREVIEW_CARD_HELPER.raw_embed_url   (env, jvm_instance);
      let blurhash         = PREVIEW_CARD_HELPER.blurhash        (env, jvm_instance);

      PreviewCard {
         url: raw_url.map(|u| u.parse().unwrap()),
         title,
         description,
         card_type: raw_card_type,
         authors,
         provider_name,
         provider_url: raw_provider_url.map(|u| u.parse().unwrap()),
         html,
         width,
         height,
         image_url: raw_image_url.map(|u| u.parse().unwrap()),
         embed_url: raw_embed_url.map(|u| u.parse().unwrap()),
         blurhash,
      }
   }
}

#[cfg(feature = "jvm")]
convert_jvm_helper! {
   static PREVIEW_CARD_AUTHOR_HELPER = impl struct PreviewCardAuthorHelper
      where jvm_class: "com/wcaokaze/probosqis/mastodon/entity/PreviewCard$Author"
   {
      fn clone_into_jvm<'local>(..) -> JvmPreviewCardAuthor<'local>
         where jvm_constructor: "(\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Lcom/wcaokaze/probosqis/panoptiqon/Cache;\
            Lkotlin/Unit;\
         )V";

      fn name<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getName",
               jvm_return_type: "Ljava/lang/String;";

      fn raw_url<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getRawUrl",
               jvm_return_type: "Ljava/lang/String;";

      fn account<'local>(..) -> Option<Cache<Account>>
         where jvm_type: JvmNullable<'local, JvmCache<'local, JvmAccount<'local>>>,
               jvm_getter_method: "getAccount",
               jvm_return_type: "Lcom/wcaokaze/probosqis/panoptiqon/Cache;";

      fn dummy<'local>(..) -> Option<()>
         where jvm_type: JvmNullable<'local, JvmUnit<'local>>,
               jvm_getter_method: "getDummy",
               jvm_return_type: "Lkotlin/Unit;";
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmPreviewCardAuthor<'local>> for PreviewCardAuthor {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmPreviewCardAuthor<'local> {
      PREVIEW_CARD_AUTHOR_HELPER.clone_into_jvm(
         env,
         &self.name,
         &self.url.as_ref().map(|u| u.as_str()),
         &self.account,
         &None::<()>,
      )
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmPreviewCardAuthor<'local>> for PreviewCardAuthor {
   fn clone_from_jvm(
      env: &mut JNIEnv<'local>,
      jvm_instance: &JvmPreviewCardAuthor<'local>
   ) -> PreviewCardAuthor {
      let name    = PREVIEW_CARD_AUTHOR_HELPER.name   (env, jvm_instance);
      let raw_url = PREVIEW_CARD_AUTHOR_HELPER.raw_url(env, jvm_instance);
      let account = PREVIEW_CARD_AUTHOR_HELPER.account(env, jvm_instance);

      PreviewCardAuthor {
         name,
         url: raw_url.map(|u| u.parse().unwrap()),
         account,
      }
   }
}
