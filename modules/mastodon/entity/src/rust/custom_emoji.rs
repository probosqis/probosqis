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

use panoptiqon::cache::Cache;
use serde::Deserialize;
use url::Url;
use crate::instance::Instance;

#[cfg(feature = "jvm")]
use {
   jni::JNIEnv,
   ext_panoptiqon::convert_jvm_helper,
   panoptiqon::convert_jvm::{CloneFromJvm, CloneIntoJvm},
   panoptiqon::jvm_types::{JvmBoolean, JvmCache, JvmNullable, JvmString, JvmUnit},
   crate::jvm_types::{JvmCustomEmoji, JvmInstance},
};

#[derive(Debug, Eq, PartialEq, Clone, Deserialize)]
pub struct CustomEmoji {
   pub instance: Cache<Instance>,
   pub shortcode: String,
   pub image_url: Url,
   pub static_image_url: Option<Url>,
   pub is_visible_in_picker: Option<bool>,
   pub category: Option<String>,
}

#[cfg(feature = "jvm")]
convert_jvm_helper! {
   static HELPER = impl struct CustomEmojiConvertHelper
      where jvm_class: "com/wcaokaze/probosqis/mastodon/entity/CustomEmoji"
   {
      fn clone_into_jvm<'local>(..) -> JvmCustomEmoji<'local>
         where jvm_constructor: "(\
            Lcom/wcaokaze/probosqis/panoptiqon/Cache;\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Ljava/lang/Boolean;\
            Ljava/lang/String;\
            Lkotlin/Unit;\
         )V";

      fn instance<'local>(..) -> Cache<Instance>
         where jvm_type: JvmCache<'local, JvmInstance<'local>>,
               jvm_getter_method: "getInstance",
               jvm_return_type: "Lcom/wcaokaze/probosqis/panoptiqon/Cache;";

      fn shortcode<'local>(..) -> String
         where jvm_type: JvmString<'local>,
               jvm_getter_method: "getShortcode",
               jvm_return_type: "Ljava/lang/String;";

      fn raw_image_url<'local>(..) -> String
         where jvm_type: JvmString<'local>,
               jvm_getter_method: "getRawImageUrl",
               jvm_return_type: "Ljava/lang/String;";

      fn raw_static_image_url<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getRawStaticImageUrl",
               jvm_return_type: "Ljava/lang/String;";

      fn is_visible_in_picker<'local>(..) -> Option<bool>
         where jvm_type: JvmNullable<'local, JvmBoolean<'local>>,
               jvm_getter_method: "isVisibleInPicker",
               jvm_return_type: "Ljava/lang/Boolean;";

      fn category<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getCategory",
               jvm_return_type: "Ljava/lang/String;";

      fn dummy<'local>(..) -> Option<()>
         where jvm_type: JvmNullable<'local, JvmUnit<'local>>,
               jvm_getter_method: "getDummy",
               jvm_return_type: "Lkotlin/Unit;";
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmCustomEmoji<'local>> for CustomEmoji {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmCustomEmoji<'local> {
      HELPER.clone_into_jvm(
         env,
         &self.instance,
         &self.shortcode,
         self.image_url.as_str(),
         &self.static_image_url.as_ref().map(Url::as_str),
         &self.is_visible_in_picker,
         &self.category,
         &None::<()>,
      )
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmCustomEmoji<'local>> for CustomEmoji {
   fn clone_from_jvm(
      env: &mut JNIEnv<'local>,
      jvm_instance: &JvmCustomEmoji<'local>
   ) -> CustomEmoji {
      let instance             = HELPER.instance            (env, jvm_instance);
      let shortcode            = HELPER.shortcode           (env, jvm_instance);
      let raw_image_url        = HELPER.raw_image_url       (env, jvm_instance);
      let raw_static_image_url = HELPER.raw_static_image_url(env, jvm_instance);
      let is_visible_in_picker = HELPER.is_visible_in_picker(env, jvm_instance);
      let category             = HELPER.category            (env, jvm_instance);

      CustomEmoji {
         instance,
         shortcode,
         image_url: raw_image_url.parse().unwrap(),
         static_image_url: raw_static_image_url.map(|url| url.parse().unwrap()),
         is_visible_in_picker,
         category,
      }
   }
}
