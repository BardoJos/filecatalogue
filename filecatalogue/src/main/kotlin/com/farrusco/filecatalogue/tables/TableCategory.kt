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
import android.content.Context
import android.text.TextUtils
import com.farrusco.filecatalogue.R
import com.farrusco.filecatalogue.business.Systeem
import com.farrusco.filecatalogue.common.ConstantsLocal
import com.farrusco.filecatalogue.common.SystemAttr
import com.farrusco.projectclasses.databases.tables.CursorX.getColumnValueInt
import com.farrusco.projectclasses.databases.tables.DBUtils
import com.farrusco.projectclasses.databases.tables.Tables
import com.farrusco.projectclasses.utils.CalcObjects
import com.farrusco.projectclasses.utils.FolderProduct
import com.farrusco.projectclasses.utils.Logging
import com.farrusco.projectclasses.utils.ReturnValue

@Suppress("EnumEntryName")
open class TableCategory(context: Context) : Tables(TABLE_NAME, Columns.entries.joinToString(",")) {
    private val contextLocal: Context = context

    companion object{
        const val TABLE_NAME = "POS_CATEGORY"
    }
    enum class Columns { _id, _mainid, seqno, type, level, title, description }
    private val arrDbFolder: MutableList<FolderProduct> = mutableListOf()

    inner class IdDescription{
        var id = StringBuilder()
        var description = StringBuilder()
    }

    override fun deletePrimaryKey(values: ContentValues): ReturnValue {
        val tableProductRel = TableProductRel()
        var where = getSubCategory(values.getAsInteger("_id")).id.toString()
        if (where != "") where += ","
        where += "${values.getAsInteger("_id")}"
        tableProductRel.delete("${TableProductRel.Columns._categoryid.name} in (${where})")
        val tableFace = TableFace()
        tableFace.delete("${TableFace.Columns._categoryid.name} in (${where})")
        return super.delete("${Columns._id.name} in ($where)")
    }

    override fun delete(whereClause: String): ReturnValue {
        val tableProductRel = TableProductRel()
        val tableFace = TableFace()
        val sql =
            "SELECT ${Columns._id} FROM $TABLE_NAME where $whereClause"
        var rtn = rawQuery(sql, null)
        var whereX = ""
        if (rtn.cursor.moveToFirst()) {
            do {
                val id = rtn.cursor.getColumnValueInt(Columns._id.name)
                whereX += "$id,"
            } while (rtn.cursor.moveToNext())
        }
        rtn.cursorClose()

        rtn = super.delete(whereClause)
        if (whereX != ""){
            whereX = whereX.substring(0,whereX.length-1)
            tableProductRel.delete( "${TableProductRel.Columns._categoryid.name} in ($whereX)")
            tableFace.delete( "${TableFace.Columns._categoryid.name} in ($whereX)")
            rtn = delete(Columns._mainid.name + " in ($whereX)")
        }
        return rtn
    }

    fun getCategoryType(type: String): ReturnValue {
        val empty = contextLocal.getText(R.string.empty)

        var sql = "SELECT ${Columns._id.name},${Columns._mainid.name}, ${Columns.seqno.name}, ${Columns.type.name}, ${Columns.level.name}" +
                ", case when ${Columns.title.name} = '' then '$empty' else ${Columns.title.name} end as ${Columns.title.name}, ${Columns.description.name}" +
                " FROM $TABLE_NAME"

        if (type != "" && type != ConstantsLocal.TYPE_ALL.toString()) {
            sql += " WHERE ${Columns.type.name} in ( $type )"
        }
        var level = ""
        var sortDesc = ""
        if (ConstantsLocal.sortCreationYearDesc) {
            if (ConstantsLocal.sortCreationMonthDesc) {
                level = "and ${Columns.level.name} in (2,3)"
                sortDesc="10000 -"
            } else {
                level = "and ${Columns.level.name} = 2"
                sortDesc="10000 -"
            }
        } else {
            if (ConstantsLocal.sortCreationMonthDesc) {
                level = "and ${Columns.level.name} = 3"
                sortDesc="10000 -"
            }
        }
        sql += " ORDER BY case when ${Columns.type.name}=${ConstantsLocal.TYPE_CREATION} $level then $sortDesc ${Columns.title.name} else ${Columns.seqno.name} end, ${Columns.title.name}"
        return rawQuery(sql, null)
    }

