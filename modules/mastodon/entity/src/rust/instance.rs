/*
 * Copyright 2024 wcaokaze
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

#[cfg(feature="jvm")]
use {
   ext_panoptiqon::convert_java_helper::CloneIntoJava,
   ext_panoptiqon::convert_java_helper::ConvertJavaHelper,
   jni::JNIEnv,
   jni::objects::JObject,
   panoptiqon::convert_java::ConvertJava,
};

#[derive(Debug, Eq, PartialEq, Clone, Deserialize)]
pub struct Instance {
   pub url: Url,
   pub version: String,
   pub version_checked_time: DateTime<Utc>,
}

#[cfg(feature="jvm")]
static HELPER: ConvertJavaHelper<3> = ConvertJavaHelper::new(
   "com/wcaokaze/probosqis/mastodon/entity/Instance",
   CloneIntoJava::ViaConstructor("(Ljava/lang/String;Ljava/lang/String;J)V"),
   [
      ("getUrl",                           "Ljava/lang/String;"),
      ("getVersion",                       "Ljava/lang/String;"),
      ("getVersionCheckedTimeEpochMillis", "J"),
   ]
);

#[cfg(feature="jvm")]
impl ConvertJava for Instance {
   fn clone_into_java<'local>(&self, env: &mut JNIEnv<'local>) -> JObject<'local> {
      use jni::sys::jvalue;

      let url                               = self.url.to_string().clone_into_java(env);
      let version                           = self.version        .clone_into_java(env);
      let version_checked_time_epoch_millis = self.version_checked_time.timestamp_millis();

      let args = [
         jvalue { l: url    .into_raw() },
         jvalue { l: version.into_raw() },
         jvalue { j: version_checked_time_epoch_millis },
      ];

      HELPER.clone_into_java(env, &args)
   }

   fn clone_from_java(env: &mut JNIEnv, java_object: &JObject) -> Self {
      let url                               = HELPER.get(env, &java_object, 0).l().unwrap();
      let version                           = HELPER.get(env, &java_object, 1).l().unwrap();
      let version_checked_time_epoch_millis = HELPER.get(env, &java_object, 2).j().unwrap();

      Instance {
         url:     String::clone_from_java(env, &url).parse().unwrap(),
         version: String::clone_from_java(env, &version),
         version_checked_time: DateTime::from_timestamp_millis(version_checked_time_epoch_millis).unwrap(),
      }
   }
}
