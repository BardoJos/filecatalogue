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
package com.farrusco.projectclasses.filepicker.model

object DialogConfigs {
    const val SINGLE_MODE = 0
    const val MULTI_MODE = 1
    const val FILE_SELECT = 0
    const val DIR_SELECT = 1
    const val FILE_AND_DIR_SELECT = 2

    const val STORAGE_STORAGE = "/storage"
    //var STORAGE_SDCARD = Environment.getExternalStorageDirectory().getPath()
    const val  STORAGE_SDCARD = "/sdcard"
}