    fun syncQuery(){
        val systeem = Systeem(contextLocal)
        val storeQuery = CalcObjects.objectToInteger(systeem.getValue(SystemAttr.StoreQuery))
        delete("${Columns.type.name} = ${ConstantsLocal.TYPE_SEARCH}" +
                " and ${Columns.seqno.name} between $storeQuery+1 and 98")
        val max = DBUtils.eLookUpInt("max(seqno)", TABLE_NAME, "${Columns.type.name} = ${ConstantsLocal.TYPE_SEARCH}" +
                " and ${Columns.seqno.name} < 99") + 1
        for (idx in max ..storeQuery){
            val map = ContentValues()
            map.put(Columns.seqno.name,idx)
            map.put(Columns.description.name,"")
            map.put(Columns.title.name,idx)
            map.put(Columns.type.name, ConstantsLocal.TYPE_SEARCH)
            insertPrimaryKey(map)
        }
    }

    fun getCategory(
        id: Int, mainid: Int, types: ArrayList<Int>,
        related: Boolean
    ): ReturnValue {
        var where = ""
        if (id > 0) {
            where += "_id = $id and"
        }
        if (mainid >= 0) {
            where += " _mainid = $mainid and"
        }
        if (types.size>1) {
            where += " type in ("
            types.forEach {where += "$it ," }
            where = where.substring(0, where.length - 1) + ") and"
        } else if (types.size == 1 && types[0] != ConstantsLocal.TYPE_ALL) {
            where += " type = ${types[0]} and"
        }
        if (related) {
            where += " exists(select 1 from pos_product_rel where pos_product_rel._categoryid = pos_category._id) and"
        }

        val orderby = "${Columns.type.name}, ${Columns.seqno.name}, ${Columns.description.name}"
        return if (where != "") {
            rawQuery(
                "SELECT " + TextUtils.join(",",dbColumns) +
                        " FROM $TABLE_NAME" +
                        " WHERE " + where.substring(0, where.length - 3) +
                        " ORDER BY $orderby", null
            )
        } else rawQuery(
            "SELECT " + TextUtils.join(",",dbColumns) +
                    " FROM $TABLE_NAME" +
                    " ORDER BY $orderby", null
        )
    }

    fun fillFolder( mainid:Int, arrFolder: MutableList<FolderProduct>): ReturnValue {

        arrDbFolder.clear()
        if (mainid == 0){
            return ReturnValue()
        }
        val sql = "WITH RECURSIVE generation AS (" +
                " SELECT ${Columns._id.name}, ${Columns._mainid.name}, ${Columns.level.name}, ${Columns.seqno.name}, ${Columns.description.name} FROM $TABLE_NAME" +
                " WHERE ${Columns._mainid.name} = $mainid and ${Columns.type.name} = ${ConstantsLocal.TYPE_DIRECTORY}" +
                " UNION" +
                " SELECT child.${Columns._id.name}, child.${Columns._mainid.name}, child.${Columns.level.name}, child.${Columns.seqno.name}, child.${Columns.description.name}" +
                " FROM $TABLE_NAME child" +
                " JOIN generation g ON g.${Columns._id.name} = child.${Columns._mainid.name})" +
                " SELECT ${Columns._id.name}, ${Columns._mainid.name}, ${Columns.level.name}, ${Columns.seqno.name}, ${Columns.description.name}" +
                " FROM $TABLE_NAME" +
                " where ${Columns._id.name} = $mainid and ${Columns.type.name} = ${ConstantsLocal.TYPE_DIRECTORY}" +
                " union" +
                " SELECT ${Columns._id.name}, ${Columns._mainid.name}, ${Columns.level.name}, ${Columns.seqno.name}, ${Columns.description.name}" +
                " FROM generation"
        val rtn = rawQuery(sql, null)
        if (rtn.cursor.moveToFirst()) {
            do {
                val fp = FolderProduct()
                fp.id = rtn.cursor.getString(0).toInt()
                fp.mainid = rtn.cursor.getString(1).toInt()
                fp.level = rtn.cursor.getString(2).toInt()
                fp.isChecked = (rtn.cursor.getString(3).toInt() != 0)
                fp.folder = if (rtn.cursor.getString(4) == null) "" else rtn.cursor.getString(4)
                fp.title = fp.folder
                arrDbFolder.add(fp)
            } while (rtn.cursor.moveToNext())
        }
        rtn.cursorClose()

        arrDbFolder.forEach { db ->
            arrFolder.filter { db.folder == it.folder }.forEach { fldr ->
                fldr.id = db.id
                fldr.mainid = db.mainid
                fldr.isChecked = db.isChecked
                //fldr.level = db.level
                fldr.title = db.title
            }
        }

        if (arrDbFolder.isNotEmpty() && arrFolder.isNotEmpty()){
            arrFolder[0].id = arrDbFolder[0].id
            arrFolder[0].mainid = arrDbFolder[0].mainid
            //arrFolder[0].level = arrDbFolder[0].level
            arrFolder[0].isChecked = arrDbFolder[0].isChecked
            arrFolder.filterIndexed { index, folderProduct -> index > 0 && folderProduct.id == arrDbFolder[0].id }.forEach {
                it.id = 0
                it.mainid = 0
                it.isChecked = true
            }
        }

        return rtn
    }

