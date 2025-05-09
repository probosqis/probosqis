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

#[cfg(feature = "jvm")]
use {
   ext_panoptiqon::convert_jvm_helper,
   jni::JNIEnv,
   panoptiqon::convert_jvm::{CloneFromJvm, CloneIntoJvm},
   panoptiqon::jvm_types::JvmString,
   url::Url,
   crate::jvm_types::JvmUrl,
};

#[cfg(feature = "jvm")]
convert_jvm_helper! {
   static HELPER = impl struct ConvertHelper
      where jvm_class: "com/wcaokaze/probosqis/entity/Url"
   {
      fn clone_into_jvm<'local>(..) -> JvmUrl<'local>
         where jvm_constructor: "(Ljava/lang/String;)V";

      fn raw<'local>(..) -> String
         where jvm_type: JvmString<'local>,
               jvm_getter_method: "getRaw",
               jvm_return_type: "Ljava/lang/String;";
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmUrl<'local>> for Url {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmUrl<'local> {
      HELPER.clone_into_jvm(
         env,
         self.as_str(),
      )
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmUrl<'local>> for Url {
   fn clone_from_jvm(
      env: &mut JNIEnv<'local>,
      jvm_instance: &JvmUrl<'local>
   ) -> Url {
      let url = HELPER.raw(env, jvm_instance);
      url.parse().unwrap()
   }
}
