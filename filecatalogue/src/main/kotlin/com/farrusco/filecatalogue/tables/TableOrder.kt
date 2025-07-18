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

import android.content.Context
import com.farrusco.projectclasses.databases.tables.CursorX.getColumnValueInt
import com.farrusco.projectclasses.databases.tables.Tables
import com.farrusco.projectclasses.utils.ReturnValue

@Suppress("EnumEntryName")
open class TableOrder(val context: Context) : Tables(TABLE_NAME, Columns.entries.joinToString(",")) {

    companion object{
        const val TABLE_NAME = "POS_ORDER"
    }
    enum class Columns { _id, orderdate, deliverdate, buyername
    , address, email, description, discount }

    override fun delete(whereClause: String): ReturnValue {
        val sql =
            "SELECT ${Columns._id} FROM $TABLE_NAME where $whereClause"
        val rtn = rawQuery(sql, null)
        val tblOrderLine = TableOrderLine()
        if (rtn.cursor.moveToFirst()) {
            do{
                val id = rtn.cursor.getColumnValueInt(Columns._id.name)
                tblOrderLine.deleteOrderLineTotal(id)
            } while (rtn.cursor.moveToNext())
        }
        rtn.cursorClose()
        return super.delete(whereClause)
    }
}