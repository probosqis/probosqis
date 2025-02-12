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

#[cfg(feature = "jvm")]
use {
   jni::JNIEnv,
   ext_panoptiqon::convert_jvm_helper,
   panoptiqon::convert_jvm::{CloneFromJvm, CloneIntoJvm},
   crate::jvm_types::JvmStatusVisibility,
};

#[derive(Debug, Eq, PartialEq, Copy, Clone, Deserialize)]
pub enum StatusVisibility {
   Public   = 0,
   Unlisted = 1,
   Private  = 2,
   Direct   = 3,
}

#[cfg(feature = "jvm")]
convert_jvm_helper! {
   static STATUS_VISIBILITY_HELPER = impl struct StatusVisibilityConvertHelper
      where jvm_class: "com/wcaokaze/probosqis/mastodon/entity/Status$Visibility"
   {
      fn clone_into_jvm<'local>(..) -> JvmStatusVisibility<'local>
         where jvm_static_method: "fromInt",
               jvm_signature: "(I)Lcom/wcaokaze/probosqis/mastodon/entity/Status$Visibility;";

      fn value<'local>(..) -> i32
         where jvm_getter_method: "getValue",
               jvm_return_type: "I";
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmStatusVisibility<'local>> for StatusVisibility {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmStatusVisibility<'local> {
      let visibility = *self as i32;

      STATUS_VISIBILITY_HELPER.clone_into_jvm(
         env,
         visibility,
      )
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmStatusVisibility<'local>> for StatusVisibility {
   fn clone_from_jvm(
      env: &mut JNIEnv<'local>,
      jvm_instance: &JvmStatusVisibility<'local>
   ) -> StatusVisibility {
      let v = STATUS_VISIBILITY_HELPER.value(env, jvm_instance);
      match v {
         0 => StatusVisibility::Public,
         1 => StatusVisibility::Unlisted,
         2 => StatusVisibility::Private,
         3 => StatusVisibility::Direct,
         _ => panic!()
      }
   }
}
