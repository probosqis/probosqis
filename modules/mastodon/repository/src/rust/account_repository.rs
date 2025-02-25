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

use entity::image_bytes::ImageBytes;
use ext_reqwest::CLIENT;
use mastodon_entity::account::Account;

#[cfg(not(feature = "jvm"))]
use std::marker::PhantomData;

#[cfg(feature = "jvm")]
use jni::JNIEnv;
use panoptiqon::cache::Cache;

pub struct AccountRepository<'jni> {
   #[cfg(not(feature = "jvm"))]
   env: PhantomData<&'jni ()>,
   #[cfg(feature = "jvm")]
   env: JNIEnv<'jni>
}

impl AccountRepository<'_> {
   #[cfg(not(feature = "jvm"))]
   pub fn new() -> AccountRepository<'static> {
      AccountRepository {
         env: PhantomData
      }
   }

   #[cfg(feature = "jvm")]
   pub fn new<'jni>(env: &JNIEnv<'jni>) -> AccountRepository<'jni> {
      AccountRepository {
         env: unsafe { env.unsafe_clone() }
      }
   }

   pub fn get_account_icon(
      &mut self,
      account: Account
   ) -> anyhow::Result<Cache<ImageBytes>> {
      use crate::cache;

      let icon_url = account.avatar_image_url
         .ok_or(anyhow::anyhow!("no avatar image url"))?;

      let bytes = CLIENT.get(icon_url.clone())
         .send()?
         .bytes()?;

      let image_bytes = ImageBytes::new(icon_url, bytes);

      let icon_cache = cache::account_icon::repo()
         .write(#[cfg(feature = "jvm")] &mut self.env)?
         .save(image_bytes);

      Ok(icon_cache)
   }
}

#[cfg(feature = "jvm")]
mod jvm {
   use jni::JNIEnv;
   use jni::objects::JObject;
   use entity::jvm_types::JvmImage;
   use mastodon_entity::jvm_types::JvmAccount;
   use panoptiqon::jvm_types::{JvmCache, JvmNullable};

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_repository_DesktopAccountRepository_getAccountIcon<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      account: JvmAccount<'local>
   ) -> JvmCache<'local, JvmNullable<'local, JvmImage<'local>>> {
      use ext_panoptiqon::unwrap_or_throw::UnwrapOrThrow;

      get_account_icon(&mut env, account)
         .unwrap_or_throw_io_exception(&mut env)
   }

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_mastodon_repository_AndroidAccountRepository_getAccountIcon<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      account: JvmAccount<'local>
   ) -> JvmCache<'local, JvmNullable<'local, JvmImage<'local>>> {
      use ext_panoptiqon::unwrap_or_throw::UnwrapOrThrow;

      get_account_icon(&mut env, account)
         .unwrap_or_throw_io_exception(&mut env)
   }

   fn get_account_icon<'local>(
      env: &mut JNIEnv<'local>,
      account: JvmAccount<'local>
   ) -> anyhow::Result<JvmCache<'local, JvmNullable<'local, JvmImage<'local>>>> {
      use mastodon_entity::account::Account;
      use panoptiqon::convert_jvm::{CloneFromJvm, CloneIntoJvm};
      use crate::account_repository::AccountRepository;

      let account = Account::clone_from_jvm(env, &account);

      let mut account_repository = AccountRepository::new(env);

      let icon = account_repository.get_account_icon(account)?;
      let icon = icon.clone_into_jvm(env);
      Ok(icon)
   }
}
