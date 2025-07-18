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
package com.farrusco.filecatalogue.common

open class ObjectFile {

    open fun add(id: Int, filename: String, title: String, categoryid: Int, rotation: Int) {
        arrObj.add(arrayOf<Any>(id, filename, title, categoryid, rotation))
    }

    open fun getTitle(pos: Int): String {
        val arrLoc = arrObj[pos] as Array<*>
        return arrLoc[2] as String
    }

    open fun getSize(): Int {
        return arrObj.size
    }

    open fun getFilename(pos: Int): String {
        val arrLoc = arrObj[pos] as Array<*>
        return arrLoc[1] as String
    }
    open fun getRotation(pos: Int): Int {
        val arrLoc = arrObj[pos] as Array<*>
        return arrLoc[4] as Int
    }

    open fun getId(pos: Int): Int {
        val arrLoc = arrObj[pos] as Array<*>
        return arrLoc[0] as Int
    }

    companion object {
        var arrObj: ArrayList<Any> = ArrayList()
        fun initArray() {
            arrObj = ArrayList()
        }
        fun add(id: Int, filename: String, title: String, categoryid: Int, rotation: Int) {
            arrObj.add(arrayOf<Any>(id, filename, title, categoryid, rotation))
        }
    }
}