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
import android.location.Address
import androidx.collection.ArrayMap
import com.farrusco.filecatalogue.R
import com.farrusco.filecatalogue.common.CategoryDetail
import com.farrusco.filecatalogue.common.CategoryLine
import com.farrusco.filecatalogue.common.ConstantsLocal
import com.farrusco.filecatalogue.tables.TableCategory
import com.farrusco.filecatalogue.tables.TableProduct
import com.farrusco.filecatalogue.tables.TableProductRel
import com.farrusco.projectclasses.databases.tables.CursorX.getColumnValueInt
import com.farrusco.projectclasses.databases.tables.CursorX.getColumnValueString
import com.farrusco.projectclasses.databases.tables.DBUtils
import com.farrusco.projectclasses.utils.CalcObjects
import com.farrusco.projectclasses.utils.FolderProduct
import com.farrusco.projectclasses.utils.ReturnValue
import com.farrusco.projectclasses.utils.ViewUtils
import java.io.File

class Category(private val context: Context): TableCategory(context) {
    private var seqno = 0
    private var tblProductRel: ProductRel? = null
        get(){
            if ( field != null) return field
            field = ProductRel(context)
            return field
        }

    companion object{
        // only used by SearchPhotos
        var arrCategoryTitle: ArrayList<CategoryLine> = ArrayList()
    }

    private var arrCatProd: ArrayList<Pair<Int,String>> = ArrayList()

    private var arrLocalCategoryDynamic: ArrayList<CategoryLine> = ArrayList()

    fun fillCategoryArray(types: List<Int>, inclCount: Boolean){
        arrCategoryTitle = fillCategoryArray(types, true, inclCount)
    }

    fun initArray(){
        arrLocalCategoryDynamic = ArrayList()
    }

    fun fillCategoryArray(types: List<Int>, inclSeqno: Boolean, inclCount: Boolean): ArrayList<CategoryLine>{
        val arrCat = ArrayList<CategoryLine>()
        var where = ""
        if (types.size>1) {
            where = types.joinToString(",")
        } else if (types.size == 1 && types[0] != ConstantsLocal.TYPE_ALL) {
            where += "${types[0]}"
        }
        val rtn = getCategoryType(where)
        if (rtn.cursor.moveToFirst()) {
            do {
                val categoryLine = CategoryLine()

                categoryLine.id = rtn.cursor.getColumnValueInt(Columns._id.name,0)
                categoryLine.title = rtn.cursor.getColumnValueString(Columns.title.name).toString()

                categoryLine.mainid = rtn.cursor.getColumnValueInt(Columns._mainid.name,0)
                categoryLine.level = rtn.cursor.getColumnValueInt(Columns.level.name,0)
                categoryLine.checked = 0
                categoryLine.checkedCurr = 0
                categoryLine.type = rtn.cursor.getColumnValueString(Columns.type.name)!!.toInt()

                arrCat.add(categoryLine)
            } while (rtn.cursor.moveToNext())
        }
        rtn.cursorClose()
        if (inclSeqno) {
            seqno = 0
            makeSeqno(0, 1, arrCat)
            // sort array
            arrCat.sortBy{ it.seqno }
        }

        if (inclCount) {
            arrCat.forEach { itMain ->
                itMain.hasChild = arrCat.find { it.mainid == itMain.id }?.id != null
            }
            fillCountFiles(where, arrCat)
            var level = 0
            do {
                level++
            } while (countLevel(level, arrCat))
        }
        return arrCat
    }

    private fun fillCountFiles(type: String, arrCat: ArrayList<CategoryLine>){
        var sql = "select pos_product_rel._categoryid, count(distinct pos_product._id) as count" +
                " from pos_product" +
                " join pos_product_rel on pos_product_rel._productid = pos_product._id"
        var where = " where pos_product.type = 3"
        if (ConstantsLocal.isSearchFilesAvailableEnabled){
            if (!ConstantsLocal.isSearchFilesHiddenEnabled){
                where += " AND " + TableProduct.Columns.blocked + " = 0"
            }
        } else {
            where += " AND " + TableProduct.Columns.blocked + " = "
            where += if (ConstantsLocal.isSearchFilesHiddenEnabled){
                "-1"
            } else {
                // conflict
                "9"
            }
        }
        if (type != "" && type != ConstantsLocal.TYPE_ALL.toString()) {
            sql += " join $TABLE_NAME on $TABLE_NAME._id = pos_product_rel._categoryid"
            where += " and $TABLE_NAME.${Columns.type.name} in ( $type )"
        }
        sql += "$where group by pos_product_rel._categoryid"

        val rtn = rawQuery(sql,null)
        if (rtn.cursor.moveToFirst()) {
            do {
                val id = getInt(rtn.cursor, TableProductRel.Columns._categoryid.name)
                val count = getInt(rtn.cursor, "count")
                arrCat.find { it.id == id }?.count = count

            } while (rtn.cursor.moveToNext())
        }
        rtn.cursorClose()
        arrCat.filter { it.count > 0 }.forEach { it.countCurr = it.count }
    }

