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

import android.database.SQLException
import android.text.TextUtils
import com.farrusco.filecatalogue.common.ConstantsLocal
import com.farrusco.projectclasses.databases.tables.CursorX.getColumnValueInt
import com.farrusco.projectclasses.databases.tables.CursorX.getColumnValueString
import com.farrusco.projectclasses.databases.tables.Tables
import com.farrusco.projectclasses.utils.Logging
import com.farrusco.projectclasses.utils.ReturnValue

@Suppress("EnumEntryName")
open class TableProductRel: Tables(TABLE_NAME, Columns.entries.joinToString(",")) {

    companion object{
        const val TABLE_NAME = "POS_PRODUCT_REL"
    }
    enum class Columns { _id, _productid, _categoryid }

    fun deleteTableRow(
        id: Int, productid: Int,
        categoryid: Int
    ): Boolean {
        try {
            var where = ""
            if (id != 0) {
                where += " ${Columns._id.name} = $id and"
            }
            if (productid != 0) {
                where += " ${Columns._productid.name} = $productid and"
            }
            if (categoryid != 0) {
                where += " ${Columns._categoryid.name} = $categoryid and"
            }
            if (where == "") {
                Logging.e("Error delete $TABLE_NAME" , "no condition")
                return false
            }

            val rtn = getWhere(where.substring(0, where.length - 3))
            if (rtn.cursor.moveToFirst()){
                val tblFace = TableFace()
                do {
                    val catId = rtn.cursor.getColumnValueString(Columns._categoryid.name)
                    val prodId = rtn.cursor.getColumnValueString(Columns._productid.name)
                    tblFace.delete("${TableFace.Columns._productid.name} = $prodId and ${TableFace.Columns._categoryid.name} = $catId")
                } while(rtn.cursor.moveToNext())
            }
            rtn.cursorClose()
            delete(where.substring(0, where.length - 3))
        } catch (e: SQLException) {
            Logging.e("Error delete $TABLE_NAME", e.toString())
            return false
        }
        return true
    }

    fun getWhere(where: String): ReturnValue {
        val sql = "SELECT " + TextUtils.join(",",dbColumns) + " FROM $tableName where $where"
        return rawQuery(sql, null)
    }

    fun deleteNotProduct(): ReturnValue {
        return try {
            val where = " not exists (select 1 from ${TableProduct.TABLE_NAME} " +
                    " where ${TableProduct.Columns._id.name} = $TABLE_NAME.${Columns._productid.name})"
            delete(where)
        } catch (e: SQLException) {
            Logging.e("Error delete $TABLE_NAME", e.toString())
            val rtn = ReturnValue()
            rtn.returnValue = false
            rtn
        }
    }

    fun deleteProductRel(productid: Int, categoryid: Int): Boolean {
        return deleteTableRow(0, productid, categoryid)
    }

    fun insertProductRel(productid: Int?, categoryid: Int): ReturnValue {
        val sql = "insert into $TABLE_NAME (_productid, _categoryid)" +
                " select $productid, $categoryid" +
                " where not exists(select 1 from $TABLE_NAME" +
                " where _productid = $productid and _categoryid = $categoryid)"
        return execSQL(sql)
    }

    fun linkProduct(){
        val sql = "select distinct pos_product._id as _productid,  pos_category._id as _categoryid" +
                " from pos_category" +
                " join pos_product on pos_product.dirname = pos_category.description" +
                " left join $TABLE_NAME on $TABLE_NAME._productid = pos_product._id and $TABLE_NAME._categoryid = pos_category._id" +
                " where pos_product.type = ${ConstantsLocal.TYPE_FILE}" +
                " and $TABLE_NAME._id is null" +
                " and pos_category.type = ${ConstantsLocal.TYPE_DIRECTORY}"

        val rtn = rawQuery( sql, null)
        if (rtn.cursor.moveToFirst()) {
            do {
                val productid = rtn.cursor.getColumnValueInt(Columns._productid.name)
                val categoryid = rtn.cursor.getColumnValueInt(Columns._categoryid.name)!!
                insertProductRel(productid, categoryid)
            } while (rtn.cursor.moveToNext())
        }
        rtn.cursorClose()
    }

    fun getProductRelProductId(id: Int): ReturnValue {
        return rawQuery(
            "SELECT "+ TextUtils.join(",",dbColumns) + " FROM $TABLE_NAME"
                    + " WHERE ${Columns._productid.name} = $id", null)
    }

    fun getCategoryFromProduct(productid: Int): ReturnValue{
        var sql = "SELECT _productid, group_concat( _categoryid ) as _categoryid" +
            " from pos_product_rel"
        if (productid > 0){
            sql += " where _productid = $productid"
        }
        return rawQuery("$sql group by  _productid",null)
    }
}