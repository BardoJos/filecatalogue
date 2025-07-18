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
@file:Suppress("unused")

package com.farrusco.projectclasses.databases.tables

import android.database.Cursor
import androidx.core.database.getDoubleOrNull
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull

object CursorX  {

    fun Cursor.getColumnValueBoolean(columnName: String): Boolean {
        val idx = this.getColumnIndex(columnName)
        if (idx >= 0) {
            return this.getInt(idx) != 0
        }
        return false
    }

    fun Cursor.getColumnValueString(columnName: String, dflt: String? = null): String?{
        val idx = this.getColumnIndex(columnName)
        if (idx >= 0){
            return this.getStringOrNull(idx) ?: return dflt
        }
        return dflt
    }

    fun Cursor.getColumnValueInt(columnName: String): Int?{
        val idx = this.getColumnIndex(columnName)
        if (idx >= 0){
            return this.getIntOrNull(idx)
        }
        return null
    }

    fun Cursor.getColumnValueInt(columnName: String, dflt: Int): Int{
        val idx = this.getColumnIndex(columnName)
        if (idx >= 0){
            return try {
                this.getInt(idx)
            } catch (_: Exception){
                dflt
            }
        }
        return dflt
    }

    fun Cursor.getColumnValueDouble(columnName: String): Double?{
        val idx = this.getColumnIndex(columnName)
        if (idx >= 0){
            return this.getDoubleOrNull(idx)
        }
        return null
    }
    fun Cursor.getColumnValueFloat(columnName: String): Float{
        val idx = this.getColumnIndex(columnName)
        if (idx >= 0){
            return try {
                this.getFloat(idx)
            } catch (_: Exception){
                0f
            }
        }
        return 0f
    }
}