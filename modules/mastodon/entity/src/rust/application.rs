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
   ext_panoptiqon::convert_jvm_helper::{ConvertJniHelper, JvmInstantiationStrategy},
   jni::JNIEnv,
   panoptiqon::convert_jvm::{CloneFromJvm, CloneIntoJvm},
   crate::jvm_types::JvmApplication,
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
static HELPER: ConvertJniHelper<5> = convert_jvm_helper!(
   "com/wcaokaze/probosqis/mastodon/entity/Application",
   JvmInstantiationStrategy::ViaConstructor(
      "(Lcom/wcaokaze/probosqis/panoptiqon/Cache;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V"
   ),
   [
      ("getInstance",     "Lcom/wcaokaze/probosqis/panoptiqon/Cache;"),
      ("getName",         "Ljava/lang/String;"),
      ("getWebsite",      "Ljava/lang/String;"),
      ("getClientId",     "Ljava/lang/String;"),
      ("getClientSecret", "Ljava/lang/String;"),
   ]
);

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmApplication<'local>> for Application {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmApplication<'local> {
      use jni::sys::jvalue;
      use panoptiqon::jvm_type::JvmType;
      use panoptiqon::jvm_types::JvmCache;
      use crate::jvm_types::JvmInstance;

      let instance: JvmCache<JvmInstance> = self.instance     .clone_into_jvm(env);
      let name                            = self.name         .clone_into_jvm(env);
      let website                         = self.website      .clone_into_jvm(env);
      let client_id                       = self.client_id    .clone_into_jvm(env);
      let client_secret                   = self.client_secret.clone_into_jvm(env);

      let args = [
         jvalue { l: instance     .j_object().as_raw() },
         jvalue { l: name         .j_object().as_raw() },
         jvalue { l: website      .j_object().as_raw() },
         jvalue { l: client_id    .j_object().as_raw() },
         jvalue { l: client_secret.j_object().as_raw() },
      ];

      let j_object = HELPER.clone_into_jvm(env, &args);
      unsafe { JvmApplication::from_j_object(j_object) }
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmApplication<'local>> for Application {
   fn clone_from_jvm(
      env: &mut JNIEnv,
      jvm_instance: &JvmApplication<'local>
   ) -> Application {
      use panoptiqon::jvm_type::JvmType;
      use panoptiqon::jvm_types::{JvmCache, JvmNullable, JvmString};
      use crate::jvm_types::JvmInstance;

      let instance      = HELPER.get(env, jvm_instance.j_object(), 0).l().unwrap();
      let name          = HELPER.get(env, jvm_instance.j_object(), 1).l().unwrap();
      let website       = HELPER.get(env, jvm_instance.j_object(), 2).l().unwrap();
      let client_id     = HELPER.get(env, jvm_instance.j_object(), 3).l().unwrap();
      let client_secret = HELPER.get(env, jvm_instance.j_object(), 4).l().unwrap();

      let instance      = unsafe { JvmCache::<JvmInstance> ::from_j_object(instance)      };
      let name          = unsafe { JvmString               ::from_j_object(name)          };
      let website       = unsafe { JvmNullable::<JvmString>::from_j_object(website)       };
      let client_id     = unsafe { JvmNullable::<JvmString>::from_j_object(client_id)     };
      let client_secret = unsafe { JvmNullable::<JvmString>::from_j_object(client_secret) };

      Application {
         instance:      Cache::<Instance>::clone_from_jvm(env, &instance),
         name:          String           ::clone_from_jvm(env, &name),
         website:       Option::<String> ::clone_from_jvm(env, &website),
         client_id:     Option::<String> ::clone_from_jvm(env, &client_id),
         client_secret: Option::<String> ::clone_from_jvm(env, &client_secret),
      }
   }
}
