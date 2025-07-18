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
package com.farrusco.projectclasses.databases.tables

import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteConstraintException
import android.text.TextUtils
import android.util.SparseArray
import androidx.core.database.getStringOrNull
import com.farrusco.projectclasses.BuildConfig
import com.farrusco.projectclasses.R
import com.farrusco.projectclasses.common.Constants
import com.farrusco.projectclasses.databases.tables.CursorX.getColumnValueString
import com.farrusco.projectclasses.utils.Logging
import com.farrusco.projectclasses.utils.ReturnValue
import com.farrusco.projectclasses.utils.StringUtils
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.core.util.size
import com.farrusco.projectclasses.databases.tables.CursorX.getColumnValueDouble

@Suppress("unused")
abstract class Tables(tableName: String, usrColumns: String) {
    private val lenLine = 40
    private val bOutputSelect: Boolean = Constants.isDebuggable

    var tableName: String
        private set
    lateinit var dbColumns: List<String>
        private set
    lateinit var primaryKeys: Array<String>
        private set
    var dbTableColumns = ""

    var dbDateCreated = ""
    var dbDateLastModified = ""
    //inline fun <reified T : Enum<T>> getNames() = enumValues<T>().map { it.name }

    init {
        this.tableName = tableName
        getDbColumns()
        val arrCol = usrColumns.split(",")
        checkDbColumns(arrCol)

        var dbTableColumns = ""
        arrCol.forEach {
            dbTableColumns += "$tableName.$it,"
        }
        dbTableColumns.substring(0,dbTableColumns.length-1)
    }

    private fun checkDbColumns(dbColumns2: List<String>){
        if (dbColumns2.size != dbColumns.size){
            Logging.e("checkDbColumns) Mismatch count of columns: Defined-${dbColumns2.size}, DB-${dbColumns.size}")
        }
        dbColumns.forEach { itDb ->
            if ( dbColumns2.find { it.equals(itDb,true)  }.isNullOrEmpty()){
                Logging.e("checkDbColumns) Mismatch dbColumns: $tableName.$itDb")
            }
        }
        dbColumns2.forEach { itDb ->
            if ( dbColumns.find { it.equals(itDb,true)  }.isNullOrEmpty()){
                Logging.e("checkDbColumns) Mismatch defined columns: $tableName.$itDb")
            }
        }
    }
    private fun getDbColumns(){
        var sql = "select group_concat(name) as names from pragma_table_info('$tableName' ) where pk = 1"
        var rtn = rawQuery(sql, null)
        if (rtn.cursor.moveToFirst()) {
            primaryKeys = rtn.cursor.getColumnValueString("names")!!.lowercase().split(",").toTypedArray()
        }
        sql = "select group_concat(name) as names from pragma_table_info('$tableName' )"
        rtn = rawQuery(sql, null)
        if (rtn.cursor.moveToFirst()) {
            dbColumns = rtn.cursor.getColumnValueString("names")!!.lowercase().split(",")
        }
        rtn.cursorClose()
    }

    private val rowCount: Long get() {
        val sql = "select count(1) as cnt from $tableName"
        val rtn = rawQuery(sql, null)
        var cnt = 0L
        if (rtn.cursor.moveToFirst()) {
            cnt = rtn.cursor.getColumnValueDouble("cnt")!!.toLong()
        }
        rtn.cursorClose()
        return cnt
    }

    private fun filterColumns(contentValues: ContentValues): ContentValues{
        val map = ContentValues()
        dbColumns.forEach {
            if (contentValues.containsKey(it)){
                if (contentValues.get(it) != null){
                    map.put(it,contentValues.get(it).toString())
                }
            }
        }
        return map
    }

    fun getColumnLength(column: String): Int {
        val rtn = Constants.dbStructure.getValue("$tableName.$column")
        return rtn?.len ?: 0
    }

    open fun updatePrimaryKey(map: ContentValues): ReturnValue {
        val values = filterColumns(map)
        var where = ""
        primaryKeys.forEach {
            if (values.containsKey(it)) {
                where += "$it = ${values.getAsInteger(it)} and"
                values.remove(it)
            }
        }
        if (where == "") {
            Logging.d("updatePrimaryKey","No primary key(s))")
            return ReturnValue().setReturnvalue(false).setMessnr(R.string.mess032_notsaved)
        }

        return update( values, where.substring(0,where.length-4), null)
    }

