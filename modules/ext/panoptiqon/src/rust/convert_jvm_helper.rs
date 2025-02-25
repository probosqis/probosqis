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
         static $var_name:ident = impl struct $type_name:ident
            where jvm_class: $class_fully_qualified_name:literal
         {
            fn clone_into_jvm<'local>(..) -> $jvm_type:ty
               where jvm_constructor: $constructor_signature:expr;

            $(
               fn $prop_name:ident <'local>(..) -> $prop_ret_type:ty
                  where $(jvm_type: $prop_jvm_type:ty,)?
                        jvm_getter_method: $getter_method_name:expr,
                        jvm_return_type: $getter_ret_type:expr
            );* $(;)?
         }
      )*
   ) => {
      $(
         $(#[$attr])*
         static $var_name: $type_name = $type_name::new();

         $crate::convert_jvm_helper::paste! {
            $(#[$attr])*
            struct $type_name(
               ::std::sync::RwLock<[<$type_name Inner>]>
            );

            $(#[$attr])*
            enum [<$type_name Inner>] {
               Uninit,
               JvmIds {
                  class: ::jni::objects::GlobalRef,
                  constructor_id: ::jni::objects::JMethodID,
                  $(
                     [<$prop_name _id>]: (::jni::objects::JMethodID, ::jni::signature::ReturnType)
                  ),*
               }
            }

            $(#[$attr])*
            impl $type_name {
               pub const fn new() -> Self {
                  let inner = [<$type_name Inner>]::Uninit;
                  $type_name(::std::sync::RwLock::new(inner))
               }

               fn get_method_id(
                  env: &mut ::jni::JNIEnv,
                  class: &::jni::objects::GlobalRef,
                  name: &str,
                  ret_type: &str
               ) -> (::jni::objects::JMethodID, ::jni::signature::ReturnType) {
                  let type_signature_str = format!("(){ret_type}");
                  let type_signature = ::jni::signature
                     ::TypeSignature::from_str(&type_signature_str).unwrap();
                  let method_id = env
                     .get_method_id(class, name, &type_signature_str).unwrap();
                  (method_id, type_signature.ret)
               }

               fn prepare_ids(env: &mut ::jni::JNIEnv) -> [<$type_name Inner>] {
                  let class = env.find_class($class_fully_qualified_name).unwrap();
                  let class = env.new_global_ref(class).unwrap();

                  let constructor_id = env
                     .get_method_id(&class, "<init>", $constructor_signature).unwrap();

                  $(
                     let [<$prop_name _id>] = Self::get_method_id(
                           env, &class, $getter_method_name, $getter_ret_type
                     );
                  )*

                  [<$type_name Inner>]::JvmIds {
                     class, constructor_id,
                     $([<$prop_name _id>]),*
                  }
               }

               pub fn clone_into_jvm<'local>(
                  &self,
                  env: &mut ::jni::JNIEnv<'local>,
                  $(
                     $prop_name: $crate::convert_jvm_helper_clone_param_type!(
                        $prop_ret_type$(, $prop_jvm_type)?
                     )
                  ),*
               ) -> $jvm_type {
                  $(
                     let $prop_name = $crate::convert_jvm_helper_clone_arg_conversion!(
                        env, $prop_name, $prop_ret_type$(, $prop_jvm_type)?
                     );
                  )*

                  self.clone_into_jvm_from_jvm_type(
                     env,
                     $($prop_name),*
                  )
               }

               pub fn clone_into_jvm_from_jvm_type<'local>(
                  &self,
                  env: &mut ::jni::JNIEnv<'local>,
                  $(
                     $prop_name: $crate::convert_jvm_helper_clone_jvm_param_type!(
                        $prop_ret_type$(, $prop_jvm_type)?
                     )
                  ),*
               ) -> $jvm_type {
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
                           let j_object = env
                              .new_object_unchecked(class, *constructor_id, &args)
                              .unwrap();

                           ::panoptiqon::jvm_type::JvmType::from_j_object(j_object)
                        }
                     }

                     [<$type_name Inner>]::Uninit => {
                        let jvm_ids = Self::prepare_ids(env);
                        drop(lock);
                        let mut lock = self.0.write().unwrap();
                        *lock = jvm_ids;
                        drop(lock);
                        self.clone_into_jvm_from_jvm_type(env, $($prop_name),*)
                     }
                  }
               }

               $(
                  $crate::convert_jvm_helper_prop!(
                     $jvm_type,
                     [<$type_name Inner>],
                     [<$prop_name _id>],
                     $prop_name,
                     $prop_ret_type,
                     $($prop_jvm_type,)?
                     [<$prop_name _jvm_type>]
                  );
               )*
            }
         }
      )*
   };

   (
      $(
         $(#[$attr:meta])*
         static $var_name:ident = impl struct $type_name:ident
            where jvm_class: $class_fully_qualified_name:literal
         {
            fn clone_into_jvm<'local>(..) -> $jvm_type:ty
               where jvm_static_method: $factory_method_name:expr,
                     jvm_signature: $factory_signature:expr;

            $(
               fn $prop_name:ident <'local>(..) -> $prop_ret_type:ty
                  where $(jvm_type: $prop_jvm_type:ty,)?
                        jvm_getter_method: $getter_method_name:expr,
                        jvm_return_type: $getter_ret_type:expr
            );* $(;)?
         }
      )*
   ) => {
      $(
         $(#[$attr])*
         static $var_name: $type_name = $type_name::new();

         $crate::convert_jvm_helper::paste! {
            $(#[$attr])*
            struct $type_name(
               ::std::sync::RwLock<[<$type_name Inner>]>
            );

            $(#[$attr])*
            enum [<$type_name Inner>] {
               Uninit,
               JvmIds {
                  class: ::jni::objects::GlobalRef,
                  factory_method_id: ::jni::objects::JStaticMethodID,
                  factory_return_type: ::jni::signature::ReturnType,
                  $(
                     [<$prop_name _id>]: (::jni::objects::JMethodID, ::jni::signature::ReturnType)
                  ),*
               }
            }

            $(#[$attr])*
            impl $type_name {
               pub const fn new() -> Self {
                  let inner = [<$type_name Inner>]::Uninit;
                  $type_name(::std::sync::RwLock::new(inner))
               }

               fn get_method_id(
                  env: &mut ::jni::JNIEnv,
                  class: &::jni::objects::GlobalRef,
                  name: &str,
                  ret_type: &str
               ) -> (::jni::objects::JMethodID, ::jni::signature::ReturnType) {
                  let type_signature_str = format!("(){ret_type}");
                  let type_signature = ::jni::signature
                     ::TypeSignature::from_str(&type_signature_str).unwrap();
                  let method_id = env
                     .get_method_id(class, name, &type_signature_str).unwrap();
                  (method_id, type_signature.ret)
               }

               fn prepare_ids(env: &mut ::jni::JNIEnv) -> [<$type_name Inner>] {
                  let class = env.find_class($class_fully_qualified_name).unwrap();
                  let class = env.new_global_ref(class).unwrap();

                  let factory_method_id = env
                     .get_static_method_id(&class, $factory_method_name, $factory_signature).unwrap();

                  let type_signature = ::jni::signature::TypeSignature::from_str($factory_signature).unwrap();

                  $(
                     let [<$prop_name _id>] = Self::get_method_id(
                           env, &class, $getter_method_name, $getter_ret_type
                     );
                  )*

                  [<$type_name Inner>]::JvmIds {
                     class,
                     factory_method_id,
                     factory_return_type: type_signature.ret,
                     $([<$prop_name _id>]),*
                  }
               }

               pub fn clone_into_jvm<'local>(
                  &self,
                  env: &mut ::jni::JNIEnv<'local>,
                  $(
                     $prop_name: $crate::convert_jvm_helper_clone_param_type!(
                        $prop_ret_type$(, $prop_jvm_type)?
                     )
                  ),*
               ) -> $jvm_type {
                  $(
                     let $prop_name = $crate::convert_jvm_helper_clone_arg_conversion!(
                        env, $prop_name, $prop_ret_type$(, $prop_jvm_type)?
                     );
                  )*

                  self.clone_into_jvm_from_jvm_type(
                     env,
                     $($prop_name),*
                  )
               }

               pub fn clone_into_jvm_from_jvm_type<'local>(
                  &self,
                  env: &mut ::jni::JNIEnv<'local>,
                  $(
                     $prop_name: $crate::convert_jvm_helper_clone_jvm_param_type!(
                        $prop_ret_type$(, $prop_jvm_type)?
                     )
                  ),*
               ) -> $jvm_type {
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
                           let j_object = env
                              .call_static_method_unchecked(
                                 class, *factory_method_id, factory_return_type.clone(), &args
                              )
                              .unwrap().l().unwrap();

                           ::panoptiqon::jvm_type::JvmType::from_j_object(j_object)
                        }
                     }

                     [<$type_name Inner>]::Uninit => {
                        let jvm_ids = Self::prepare_ids(env);
                        drop(lock);
                        let mut lock = self.0.write().unwrap();
                        *lock = jvm_ids;
                        drop(lock);
                        self.clone_into_jvm_from_jvm_type(env, $($prop_name),*)
                     }
                  }
               }

               $(
                  $crate::convert_jvm_helper_prop!(
                     $jvm_type,
                     [<$type_name Inner>],
                     [<$prop_name _id>],
                     $prop_name,
                     $prop_ret_type,
                     $($prop_jvm_type,)?
                     [<$prop_name _jvm_type>]
                  );
               )*
            }
         }
      )*
   };
}

