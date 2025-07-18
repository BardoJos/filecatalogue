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
package com.farrusco.projectclasses.filepicker.utils

import com.farrusco.projectclasses.filepicker.model.DialogConfigs
import com.farrusco.projectclasses.filepicker.model.DialogProperties
import java.io.File
import java.io.FileFilter
import java.util.*

class ExtensionFilter(private val properties: DialogProperties) : FileFilter {
    private val validExtensions: Array<String>? = properties.extensions

    /**
     * Function to filter files based on defined rules.
     */
    override fun accept(file: File): Boolean {
        //All directories are added in the least that can be read by the Application
        if (file.isDirectory && file.canRead()) {
            return true
        } else if (properties.selectionType == DialogConfigs.DIR_SELECT) {
            return false
        } else {
            val name = file.name.lowercase(Locale.getDefault())
            for (ext in validExtensions!!) {
                if (name.endsWith(ext)) {
                    return true
                }
            }
        }
        return false
    }
}