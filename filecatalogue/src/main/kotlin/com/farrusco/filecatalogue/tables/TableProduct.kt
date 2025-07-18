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

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.text.TextUtils
import android.widget.Toast
import com.farrusco.filecatalogue.BuildConfig
import com.farrusco.filecatalogue.R
import com.farrusco.filecatalogue.business.OrderLine
import com.farrusco.filecatalogue.business.ProductRel
import com.farrusco.filecatalogue.common.ConstantsLocal
import com.farrusco.projectclasses.databases.tables.CursorX.getColumnValueInt
import com.farrusco.projectclasses.databases.tables.DBUtils
import com.farrusco.projectclasses.databases.tables.Tables
import com.farrusco.projectclasses.utils.CSVFileReader
import com.farrusco.projectclasses.utils.Logging
import com.farrusco.projectclasses.utils.ReturnValue
import com.farrusco.projectclasses.messages.ToastExt
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Suppress("EnumEntryName")
open class TableProduct(context: Context) : Tables(TABLE_NAME, Columns.entries.joinToString(",")) {
    private var _context = context

    companion object{
        const val TABLE_NAME = "POS_PRODUCT"
    }
    enum class Columns { _id, _categoryid, type, description, title,
        price, code, blocked, dateavailable, filelastmodified, recursive, dirname, filename,
        rotation, gpslocation, gpsstatus, sizekb}