#[macro_export]
macro_rules! convert_jvm_helper_clone_param_type {
   ($prop_ret_type:ty) => { $prop_ret_type };
   ($prop_ret_type:ty, $prop_jvm_type:ty) => {
      &(impl ::panoptiqon::convert_jvm::CloneIntoJvm<'local, $prop_jvm_type> + ?Sized)
   };
}

#[macro_export]
macro_rules! convert_jvm_helper_clone_arg_conversion {
   ($jni_env:ident, $prop_name:ident, $prop_ret_type:ty) => { $prop_name };
   ($jni_env:ident, $prop_name:ident, $prop_ret_type:ty, $prop_jvm_type:ty) => {
      ::panoptiqon::convert_jvm::CloneIntoJvm::clone_into_jvm($prop_name, $jni_env)
   };
}

#[macro_export]
macro_rules! convert_jvm_helper_clone_jvm_param_type {
   ($prop_ret_type:ty) => { $prop_ret_type };
   ($prop_ret_type:ty, $prop_jvm_type:ty) => { $prop_jvm_type };
}

#[macro_export]
macro_rules! convert_jvm_helper_prop {
   (
      $jvm_type:ty, $helper_inner:ident, $helper_inner_prop_id:ident,
      $prop_name:ident, $prop_ret_type:tt, $jvm_prop:ident
   ) => {
      fn $prop_name<'local>(
         &self,
         env: &mut ::jni::JNIEnv<'local>,
         instance: &$jvm_type
      ) -> $prop_ret_type {
         use std::ops::Deref;
         use $crate::from_jvalue;

         let lock = self.0.read().unwrap();
         match lock.deref() {
            $helper_inner::JvmIds {
               $helper_inner_prop_id: (getter_id, return_type),
               ..
            } => {
               let jvalue = unsafe {
                  env.call_method_unchecked(
                        ::panoptiqon::jvm_type::JvmType::j_object(instance),
                        getter_id,
                        return_type.clone(),
                        &[]
                     )
                     .unwrap()
               };

               from_jvalue!($prop_ret_type, jvalue)
            }

            $helper_inner::Uninit => {
               let jvm_ids = Self::prepare_ids(env);
               drop(lock);
               let mut lock = self.0.write().unwrap();
               *lock = jvm_ids;
               drop(lock);
               self.$prop_name(env, instance)
            }
         }
      }
   };
   (
      $jvm_type:ty, $helper_inner:ident, $helper_inner_prop_id:ident,
      $prop_name:ident, $prop_ret_type:ty, $prop_jvm_type:ty, $jvm_prop:ident
   ) => {
      fn $prop_name<'local>(
         &self,
         env: &mut ::jni::JNIEnv<'local>,
         instance: &$jvm_type
      ) -> $prop_ret_type {
         let jvm_type = self.$jvm_prop(env, instance);
         ::panoptiqon::convert_jvm::CloneFromJvm::clone_from_jvm(env, &jvm_type)
      }

      fn $jvm_prop<'local>(
         &self,
         env: &mut ::jni::JNIEnv<'local>,
         instance: &$jvm_type
      ) -> $prop_jvm_type {
         use std::ops::Deref;
         use $crate::from_jvalue;

         let lock = self.0.read().unwrap();
         match lock.deref() {
            $helper_inner::JvmIds {
               $helper_inner_prop_id: (getter_id, return_type),
               ..
            } => {
               let jvalue = unsafe {
                  env.call_method_unchecked(
                        ::panoptiqon::jvm_type::JvmType::j_object(instance),
                        getter_id,
                        return_type.clone(),
                        &[]
                     )
                     .unwrap()
               };

               from_jvalue!($prop_jvm_type, jvalue)
            }

            $helper_inner::Uninit => {
               let jvm_ids = Self::prepare_ids(env);
               drop(lock);
               let mut lock = self.0.write().unwrap();
               *lock = jvm_ids;
               drop(lock);
               self.$jvm_prop(env, instance)
            }
         }
      }
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

