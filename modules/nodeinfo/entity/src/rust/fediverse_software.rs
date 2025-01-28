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

#[cfg(feature = "jvm")]
use {
   ext_panoptiqon::convert_jvm_helper,
   ext_panoptiqon::convert_jvm_helper::JvmInstantiationStrategy,
   jni::JNIEnv,
   crate::jvm_types::JvmFediverseSoftware,
};

#[cfg(feature = "jvm")]
convert_jvm_helper! {
   static HELPER_UNSUPPORTED: ConvertJniHelper<0> = convert_jvm_helper!(
      "com/wcaokaze/probosqis/nodeinfo/entity/FediverseSoftware$Unsupported",
      JvmInstantiationStrategy::ViaConstructor(
         "(Ljava/lang/String;Ljava/lang/String;)V"
      ),
      []
   );

   static HELPER_MASTODON: ConvertJniHelper<0> = convert_jvm_helper!(
      "com/wcaokaze/probosqis/nodeinfo/entity/FediverseSoftware$Mastodon",
      JvmInstantiationStrategy::ViaConstructor(
         "(Ljava/lang/String;Ljava/lang/String;)V"
      ),
      []
   );
}

#[cfg(feature = "jvm")]
pub fn instantiate_unsupported<'local>(
   env: &mut JNIEnv<'local>,
   name: &str,
   version: &str
) -> JvmFediverseSoftware<'local> {
   use jni::sys::jvalue;
   use panoptiqon::convert_jvm::CloneIntoJvm;
   use panoptiqon::jvm_type::JvmType;

   let name = name.clone_into_jvm(env);
   let version = version.clone_into_jvm(env);

   let args = [
      jvalue { l: name.j_string().as_raw() },
      jvalue { l: version.j_string().as_raw() },
   ];

   let j_object = HELPER_UNSUPPORTED.clone_into_jvm(env, &args);
   unsafe { JvmFediverseSoftware::from_j_object(j_object) }
}

#[cfg(feature = "jvm")]
pub fn instantiate_mastodon<'local>(
   env: &mut JNIEnv<'local>,
   instance_base_url: &str,
   version: &str
) -> JvmFediverseSoftware<'local> {
   use jni::sys::jvalue;
   use panoptiqon::convert_jvm::CloneIntoJvm;
   use panoptiqon::jvm_type::JvmType;

   let instance_base_url = instance_base_url.clone_into_jvm(env);
   let version = version.clone_into_jvm(env);

   let args = [
      jvalue { l: instance_base_url.j_string().as_raw() },
      jvalue { l: version.j_string().as_raw() },
   ];

   let j_object = HELPER_MASTODON.clone_into_jvm(env, &args);
   unsafe { JvmFediverseSoftware::from_j_object(j_object) }
}