    open fun updateWhere(where: String, map: ContentValues): ReturnValue {
        return update( filterColumns(map), where, null)
    }

    open fun deletePrimaryKey(values: ContentValues): ReturnValue {
        var where = ""
        primaryKeys.forEach {
            if (values.containsKey(it)) {
                where += "$it = ${values.getAsInteger(it)} and"
            }
        }
        if (where == "") {
            Logging.d("deletePrimaryKey","No primary key(s))")
            return ReturnValue().setReturnvalue(false).setMessnr(R.string.mess032_notsaved)
        }
        return delete( where.substring(0,where.length-4) )
    }

    private fun removeNulls(values: ContentValues): ContentValues {
        val arrList: SparseArray<String> = SparseArray<String>()
        var i = 0
        for (name in values.keySet()) {
            if (values.get(name) == null) {
                arrList.append(i++, name)
            }
        }
        val m: Int = arrList.size
        for (x in 0 until m) {
            values.remove(arrList.get(x))
        }
        return values
    }

    fun selectWhere(where: String): ReturnValue {
        var whereTmp = where
        if (whereTmp != "") {
            whereTmp = " WHERE $where"
        }
        return rawQuery( "SELECT " + TextUtils.join(", ",dbColumns)
                    + " FROM $tableName $whereTmp", null)
    }
    open fun selectContentValues(values: ContentValues): ReturnValue {
        var where = ""
        primaryKeys.forEach {
            if (values.containsKey(it)) {
                where += "$it = ${values.getAsInteger(it)} and"
            }
        }
        if (where != "") {
            where = " WHERE ${where.substring(0,where.length-4)}"
        }
        var orderby = ""
        if (values.containsKey("orderby")){
            orderby = " ORDER BY " + values.getAsString("orderby")
        }
        return  rawQuery(
            "SELECT " + TextUtils.join(", ",dbColumns)
                    + " FROM $tableName $where $orderby", null)

    }

    open fun insertPrimaryKey(values: ContentValues): ReturnValue {
        val values1 = filterColumns(values)
        primaryKeys.forEach {
            if (!values1.containsKey(it) ||
                values1.get(it) == null ||
                values1.get(it) == "" ||
                values1.getAsInteger(it) <= 0 ) {
                val tmp = DBUtils.eLookUp("max($it)", tableName, null)
                val max = if (tmp == null) 1 else (tmp.toString().toInt() + 1)
                values1.put(it,max)
            }
        }
        return insertTableRow(values1)
    }

