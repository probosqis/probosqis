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

pub use paste::paste;

#[macro_export]
macro_rules! convert_jvm_helper {
   (
      $(
         $(#[$attr:meta])*
         static $var_name:ident = impl struct $type_name:ident < $arity:literal >
            where jvm_class: $class_fully_qualified_name:literal
         {
            fn clone_into_jvm<'local>(..) -> $jvm_type:ty
               where jvm_constructor: $constructor_signature:expr;

            $(
               fn $prop_name:ident <'local>(..) -> $prop_ret_type:ty
                  where jvm_getter_method: $getter_method_name:expr,
                        jvm_return_type: $getter_ret_type:expr
            );* $(;)?
         }
      )*
   ) => {
      $(
         $(#[$attr])*
         static $var_name: $type_name<$arity> = $type_name::new(
            [$(($getter_method_name, $getter_ret_type)),*]
         );

         $crate::convert_jvm_helper::paste! {
            $(#[$attr])*
            struct $type_name<'a, const ARITY: usize>(
               ::std::sync::RwLock<[<$type_name Inner>]<'a, ARITY>>
            );

            $(#[$attr])*
            enum [<$type_name Inner>]<'a, const ARITY: usize> {
               SignatureStrs {
                  getter_signatures: [(&'a str, &'a str); ARITY]
               },
               JvmIds {
                  class: ::jni::objects::GlobalRef,
                  constructor_id: ::jni::objects::JMethodID,
                  getter_ids: [(::jni::objects::JMethodID, ::jni::signature::ReturnType); ARITY]
               }
            }

            impl<'a, const ARITY: usize> $type_name<'a, ARITY> {
               pub const fn new(
                  getter_signatures: [(&'a str, &'a str); ARITY]
               ) -> Self {
                  let inner = [<$type_name Inner>]::SignatureStrs {
                     getter_signatures
                  };
                  $type_name(::std::sync::RwLock::new(inner))
               }

               fn prepare_ids(
                  env: &mut ::jni::JNIEnv,
                  getter_signatures: &[(&str, &str); ARITY]
               ) -> (::jni::objects::GlobalRef, ::jni::objects::JMethodID, [(::jni::objects::JMethodID, ::jni::signature::ReturnType); ARITY]) {
                  let class = env.find_class($class_fully_qualified_name).unwrap();
                  let class = env.new_global_ref(class).unwrap();

                  let constructor_id = env.get_method_id(&class, "<init>", $constructor_signature).unwrap();

                  let getter_ids = getter_signatures.map(|(name, ty)| {
                     let type_signature_str = format!("(){ty}");
                     let type_signature = ::jni::signature::TypeSignature::from_str(&type_signature_str).unwrap();
                     let getter_id = env.get_method_id(&class, name, &type_signature_str).unwrap();
                     (getter_id, type_signature.ret)
                  });

                  (class, constructor_id, getter_ids)
               }

               pub fn clone_into_jvm<'local>(
                  &self,
                  env: &mut ::jni::JNIEnv<'local>,
                  $($prop_name: $prop_ret_type),*
               ) -> ::jni::objects::JObject<'local> {
                  use std::ops::Deref;
                  use $crate::jvalue;

                  let lock = self.0.read().unwrap();
                  match lock.deref() {
                     [<$type_name Inner>]::JvmIds {
                        class,
                        constructor_id,
                        ..
                     } => {
                        let args = [
                           $(jvalue!($prop_ret_type, $prop_name)),*
                        ];

                        unsafe {
                           env.new_object_unchecked(class, *constructor_id, &args).unwrap()
                        }
                     }

                     [<$type_name Inner>]::SignatureStrs {
                        getter_signatures
                     } => {
                        let (class, constructor_id, getter_ids) = Self::prepare_ids(
                           env,
                           getter_signatures
                        );
                        drop(lock);
                        let mut lock = self.0.write().unwrap();
                        *lock = [<$type_name Inner>]::JvmIds {
                           class, constructor_id, getter_ids
                        };
                        drop(lock);
                        self.clone_into_jvm(env, $($prop_name),*)
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
                        getter_signatures
                     } => {
                        let (class, constructor_id, getter_ids) = Self::prepare_ids(
                           env,
                           getter_signatures
                        );
                        drop(lock);
                        let mut lock = self.0.write().unwrap();
                        *lock = [<$type_name Inner>]::JvmIds {
                           class, constructor_id, getter_ids
                        };
                        drop(lock);
                        self.get(env, instance, n)
                     }
                  }
               }

               $(
                  fn $prop_name<'local>(
                     &self,
                     env: &mut ::jni::JNIEnv<'local>,
                     instance: &$jvm_type
                  ) -> $prop_ret_type {
                     todo!();
                  }
               )*
            }
         }
      )*
   };

   (
      $(
         $(#[$attr:meta])*
         static $var_name:ident = impl struct $type_name:ident < $arity:literal >
            where jvm_class: $class_fully_qualified_name:literal
         {
            fn clone_into_jvm<'local>(..) -> $jvm_type:ty
               where jvm_static_method: $factory_method_name:expr,
                     jvm_signature: $factory_signature:expr;

            $(
               fn $prop_name:ident <'local>(..) -> $prop_ret_type:ty
                  where jvm_getter_method: $getter_method_name:expr,
                        jvm_return_type: $getter_ret_type:expr
            );* $(;)?
         }
      )*
   ) => {
      $(
         $(#[$attr])*
         static $var_name: $type_name<$arity> = $type_name::new(
            [$(($getter_method_name, $getter_ret_type)),*]
         );

         $crate::convert_jvm_helper::paste! {
            $(#[$attr])*
            struct $type_name<'a, const ARITY: usize>(
               ::std::sync::RwLock<[<$type_name Inner>]<'a, ARITY>>
            );

            $(#[$attr])*
            enum [<$type_name Inner>]<'a, const ARITY: usize> {
               SignatureStrs {
                  getter_signatures: [(&'a str, &'a str); ARITY]
               },
               JvmIds {
                  class: ::jni::objects::GlobalRef,
                  factory_method_id: ::jni::objects::JStaticMethodID,
                  factory_return_type: ::jni::signature::ReturnType,
                  getter_ids: [(::jni::objects::JMethodID, ::jni::signature::ReturnType); ARITY]
               }
            }

            impl<'a, const ARITY: usize> $type_name<'a, ARITY> {
               pub const fn new(
                  getter_signatures: [(&'a str, &'a str); ARITY]
               ) -> Self {
                  let inner = [<$type_name Inner>]::SignatureStrs {
                     getter_signatures
                  };
                  $type_name(::std::sync::RwLock::new(inner))
               }

               fn prepare_ids(
                  env: &mut ::jni::JNIEnv,
                  getter_signatures: &[(&str, &str); ARITY]
               ) -> (::jni::objects::GlobalRef, ::jni::objects::JStaticMethodID, ::jni::signature::ReturnType, [(::jni::objects::JMethodID, ::jni::signature::ReturnType); ARITY]) {
                  let class = env.find_class($class_fully_qualified_name).unwrap();
                  let class = env.new_global_ref(class).unwrap();

                  let method_id = env
                     .get_static_method_id(&class, $factory_method_name, $factory_signature).unwrap();

                  let type_signature = ::jni::signature::TypeSignature::from_str($factory_signature).unwrap();

                  let getter_ids = getter_signatures.map(|(name, ty)| {
                     let type_signature_str = format!("(){ty}");
                     let type_signature = ::jni::signature::TypeSignature::from_str(&type_signature_str).unwrap();
                     let getter_id = env.get_method_id(&class, name, &type_signature_str).unwrap();
                     (getter_id, type_signature.ret)
                  });

                  (class, method_id, type_signature.ret, getter_ids)
               }

               pub fn clone_into_jvm<'local>(
                  &self,
                  env: &mut ::jni::JNIEnv<'local>,
                  $($prop_name: $prop_ret_type),*
               ) -> ::jni::objects::JObject<'local> {
                  use std::ops::Deref;
                  use $crate::jvalue;

                  let lock = self.0.read().unwrap();
                  match lock.deref() {
                     [<$type_name Inner>]::JvmIds {
                        class,
                        factory_method_id,
                        factory_return_type,
                        ..
                     } => {
                        let args = [
                           $(jvalue!($prop_ret_type, $prop_name)),*
                        ];

                        unsafe {
                           env.call_static_method_unchecked(
                                 class, *factory_method_id, factory_return_type.clone(), &args
                              )
                              .unwrap().l().unwrap()
                        }
                     }

                     [<$type_name Inner>]::SignatureStrs {
                        getter_signatures
                     } => {
                        let (class, factory_method_id, factory_return_type, getter_ids) = Self::prepare_ids(
                           env,
                           getter_signatures
                        );
                        drop(lock);
                        let mut lock = self.0.write().unwrap();
                        *lock = [<$type_name Inner>]::JvmIds {
                           class, factory_method_id, factory_return_type, getter_ids
                        };
                        drop(lock);
                        self.clone_into_jvm(env, $($prop_name),*)
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
                        getter_signatures
                     } => {
                        let (class, factory_method_id, factory_return_type, getter_ids) = Self::prepare_ids(
                           env,
                           getter_signatures
                        );
                        drop(lock);
                        let mut lock = self.0.write().unwrap();
                        *lock = [<$type_name Inner>]::JvmIds {
                           class, factory_method_id, factory_return_type, getter_ids
                        };
                        drop(lock);
                        self.get(env, instance, n)
                     }
                  }
               }

               $(
                  fn $prop_name<'local>(
                     &self,
                     env: &mut ::jni::JNIEnv<'local>,
                     instance: &$jvm_type
                  ) -> $prop_ret_type {
                     todo!();
                  }
               )*
            }
         }
      )*
   };
}

