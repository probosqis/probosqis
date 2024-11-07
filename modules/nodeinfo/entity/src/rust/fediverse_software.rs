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
use serde::Deserialize;

#[cfg(feature="jvm")]
use {
   ext_panoptiqon::convert_java_helper::ConvertJavaHelper,
   jni::JNIEnv,
   jni::objects::JObject,
   jni::sys::jvalue,
   panoptiqon::convert_java::ConvertJava,
};

#[derive(Deserialize)]
pub struct FediverseSoftware {
   pub name: String,
   pub version: String,
}

#[cfg(feature="jvm")]
const HELPER_UNSUPPORTED: ConvertJavaHelper<2> = ConvertJavaHelper::new(
   "com/wcaokaze/probosqis/nodeinfo/entity/FediverseSoftware$Unsupported",
   "(Ljava/lang/String;Ljava/lang/String;)V",
   [
      ("getName",    "Ljava/lang/String;"),
      ("getVersion", "Ljava/lang/String;"),
   ]
);

#[cfg(feature="jvm")]
impl ConvertJava for FediverseSoftware {
   fn clone_into_java<'local>(&self, env: &mut JNIEnv<'local>) -> JObject<'local> {
      let name    = self.name.clone_into_java(env);
      let version = self.version.clone_into_java(env);

      let args = [
         jvalue { l: name   .into_raw() },
         jvalue { l: version.into_raw() },
      ];

      HELPER_UNSUPPORTED.clone_into_java(env, &args)
   }

   fn clone_from_java(env: &mut JNIEnv, java_object: &JObject) -> Self {
      let name    = HELPER_UNSUPPORTED.get(env, &java_object, 0).l().unwrap();
      let version = HELPER_UNSUPPORTED.get(env, &java_object, 1).l().unwrap();

      FediverseSoftware {
         name:    String::clone_from_java(env, &name),
         version: String::clone_from_java(env, &version),
      }
   }
}
