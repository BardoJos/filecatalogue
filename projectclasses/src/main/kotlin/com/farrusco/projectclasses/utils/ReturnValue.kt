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
package com.farrusco.projectclasses.utils

import android.content.ContentValues
import android.database.Cursor

class ReturnValue {
    var returnValue: Boolean = true
    var showMessage: Boolean = true
    var messnr = 0
    var mess = ""
    var id: Int = -1
    var rowsaffected = 0
    var arrParams = ArrayList<String>()
    lateinit var cursor: Cursor
    var map = ContentValues()

    fun setShowmessage(showmessage: Boolean): ReturnValue {
        showMessage = showmessage
        return this
    }

    fun setId(id: Int): ReturnValue {
        this.id = id
        if (id < 0) returnValue = false
        return this
    }

    fun cursorClose(): ReturnValue {
        try {
            cursor.close()
        } catch (_: Exception) {
            // lazy
        }
        return this
    }

    fun setReturnvalue(returnvalue: Boolean): ReturnValue {
        this.returnValue = returnvalue
        return this
    }

    fun setMessnr(messnr: Int): ReturnValue {
        this.messnr = messnr
        return this
    }

    fun setAddParams(arrParams: String): ReturnValue {
        this.arrParams.add(arrParams)
        return this
    }

    fun setArrParams(arrParams: ArrayList<String>): ReturnValue {
        this.arrParams = arrParams
        return this
    }

    fun cursorToMap(): ContentValues{
        map.clear()
        try {
            for (i in 0 until cursor.columnCount) {
                map.put(cursor.getColumnName(i),cursor.getString(i))
            }
        } catch (_: Exception){
           // lazy
        }
        return map
    }
/*
    fun getColumnValueString(columnName: String): String?{
        val idx = cursor.getColumnIndex(columnName)
        if (idx >= 0){
            return cursor.getStringOrNull(idx)
        }
        return null
    }*/
}