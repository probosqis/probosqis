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

#![cfg(feature="jvm")]

use jni::objects::{JMethodID, JStaticMethodID};
use jni::signature::ReturnType;

pub use paste::paste;

#[macro_export]
macro_rules! convert_jvm_helper {
   (
      $(
         $(#[$attr:meta])*
         static $var_name:ident : $type_name:ident < $arity:literal > = convert_jvm_helper!(
            $class_fully_qualified_name:literal,
            $instantiation_strategy:expr,
            [$(($getter_method_name:expr, $getter_signatures:expr)),* $(,)?]
         );
      )*
   ) => {
      $(
         $(#[$attr])*
         static $var_name: $type_name<$arity> = $type_name::new(
            $class_fully_qualified_name,
            $instantiation_strategy,
            [$(($getter_method_name, $getter_signatures)),*]
         );

         $crate::convert_jvm_helper::paste! {
            $(#[$attr])*
            struct $type_name<'a, const ARITY: usize>(
               ::std::sync::RwLock<[<$type_name Inner>]<'a, ARITY>>
            );

            $(#[$attr])*
            enum [<$type_name Inner>]<'a, const ARITY: usize> {
               SignatureStrs {
                  class_fully_qualified_name: &'a str,
                  clone_into_jvm: $crate::convert_jvm_helper::JvmInstantiationStrategy<'a>,
                  getter_signatures: [(&'a str, &'a str); ARITY]
               },
               JvmIds {
                  class: ::jni::objects::GlobalRef,
                  clone_into_jvm: $crate::convert_jvm_helper::JvmInstantiationMethodId,
                  getter_ids: [(::jni::objects::JMethodID, ::jni::signature::ReturnType); ARITY]
               }
            }

            impl<'a, const ARITY: usize> $type_name<'a, ARITY> {
               pub const fn new(
                  class_fully_qualified_name: &'a str,
                  clone_into_jvm: $crate::convert_jvm_helper::JvmInstantiationStrategy<'a>,
                  getter_signatures: [(&'a str, &'a str); ARITY]
               ) -> Self {
                  let inner = [<$type_name Inner>]::SignatureStrs {
                     class_fully_qualified_name,
                     clone_into_jvm,
                     getter_signatures
                  };
                  $type_name(::std::sync::RwLock::new(inner))
               }

               fn prepare_ids(
                  &self,
                  env: &mut ::jni::JNIEnv,
                  class_fully_qualified_name: &str,
                  clone_into_jvm: &$crate::convert_jvm_helper::JvmInstantiationStrategy,
                  getter_signatures: &[(&str, &str); ARITY]
               ) -> (::jni::objects::GlobalRef, $crate::convert_jvm_helper::JvmInstantiationMethodId, [(::jni::objects::JMethodID, ::jni::signature::ReturnType); ARITY]) {
                  let class = env.find_class(class_fully_qualified_name).unwrap();
                  let class = env.new_global_ref(class).unwrap();

                  let clone_into_jvm_id = match clone_into_jvm {
                     $crate::convert_jvm_helper::JvmInstantiationStrategy::ViaConstructor(constructor_signature) => {
                        let constructor_id = env
                           .get_method_id(&class, "<init>", constructor_signature).unwrap();

                        $crate::convert_jvm_helper::JvmInstantiationMethodId::ViaConstructor { constructor_id }
                     },
                     $crate::convert_jvm_helper::JvmInstantiationStrategy::ViaStaticMethod(method_name, signature) => {
                        let method_id = env
                           .get_static_method_id(&class, method_name, signature).unwrap();

                        let type_signature = ::jni::signature::TypeSignature::from_str(signature).unwrap();

                        $crate::convert_jvm_helper::JvmInstantiationMethodId::ViaStaticMethod {
                           method_id,
                           return_type: type_signature.ret
                        }
                     }
                  };

                  let getter_ids = getter_signatures.map(|(name, ty)| {
                     let type_signature_str = format!("(){ty}");
                     let type_signature = ::jni::signature::TypeSignature::from_str(&type_signature_str).unwrap();
                     let getter_id = env.get_method_id(&class, name, &type_signature_str).unwrap();
                     (getter_id, type_signature.ret)
                  });

                  (class, clone_into_jvm_id, getter_ids)
               }

               pub fn clone_into_jvm<'local>(
                  &self,
                  env: &mut ::jni::JNIEnv<'local>,
                  args: &[::jni::sys::jvalue],
               ) -> ::jni::objects::JObject<'local> {
                  use std::ops::Deref;

                  let lock = self.0.read().unwrap();
                  match lock.deref() {
                     [<$type_name Inner>]::JvmIds {
                        class,
                        clone_into_jvm,
                        ..
                     } => {
                        match clone_into_jvm {
                           $crate::convert_jvm_helper::JvmInstantiationMethodId::ViaConstructor { constructor_id } => unsafe {
                              env.new_object_unchecked(class, *constructor_id, args).unwrap()
                           },
                           $crate::convert_jvm_helper::JvmInstantiationMethodId::ViaStaticMethod { method_id, return_type } => unsafe {
                              env.call_static_method_unchecked(
                                    class, *method_id, return_type.clone(), args
                                 )
                                 .unwrap().l().unwrap()
                           }
                        }
                     }

                     [<$type_name Inner>]::SignatureStrs {
                        class_fully_qualified_name,
                        clone_into_jvm,
                        getter_signatures
                     } => {
                        let (class, clone_into_jvm_id, getter_ids) = self.prepare_ids(
                           env, class_fully_qualified_name, clone_into_jvm,
                           getter_signatures
                        );
                        drop(lock);
                        let mut lock = self.0.write().unwrap();
                        *lock = [<$type_name Inner>]::JvmIds {
                           class, clone_into_jvm: clone_into_jvm_id, getter_ids
                        };
                        drop(lock);
                        self.clone_into_jvm(env, args)
                     }
                  }
               }

               pub fn get<'local>(
                  &self,
                  env: &mut ::jni::JNIEnv<'local>,
                  instance: &::jni::objects::JObject,
                  n: usize
               ) -> ::jni::objects::JValueOwned<'local> {
                  use std::ops::Deref;

                  let lock = self.0.read().unwrap();
                  match lock.deref() {
                     [<$type_name Inner>]::JvmIds {
                        getter_ids,
                        ..
                     } => {
                        let (getter_id, return_type) = &getter_ids[n];
                        unsafe {
                           env.call_method_unchecked(instance, getter_id, return_type.clone(), &[])
                              .unwrap()
                        }
                     }

                     [<$type_name Inner>]::SignatureStrs {
                        class_fully_qualified_name,
                        clone_into_jvm,
                        getter_signatures
                     } => {
                        let (class, clone_into_jvm_id, getter_ids) = self.prepare_ids(
                           env, class_fully_qualified_name, clone_into_jvm,
                           getter_signatures
                        );
                        drop(lock);
                        let mut lock = self.0.write().unwrap();
                        *lock = [<$type_name Inner>]::JvmIds {
                           class, clone_into_jvm: clone_into_jvm_id, getter_ids
                        };
                        drop(lock);
                        self.get(env, instance, n)
                     }
                  }
               }
            }
         }
      )*
   };
}

