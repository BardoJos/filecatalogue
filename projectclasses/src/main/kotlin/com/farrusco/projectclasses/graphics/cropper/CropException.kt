/*
 * Copyright (c) 2022. farrusco (jos dot farrusco at gmail dot com)
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
package com.farrusco.projectclasses.graphics.cropper

import android.net.Uri

sealed class CropException(message: String) : Exception(message) {
/*  class Cancellation : CropException("$EXCEPTION_PREFIX cropping has been cancelled by the user") {
    internal companion object {
      private const val serialVersionUID: Long = -6896269134508601990L
    }
  }*/

  class FailedToLoadBitmap(uri: Uri, message: String?) : CropException("$EXCEPTION_PREFIX Failed to load sampled bitmap: $uri\r\n$message") {
    internal companion object {
      private const val serialVersionUID: Long = 7791142932960927332L
    }
  }

  class FailedToDecodeImage(uri: Uri) : CropException("$EXCEPTION_PREFIX Failed to decode image: $uri") {
    internal companion object {
      private const val serialVersionUID: Long = 3516154387706407275L
    }
  }

  internal companion object {
    private const val serialVersionUID: Long = 4933890872862969613L
    const val EXCEPTION_PREFIX = "crop:"
  }
}
