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

use url::Url;
use panoptiqon::cache::CacheContent;
use crate::account::{Account, AccountId};
use crate::instance::Instance;
use crate::poll::{NoCredentialPoll, PollId};
use crate::status::{NoCredentialStatus, Status, StatusId};

#[cfg(feature = "jvm")]
use crate::jvm_types::{
   JvmAccount, JvmInstance, JvmPollNoCredential, JvmStatus, JvmStatusNoCredential,
};

impl CacheContent for Instance {
   type Key = Url;

   #[cfg(feature = "jvm")]
   type JvmType<'local> = JvmInstance<'local>;

   fn key(&self) -> Url {
      self.url.clone()
   }
}

impl CacheContent for Account {
   type Key = AccountId;

   #[cfg(feature = "jvm")]
   type JvmType<'local> = JvmAccount<'local>;

   fn key(&self) -> AccountId {
      self.id.clone()
   }
}

impl CacheContent for Status {
   type Key = StatusId;

   #[cfg(feature = "jvm")]
   type JvmType<'local> = JvmStatus<'local>;

   fn key(&self) -> StatusId {
      self.id.clone()
   }
}

impl CacheContent for NoCredentialStatus {
   type Key = StatusId;

   #[cfg(feature = "jvm")]
   type JvmType<'local> = JvmStatusNoCredential<'local>;

   fn key(&self) -> StatusId {
      self.id.clone()
   }
}

impl CacheContent for NoCredentialPoll {
   type Key = PollId;

   #[cfg(feature = "jvm")]
   type JvmType<'local> = JvmPollNoCredential<'local>;

   fn key(&self) -> PollId {
      self.id.clone()
   }
}