pub enum JvmInstantiationStrategy<'a> {
   ViaConstructor(&'a str),
   ViaStaticMethod(&'a str, &'a str)
}

pub enum JvmInstantiationMethodId {
   ViaConstructor {
      constructor_id: JMethodID
   },
   ViaStaticMethod {
      method_id: JStaticMethodID,
      return_type: ReturnType
   }
}

#[cfg(feature="jni-test")]
mod jni_tests {
   use std::ops::Deref;

   use jni::JNIEnv;
   use jni::objects::{JIntArray, JObject};
   use jni::sys::{JNI_FALSE, jvalue};
   use panoptiqon::convert_jvm::CloneFromJvm;
   use paste::paste;
   use super::JvmInstantiationStrategy;

   macro_rules! helper {
      ($name:ident) => {
         paste! {
            convert_jvm_helper! {
               #[allow(non_upper_case_globals, non_camel_case_types)]
               static $name: [<$name Helper>]<10> = convert_jvm_helper!(
                  "com/wcaokaze/probosqis/ext/panoptiqon/TestEntity",
                  JvmInstantiationStrategy::ViaConstructor("(ZBSIJFDCLjava/lang/String;[I)V"),
                  [
                     ("getZ", "Z"),
                     ("getB", "B"),
                     ("getS", "S"),
                     ("getI", "I"),
                     ("getJ", "J"),
                     ("getF", "F"),
                     ("getD", "D"),
                     ("getC", "C"),
                     ("getStr", "Ljava/lang/String;"),
                     ("getArr", "[I"),
                  ]
               );
            }
         }
      };
   }

   helper!(variantStrsOnInit_helper);

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_ext_panoptiqon_ConvertJniHelperTest_variantStrsOnInit(
      _env: JNIEnv,
      _obj: JObject
   ) {
      let helper_inner= variantStrsOnInit_helper.0.read().unwrap();
      assert!(matches!(helper_inner.deref(), &variantStrsOnInit_helperHelperInner::SignatureStrs { .. }));
   }

