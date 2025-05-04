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

use chrono::{DateTime, Utc};
use panoptiqon::cache::Cache;
use serde::Deserialize;
use crate::account::{AccountId, CredentialAccount};
use crate::instance::Instance;

#[cfg(feature = "jvm")]
use {
   ext_panoptiqon::convert_jvm_helper,
   jni::JNIEnv,
   panoptiqon::convert_jvm::{CloneFromJvm, CloneIntoJvm},
   panoptiqon::jvm_types::{JvmCache, JvmNullable, JvmString},
   crate::jvm_types::{JvmCredentialAccount, JvmInstance, JvmToken},
};

#[derive(Debug, Eq, PartialEq, Clone, Deserialize)]
pub struct Token {
   pub instance: Cache<Instance>,
   pub account: Option<Cache<CredentialAccount>>,
   pub account_id: AccountId,
   pub access_token: String,
   pub token_type: String,
   pub scope: String,
   pub created_at: DateTime<Utc>,
}

#[cfg(feature = "jvm")]
convert_jvm_helper! {
   static HELPER = impl struct TokenConvertHelper
      where jvm_class: "com/wcaokaze/probosqis/mastodon/entity/Token"
   {
      fn clone_into_jvm<'local>(..) -> JvmToken<'local>
         where jvm_constructor: "(\
            Lcom/wcaokaze/probosqis/panoptiqon/Cache;\
            Lcom/wcaokaze/probosqis/panoptiqon/Cache;\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Ljava/lang/String;\
            J\
         )V";

      fn instance<'local>(..) -> Cache<Instance>
         where jvm_type: JvmCache<'local, JvmInstance<'local>>,
               jvm_getter_method: "getInstance",
               jvm_return_type: "Lcom/wcaokaze/probosqis/panoptiqon/Cache;";

      fn account<'local>(..) -> Option<Cache<CredentialAccount>>
         where jvm_type: JvmNullable<'local, JvmCache<'local, JvmCredentialAccount<'local>>>,
               jvm_getter_method: "getAccount",
               jvm_return_type: "Lcom/wcaokaze/probosqis/panoptiqon/Cache;";
      
      fn raw_instance_url<'local>(..) -> String
         where jvm_type: JvmString<'local>,
               jvm_getter_method: "getRawInstanceUrl",
               jvm_return_type: "Ljava/lang/String;";

      fn raw_local_id<'local>(..) -> String
         where jvm_type: JvmString<'local>,
               jvm_getter_method: "getRawLocalId",
               jvm_return_type: "Ljava/lang/String;";

      fn access_token<'local>(..) -> String
         where jvm_type: JvmString<'local>,
               jvm_getter_method: "getAccessToken",
               jvm_return_type: "Ljava/lang/String;";

      fn token_type<'local>(..) -> String
         where jvm_type: JvmString<'local>,
               jvm_getter_method: "getTokenType",
               jvm_return_type: "Ljava/lang/String;";

      fn scope<'local>(..) -> String
         where jvm_type: JvmString<'local>,
               jvm_getter_method: "getScope",
               jvm_return_type: "Ljava/lang/String;";

      fn created_at_epoch_millis<'local>(..) -> i64
         where jvm_getter_method: "getCreatedAtEpochMillis",
               jvm_return_type: "J";
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmToken<'local>> for Token {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmToken<'local> {
      HELPER.clone_into_jvm(
         env,
         &self.instance,
         &self.account,
         self.account_id.instance_url.as_str(),
         &self.account_id.local.0,
         &self.access_token,
         &self.token_type,
         &self.scope,
         self.created_at.timestamp_millis(),
      )
   }
}

#[cfg(feature = "jvm")]
impl<'local> JvmToken<'local> {
   pub fn instance(
      &self,
      env: &mut JNIEnv<'local>
   ) -> JvmCache<'local, JvmInstance<'local>> {
      HELPER.instance_jvm_type(env, self)
   }
}

#[cfg(feature = "jvm")]
// impl<'local> CloneFromJvm<'local, JvmToken<'local>> for Token {
impl<'local> Token {
   pub fn clone_from_jvm(
      env: &mut JNIEnv<'local>,
      jvm_instance: &JvmToken<'local>,
      instance: Cache<Instance>
   ) -> Token {
      use crate::account::AccountLocalId;

   // let instance                = HELPER.instance               (env, jvm_instance);
      let account                 = HELPER.account                (env, jvm_instance);
      let raw_instance_url        = HELPER.raw_instance_url       (env, jvm_instance);
      let raw_local_id            = HELPER.raw_local_id           (env, jvm_instance);
      let access_token            = HELPER.access_token           (env, jvm_instance);
      let token_type              = HELPER.token_type             (env, jvm_instance);
      let scope                   = HELPER.scope                  (env, jvm_instance);
      let created_at_epoch_millis = HELPER.created_at_epoch_millis(env, jvm_instance);

      Token {
         instance,
         account,
         account_id: AccountId {
            instance_url: raw_instance_url.parse().unwrap(),
            local: AccountLocalId(raw_local_id),
         },
         access_token,
         token_type,
         scope,
         created_at: DateTime::from_timestamp_millis(created_at_epoch_millis).unwrap(),
      }
   }
}
