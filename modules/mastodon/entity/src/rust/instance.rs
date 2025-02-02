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
use serde::Deserialize;
use url::Url;

#[cfg(feature = "jvm")]
use {
   ext_panoptiqon::convert_jvm_helper,
   jni::JNIEnv,
   panoptiqon::convert_jvm::{CloneFromJvm, CloneIntoJvm},
   panoptiqon::jvm_types::JvmString,
   crate::jvm_types::JvmInstance,
};

#[derive(Debug, Eq, PartialEq, Clone, Deserialize)]
pub struct Instance {
   pub url: Url,
   pub version: String,
   pub version_checked_time: DateTime<Utc>,
}

#[cfg(feature = "jvm")]
convert_jvm_helper! {
   static HELPER = impl struct ConvertJniHelper
      where jvm_class: "com/wcaokaze/probosqis/mastodon/entity/Instance"
   {
      fn clone_into_jvm<'local>(..) -> JvmInstance<'local>
         where jvm_constructor: "(Ljava/lang/String;Ljava/lang/String;J)V";

      fn url<'local>(..) -> String
         where jvm_type: JvmString<'local>,
               jvm_getter_method: "getUrl",
               jvm_return_type: "Ljava/lang/String;";

      fn version<'local>(..) -> String
         where jvm_type: JvmString<'local>,
               jvm_getter_method: "getVersion",
               jvm_return_type: "Ljava/lang/String;";

      fn version_checked_time_epoch_millis<'local>(..) -> i64
         where jvm_getter_method: "getVersionCheckedTimeEpochMillis",
               jvm_return_type: "J";
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmInstance<'local>> for Instance {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmInstance<'local> {
      HELPER.clone_into_jvm(
         env,
         self.url.as_str(),
         &self.version,
         self.version_checked_time.timestamp_millis(),
      )
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmInstance<'local>> for Instance {
   fn clone_from_jvm(
      env: &mut JNIEnv<'local>,
      jvm_instance: &JvmInstance<'local>
   ) -> Instance {
      let url                               = HELPER.url                              (env, jvm_instance);
      let version                           = HELPER.version                          (env, jvm_instance);
      let version_checked_time_epoch_millis = HELPER.version_checked_time_epoch_millis(env, jvm_instance);

      Instance {
         url: url.parse().unwrap(),
         version,
         version_checked_time:
            DateTime::from_timestamp_millis(version_checked_time_epoch_millis).unwrap(),
      }
   }
}
