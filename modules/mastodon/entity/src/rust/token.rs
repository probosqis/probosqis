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
use chrono::{DateTime, Utc};
use serde::Deserialize;

#[cfg(feature="jvm")]
use {
   ext_panoptiqon::convert_java_helper::CloneIntoJava,
   ext_panoptiqon::convert_java_helper::ConvertJavaHelper,
   jni::JNIEnv,
   jni::objects::JObject,
   jni::sys::jvalue,
   panoptiqon::convert_java::ConvertJava,
};
use panoptiqon::cache::Cache;

use crate::instance::Instance;

#[derive(Deserialize)]
pub struct Token {
   pub instance: Cache<Instance>,
   pub access_token: String,
   pub token_type: String,
   pub scope: String,
   pub created_at: DateTime<Utc>,
}

#[cfg(feature="jvm")]
static HELPER: ConvertJavaHelper<5> = ConvertJavaHelper::new(
   "com/wcaokaze/probosqis/mastodon/entity/Token",
   CloneIntoJava::ViaConstructor(
      "(Lcom/wcaokaze/probosqis/panoptiqon/Cache;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;J)V"
   ),
   [
      ("getInstance",             "Lcom/wcaokaze/probosqis/panoptiqon/Cache;"),
      ("getAccessToken",          "Ljava/lang/String;"),
      ("getTokenType",            "Ljava/lang/String;"),
      ("getScope",                "Ljava/lang/String;"),
      ("getCreatedAtEpochMillis", "J"),
   ]
);

#[cfg(feature="jvm")]
impl ConvertJava for Token {
   fn clone_into_java<'local>(&self, env: &mut JNIEnv<'local>) -> JObject<'local> {
      let instance                = self.instance                     .clone_into_java(env);
      let access_token            = self.access_token                 .clone_into_java(env);
      let token_type              = self.token_type                   .clone_into_java(env);
      let scope                   = self.scope                        .clone_into_java(env);
      let created_at_epoch_millis = self.created_at.timestamp_millis();

      let args = [
         jvalue { l: instance         .into_raw() },
         jvalue { l: access_token     .into_raw() },
         jvalue { l: token_type       .into_raw() },
         jvalue { l: scope            .into_raw() },
         jvalue { j: created_at_epoch_millis      },
      ];

      HELPER.clone_into_java(env, &args)
   }

   fn clone_from_java(env: &mut JNIEnv, java_object: &JObject) -> Self {
      let instance_base_url       = HELPER.get(env, &java_object, 0).l().unwrap();
      let access_token            = HELPER.get(env, &java_object, 1).l().unwrap();
      let token_type              = HELPER.get(env, &java_object, 2).l().unwrap();
      let scope                   = HELPER.get(env, &java_object, 3).l().unwrap();
      let created_at_epoch_millis = HELPER.get(env, &java_object, 4).j().unwrap();

      Token {
         instance:     Cache::<Instance>::clone_from_java(env, &instance_base_url),
         access_token: String           ::clone_from_java(env, &access_token),
         token_type:   String           ::clone_from_java(env, &token_type),
         scope:        String           ::clone_from_java(env, &scope),
         created_at: DateTime::from_timestamp_millis(created_at_epoch_millis).unwrap(),
      }
   }
}