    fun updSubCategory(productid: Int, mainid:Int, title: String,
                       arrFolder: MutableList<FolderProduct>): ReturnValue {
        val product = TableProduct(contextLocal)
        val productRel = TableProductRel()
        val rtn = ReturnValue()
        rtn.rowsaffected = 0
        if ( arrFolder.size == 1 && arrDbFolder.size == 1){
            // 1 folder no sub folders
            if (arrFolder[0].folder.equals(arrDbFolder[0].folder,true)){
                // nothing changed in root folder
            } else {
                // folder does nog match with database
                arrFolder[0].title = arrDbFolder[0].title
                var map = ContentValues()
                map.put(Columns._id.name, arrDbFolder[0].id)
                map.put(Columns.title.name, title.trim())
                map.put(Columns.description.name, arrFolder[0].folder)
                updatePrimaryKey(map)

                map = ContentValues()
                map.put(TableProduct.Columns._id.name, productid)
                map.put(TableProduct.Columns.dirname.name, arrFolder[0].folder)
                product.updatePrimaryKey(map)

                // disconnect product from folder
                map = ContentValues()
                map.put(TableProduct.Columns._categoryid.name, 0)
                product.updateWhere(
                    "${TableProduct.Columns._id.name} != $productid and ${TableProduct.Columns._categoryid.name} = ${arrDbFolder[0].id}",
                    map
                )

                productRel.deleteTableRow(0, 0, arrDbFolder[0].id)
                rtn.rowsaffected = 1
            }
        } else if (arrFolder.isNotEmpty()) {

            when (arrDbFolder.size){
                0 -> {
                    // no registration of sub folders
                    val mainid1 = DBUtils.eLookUpInt(Columns._id.name, TABLE_NAME,
                        "${Columns._mainid.name} = 0 " +
                                " and ${Columns.type.name} = ${ConstantsLocal.TYPE_DIRECTORY} " +
                                " and ${Columns.level.name} = 1" )
                    arrFolder.forEach {
                        val map = ContentValues()
                        val fp = FolderProduct()
                        map.put(Columns.title.name, it.title.trim())
                        map.put(Columns.description.name, it.folder.trim())
                        map.put(Columns.seqno.name, if(it.isChecked) -1 else 0)
                        map.put(Columns.level.name, it.level)
                        val tmpid = if (it.level == 2 || arrDbFolder.isEmpty()) mainid1 else {
                            arrFolder[it.parentRow].id
                        }
                        map.put(Columns._mainid.name, tmpid)
                        map.put(Columns.type.name,ConstantsLocal.TYPE_DIRECTORY)

                        fp.id = insertPrimaryKey(map).id
                        it.id = fp.id
                        it.mainid = tmpid

                        fp.isChecked = it.isChecked
                        fp.folder = it.folder
                        fp.title = it.title
                        fp.level = it.level
                        fp.mainid = tmpid
                        arrDbFolder.add(fp)
                        rtn.rowsaffected=1
                    }
                }
                1 -> {
                    // change folder to multiple folder
                    if (arrFolder[0].folder.equals(arrDbFolder[0].folder,true)){
                        // nothing changed in root folder
                    } else {
                        arrFolder[0].title = arrDbFolder[0].title
                        // folder does not match with database
                        var map = ContentValues()
                        map.put(Columns._id.name, arrDbFolder[0].id)
                        map.put(Columns.title.name, title.trim())
                        map.put(Columns.description.name, arrFolder[0].folder.trim())
                        updatePrimaryKey(map)

                        map = ContentValues()
                        map.put(TableProduct.Columns._id.name, productid)
                        map.put(TableProduct.Columns.dirname.name, arrFolder[0].folder)
                        product.updatePrimaryKey(map)

                        // disconnect product from folder
                        map = ContentValues()
                        map.put(TableProduct.Columns._categoryid.name, 0)
                        product.updateWhere("${TableProduct.Columns._id.name} != $productid and ${TableProduct.Columns._categoryid.name} = ${arrDbFolder[0].id}",map)

                        productRel.deleteTableRow(0,0,arrDbFolder[0].id)
                        rtn.rowsaffected=1
                    }
                    arrFolder.forEachIndexed { index, folderProduct ->
                        if (index > 0) {
                            // insert missing sub-folders
                            val fp = FolderProduct()
                            val map = ContentValues()
                            map.put(Columns.title.name, folderProduct.title.trim())
                            map.put(Columns.description.name, folderProduct.folder.trim())
                            map.put(Columns.level.name, folderProduct.level)
                            map.put(Columns.seqno.name, if(folderProduct.isChecked) -1 else 0)
                            map.put(Columns.type.name,ConstantsLocal.TYPE_DIRECTORY)
                            try{
                                val tmpid = if (folderProduct.level == 2 || arrDbFolder.isEmpty()) mainid else {
                                    arrFolder[folderProduct.parentRow].id
                                }
                                map.put(Columns._mainid.name, tmpid)
                                fp.mainid = tmpid
                                folderProduct.mainid = tmpid
                            } catch(_: Exception){
                                map.put(Columns._mainid.name, mainid)
                                fp.mainid = mainid
                                folderProduct.mainid = mainid
                            }
                            fp.id = insertPrimaryKey(map).id
                            folderProduct.id = fp.id

                            fp.isChecked = folderProduct.isChecked
                            fp.folder = folderProduct.folder
                            fp.level = folderProduct.level
                            arrDbFolder.add(fp)
                            rtn.rowsaffected=1
                        }
                    }
                }
                else -> {
                    arrDbFolder[0].inUse = true
                    arrFolder.forEach { folderProduct ->
                        val map = ContentValues()
                        map.put(Columns.seqno.name, if(folderProduct.isChecked) -1 else 0)
                        if (folderProduct.id == 0){
                            map.put(Columns.title.name, folderProduct.title.trim())
                            map.put(Columns.description.name, folderProduct.folder.trim())
                            map.put(Columns.level.name, folderProduct.level)
                            map.put(Columns.type.name,ConstantsLocal.TYPE_DIRECTORY)
                            map.put(Columns._mainid.name,arrFolder[folderProduct.parentRow].id)
                            folderProduct.id = insertPrimaryKey(map).id
                            rtn.rowsaffected=1
                        } else {
                            map.put(Columns._id.name, folderProduct.id)
                            updatePrimaryKey(map)
                        }
                        arrDbFolder.find{it.folder == folderProduct.folder}?.inUse = true
                    }
                    arrDbFolder.filter { !it.inUse && it.id > 0 }.forEach {
                        // db-folder doesn't exists anymore
                        var map = ContentValues()
                        map.put(Columns._id.name, it.id)
                        deletePrimaryKey(map)
                        // disconnect product from folder
                        map = ContentValues()
                        map.put(TableProduct.Columns._categoryid.name, 0)
                        product.updateWhere("${TableProduct.Columns._id.name} != $productid and ${TableProduct.Columns._categoryid.name} = ${it.id}",map)

                        rtn.rowsaffected=1}
                }
            }
        }

        return rtn
    }

