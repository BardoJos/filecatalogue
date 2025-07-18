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
import com.farrusco.filecatalogue.common.ConstantsLocal
import com.farrusco.projectclasses.common.Constants
import com.farrusco.projectclasses.databases.tables.DBUtils
import com.farrusco.projectclasses.databases.tables.SqlBuilder
import com.farrusco.projectclasses.databases.tables.Tables
import com.farrusco.projectclasses.utils.ReturnValue
import java.util.*

@Suppress("EnumEntryName")
open class TableHelp : Tables(TABLE_NAME, Columns.entries.joinToString(",")) {

    companion object{
        const val TABLE_NAME = "POS_HELP"
    }
    enum class Columns { _id, language, header, headertrans, description }

    override fun insertPrimaryKey(values: ContentValues): ReturnValue {
        values.put(Columns.language.name, Locale.getDefault().language)
        return super.insertPrimaryKey(values)
    }

    fun getHelp(id: Int?): ReturnValue {
        val sb = SqlBuilder()
        Columns.entries.forEach {
            sb.column(this, it.name)
        }
        sb.from(this)

        var where = " ${Columns.language.name} = "
        val langExists=DBUtils.eLookUp("1", TABLE_NAME
            , "${Columns.language.name}='" + Locale.getDefault().language + "'")
        where += if (langExists == null){
            " 'en' and"
        } else {
            "'" + Locale.getDefault().language + "' and"
        }

        if (id != null) {
            where = " ${Columns._id.name} = $id and"
        }
        if (!ConstantsLocal.isOrderEnabled){
            where += " ${Columns.header.name} not like '%order%' and"
        }
        if (!Constants.isDebuggable){
            where += " ${Columns.header.name} not like '%manager%' and"
            where += " ${Columns.header.name} not like '%reorganize%' and"
        }
        if (where.isNotEmpty()){
            sb.where(where.substring(0,where.length-4) )
        }
        sb.orderBy(Columns.headertrans.toString())
        return rawQuery(sb.toString(), null)
    }
}