   helper!(variantJvmIdsAfterCloneIntoJvm_helper);

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_ext_panoptiqon_ConvertJniHelperTest_variantJvmIdsAfterCloneIntoJvm(
      mut env: JNIEnv,
      _obj: JObject
   ) {
      let l = env.new_string("").unwrap();
      let a = env.new_int_array(0).unwrap();

      variantJvmIdsAfterCloneIntoJvm_helper.clone_into_jvm(&mut env, &[
         jvalue { z: JNI_FALSE },
         jvalue { b: 0 },
         jvalue { s: 0 },
         jvalue { i: 0 },
         jvalue { j: 0 },
         jvalue { f: 0.0 },
         jvalue { d: 0.0 },
         jvalue { c: 'a' as u16  },
         jvalue { l: l.into_raw() },
         jvalue { l: a.into_raw() },
      ]);

      let helper_inner= variantJvmIdsAfterCloneIntoJvm_helper.0.read().unwrap();
      assert!(matches!(helper_inner.deref(), &variantJvmIdsAfterCloneIntoJvm_helperHelperInner::JvmIds { .. }));
   }

   helper!(variantJvmIdsAfterGet_helper);

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_ext_panoptiqon_ConvertJniHelperTest_variantJvmIdsAfterGet_00024assert(
      mut env: JNIEnv,
      _obj: JObject,
      jvm_entity: JObject
   ) {
      variantJvmIdsAfterGet_helper.get(&mut env, &jvm_entity, 0);

      let helper_inner = variantJvmIdsAfterGet_helper.0.read().unwrap();
      assert!(matches!(helper_inner.deref(), &variantJvmIdsAfterGet_helperHelperInner::JvmIds { .. }));
   }

   helper!(cloneIntoJvm_helper);

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_ext_panoptiqon_ConvertJniHelperTest_cloneIntoJvm_00024createEntity<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JObject<'local> {
      let l = env.new_string("9012345").unwrap();
      let a = env.new_int_array(5).unwrap();
      env.set_int_array_region(&a, 0, &[6, 7, 8, 9, 0]).unwrap();

      cloneIntoJvm_helper.clone_into_jvm(&mut env, &[
         jvalue { z: JNI_FALSE },
         jvalue { b: 0 },
         jvalue { s: 1 },
         jvalue { i: 2 },
         jvalue { j: 3 },
         jvalue { f: 4.5 },
         jvalue { d: 6.75 },
         jvalue { c: '8' as u16 },
         jvalue { l: l.into_raw() },
         jvalue { l: a.into_raw() },
      ])
   }

   helper!(cloneFromJvm_helper);

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_ext_panoptiqon_ConvertJniHelperTest_cloneFromJvm_00024assert(
      mut env: JNIEnv,
      _obj: JObject,
      jvm_entity: JObject
   ) {
      let z = cloneFromJvm_helper.get(&mut env, &jvm_entity, 0).z().unwrap();
      let b = cloneFromJvm_helper.get(&mut env, &jvm_entity, 1).b().unwrap();
      let s = cloneFromJvm_helper.get(&mut env, &jvm_entity, 2).s().unwrap();
      let i = cloneFromJvm_helper.get(&mut env, &jvm_entity, 3).i().unwrap();
      let j = cloneFromJvm_helper.get(&mut env, &jvm_entity, 4).j().unwrap();
      let f = cloneFromJvm_helper.get(&mut env, &jvm_entity, 5).f().unwrap();
      let d = cloneFromJvm_helper.get(&mut env, &jvm_entity, 6).d().unwrap();
      let c = cloneFromJvm_helper.get(&mut env, &jvm_entity, 7).c().unwrap();

      let str = cloneFromJvm_helper.get(&mut env, &jvm_entity, 8).l().unwrap();
      let str = unsafe { String::clone_from_j_object(&mut env, &str) };

      let arr = cloneFromJvm_helper.get(&mut env, &jvm_entity, 9).l().unwrap();
      let mut buf = [0; 5];
      env.get_int_array_region(JIntArray::from(arr), 0, &mut buf).unwrap();

      assert_eq!(false, z);
      assert_eq!(0, b);
      assert_eq!(1, s);
      assert_eq!(2, i);
      assert_eq!(3, j);
      assert_eq!(4.5, f);
      assert_eq!(6.75, d);
      assert_eq!('8' as u16, c);
      assert_eq!("9012345".to_string(), str);
      assert_eq!([6, 7, 8, 9, 0], buf);
   }
}
