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
   panoptiqon::jvm_types::{JvmRepository, JvmString, JvmUnit},
   crate::jvm_types::JvmSerializedPageDeck,
};

#[derive(Debug, Eq, PartialEq, Clone, Serialize, Deserialize)]
pub(crate) struct SerializedPageDeck {
   pub json: String
}

impl CacheContent for SerializedPageDeck {
   type Key = ();

   #[cfg(feature = "jvm")]
   type JvmKey<'local> = JvmUnit<'local>;

   #[cfg(feature = "jvm")]
   type JvmType<'local> = JvmSerializedPageDeck<'local>;

   fn key(&self) -> &() {
      &()
   }

   fn file_path_for_key(dir_path: &Path, _key: &()) -> PathBuf {
      dir_path.join("0")
   }
}

#[cfg(feature = "jvm")]
convert_jvm_helper! {
   static SERIALIZED_PAGE_DECK_HELPER = impl struct SerializedPageDeckHelper
   where
      jvm_class: "com/wcaokaze/probosqis/app/pagedeck/SerializedPageDeck"
   {
      fn clone_into_jvm<'local>(..) -> JvmSerializedPageDeck<'local>
      where
         jvm_constructor: "(Ljava/lang/String;)V";

      fn json<'local>(..) -> String
      where
         jvm_type: JvmString<'local>,
         jvm_getter_method: "getJson",
         jvm_return_type: "Ljava/lang/String;";
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmSerializedPageDeck<'local>> for SerializedPageDeck {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmSerializedPageDeck<'local> {
      SERIALIZED_PAGE_DECK_HELPER.clone_into_jvm(env, &self.json)
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmSerializedPageDeck<'local>> for SerializedPageDeck {
   fn clone_from_jvm(
      env: &mut JNIEnv<'local>,
      jvm_instance: &JvmSerializedPageDeck<'local>
   ) -> SerializedPageDeck {
      let json = SERIALIZED_PAGE_DECK_HELPER.json(env, jvm_instance);
      SerializedPageDeck { json }
   }
}
