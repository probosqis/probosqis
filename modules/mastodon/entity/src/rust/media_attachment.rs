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

use std::time::Duration;
use serde::Deserialize;
use url::Url;

#[cfg(feature = "jvm")]
use {
   ext_panoptiqon::convert_jvm_helper,
   jni::JNIEnv,
   panoptiqon::convert_jvm::{CloneFromJvm, CloneIntoJvm},
   panoptiqon::jvm_type::JvmType,
   panoptiqon::jvm_types::{JvmDouble, JvmLong, JvmNullable, JvmString, JvmUnit},
   crate::jvm_types::{
      JvmMediaAttachment, JvmMediaAttachmentAudioSize, JvmMediaAttachmentImageFocus,
      JvmMediaAttachmentImageSize, JvmMediaAttachmentMetadata,
      JvmMediaAttachmentVideoSize,
   },
};

#[derive(Debug, PartialEq, Clone, Deserialize)]
pub struct MediaAttachment {
   pub id: MediaAttachmentId,
   pub url: Option<Url>,
   pub preview_url: Option<Url>,
   pub remote_url: Option<Url>,
   pub metadata: Option<MediaAttachmentMetadata>,
   pub description: Option<String>,
   pub blurhash: Option<String>,
}

#[derive(Debug, Eq, PartialEq, Clone, Deserialize)]
pub struct MediaAttachmentId(pub String);

#[derive(Debug, PartialEq, Clone, Deserialize)]
pub enum MediaAttachmentMetadata {
   Image {
      original_size: Option<ImageSize>,
      small_size: Option<ImageSize>,
      focus: Option<ImageFocus>,
   },

   Video {
      original_size: Option<VideoSize>,
      small_size: Option<ImageSize>,
      length: Option<String>,
      fps: Option<i64>,
      audio_encode: Option<String>,
      audio_bitrate: Option<String>,
      audio_channels: Option<String>,
   },

   Gifv {
      original_size: Option<VideoSize>,
      small_size: Option<ImageSize>,
      length: Option<String>,
      fps: Option<i64>,
   },

   Audio {
      original_size: Option<AudioSize>,
      length: Option<String>,
      audio_encode: Option<String>,
      audio_bitrate: Option<String>,
      audio_channels: Option<String>,
   },
}

#[derive(Debug, Eq, PartialEq, Clone, Deserialize)]
pub struct ImageSize {
   pub width: i64,
   pub height: i64,
}

#[derive(Debug, PartialEq, Clone, Deserialize)]
pub struct ImageFocus {
   pub x: f64,
   pub y: f64,
}

#[derive(Debug, Eq, PartialEq, Clone, Deserialize)]
pub struct VideoSize {
   pub width: Option<i64>,
   pub height: Option<i64>,
   pub frame_rate: Option<String>,
   pub duration: Option<Duration>,
   pub bitrate: Option<i64>,
}

#[derive(Debug, Eq, PartialEq, Clone, Deserialize)]
pub struct AudioSize {
   pub duration: Option<Duration>,
   pub bitrate: Option<i64>,
}

