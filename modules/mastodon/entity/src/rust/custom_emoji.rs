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

#[cfg(feature="jvm")]
use {
   jni::JNIEnv,
   jni::objects::JObject,
   ext_panoptiqon::convert_java_helper::{CloneIntoJava, ConvertJavaHelper},
   panoptiqon::convert_java::ConvertJava,
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

#[cfg(feature="jvm")]
static HELPER: ConvertJavaHelper<6> = ConvertJavaHelper::new(
   "com/wcaokaze/probosqis/mastodon/entity/CustomEmoji",
   CloneIntoJava::ViaConstructor(
      "(Lcom/wcaokaze/probosqis/panoptiqon/Cache;\
      Ljava/lang/String;\
      Ljava/lang/String;\
      Ljava/lang/String;\
      Ljava/lang/Boolean;\
      Ljava/lang/String;)V"
   ),
   [
      ("getInstance", "Lcom/wcaokaze/probosqis/panoptiqon/Cache;"),
      ("getShortcode", "Ljava/lang/String;"),
      ("getImageUrl", "Ljava/lang/String;"),
      ("getStaticImageUrl", "Ljava/lang/String;"),
      ("isVisibleInPicker", "Ljava/lang/Boolean;"),
      ("getCategory", "Ljava/lang/String;"),
   ]
);

#[cfg(feature="jvm")]
impl ConvertJava for CustomEmoji {
   fn clone_into_java<'local>(&self, env: &mut JNIEnv<'local>) -> JObject<'local> {
      use jni::sys::jvalue;

      let instance             = self.instance.clone_into_java(env);
      let shortcode            = self.shortcode                                           .clone_into_java(env);
      let image_url            = self.image_url.to_string()                               .clone_into_java(env);
      let static_image_url     = self.static_image_url.as_ref().map(|url| url.to_string()).clone_into_java(env);
      let is_visible_in_picker = self.is_visible_in_picker                                .clone_into_java(env);
      let category             = self.category                                            .clone_into_java(env);

      let args = [
         jvalue { l: instance            .into_raw() },
         jvalue { l: shortcode           .into_raw() },
         jvalue { l: image_url           .into_raw() },
         jvalue { l: static_image_url    .into_raw() },
         jvalue { l: is_visible_in_picker.into_raw() },
         jvalue { l: category            .into_raw() },
      ];

      HELPER.clone_into_java(env, &args)
   }

   fn clone_from_java(env: &mut JNIEnv, java_object: &JObject) -> CustomEmoji {
      let instance             = HELPER.get(env, &java_object, 0).l().unwrap();
      let shortcode            = HELPER.get(env, &java_object, 1).l().unwrap();
      let image_url            = HELPER.get(env, &java_object, 2).l().unwrap();
      let static_image_url     = HELPER.get(env, &java_object, 3).l().unwrap();
      let is_visible_in_picker = HELPER.get(env, &java_object, 4).l().unwrap();
      let category             = HELPER.get(env, &java_object, 5).l().unwrap();

      let instance             = Cache::<Instance>::clone_from_java(env, &instance);
      let shortcode            = String           ::clone_from_java(env, &shortcode);
      let image_url            = String           ::clone_from_java(env, &image_url);
      let static_image_url     = Option::<String> ::clone_from_java(env, &static_image_url);
      let is_visible_in_picker = Option::<bool>   ::clone_from_java(env, &is_visible_in_picker);
      let category             = Option::<String> ::clone_from_java(env, &category);

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
