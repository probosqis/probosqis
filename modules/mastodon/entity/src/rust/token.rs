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
   ext_panoptiqon::convert_jvm_helper::JvmInstantiationStrategy,
   jni::JNIEnv,
   panoptiqon::convert_jvm::{CloneFromJvm, CloneIntoJvm},
   crate::jvm_types::JvmToken,
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
   static HELPER: TokenConvertHelper<5> = convert_jvm_helper!(
      "com/wcaokaze/probosqis/mastodon/entity/Token",
      JvmInstantiationStrategy::ViaConstructor(
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
}

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmToken<'local>> for Token {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmToken<'local> {
      use jni::sys::jvalue;
      use panoptiqon::jvm_type::JvmType;
      use panoptiqon::jvm_types::JvmCache;
      use crate::jvm_types::JvmInstance;

      let instance: JvmCache<JvmInstance> = self.instance    .clone_into_jvm(env);
      let access_token                    = self.access_token.clone_into_jvm(env);
      let token_type                      = self.token_type  .clone_into_jvm(env);
      let scope                           = self.scope       .clone_into_jvm(env);
      let created_at_epoch_millis         = self.created_at.timestamp_millis();

      let args = [
         jvalue { l: instance    .j_object().as_raw() },
         jvalue { l: access_token.j_object().as_raw() },
         jvalue { l: token_type  .j_object().as_raw() },
         jvalue { l: scope       .j_object().as_raw() },
         jvalue { j: created_at_epoch_millis          },
      ];

      let j_object = HELPER.clone_into_jvm(env, &args);
      unsafe { JvmToken::from_j_object(j_object) }
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmToken<'local>> for Token {
   fn clone_from_jvm(
      env: &mut JNIEnv<'local>,
      jvm_instance: &JvmToken<'local>
   ) -> Token {
      use panoptiqon::jvm_type::JvmType;
      use panoptiqon::jvm_types::{JvmCache, JvmString};
      use crate::jvm_types::JvmInstance;

      let instance                = HELPER.get(env, jvm_instance.j_object(), 0).l().unwrap();
      let access_token            = HELPER.get(env, jvm_instance.j_object(), 1).l().unwrap();
      let token_type              = HELPER.get(env, jvm_instance.j_object(), 2).l().unwrap();
      let scope                   = HELPER.get(env, jvm_instance.j_object(), 3).l().unwrap();
      let created_at_epoch_millis = HELPER.get(env, jvm_instance.j_object(), 4).j().unwrap();

      let instance     = unsafe { JvmCache::<JvmInstance>::from_j_object(instance)     };
      let access_token = unsafe { JvmString              ::from_j_object(access_token) };
      let token_type   = unsafe { JvmString              ::from_j_object(token_type)   };
      let scope        = unsafe { JvmString              ::from_j_object(scope)        };

      Token {
         instance:     Cache::<Instance>::clone_from_jvm(env, &instance),
         access_token: String           ::clone_from_jvm(env, &access_token),
         token_type:   String           ::clone_from_jvm(env, &token_type),
         scope:        String           ::clone_from_jvm(env, &scope),
         created_at: DateTime::from_timestamp_millis(created_at_epoch_millis).unwrap(),
      }
   }
}
