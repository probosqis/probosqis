/*
 * Copyright 2024-2025 wcaokaze
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
use panoptiqon::cache::Cache;
use serde::Deserialize;
use crate::instance::Instance;

#[cfg(feature = "jvm")]
use {
   ext_panoptiqon::convert_jvm_helper,
   jni::JNIEnv,
   panoptiqon::convert_jvm::{CloneFromJvm, CloneIntoJvm},
   panoptiqon::jvm_types::{JvmCache, JvmString},
   crate::jvm_types::{JvmInstance, JvmToken},
};

#[derive(Deserialize)]
pub struct Token {
   pub instance: Cache<Instance>,
   pub access_token: String,
   pub token_type: String,
   pub scope: String,
   pub created_at: DateTime<Utc>,
}

#[cfg(feature = "jvm")]
convert_jvm_helper! {
   static HELPER = impl struct TokenConvertHelper
      where jvm_class: "com/wcaokaze/probosqis/mastodon/entity/Token"
   {
      fn clone_into_jvm<'local>(..) -> JvmToken<'local>
         where jvm_constructor: "(\
            Lcom/wcaokaze/probosqis/panoptiqon/Cache;\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Ljava/lang/String;\
            J\
         )V";

      fn instance<'local>(..) -> JvmCache<'local, JvmInstance<'local>>
         where jvm_getter_method: "getInstance",
               jvm_return_type: "Lcom/wcaokaze/probosqis/panoptiqon/Cache;";

      fn access_token<'local>(..) -> JvmString<'local>
         where jvm_getter_method: "getAccessToken",
               jvm_return_type: "Ljava/lang/String;";

      fn token_type<'local>(..) -> JvmString<'local>
         where jvm_getter_method: "getTokenType",
               jvm_return_type: "Ljava/lang/String;";

      fn scope<'local>(..) -> JvmString<'local>
         where jvm_getter_method: "getScope",
               jvm_return_type: "Ljava/lang/String;";

      fn created_at_epoch_millis<'local>(..) -> i64
         where jvm_getter_method: "getCreatedAtEpochMillis",
               jvm_return_type: "J";
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmToken<'local>> for Token {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmToken<'local> {
      use panoptiqon::jvm_types::JvmCache;
      use crate::jvm_types::JvmInstance;

      let instance: JvmCache<JvmInstance> = self.instance    .clone_into_jvm(env);
      let access_token                    = self.access_token.clone_into_jvm(env);
      let token_type                      = self.token_type  .clone_into_jvm(env);
      let scope                           = self.scope       .clone_into_jvm(env);
      let created_at_epoch_millis         = self.created_at.timestamp_millis();

      HELPER.clone_into_jvm(
         env,
         instance,
         access_token,
         token_type,
         scope,
         created_at_epoch_millis,
      )
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmToken<'local>> for Token {
   fn clone_from_jvm(
      env: &mut JNIEnv<'local>,
      jvm_instance: &JvmToken<'local>
   ) -> Token {
      let instance                = HELPER.instance               (env, jvm_instance);
      let access_token            = HELPER.access_token           (env, jvm_instance);
      let token_type              = HELPER.token_type             (env, jvm_instance);
      let scope                   = HELPER.scope                  (env, jvm_instance);
      let created_at_epoch_millis = HELPER.created_at_epoch_millis(env, jvm_instance);

      Token {
         instance:     Cache::<Instance>::clone_from_jvm(env, &instance),
         access_token: String           ::clone_from_jvm(env, &access_token),
         token_type:   String           ::clone_from_jvm(env, &token_type),
         scope:        String           ::clone_from_jvm(env, &scope),
         created_at: DateTime::from_timestamp_millis(created_at_epoch_millis).unwrap(),
      }
   }
}
