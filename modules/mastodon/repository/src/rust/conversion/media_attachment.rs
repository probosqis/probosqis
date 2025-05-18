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

use mastodon_entity::media_attachment::{AudioSize, ImageFocus, ImageSize, MediaAttachment, MediaAttachmentMetadata, VideoSize};
use mastodon_webapi::entity::media_attachment::{
   MediaAttachment as ApiMediaAttachment,
   MediaAttachmentFocus as ApiMediaAttachmentFocus,
   MediaAttachmentMetadata as ApiMediaAttachmentMetadata,
   MediaAttachmentSize as ApiMediaAttachmentSize,
};

pub fn from_api(entity: ApiMediaAttachment) -> anyhow::Result<MediaAttachment> {
   use mastodon_entity::media_attachment::MediaAttachmentId;

   let ApiMediaAttachment {
      id, r#type, url, preview_url, remote_url, meta, description, blurhash,
      text_url: _,
   } = entity;

   let media_attachment = MediaAttachment {
      id: MediaAttachmentId(id.ok_or(anyhow::format_err!("No media attachment ID"))?),
      url: url.and_then(|u| u.parse().ok()),
      preview_url: preview_url.and_then(|u| u.parse().ok()),
      remote_url: remote_url.and_then(|u| u.parse().ok()),
      metadata: meta.and_then(|m| metadata_from_api(r#type, m).ok()),
      description,
      blurhash,
   };

   Ok(media_attachment)
}

pub fn metadata_from_api(
   media_type: Option<String>,
   entity: ApiMediaAttachmentMetadata
) -> anyhow::Result<MediaAttachmentMetadata> {
   let Some(mut media_type) = media_type else {
      return Err(anyhow::format_err!("No media type"))
   };

   media_type.make_ascii_lowercase();

   match media_type.as_str() {
      "image" => {
         let ApiMediaAttachmentMetadata {
            original,
            small,
            focus,
            length: _,
            duration: _,
            fps: _,
            size: _,
            width: _,
            height: _,
            aspect: _,
            audio_encode: _,
            audio_bitrate: _,
            audio_channels: _,
         } = entity;

         let media_attachment_metadata = MediaAttachmentMetadata::Image {
            original_size: original.and_then(|s| image_size_from_api(s).ok()),
            small_size: small.and_then(|s| image_size_from_api(s).ok()),
            focus: focus.and_then(|f| focus_from_api(f).ok()),
         };

         Ok(media_attachment_metadata)
      },
      "video" => {
         use std::time::Duration;

         let ApiMediaAttachmentMetadata {
            original,
            small,
            focus: _,
            length,
            duration,
            fps,
            size: _,
            width,
            height,
            aspect: _,
            audio_encode,
            audio_bitrate,
            audio_channels,
         } = entity;

         let mut original_size = original.and_then(|s| video_size_from_api(s).ok());

         if let Some(ref mut original_size) = original_size {
            if original_size.width.is_none() {
               original_size.width = width;
            }

            if original_size.height.is_none() {
               original_size.height = height;
            }

            if original_size.duration.is_none() {
               original_size.duration = duration.map(Duration::from_secs_f64);
            }
         }

         let media_attachment_metadata = MediaAttachmentMetadata::Video {
            original_size,
            small_size: small.and_then(|s| image_size_from_api(s).ok()),
            length,
            fps,
            audio_encode,
            audio_bitrate,
            audio_channels,
         };

         Ok(media_attachment_metadata)
      },
      "gifv" => {
         use std::time::Duration;

         let ApiMediaAttachmentMetadata {
            original,
            small,
            focus: _,
            length,
            duration,
            fps,
            size: _,
            width,
            height,
            aspect: _,
            audio_encode: _,
            audio_bitrate: _,
            audio_channels: _,
         } = entity;

         let mut original_size = original.and_then(|s| video_size_from_api(s).ok());

         if let Some(ref mut original_size) = original_size {
            if original_size.width.is_none() {
               original_size.width = width;
            }

            if original_size.height.is_none() {
               original_size.height = height;
            }

            if original_size.duration.is_none() {
               original_size.duration = duration.map(Duration::from_secs_f64);
            }
         }

         let media_attachment_metadata = MediaAttachmentMetadata::Gifv {
            original_size,
            small_size: small.and_then(|s| image_size_from_api(s).ok()),
            length,
            fps,
         };

         Ok(media_attachment_metadata)
      },
      "audio" => {
         use std::time::Duration;

         let ApiMediaAttachmentMetadata {
            original,
            small: _,
            focus: _,
            length,
            duration,
            fps: _,
            size: _,
            width: _,
            height: _,
            aspect: _,
            audio_encode,
            audio_bitrate,
            audio_channels,
         } = entity;

         let mut original_size = original.and_then(|s| audio_size_from_api(s).ok());

         if let Some(ref mut original_size) = original_size {
            if original_size.duration.is_none() {
               original_size.duration = duration.map(Duration::from_secs_f64);
            }
         }

         let media_attachment_metadata = MediaAttachmentMetadata::Audio {
            original_size,
            length,
            audio_encode,
            audio_bitrate,
            audio_channels,
         };

         Ok(media_attachment_metadata)
      },
      _ => Err(anyhow::format_err!("Unknown media type: {media_type}"))
   }
}

pub fn image_size_from_api(
   entity: ApiMediaAttachmentSize
) -> anyhow::Result<ImageSize> {
   let ApiMediaAttachmentSize {
      width,
      height,
      size: _,
      aspect: _,
      frame_rate: _,
      duration: _,
      bitrate: _,
   } = entity;

   let media_attachment_size = ImageSize {
      width:  width .ok_or(anyhow::format_err!("No width")) ?,
      height: height.ok_or(anyhow::format_err!("No height"))?,
   };

   Ok(media_attachment_size)
}

pub fn video_size_from_api(
   entity: ApiMediaAttachmentSize
) -> anyhow::Result<VideoSize> {
   use std::time::Duration;

   let ApiMediaAttachmentSize {
      width,
      height,
      size: _,
      aspect: _,
      frame_rate,
      duration,
      bitrate,
   } = entity;

   let media_attachment_size = VideoSize {
      width,
      height,
      frame_rate,
      duration: duration.map(Duration::from_secs_f64),
      bitrate,
   };

   Ok(media_attachment_size)
}

pub fn audio_size_from_api(
   entity: ApiMediaAttachmentSize
) -> anyhow::Result<AudioSize> {
   use std::time::Duration;

   let ApiMediaAttachmentSize {
      width: _,
      height: _,
      size: _,
      aspect: _,
      frame_rate: _,
      duration,
      bitrate,
   } = entity;

   let media_attachment_size = AudioSize {
      duration: duration.map(Duration::from_secs_f64),
      bitrate,
   };

   Ok(media_attachment_size)
}

pub fn focus_from_api(
   entity: ApiMediaAttachmentFocus
) -> anyhow::Result<ImageFocus> {
   let ApiMediaAttachmentFocus { x, y } = entity;

   let media_attachment_focus = ImageFocus {
      x: x.ok_or(anyhow::format_err!("No x"))?,
      y: y.ok_or(anyhow::format_err!("No y"))?,
   };

   Ok(media_attachment_focus)
}
