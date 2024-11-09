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
#[cfg(feature="jvm")]
use {
   ext_panoptiqon::convert_java_helper::ConvertJavaHelper,
   jni::JNIEnv,
   jni::objects::JObject,
   jni::sys::jvalue,
};

#[cfg(feature="jvm")]
const HELPER_UNSUPPORTED: ConvertJavaHelper<0> = ConvertJavaHelper::new(
   "com/wcaokaze/probosqis/nodeinfo/entity/FediverseSoftware$Unsupported",
   "(Ljava/lang/String;Ljava/lang/String;)V",
   []
);

#[cfg(feature="jvm")]
const HELPER_MASTODON: ConvertJavaHelper<0> = ConvertJavaHelper::new(
   "com/wcaokaze/probosqis/nodeinfo/entity/FediverseSoftware$Mastodon",
   "(Ljava/lang/String;Ljava/lang/String;)V",
   []
);

#[cfg(feature="jvm")]
pub fn instantiate_unsupported<'local>(
   env: &mut JNIEnv<'local>,
   name: &str,
   version: &str
) -> JObject<'local> {
   let name    = env.new_string(name)   .unwrap();
   let version = env.new_string(version).unwrap();

   let args = [
      jvalue { l: name   .into_raw() },
      jvalue { l: version.into_raw() },
   ];

   HELPER_UNSUPPORTED.clone_into_java(env, &args)
}

#[cfg(feature="jvm")]
pub fn instantiate_mastodon<'local>(
   env: &mut JNIEnv<'local>,
   instance_base_url: &str,
   version: &str
) -> JObject<'local> {
   let instance_base_url = env.new_string(instance_base_url).unwrap();
   let version           = env.new_string(version)          .unwrap();

   let args = [
      jvalue { l: instance_base_url.into_raw() },
      jvalue { l: version          .into_raw() },
   ];

   HELPER_MASTODON.clone_into_java(env, &args)
}
