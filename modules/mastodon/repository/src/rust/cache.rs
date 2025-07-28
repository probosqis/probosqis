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

use ext_panoptiqon::PANOPTIQON;

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
   use mastodon_entity::application::Application;
   use mastodon_entity::instance::Instance;
   use mastodon_entity::poll::NoCredentialPoll;
   use mastodon_entity::status::{NoCredentialStatus, Status};
   use panoptiqon::jvm_type::JvmType;
   use panoptiqon::repository::JvmRepositoryCreator;

   let data_dir_path: String = env.get_string(&data_dir_path).unwrap().into();

   let repository_creator = JvmRepositoryCreator::new(&mut env);

   let instance_repo             = PANOPTIQON.new_repository::<Instance>          (&mut env, Path::new(&data_dir_path).join("mastodon/Instance"));
   let application_repo          = PANOPTIQON.new_repository::<Application>       (&mut env, Path::new(&data_dir_path).join("mastodon/Application"));
   let account_repo              = PANOPTIQON.new_repository::<Account>           (&mut env, Path::new(&data_dir_path).join("mastodon/Account"));
   let credential_account_repo   = PANOPTIQON.new_repository::<CredentialAccount> (&mut env, Path::new(&data_dir_path).join("mastodon/CredentialAccount"));
   let account_icon_repo         = PANOPTIQON.new_repository::<ImageBytes>        (&mut env, Path::new(&data_dir_path).join("mastodon/ImageBytes"));
   let status_repo               = PANOPTIQON.new_repository::<Status>            (&mut env, Path::new(&data_dir_path).join("mastodon/Status"));
   let no_credential_status_repo = PANOPTIQON.new_repository::<NoCredentialStatus>(&mut env, Path::new(&data_dir_path).join("mastodon/Status"));
   let no_credential_poll_repo   = PANOPTIQON.new_repository::<NoCredentialPoll>  (&mut env, Path::new(&data_dir_path).join("mastodon/NoCredentialPoll"));
   let instance_repo             = repository_creator.create_jvm_wrapper(&mut env, instance_repo);
   let application_repo          = repository_creator.create_jvm_wrapper(&mut env, application_repo);
   let account_repo              = repository_creator.create_jvm_wrapper(&mut env, account_repo);
   let credential_account_repo   = repository_creator.create_jvm_wrapper(&mut env, credential_account_repo);
   let account_icon_repo         = repository_creator.create_jvm_wrapper(&mut env, account_icon_repo);
   let status_repo               = repository_creator.create_jvm_wrapper(&mut env, status_repo);
   let no_credential_status_repo = repository_creator.create_jvm_wrapper(&mut env, no_credential_status_repo);
   let no_credential_poll_repo   = repository_creator.create_jvm_wrapper(&mut env, no_credential_poll_repo);

   let jvm_cache_repositories = env.new_object(
      "com/wcaokaze/probosqis/mastodon/repository/CacheRepositories",
      "(\
         Lcom/wcaokaze/probosqis/panoptiqon/Repository;\
         Lcom/wcaokaze/probosqis/panoptiqon/Repository;\
         Lcom/wcaokaze/probosqis/panoptiqon/Repository;\
         Lcom/wcaokaze/probosqis/panoptiqon/Repository;\
         Lcom/wcaokaze/probosqis/panoptiqon/Repository;\
         Lcom/wcaokaze/probosqis/panoptiqon/Repository;\
         Lcom/wcaokaze/probosqis/panoptiqon/Repository;\
         Lcom/wcaokaze/probosqis/panoptiqon/Repository;\
      )V",
      &[
         instance_repo            .j_object().into(),
         application_repo         .j_object().into(),
         account_repo             .j_object().into(),
         credential_account_repo  .j_object().into(),
         account_icon_repo        .j_object().into(),
         status_repo              .j_object().into(),
         no_credential_status_repo.j_object().into(),
         no_credential_poll_repo  .j_object().into(),
      ]
   ).unwrap();

   unsafe { JvmCacheRepositories::from_j_object(jvm_cache_repositories) }
}
