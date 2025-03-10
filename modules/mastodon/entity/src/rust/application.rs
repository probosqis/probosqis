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
use url::Url;
use crate::instance::Instance;

#[cfg(feature = "jvm")]
use {
   ext_panoptiqon::convert_jvm_helper,
   jni::JNIEnv,
   panoptiqon::convert_jvm::{CloneFromJvm, CloneIntoJvm},
   panoptiqon::jvm_types::{ JvmCache, JvmList, JvmLong, JvmNullable, JvmString},
   crate::jvm_types::{JvmApplication, JvmInstance},
};

#[derive(Deserialize)]
pub struct Application {
   pub instance: Cache<Instance>,
   pub name: String,
   pub website: Option<Url>,
   pub scopes: Vec<String>,
   pub redirect_uris: Vec<String>,
   pub client_id: Option<String>,
   pub client_secret: Option<String>,
   pub client_secret_expire_time: Option<DateTime<Utc>>,
}

#[cfg(feature = "jvm")]
convert_jvm_helper! {
   static HELPER = impl struct ApplicationConvertHelper
      where jvm_class: "com/wcaokaze/probosqis/mastodon/entity/Application"
   {
      fn clone_into_jvm<'local>(..) -> JvmApplication<'local>
         where jvm_constructor: "(\
            Lcom/wcaokaze/probosqis/panoptiqon/Cache;\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Ljava/util/List;\
            Ljava/util/List;\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Ljava/lang/Long;\
         )V";

      fn instance<'local>(..) -> Cache<Instance>
         where jvm_type: JvmCache<'local, JvmInstance<'local>>,
               jvm_getter_method: "getInstance",
               jvm_return_type: "Lcom/wcaokaze/probosqis/panoptiqon/Cache;";

      fn name<'local>(..) -> String
         where jvm_type: JvmString<'local>,
               jvm_getter_method: "getName",
               jvm_return_type: "Ljava/lang/String;";

      fn raw_website<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getRawWebsite",
               jvm_return_type: "Ljava/lang/String;";

      fn scopes<'local>(..) -> Vec<String>
         where jvm_type: JvmList<'local, JvmString<'local>>,
               jvm_getter_method: "getScopes",
               jvm_return_type: "Ljava/util/List;";

      fn redirect_uris<'local>(..) -> Vec<String>
         where jvm_type: JvmList<'local, JvmString<'local>>,
               jvm_getter_method: "getRedirectUris",
               jvm_return_type: "Ljava/util/List;";

      fn client_id<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getClientId",
               jvm_return_type: "Ljava/lang/String;";

      fn client_secret<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getClientSecret",
               jvm_return_type: "Ljava/lang/String;";

      fn client_secret_expire_time_epoch_millis<'local>(..) -> Option<i64>
         where jvm_type: JvmNullable<'local, JvmLong<'local>>,
               jvm_getter_method: "getClientSecretExpireTimeEpochMillis",
               jvm_return_type: "Ljava/lang/Long;";
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmApplication<'local>> for Application {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmApplication<'local> {
      HELPER.clone_into_jvm(
         env,
         &self.instance,
         &self.name,
         &self.website.as_ref().map(Url::as_str),
         &self.scopes,
         &self.redirect_uris,
         &self.client_id,
         &self.client_secret,
         &self.client_secret_expire_time.map(|t| t.timestamp_millis()),
      )
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmApplication<'local>> for Application {
   fn clone_from_jvm(
      env: &mut JNIEnv<'local>,
      jvm_instance: &JvmApplication<'local>
   ) -> Application {
      let instance                               = HELPER.instance                              (env, jvm_instance);
      let name                                   = HELPER.name                                  (env, jvm_instance);
      let raw_website                            = HELPER.raw_website                           (env, jvm_instance);
      let scopes                                 = HELPER.scopes                                (env, jvm_instance);
      let redirect_uris                          = HELPER.redirect_uris                         (env, jvm_instance);
      let client_id                              = HELPER.client_id                             (env, jvm_instance);
      let client_secret                          = HELPER.client_secret                         (env, jvm_instance);
      let client_secret_expire_time_epoch_millis = HELPER.client_secret_expire_time_epoch_millis(env, jvm_instance);

      Application {
         instance,
         name,
         website: raw_website.map(|url| url.parse().unwrap()),
         scopes,
         redirect_uris,
         client_id,
         client_secret,
         client_secret_expire_time: client_secret_expire_time_epoch_millis.map(|time| DateTime::from_timestamp_millis(time).unwrap()),
      }
   }
}
