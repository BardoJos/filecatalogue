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

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteException
import android.widget.Toast
import androidx.collection.ArrayMap
import androidx.core.text.trimmedLength
import com.farrusco.projectclasses.R
import com.farrusco.projectclasses.common.Constants
import com.farrusco.projectclasses.messages.ToastExt
import com.farrusco.projectclasses.utils.CalcObjects
import com.farrusco.projectclasses.utils.Logging
import com.farrusco.projectclasses.utils.ReturnValue
import com.farrusco.projectclasses.utils.StringUtils
import net.sqlcipher.database.SQLiteDatabase
import java.io.File

object DBUtils {

    fun getMaxId( table: String): Int{
        val max = eLookUp("max(_id)", table,null) ?: return 0
        return max.toString().toInt()
    }

    fun eLookUp(column: String,table: String, where: Any?): Any? {
        Logging.d(
                StringUtils.repeat("-", 40)
                        + "\nELookup Table:" + table + "\nColumn:" + column
                        + "\nWhere: $where limit 1"
            )
        var rtn: String? = null
        val cursor = Constants.db.rawQuery(
            "SELECT "
                    + column
                    + " FROM "
                    + table
                    + if (where == null) "" else " WHERE $where limit 1", null
        )
        if (cursor.moveToFirst()) {
            rtn = cursor.getString(0)
        }
       Logging.d(
                """Cursor count: ${cursor.count} Return: $rtn
                ${StringUtils.repeat("-", 40)}"""
            )
        cursor.close()
        return rtn
    }
    fun eLookUpInt(column: String,table: String, where: Any?): Int {
        return CalcObjects.objectToInteger(eLookUp(column,table, where))
    }

    fun getDBStructure(): ArrayMap<String, DBStructure> {
        // get length of all columns

        val map = ArrayMap<String, DBStructure>()
        val sqlMain = "select group_concat(\"select '\" || name || \"' as table_name, * from pragma_table_info('\" || name || \"')\", ' where type like \"%(%)\" union ') || ' ' from sqlite_master where type = 'table';"
        // table, cid, name, type, notnull, default, pk
        // if pk = 1 then field is pk and cannot be more than 1 otherwise it is an index

        val cursor = Constants.db.rawQuery(sqlMain, null)
        if (cursor.moveToFirst()) {
            val sql = cursor.getString(0)
            val cursor2 = Constants.db.rawQuery(sql, null)
            if (cursor2.moveToFirst()) {
                do{
                    val db = DBStructure()
                    db.tableName = cursor2.getString(0).lowercase()
                    db.columnName = cursor2.getString(2).lowercase()
                    db.default = cursor2.getString(5)
                    db.pk = cursor2.getInt(6)
                    db.notnull = cursor2.getInt(4)
                    db.type = cursor2.getString(3)
                    if (db.type.contains("VARCHAR",true)){
                        val pos1 = db.type.indexOf("(")
                        val pos2 = db.type.indexOf(")")
                        val len = db.type.substring(pos1+1,pos2)
                        db.len = len.toInt()
                        map["${db.tableName}.${db.columnName}"] = db
                    }
                } while (cursor2.moveToNext())
                cursor2.close()
            }
        }
        cursor.close()
        Constants.dbStructure = map
        return map
    }

/*    fun sqlDateFormat(column: String, display format: String): String {
        var fmt = display format.replace("dd","d",true).replace("d","%d",true)
        fmt = fmt.replace("mm","m",true).replace("m","%m",true)
        fmt = fmt.replace("yyyy","Y",true).replace("Y","%Y",true)

        // var sql = "STRF TIME('%d-%m-%Y', $column)"
        return "STRF TIME('$fmt', $column)"
    }*/

    fun trimColumnValue(map: ContentValues, key: String){
        if (map.containsKey(key)) {
            val trimString = map.getAsString(key).toString()
            if (trimString.length != trimString.trimmedLength()) {
                map.remove(key)
                map.put(key, trimString.trim())
            }
        }
    }

    fun getQualifiedColumn(table: String, column: String?): String? {
        return getQualifiedColumn(table, column, "")
    }

    fun getQualifiedColumn(table: Tables, column: String?): String? {
        return getQualifiedColumn(table.tableName, column, "")
    }

    fun getColumnAlias(table: Tables, column: String): String {
        return table.tableName + "_" + column
    }

    @Suppress("SameParameterValue")
    private fun getQualifiedColumn(
        table: String, column: String?,
        alias: String
    ): String? {
        var locColumn: String? = ""
        if (table != "") locColumn += "$table."
        locColumn += column
        if (alias != "") locColumn += " AS $alias"
        return locColumn
    }

    fun isDatabaseOpen(): ReturnValue {
        val rtn = ReturnValue()
        if (Constants.isDbInitialized() && Constants.db.isOpen) {
            rtn.mess = "DBOpenLoader: database is open"
            Logging.d(rtn.mess)
        } else{
            rtn.returnValue = false
            rtn.mess = "DBOpenLoader: database is closed"
        }
        return rtn
    }
    fun openOrCreateDatabase(context: Context): ReturnValue {
        val rtn = ReturnValue()
        if (Constants.isDbInitialized() && Constants.db.isOpen){
            rtn.mess="DBOpenLoader: database is open"
            Logging.d(rtn.mess )
            return rtn
        }
        rtn.returnValue = false
        val dbFile: File = context.getDatabasePath(Constants.DATABASE_NAME)

        // do not forget this for correct use of cipher
        SQLiteDatabase.loadLibs(context)

        try {
            Constants.db = SQLiteDatabase.openOrCreateDatabase(
                dbFile, Constants.DATABASE_PW,
                null
            )

            rtn.returnValue = true
        } catch (e: SQLiteException) {
            rtn.returnValue = false
            ToastExt().makeText(
                context,
                context.getText(R.string.mess001_dberror).toString(),
                Toast.LENGTH_LONG
            ).show()
            Logging.d(
                "SQLiteDatabase.openOrCreateDatabase() of encrypted database with a password did throw a SQLiteException as expected OK\n$e"
            )
        } catch (e: Exception) {
            rtn.returnValue = false
            ToastExt().makeText(
                context,
                context.getText(R.string.mess001_dberror).toString(),
                Toast.LENGTH_LONG
            ).show()
            Logging.d(
                "openDatabase: NOT EXPECTED with invalid password did throw an unexpected exception",
                e.stackTraceToString()
            )
        }
        return rtn
    }

}
class DBStructure{
    var tableName: String =""
    var columnName: String =""
    var type: String =""
    var default: String? = null
    var len: Int=0
    var notnull: Int=0
    var pk: Int=0
}