#[macro_export]
macro_rules! from_jvalue {
    (  i8, $value:expr) => { $value.b().unwrap() };
    ( i16, $value:expr) => { $value.s().unwrap() };
    ( i32, $value:expr) => { $value.i().unwrap() };
    ( i64, $value:expr) => { $value.j().unwrap() };
    ( f32, $value:expr) => { $value.f().unwrap() };
    ( f64, $value:expr) => { $value.d().unwrap() };
    (bool, $value:expr) => { $value.z().unwrap() };
    ($_:ty, $value:expr) => {
       {
          let j_object = $value.l().unwrap();
          unsafe { ::panoptiqon::jvm_type::JvmType::from_j_object(j_object) }
       }
    };
}

#[cfg(feature="jni-test")]
mod jni_tests {
   use std::ops::Deref;

   use jni::JNIEnv;
   use jni::objects::JObject;
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
               #[allow(non_upper_case_globals, non_camel_case_types, dead_code)]
               static $name = impl struct [<$name Helper>]
                  where jvm_class: "com/wcaokaze/probosqis/ext/panoptiqon/TestEntity"
               {
                  fn clone_into_jvm<'local>(..) -> JvmTestEntity<'local>
                     where jvm_constructor: "(ZBSIJFDLjava/lang/String;)V";

                  fn z  <'local>(..) -> bool   where jvm_getter_method: "getZ",   jvm_return_type: "Z";
                  fn b  <'local>(..) -> i8     where jvm_getter_method: "getB",   jvm_return_type: "B";
                  fn s  <'local>(..) -> i16    where jvm_getter_method: "getS",   jvm_return_type: "S";
                  fn i  <'local>(..) -> i32    where jvm_getter_method: "getI",   jvm_return_type: "I";
                  fn j  <'local>(..) -> i64    where jvm_getter_method: "getJ",   jvm_return_type: "J";
                  fn f  <'local>(..) -> f32    where jvm_getter_method: "getF",   jvm_return_type: "F";
                  fn d  <'local>(..) -> f64    where jvm_getter_method: "getD",   jvm_return_type: "D";
                  fn str<'local>(..) -> String where jvm_type: JvmString<'local>, jvm_getter_method: "getStr", jvm_return_type: "Ljava/lang/String;";
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
      assert!(matches!(helper_inner.deref(), &variantStrsOnInit_helperHelperInner::Uninit));
   }