    override fun updatePrimaryKey(map: ContentValues): ReturnValue{
        DBUtils.trimColumnValue(map, TableCategory.Columns.description.name)
        DBUtils.trimColumnValue(map, TableCategory.Columns.title.name)
        val values = ContentValues()

        values.put(Columns._id.name, map.getAsInteger(Columns._id.name))
        values.put(Columns.description.name, map.getAsString(Columns.description.name))
        values.put(Columns.title.name, map.getAsString(Columns.title.name))
        values.put(Columns.code.name, map.getAsString(Columns.code.name))
        values.put(Columns.dirname.name, map.getAsString(Columns.dirname.name))
        values.put(Columns.type.name, map.getAsString(Columns.type.name))
        when (map.getAsInteger(Columns.type.name)){
            ConstantsLocal.TYPE_DIRECTORY -> {
                if (map.getAsInteger(Columns.recursive.name) != null){
                    values.put(Columns.recursive.name, map.getAsInteger(Columns.recursive.name))
                }
                if (map.getAsInteger(Columns._categoryid.name) != null){
                    // when folder complete is changed the old category is deleted and a new one is created
                    values.put(Columns._categoryid.name, map.getAsInteger(Columns._categoryid.name))
                }
            }
            ConstantsLocal.TYPE_FILE -> {
                values.put(Columns.price.name, map.getAsDouble(Columns.price.name))
                values.put(Columns.gpslocation.name, map.getAsString(Columns.gpslocation.name))
                values.put(Columns.gpsstatus.name, map.getAsString(Columns.gpsstatus.name))
                values.put(Columns.filename.name, map.getAsString(Columns.filename.name))
                values.put(Columns.blocked.name, map.getAsInteger(Columns.blocked.name))
                values.put(Columns.rotation.name, map.getAsString(Columns.rotation.name))
                values.put(Columns.sizekb.name, map.getAsInteger(Columns.sizekb.name))
                //val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                if (map.getAsString(Columns.dateavailable.name) == null){
                    values.put(Columns.dateavailable.name,LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                } else{
                    values.put(Columns.dateavailable.name,map.getAsString(Columns.dateavailable.name).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                }
                if (map.getAsString(Columns.filelastmodified.name)  == null){
                    values.put(Columns.filelastmodified.name,LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                } else{
                    values.put(Columns.filelastmodified.name,map.getAsString(Columns.filelastmodified.name).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                }
                updateCategory(values)
            }
            else -> {
                return super.updatePrimaryKey(map)
            }
        }
        return super.updatePrimaryKey(values)
    }

    fun updateGPSInfo(values: ContentValues): ReturnValue {
        val map = ContentValues()
        map.put(Columns._id.name, values.getAsInteger(Columns._id.name))
        map.put(Columns.gpslocation.name, values.getAsString(Columns.gpslocation.name))
        map.put(Columns.gpsstatus.name, values.getAsString(Columns.gpsstatus.name))
        return super.updatePrimaryKey(map)
    }

    fun updateCategory(values: ContentValues): ReturnValue{
        var rtn = ReturnValue()
        if (values.getAsInteger(Columns.type.name) != ConstantsLocal.TYPE_FILE) {
            rtn.returnValue = false
            return rtn
        }

        if (values.getAsInteger(Columns._categoryid.name) == null) {
            values.put(Columns._categoryid.name, 0)
        }

        val dir = values.getAsString(Columns.dirname.name)
        var categoryIdNew = try {
            DBUtils.eLookUp(
                TableCategory.Columns._id.name,
                TableCategory.TABLE_NAME,
                TableCategory.Columns.description.name + "='$dir'"
            ).toString().toInt()
        } catch (_: Exception){
            -1
        }

        if (categoryIdNew <= 0) {
            val mainId = try {
                DBUtils.eLookUp(
                    TableCategory.Columns._id.name,
                    TableCategory.TABLE_NAME,
                    "${TableCategory.Columns.type.name} = ${ConstantsLocal.TYPE_DIRECTORY} and ${TableCategory.Columns._mainid.name} = 0"
                ).toString().toInt()
            } catch (_: Exception){
                rtn.returnValue = false
                return rtn
            }
            if (mainId > 0){
                val tblCategory = TableCategory(_context)
                val mapCategory = ContentValues()
                mapCategory.put(TableCategory.Columns.type.name, ConstantsLocal.TYPE_DIRECTORY)
                mapCategory.put(TableCategory.Columns.title.name, dir)
                mapCategory.put(TableCategory.Columns.description.name, dir)
                mapCategory.put(TableCategory.Columns.seqno.name, -1)
                mapCategory.put(TableCategory.Columns.level.name, 2)
                mapCategory.put(TableCategory.Columns._mainid.name, mainId)
                rtn = tblCategory.insertTableRow(mapCategory)
                categoryIdNew = rtn.id
            }
        }

        if (categoryIdNew != values.getAsInteger(Columns._categoryid.name)){
            if (values.getAsInteger(Columns._categoryid.name)>0){
                TableProductRel().deleteProductRel(values.getAsInteger(Columns._id.name),values.getAsInteger(Columns._categoryid.name))
            }
            TableProductRel().deleteProductRel(values.getAsInteger(Columns._id.name),categoryIdNew)
            val map = ContentValues()
            map.put(TableProductRel.Columns._categoryid.name,categoryIdNew)
            map.put(TableProductRel.Columns._productid.name,values.getAsInteger(Columns._id.name))
            rtn = TableProductRel().insertPrimaryKey(map)
            values.put(Columns._categoryid.name, categoryIdNew)
        }
        return rtn
    }

    @SuppressLint("ObsoleteSdkInt")
    override fun insertPrimaryKey(values: ContentValues): ReturnValue{
        if (BuildConfig.LIMIT > 0
            && getFilesCount() >= BuildConfig.LIMIT
            && values.getAsInteger(Columns.type.name) == ConstantsLocal.TYPE_FILE){
            val rtn = ReturnValue()
            rtn.returnValue = false
            rtn.messnr = R.string.mess058_demo
            return rtn
        }
        val map2 = ContentValues()
        map2.put(Columns.type.name, values.getAsInteger(Columns.type.name))
        map2.put(Columns.dirname.name, values.getAsString(Columns.dirname.name))

        DBUtils.trimColumnValue(values, TableCategory.Columns.description.name)
        DBUtils.trimColumnValue(values, TableCategory.Columns.title.name)

        map2.put(Columns._id.name, values.getAsInteger(Columns._id.name))
        map2.put(Columns._categoryid.name, values.getAsInteger(Columns._categoryid.name))
        map2.put(Columns.description.name, values.getAsString(Columns.description.name))
        map2.put(Columns.title.name, values.getAsString(Columns.title.name))

        if (values.getAsInteger(Columns.type.name)  == ConstantsLocal.TYPE_DIRECTORY){
            map2.put(Columns.recursive.name, values.getAsBoolean(Columns.recursive.name))
        } else {
            map2.put(Columns.sizekb.name, values.getAsInteger(Columns.sizekb.name))
            if (values.getAsDouble(Columns.price.name) != null) {
                map2.put(Columns.price.name, values.getAsDouble(Columns.price.name))
            } else {
                map2.put(Columns.price.name, 0)
            }
            if (values.getAsString(Columns.code.name) != null) {
                map2.put(Columns.code.name, values.getAsString(Columns.code.name))
            } else {
                map2.put(Columns.code.name, "Code ${values.getAsInteger(Columns._id.name)}")
            }
            //val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (values.getAsString(Columns.dateavailable.name) == null) {
                    map2.put(
                        Columns.dateavailable.name,
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    )
                } else {
                    map2.put(
                        Columns.dateavailable.name,
                        values.getAsString(Columns.dateavailable.name)
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    )
                }
                if (values.getAsString(Columns.filelastmodified.name) == null){
                    map2.put(Columns.filelastmodified.name,LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                } else{
                    map2.put(Columns.filelastmodified.name,values.getAsString(Columns.filelastmodified.name).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                }
            } else{
                if (values.getAsString(Columns.dateavailable.name) == null) {
                    map2.put(
                        Columns.dateavailable.name,
                        Calendar.getInstance().time.toString())
                } else {
                    map2.put(
                        Columns.dateavailable.name,
                        values.getAsString(Columns.dateavailable.name)
                    )
                }
                map2.put(Columns.filelastmodified.name,map2.getAsString(Columns.dateavailable.name))
            }
            if (values.getAsString(Columns.filename.name) != "") {
                map2.put(Columns.filename.name, values.getAsString(Columns.filename.name) )
                map2.put(Columns.gpslocation.name, values.getAsString(Columns.gpslocation.name) )
                map2.put(Columns.gpsstatus.name, values.getAsString(Columns.gpsstatus.name) )
            }

            if (values.getAsString(Columns.gpsstatus.name) != null) {
                map2.put(Columns.gpsstatus.name, "2")
            }
            map2.put(Columns.rotation.name, 0)
            map2.put(Columns.blocked.name, 0)
        }
        var rtn = super.insertPrimaryKey(map2)
        if (!rtn.returnValue) {
            return rtn
        }
        val productId = rtn.id
        map2.put(Columns._id.name, productId )
        val rtn2 = updateCategory(map2)
        if (rtn2.returnValue){
            rtn = super.updatePrimaryKey(map2)
        }
        rtn.id = productId
        return rtn
    }

    fun getColumnsWhere(columns: String, where: String): ReturnValue {
        val limit = if (BuildConfig.LIMIT > 0) {
            "limit ${BuildConfig.LIMIT}"
        } else {
            ""
        }
        val sql = "SELECT $columns FROM $tableName where $where" +
                " ORDER BY ${Columns.dirname.name}, ${Columns.filename.name} $limit "
        return rawQuery(sql, null)
    }

    fun getWhere(where: String): ReturnValue {
        val limit = if (BuildConfig.LIMIT > 0) {
            "limit ${BuildConfig.LIMIT}"
        } else {
            ""
        }

        val sql = "SELECT " + TextUtils.join(",",dbColumns) + " FROM $tableName where $where" +
                " ORDER BY ${Columns.dirname.name}, ${Columns.filename.name} $limit "
        return rawQuery(sql, null)
    }

    fun getProductFaces(): ReturnValue{
        val filetypes =  "(substring(${Columns.filename.name},-4) in ('.gif', '.jpg', '.raw', '.png', '.webp', '.bmp')" +
                " or substring(${Columns.filename.name},-5) = '.jpeg')"
        val columns = "$tableName._id, ${Columns.dirname.name}, ${Columns.filename.name}"
        val sql = "SELECT $columns, pos_product_rel._categoryid" +
                " FROM $tableName" +
                " JOIN pos_product_rel on pos_product_rel._productid = $tableName._id" +
                " JOIN POS_CATEGORY on POS_CATEGORY._id = pos_product_rel._categoryid and POS_CATEGORY.type = 9" +
                " WHERE pos_product.type = ${ConstantsLocal.TYPE_FILE}" +
                " and $filetypes" +
                " union" +
                " select $columns,0" +
                " FROM $tableName" +
                " WHERE $tableName.type = ${ConstantsLocal.TYPE_FILE}"+
                " and $filetypes" +
                " and not $tableName._id in  (select  pos_product_rel._productid" +
                " from pos_product_rel" +
                " JOIN POS_CATEGORY on POS_CATEGORY._id = pos_product_rel._categoryid and POS_CATEGORY.type = ${ConstantsLocal.TYPE_FACE})"
        return rawQuery(sql, null)
    }
    fun getCountCategory(): ReturnValue{
        val sql = "select $tableName._id, count(POS_CATEGORY._id) as countCategory" +
                " FROM $tableName" +
                " JOIN pos_product_rel on pos_product_rel._productid = $tableName._id" +
                " JOIN POS_CATEGORY on POS_CATEGORY._id = pos_product_rel._categoryid and POS_CATEGORY.type = ${ConstantsLocal.TYPE_FACE}" +
                " WHERE $tableName.type = ${ConstantsLocal.TYPE_FILE}" +
                " group by $tableName._id"
        return rawQuery(sql, null)
    }

    fun getProduct(id: Int): ReturnValue {
        val limit = if (BuildConfig.LIMIT > 0) {
            "limit ${BuildConfig.LIMIT}"
        } else {
            ""
        }
        val sql = "SELECT " + TextUtils.join(",",dbColumns) + " FROM $tableName"
        return if (id == 0) {
            rawQuery(sql + " WHERE ${Columns.type.name} = ${ConstantsLocal.TYPE_FILE} $limit", null
            )
        } else rawQuery( sql + " WHERE ${Columns._id.name} = $id", null)
    }

    fun getFilesCount(): Int {
        return DBUtils.eLookUp("count(*)", tableName,
            "${Columns.type.name} = " + ConstantsLocal.TYPE_FILE).toString().toInt()
    }

    fun getProductDirectory(): ReturnValue {
        val sql = "SELECT " + TextUtils.join(",",dbColumns) + " FROM $tableName"
        return rawQuery(sql + " WHERE ${Columns.type.name} = ${ConstantsLocal.TYPE_DIRECTORY}" , null )
    }

    fun getGPSProductRefresh(): ReturnValue {
        val sql = "SELECT " + TextUtils.join(",",dbColumns) + " FROM $tableName" +
                " where ${Columns.gpsstatus.name} = 1"
        return rawQuery( sql, null)

    }
    private fun deleteCascade(id: Int): ReturnValue{
        val rtn = getProduct(id)
        if (rtn.cursor.moveToFirst()){
            val tblProductRel = ProductRel(_context)
            val tblOrderLine = OrderLine()
            val type = rtn.cursor.getColumnValueInt(Columns.type.name)
            val categoryid = rtn.cursor.getColumnValueInt(Columns._categoryid.name)
            when (type){
                ConstantsLocal.TYPE_DIRECTORY ->{
                    if (categoryid != null && categoryid > 0){
                        TableCategory(_context).delete("_id = $categoryid")
                        val where =
                            "${Columns._categoryid.name} = $categoryid and ${Columns.type.name} = ${ConstantsLocal.TYPE_FILE}"
                        super.delete(where)
                        tblOrderLine.deleteNotProduct()
                        tblProductRel.deleteNotProduct()
                    }
                }
                ConstantsLocal.TYPE_FILE -> {
                    TableFace().delete("${TableFace.Columns._productid.name} = $id")
                    if (!TableOrderLine().deleteProduct(id).returnValue) {
                        Logging.e("Error deleting $tableName", "Product.deleteProduct(id)")
                        return ReturnValue().setReturnvalue(false)
                    }
                }
            }
            tblProductRel.delete("_productid = $id")
        }
        rtn.cursorClose()
        return rtn
    }
    override fun deletePrimaryKey(values: ContentValues): ReturnValue {
        val id = values.getAsInteger(Columns._id.name)
        deleteCascade(id)
        return super.deletePrimaryKey(values)
    }

    fun readProductPrices( filename: String): Int {
        // import
        // code,title, price, description
        var countRecords = 0
        val f = File(filename.replace("\n".toRegex(), ""))

        if (!f.exists()) return 0
        if (f.isDirectory) return 0

        val csvFile = CSVFileReader(f.path, ",")
        csvFile.readFile()

        val arrRecords: ArrayList<Any> = csvFile.fileValuesSplit
        var i = 0
        val j = arrRecords.size
        while (i < j) {
            val arrDetail = ((arrRecords[i] as ArrayList<*>)[0] as CharSequence).split(";")
            val rtn = selectWhere("${Columns.code.name} = \"${arrDetail[0]}\"")
            if (rtn.cursor.moveToFirst()) {
                val map = rtn.cursorToMap()
                map.put(Columns.title.name, arrDetail[1])
                if (ConstantsLocal.isPriceUseEnabled && arrDetail.size == 4){
                    map.put(Columns.price.name, arrDetail[2])
                    map.put(Columns.description.name, arrDetail[3])
                } else{
                    map.put(Columns.description.name, arrDetail[2])
                }
                updatePrimaryKey(map)
                countRecords++
            }
            rtn.cursorClose()
            i++
        }
        return countRecords
    }

    fun writeProductPrices( path: String): Boolean {
        val c = Calendar.getInstance()
        val dir = File(path)
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                ToastExt().makeText(
                    _context, _context.getText(R.string.mess005_nocreatefolder)
                        .toString() + dir.path, Toast.LENGTH_SHORT
                ).show()
                return false
            }
        }
        val objects = StringBuffer()
        val rtn = rawQuery(
            "SELECT ${Columns.code.name},${Columns.title.name},${Columns.price.name},${Columns.description.name} FROM $tableName"
                    + " order by ${Columns.code.name}", null
        )
        rtn.cursor.moveToPosition(-1)
        while (rtn.cursor.moveToNext()) {
            var line = if (rtn.cursor.getString(0) == null) "" else rtn.cursor.getString(0) + ","
            line += if (rtn.cursor.getString(1) == null) "" else rtn.cursor.getString(1) + ","
            if (ConstantsLocal.isPriceUseEnabled){
                line += rtn.cursor.getDouble(2)
            }
            line += if (rtn.cursor.getString(3) == null) "" else rtn.cursor.getString(3)
            objects.append(line.trimIndent())
        }
        val file = File(
            dir, "PosProduct_" + c[Calendar.YEAR]
                    + c[Calendar.MONTH] + c[Calendar.DAY_OF_MONTH] + ".csv"
        )
        try {
            writeToFile(file, objects)
            ToastExt().makeText(
                _context, _context.getText(R.string.mess018_listready)
                    .toString() + file.path, Toast.LENGTH_LONG
            ).show()
        } catch (e: IOException) {
            ToastExt().makeText(
                _context, _context.getText(R.string.mess019_errexport)
                    .toString() + e.message, Toast.LENGTH_LONG
            ).show()
            e.printStackTrace()
        }
        rtn.cursorClose()
        return true
    }

    @Throws(IOException::class)
    private fun writeToFile(
        file: File,
        objects: StringBuffer) {
        val fwrite = FileWriter(file, false)
        fwrite.write(objects.toString())
        fwrite.close()
    }

    fun existsProduct(where: String): Boolean {
        var whereX = Columns.type.name +" = " + ConstantsLocal.TYPE_FILE
        var sql = "SELECT 1 FROM $tableName WHERE ${Columns.type.name} = ${ConstantsLocal.TYPE_FILE}"
        if (where.isNotEmpty()){
            sql += " and ($where)"
            whereX += " and ($where)"
        }
        return "1" == DBUtils.eLookUp("1", tableName, whereX)
    }

    fun getDirectory(id: Int): ReturnValue {
        return if (id == 0) {
            rawQuery(
                "SELECT " + TextUtils.join(", ",dbColumns)
                        + " FROM $tableName"
                        + " WHERE ${Columns.type.name} = ${ConstantsLocal.TYPE_DIRECTORY}"
                        + " ORDER BY ${Columns.description.name}, ${Columns.dirname.name}" , null
            )
        } else rawQuery(
            "SELECT " + TextUtils.join(", ",dbColumns)
                    + " FROM $tableName WHERE ${Columns._id.name} = $id",null)
    }

    override fun delete(whereClause: String): ReturnValue {
        var rtn = super.delete(whereClause)
        if (rtn.returnValue) {
            // delete all orphans
            rtn = TableProductRel().deleteNotProduct()
        }
        return rtn
    }
}