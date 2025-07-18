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
package com.farrusco.filecatalogue.business

import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.util.SparseArray
import androidx.collection.ArrayMap
import androidx.core.util.containsValue
import com.farrusco.filecatalogue.R
import com.farrusco.filecatalogue.tables.TableHelp
import com.farrusco.projectclasses.common.Constants
import com.farrusco.projectclasses.databases.tables.DBUtils
import java.util.Locale
import androidx.core.util.size

class Help : TableHelp() {

    fun createActivities(context: Context) {
        val cat = context.getText(R.string.packagename).toString()
        val pm = context.packageManager

        val mapTrans = ArrayMap<String, String>()
        pm.getPackageInfo(cat, PackageManager.GET_ACTIVITIES).activities.filter {it.name.startsWith(cat) }.forEach {
            if (it.labelRes > 0 ){
                val key = it.name.substringAfterLast(".").lowercase()
                if (!mapTrans.containsKey(key)){
                    mapTrans[key] = context.getString(it.labelRes)
                }
            }
        }
        var totalList = ""
        mapTrans.forEach { (t, _) -> totalList += "'$t'," }
        totalList = totalList.substring(0,totalList.length-1)

        val arrTemp: SparseArray<String> = SparseArray<String>()

        val rtn = rawQuery("select ${Columns._id.name},${Columns.header.name},${Columns.headertrans.name} " +
                " from $tableName " +
                " where lower(${Columns.header.name}) in ($totalList)",null)
        if (rtn.cursor.moveToFirst()) {
            do {
                val header = getString(rtn.cursor,Columns.header.name).lowercase()
                val headerTrans = getString(rtn.cursor,Columns.headertrans.name)
                if (mapTrans.containsKey(header)){
                    if (mapTrans.getValue(header) != headerTrans){
                        delete(Columns._id.name + "=" + rtn.cursor.getInt(0))
                    } else if (arrTemp.containsValue(header)){
                        // delete duplicates
                        delete(Columns._id.name + "=" + rtn.cursor.getInt(0))
                    } else {
                        arrTemp.append(rtn.cursor.getInt(0), getString(rtn.cursor, Columns.header.name))
                    }
                } else {
                    delete(Columns._id.name + "=" + rtn.cursor.getInt(0))
                }
            } while (rtn.cursor.moveToNext())
        }
        rtn.cursorClose()

        mapTrans.forEach { (t, u) ->
            var bFound = false
            for (i1 in 0 until arrTemp.size){
                if (arrTemp.valueAt(i1)==t){
                    bFound = true
                    break
                }
            }
            if (!bFound) {
                val map = ContentValues()
                map.put(Columns._id.name,0)
                map.put(Columns.language.name,Locale.getDefault().language)
                map.put(Columns.header.name,t)
                map.put(Columns.headertrans.name,u)
                map.put(Columns.description.name,u)
                this.insertPrimaryKey(map)
            }
        }
    }

    fun helpText(header: String): String {
        val help = DBUtils.eLookUp(
            Columns.description.name,
            tableName, "${Columns.header.name}='${header.lowercase()}'")
        if (help == null) {
            if (header.isEmpty()) return ""
            return "Help: $header"
        }
        return help.toString()
    }

    fun getHelpTitle(header: String): String {
        if (!Constants.isHelpEnabled) return ""
        val language =  Locale.getDefault().language
        var rtn = DBUtils.eLookUp("description", Constants.helpTableName,
            "${Columns.language.name} = '$language' and header='${header.lowercase()}'"
        )
        if (rtn == null) {
            rtn = DBUtils.eLookUp("description", Constants.helpTableName,
                "${Columns.language.name} = 'en' and header='${header.lowercase()}'"
            )
            if (rtn == null) return ""
        }
        return rtn.toString()
    }
}