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
use crate::account::{Account, AccountLocalId};
use crate::instance::Instance;

#[cfg(feature = "jvm")]
use crate::jvm_types::{JvmAccount, JvmInstance};

impl CacheContent for Instance {
   type Key = Url;

   #[cfg(feature = "jvm")]
   type JvmType<'local> = JvmInstance<'local>;

   fn key(&self) -> Url {
      self.url.clone()
   }
}

impl CacheContent for Account {
   type Key = (Url, AccountLocalId);

   #[cfg(feature = "jvm")]
   type JvmType<'local> = JvmAccount<'local>;

   fn key(&self) -> (Url, AccountLocalId) {
      let instance_url = self.instance.read().unwrap().url.clone();
      let account_id = self.local_id.clone();

      (instance_url, account_id)
   }
}
