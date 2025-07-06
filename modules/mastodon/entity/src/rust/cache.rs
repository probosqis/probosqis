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
use url::Url;
use panoptiqon::cache::CacheContent;
use crate::account::{Account, AccountId, CredentialAccount};
use crate::instance::Instance;
use crate::poll::{NoCredentialPoll, PollId};
use crate::status::{NoCredentialStatus, Status, StatusId};

#[cfg(feature = "jvm")]
use crate::jvm_types::{
   JvmAccount, JvmCredentialAccount, JvmInstance, JvmPollNoCredential, JvmStatus,
   JvmStatusNoCredential,
};

impl CacheContent for Instance {
   type Key = Url;

   #[cfg(feature = "jvm")]
   type JvmType<'local> = JvmInstance<'local>;

   fn key(&self) -> &Url {
      &self.url
   }

   fn file_path_for_key(dir_path: &Path, url: &Url) -> PathBuf {
      use percent_encoding::{utf8_percent_encode, NON_ALPHANUMERIC};

      let encoded_url: String
         = utf8_percent_encode(url.as_str(), NON_ALPHANUMERIC).collect();

      dir_path.join(&encoded_url)
   }
}

impl CacheContent for Account {
   type Key = AccountId;

   #[cfg(feature = "jvm")]
   type JvmType<'local> = JvmAccount<'local>;

   fn key(&self) -> &AccountId {
      &self.id
   }

   fn file_path_for_key(dir_path: &Path, id: &AccountId) -> PathBuf {
      join_id_as_file_path(dir_path, &id.instance_url, &id.local.0)
   }
}

impl CacheContent for CredentialAccount {
   type Key = AccountId;

   #[cfg(feature = "jvm")]
   type JvmType<'local> = JvmCredentialAccount<'local>;

   fn key(&self) -> &AccountId {
      &self.id
   }

   fn file_path_for_key(dir_path: &Path, id: &AccountId) -> PathBuf {
      join_id_as_file_path(dir_path, &id.instance_url, &id.local.0)
   }
}

impl CacheContent for Status {
   type Key = StatusId;

   #[cfg(feature = "jvm")]
   type JvmType<'local> = JvmStatus<'local>;

   fn key(&self) -> &StatusId {
      &self.id
   }

   fn file_path_for_key(dir_path: &Path, id: &StatusId) -> PathBuf {
      join_id_as_file_path(dir_path, &id.instance_url, &id.local.0)
   }
}

impl CacheContent for NoCredentialStatus {
   type Key = StatusId;

   #[cfg(feature = "jvm")]
   type JvmType<'local> = JvmStatusNoCredential<'local>;

   fn key(&self) -> &StatusId {
      &self.id
   }

   fn file_path_for_key(dir_path: &Path, id: &StatusId) -> PathBuf {
      join_id_as_file_path(dir_path, &id.instance_url, &id.local.0)
   }
}

impl CacheContent for NoCredentialPoll {
   type Key = PollId;

   #[cfg(feature = "jvm")]
   type JvmType<'local> = JvmPollNoCredential<'local>;

   fn key(&self) -> &PollId {
      &self.id
   }

   fn file_path_for_key(dir_path: &Path, id: &PollId) -> PathBuf {
      join_id_as_file_path(dir_path, &id.instance_url, &id.local.0)
   }
}

fn join_id_as_file_path(
   dir_path: &Path,
   instance_url: &Url,
   local_id: &str
) -> PathBuf {
   use percent_encoding::{utf8_percent_encode, NON_ALPHANUMERIC};

   let mut path_buf = dir_path.to_path_buf();

   let encoded_url: String
      = utf8_percent_encode(instance_url.as_str(), NON_ALPHANUMERIC).collect();

   path_buf.push(encoded_url);
   path_buf.push(local_id);

   path_buf
}

#[cfg(test)]
mod test {
   #[test]
   fn join_id_as_file_path() {
      use std::path::{Path, PathBuf};

      assert_eq!(
         PathBuf::from("test/join_id_as_file_path/https%3A%2F%2Fexample%2Ecom%2F/local_id"),
         super::join_id_as_file_path(
            &Path::new("test/join_id_as_file_path"),
            &"https://example.com".parse().unwrap(),
            "local_id"
         )
      );
   }
}