    fun getSubCategory(productid: Int, mainid:Int, title: String,
                       arrFolder: MutableList<FolderProduct>): ReturnValue {
        val product = TableProduct(contextLocal)
        val productrel = TableProductRel()
        //fillFolder( mainid , arrFolder)
        val sql = "WITH RECURSIVE generation AS (" +
                " SELECT ${Columns._id.name}, ${Columns._mainid.name}, ${Columns.level.name}, ${Columns.seqno.name}, ${Columns.description.name} FROM $TABLE_NAME" +
                " WHERE ${Columns._mainid.name} = $mainid" +
                " UNION" +
                " SELECT child.${Columns._id.name}, child.${Columns._mainid.name}, child.${Columns.level.name}, child.${Columns.seqno.name}, child.${Columns.description.name}" +
                " FROM $TABLE_NAME child" +
                " JOIN generation g ON g.${Columns._id.name} = child.${Columns._mainid.name})" +
                " SELECT ${Columns._id.name}, ${Columns._mainid.name}, ${Columns.level.name}, ${Columns.seqno.name}, ${Columns.description.name}" +
                " FROM $TABLE_NAME" +
                " where _id = $mainid" +
                " union" +
                " SELECT ${Columns._id.name}, ${Columns._mainid.name}, ${Columns.level.name}, ${Columns.seqno.name}, ${Columns.description.name}" +
                " FROM generation"
        val rtn = rawQuery(sql, null)
        val arrDbFolder: MutableList<FolderProduct> = mutableListOf()
        if (rtn.cursor.moveToFirst()) {
            do {
                val fp = FolderProduct()
                fp.id = rtn.cursor.getString(0).toInt()
                fp.mainid = rtn.cursor.getString(1).toInt()
                fp.level = rtn.cursor.getString(2).toInt()
                fp.isChecked = (rtn.cursor.getString(3).toInt() == -1)
                fp.folder = if (rtn.cursor.getString(4) == null) "" else rtn.cursor.getString(4)
                fp.title = fp.folder
                arrDbFolder.add(fp)
            } while (rtn.cursor.moveToNext())
        }
        rtn.cursorClose()

        if (arrDbFolder.isNotEmpty() && arrFolder.isNotEmpty()){
            arrFolder[0].id = arrDbFolder[0].id
            arrFolder[0].mainid = arrDbFolder[0].mainid
            //arrFolder[0].level = arrDbFolder[0].level
            //arrFolder[0].isChecked = arrDbFolder[0].isChecked
        }

        rtn.rowsaffected = 0
        when (arrFolder.size){
            0 -> {
                // folder does not exists
            }
            1 -> {
                if (arrDbFolder.size == 0){
                    // problem
                    Logging.d("no default folder in database")
                    rtn.returnValue = false
                } else if (arrDbFolder.size == 1){
                    // 1 folder no sub folders
                    if (arrFolder[0].folder.equals(arrDbFolder[0].folder,true)){
                        // nothing changed in root folder
                    } else {
                        // folder does nog match with database
                        arrFolder[0].title = arrDbFolder[0].title
                        var map = ContentValues()
                        map.put(Columns._id.name, arrDbFolder[0].id)
                        map.put(Columns.title.name, title.trim())
                        map.put(Columns.description.name, arrFolder[0].folder.trim())
                        updatePrimaryKey(map)

                        map = ContentValues()
                        map.put(TableProduct.Columns._id.name, productid)
                        map.put(TableProduct.Columns.dirname.name, arrFolder[0].folder)
                        product.updatePrimaryKey(map)

                        // disconnect product from folder
                        map = ContentValues()
                        map.put(TableProduct.Columns._categoryid.name, 0)
                        product.updateWhere("${TableProduct.Columns._id.name} != $productid and ${TableProduct.Columns._categoryid.name} = ${arrDbFolder[0].id}",map)

                        productrel.deleteTableRow(0,0,arrDbFolder[0].id)
                        rtn.rowsaffected=1
                    }
                } else {

                    // cleanup productrel, product
                    if (arrFolder[0].folder.equals(arrDbFolder[0].folder,true)){
                        // nothing changed in root folder
                    } else {
                        // folder does nog match with database
                        arrFolder[0].title = arrDbFolder[0].title
                        var map = ContentValues()
                        map.put(Columns._id.name, arrDbFolder[0].id)
                        map.put(Columns.title.name, title.trim())
                        map.put(Columns.description.name, arrFolder[0].folder.trim())
                        updatePrimaryKey(map)
                        map = ContentValues()
                        map.put(TableProduct.Columns._id.name, productid)
                        map.put(TableProduct.Columns.dirname.name, arrFolder[0].folder)
                        product.updatePrimaryKey(map)

                        // disconnect product from folder
                        map = ContentValues()
                        map.put(TableProduct.Columns._categoryid.name, 0)
                        product.updateWhere("${TableProduct.Columns._id.name} != $productid and ${TableProduct.Columns._categoryid.name} = ${arrDbFolder[0].id}",map)

                        productrel.deleteTableRow(0,0,arrDbFolder[0].id)
                        rtn.rowsaffected=1
                    }
                    arrDbFolder.forEachIndexed { index, fp ->
                        if (index > 0){
                            var map = ContentValues()
                            map.put(Columns._id.name, fp.id)
                            deletePrimaryKey(map)

                            // disconnect product from folder
                            map = ContentValues()
                            map.put(TableProduct.Columns._categoryid.name, 0)
                            product.updateWhere("${TableProduct.Columns._id.name} != $productid and ${TableProduct.Columns._categoryid.name} = ${fp.id}",map)

                            rtn.rowsaffected=1
                        }
                    }
                }
            }
            else ->{
                when (arrDbFolder.size){
                    0 -> {
                        // no registration of sub folders
                        val mainid1 = DBUtils.eLookUpInt(Columns._id.name, TABLE_NAME,
                            "${Columns._mainid.name} = 0 " +
                                    " and ${Columns.type.name} = ${ConstantsLocal.TYPE_DIRECTORY} " +
                                    " and ${Columns.level.name} = 1" )
                        arrFolder.forEach {
                            val map = ContentValues()
                            val fp = FolderProduct()
                            map.put(Columns.title.name, it.title.trim())
                            map.put(Columns.description.name, it.folder.trim())
                            map.put(Columns.seqno.name, if(it.isChecked) -1 else 0)
                            map.put(Columns.level.name, it.level)
                            val tmpid = if (it.level == 2 || arrDbFolder.isEmpty()) mainid1 else {
                                arrFolder[it.parentRow].id
                            }
                            map.put(Columns._mainid.name, tmpid)
                            map.put(Columns.type.name,ConstantsLocal.TYPE_DIRECTORY)

                            fp.id = insertPrimaryKey(map).id
                            fp.id = tmpid
                            fp.isChecked = it.isChecked
                            fp.folder = it.folder
                            fp.title = it.title
                            fp.level = it.level
                            arrDbFolder.add(fp)
                            it.id = fp.id
                            rtn.rowsaffected=1
                        }
                    }
                    1 -> {
                        // change folder to multiple folder
                        if (arrFolder[0].folder.equals(arrDbFolder[0].folder,true)){
                            // nothing changed in root folder
                        } else {
                            arrFolder[0].title = arrDbFolder[0].title
                            // folder does not match with database
                            var map = ContentValues()
                            map.put(Columns._id.name, arrDbFolder[0].id)
                            map.put(Columns.title.name, title.trim())
                            map.put(Columns.description.name, arrFolder[0].folder.trim())
                            updatePrimaryKey(map)

                            map = ContentValues()
                            map.put(TableProduct.Columns._id.name, productid)
                            map.put(TableProduct.Columns.dirname.name, arrFolder[0].folder)
                            product.updatePrimaryKey(map)

                            // disconnect product from folder
                            map = ContentValues()
                            map.put(TableProduct.Columns._categoryid.name, 0)
                            product.updateWhere("${TableProduct.Columns._id.name} != $productid and ${TableProduct.Columns._categoryid.name} = ${arrDbFolder[0].id}",map)

                            productrel.deleteTableRow(0,0,arrDbFolder[0].id)
                            rtn.rowsaffected=1
                        }
                        arrFolder.forEachIndexed { index, folderProduct ->
                            if (index > 0) {
                                // insert missing sub-folders
                                val fp = FolderProduct()
                                val map = ContentValues()
                                map.put(Columns.title.name, folderProduct.title.trim())
                                map.put(Columns.description.name, folderProduct.folder.trim())
                                map.put(Columns.level.name, folderProduct.level)
                                map.put(Columns.seqno.name, if(folderProduct.isChecked) -1 else 0)
                                map.put(Columns.type.name,ConstantsLocal.TYPE_DIRECTORY)
                                try{
                                    val tmpId = if (folderProduct.level == 2 || arrDbFolder.isEmpty()) mainid else {
                                        arrFolder[folderProduct.parentRow].id
                                    }
                                    map.put(Columns._mainid.name, tmpId)
                                    fp.id = tmpId
                                } catch(_: Exception){
                                    map.put(Columns._mainid.name, mainid)
                                    fp.id = mainid
                                }
                                fp.id = insertPrimaryKey(map).id
                                folderProduct.id = fp.id
                                fp.isChecked = folderProduct.isChecked
                                fp.folder = folderProduct.folder
                                fp.level = folderProduct.level
                                arrDbFolder.add(fp)
                                rtn.rowsaffected=1
                            }
                        }
                    }
                    else -> {
                        arrDbFolder[0].inUse = true
                        arrFolder.forEachIndexed { index, folderProduct ->
                            if (index > 0){
                                arrDbFolder.filter { folderProduct.folder.equals(it.folder,true)}.forEach {
                                    folderProduct.id = it.id
                                    folderProduct.isChecked = it.isChecked
                                    folderProduct.level = it.level
                                    folderProduct.mainid = it.mainid
                                    folderProduct.title = it.title
                                    it.inUse = true
                                }
                                if (folderProduct.id == 0){
                                    val map = ContentValues()
                                    map.put(Columns.title.name, folderProduct.title.trim())
                                    map.put(Columns.description.name, folderProduct.folder.trim())
                                    map.put(Columns.level.name, folderProduct.level)
                                    map.put(Columns.seqno.name, -1)
                                    map.put(Columns.type.name,ConstantsLocal.TYPE_DIRECTORY)
                                    val tmpid = if (folderProduct.level == 2 || arrDbFolder.size == 0) mainid else {
                                        arrFolder[folderProduct.parentRow].id
                                    }
                                    map.put(Columns._mainid.name, tmpid)
                                    folderProduct.id = insertPrimaryKey(map).id
                                    rtn.rowsaffected=1
                                }
                            }
                        }
                        arrDbFolder.filter { !it.inUse && it.id > 0 }.forEach {
                            // db-folder doesn't exists anymore
                            var map = ContentValues()
                            map.put(Columns._id.name, it.id)
                            deletePrimaryKey(map)
                            // disconnect product from folder
                            map = ContentValues()
                            map.put(TableProduct.Columns._categoryid.name, 0)
                            product.updateWhere("${TableProduct.Columns._id.name} != $productid and ${TableProduct.Columns._categoryid.name} = ${it.id}",map)

                            rtn.rowsaffected=1}
                    }
                }
            }
        }

        return rtn
    }

