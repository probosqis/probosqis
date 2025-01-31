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
   ext_panoptiqon::convert_jvm_helper::JvmInstantiationStrategy,
   panoptiqon::convert_jvm::{CloneFromJvm, CloneIntoJvm},
   panoptiqon::jvm_types::{JvmBoolean, JvmCache, JvmNullable, JvmString},
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
   static HELPER = impl struct CustomEmojiConvertHelper<6>
      where jvm_class: "com/wcaokaze/probosqis/mastodon/entity/CustomEmoji"
   {
      fn clone_into_jvm<'local>(..) -> JvmCustomEmoji<'local>
         where JvmInstantiationStrategy::ViaConstructor(
            "(Lcom/wcaokaze/probosqis/panoptiqon/Cache;\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Ljava/lang/Boolean;\
            Ljava/lang/String;)V"
         );

      fn instance<'local>(..) -> JvmCache<'local, JvmInstance<'local>>
         where jvm_getter_method: "getInstance",
               jvm_return_type: "Lcom/wcaokaze/probosqis/panoptiqon/Cache;";

      fn shortcode<'local>(..) -> JvmString<'local>
         where jvm_getter_method: "getShortcode",
               jvm_return_type: "Ljava/lang/String;";

      fn image_url<'local>(..) -> JvmString<'local>
         where jvm_getter_method: "getImageUrl",
               jvm_return_type: "Ljava/lang/String;";

      fn static_image_url<'local>(..) -> JvmNullable<'local, JvmString<'local>>
         where jvm_getter_method: "getStaticImageUrl",
               jvm_return_type: "Ljava/lang/String;";

      fn is_visible_in_picker<'local>(..) -> JvmNullable<'local, JvmBoolean<'local>>
         where jvm_getter_method: "isVisibleInPicker",
               jvm_return_type: "Ljava/lang/Boolean;";

      fn category<'local>(..) -> JvmNullable<'local, JvmString<'local>>
         where jvm_getter_method: "getCategory",
               jvm_return_type: "Ljava/lang/String;";
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmCustomEmoji<'local>> for CustomEmoji {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmCustomEmoji<'local> {
      use panoptiqon::jvm_type::JvmType;
      use panoptiqon::jvm_types::JvmCache;
      use crate::jvm_types::JvmInstance;

      let instance: JvmCache<JvmInstance> = self.instance                                            .clone_into_jvm(env);
      let shortcode                       = self.shortcode                                           .clone_into_jvm(env);
      let image_url                       = self.image_url.to_string()                               .clone_into_jvm(env);
      let static_image_url                = self.static_image_url.as_ref().map(|url| url.to_string()).clone_into_jvm(env);
      let is_visible_in_picker            = self.is_visible_in_picker                                .clone_into_jvm(env);
      let category                        = self.category                                            .clone_into_jvm(env);

      let j_object = HELPER.clone_into_jvm(
         env,
         instance,
         shortcode,
         image_url,
         static_image_url,
         is_visible_in_picker,
         category,
      );
      unsafe { JvmCustomEmoji::from_j_object(j_object) }
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmCustomEmoji<'local>> for CustomEmoji {
   fn clone_from_jvm(
      env: &mut JNIEnv<'local>,
      jvm_instance: &JvmCustomEmoji<'local>
   ) -> CustomEmoji {
      use panoptiqon::jvm_type::JvmType;
      use panoptiqon::jvm_types::{JvmBoolean, JvmCache, JvmNullable, JvmString};
      use crate::jvm_types::JvmInstance;

      let instance             = HELPER.get(env, jvm_instance.j_object(), 0).l().unwrap();
      let shortcode            = HELPER.get(env, jvm_instance.j_object(), 1).l().unwrap();
      let image_url            = HELPER.get(env, jvm_instance.j_object(), 2).l().unwrap();
      let static_image_url     = HELPER.get(env, jvm_instance.j_object(), 3).l().unwrap();
      let is_visible_in_picker = HELPER.get(env, jvm_instance.j_object(), 4).l().unwrap();
      let category             = HELPER.get(env, jvm_instance.j_object(), 5).l().unwrap();

      let instance             = unsafe { JvmCache::<JvmInstance>  ::from_j_object(instance)             };
      let shortcode            = unsafe { JvmString                ::from_j_object(shortcode)            };
      let image_url            = unsafe { JvmString                ::from_j_object(image_url)            };
      let static_image_url     = unsafe { JvmNullable::<JvmString> ::from_j_object(static_image_url)     };
      let is_visible_in_picker = unsafe { JvmNullable::<JvmBoolean>::from_j_object(is_visible_in_picker) };
      let category             = unsafe { JvmNullable::<JvmString> ::from_j_object(category)             };

      let instance             = Cache::<Instance>::clone_from_jvm(env, &instance);
      let shortcode            = String           ::clone_from_jvm(env, &shortcode);
      let image_url            = String           ::clone_from_jvm(env, &image_url);
      let static_image_url     = Option::<String> ::clone_from_jvm(env, &static_image_url);
      let is_visible_in_picker = Option::<bool>   ::clone_from_jvm(env, &is_visible_in_picker);
      let category             = Option::<String> ::clone_from_jvm(env, &category);

      CustomEmoji {
         instance,
         shortcode,
         image_url: image_url.parse().unwrap(),
         static_image_url: static_image_url.map(|url| url.parse().unwrap()),
         is_visible_in_picker,
         category,
      }
   }
}
