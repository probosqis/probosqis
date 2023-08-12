/*
 * Copyright 2023 wcaokaze
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

package com.wcaokaze.probosqis.ext.compose.layout

import androidx.compose.ui.tooling.preview.Preview

@Preview(name = "portrait", widthDp = 450, heightDp = 800)
@Preview(name = "landscape", widthDp = 800, heightDp = 450)
@Preview(name = "narrow", widthDp = 250, heightDp = 500)
@Preview(name = "tablet", widthDp = 1200, heightDp = 800)
annotation class MultiDevicePreview
