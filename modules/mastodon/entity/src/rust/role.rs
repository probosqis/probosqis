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
   jni::JNIEnv,
   panoptiqon::convert_jvm::{CloneFromJvm, CloneIntoJvm},
   panoptiqon::jvm_types::{JvmBoolean, JvmCache, JvmNullable, JvmString},
   crate::jvm_types::{JvmInstance, JvmRole},
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
   static HELPER = impl struct RoleConvertHelper
      where jvm_class: "com/wcaokaze/probosqis/mastodon/entity/Role"
   {
      fn clone_into_jvm<'local>(..) -> JvmRole<'local>
         where jvm_constructor: "(\
            Lcom/wcaokaze/probosqis/panoptiqon/Cache;\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Ljava/lang/Boolean;\
         )V";

      fn instance<'local>(..) -> Cache<Instance>
         where jvm_type: JvmCache<'local, JvmInstance<'local>>,
               jvm_getter_method: "getInstance",
               jvm_return_type: "Lcom/wcaokaze/probosqis/panoptiqon/Cache;";

      fn raw_id<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getRawId",
               jvm_return_type: "Ljava/lang/String;";

      fn name<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getName",
               jvm_return_type: "Ljava/lang/String;";

      fn color<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getColor",
               jvm_return_type: "Ljava/lang/String;";

      fn permissions<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getPermissions",
               jvm_return_type: "Ljava/lang/String;";

      fn is_highlighted<'local>(..) -> Option<bool>
         where jvm_type: JvmNullable<'local, JvmBoolean<'local>>,
               jvm_getter_method: "isHighlighted",
               jvm_return_type: "Ljava/lang/Boolean;";
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmRole<'local>> for Role {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmRole<'local> {
      HELPER.clone_into_jvm(
         env,
         &self.instance,
         &self.id,
         &self.name,
         &self.color,
         &self.permissions,
         &self.is_highlighted,
      )
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmRole<'local>> for Role {
   fn clone_from_jvm(
      env: &mut JNIEnv<'local>,
      jvm_instance: &JvmRole<'local>
   ) -> Role {
      let instance       = HELPER.instance      (env, jvm_instance);
      let raw_id         = HELPER.raw_id        (env, jvm_instance);
      let name           = HELPER.name          (env, jvm_instance);
      let color          = HELPER.color         (env, jvm_instance);
      let permissions    = HELPER.permissions   (env, jvm_instance);
      let is_highlighted = HELPER.is_highlighted(env, jvm_instance);

      Role {
         instance,
         id: raw_id.map(RoleId),
         name,
         color,
         permissions,
         is_highlighted,
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
