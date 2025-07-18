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

import java.io.File
import java.util.*

class DialogProperties {
    var selectionMode: Int = DialogConfigs.SINGLE_MODE
    var selectionType: Int = DialogConfigs.FILE_SELECT
    var dir = DialogConfigs.STORAGE_STORAGE
    var root: File = File(dir)
    var offset: File = File(dir)
    var errorDir: File = File(dir)
    var extensions: Array<String> = arrayOf("*")
    var title: String = ""
    private var markFiles: List<String?>? = null

    var markfiles: List<String?>?
        get() = markFiles
        set(value) {
            markFiles = value
            markFiles()
        }

    // var listItem: ArrayList<FileListItem> = ArrayList<FileListItem>()

    private fun markFiles() {
        if (markfiles != null && markfiles!!.isNotEmpty()) {
            if (selectionMode == DialogConfigs.SINGLE_MODE) {
                val temp = File(markfiles!![0]!!)
                when (selectionType) {
                    DialogConfigs.DIR_SELECT -> if (temp.exists() && temp.isDirectory) {
                        val item = FileListItem()
                        item.filename=temp.name
                        item.isDirectory=temp.isDirectory
                        item.isMarked=true
                        item.time=temp.lastModified()
                        item.location=temp.absolutePath
                        MarkedItemList.addSelectedItem(item)
                    }
                    DialogConfigs.FILE_SELECT -> if (temp.exists() && temp.isFile) {
                        val item = FileListItem()
                        item.filename=temp.name
                        item.isDirectory=temp.isDirectory
                        item.isMarked=true
                        item.time=temp.lastModified()
                        item.location=temp.absolutePath
                        MarkedItemList.addSelectedItem(item)
                    }
                    DialogConfigs.FILE_AND_DIR_SELECT -> if (temp.exists()) {
                        val item = FileListItem()
                        item.filename=temp.name
                        item.isDirectory=temp.isDirectory
                        item.isMarked=true
                        item.time=temp.lastModified()
                        item.location=temp.absolutePath
                        MarkedItemList.addSelectedItem(item)
                    }
                }
            } else {
                for (path in markfiles!!) {
                    when (selectionType) {
                        DialogConfigs.DIR_SELECT -> {
                            val temp = File(path!!)
                            if (temp.exists() && temp.isDirectory) {
                                val item = FileListItem()
                                item.filename=temp.name
                                item.isDirectory=temp.isDirectory
                                item.isMarked=true
                                item.time=temp.lastModified()
                                item.location=temp.absolutePath
                                MarkedItemList.addSelectedItem(item)
                            }
                        }
                        DialogConfigs.FILE_SELECT -> {
                            val temp = File(path!!)
                            if (temp.exists() && temp.isFile) {
                                val item = FileListItem()
                                item.filename=temp.name
                                item.isDirectory=temp.isDirectory
                                item.isMarked=true
                                item.time=temp.lastModified()
                                item.location=temp.absolutePath
                                MarkedItemList.addSelectedItem(item)
                            }
                        }
                        DialogConfigs.FILE_AND_DIR_SELECT -> {
                            val temp = File(path!!)
                            if (temp.exists() && (temp.isFile || temp.isDirectory)) {
                                val item = FileListItem()
                                item.filename=temp.name
                                item.isDirectory=temp.isDirectory
                                item.isMarked=true
                                item.time=temp.lastModified()
                                item.location=temp.absolutePath
                                MarkedItemList.addSelectedItem(item)
                            }
                        }
                    }
                }
            }
        }
    }
}

class FileListItem : Comparable<FileListItem> {
    var filename: String? = null
    var directory: String? = null
    var location: String? = null
    var isDirectory = false
    var isMarked = false
    var time: Long = 0
    // public operator fun compareTo(other: T): Int
    override operator fun compareTo(other: FileListItem): Int {
        return if (other.isDirectory && isDirectory) {
            filename!!.lowercase(Locale.getDefault())
                .compareTo(other.filename!!.lowercase(Locale.getDefault()))
        } else if (!other.isDirectory && !isDirectory) {
            //If the comparison is not between two directories, return the file with
            //alphabetic order first.
            filename!!.lowercase(Locale.getDefault())
                .compareTo(other.filename!!.lowercase(Locale.getDefault()))
        } else if (other.isDirectory) {   //If the comparison is between a directory and a file, return the directory.
            1
        } else {
            -1
        }
    }
}