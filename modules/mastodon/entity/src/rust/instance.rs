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
   ext_panoptiqon::convert_jvm_helper::JvmInstantiationStrategy,
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
   static HELPER = impl struct ConvertJniHelper<3>
      where jvm_class: "com/wcaokaze/probosqis/mastodon/entity/Instance"
   {
      fn clone_into_jvm<'local>(..) -> JvmInstance<'local>
         where JvmInstantiationStrategy::ViaConstructor("(Ljava/lang/String;Ljava/lang/String;J)V");

      fn url<'local>(..) -> JvmString<'local>
         where jvm_getter_method: "getUrl",
               jvm_return_type: "Ljava/lang/String;";

      fn version<'local>(..) -> JvmString<'local>
         where jvm_getter_method: "getVersion",
               jvm_return_type: "Ljava/lang/String;";

      fn version_checked_time_epoch_millis<'local>(..) -> i64
         where jvm_getter_method: "getVersionCheckedTimeEpochMillis",
               jvm_return_type: "J";
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmInstance<'local>> for Instance {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmInstance<'local> {
      use jni::sys::jvalue;
      use panoptiqon::jvm_type::JvmType;

      let url                               = self.url.to_string().clone_into_jvm(env);
      let version                           = self.version        .clone_into_jvm(env);
      let version_checked_time_epoch_millis = self.version_checked_time.timestamp_millis();

      let j_object = HELPER.clone_into_jvm(env,
         jvalue { l: url    .j_object().as_raw() },
         jvalue { l: version.j_object().as_raw() },
         jvalue { j: version_checked_time_epoch_millis },
      );
      unsafe { JvmInstance::from_j_object(j_object) }
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmInstance<'local>> for Instance {
   fn clone_from_jvm(
      env: &mut JNIEnv,
      jvm_instance: &JvmInstance<'local>
   ) -> Instance {
      use panoptiqon::jvm_type::JvmType;
      use panoptiqon::jvm_types::JvmString;

      let url                               = HELPER.get(env, jvm_instance.j_object(), 0).l().unwrap();
      let version                           = HELPER.get(env, jvm_instance.j_object(), 1).l().unwrap();
      let version_checked_time_epoch_millis = HELPER.get(env, jvm_instance.j_object(), 2).j().unwrap();

      let url = unsafe { JvmString::from_j_object(url) };
      let version = unsafe { JvmString::from_j_object(version) };

      Instance {
         url:     String::clone_from_jvm(env, &url).parse().unwrap(),
         version: String::clone_from_jvm(env, &version),
         version_checked_time: DateTime::from_timestamp_millis(version_checked_time_epoch_millis).unwrap(),
      }
   }
}
