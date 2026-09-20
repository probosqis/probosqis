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

use std::sync::LazyLock;
use panoptiqon::Panoptiqon;

#[cfg(feature="jvm")]
use {
   jni::JNIEnv,
   jni::objects::JObject,
   panoptiqon::jvm_types::{JvmCache, JvmCacheId, JvmErased},
   crate::unwrap_or_throw::UnwrapOrThrow,
};

#[cfg(feature="jvm")]
pub mod convert_jvm_helper;

#[cfg(feature="jvm")]
pub mod unwrap_or_throw;

pub static PANOPTIQON: LazyLock<Panoptiqon> = LazyLock::new(|| Panoptiqon::new());

#[cfg(feature="jvm")]
#[no_mangle]
extern "C" fn Java_com_wcaokaze_probosqis_ext_panoptiqon_Panoptiqon_loadById<'local>(
   mut env: JNIEnv<'local>,
   _obj: JObject<'local>,
   id: JvmCacheId<'local>
) -> JvmCache<'local, JvmErased<'local>> {
   PANOPTIQON.load_jvm(&mut env, &id).unwrap_or_throw_io_exception(&mut env)
}

#[cfg(feature="jvm")]
#[no_mangle]
extern "C" fn Java_com_wcaokaze_probosqis_ext_panoptiqon_Panoptiqon_clearInMemoryDb<'local>(
   mut env: JNIEnv<'local>,
   _obj: JObject<'local>
) {
   #[cfg(feature = "testable")]
   {
      PANOPTIQON.clear_in_memory_db();
   }

   #[cfg(not(feature = "testable"))]
   {
      env.throw_new(
         "java/lang/IllegalStateException",
         "clearInMemoryDb is available only from tests."
      ).unwrap();
   }
}
