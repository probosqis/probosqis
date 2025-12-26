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
use panoptiqon::cache::{Cache, CacheContent};

#[cfg(feature = "jvm")]
use {
   jni::JNIEnv,
   jni::objects::{JObject, JString},
   ext_panoptiqon::convert_jvm_helper,
   panoptiqon::convert_jvm::{CloneFromJvm, CloneIntoJvm},
   panoptiqon::jvm_types::{JvmCache, JvmList, JvmRepository, JvmString, JvmUnit},
   crate::jvm_types::{JvmCredentialList, JvmSerializedCredential},
};

#[derive(Debug, Eq, PartialEq, Clone, Serialize, Deserialize)]
pub(crate) struct SerializedCredential {
   pub id: String,
   pub json: String
}

#[derive(Debug, Eq, PartialEq, Clone, Serialize, Deserialize)]
pub(crate) struct CredentialList(Vec<Cache<SerializedCredential>>);

impl CacheContent for SerializedCredential {
   type Key = String;

   #[cfg(feature = "jvm")]
   type JvmKey<'local> = JvmString<'local>;

   #[cfg(feature = "jvm")]
   type JvmType<'local> = JvmSerializedCredential<'local>;

   fn key(&self) -> &String {
      &self.id
   }

   fn file_path_for_key(dir_path: &Path, key: &String) -> PathBuf {
      use percent_encoding::{utf8_percent_encode, NON_ALPHANUMERIC};

      let encoded_url: String
         = utf8_percent_encode(key.as_str(), NON_ALPHANUMERIC).collect();

      dir_path.join(encoded_url)
   }
}

impl CacheContent for CredentialList {
   type Key = ();

   #[cfg(feature = "jvm")]
   type JvmKey<'local> = JvmUnit<'local>;

   #[cfg(feature = "jvm")]
   type JvmType<'local> = JvmCredentialList<'local>;

   fn key(&self) -> &() {
      &()
   }

   fn file_path_for_key(dir_path: &Path, _key: &()) -> PathBuf {
      dir_path.join("0")
   }
}

#[cfg(feature = "jvm")]
convert_jvm_helper! {
   static SERIALIZED_CREDENTIAL_HELPER = impl struct SerializedCredentialConvertHelper
   where
      jvm_class: "com/wcaokaze/probosqis/foundation/credential/SerializedCredential"
   {
      fn clone_into_jvm<'local>(..) -> JvmSerializedCredential<'local>
      where
         jvm_constructor: "(Ljava/lang/String;Ljava/lang/String;)V";

      fn id<'local>(..) -> String
      where
         jvm_type: JvmString<'local>,
         jvm_getter_method: "getId",
         jvm_return_type: "Ljava/lang/String;";

      fn json<'local>(..) -> String
      where
         jvm_type: JvmString<'local>,
         jvm_getter_method: "getJson",
         jvm_return_type: "Ljava/lang/String;";
   }

   static CREDENTIAL_LIST_HELPER = impl struct CredentialListHelper
   where
      jvm_class: "com/wcaokaze/probosqis/foundation/credential/CredentialList"
   {
      fn clone_into_jvm<'local>(..) -> JvmCredentialList<'local>
      where
         jvm_constructor: "(Ljava/util/List;)V";

      fn credentials<'local>(..) -> Vec<Cache<SerializedCredential>>
      where
         jvm_type: JvmList<'local, JvmCache<'local, JvmSerializedCredential<'local>>>,
         jvm_getter_method: "getCredentials",
         jvm_return_type: "Ljava/util/List;";
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmSerializedCredential<'local>> for SerializedCredential {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmSerializedCredential<'local> {
      SERIALIZED_CREDENTIAL_HELPER.clone_into_jvm(env, &self.id, &self.json)
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmSerializedCredential<'local>> for SerializedCredential {
   fn clone_from_jvm(
      env: &mut JNIEnv<'local>,
      jvm_instance: &JvmSerializedCredential<'local>
   ) -> SerializedCredential {
      let id   = SERIALIZED_CREDENTIAL_HELPER.id  (env, jvm_instance);
      let json = SERIALIZED_CREDENTIAL_HELPER.json(env, jvm_instance);
      SerializedCredential { id, json }
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmCredentialList<'local>> for CredentialList {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmCredentialList<'local> {
      CREDENTIAL_LIST_HELPER.clone_into_jvm(env, &self.0)
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmCredentialList<'local>> for CredentialList {
   fn clone_from_jvm(
      env: &mut JNIEnv<'local>,
      jvm_instance: &JvmCredentialList<'local>
   ) -> CredentialList {
      let credentials = CREDENTIAL_LIST_HELPER.credentials(env, jvm_instance);
      CredentialList(credentials)
   }
}

#[cfg(feature = "jvm")]
#[no_mangle]
extern "C" fn Java_com_wcaokaze_probosqis_foundation_credential_AndroidCredentialRepository_createCredentialRepository<'local>(
   mut env: JNIEnv<'local>,
   _obj: JObject<'local>,
   data_dir_path: JString<'local>
) -> JvmRepository<'local, JvmSerializedCredential<'local>> {
   use panoptiqon::jvm_repository_creator::JvmRepositoryCreator;

   let data_dir_path: String = env.get_string(&data_dir_path).unwrap().into();

   let repo = PANOPTIQON.new_repository::<SerializedCredential>(
      &mut env,
      Path::new(&data_dir_path).join("credential")
   );

   let repository_creator = JvmRepositoryCreator::new(&mut env);
   repository_creator.create_jvm_wrapper(&mut env, repo)
}

#[cfg(feature = "jvm")]
#[no_mangle]
extern "C" fn Java_com_wcaokaze_probosqis_foundation_credential_DesktopCredentialRepository_createCredentialRepository<'local>(
   mut env: JNIEnv<'local>,
   _obj: JObject<'local>,
   data_dir_path: JString<'local>
) -> JvmRepository<'local, JvmSerializedCredential<'local>> {
   use panoptiqon::jvm_repository_creator::JvmRepositoryCreator;

   let data_dir_path: String = env.get_string(&data_dir_path).unwrap().into();

   let repo = PANOPTIQON.new_repository::<SerializedCredential>(
      &mut env,
      Path::new(&data_dir_path).join("credential")
   );

   let repository_creator = JvmRepositoryCreator::new(&mut env);
   repository_creator.create_jvm_wrapper(&mut env, repo)
}

#[cfg(feature = "jvm")]
#[no_mangle]
extern "C" fn Java_com_wcaokaze_probosqis_foundation_credential_AndroidCredentialRepository_createCredentialListRepository<'local>(
   mut env: JNIEnv<'local>,
   _obj: JObject<'local>,
   data_dir_path: JString<'local>
) -> JvmRepository<'local, JvmCredentialList<'local>> {
   use panoptiqon::jvm_repository_creator::JvmRepositoryCreator;

   let data_dir_path: String = env.get_string(&data_dir_path).unwrap().into();

   let repo = PANOPTIQON.new_repository::<CredentialList>(
      &mut env,
      Path::new(&data_dir_path).join("credentialList")
   );

   let repository_creator = JvmRepositoryCreator::new(&mut env);
   repository_creator.create_jvm_wrapper(&mut env, repo)
}

#[cfg(feature = "jvm")]
#[no_mangle]
extern "C" fn Java_com_wcaokaze_probosqis_foundation_credential_DesktopCredentialRepository_createCredentialListRepository<'local>(
   mut env: JNIEnv<'local>,
   _obj: JObject<'local>,
   data_dir_path: JString<'local>
) -> JvmRepository<'local, JvmCredentialList<'local>> {
   use panoptiqon::jvm_repository_creator::JvmRepositoryCreator;

   let data_dir_path: String = env.get_string(&data_dir_path).unwrap().into();

   let repo = PANOPTIQON.new_repository::<CredentialList>(
      &mut env,
      Path::new(&data_dir_path).join("credentialList")
   );

   let repository_creator = JvmRepositoryCreator::new(&mut env);
   repository_creator.create_jvm_wrapper(&mut env, repo)
}