    private fun countLevel(level: Int, arrCat: ArrayList<CategoryLine>): Boolean{
        var ids = ""
        var rtn = false
        var idx = 0
        arrCat.filter{it.level == level && it.hasChild}.forEach{ itMain ->
            // collect all child category pro level
            rtn=true
            itMain.count=0
            idx++
            ids += "$idx,${itMain.id},"
            var child = "${itMain.id},"
            var arrChild: List<String>
            do{
                arrChild = child.substring(0,child.length-1).split(",")
                child=""
                arrChild.forEach { itChild ->
                    val id = itChild.toInt()
                    // if checked all child are included
                    arrCat.filter{ it.mainid == id}.forEach{
                        ids += "$idx,${it.id},"
                        if (it.hasChild) child += "${it.id},"
                    }
                }
            } while (child.isNotEmpty())
        }
        if (rtn) {
            // group together as a sql 'AND' condition
            // each level get all main and child category
            val arrIds = ids.split(",")
            val mapIds = ArrayMap<String, String>()
            for (idx1 in 0 until arrIds.size-1 step 2){
                // every element is an 'OR'
                if (mapIds.containsKey(arrIds[idx1])){
                    val keys = mapIds[arrIds[idx1]] + "," + arrIds[idx1+1]
                    mapIds.replace(arrIds[idx1],keys)
                } else{
                    @Suppress("ReplacePutWithAssignment")
                    mapIds.put(arrIds[idx1],arrIds[idx1+1])
                }
            }
            if (arrCatProd.isEmpty()){
                fillCatProdCount()
            }
            mapIds.onEach { itIds ->
                var count = 0
                val idsX = itIds.value.split(",")
                var prodDone = ","
                idsX.forEach { idSingle ->
                    arrCatProd.filter { idSingle.toInt() == it.first }.forEach {
                        if (prodDone == ","){
                            count = it.second.split(",").size
                            prodDone += ",${it.second},"
                        } else {
                            val arrProd = it.second.split(",")
                            arrProd.filterNot { prods -> prodDone.contains(",${prods},") }
                                .forEach { prods ->
                                    count++
                                    // don't count duplicate products
                                    prodDone += "${prods},"
                                }
                        }
                    }
                }
                if (count > 0) {
                    arrCat.filter { it.id == idsX[0].toInt() }.forEach{
                        it.count += count.toString().toInt()
                    }
                }
            }
        }
        return rtn
    }

    private fun fillCatProdCount(){

        val sql = "select POS_PRODUCT_REL._categoryid, group_concat(POS_PRODUCT_REL._productid) as ids " +
            " FROM POS_PRODUCT"+
            " JOIN POS_PRODUCT_REL ON pos_product._id = _productid"
        var where  = " WHERE pos_product.type = 3"
        if (ConstantsLocal.isSearchFilesAvailableEnabled){
            if (!ConstantsLocal.isSearchFilesHiddenEnabled){
                where += " AND " + TableProduct.Columns.blocked + " = 0"
            }
        } else {
            where += " AND " + TableProduct.Columns.blocked + " = "
            where += if (ConstantsLocal.isSearchFilesHiddenEnabled){
                "-1"
            } else {
                // conflict
                "9"
            }
        }
         val rtn = rawQuery( "$sql $where group by POS_PRODUCT_REL._categoryid", null)
        if (rtn.cursor.moveToFirst()) {
            do {
                val categoryid = rtn.cursor.getColumnValueInt(TableProductRel.Columns._categoryid.name)!!
                val productid = rtn.cursor.getColumnValueString("ids")!!
                arrCatProd.add(Pair( categoryid,productid) )
            } while (rtn.cursor.moveToNext())
        }
        rtn.cursorClose()

    }

