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

use serde::Deserialize;
use panoptiqon::cache::Cache;
use crate::instance::Instance;

#[cfg(feature = "jvm")]
use {
   ext_panoptiqon::convert_jvm_helper,
   jni::JNIEnv,
   panoptiqon::convert_jvm::{CloneFromJvm, CloneIntoJvm},
   panoptiqon::jvm_types::{JvmCache, JvmNullable, JvmString},
   crate::jvm_types::{JvmApplication, JvmInstance},
};

#[derive(Deserialize)]
pub struct Application {
   pub instance: Cache<Instance>,
   pub name: String,
   pub website: Option<String>,
   pub client_id: Option<String>,
   pub client_secret: Option<String>,
}

#[cfg(feature = "jvm")]
convert_jvm_helper! {
   static HELPER = impl struct ApplicationConvertHelper
      where jvm_class: "com/wcaokaze/probosqis/mastodon/entity/Application"
   {
      fn clone_into_jvm<'local>(..) -> JvmApplication<'local>
         where jvm_constructor: "(\
            Lcom/wcaokaze/probosqis/panoptiqon/Cache;\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Ljava/lang/String;\
         )V";

      fn instance<'local>(..) -> JvmCache<'local, JvmInstance<'local>>
         where jvm_getter_method: "getInstance",
               jvm_return_type: "Lcom/wcaokaze/probosqis/panoptiqon/Cache;";

      fn name<'local>(..) -> JvmString<'local>
         where jvm_getter_method: "getName",
               jvm_return_type: "Ljava/lang/String;";

      fn website<'local>(..) -> JvmNullable<'local, JvmString<'local>>
         where jvm_getter_method: "getWebsite",
               jvm_return_type: "Ljava/lang/String;";

      fn client_id<'local>(..) -> JvmNullable<'local, JvmString<'local>>
         where jvm_getter_method: "getClientId",
               jvm_return_type: "Ljava/lang/String;";

      fn client_secret<'local>(..) -> JvmNullable<'local, JvmString<'local>>
         where jvm_getter_method: "getClientSecret",
               jvm_return_type: "Ljava/lang/String;";
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmApplication<'local>> for Application {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmApplication<'local> {
      use panoptiqon::jvm_types::JvmCache;
      use crate::jvm_types::JvmInstance;

      let instance: JvmCache<JvmInstance> = self.instance     .clone_into_jvm(env);
      let name                            = self.name         .clone_into_jvm(env);
      let website                         = self.website      .clone_into_jvm(env);
      let client_id                       = self.client_id    .clone_into_jvm(env);
      let client_secret                   = self.client_secret.clone_into_jvm(env);

      HELPER.clone_into_jvm(
         env,
         instance,
         name,
         website,
         client_id,
         client_secret,
      )
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmApplication<'local>> for Application {
   fn clone_from_jvm(
      env: &mut JNIEnv<'local>,
      jvm_instance: &JvmApplication<'local>
   ) -> Application {
      let instance      = HELPER.instance     (env, jvm_instance);
      let name          = HELPER.name         (env, jvm_instance);
      let website       = HELPER.website      (env, jvm_instance);
      let client_id     = HELPER.client_id    (env, jvm_instance);
      let client_secret = HELPER.client_secret(env, jvm_instance);

      Application {
         instance:      Cache::<Instance>::clone_from_jvm(env, &instance),
         name:          String           ::clone_from_jvm(env, &name),
         website:       Option::<String> ::clone_from_jvm(env, &website),
         client_id:     Option::<String> ::clone_from_jvm(env, &client_id),
         client_secret: Option::<String> ::clone_from_jvm(env, &client_secret),
      }
   }
}
