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
use ext_panoptiqon::PANOPTIQON;
use panoptiqon::cache::CacheContent;

#[cfg(feature = "jvm")]
use {
   jni::JNIEnv,
   jni::objects::{JClass, JString},
   ext_panoptiqon::convert_jvm_helper,
   panoptiqon::convert_jvm::{CloneIntoJvm, CloneFromJvm},
   panoptiqon::jvm_types::{JvmRepository, JvmString, JvmUnit},
   crate::jvm_types::JvmErrorListJson,
};

#[derive(Debug, Eq, PartialEq, Clone, Serialize, Deserialize)]
pub(crate) struct ErrorListJson(String);

impl CacheContent for ErrorListJson {
   type Key = ();

   #[cfg(feature = "jvm")]
   type JvmKey<'local> = JvmUnit<'local>;

   #[cfg(feature = "jvm")]
   type JvmType<'local> = JvmErrorListJson<'local>;

   fn key(&self) -> &() {
      &()
   }

   fn file_path_for_key(dir_path: &Path, _key: &()) -> PathBuf {
      dir_path.join("0")
   }
}

#[cfg(feature = "jvm")]
convert_jvm_helper! {
   static ERROR_LIST_JSON_HELPER = impl struct ErrorListJsonConvertHelper
      where jvm_class: "com/wcaokaze/probosqis/foundation/error/ErrorListJson"
   {
      fn clone_into_jvm<'local>(..) -> JvmErrorListJson<'local>
         where jvm_constructor: "(Ljava/lang/String;)V";

      fn json<'local>(..) -> String
      where
         jvm_type: JvmString<'local>,
         jvm_getter_method: "getJson",
         jvm_return_type: "Ljava/lang/String;";
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmErrorListJson<'local>> for ErrorListJson {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmErrorListJson<'local> {
      ERROR_LIST_JSON_HELPER.clone_into_jvm(env, self.0.as_str())
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmErrorListJson<'local>> for ErrorListJson {
   fn clone_from_jvm(
      env: &mut JNIEnv<'local>,
      jvm_instance: &JvmErrorListJson<'local>
   ) -> ErrorListJson {
      let json = ERROR_LIST_JSON_HELPER.json(env, jvm_instance);
      ErrorListJson(json)
   }
}

#[cfg(feature = "jvm")]
#[no_mangle]
extern "C" fn Java_com_wcaokaze_probosqis_foundation_error_AndroidPErrorListRepositoryKt_createPErrorListRepository<'local>(
   mut env: JNIEnv<'local>,
   _class: JClass<'local>,
   data_dir_path: JString<'local>
) -> JvmRepository<'local, JvmErrorListJson<'local>> {
   use panoptiqon::jvm_repository_creator::JvmRepositoryCreator;

   let data_dir_path: String = env.get_string(&data_dir_path).unwrap().into();

   let repo = PANOPTIQON.new_repository::<ErrorListJson>(
      &mut env,
      Path::new(&data_dir_path).join("errorList")
   );

   let repository_creator = JvmRepositoryCreator::new(&mut env);
   repository_creator.create_jvm_wrapper(&mut env, repo)
}

#[cfg(feature = "jvm")]
#[no_mangle]
extern "C" fn Java_com_wcaokaze_probosqis_foundation_error_DesktopPErrorListRepositoryKt_createPErrorListRepository<'local>(
   mut env: JNIEnv<'local>,
   _class: JClass<'local>,
   data_dir_path: JString<'local>
) -> JvmRepository<'local, JvmErrorListJson<'local>> {
   use panoptiqon::jvm_repository_creator::JvmRepositoryCreator;

   let data_dir_path: String = env.get_string(&data_dir_path).unwrap().into();

   let repo = PANOPTIQON.new_repository::<ErrorListJson>(
      &mut env,
      Path::new(&data_dir_path).join("errorList")
   );

   let repository_creator = JvmRepositoryCreator::new(&mut env);
   repository_creator.create_jvm_wrapper(&mut env, repo)
}

#[cfg(feature = "jni-test")]
mod jni_tests {
   use jni::JNIEnv;
   use jni::objects::JObject;
   use panoptiqon::jvm_types::JvmRepository;
   use crate::jvm_types::JvmErrorListJson;

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_foundation_error_PErrorListRepositoryTest_00024PErrorListRepository_createPanoptiqonRepository<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmRepository<'local, JvmErrorListJson<'local>> {
      use panoptiqon::Panoptiqon;
      use panoptiqon::jvm_repository_creator::JvmRepositoryCreator;
      use crate::error_list_repository::ErrorListJson;

      let panoptiqon = Panoptiqon::new();
      let repo = panoptiqon.new_repository::<ErrorListJson>(
         &mut env,
         "test/PErrorListRepositoryTest"
      );

      let repository_creator = JvmRepositoryCreator::new(&mut env);
      repository_creator.create_jvm_wrapper(&mut env, repo)
   }
}