#[macro_export]
macro_rules! jvalue {
    (   i8, $value:expr) => { ::jni::sys::jvalue { b: $value } };
    (  i16, $value:expr) => { ::jni::sys::jvalue { s: $value } };
    (  i32, $value:expr) => { ::jni::sys::jvalue { i: $value } };
    (  i64, $value:expr) => { ::jni::sys::jvalue { j: $value } };
    (  f32, $value:expr) => { ::jni::sys::jvalue { f: $value } };
    (  f64, $value:expr) => { ::jni::sys::jvalue { d: $value } };
    ( bool,        true) => { ::jni::sys::jvalue { z: ::jni::sys::JNI_TRUE  } };
    ( bool,       false) => { ::jni::sys::jvalue { z: ::jni::sys::JNI_FALSE } };
    (bool, $value:expr) => {
       ::jni::sys::jvalue {
          z: if $value { ::jni::sys::JNI_TRUE } else { ::jni::sys::JNI_FALSE }
       }
    };
    ($_:ty, $value:expr) => {
       ::jni::sys::jvalue {
          l: ::panoptiqon::jvm_type::JvmType::j_object(&$value).as_raw()
       }
    };
}

#[cfg(feature="jni-test")]
mod jni_tests {
   use std::ops::Deref;

   use jni::JNIEnv;
   use jni::objects::JObject;
   use panoptiqon::convert_jvm::CloneFromJvm;
   use paste::paste;
   use panoptiqon::jvm_type;
   use panoptiqon::jvm_types::JvmString;

