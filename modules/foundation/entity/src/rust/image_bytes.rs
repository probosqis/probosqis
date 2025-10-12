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

use bytes::Bytes;
use serde::{Deserialize, Deserializer, Serialize, Serializer};
use url::Url;

#[cfg(feature = "jvm")]
use {
   ext_panoptiqon::convert_jvm_helper,
   jni::JNIEnv,
   jni::objects::{JByteArray, JObject},
   panoptiqon::convert_jvm::{CloneFromJvm, CloneIntoJvm},
   panoptiqon::jvm_type::JvmType,
   panoptiqon::jvm_types::{JvmNullable, JvmString, JvmUnit},
   crate::jvm_types::JvmImage,
};

#[derive(Serialize, Deserialize)]
pub struct ImageBytes {
   pub url: Url,
   pub image_bytes: SerializableBytes
}

impl ImageBytes {
   pub fn new(url: Url, bytes: Bytes) -> ImageBytes {
      ImageBytes {
         url,
         image_bytes: SerializableBytes(bytes)
      }
   }
}

pub struct SerializableBytes(pub Bytes);

impl Serialize for SerializableBytes {
   fn serialize<S>(&self, serializer: S) -> Result<S::Ok, S::Error>
      where S: Serializer
   {
      serializer.serialize_bytes(&self.0)
   }
}

impl<'de> Deserialize<'de> for SerializableBytes {
   fn deserialize<D>(deserializer: D) -> Result<SerializableBytes, D::Error>
      where D: Deserializer<'de>
   {
      use std::fmt;
      use serde::de::{Error, Visitor};

      struct ImageBytesVisitor;

      impl<'de> Visitor<'de> for ImageBytesVisitor {
         type Value = SerializableBytes;

         fn expecting(&self, formatter: &mut fmt::Formatter) -> fmt::Result {
            formatter.write_str("bytes of an image")
         }

         fn visit_bytes<E>(self, v: &[u8]) -> Result<SerializableBytes, E>
            where E: Error,
         {
            let bytes = Bytes::copy_from_slice(v);
            let image_bytes = SerializableBytes(bytes);
            Ok(image_bytes)
         }
      }

      deserializer.deserialize_bytes(ImageBytesVisitor)
   }
}

#[cfg(feature = "jvm")]
#[repr(transparent)]
struct JvmByteArray<'local>(JByteArray<'local>);

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmByteArray<'local>> for Bytes {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmByteArray<'local> {
      let byte_array = env.byte_array_from_slice(&self).unwrap();
      JvmByteArray(byte_array)
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmByteArray<'local>> for Bytes {
   fn clone_from_jvm(
      env: &mut JNIEnv<'local>,
      jvm_instance: &JvmByteArray<'local>
   ) -> Bytes {
      Bytes::from(
         env.convert_byte_array(&jvm_instance.0).unwrap()
      )
   }
}

#[cfg(feature = "jvm")]
impl<'local> JvmType<'local> for JvmByteArray<'local> {
   unsafe fn from_j_object(j_object: JObject<'local>) -> JvmByteArray<'local> {
      JvmByteArray(j_object.into())
   }

   fn j_object(&self) -> &JObject<'local> {
      &self.0
   }

   fn into_j_object(self) -> JObject<'local> {
      self.0.into()
   }
}

#[cfg(feature = "jvm")]
convert_jvm_helper! {
   static IMAGE_BYTES_HELPER = impl struct ImageBytesConvertHelper
   where
      jvm_class: "com/wcaokaze/probosqis/entity/ImageBytes"
   {
      fn clone_into_jvm<'local>(..) -> JvmImage<'local>
      where
         jvm_constructor: "(\
            Ljava/lang/String;\
            [B\
            Lkotlin/Unit;\
         )V";

      fn raw_url<'local>(..) -> String
      where
         jvm_type: JvmString<'local>,
         jvm_getter_method: "getRawUrl",
         jvm_return_type: "Ljava/lang/String;";

      fn bytes<'local>(..) -> Bytes
      where
         jvm_type: JvmByteArray<'local>,
         jvm_getter_method: "getBytes",
         jvm_return_type: "[B";

      fn dummy<'local>(..) -> Option<()>
      where
         jvm_type: JvmNullable<'local, JvmUnit<'local>>,
         jvm_getter_method: "getDummy",
         jvm_return_type: "Lkotlin/Unit;";
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmImage<'local>> for ImageBytes {
   fn clone_into_jvm(
      &self,
      env: &mut JNIEnv<'local>
   ) -> JvmImage<'local> {
      IMAGE_BYTES_HELPER.clone_into_jvm(
         env,
         self.url.as_str(),
         &self.image_bytes.0,
         &None::<()>,
      )
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmImage<'local>> for ImageBytes {
   fn clone_from_jvm(
      env: &mut JNIEnv<'local>,
      jvm_instance: &JvmImage<'local>
   ) -> ImageBytes {
      let raw_url = IMAGE_BYTES_HELPER.raw_url(env, jvm_instance);
      let bytes   = IMAGE_BYTES_HELPER.bytes  (env, jvm_instance);

      ImageBytes::new(raw_url.parse().unwrap(), bytes)
   }
}
