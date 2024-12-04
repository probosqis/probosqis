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
use serde::Deserialize;

#[cfg(feature="jvm")]
use {
   ext_panoptiqon::convert_java_helper::ConvertJavaHelper,
   jni::JNIEnv,
   jni::objects::JObject,
   jni::sys::jvalue,
   panoptiqon::convert_java::ConvertJava,
};
use panoptiqon::cache::Cache;

use crate::instance::Instance;

#[derive(Deserialize)]
pub struct Application {
   pub instance: Cache<Instance>,
   pub name: String,
   pub website: Option<String>,
   pub client_id: Option<String>,
   pub client_secret: Option<String>,
}

#[cfg(feature="jvm")]
const HELPER: ConvertJavaHelper<5> = ConvertJavaHelper::new(
   "com/wcaokaze/probosqis/mastodon/entity/Application",
   "(Lcom/wcaokaze/probosqis/panoptiqon/Cache;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V",
   [
      ("getInstance",     "Lcom/wcaokaze/probosqis/panoptiqon/Cache;"),
      ("getName",         "Ljava/lang/String;"),
      ("getWebsite",      "Ljava/lang/String;"),
      ("getClientId",     "Ljava/lang/String;"),
      ("getClientSecret", "Ljava/lang/String;"),
   ]
);

#[cfg(feature="jvm")]
impl ConvertJava for Application {
   fn clone_into_java<'local>(&self, env: &mut JNIEnv<'local>) -> JObject<'local> {
      let instance      = self.instance     .clone_into_java(env);
      let name          = self.name         .clone_into_java(env);
      let website       = self.website      .clone_into_java(env);
      let client_id     = self.client_id    .clone_into_java(env);
      let client_secret = self.client_secret.clone_into_java(env);

      let args = [
         jvalue { l: instance     .into_raw() },
         jvalue { l: name         .into_raw() },
         jvalue { l: website      .into_raw() },
         jvalue { l: client_id    .into_raw() },
         jvalue { l: client_secret.into_raw() },
      ];

      HELPER.clone_into_java(env, &args)
   }

   fn clone_from_java(env: &mut JNIEnv, java_object: &JObject) -> Self {
      let instance      = HELPER.get(env, &java_object, 0).l().unwrap();
      let name          = HELPER.get(env, &java_object, 1).l().unwrap();
      let website       = HELPER.get(env, &java_object, 2).l().unwrap();
      let client_id     = HELPER.get(env, &java_object, 3).l().unwrap();
      let client_secret = HELPER.get(env, &java_object, 4).l().unwrap();

      Application {
         instance:      Cache::<Instance>::clone_from_java(env, &instance),
         name:          String           ::clone_from_java(env, &name),
         website:       Option::<String> ::clone_from_java(env, &website),
         client_id:     Option::<String> ::clone_from_java(env, &client_id),
         client_secret: Option::<String> ::clone_from_java(env, &client_secret),
      }
   }
}