   helper!(variantJvmIdsAfterCloneIntoJvm_helper);

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_ext_panoptiqon_ConvertJniHelperTest_variantJvmIdsAfterCloneIntoJvm(
      mut env: JNIEnv,
      _obj: JObject
   ) {
      variantJvmIdsAfterCloneIntoJvm_helper.clone_into_jvm(
         &mut env,
         false,
         0i8,
         0i16,
         0i32,
         0i64,
         0.0f32,
         0.0f64,
         "",
      );

      let helper_inner= variantJvmIdsAfterCloneIntoJvm_helper.0.read().unwrap();
      assert!(matches!(helper_inner.deref(), &variantJvmIdsAfterCloneIntoJvm_helperHelperInner::JvmIds { .. }));
   }

   helper!(variantJvmIdsAfterGet_helper);

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_ext_panoptiqon_ConvertJniHelperTest_variantJvmIdsAfterGet_00024assert<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      jvm_entity: JvmTestEntity<'local>
   ) {
      variantJvmIdsAfterGet_helper.z(&mut env, &jvm_entity);

      let helper_inner = variantJvmIdsAfterGet_helper.0.read().unwrap();
      assert!(matches!(helper_inner.deref(), &variantJvmIdsAfterGet_helperHelperInner::JvmIds { .. }));
   }

   helper!(cloneIntoJvm_helper);

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_ext_panoptiqon_ConvertJniHelperTest_cloneIntoJvm_00024createEntity<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>
   ) -> JvmTestEntity<'local> {
      cloneIntoJvm_helper.clone_into_jvm(
         &mut env,
         false,
         0i8,
         1i16,
         2i32,
         3i64,
         4.5f32,
         6.75f64,
         "9012345",
      )
   }

   helper!(cloneFromJvm_helper);

   #[no_mangle]
   extern "C" fn Java_com_wcaokaze_probosqis_ext_panoptiqon_ConvertJniHelperTest_cloneFromJvm_00024assert<'local>(
      mut env: JNIEnv<'local>,
      _obj: JObject<'local>,
      jvm_entity: JvmTestEntity<'local>
   ) {
      let z = cloneFromJvm_helper.z(&mut env, &jvm_entity);
      let b = cloneFromJvm_helper.b(&mut env, &jvm_entity);
      let s = cloneFromJvm_helper.s(&mut env, &jvm_entity);
      let i = cloneFromJvm_helper.i(&mut env, &jvm_entity);
      let j = cloneFromJvm_helper.j(&mut env, &jvm_entity);
      let f = cloneFromJvm_helper.f(&mut env, &jvm_entity);
      let d = cloneFromJvm_helper.d(&mut env, &jvm_entity);

      let str = cloneFromJvm_helper.str(&mut env, &jvm_entity);

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
