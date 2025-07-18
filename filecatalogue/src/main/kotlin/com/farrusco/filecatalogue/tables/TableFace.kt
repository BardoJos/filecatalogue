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
import com.farrusco.projectclasses.databases.tables.Tables
import com.farrusco.projectclasses.utils.ReturnValue

@Suppress("EnumEntryName")
open class TableFace() : Tables(TABLE_NAME, Columns.entries.joinToString(",")) {

    companion object{
        const val TABLE_NAME = "POS_FACE"
    }
    enum class Columns { _id,_categoryid,_productid,seqno,spoof,distance, extra }

    override fun insertPrimaryKey(values: ContentValues): ReturnValue{
        if (values.getAsInteger(Columns._productid.name) < 1 ||
            values.getAsInteger(Columns.distance.name) < 0){
            throw RuntimeException("$TABLE_NAME: product or distance are zero")
        }
        return super.updatePrimaryKey(values)
    }
    fun resetDistance(){
        val map = ContentValues()
        map.put(Columns.distance.name, 0)
        map.put(Columns.seqno.name, 0)
        super.updateWhere("${Columns.distance.name} != 0 or ${Columns.seqno.name} > 0",map)
    }
}