    private fun makeSeqno(mainid: Int, level: Int, arrCategorySeqno: ArrayList<CategoryLine>): Boolean{
        var rtn = false
        for(idx1 in 0 until arrCategorySeqno.size) {
            if (arrCategorySeqno[idx1].mainid == mainid
                && arrCategorySeqno[idx1].level == level
                && arrCategorySeqno[idx1].seqno == 0){
                arrCategorySeqno[idx1].seqno = ++seqno
                rtn = true
                arrCategorySeqno[idx1].hasChild = makeSeqno(arrCategorySeqno[idx1].id, level+1, arrCategorySeqno)
            }
        }
        return rtn
    }

    fun getCategoryCodes(productid: Int, types: ArrayList<Int>): MutableList<CategoryDetail>{
        // category, level
        val productRel = ProductRel(context)
        val rtn = productRel.getCategoryFromProduct(productid)
        var categoryId = "-1"
        if (rtn.cursor.moveToFirst()) {
            categoryId = rtn.cursor.getColumnValueString(TableProductRel.Columns._categoryid.name,"")!!
        }
        rtn.cursorClose()
        if (categoryId.isNotEmpty()){
            return fillCategoryMut(categoryId.split(",").map { it.toInt() }, types)
        }
        return mutableListOf()
    }

    fun fillCategoryMut(strRel: List<Int>, types: List<Int>): MutableList<CategoryDetail>{
        if (arrLocalCategoryDynamic.isEmpty()){
            arrLocalCategoryDynamic = fillCategoryArray(types, inclSeqno = false, inclCount = false)
        }
        val arrCategoryMut: MutableList<CategoryDetail> = mutableListOf()
        for(idx in 0 until arrLocalCategoryDynamic.size){
            val category = CategoryDetail()
            category.categoryid = arrLocalCategoryDynamic[idx].id
            category.level = arrLocalCategoryDynamic[idx].level
            category.type = arrLocalCategoryDynamic[idx].type

            category.checked = if (arrLocalCategoryDynamic[idx].id in strRel){
                -1
            } else {
                0
            }
            category.checkedOld=category.checked
            arrCategoryMut.add(category)
        }
        return arrCategoryMut
    }

    private fun getCreateMainId(): Int {
        if (ConstantsLocal.createMainId < 0) {
            ConstantsLocal.createMainId = CalcObjects.objectToInteger(
                DBUtils.eLookUp(
                    Columns._id.name, TABLE_NAME,
                    "${Columns._mainid.name} = 0" +
                            " and ${Columns.type.name} = ${ConstantsLocal.TYPE_CREATION}"
                )
            )
        }
        if (ConstantsLocal.createMainId == 0) {
            val map = ContentValues()
            map.put(Columns._id.name, 0)
            map.put(Columns._mainid.name, 0)
            map.put(Columns.seqno.name, 1)
            map.put(Columns.level.name, 1)
            map.put(Columns.type.name, ConstantsLocal.TYPE_CREATION)
            map.put(Columns.title.name, context.getString(R.string.creation))
            map.put(Columns.description.name, context.getString(R.string.creation))
            val rtnMain = insertPrimaryKey(map)
            ConstantsLocal.createMainId = rtnMain.id
        }
        return ConstantsLocal.createMainId
    }

    private fun getExtensionMainId(): Int {
        if (ConstantsLocal.extensionMainId > 0){
            return ConstantsLocal.extensionMainId
        }

        ConstantsLocal.extensionMainId = CalcObjects.objectToInteger(
            DBUtils.eLookUp(
                Columns._id.name, TABLE_NAME,
                "${Columns._mainid.name} = 0" +
                        " and ${Columns.type.name} = ${ConstantsLocal.TYPE_FILE_EXTENSION}"
            )
        )

        if (ConstantsLocal.extensionMainId == 0) {
            val map = ContentValues()
            map.put(Columns._id.name, 0)
            map.put(Columns._mainid.name, 0)
            map.put(Columns.seqno.name, 1)
            map.put(Columns.level.name, 1)
            map.put(Columns.type.name, ConstantsLocal.TYPE_FILE_EXTENSION)
            map.put(Columns.title.name, context.getString(R.string.fileextension))
            map.put(Columns.description.name, context.getString(R.string.fileextension))
            val rtnMain = insertPrimaryKey(map)
            ConstantsLocal.extensionMainId = rtnMain.id
        }
        return ConstantsLocal.extensionMainId
    }