    override fun insertPrimaryKey(values: ContentValues): ReturnValue {
        val mainid = values.getAsInteger(Columns._mainid.name)
        DBUtils.trimColumnValue(values, Columns.description.name)
        DBUtils.trimColumnValue(values, Columns.title.name)

        if (mainid == null || mainid == 0) {
            // possible error in program
            values.put(Columns.level.name, 1)
            values.put(Columns._mainid.name, 0)
        } else {
            var level = values.getAsInteger(Columns.level.name)
            if (level == null || level == 0){
                level = CalcObjects.objectToInteger(
                    DBUtils.eLookUp("${Columns.level.name}+1", TABLE_NAME, "${Columns._id.name}=$mainid"))
                values.put(Columns.level.name, level)
            }
        }
        if (values.getAsInteger(Columns.seqno.name) == 0){
            values.put(Columns.seqno.name, 0)
        }
        return super.insertPrimaryKey(values)
    }

    private fun getSubCategory(mainid: Int): IdDescription {
        val idDescription = IdDescription()
        val sql = "WITH RECURSIVE generation AS (" +
                " SELECT ${Columns._id.name}, ${Columns._mainid.name}, ${Columns.description.name} FROM $TABLE_NAME" +
                " WHERE ${Columns._mainid.name} = $mainid" +
                " UNION" +
                " SELECT child.${Columns._id.name}, child.${Columns._mainid.name}, child.${Columns.description.name}" +
                " FROM $TABLE_NAME child" +
                " JOIN generation g ON g.${Columns._id.name} = child.${Columns._mainid.name})" +
                " SELECT group_concat(${Columns._id.name}), group_concat(${Columns.description.name}) as result" +
                " FROM generation group by ${Columns._mainid.name}"
        val rtn = rawQuery(sql, null)
        if (rtn.cursor.moveToFirst()) {
            do {
                idDescription.id.append(rtn.cursor.getString(0)).append(",")
                idDescription.description.append(rtn.cursor.getString(1)).append(",")
            } while (rtn.cursor.moveToNext())
        }
        var idx = idDescription.id.lastIndexOf(",")
        if (idx>0){
            idDescription.id.deleteCharAt(idx)
            idx = idDescription.description.lastIndexOf(",")
            idDescription.description.deleteCharAt(idx)
        }
        rtn.cursorClose()
        return idDescription
    }

