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
   ext_panoptiqon::convert_jvm_helper::JvmInstantiationStrategy,
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
   static HELPER_UNSUPPORTED = impl struct UnsupportedConvertHelper<2>
      where jvm_class: "com/wcaokaze/probosqis/nodeinfo/entity/FediverseSoftware$Unsupported"
   {
      fn clone_into_jvm<'local>(..) -> JvmFediverseSoftware<'local>
         where JvmInstantiationStrategy::ViaConstructor(
            "(Ljava/lang/String;Ljava/lang/String;)V"
         );

      fn name<'local>(..) -> JvmString<'local>
         where jvm_getter_method: "getName",
               jvm_return_type: "Ljava/lang/String;";

      fn version<'local>(..) -> JvmString<'local>
         where jvm_getter_method: "getVersion",
               jvm_return_type: "Ljava/lang/String;";
   }

   static HELPER_MASTODON = impl struct MastodonConvertHelper<1>
      where jvm_class: "com/wcaokaze/probosqis/nodeinfo/entity/FediverseSoftware$Mastodon"
   {
      fn clone_into_jvm<'local>(..) -> JvmFediverseSoftware<'local>
         where JvmInstantiationStrategy::ViaConstructor(
            "(Lcom/wcaokaze/probosqis/mastodon/entity/Instance;)V"
         );

      fn instance<'local>(..) -> JvmInstance<'local>
         where jvm_getter_method: "getInstance",
               jvm_return_type: "Lcom/wcaokaze/probosqis/mastodon/entity/Instance;";
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmFediverseSoftware<'local>> for FediverseSoftware {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmFediverseSoftware<'local> {
      use panoptiqon::convert_jvm::CloneIntoJvm;
      use panoptiqon::jvm_type::JvmType;

      match self {
         FediverseSoftware::Unsupported { name, version } => {
            let name = name.clone_into_jvm(env);
            let version = version.clone_into_jvm(env);

            let j_object = HELPER_UNSUPPORTED.clone_into_jvm(
               env,
               name,
               version,
            );
            unsafe { JvmFediverseSoftware::from_j_object(j_object) }
         }

         FediverseSoftware::Mastodon { instance } => {
            let jvm_instance = instance.clone_into_jvm(env);

            let j_object = HELPER_MASTODON.clone_into_jvm(
               env,
               jvm_instance,
            );
            unsafe { JvmFediverseSoftware::from_j_object(j_object) }
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
         let instance = HELPER_UNSUPPORTED.get(env, jvm_instance.j_object(), 0).l().unwrap();
         let jvm_instance = unsafe { JvmInstance::from_j_object(instance) };
         let instance = Instance::clone_from_jvm(env, &jvm_instance);

         FediverseSoftware::Mastodon { instance }
      } else {
         let name = HELPER_UNSUPPORTED.get(env, jvm_instance.j_object(), 0).l().unwrap();
         let version = HELPER_UNSUPPORTED.get(env, jvm_instance.j_object(), 1).l().unwrap();

         let name = unsafe { JvmString::from_j_object(name) };
         let version = unsafe { JvmString::from_j_object(version) };

         let name = String::clone_from_jvm(env, &name);
         let version = String::clone_from_jvm(env, &version);

         FediverseSoftware::Unsupported { name, version }
      }
   }
}