    fun getCreateNewId(): Int{

        if (!ConstantsLocal.isAutoNewEnabled) return 0
        if (ConstantsLocal.createNewId > 0) return ConstantsLocal.createNewId

        ConstantsLocal.createNewId = DBUtils.eLookUpInt(Columns._id.name, TABLE_NAME, "${Columns.type.name} = ${ConstantsLocal.TYPE_AUTOMATED_NEW}")
        if (ConstantsLocal.createNewId > 0) return ConstantsLocal.createNewId

        getCreateMainId()

        val map = ContentValues()
        map.put(Columns._id.name,0)
        map.put(Columns._mainid.name,ConstantsLocal.createMainId)
        map.put(Columns.seqno.name,1)
        map.put(Columns.level.name,2)
        map.put(Columns.type.name,ConstantsLocal.TYPE_AUTOMATED_NEW)
        map.put(Columns.title.name,context.getString(R.string.newfile))
        map.put(Columns.description.name,context.getString(R.string.newfile))
        val rtnNew = insertPrimaryKey(map)

        ConstantsLocal.createNewId =  rtnNew.id
        return ConstantsLocal.createNewId
    }

    fun creationDate(fileProduct: FolderProduct,
                     arrCategoryCreation: ArrayList<CategoryLine>, dateId:String){
        /*
        build tree for creationDate
         */
        // creationTime:2010-11-11T17:25:48Z
        val creationTime = fileProduct.creationDateStr()
        if (creationTime.length<10) return
        var categoryLine: CategoryLine
        val monthId: Int
        var map: ContentValues
        val sYear = creationTime.substring(0,4)
        val sMonth = creationTime.substring(5,7)

        val lineCreationYear = arrCategoryCreation.find{ it.title == sYear && it.mainid == getCreateMainId()}
        if (lineCreationYear == null){
            map = ContentValues()
            map.put(Columns._id.name,0)
            map.put(Columns._mainid.name,getCreateMainId())
            map.put(Columns.seqno.name, sYear.toInt())
            map.put(Columns.type.name, ConstantsLocal.TYPE_CREATION)
            map.put(Columns.title.name, sYear)
            map.put(Columns.level.name, 2)
            val rtnYear = insertPrimaryKey(map)

            categoryLine = CategoryLine()
            categoryLine.id = rtnYear.id
            categoryLine.title = sYear
            categoryLine.mainid = getCreateMainId()
            categoryLine.level = 2
            categoryLine.type=ConstantsLocal.TYPE_CREATION
            arrCategoryCreation.add(categoryLine)

            map = ContentValues()
            map.put(Columns._id.name,0)
            map.put(Columns._mainid.name,rtnYear.id)
            map.put(Columns.seqno.name, sMonth.toInt())
            map.put(Columns.type.name,ConstantsLocal.TYPE_CREATION)
            map.put(Columns.title.name,sMonth)
            map.put(Columns.description.name,sMonth)
            map.put(Columns.level.name, 3)
            val rtnMonth = insertPrimaryKey(map)

            monthId=rtnMonth.id
            categoryLine = CategoryLine()
            categoryLine.id = monthId
            categoryLine.title = sMonth
            categoryLine.mainid = rtnYear.id
            categoryLine.level = 3
            categoryLine.type=ConstantsLocal.TYPE_CREATION
            arrCategoryCreation.add(categoryLine)
        } else {
            val lineCreationMonth = arrCategoryCreation.find {
                it.title == creationTime.substring(
                    5,
                    7
                ) && it.mainid == lineCreationYear.id
            }
            if (lineCreationMonth == null) {
                val mnd = creationTime.substring(5, 7)
                map = ContentValues()
                map.put(Columns._id.name, 0)
                map.put(Columns._mainid.name, lineCreationYear.id)
                map.put(Columns.seqno.name, mnd)
                map.put(Columns.type.name, ConstantsLocal.TYPE_CREATION)
                map.put(Columns.title.name, mnd)
                map.put(Columns.description.name, mnd)
                map.put(Columns.level.name, 3)
                val rtnMonth = insertPrimaryKey(map)

                monthId = rtnMonth.id
                categoryLine = CategoryLine()
                categoryLine.id = monthId
                categoryLine.title = sMonth
                categoryLine.mainid = lineCreationYear.id
                categoryLine.level = 3
                categoryLine.type = ConstantsLocal.TYPE_CREATION
                arrCategoryCreation.add(categoryLine)
            } else {
                monthId = lineCreationMonth.id
            }
        }
        if (",$dateId,".indexOf(",$monthId,")<0){
            val tblTableProductRel = TableProductRel()
            tblTableProductRel.insertProductRel(fileProduct.id, monthId)
        }
    }

