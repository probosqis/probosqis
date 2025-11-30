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

use std::path::{Path, PathBuf};
use serde::{Deserialize, Serialize};
use panoptiqon::cache::CacheContent;

#[cfg(feature = "jvm")]
use {
   ext_panoptiqon::convert_jvm_helper,
   jni::JNIEnv,
   panoptiqon::convert_jvm::{CloneFromJvm, CloneIntoJvm},
   panoptiqon::jvm_types::{JvmLong, JvmString},
   crate::jvm_types::JvmSerializedPageStack,
};

#[derive(Debug, Eq, PartialEq, Clone, Serialize, Deserialize)]
pub(crate) struct SerializedPageStack {
   pub id: i64,
   pub json:String
}

impl CacheContent for SerializedPageStack {
   type Key = i64;

   #[cfg(feature = "jvm")]
   type JvmKey<'local> = JvmLong<'local>;

   #[cfg(feature = "jvm")]
   type JvmType<'local> = JvmSerializedPageStack<'local>;

   fn key(&self) -> &i64 {
      &self.id
   }

   fn file_path_for_key(dir_path: &Path, key: &i64) -> PathBuf {
      dir_path.join(key.to_string())
   }
}

#[cfg(feature = "jvm")]
convert_jvm_helper! {
   static SERIALIZED_PAGE_STACK_HELPER = impl struct SerializedPageStackHelper
   where
      jvm_class: "com/wcaokaze/probosqis/app/pagedeck/SerializedPageStack"
   {
      fn clone_into_jvm<'local>(..) -> JvmSerializedPageStack<'local>
      where
         jvm_constructor: "(JLjava/lang/String;)V";

      fn id<'local>(..) -> i64
      where
         jvm_getter_method: "getId",
         jvm_return_type: "J";

      fn json<'local>(..) -> String
      where
         jvm_type: JvmString<'local>,
         jvm_getter_method: "getJson",
         jvm_return_type: "Ljava/lang/String;";
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmSerializedPageStack<'local>> for SerializedPageStack {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmSerializedPageStack<'local> {
      SERIALIZED_PAGE_STACK_HELPER.clone_into_jvm(env, self.id, &self.json)
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmSerializedPageStack<'local>> for SerializedPageStack {
   fn clone_from_jvm(
      env: &mut JNIEnv<'local>,
      jvm_instance: &JvmSerializedPageStack<'local>
   ) -> SerializedPageStack {
      let id   = SERIALIZED_PAGE_STACK_HELPER.id  (env, jvm_instance);
      let json = SERIALIZED_PAGE_STACK_HELPER.json(env, jvm_instance);
      SerializedPageStack { id, json }
   }
}