    private fun addDatestamp(bInsert: Boolean, values: ContentValues){
        if (bInsert){
            if (dbDateCreated.isNotEmpty()){
                if (values.containsKey(dbDateCreated)){
                    if (values.get(dbDateCreated) == null || values.get(dbDateCreated) == ""){
                        values.remove(dbDateCreated)
                    }
                }
                values.put(
                    dbDateCreated, LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd")))

            }
            if (dbDateLastModified.isNotEmpty() && values.containsKey(dbDateLastModified)){
                values.remove(dbDateLastModified)
            }
        } else {
            if (dbDateLastModified.isNotEmpty()){
                if (values.containsKey(dbDateLastModified)){
                    if (values.get(dbDateLastModified) == null || values.get(dbDateLastModified) == ""){
                        values.remove(dbDateLastModified)
                    }
                }
                values.put(
                    dbDateLastModified, LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd")))

            }
            if (dbDateCreated.isNotEmpty() && values.containsKey(dbDateCreated)){
                values.remove(dbDateCreated)
            }
        }
    }

    open fun insertTableRow(values: ContentValues): ReturnValue {
        return try {
            insert( removeNulls(filterColumns(values)))
        } catch (e: SQLException) {
            Logging.e("Error writing new $tableName", e.toString())
            val rtn = ReturnValue()
            rtn.returnValue = false
            rtn.messnr = R.string.mess_exception
            rtn.setAddParams( e.message!!)
            rtn
        }
    }

    fun rawUpdate(tableName:String, values: ContentValues, where:String, whereArgs: Array<String?>?) {
       Constants.db.update(tableName, values, where, whereArgs)
    }

    fun execSQL(sql: String): ReturnValue{
        val rtn = ReturnValue()
        Logging.d("execSQL",sql)
        try{
            Constants.db.execSQL(sql)
            rtn.setReturnvalue(true)
        } catch (e: Exception){
            Logging.e("execSQL: $sql",e.toString())
        }
        return rtn
    }

    fun rawQuery(sql: String, selectionArgs: Array<String?>?): ReturnValue {
        val rtn = ReturnValue()
        if (BuildConfig.DEBUG) {
            val tmp = sql.replace(" AND "," \nAND ",true)
                .replace(" WHERE "," \nWHERE ",true)
                .replace(" FROM "," \nFROM ",true)
                .replace(" INTO "," \nINTO ",true)
                .replace(" JOIN "," \nJOIN ",true)
                .replace(" ORDER "," \nORDER ",true)
            Logging.d(
                "rawQuery",
                StringUtils.repeat(
                    "-",
                    lenLine
                ) + "\nSQL: " + tmp + "\nselectionArgs: " + selectionArgs.contentToString()
            )
        }

        rtn.cursor = Constants.db.rawQuery(sql, selectionArgs)

        if (BuildConfig.DEBUG) {
            if (sql.startsWith("select", true) || sql.startsWith("WITH RECURSIVE", true)) {
                var line = ""
                val m = rtn.cursor.columnCount
                for (i in 0 until m) {
                    line += rtn.cursor.getColumnName(i) + ","
                }
                Logging.d(line.substring(0, line.length - 1))
                if (rtn.cursor.moveToFirst()) {
                    do {
                        line = ""
                        for (i in 0 until m) {
                            line += when (rtn.cursor.getType(i)) {
                                Cursor.FIELD_TYPE_NULL -> "null,"
                                Cursor.FIELD_TYPE_INTEGER -> rtn.cursor.getInt(i).toString() + ","
                                Cursor.FIELD_TYPE_FLOAT -> rtn.cursor.getFloat(i).toString() + ","
                                Cursor.FIELD_TYPE_STRING -> "'" + rtn.cursor.getString(i) + "',"
                                else -> "'?" + rtn.cursor.getType(i) + "?',"
                            }
                        }
                        Logging.d(line.substring(0, line.length - 1))
                    } while (rtn.cursor.moveToNext())
                    rtn.cursor.moveToFirst()
                }
            }
        }
        if (sql.startsWith("delete", true) ||
            sql.startsWith("insert", true) ||
            sql.startsWith("update", true)) {
            rtn.cursorClose()
            Logging.d( "Table row count: $rowCount \n${StringUtils.repeat("-", lenLine)}".trimIndent()
                )
        }
        rtn.setReturnvalue(true)
        return rtn
    }

    fun getRow(id: Int? = null, orderby: String? = null): ReturnValue{
        var sql = "SELECT " + TextUtils.join(",",dbColumns) + " FROM $tableName"
        if (id != null && id > 0) {
            sql += " WHERE " + primaryKeys[0] + "=$id"
        }
        sql += " ORDER BY " + (orderby ?: primaryKeys[0])
        return rawQuery(sql, null)
    }

    private fun insert(values: ContentValues): ReturnValue {
        addDatestamp(true, values)
        val rtn = ReturnValue()
        rtn.returnValue=true
        if (BuildConfig.DEBUG) {
            Logging.d(
                Constants.APP_NAME,
                StringUtils.repeat(
                    "-",
                    lenLine
                ) + "\nINSERT Table: $tableName \nvalues: " + values
            )
        }
        try {
            val id = Constants.db.insert(tableName, null, values)
            rtn.setId(id.toInt())
        } catch (e: SQLiteConstraintException){
            rtn.returnValue=false
            Logging.d(  ("Insert an error occurred (SQLiteConstraintException)\n" + e.toString() +
                 "\n ${StringUtils.repeat("-", lenLine)}").trimIndent()
            )
            return rtn
        }
        if (BuildConfig.DEBUG) {
            Logging.d(  """
     The row ID of the newly inserted row, or -1 if an error occurred: ${rtn.id}
     ${StringUtils.repeat("-", lenLine)}
     """.trimIndent()
            )
        }
        return rtn
    }

    open fun delete(whereClause: String): ReturnValue {
        if (BuildConfig.DEBUG) {
            Logging.d(
                StringUtils.repeat(
                    "-",
                    lenLine
                ) + "\nDELETE Table: $tableName \nwhereClause: $whereClause"
            )
        }

        val rtn = ReturnValue()
        try {
            rtn.rowsaffected = Constants.db.delete(tableName, whereClause, null)
            rtn.returnValue = true
        } catch (e: SQLException) {
            Logging.e("Error delete $tableName", e.toString())
            rtn.returnValue = false
            rtn.messnr = R.string.mess_exception
            rtn.setAddParams(e.message!!)
        }


        if (BuildConfig.DEBUG) {
            Logging.d(  """
     The number of rows affected: ${rtn.rowsaffected}
     ${StringUtils.repeat("-", lenLine)}
     """.trimIndent()
            )
        }
        return ReturnValue()
    }

    fun update(
        values: ContentValues,
        whereClause: String,
        whereArgs: Array<String?>?
    ): ReturnValue {
        addDatestamp(false, values)
        if (BuildConfig.DEBUG) {
            Logging.d(
                StringUtils.repeat(
                    "-",
                    lenLine
                ) + "\nUPDATE Table: $tableName \nContentValues: "
                        + values.toString() + "\nwhereClause: " + whereClause
                        + if (whereArgs == null) "" else """
     
     whereArgs:${whereArgs.contentToString()}
     """.trimIndent()
            )
        }

        val rtn = ReturnValue()
        try {
            rtn.rowsaffected = Constants.db.update(tableName, values, whereClause, whereArgs)
            rtn.returnValue = true
        } catch (e: SQLException) {
            Logging.e("Error update $tableName", e.toString())
            rtn.returnValue = false
            rtn.messnr = R.string.mess_exception
            rtn.setAddParams(e.message!!)
        }

        if (BuildConfig.DEBUG) {
            Logging.d(  """
     The number of rows affected: ${rtn.rowsaffected}
     ${StringUtils.repeat("-", lenLine)}
     """.trimIndent()
            )
        }
        return rtn
    }

    fun mapData(names: Array<String?>, values: Array<Any?>): ContentValues {
        val map = ContentValues()
        val j = names.size
        for (i in 0 until j) {
            if (values[i] == null) {
                map.put(names[i], "")
            } else {
                map.put(names[i], values[i].toString())
            }
        }
        return map
    }

    companion object {

        fun cursor2Intent(cursor: Cursor): Intent{
            val intent = Intent()
            for (i in 1 until cursor.columnCount) {
                intent.putExtra(cursor.getColumnName(i), cursor.getString(i))
            }
            return intent
        }

        fun cursor2map(cursor: Cursor): ContentValues{
            val map = ContentValues()
            for (i in 1 until cursor.columnCount) {
                map.put(cursor.getColumnName(i), cursor.getString(i))
            }
            return map
        }

        private fun getColumnIndex(cursor: Cursor?, name: String): Int {
            if (null == cursor) {
                throw IllegalArgumentException(Constants.APP_NAME + " Error getColumnIndex cursor is null search:" + name)
            }
            val names = cursor.columnNames
            var namesDisp = ""
            for (i in names.indices) {
                if (names[i].equals(name, ignoreCase = true)) {
                    return i
                }
                namesDisp += names[i].toString() + ","
            }
            throw IllegalArgumentException(Constants.APP_NAME + " Error getColumnIndex array: " + namesDisp + " search:" + name)
            //return -1;
        }

        fun getLong(cursor: Cursor, name: String): Long {
            val i = getColumnIndex(cursor, name)
            return if (i < 0) {
                0L
            } else cursor.getLong(i)
        }
        fun getDouble(cursor: Cursor, name: String): Double {
            val i = getColumnIndex(cursor, name)
            return if (i < 0) {
                0.0
            } else cursor.getDouble(i)
        }

        fun getFloat(cursor: Cursor, name: String): Float {
            val i = getColumnIndex(cursor, name)
            return if (i < 0) {
                0.0f
            } else cursor.getFloat(i)
        }

        fun getInt(cursor: Cursor, name: String): Int {
            val i = getColumnIndex(cursor, name)
            return if (i < 0) {
                0
            } else cursor.getInt(i)
        }

        fun getString(cursor: Cursor, name: String): String {
            val i = getColumnIndex(cursor, name)
            return if (i < 0) {
                ""
            } else {
                return cursor.getStringOrNull(i) ?: return ""
            }
        }

        fun getId(cursor: Cursor): Int {
            return Integer.valueOf(getString(cursor, "_id"))
        }
    }
}