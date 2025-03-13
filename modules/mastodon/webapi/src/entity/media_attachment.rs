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

/// since mastodon 0.6.0
#[derive(Deserialize)]
pub struct MediaAttachment {
   /// since mastodon 0.6.0
   pub id: Option<String>,
   /// since mastodon 0.6.0
   /// mastodon 2.9.1 "audio"追加
   #[serde(rename = "type")]
   pub r#type: Option<String>,
   /// since mastodon 0.6.0
   pub url: Option<String>,
   /// since mastodon 0.6.0
   pub preview_url: Option<String>,
   /// since mastodon 0.6.0
   pub remote_url: Option<String>,
   /// since mastodon 1.5.0
   pub meta: Option<MediaAttachmentMetadata>,
   /// since mastodon 2.0.0
   pub description: Option<String>,
   /// since mastodon 2.8.1
   pub blurhash: Option<String>,
   /// since mastodon 0.6.0
   /// mastodon 3.5.0 削除
   pub text_url: Option<String>,
}

/// since mastodon 1.5.0
#[derive(Deserialize)]
pub struct MediaAttachmentMetadata {
   /// MediaAttachment::type == "image", "video", "gifv"の場合のみ
   pub original: Option<MediaAttachmentSize>,
   /// MediaAttachment::type == "image", "video", "gifv"の場合のみ
   pub small: Option<MediaAttachmentSize>,
   /// since mastodon 2.3.0
   /// MediaAttachment::type == "image"の場合のみ
   pub focus: Option<MediaAttachmentFocus>,
   /// MediaAttachment::type == "video", "gifv", "audio"の場合のみ
   pub length: Option<String>,
   /// MediaAttachment::type == "video", "gifv", "audio"の場合のみ
   pub duration: Option<f64>,
   /// MediaAttachment::type == "video", "gifv"の場合のみ
   pub fps: Option<i64>,
   /// MediaAttachment::type == "video", "gifv"の場合のみ
   pub size: Option<String>,
   /// MediaAttachment::type == "video", "gifv"の場合のみ
   pub width: Option<i64>,
   /// MediaAttachment::type == "video", "gifv"の場合のみ
   pub height: Option<i64>,
   /// MediaAttachment::type == "video", "gifv"の場合のみ
   pub aspect: Option<f64>,
   /// MediaAttachment::type == "video", "audio"の場合のみ
   pub audio_encode: Option<String>,
   /// MediaAttachment::type == "video", "audio"の場合のみ
   pub audio_bitrate: Option<String>,
   /// MediaAttachment::type == "video", "audio"の場合のみ
   pub audio_channels: Option<String>,
}

/// since mastodon 1.5.0
#[derive(Deserialize)]
pub struct MediaAttachmentSize {
   /// MediaAttachment::type == "image", "video", "gifv"の場合のみ
   pub width: Option<i64>,
   /// MediaAttachment::type == "image", "video", "gifv"の場合のみ
   pub height: Option<i64>,
   /// MediaAttachment::type == "image", "video", "gifv"の場合のみ
   pub size: Option<String>,
   /// MediaAttachment::type == "image", "video", "gifv"の場合のみ
   pub aspect: Option<f64>,
   /// MediaAttachment::type == "video", "gifv"の場合のみ
   pub frame_rate: Option<String>,
   /// MediaAttachment::type == "video", "gifv", "audio"の場合のみ
   pub duration: Option<f64>,
   /// MediaAttachment::type == "video", "gifv", "audio"の場合のみ
   pub bitrate: Option<i64>,
}

#[derive(Deserialize)]
pub struct MediaAttachmentFocus {
   pub x: Option<f64>,
   pub y: Option<f64>,
}
