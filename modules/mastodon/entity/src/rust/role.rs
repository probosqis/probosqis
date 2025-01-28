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
use crate::instance::Instance;

#[cfg(feature = "jvm")]
use {
   ext_panoptiqon::convert_jvm_helper,
   ext_panoptiqon::convert_jvm_helper::{ConvertJniHelper, JvmInstantiationStrategy},
   jni::JNIEnv,
   panoptiqon::convert_jvm::{CloneFromJvm, CloneIntoJvm},
   panoptiqon::jvm_types::JvmString,
   crate::jvm_types::JvmRole,
};

#[derive(Debug, Eq, PartialEq, Clone, Deserialize)]
pub struct Role {
   pub instance: Cache<Instance>,
   pub id: Option<RoleId>,
   pub name: Option<String>,
   pub color: Option<String>,
   pub permissions: Option<String>,
   pub is_highlighted: Option<bool>,
}

#[derive(Debug, Eq, PartialEq, Hash, Clone, Deserialize)]
pub struct RoleId(pub String);

#[cfg(feature = "jvm")]
convert_jvm_helper! {
   static HELPER: ConvertJniHelper<6> = convert_jvm_helper!(
      "com/wcaokaze/probosqis/mastodon/entity/Role",
      JvmInstantiationStrategy::ViaConstructor(
         "(Lcom/wcaokaze/probosqis/panoptiqon/Cache;\
         Ljava/lang/String;\
         Ljava/lang/String;\
         Ljava/lang/String;\
         Ljava/lang/String;\
         Ljava/lang/Boolean;)V"
      ),
      [
         ("getInstance", "Lcom/wcaokaze/probosqis/panoptiqon/Cache;"),
         ("getRawId", "Ljava/lang/String;"),
         ("getName", "Ljava/lang/String;"),
         ("getColor", "Ljava/lang/String;"),
         ("getPermissions", "Ljava/lang/String;"),
         ("isHighlighted", "Ljava/lang/Boolean;"),
      ]
   );
}

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmRole<'local>> for Role {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmRole<'local> {
      use jni::sys::jvalue;
      use panoptiqon::jvm_type::JvmType;
      use panoptiqon::jvm_types::JvmCache;
      use crate::jvm_types::JvmInstance;

      let instance: JvmCache<JvmInstance> = self.instance      .clone_into_jvm(env);
      let id                              = self.id            .clone_into_jvm(env);
      let name                            = self.name          .clone_into_jvm(env);
      let color                           = self.color         .clone_into_jvm(env);
      let permissions                     = self.permissions   .clone_into_jvm(env);
      let is_highlighted                  = self.is_highlighted.clone_into_jvm(env);

      let args = [
         jvalue { l: instance      .j_object().as_raw() },
         jvalue { l: id            .j_object().as_raw() },
         jvalue { l: name          .j_object().as_raw() },
         jvalue { l: color         .j_object().as_raw() },
         jvalue { l: permissions   .j_object().as_raw() },
         jvalue { l: is_highlighted.j_object().as_raw() },
      ];

      let j_object = HELPER.clone_into_jvm(env, &args);
      unsafe { JvmRole::from_j_object(j_object) }
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmRole<'local>> for Role {
   fn clone_from_jvm(
      env: &mut JNIEnv,
      jvm_instance: &JvmRole<'local>
   ) -> Role {
      use panoptiqon::jvm_type::JvmType;
      use panoptiqon::jvm_types::{JvmBoolean, JvmCache, JvmNullable};
      use crate::jvm_types::JvmInstance;

      let instance       = HELPER.get(env, jvm_instance.j_object(), 0).l().unwrap();
      let id             = HELPER.get(env, jvm_instance.j_object(), 1).l().unwrap();
      let name           = HELPER.get(env, jvm_instance.j_object(), 2).l().unwrap();
      let color          = HELPER.get(env, jvm_instance.j_object(), 3).l().unwrap();
      let permissions    = HELPER.get(env, jvm_instance.j_object(), 4).l().unwrap();
      let is_highlighted = HELPER.get(env, jvm_instance.j_object(), 5).l().unwrap();

      let instance       = unsafe { JvmCache::<JvmInstance>  ::from_j_object(instance)       };
      let id             = unsafe { JvmNullable::<JvmString> ::from_j_object(id)             };
      let name           = unsafe { JvmNullable::<JvmString> ::from_j_object(name)           };
      let color          = unsafe { JvmNullable::<JvmString> ::from_j_object(color)          };
      let permissions    = unsafe { JvmNullable::<JvmString> ::from_j_object(permissions)    };
      let is_highlighted = unsafe { JvmNullable::<JvmBoolean>::from_j_object(is_highlighted) };

      Role {
         instance:       Cache::<Instance>::clone_from_jvm(env, &instance),
         id:             Option::<RoleId> ::clone_from_jvm(env, &id),
         name:           Option::<String> ::clone_from_jvm(env, &name),
         color:          Option::<String> ::clone_from_jvm(env, &color),
         permissions:    Option::<String> ::clone_from_jvm(env, &permissions),
         is_highlighted: Option::<bool>   ::clone_from_jvm(env, &is_highlighted),
      }
   }
}

/// com.wcaokaze.probosqis.mastodon.entity.Role.Idではなくjava.lang.Stringに変換する
#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmString<'local>> for RoleId {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmString<'local> {
      self.0.clone_into_jvm(env)
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmString<'local>> for RoleId {
   fn clone_from_jvm(
      env: &mut JNIEnv<'local>,
      jvm_instance: &JvmString<'local>
   ) -> RoleId {
      let id = String::clone_from_jvm(env, jvm_instance);
      RoleId(id)
   }
}