#[cfg(feature = "jni-test")]
mod jni_tests {
   use jni::JNIEnv;
   use jni::objects::JObject;
   use panoptiqon::jvm_type::JvmType;
   use panoptiqon::jvm_types::{JvmPair, JvmRepository};
   use panoptiqon::Panoptiqon;
   use crate::credential_repository::{CredentialList, SerializedCredential};
   use crate::jvm_types::{JvmCredentialList, JvmSerializedCredential};

   fn create_repositories<'local>(
      env: &mut JNIEnv<'local>
   ) -> JvmPair<'local, JvmRepository<'local, JvmSerializedCredential<'local>>, JvmRepository<'local, JvmCredentialList<'local>>> {
      use panoptiqon::jvm_repository_creator::JvmRepositoryCreator;

      let panoptiqon = Panoptiqon::new();

      let credential_epo = panoptiqon.new_repository::<SerializedCredential>(
         env,
         "test/CredentialRepositoryTest/SerializedCredential"
      );

      let credential_list_repo = panoptiqon.new_repository::<CredentialList>(
         env,
         "test/CredentialRepositoryTest/CredentialList"
      );

      let repository_creator = JvmRepositoryCreator::new(env);
      let credential_repo      = repository_creator.create_jvm_wrapper(env, credential_epo);
      let credential_list_repo = repository_creator.create_jvm_wrapper(env, credential_list_repo);

      let j_object = env.new_object(
         "kotlin/Pair", "(Ljava/lang/Object;Ljava/lang/Object;)V",
         &[credential_repo.j_object().into(), credential_list_repo.j_object().into()]
      ).unwrap();

      unsafe { JvmPair::from_j_object(j_object) }
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_foundation_credential_CredentialRepositoryTest_loadAllCredentials_1emptyIfFileNotFound_00024createRepositories<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmPair<'local, JvmRepository<'local, JvmSerializedCredential<'local>>, JvmRepository<'local, JvmCredentialList<'local>>> {
      create_repositories(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_foundation_credential_CredentialRepositoryTest_saveLoad_00024createRepositories<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmPair<'local, JvmRepository<'local, JvmSerializedCredential<'local>>, JvmRepository<'local, JvmCredentialList<'local>>> {
      create_repositories(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_foundation_credential_CredentialRepositoryTest_save_1distinct_00024createRepositories<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmPair<'local, JvmRepository<'local, JvmSerializedCredential<'local>>, JvmRepository<'local, JvmCredentialList<'local>>> {
      create_repositories(&mut env)
   }
}
