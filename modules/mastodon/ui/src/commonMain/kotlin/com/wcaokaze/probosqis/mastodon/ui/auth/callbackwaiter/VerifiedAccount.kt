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

package com.wcaokaze.probosqis.mastodon.ui.auth.callbackwaiter

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wcaokaze.probosqis.mastodon.entity.Account

@Composable
internal fun VerifiedAccount(
   verifiedAccount: Account,
   verifiedAccountIcon: ImageBitmap?,
   modifier: Modifier = Modifier
) {
   Box(
      modifier = modifier
         .clip(MaterialTheme.shapes.large)
         .background(MaterialTheme.colorScheme.secondaryContainer)
   ) {
      Row(
         verticalAlignment = Alignment.CenterVertically,
         modifier = Modifier.padding(8.dp)
      ) {
         if (verifiedAccountIcon != null) {
            Image(
               painter = BitmapPainter(verifiedAccountIcon),
               contentDescription = verifiedAccount.username,
               modifier = Modifier
                  .padding(8.dp)
                  .clip(MaterialTheme.shapes.small)
                  .size(56.dp)
                  .background(Color.Gray)
            )
         } else {
            Spacer(
               modifier = Modifier
                  .padding(8.dp)
                  .size(56.dp)
            )
         }

         Column {
            Text(
               verifiedAccount.displayName ?: verifiedAccount.username ?: "",
               fontSize = 18.sp,
               fontWeight = FontWeight.Bold,
               modifier = Modifier.padding(horizontal = 8.dp)
            )

            Text(
               verifiedAccount.username ?: "",
               fontSize = 15.sp,
               modifier = Modifier.padding(horizontal = 8.dp)
            )
         }
      }
   }
}
