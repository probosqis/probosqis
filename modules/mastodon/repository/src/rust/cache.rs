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

#[cfg(feature = "jvm")]
use {
   jni::JNIEnv,
   jni::objects::{JClass, JString},
   panoptiqon::jvm_type,
};

#[cfg(feature = "jvm")]
jvm_type! {
   JvmCacheRepositories,
}

#[cfg(feature = "jvm")]
#[no_mangle]
extern "C" fn Java_com_wcaokaze_probosqis_mastodon_repository_CacheRepositoriesKt_createCacheRepositories<'local>(
   mut env: JNIEnv<'local>,
   _class: JClass<'local>,
   data_dir_path: JString<'local>
) -> JvmCacheRepositories<'local> {
   use std::path::Path;
   use foundation_entity::image_bytes::ImageBytes;
   use mastodon_entity::account::{Account, CredentialAccount};
   use mastodon_entity::poll::NoCredentialPoll;
   use panoptiqon::jvm_type::JvmType;
   use panoptiqon::repository::JvmRepositoryCreator;

   let data_dir_path: String = env.get_string(&data_dir_path).unwrap().into();
   let path = Path::new(&data_dir_path).join("mastodon/Account");

   let repository_creator = JvmRepositoryCreator::new(&mut env);

   let account_repo            = repository_creator.create::<Account>          (&mut env, &path);
   let credential_account_repo = repository_creator.create::<CredentialAccount>(&mut env, &path);
   let account_icon_repo       = repository_creator.create::<ImageBytes>       (&mut env, &path);
   let no_credential_poll_repo = repository_creator.create::<NoCredentialPoll> (&mut env, &path);

   let jvm_cache_repositories = env.new_object(
      "com/wcaokaze/probosqis/mastodon/repository/CacheRepositories",
      "(\
         Lcom/wcaokaze/probosqis/panoptiqon/Repository;\
         Lcom/wcaokaze/probosqis/panoptiqon/Repository;\
         Lcom/wcaokaze/probosqis/panoptiqon/Repository;\
         Lcom/wcaokaze/probosqis/panoptiqon/Repository;\
      )V",
      &[
         account_repo           .j_object().into(),
         credential_account_repo.j_object().into(),
         account_icon_repo      .j_object().into(),
         no_credential_poll_repo.j_object().into(),
      ]
   ).unwrap();

   unsafe { JvmCacheRepositories::from_j_object(jvm_cache_repositories) }
}

pub mod instance {
   use ext_panoptiqon::repository_holder::RepositoryHolder;
   use mastodon_entity::instance::Instance;
   use panoptiqon::cache::Cache;

   #[cfg(feature = "jvm")]
   use {
      jni::JNIEnv,
      mastodon_entity::jvm_types::JvmInstance,
      panoptiqon::jvm_types::JvmCache,
   };

   static REPO: RepositoryHolder<Instance>
      = RepositoryHolder::new("mastodon/Instance");

   pub fn repo() -> &'static RepositoryHolder<Instance> {
      &REPO
   }

   #[cfg(feature = "jvm")]
   pub(crate) fn clone_from_jvm<'local>(
      env: &mut JNIEnv<'local>,
      java_instance: &JvmCache<'local, JvmInstance<'local>>,
   ) -> anyhow::Result<Cache<Instance>> {
      use panoptiqon::convert_jvm::CloneFromJvm;
      use panoptiqon::jvm_type::JvmType;

      if env.is_instance_of(
         java_instance.j_object(),
         "com/wcaokaze/probosqis/panoptiqon/RepositoryCache"
      )? {
         Ok(Cache::<Instance>::clone_from_jvm(env, &java_instance))
      } else {
         let instance_java_instance = env.call_method(
            java_instance.j_object(),
            "getValue", "()Ljava/lang/Object;", &[]
         )?.l()?;

         let jvm_instance = unsafe {
            JvmInstance::from_j_object(instance_java_instance)
         };

         let instance = Instance::clone_from_jvm(env, &jvm_instance);

         let mut repo = REPO.write(env)?;
         Ok(repo.save(instance))
      }
   }
}

pub mod status {
   use ext_panoptiqon::repository_holder::RepositoryHolder;
   use mastodon_entity::status::{NoCredentialStatus, Status};

   pub type StatusRepository
      = panoptiqon::repository::Repository<Status>;

   pub type NoCredentialStatusRepository
      = panoptiqon::repository::Repository<NoCredentialStatus>;

   static REPO: RepositoryHolder<Status>
      = RepositoryHolder::new("mastodon/Status");

   static NO_CREDENTIAL_REPO: RepositoryHolder<NoCredentialStatus>
      = RepositoryHolder::new("mastodon/NoCredentialStatus");

   pub fn status_repo() -> &'static RepositoryHolder<Status> {
      &REPO
   }

   pub fn no_credential_status_repo(
   ) -> &'static RepositoryHolder<NoCredentialStatus> {
      &NO_CREDENTIAL_REPO
   }
}
