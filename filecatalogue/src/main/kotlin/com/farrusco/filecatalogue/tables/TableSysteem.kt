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
package com.farrusco.filecatalogue.tables

import android.content.ContentValues
import android.text.TextUtils
import com.farrusco.filecatalogue.common.SystemRecordType
import com.farrusco.projectclasses.databases.tables.DBUtils
import com.farrusco.projectclasses.databases.tables.Tables
import com.farrusco.projectclasses.utils.ReturnValue
import com.farrusco.projectclasses.utils.ViewUtils

@Suppress("EnumEntryName")
open class TableSysteem: Tables(TABLE_NAME, Columns.entries.joinToString(",")) {
    private var id: String? = null
    companion object{
        const val TABLE_NAME = "POS_SYSTEEM"
    }

    enum class Columns { _id, _mainid, systeemkey, description, systeemvalue, recordtype, seqno, inputtype }

    fun getSysteem(id: Int? = null): ReturnValue {
        return getRow(id, null)
    }

    private fun getSysteemByKey(key: Int): ReturnValue {
        var sql = "SELECT " + TextUtils.join(",", dbColumns) + " FROM $TABLE_NAME"
        sql += " WHERE ${Columns.systeemkey.name} = $key"
        return rawQuery(sql, null)
    }

    fun getValue(key: Int): String {
        var value = ""
        val rtn = rawQuery(
            "SELECT ${Columns._id.name}, ${Columns.systeemvalue.name} FROM $TABLE_NAME where ${Columns.systeemkey.name} = $key",
            null
        )
        if (rtn.cursor.moveToFirst()) {
            id = rtn.cursor.getString(0)
            if (!rtn.cursor.getString(1).isNullOrEmpty()){
                value = rtn.cursor.getString(1)
            }
        }
        rtn.cursorClose()
        return value
    }

    fun deleteSysteemKey(systemKey: Int): ReturnValue{
        return super.delete("${Columns.systeemkey.name}=$systemKey")
    }

    fun setValue( key: Int, description: String?, value: String?, type: Int ): ReturnValue {
        val rtn = getSysteemByKey(key)
        val map = ContentValues()
        if (rtn.cursor.count == 0){
            rtn.cursorClose()
            val mId = DBUtils.getMaxId(TABLE_NAME)+1
            map.put(Columns._id.name, mId)
            map.put(Columns.recordtype.name, type)
            map.put(Columns.systeemkey.name, key)
            map.put(Columns.description.name, description)
            map.put(Columns.systeemvalue.name, value)
            return insertTableRow(map)
        }
        ViewUtils.copyCursorToContentValues(rtn.cursor, map)
        rtn.cursorClose()
        map.put(Columns.systeemvalue.name, value)
        return updatePrimaryKey(map)
    }

    fun getUserOptions(systemKey: Int): ReturnValue {
        val sql = "SELECT $TABLE_NAME." + TextUtils.join(",$TABLE_NAME.", dbColumns) +
                " , maintable.systeemkey as main_systeemkey" +
                " FROM  $TABLE_NAME" +
                " JOIN  $TABLE_NAME as maintable on maintable._id = $TABLE_NAME._mainid" +
                " where $TABLE_NAME.${Columns.recordtype.name} = ${SystemRecordType.Available.internal}" +
                " and  maintable.${Columns.systeemkey.name} = $systemKey" +
                " order by maintable.seqno, $TABLE_NAME.${Columns.seqno.name}"
        return rawQuery(sql, null)
    }

    fun updateSysteemKey(systemKey: Int, systeemValue: String): ReturnValue {
        val id = DBUtils.eLookUp(
            Columns._id.name, TABLE_NAME,
            Columns.systeemkey.name + "=$systemKey").toString().toInt()
        return updatePrimaryKey(mapData(id, systeemValue))
    }

    private fun mapData(id: Int, systemValue: String): ContentValues {
        val map = ContentValues()
        map.put(Columns._id.name, id)
        map.put(Columns.systeemvalue.name, systemValue)
        return map
    }
}