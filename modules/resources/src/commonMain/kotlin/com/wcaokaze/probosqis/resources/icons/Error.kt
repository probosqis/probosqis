/*
 * Copyright 2024 wcaokaze
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

package com.wcaokaze.probosqis.resources.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder

private fun PathBuilder.exclamation() {
   moveTo(11.0f, 15.0f)
   horizontalLineToRelative(2.0f)
   verticalLineToRelative(2.0f)
   horizontalLineToRelative(-2.0f)
   close()
   moveTo(11.0f, 7.0f)
   horizontalLineToRelative(2.0f)
   verticalLineToRelative(6.0f)
   horizontalLineToRelative(-2.0f)
   close()
}

private fun PathBuilder.outerCircle() {
   moveTo(12.0f, 2.0f)
   curveToRelative(-5.53f, 0.0f, -10.0f, 4.47f, -10.0f, 10.0f)
   reflectiveCurveToRelative(4.47f, 10.0f, 10.0f, 10.0f)
   reflectiveCurveToRelative(10.0f, -4.47f, 10.0f, -10.0f)
   reflectiveCurveToRelative(-4.47f, -10.0f, -10.0f, -10.0f)
   close()
}

private fun PathBuilder.innerCircle() {
   moveTo(12.0f, 4.0f)
   curveToRelative(4.42f, 0.0f, 8.0f, 3.58f, 8.0f, 8.0f)
   reflectiveCurveToRelative(-3.58f, 8.0f, -8.0f, 8.0f)
   reflectiveCurveToRelative(-8.0f, -3.58f, -8.0f, -8.0f)
   reflectiveCurveToRelative(3.58f, -8.0f, 8.0f, -8.0f)
   close()
}

@Suppress("UnusedReceiverParameter")
val Icons.Outlined.Error: ImageVector get() {
   if (_outlinedError != null) { return _outlinedError!! }

   _outlinedError = materialIcon("Outlined.Error") {
      materialPath {
         exclamation()
         outerCircle()
         innerCircle()
      }
   }

   return _outlinedError!!
}

private var _outlinedError: ImageVector? = null

@Suppress("UnusedReceiverParameter")
val Icons.Filled.Error: ImageVector get() {
   if (_filledError != null) { return _filledError!! }

   _filledError = materialIcon("Filled.Error") {
      materialPath {
         exclamation()
         outerCircle()
      }
   }

   return _filledError!!
}

private var _filledError: ImageVector? = null
