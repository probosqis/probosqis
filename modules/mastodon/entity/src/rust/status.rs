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
use serde::Deserialize;

#[cfg(feature="jvm")]
use {
   jni::JNIEnv,
   jni::objects::JObject,
   ext_panoptiqon::convert_java_helper::{CloneIntoJava, ConvertJavaHelper},
   panoptiqon::convert_java::ConvertJava,
};

#[derive(Debug, Eq, PartialEq, Copy, Clone, Deserialize)]
pub enum StatusVisibility {
   Public   = 0,
   Unlisted = 1,
   Private  = 2,
   Direct   = 3,
}

#[cfg(feature="jvm")]
static STATUS_VISIBILITY_HELPER: ConvertJavaHelper<1> = ConvertJavaHelper::new(
   "com/wcaokaze/probosqis/mastodon/entity/Status$Visibility",
   CloneIntoJava::ViaStaticMethod(
      "fromInt", "(I)Lcom/wcaokaze/probosqis/mastodon/entity/Status$Visibility;"
   ),
   [
      ("getValue", "I"),
   ]
);

#[cfg(feature="jvm")]
impl ConvertJava for StatusVisibility {
   fn clone_into_java<'local>(&self, env: &mut JNIEnv<'local>) -> JObject<'local> {
      use jni::sys::jvalue;

      let visibility = *self as i32;
      let args = [
         jvalue { i: visibility },
      ];

      STATUS_VISIBILITY_HELPER.clone_into_java(env, &args)
   }

   fn clone_from_java(env: &mut JNIEnv, java_object: &JObject) -> StatusVisibility {
      let v = STATUS_VISIBILITY_HELPER.get(env, &java_object, 0).i().unwrap();
      match v {
         0 => StatusVisibility::Public,
         1 => StatusVisibility::Unlisted,
         2 => StatusVisibility::Private,
         3 => StatusVisibility::Direct,
         _ => panic!()
      }
   }
}