#[cfg(feature = "jvm")]
convert_jvm_helper! {
   static MEDIA_ATTACHMENT_HELPER = impl struct MediaAttachmentConvertHelper
      where jvm_class: "com/wcaokaze/probosqis/mastodon/entity/MediaAttachment"
   {
      fn clone_into_jvm<'local>(..) -> JvmMediaAttachment<'local>
         where jvm_constructor: "(\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Lcom/wcaokaze/probosqis/mastodon/entity/MediaAttachment$Metadata;\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Lkotlin/Unit;\
         )V";

      fn raw_id<'local>(..) -> String
         where jvm_type: JvmString<'local>,
               jvm_getter_method: "getRawId",
               jvm_return_type: "Ljava/lang/String;";

      fn raw_url<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getRawUrl",
               jvm_return_type: "Ljava/lang/String;";

      fn raw_preview_url<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getRawPreviewUrl",
               jvm_return_type: "Ljava/lang/String;";

      fn raw_remote_url<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getRawRemoteUrl",
               jvm_return_type: "Ljava/lang/String;";

      fn metadata<'local>(..) -> Option<MediaAttachmentMetadata>
         where jvm_type: JvmNullable<'local, JvmMediaAttachmentMetadata<'local>>,
               jvm_getter_method: "getMetadata",
               jvm_return_type: "Lcom/wcaokaze/probosqis/mastodon/entity/MediaAttachment$Metadata;";

      fn description<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getDescription",
               jvm_return_type: "Ljava/lang/String;";

      fn blurhash<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getBlurhash",
               jvm_return_type: "Ljava/lang/String;";

      fn dummy<'local>(..) -> Option<()>
         where jvm_type: JvmNullable<'local, JvmUnit<'local>>,
               jvm_getter_method: "getDummy",
               jvm_return_type: "Lkotlin/Unit;";
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmMediaAttachment<'local>> for MediaAttachment {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmMediaAttachment<'local> {
      MEDIA_ATTACHMENT_HELPER.clone_into_jvm(
         env,
         &self.id.0,
         &self.url.as_ref().map(Url::as_str),
         &self.preview_url.as_ref().map(Url::as_str),
         &self.remote_url.as_ref().map(Url::as_str),
         &self.metadata,
         &self.description,
         &self.blurhash,
         &None::<()>,
      )
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmMediaAttachment<'local>> for MediaAttachment {
   fn clone_from_jvm(
      env: &mut JNIEnv<'local>,
      jvm_instance: &JvmMediaAttachment<'local>
   ) -> MediaAttachment {
      let raw_id          = MEDIA_ATTACHMENT_HELPER.raw_id         (env, jvm_instance);
      let raw_url         = MEDIA_ATTACHMENT_HELPER.raw_url        (env, jvm_instance);
      let raw_preview_url = MEDIA_ATTACHMENT_HELPER.raw_preview_url(env, jvm_instance);
      let raw_remote_url  = MEDIA_ATTACHMENT_HELPER.raw_remote_url (env, jvm_instance);
      let metadata        = MEDIA_ATTACHMENT_HELPER.metadata       (env, jvm_instance);
      let description     = MEDIA_ATTACHMENT_HELPER.description    (env, jvm_instance);
      let blurhash        = MEDIA_ATTACHMENT_HELPER.blurhash       (env, jvm_instance);

      MediaAttachment {
         id: MediaAttachmentId(raw_id),
         url:         raw_url        .map(|u| u.parse().unwrap()),
         preview_url: raw_preview_url.map(|u| u.parse().unwrap()),
         remote_url:  raw_remote_url .map(|u| u.parse().unwrap()),
         metadata,
         description,
         blurhash,
      }
   }
}

#[cfg(feature = "jvm")]
convert_jvm_helper! {
   static MEDIA_ATTACHMENT_IMAGE_METADATA_HELPER = impl struct MediaAttachmentImageMetadataConvertHelper
      where jvm_class: "com/wcaokaze/probosqis/mastodon/entity/MediaAttachment$Metadata$Image"
   {
      fn clone_into_jvm<'local>(..) -> JvmMediaAttachmentMetadata<'local>
         where jvm_constructor: "(\
            Lcom/wcaokaze/probosqis/mastodon/entity/MediaAttachment$ImageSize;\
            Lcom/wcaokaze/probosqis/mastodon/entity/MediaAttachment$ImageSize;\
            Lcom/wcaokaze/probosqis/mastodon/entity/MediaAttachment$ImageFocus;\
         )V";

      fn original_size<'local>(..) -> Option<ImageSize>
         where jvm_type: JvmNullable<'local, JvmMediaAttachmentImageSize<'local>>,
               jvm_getter_method: "getOriginalSize",
               jvm_return_type: "Lcom/wcaokaze/probosqis/mastodon/entity/MediaAttachment$ImageSize;";

      fn small_size<'local>(..) -> Option<ImageSize>
         where jvm_type: JvmNullable<'local, JvmMediaAttachmentImageSize<'local>>,
               jvm_getter_method: "getSmallSize",
               jvm_return_type: "Lcom/wcaokaze/probosqis/mastodon/entity/MediaAttachment$ImageSize;";

      fn focus<'local>(..) -> Option<ImageFocus>
         where jvm_type: JvmNullable<'local, JvmMediaAttachmentImageFocus<'local>>,
               jvm_getter_method: "getFocus",
               jvm_return_type: "Lcom/wcaokaze/probosqis/mastodon/entity/MediaAttachment$ImageFocus;";
   }

   static MEDIA_ATTACHMENT_VIDEO_METADATA_HELPER = impl struct MediaAttachmentVideoMetadataConvertHelper
      where jvm_class: "com/wcaokaze/probosqis/mastodon/entity/MediaAttachment$Metadata$Video"
   {
      fn clone_into_jvm<'local>(..) -> JvmMediaAttachmentMetadata<'local>
         where jvm_constructor: "(\
            Lcom/wcaokaze/probosqis/mastodon/entity/MediaAttachment$VideoSize;\
            Lcom/wcaokaze/probosqis/mastodon/entity/MediaAttachment$ImageSize;\
            Ljava/lang/String;\
            Ljava/lang/Long;\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Ljava/lang/String;\
         )V";

      fn original_size<'local>(..) -> Option<VideoSize>
         where jvm_type: JvmNullable<'local, JvmMediaAttachmentVideoSize<'local>>,
               jvm_getter_method: "getOriginalSize",
               jvm_return_type: "Lcom/wcaokaze/probosqis/mastodon/entity/MediaAttachment$VideoSize;";

      fn small_size<'local>(..) -> Option<ImageSize>
         where jvm_type: JvmNullable<'local, JvmMediaAttachmentImageSize<'local>>,
               jvm_getter_method: "getSmallSize",
               jvm_return_type: "Lcom/wcaokaze/probosqis/mastodon/entity/MediaAttachment$ImageSize;";

      fn length<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getLength",
               jvm_return_type: "Ljava/lang/String;";

      fn fps<'local>(..) -> Option<i64>
         where jvm_type: JvmNullable<'local, JvmLong<'local>>,
               jvm_getter_method: "getFps",
               jvm_return_type: "Ljava/lang/Long;";

      fn audio_encode<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getAudioEncode",
               jvm_return_type: "Ljava/lang/String;";

      fn audio_bitrate<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getAudioBitrate",
               jvm_return_type: "Ljava/lang/String;";

      fn audio_channels<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getAudioChannels",
               jvm_return_type: "Ljava/lang/String;";
   }

   static MEDIA_ATTACHMENT_GIFV_METADATA_HELPER = impl struct MediaAttachmentGifvMetadataConvertHelper
      where jvm_class: "com/wcaokaze/probosqis/mastodon/entity/MediaAttachment$Metadata$Gifv"
   {
      fn clone_into_jvm<'local>(..) -> JvmMediaAttachmentMetadata<'local>
         where jvm_constructor: "(\
            Lcom/wcaokaze/probosqis/mastodon/entity/MediaAttachment$VideoSize;\
            Lcom/wcaokaze/probosqis/mastodon/entity/MediaAttachment$ImageSize;\
            Ljava/lang/String;\
            Ljava/lang/Long;\
         )V";

      fn original_size<'local>(..) -> Option<VideoSize>
         where jvm_type: JvmNullable<'local, JvmMediaAttachmentVideoSize<'local>>,
               jvm_getter_method: "getOriginalSize",
               jvm_return_type: "Lcom/wcaokaze/probosqis/mastodon/entity/MediaAttachment$VideoSize;";

      fn small_size<'local>(..) -> Option<ImageSize>
         where jvm_type: JvmNullable<'local, JvmMediaAttachmentImageSize<'local>>,
               jvm_getter_method: "getSmallSize",
               jvm_return_type: "Lcom/wcaokaze/probosqis/mastodon/entity/MediaAttachment$ImageSize;";

      fn length<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getLength",
               jvm_return_type: "Ljava/lang/String;";

      fn fps<'local>(..) -> Option<i64>
         where jvm_type: JvmNullable<'local, JvmLong<'local>>,
               jvm_getter_method: "getFps",
               jvm_return_type: "Ljava/lang/Long;";
   }

   static MEDIA_ATTACHMENT_AUDIO_METADATA_HELPER = impl struct MediaAttachmentAudioMetadataConvertHelper
      where jvm_class: "com/wcaokaze/probosqis/mastodon/entity/MediaAttachment$Metadata$Audio"
   {
      fn clone_into_jvm<'local>(..) -> JvmMediaAttachmentMetadata<'local>
         where jvm_constructor: "(\
            Lcom/wcaokaze/probosqis/mastodon/entity/MediaAttachment$AudioSize;\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Ljava/lang/String;\
            Ljava/lang/String;\
         )V";

      fn original_size<'local>(..) -> Option<AudioSize>
         where jvm_type: JvmNullable<'local, JvmMediaAttachmentAudioSize<'local>>,
               jvm_getter_method: "getOriginalSize",
               jvm_return_type: "Lcom/wcaokaze/probosqis/mastodon/entity/MediaAttachment$AudioSize;";

      fn length<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getLength",
               jvm_return_type: "Ljava/lang/String;";

      fn audio_encode<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getAudioEncode",
               jvm_return_type: "Ljava/lang/String;";

      fn audio_bitrate<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getAudioBitrate",
               jvm_return_type: "Ljava/lang/String;";

      fn audio_channels<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getAudioChannels",
               jvm_return_type: "Ljava/lang/String;";
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmMediaAttachmentMetadata<'local>> for MediaAttachmentMetadata {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmMediaAttachmentMetadata<'local> {
      match self {
         MediaAttachmentMetadata::Image {
            original_size, small_size, focus,
         } => {
            MEDIA_ATTACHMENT_IMAGE_METADATA_HELPER.clone_into_jvm(
               env,
               original_size,
               small_size,
               focus,
            )
         }

         MediaAttachmentMetadata::Video {
            original_size, small_size, length, fps, audio_encode, audio_bitrate,
            audio_channels,
         } => {
            MEDIA_ATTACHMENT_VIDEO_METADATA_HELPER.clone_into_jvm(
               env,
               original_size,
               small_size,
               length,
               fps,
               audio_encode,
               audio_bitrate,
               audio_channels,
            )
         }

         MediaAttachmentMetadata::Gifv {
            original_size, small_size, length, fps,
         } => {
            MEDIA_ATTACHMENT_GIFV_METADATA_HELPER.clone_into_jvm(
               env,
               original_size,
               small_size,
               length,
               fps,
            )
         }

         MediaAttachmentMetadata::Audio {
            original_size, length, audio_encode, audio_bitrate, audio_channels,
         } => {
            MEDIA_ATTACHMENT_AUDIO_METADATA_HELPER.clone_into_jvm(
               env,
               original_size,
               length,
               audio_encode,
               audio_bitrate,
               audio_channels,
            )
         }
      }
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmMediaAttachmentMetadata<'local>> for MediaAttachmentMetadata {
   fn clone_from_jvm(
      env: &mut JNIEnv<'local>,
      jvm_instance: &JvmMediaAttachmentMetadata<'local>
   ) -> MediaAttachmentMetadata {
      if env
         .is_instance_of(
            jvm_instance.j_object(),
            "com/wcaokaze/probosqis/mastodon/entity/MediaAttachment$Metadata$Image"
         )
         .unwrap()
      {
         let original_size = MEDIA_ATTACHMENT_IMAGE_METADATA_HELPER.original_size(env, jvm_instance);
         let small_size    = MEDIA_ATTACHMENT_IMAGE_METADATA_HELPER.small_size   (env, jvm_instance);
         let focus         = MEDIA_ATTACHMENT_IMAGE_METADATA_HELPER.focus        (env, jvm_instance);

         MediaAttachmentMetadata::Image {
            original_size,
            small_size,
            focus,
         }
      } else if env
         .is_instance_of(
            jvm_instance.j_object(),
            "com/wcaokaze/probosqis/mastodon/entity/MediaAttachment$Metadata$Video"
         )
         .unwrap()
      {
         let original_size  = MEDIA_ATTACHMENT_VIDEO_METADATA_HELPER.original_size (env, jvm_instance);
         let small_size     = MEDIA_ATTACHMENT_VIDEO_METADATA_HELPER.small_size    (env, jvm_instance);
         let length         = MEDIA_ATTACHMENT_VIDEO_METADATA_HELPER.length        (env, jvm_instance);
         let fps            = MEDIA_ATTACHMENT_VIDEO_METADATA_HELPER.fps           (env, jvm_instance);
         let audio_encode   = MEDIA_ATTACHMENT_VIDEO_METADATA_HELPER.audio_encode  (env, jvm_instance);
         let audio_bitrate  = MEDIA_ATTACHMENT_VIDEO_METADATA_HELPER.audio_bitrate (env, jvm_instance);
         let audio_channels = MEDIA_ATTACHMENT_VIDEO_METADATA_HELPER.audio_channels(env, jvm_instance);

         MediaAttachmentMetadata::Video {
            original_size,
            small_size,
            length,
            fps,
            audio_encode,
            audio_bitrate,
            audio_channels,
         }
      } else if env
         .is_instance_of(
            jvm_instance.j_object(),
            "com/wcaokaze/probosqis/mastodon/entity/MediaAttachment$Metadata$Gifv"
         )
         .unwrap()
      {
         let original_size = MEDIA_ATTACHMENT_GIFV_METADATA_HELPER.original_size(env, jvm_instance);
         let small_size    = MEDIA_ATTACHMENT_GIFV_METADATA_HELPER.small_size   (env, jvm_instance);
         let length        = MEDIA_ATTACHMENT_GIFV_METADATA_HELPER.length       (env, jvm_instance);
         let fps           = MEDIA_ATTACHMENT_GIFV_METADATA_HELPER.fps          (env, jvm_instance);

         MediaAttachmentMetadata::Gifv {
            original_size,
            small_size,
            length,
            fps,
         }
      } else if env
         .is_instance_of(
            jvm_instance.j_object(),
            "com/wcaokaze/probosqis/mastodon/entity/MediaAttachment$Metadata$Audio"
         )
         .unwrap()
      {
         let original_size  = MEDIA_ATTACHMENT_AUDIO_METADATA_HELPER.original_size (env, jvm_instance);
         let length         = MEDIA_ATTACHMENT_AUDIO_METADATA_HELPER.length        (env, jvm_instance);
         let audio_encode   = MEDIA_ATTACHMENT_AUDIO_METADATA_HELPER.audio_encode  (env, jvm_instance);
         let audio_bitrate  = MEDIA_ATTACHMENT_AUDIO_METADATA_HELPER.audio_bitrate (env, jvm_instance);
         let audio_channels = MEDIA_ATTACHMENT_AUDIO_METADATA_HELPER.audio_channels(env, jvm_instance);

         MediaAttachmentMetadata::Audio {
            original_size,
            length,
            audio_encode,
            audio_bitrate,
            audio_channels,
         }
      } else {
         panic!("Unexpected class");
      }
   }
}

#[cfg(feature = "jvm")]
convert_jvm_helper! {
   static IMAGE_SIZE_HELPER = impl struct ImageSizeConvertHelper
      where jvm_class: "com/wcaokaze/probosqis/mastodon/entity/MediaAttachment$ImageSize"
   {
      fn clone_into_jvm<'local>(..) -> JvmMediaAttachmentImageSize<'local>
         where jvm_constructor: "(JJ)V";

      fn width<'local>(..) -> i64
         where jvm_getter_method: "getWidth",
               jvm_return_type: "J";

      fn height<'local>(..) -> i64
         where jvm_getter_method: "getHeight",
               jvm_return_type: "J";
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmMediaAttachmentImageSize<'local>> for ImageSize {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmMediaAttachmentImageSize<'local> {
      IMAGE_SIZE_HELPER.clone_into_jvm(
         env,
         self.width,
         self.height,
      )
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmMediaAttachmentImageSize<'local>> for ImageSize {
   fn clone_from_jvm(
      env: &mut JNIEnv<'local>,
      jvm_instance: &JvmMediaAttachmentImageSize<'local>
   ) -> ImageSize {
      let width  = IMAGE_SIZE_HELPER.width (env, jvm_instance);
      let height = IMAGE_SIZE_HELPER.height(env, jvm_instance);

      ImageSize {
         width,
         height,
      }
   }
}

#[cfg(feature = "jvm")]
convert_jvm_helper! {
   static IMAGE_FOCUS_HELPER = impl struct ImageFocusConvertHelper
      where jvm_class: "com/wcaokaze/probosqis/mastodon/entity/MediaAttachment$ImageFocus"
   {
      fn clone_into_jvm<'local>(..) -> JvmMediaAttachmentImageFocus<'local>
         where jvm_constructor: "(DD)V";

      fn x<'local>(..) -> f64
         where jvm_getter_method: "getX",
               jvm_return_type: "D";

      fn y<'local>(..) -> f64
         where jvm_getter_method: "getY",
               jvm_return_type: "D";
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmMediaAttachmentImageFocus<'local>> for ImageFocus {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmMediaAttachmentImageFocus<'local> {
      IMAGE_FOCUS_HELPER.clone_into_jvm(
         env,
         self.x,
         self.y,
      )
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmMediaAttachmentImageFocus<'local>> for ImageFocus {
   fn clone_from_jvm(
      env: &mut JNIEnv<'local>,
      jvm_instance: &JvmMediaAttachmentImageFocus<'local>
   ) -> ImageFocus {
      let x = IMAGE_FOCUS_HELPER.x(env, jvm_instance);
      let y = IMAGE_FOCUS_HELPER.y(env, jvm_instance);

      ImageFocus {
         x,
         y,
      }
   }
}

#[cfg(feature = "jvm")]
convert_jvm_helper! {
   static VIDEO_SIZE_HELPER = impl struct VideoSizeConvertHelper
      where jvm_class: "com/wcaokaze/probosqis/mastodon/entity/MediaAttachment$VideoSize"
   {
      fn clone_into_jvm<'local>(..) -> JvmMediaAttachmentVideoSize<'local>
         where jvm_constructor: "(\
            Ljava/lang/Long;\
            Ljava/lang/Long;\
            Ljava/lang/String;\
            Ljava/lang/Double;\
            Ljava/lang/Long;\
            Lkotlin/Unit;\
         )V";

      fn width<'local>(..) -> Option<i64>
         where jvm_type: JvmNullable<'local, JvmLong<'local>>,
               jvm_getter_method: "getWidth",
               jvm_return_type: "Ljava/lang/Long;";

      fn height<'local>(..) -> Option<i64>
         where jvm_type: JvmNullable<'local, JvmLong<'local>>,
               jvm_getter_method: "getHeight",
               jvm_return_type: "Ljava/lang/Long;";

      fn frame_rate<'local>(..) -> Option<String>
         where jvm_type: JvmNullable<'local, JvmString<'local>>,
               jvm_getter_method: "getFrameRate",
               jvm_return_type: "Ljava/lang/String;";

      fn duration_secs<'local>(..) -> Option<f64>
         where jvm_type: JvmNullable<'local, JvmDouble<'local>>,
               jvm_getter_method: "getDurationSecs",
               jvm_return_type: "Ljava/lang/Double;";

      fn bitrate<'local>(..) -> Option<i64>
         where jvm_type: JvmNullable<'local, JvmLong<'local>>,
               jvm_getter_method: "getBitrate",
               jvm_return_type: "Ljava/lang/Long;";

      fn dummy<'local>(..) -> Option<()>
         where jvm_type: JvmNullable<'local, JvmUnit<'local>>,
               jvm_getter_method: "getDummy",
               jvm_return_type: "Lkotlin/Unit;";
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmMediaAttachmentVideoSize<'local>> for VideoSize {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmMediaAttachmentVideoSize<'local> {
      VIDEO_SIZE_HELPER.clone_into_jvm(
         env,
         &self.width,
         &self.height,
         &self.frame_rate,
         &self.duration.as_ref().map(Duration::as_secs_f64),
         &self.bitrate,
         &None::<()>,
      )
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmMediaAttachmentVideoSize<'local>> for VideoSize {
   fn clone_from_jvm(
      env: &mut JNIEnv<'local>,
      jvm_instance: &JvmMediaAttachmentVideoSize<'local>
   ) -> VideoSize {
      let width         = VIDEO_SIZE_HELPER.width        (env, jvm_instance);
      let height        = VIDEO_SIZE_HELPER.height       (env, jvm_instance);
      let frame_rate    = VIDEO_SIZE_HELPER.frame_rate   (env, jvm_instance);
      let duration_secs = VIDEO_SIZE_HELPER.duration_secs(env, jvm_instance);
      let bitrate       = VIDEO_SIZE_HELPER.bitrate      (env, jvm_instance);

      VideoSize {
         width,
         height,
         frame_rate,
         duration: duration_secs.map(Duration::from_secs_f64),
         bitrate,
      }
   }
}

#[cfg(feature = "jvm")]
convert_jvm_helper! {
   static AUDIO_SIZE_HELPER = impl struct AudioSizeConvertHelper
      where jvm_class: "com/wcaokaze/probosqis/mastodon/entity/MediaAttachment$AudioSize"
   {
      fn clone_into_jvm<'local>(..) -> JvmMediaAttachmentAudioSize<'local>
         where jvm_constructor: "(\
            Ljava/lang/Double;\
            Ljava/lang/Long;\
            Lkotlin/Unit;\
         )V";

      fn duration_secs<'local>(..) -> Option<f64>
         where jvm_type: JvmNullable<'local, JvmDouble<'local>>,
               jvm_getter_method: "getDurationSecs",
               jvm_return_type: "Ljava/lang/Double;";

      fn bitrate<'local>(..) -> Option<i64>
         where jvm_type: JvmNullable<'local, JvmLong<'local>>,
               jvm_getter_method: "getBitrate",
               jvm_return_type: "Ljava/lang/Long;";

      fn dummy<'local>(..) -> Option<()>
         where jvm_type: JvmNullable<'local, JvmUnit<'local>>,
               jvm_getter_method: "getDummy",
               jvm_return_type: "Lkotlin/Unit;";
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneIntoJvm<'local, JvmMediaAttachmentAudioSize<'local>> for AudioSize {
   fn clone_into_jvm(&self, env: &mut JNIEnv<'local>) -> JvmMediaAttachmentAudioSize<'local> {
      AUDIO_SIZE_HELPER.clone_into_jvm(
         env,
         &self.duration.as_ref().map(Duration::as_secs_f64),
         &self.bitrate,
         &None::<()>,
      )
   }
}

#[cfg(feature = "jvm")]
impl<'local> CloneFromJvm<'local, JvmMediaAttachmentAudioSize<'local>> for AudioSize {
   fn clone_from_jvm(
      env: &mut JNIEnv<'local>,
      jvm_instance: &JvmMediaAttachmentAudioSize<'local>
   ) -> AudioSize {
      let duration_secs = AUDIO_SIZE_HELPER.duration_secs(env, jvm_instance);
      let bitrate       = AUDIO_SIZE_HELPER.bitrate      (env, jvm_instance);

      AudioSize {
         duration: duration_secs.map(Duration::from_secs_f64),
         bitrate,
      }
   }
}