    fun fileExtension(fileProduct: FolderProduct,
                     arrExtension: ArrayList<CategoryLine>){
        /*
        build tree for  TYPE_FILE_EXTENSION
         */

        val empty = context.getText(R.string.empty)
        var extensionName = File(fileProduct.filename).extension
        if (extensionName == "") {
            extensionName = empty.toString()
        }
        val categoryLine: CategoryLine
        val map: ContentValues
        val extensionId:Int
        val mainId = getExtensionMainId()

        val tmpExtension = arrExtension.find{ it.title.equals(extensionName,true)  && it.mainid == mainId}
        if (tmpExtension == null){
            map = ContentValues()
            map.put(Columns._id.name,0)
            map.put(Columns._mainid.name,mainId)
            map.put(Columns.seqno.name,2)
            map.put(Columns.level.name,2)
            map.put(Columns.type.name,ConstantsLocal.TYPE_FILE_EXTENSION)
            map.put(Columns.title.name,extensionName)
            map.put(Columns.description.name,extensionName)
            val rtnExtension = insertPrimaryKey(map)
            extensionId = rtnExtension.id

            categoryLine = CategoryLine()
            categoryLine.id = rtnExtension.id
            categoryLine.title = extensionName
            categoryLine.mainid = mainId
            categoryLine.level = 2
            categoryLine.type=ConstantsLocal.TYPE_FILE_EXTENSION
            arrExtension.add(categoryLine)
        } else {
            extensionId = tmpExtension.id
        }

        tblProductRel!!.insertProductRelMultiple(fileProduct.id, extensionId)
    }

    private fun gpsLocationInsertTitle(mainid: Int, level: Int, title: String, arrCategoryGPS: ArrayList<CategoryLine>):Int{
        val lineCategory = arrCategoryGPS.find {  it.title == title && it.mainid == mainid && it.type == ConstantsLocal.TYPE_GPS}
        if (lineCategory == null) {
            val map = ContentValues()
            map.put(Columns._id.name, 0)
            map.put(Columns._mainid.name, mainid)
            map.put(Columns.seqno.name, 1)
            map.put(Columns.level.name, level)
            map.put(Columns.type.name, ConstantsLocal.TYPE_GPS)
            map.put(Columns.title.name, title)
            map.put(Columns.description.name, title)
            val rtnMain = insertPrimaryKey(map)

            val categoryLine = CategoryLine()
            map.put(Columns._id.name,rtnMain.id)
            map.put(Columns.level.name,level)
            categoryLine.setMap(map)
            arrCategoryGPS.add(categoryLine)
            return rtnMain.id
        }
        return lineCategory.id
    }

    private fun gpsLocationInsertDescription(mainid: Int, level: Int, description: String, arrCategoryGPS: ArrayList<CategoryLine>): Int {

        val lineCategory =
            arrCategoryGPS.find { it.title == description && it.mainid == mainid }
        if (lineCategory == null) {
            val map = ContentValues()
            map.put(Columns._id.name, 0)
            map.put(Columns._mainid.name, mainid)
            map.put(Columns.seqno.name, 1)
            map.put(Columns.level.name, level)
            map.put(Columns.type.name, ConstantsLocal.TYPE_GPS)
            map.put(Columns.title.name, description)
            map.put(Columns.description.name, description)
            val rtnMain = insertPrimaryKey(map)

            val categoryLine = CategoryLine()
            map.put(Columns._id.name, rtnMain.id)
            map.put(Columns.level.name, level)
            categoryLine.setMap(map)
            arrCategoryGPS.add(categoryLine)
            return rtnMain.id
        }
        return lineCategory.id
    }

