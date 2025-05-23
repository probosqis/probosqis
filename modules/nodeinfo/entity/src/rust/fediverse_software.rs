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

use mastodon_entity::instance::Instance;

#[cfg(feature = "jvm")]
use {
   ext_panoptiqon::convert_jvm_helper,
   jni::JNIEnv,
   mastodon_entity::jvm_types::JvmInstance,
   panoptiqon::convert_jvm::{CloneFromJvm, CloneIntoJvm},
   panoptiqon::jvm_type::JvmType,
   panoptiqon::jvm_types::JvmString,
   crate::jvm_types::JvmFediverseSoftware,
};

pub enum FediverseSoftware {
   Unsupported {
      name: String,
      version: String,
   },

   Mastodon {
      instance: Instance,
   },
}

#[cfg(feature = "jvm")]
convert_jvm_helper! {
   static HELPER_UNSUPPORTED = impl struct UnsupportedConvertHelper
      where jvm_class: "com/wcaokaze/probosqis/nodeinfo/entity/FediverseSoftware$Unsupported"
   {
      fn clone_into_jvm<'local>(..) -> JvmFediverseSoftware<'local>
         where jvm_constructor: "(Ljava/lang/String;Ljava/lang/String;)V";

      fn name<'local>(..) -> String
         where jvm_type: JvmString<'local>,
               jvm_getter_method: "getName",
               jvm_return_type: "Ljava/lang/String;";

      fn version<'local>(..) -> String
         where jvm_type: JvmString<'local>,
               jvm_getter_method: "getVersion",
               jvm_return_type: "Ljava/lang/String;";
   }

   static HELPER_MASTODON = impl struct MastodonConvertHelper
      where jvm_class: "com/wcaokaze/probosqis/nodeinfo/entity/FediverseSoftware$Mastodon"
   {
      fn clone_into_jvm<'local>(..) -> JvmFediverseSoftware<'local>
         where jvm_constructor: "(Lcom/wcaokaze/probosqis/mastodon/entity/Instance;)V";

      fn instance<'local>(..) -> Instance
         where jvm_type: JvmInstance<'local>,
               jvm_getter_method: "getInstance",
               jvm_return_type: "Lcom/wcaokaze/probosqis/mastodon/entity/Instance;";
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmFediverseSoftware<'local>> for FediverseSoftware {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmFediverseSoftware<'local> {
      match self {
         FediverseSoftware::Unsupported { name, version } => {
            HELPER_UNSUPPORTED.clone_into_jvm(
               env,
               name,
               version,
            )
         }

         FediverseSoftware::Mastodon { instance } => {
            HELPER_MASTODON.clone_into_jvm(
               env,
               instance,
            )
         }
      }
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmFediverseSoftware<'local>> for FediverseSoftware {
   fn clone_from_jvm(
      env: &mut JNIEnv<'local>,
      jvm_instance: &JvmFediverseSoftware<'local>
   ) -> FediverseSoftware {
      if env
         .is_instance_of(
            jvm_instance.j_object(),
            "com/wcaokaze/probosqis/nodeinfo/entity/FediverseSoftware$Mastodon"
         )
         .unwrap()
      {
         let instance = HELPER_MASTODON.instance(env, jvm_instance);

         FediverseSoftware::Mastodon { instance }
      } else {
         let name = HELPER_UNSUPPORTED.name(env, jvm_instance);
         let version = HELPER_UNSUPPORTED.version(env, jvm_instance);

         FediverseSoftware::Unsupported { name, version }
      }
   }
}