    fun getWhere(where: String, order: String=""): ReturnValue {
        var sql = "SELECT " + TextUtils.join(",",dbColumns) +
                " FROM $TABLE_NAME where $where"
        if (order.isNotEmpty()){
            sql += " order by $order"
        }
        return rawQuery(sql, null)
    }

    fun renumLevel(){

        val sql = "SELECT ${Columns._mainid}, group_concat(${Columns._id}) as ${Columns._id}" +
                " FROM $TABLE_NAME" +
                " GROUP BY ${Columns._mainid}"
        val rtn = rawQuery(sql, null)
        val arrDbFolder: MutableList<FolderProduct> = mutableListOf()

        if (rtn.cursor.moveToFirst()) {
            do {
                val mainid = rtn.cursor.getString(0).toInt()
                val arrId = rtn.cursor.getString(1).toString().split(",")
                arrId.forEach {
                    val fp = FolderProduct()
                    fp.mainid = mainid
                    fp.id = it.toInt()
                    fp.level = 0
                    arrDbFolder.add(fp)
                }
            } while (rtn.cursor.moveToNext())
        }
        rtn.cursorClose()
        arrDbFolder.filter { it.mainid == 0 }.forEach { it1 ->
            it1.level = 1
            renumSubLevel(it1.id, 1, arrDbFolder)
        }
        val temp: MutableMap<Int, String> = HashMap()
        arrDbFolder.forEach {
            if (temp[it.level] == null) temp[it.level] =""
            temp[it.level] += "${it.id},"
        }
        temp.forEach { (t, u) ->
            val map = ContentValues()
            map.put(Columns.level.name, t)
            updateWhere(Columns._id.name + " in (" + u.dropLast(1) + ") and level != $t", map)
        }
    }

    private fun renumSubLevel(id: Int, level: Int, arrDbFolder: MutableList<FolderProduct>){
        arrDbFolder.filter { it.mainid == id } .forEach {
            it.level = level+1
            renumSubLevel(it.id, it.level, arrDbFolder)
        }
    }
}