   jvm_type! {
      JvmTestEntity,
   }

   macro_rules! helper {
      ($name:ident) => {
         paste! {
            convert_jvm_helper! {
               #[allow(non_upper_case_globals, non_camel_case_types)]
               static $name = impl struct [<$name Helper>]<8>
                  where jvm_class: "com/wcaokaze/probosqis/ext/panoptiqon/TestEntity"
               {
                  fn clone_into_jvm<'local>(..) -> JvmTestEntity<'local>
                     where jvm_constructor: "(ZBSIJFDLjava/lang/String;)V";

                  fn z  <'local>(..) -> bool              where jvm_getter_method: "getZ",   jvm_return_type: "Z";
                  fn b  <'local>(..) -> i8                where jvm_getter_method: "getB",   jvm_return_type: "B";
                  fn s  <'local>(..) -> i16               where jvm_getter_method: "getS",   jvm_return_type: "S";
                  fn i  <'local>(..) -> i32               where jvm_getter_method: "getI",   jvm_return_type: "I";
                  fn j  <'local>(..) -> i64               where jvm_getter_method: "getJ",   jvm_return_type: "J";
                  fn f  <'local>(..) -> f32               where jvm_getter_method: "getF",   jvm_return_type: "F";
                  fn d  <'local>(..) -> f64               where jvm_getter_method: "getD",   jvm_return_type: "D";
                  fn str<'local>(..) -> JvmString<'local> where jvm_getter_method: "getStr", jvm_return_type: "Ljava/lang/String;";
               }
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
      let l = JvmString::from_j_string(l);

      variantJvmIdsAfterCloneIntoJvm_helper.clone_into_jvm(
         &mut env,
         false,
         0i8,
         0i16,
         0i32,
         0i64,
         0.0f32,
         0.0f64,
         l,
      );

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
      let l = JvmString::from_j_string(l);

      cloneIntoJvm_helper.clone_into_jvm(
         &mut env,
         false,
         0i8,
         1i16,
         2i32,
         3i64,
         4.5f32,
         6.75f64,
         l,
      )
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

      let str = cloneFromJvm_helper.get(&mut env, &jvm_entity, 7).l().unwrap();
      let str = unsafe { String::clone_from_j_object(&mut env, &str) };

      assert_eq!(false, z);
      assert_eq!(0, b);
      assert_eq!(1, s);
      assert_eq!(2, i);
      assert_eq!(3, j);
      assert_eq!(4.5, f);
      assert_eq!(6.75, d);
      assert_eq!("9012345".to_string(), str);
   }
}