    fun gpsLocationMain(productid: Int, addresses: List<Address>, arrCategoryGPS: ArrayList<CategoryLine>): Boolean {
        if (!ConstantsLocal.isGPSEnabled) return false
        var valueCountryId=0
        var valueId=0

        var step = 1
        do {
            when (step){
                1 -> {
                    valueId = gpsLocationInsertTitle(0,1,
                        context.getString(R.string.gps), arrCategoryGPS )
                    valueId = gpsLocationInsertDescription(valueId,2,
                        addresses[0].countryCode + " - " + addresses[0].countryName, arrCategoryGPS )
                    valueCountryId=valueId
                    step = 2
                }
                2 -> {
                    valueId = gpsLocationInsertDescription(valueId,3,
                        addresses[0].adminArea, arrCategoryGPS )
                    step = 3
                }
                3 -> {
                    valueId = gpsLocationInsertDescription(valueId,4,
                        addresses[0].subAdminArea, arrCategoryGPS )
                    step = 4
                }
                4 -> {
                    valueId = gpsLocationInsertDescription(valueId,5,
                        addresses[0].locality, arrCategoryGPS )
                    step = 5
                }
                5 -> {
                    if (!addresses[0].thoroughfare.isNullOrEmpty()){
                        valueId = gpsLocationInsertDescription(valueId,6,
                            addresses[0].thoroughfare, arrCategoryGPS )
                    }
                    step = 6
                }
                6 -> {
                    valueId = gpsLocationInsertDescription(valueId,7,
                        addresses[0].featureName, arrCategoryGPS )
                    tblProductRel!!.insertProductRelMultiple(productid, valueId)
                    step = 7
                    if (addresses[0].postalCode.isNullOrEmpty()) step = 999
                }
                7 -> {
                    valueId = gpsLocationInsertTitle(valueCountryId,3,
                         context.getString(R.string.gpspostalcode), arrCategoryGPS )
                    val arrCode = addresses[0].postalCode.split(" ")
                    var level = 4
                    if (arrCode.size == 2){
                        valueId = gpsLocationInsertDescription(valueId,4,
                            arrCode[0], arrCategoryGPS )
                        level = 5
                    }
                    valueId = gpsLocationInsertDescription(valueId,level,
                        addresses[0].postalCode, arrCategoryGPS )
                    // houseNumber
                    valueId = gpsLocationInsertDescription(valueId,level+1,
                        addresses[0].featureName, arrCategoryGPS )
                    //TableProductRel().insertProductRel(productid, valueId)
                    tblProductRel!!.insertProductRelMultiple(productid, valueId)
                    step = 999
                }

            }
        } while (step != 999 )
        return true
    }

