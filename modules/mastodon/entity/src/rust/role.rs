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

#[cfg(feature="jvm")]
use {
   ext_panoptiqon::convert_java_helper::{CloneIntoJava, ConvertJavaHelper},
   jni::JNIEnv,
   jni::objects::JObject,
   panoptiqon::convert_java::ConvertJava,
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

#[cfg(feature="jvm")]
static HELPER: ConvertJavaHelper<6> = ConvertJavaHelper::new(
   "com/wcaokaze/probosqis/mastodon/entity/Role",
   CloneIntoJava::ViaConstructor(
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

#[cfg(feature="jvm")]
impl ConvertJava for Role {
   fn clone_into_java<'local>(&self, env: &mut JNIEnv<'local>) -> JObject<'local> {
      use jni::sys::jvalue;

      let instance             = self.instance.clone_into_java(env);
      let id             = self.id            .clone_into_java(env);
      let name           = self.name          .clone_into_java(env);
      let color          = self.color         .clone_into_java(env);
      let permissions    = self.permissions   .clone_into_java(env);
      let is_highlighted = self.is_highlighted.clone_into_java(env);

      let args = [
         jvalue { l: instance            .into_raw() },
         jvalue { l: id            .into_raw() },
         jvalue { l: name          .into_raw() },
         jvalue { l: color         .into_raw() },
         jvalue { l: permissions   .into_raw() },
         jvalue { l: is_highlighted.into_raw() },
      ];

      HELPER.clone_into_java(env, &args)
   }

   fn clone_from_java(env: &mut JNIEnv, java_object: &JObject) -> Role {
      let instance             = HELPER.get(env, &java_object, 0).l().unwrap();
      let id             = HELPER.get(env, &java_object, 1).l().unwrap();
      let name           = HELPER.get(env, &java_object, 2).l().unwrap();
      let color          = HELPER.get(env, &java_object, 3).l().unwrap();
      let permissions    = HELPER.get(env, &java_object, 4).l().unwrap();
      let is_highlighted = HELPER.get(env, &java_object, 5).l().unwrap();

      Role {
         instance:       Cache::<Instance>::clone_from_java(env, &instance),
         id:             Option::<RoleId> ::clone_from_java(env, &id),
         name:           Option::<String> ::clone_from_java(env, &name),
         color:          Option::<String> ::clone_from_java(env, &color),
         permissions:    Option::<String> ::clone_from_java(env, &permissions),
         is_highlighted: Option::<bool>   ::clone_from_java(env, &is_highlighted),
      }
   }
}

/// com.wcaokaze.probosqis.mastodon.entity.Role.Idではなくjava.lang.Stringに変換する
#[cfg(feature="jvm")]
impl ConvertJava for RoleId {
   fn clone_into_java<'local>(&self, env: &mut JNIEnv<'local>) -> JObject<'local> {
      self.0.clone_into_java(env)
   }

   fn clone_from_java(env: &mut JNIEnv, java_object: &JObject) -> RoleId {
      let id = String::clone_from_java(env, &java_object);
      RoleId(id)
   }
}