    fun gpsLocation(productid: Int, section:String, value:String?, seqno:Int, arrCategoryGPS: ArrayList<CategoryLine>): Boolean{
        if (!ConstantsLocal.isGPSEnabled || value == null || value == "") return false
        val valueId: Int
        var categoryLine: CategoryLine

        val lineCategory = arrCategoryGPS.find{ it.level == 1 && it.mainid == 0 && it.type == ConstantsLocal.TYPE_GPS}
        if (lineCategory == null){
            var map = ContentValues()
            map.put(Columns._id.name,0)
            map.put(Columns._mainid.name,0)
            map.put(Columns.seqno.name, 1)
            map.put(Columns.level.name, 1)
            map.put(Columns.type.name,ConstantsLocal.TYPE_GPS)
            map.put(Columns.title.name,context.getString(R.string.gps))
            map.put(Columns.description.name,context.getString(R.string.gps))
            val rtnMain = insertPrimaryKey(map)

            categoryLine = CategoryLine()
            map.put(Columns._id.name,rtnMain.id)
            map.put(Columns.level.name,1)
            categoryLine.setMap(map)
            arrCategoryGPS.add(categoryLine)

            // section level
            map = ContentValues()
            map.put(Columns._id.name,0)
            map.put(Columns._mainid.name,rtnMain.id)
            map.put(Columns.seqno.name,seqno)
            map.put(Columns.level.name, 2)
            map.put(Columns.type.name,ConstantsLocal.TYPE_GPS)
            map.put(Columns.title.name,section)
            map.put(Columns.description.name,section)
            val rtnSection = insertPrimaryKey(map)

            categoryLine = CategoryLine()
            map.put(Columns._id.name,rtnSection.id)
            map.put(Columns.level.name,2)
            categoryLine.setMap(map)
            arrCategoryGPS.add(categoryLine)

            // value level
            map = ContentValues()
            map.put(Columns._id.name,0)
            map.put(Columns._mainid.name,rtnSection.id)
            map.put(Columns.seqno.name,1)
            map.put(Columns.level.name, 3)
            map.put(Columns.type.name,ConstantsLocal.TYPE_GPS)
            map.put(Columns.title.name,value)
            map.put(Columns.description.name,value)
            val rtnValue = insertPrimaryKey(map)
            valueId=rtnValue.id

            categoryLine = CategoryLine()
            map.put(Columns._id.name,rtnValue.id)
            map.put(Columns.level.name,3)
            categoryLine.setMap(map)
            arrCategoryGPS.add(categoryLine)
        } else {

            val lineCreationSection = arrCategoryGPS.find{ it.title == section && it.mainid == lineCategory.id}
            if (lineCreationSection == null){
                var map = ContentValues()
                map.put(Columns._id.name,0)
                map.put(Columns._mainid.name,lineCategory.id)
                map.put(Columns.seqno.name,seqno)
                map.put(Columns.level.name, 2)
                map.put(Columns.type.name,ConstantsLocal.TYPE_GPS)
                map.put(Columns.title.name,section)
                map.put(Columns.description.name,section)
                val rtnSection = insertPrimaryKey(map)

                categoryLine = CategoryLine()
                map.put(Columns._id.name,rtnSection.id)
                map.put(Columns.level.name,2)
                categoryLine.setMap(map)
                arrCategoryGPS.add(categoryLine)

                map = ContentValues()
                map.put(Columns._id.name,0)
                map.put(Columns._mainid.name,rtnSection.id)
                map.put(Columns.seqno.name,1)
                map.put(Columns.level.name, 3)
                map.put(Columns.type.name,ConstantsLocal.TYPE_GPS)
                map.put(Columns.title.name,value)
                map.put(Columns.description.name,value)
                val rtnValue = insertPrimaryKey(map)
                valueId=rtnValue.id

                categoryLine = CategoryLine()
                map.put(Columns._id.name,rtnValue.id)
                map.put(Columns.level.name,3)
                categoryLine.setMap(map)
                arrCategoryGPS.add(categoryLine)
            } else{
                val lineCreationValue = arrCategoryGPS.find{ it.title == value && it.mainid == lineCreationSection.id}
                if (lineCreationValue == null){
                    val map = ContentValues()
                    map.put(Columns._id.name,0)
                    map.put(Columns._mainid.name,lineCreationSection.id)
                    map.put(Columns.seqno.name,1)
                    map.put(Columns.level.name, 3)
                    map.put(Columns.type.name,ConstantsLocal.TYPE_GPS)
                    map.put(Columns.title.name,value)
                    map.put(Columns.description.name,value)
                    val rtnValue = insertPrimaryKey(map)
                    valueId=rtnValue.id

                    categoryLine = CategoryLine()
                    map.put(Columns._id.name,rtnValue.id)
                    map.put(Columns.level.name,3)
                    categoryLine.setMap(map)
                    arrCategoryGPS.add(categoryLine)
                } else{
                    valueId=lineCreationValue.id
                }
            }
        }

        TableProductRel().insertProductRel(productid, valueId)

        return true
    }

    fun saveTypeSeqno(type:Int, seqno:Int, title:String, description:String): ReturnValue{
        var id = -1
        var rtn = getWhere("${Columns.seqno.name} = $seqno" +
                " and ${Columns.type.name} = $type")
        if (rtn.cursor.moveToFirst()){
            do{
                id = ViewUtils.getValueCursor(rtn.cursor, Columns._id.name).toInt()
            } while (rtn.cursor.moveToNext())
        }
        rtn.cursorClose()

        val map = ContentValues()
        map.put(Columns.title.name,title)
        map.put(Columns.description.name,description)
        if (id>0){
            map.put(Columns._id.name,id)
            rtn = updatePrimaryKey(map)
        } else {
            map.put(Columns.seqno.name,seqno)
            map.put(Columns.type.name, type)
            rtn = insertPrimaryKey(map)
        }
        return rtn
    }

    fun loadTypeSeqno(type:Int, seqno:Int): ReturnValue {
        var rtn = ReturnValue()
        rtn.returnValue = false
        if (type == ConstantsLocal.TYPE_SEARCH && !ConstantsLocal.isStoreLastQuery && seqno == 99){
            return rtn
        }
        rtn = getWhere(
            "${Columns.seqno.name} = $seqno and ${Columns.type.name} = $type"
        )
        if (rtn.cursor.moveToFirst()) {
            rtn.mess = ViewUtils.getValueCursor(
                rtn.cursor,
                Columns.description.name
            )
            rtn.returnValue = true
        }
        rtn.cursorClose()
        return rtn
    }
}