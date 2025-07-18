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

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.location.Address
import android.net.Uri
import com.farrusco.filecatalogue.BuildConfig
import com.farrusco.filecatalogue.R
import com.farrusco.filecatalogue.common.CategoryLine
import com.farrusco.filecatalogue.common.ConstantsLocal
import com.farrusco.filecatalogue.common.SystemAttr
import com.farrusco.filecatalogue.tables.TableCategory
import com.farrusco.filecatalogue.tables.TableFace
import com.farrusco.filecatalogue.tables.TableOrderLine
import com.farrusco.filecatalogue.tables.TableProduct
import com.farrusco.filecatalogue.tables.TableProductRel
import com.farrusco.projectclasses.databases.tables.CursorX.getColumnValueInt
import com.farrusco.projectclasses.databases.tables.CursorX.getColumnValueString
import com.farrusco.projectclasses.graphics.BitmapManager
import com.farrusco.projectclasses.graphics.Graphics
import com.farrusco.projectclasses.graphics.face.MediapipeFaceDetector
import com.farrusco.projectclasses.graphics.face.MediapipeFaceDetector.Companion.loadModelFile
import com.farrusco.projectclasses.utils.FilesFolders
import com.farrusco.projectclasses.utils.FilesGPS
import com.farrusco.projectclasses.utils.FolderProduct
import com.farrusco.projectclasses.utils.GPSInfo
import com.farrusco.projectclasses.utils.Logging
import com.farrusco.projectclasses.utils.ReturnValue
import com.farrusco.projectclasses.utils.ViewUtils
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.IOException

class Product(context: Context) : TableProduct(context) {
    private var contextLocal = context
    private var categoryidMain = 0

    var arrCategoryCreation: ArrayList<CategoryLine> = ArrayList()
    var arrFileExtension: ArrayList<CategoryLine> = ArrayList()
    var arrCategoryGPS: ArrayList<CategoryLine> = ArrayList()

    private var tblFace: Face? = null
        get(){
            if ( field != null) return field
            field = Face()
            return field
        }
    private var tblCategory: Category? = null
        get(){
            if ( field != null) return field
            field = Category(contextLocal)
            return field
        }

    private fun fillArrayCategoryCreation(category: Category){
        val arrCatWhere = arrayListOf(ConstantsLocal.TYPE_CREATION)
        if (ConstantsLocal.isAutoNewEnabled) arrCatWhere.add(ConstantsLocal.TYPE_AUTOMATED_NEW)
        arrCategoryCreation = category.fillCategoryArray(arrCatWhere,
            inclSeqno = false,
            inclCount = false
        )
    }

    fun fillArrayCategory(context: Context){
        val category = Category(context)
        val arrCatWhere = arrayListOf(ConstantsLocal.TYPE_CREATION)
        if (ConstantsLocal.isAutoNewEnabled) arrCatWhere.add(ConstantsLocal.TYPE_AUTOMATED_NEW)
        arrCategoryCreation = category.fillCategoryArray(arrCatWhere,
            inclSeqno = false,
            inclCount = false
        )
        arrCategoryGPS = category.fillCategoryArray(arrayListOf(ConstantsLocal.TYPE_GPS),
            inclSeqno = false,
            inclCount = false
        )
        arrFileExtension = category.fillCategoryArray(arrayListOf(ConstantsLocal.TYPE_FILE_EXTENSION),
            inclSeqno = false,
            inclCount = false
        )
    }

    fun cleanupDemo(): ReturnValue {
        var rtn = ReturnValue()
        if (BuildConfig.LIMIT > 0 && Product(contextLocal).getFilesCount() > BuildConfig.LIMIT) {
            var cnt = 0
            val sql = "SELECT ${Columns._id.name}" +
                    " FROM $tableName where ${Columns.type.name} = ${ConstantsLocal.TYPE_FILE} " +
                    " ORDER BY 1"
            rtn = rawQuery(sql, null)
            var id = ""
            if (rtn.cursor.moveToFirst()){
                do{
                    cnt++
                    if (cnt > BuildConfig.LIMIT){
                        id += rtn.cursor.getColumnValueInt(Columns._id.name).toString() + ","
                    }
                } while (rtn.cursor.moveToNext())
            }
            rtn.cursorClose()
            if (id != ""){
                id = id.substring(0,id.length-1)
                id.split(",").forEach {
                    delete("${Columns._id.name}} = ${it.toInt()}")
                }
            }
        }
        return rtn
    }

    @SuppressLint("ObsoleteSdkInt")
    fun visitAllDirsAndFiles(
        context: Context, productid: Int
    ): ReturnValue {
        // caution: this is used by multi threading; don't use database often
        val product = Product(context)
        val category = Category(context)
        val productRel = ProductRel(context)
        val orderLine = OrderLine()
        //val systeem = Systeem(context)
        var rtn = product.getDirectory(productid)
        if (!rtn.cursor.moveToFirst()){
            Logging.i(context.getText(R.string.mess008_nodata).toString() + "\nproductid: $productid")
            rtn.cursorClose()
            rtn.returnValue = false
            return rtn
        }

        val categoryid = ViewUtils.getValueCursor(rtn.cursor, Columns._categoryid.name).toInt()
        if (categoryid == 0){
            Logging.i(context.getText(R.string.mess008_nodata).toString() + "\ncategoryid: 0")
            rtn.cursorClose()
            rtn.returnValue = false
            return rtn
        }

        val title = ViewUtils.getValueCursor(rtn.cursor, Columns.title.name)
        val folder = ViewUtils.getValueCursor(rtn.cursor, Columns.dirname.name)
        val mSwRecursive = ViewUtils.getValueCursor(rtn.cursor, Columns.recursive.name) != "0"

        rtn.cursorClose()

        // delete duplicate rows
        delete(" type = ${ConstantsLocal.TYPE_FILE} " +
                "and exists(select 1 from $TABLE_NAME b" +
                " where $TABLE_NAME.filename = b.filename" +
                " and $TABLE_NAME.dirname = b.dirname" +
                " and $TABLE_NAME._id < b._id" +
                " and $TABLE_NAME.type = b.type)")

        val arrFolders = FilesFolders().getFolder(folder, mSwRecursive)
        category.getSubCategory(productid, categoryid, title, arrFolders)

        // get all files from mobile
        val filterFolder: MutableList<String> = mutableListOf()
        arrFolders.filter { !it.isChecked }.forEach {
            filterFolder.add(it.folder.lowercase())
            // delete file database entries
            delete("${Columns.dirname.name} = \"${it.folder}\" and type = ${ConstantsLocal.TYPE_FILE}")
        }

        val arrFiles = FilesFolders().getFilesFolder(folder, mSwRecursive)
        val max = arrFiles.size-1
        for (i in max downTo  0) {
            if (arrFiles[i].folder.lowercase() in filterFolder) {
                // file may not be added to database
                arrFiles.removeAt(i)
            }
        }
        // remove orphans

        var countFiles = 0

        if (arrFolders.isNotEmpty()){
            var folders = ""
            //filterFolder = ArrayList()
            arrFolders.filter { it.isChecked }.forEach {
                //filterFolder.add( it.folder.lowercase() )
                folders += "'${it.folder}',"
            }
            folders = folders.substring(0,folders.length-1)
            // get files from database
            val where = " ${Columns.type.name} = ${ConstantsLocal.TYPE_FILE} and ${Columns.dirname.name} in ($folders)"
            val rtnProd = getColumnsWhere("${Columns._id.name},${Columns.filename.name},${Columns.dirname.name},${Columns.sizekb.name}", where)
            var deleteProduct = ""
            if (rtnProd.cursor.moveToFirst()) {
                do {
                    val filename = getString(rtnProd.cursor, Columns.filename.name)
                    val dirname = getString(rtnProd.cursor, Columns.dirname.name)

                    refreshSize(getInt(rtnProd.cursor, Columns._id.name), dirname, filename, getInt(rtnProd.cursor, Columns.sizekb.name))

                    val file = File("$dirname/$filename")
                    if (file.exists()){
                        arrFiles.filter{it.filename.equals(filename,true) && it.folder.equals(dirname,true) }                        .forEach {
                            it.id = getInt(rtnProd.cursor, Columns._id.name)
                            it.mainid = arrFolders.find { fldr -> fldr.folder.equals(dirname,true) }!!.id
                            it.inUse = true
                        }
                    } else {
                        deleteProduct += getInt(rtnProd.cursor, Columns._id.name).toString() + ","
                    }
                } while (rtnProd.cursor.moveToNext())
            }
            rtnProd.cursorClose()
            if (deleteProduct.isNotEmpty()){
                deleteProduct = deleteProduct.substring(0,deleteProduct.length-1)

                delete("${Columns._id.name} in ($deleteProduct)")
                productRel.delete(" ${TableProductRel.Columns._productid.name} in ($deleteProduct)")
                orderLine.delete(" ${TableOrderLine.Columns._productid.name} in ($deleteProduct)")
            }

            arrFiles.filter{it.id == 0 }.forEach {
                val fp = FolderProduct()
                val file = File("${it.folder}/${it.filename}")
                val code = file.nameWithoutExtension

                var gpsLocation = ""
                var gpsInfo = GPSInfo()
                gpsInfo.rtn = 0
                if (ConstantsLocal.isGPSEnabled) {
                    gpsInfo = FilesGPS.gpsLocation(context, it.folder, it.filename)
                    if (gpsInfo.address != null) {
                        gpsLocation = gpsInfo.address!!.joinToString(";")
                    }
                }
                val rotation = Graphics.getFileRotation (file.path)
                // add product
                fp.folder = it.folder
                fp.filename = it.filename
                fp.gpsinfo = gpsInfo
                fp.isChecked = true
                fp.inUse = true
                fp.mainid = arrFolders.find { fldr -> fldr.folder.equals(fp.folder,true) }!!.id
                val  creationDateStr = fp.creationDateStr()

                var map = ContentValues()
                map.put(Columns._categoryid.name,categoryid)
                map.put(Columns.type.name,ConstantsLocal.TYPE_FILE)
                map.put(Columns.code.name,code)
                map.put(Columns.title.name,code)
                map.put(Columns.description.name,code)
                map.put(Columns.dateavailable.name, creationDateStr )
                map.put(Columns.filelastmodified.name, creationDateStr)
                map.put(Columns.dirname.name,it.folder)
                map.put(Columns.filename.name,file.name)
                map.put(Columns.gpslocation.name,gpsLocation)
                map.put(Columns.gpsstatus.name, gpsInfo.rtn)
                map.put(Columns.rotation.name, rotation)
                map.put(Columns.sizekb.name,file.length() / 1024)
                rtn = TableProduct(context).insertPrimaryKey(map)
                fp.id = rtn.id

                map = ContentValues()
                map.put(TableProductRel.Columns._categoryid.name,fp.mainid)
                map.put(TableProductRel.Columns._productid.name,fp.id)
                rtn = TableProductRel().insertPrimaryKey(map)

                val tblTableProductRel = TableProductRel()
                if (ConstantsLocal.isAutoNewEnabled){
                    tblTableProductRel.insertProductRel(fp.id, category.getCreateNewId())
                }

                var categoryId = 0
                val rtnCategory = category.selectWhere("${TableCategory.Columns.type.name} = ${ConstantsLocal.TYPE_FACE} and ${TableCategory.Columns._mainid.name} = 0")
                if (rtnCategory.cursor.moveToFirst()){
                    categoryId = rtnCategory.cursor.getColumnValueInt(TableCategory.Columns._id.name,0)
                }

                val tblFace = TableFace()
                val arrFaces = MediapipeFaceDetector().procesFile(context,it.folder,file.name)
                if (arrFaces != null){
                    tblFace.delete("${TableFace.Columns._productid.name} = $productid and ${TableFace.Columns._categoryid.name} = 0")
                    arrFaces.forEach {
                        val mapFace = ContentValues()
                        mapFace.put(TableFace.Columns._categoryid.name, categoryId)
                        mapFace.put(TableFace.Columns._productid.name, productid)
                        mapFace.put(TableFace.Columns.seqno.name, 1)
                        mapFace.put(TableFace.Columns.spoof.name, 0)
                        mapFace.put(TableFace.Columns.distance.name, 0f)
                        mapFace.put(TableFace.Columns.extra.name, it.toString())
                        rtn = tblFace.insertTableRow(mapFace)
                        if (!rtn.returnValue) {
                            return rtn
                        }
                    }
                }

                arrFiles.add(fp)
                countFiles++
            }
        }
        var filesId = ""
        arrFiles.filter{it.inUse}.forEach {
            filesId += "${it.id},"
            it.category=""
        }

        if (filesId.isNotEmpty()){
            // get all creation id's from database
            filesId = filesId.substring(0,filesId.length-1)
            rtn = productRel.rawQuery("select _categoryid, group_concat(_productid) as _productid" +
                    " from ${TableProductRel.TABLE_NAME}" +
                    " join ${TableCategory.TABLE_NAME} on pos_category._id = ${TableProductRel.TABLE_NAME}._categoryid" +
                    " where _productid in ($filesId)" +
                    " and pos_category.type = ${ConstantsLocal.TYPE_CREATION}" +
                    " group by _categoryid",null)

            if (rtn.cursor.moveToFirst()){
                do {
                    val catid = rtn.cursor.getColumnValueString(TableProductRel.Columns._categoryid.name)
                    val arrProdId = rtn.cursor.getColumnValueString(TableProductRel.Columns._productid.name).toString()
                    arrFiles.filter{ (",$arrProdId,").contains(",${it.id},")}.forEach { it.category += "$catid," }
                } while(rtn.cursor.moveToNext())
            }
            rtn.cursorClose()
        }

        arrFiles.filter{it.id > 0}.forEach {
            if (arrCategoryCreation.isEmpty()) {
                fillArrayCategoryCreation(category)
            }
            // insert only if existing monthId not match with calculated
            category.creationDate(it, arrCategoryCreation, it.category)
        }

        if (ConstantsLocal.isFileExtensionEnabled) {
            arrFiles.filter { it.id > 0 }.forEach {
                if (arrFileExtension.isEmpty()) {
                    arrFileExtension = category.fillCategoryArray(
                        arrayListOf(ConstantsLocal.TYPE_FILE_EXTENSION),
                        inclSeqno = false,
                        inclCount = false
                    )
                }
                category.fileExtension(it, arrFileExtension)
            }
        }

        if (ConstantsLocal.isGPSEnabled){
            arrFiles.filter{it.id > 0}.forEach {
                // add GPS information
                if (arrCategoryGPS.isEmpty()){
                    arrCategoryGPS = category.fillCategoryArray(arrayListOf(ConstantsLocal.TYPE_GPS),
                        inclSeqno = false,
                        inclCount = false
                    )
                }
                gpsLocations(context, it.id,  it.folder, it.filename, it.gpsinfo, true, arrCategoryGPS)
            }
        }

        var sql = "insert into ${TableProductRel.TABLE_NAME} (${TableProductRel.Columns._productid.name}, ${TableProductRel.Columns._categoryid.name} )" +
                " select distinct f._id, pos_category._id" +
                " from $TABLE_NAME f" +
                " join ${TableCategory.TABLE_NAME} on pos_category.description = f.dirname" +
                " join ${TableCategory.TABLE_NAME} c2 on c2._id = f._categoryid" +
                " where f._categoryid = $categoryid and c2.type = ${ConstantsLocal.TYPE_DIRECTORY} and f.type = ${ConstantsLocal.TYPE_FILE}" +
                " and not exists(select 1 from ${TableProductRel.TABLE_NAME}" +
                " where ${TableProductRel.Columns._productid.name}  = f._id"+
                " and   ${TableProductRel.Columns._categoryid.name} = pos_category._id)"
        rtn = execSQL(sql)

        // cleanup database
        sql = "delete from ${TableProductRel.TABLE_NAME}" +
                " where not exists(select 1 from $TABLE_NAME where $TABLE_NAME._id = ${TableProductRel.TABLE_NAME}._productid)"
        rtn = execSQL(sql)

        sql = "delete from ${TableProductRel.TABLE_NAME}" +
                " where not exists(select 1 from ${TableCategory.TABLE_NAME} where ${TableCategory.TABLE_NAME}._id = ${TableProductRel.TABLE_NAME}._categoryid)"
        rtn = execSQL(sql)

        sql = "DELETE FROM ${TableProductRel.TABLE_NAME}" +
                " where exists (" +
                " select 1" +
                " FROM  ${TableProductRel.TABLE_NAME} sub" +
                " WHERE ${TableProductRel.TABLE_NAME}.${TableProductRel.Columns._productid.name} = sub._productid" +
                " AND   ${TableProductRel.TABLE_NAME}._categoryid = sub._categoryid" +
                " AND   ${TableProductRel.TABLE_NAME}.rowid < sub.rowid ) "
        rtn = execSQL(sql)

        val bDelete = (Systeem(context).getValue(SystemAttr.DeleteEmptyProduct) != "0")
        // delete empty of not existing file(s)
        if (bDelete) {
            var where = ""
            rtn = product.getProduct(0)
            if (rtn.cursor.moveToFirst()) {
                do {
                    val filename =
                        rtn.cursor.getColumnValueString(Columns.filename.name)
                    val dirname = rtn.cursor.getColumnValueString(Columns.dirname.name)
                    val id = rtn.cursor.getColumnValueString(Columns._id.name)
                    if (filename.isNullOrEmpty() || !File("$dirname/$filename").isFile) {
                        where += "$id,"
                        countFiles++
                    }
                } while (rtn.cursor.moveToNext())
            }
            rtn.cursorClose()
            if (where.isNotEmpty()) {
                product.delete(
                    Columns._id.name + " in (${
                        where.substring(
                            0,
                            where.length - 1
                        )
                    })"
                )
            }
        }

        rtn.rowsaffected = countFiles
        return rtn
    }

    @SuppressLint("ObsoleteSdkInt")
    fun imageCreationDate(context: Context, fileProduct: FolderProduct, arrCategoryCreation: ArrayList<CategoryLine> ){
        val tblCategory = Category(context)
        //val file = File(fileProduct.file)
        val tblProductRel = ProductRel(context)

        tblProductRel.deleteIdType(fileProduct.id, ConstantsLocal.TYPE_AUTOMATED_NEW)
        tblProductRel.deleteIdType(fileProduct.id, ConstantsLocal.TYPE_CREATION)

        tblCategory.creationDate(fileProduct,arrCategoryCreation,"")
    }

    fun fileExtension(context: Context, fileProduct: FolderProduct, arrFileExtension: ArrayList<CategoryLine> ){
        val tblCategory = Category(context)
        val tblProductRel = ProductRel(context)

        tblProductRel.deleteIdType(fileProduct.id, ConstantsLocal.TYPE_FILE_EXTENSION)

        tblCategory.fileExtension( fileProduct,arrFileExtension )
    }

    fun gpsLocations(context: Context, id:Int, dirname: String, filename: String, gpsInfoParam: GPSInfo?,
                     isNew: Boolean, arrCategoryGPS: ArrayList<CategoryLine>): Int {

        val tblCategory = Category(context)
        val tblProductRel = ProductRel(context)
        val gpsInfo: GPSInfo
        if (!isNew) {
            tblProductRel.deleteIdType(id, ConstantsLocal.TYPE_GPS)
        }

        if (!ConstantsLocal.isGPSEnabled) return 0

        val addresses: List<Address>?
        if (gpsInfoParam?.address == null){
            gpsInfo = FilesGPS.gpsLocation(context, dirname, filename)
            if (gpsInfo.address == null) return 0
            addresses = gpsInfo.address
        } else{
            gpsInfo = gpsInfoParam
            addresses = gpsInfoParam.address
        }

        if (gpsInfo.rtn == 0 && !addresses.isNullOrEmpty()){
            tblCategory.gpsLocationMain(id, addresses, arrCategoryGPS)

            if (addresses[0].phone != null) tblCategory.gpsLocation(id, context.getText(R.string.gpsphone).toString(), addresses[0].phone,9,arrCategoryGPS)
            if (addresses[0].url != null) tblCategory.gpsLocation(id, context.getText(R.string.gpsurl).toString(), addresses[0].url,10,arrCategoryGPS)
            if (addresses[0].extras != null) tblCategory.gpsLocation(id, context.getText(R.string.gpsextras).toString(), addresses[0].extras.toString(),11,arrCategoryGPS)
        }
        return 1
    }

    fun refreshFaceInfo(context: Context): ReturnValue {
        var cnt = 0
/*        var distance =  1.0f
        var distanceSysteem = Systeem(context).getValue(SystemAttr.FaceDistance).toFloat()
        if (distanceSysteem < 1.0f && distanceSysteem >= 0.0f){
            distance = distanceSysteem
        }*/
        var spoof = 0

        var rtn = getCountCategory()
        var arrCountCategory: ArrayList<Pair<Int,Int>> = arrayListOf()
        if (rtn.cursor.moveToFirst()) {
            do {
                val id =  rtn.cursor.getColumnValueInt(Columns._id.name)!!
                val countCategory =  rtn.cursor.getColumnValueInt("countCategory")!!
                arrCountCategory.add(Pair(id,countCategory))
            } while (rtn.cursor.moveToNext())
        }

        val detector = getDetector()
        rtn = getProductFaces()
        //rtn = getProduct(7860)
        if (rtn.cursor.moveToFirst()) {
            do {
                //memoryUsed("refreshFaceInfo")
                cnt++
                val id = rtn.cursor.getColumnValueInt(Columns._id.name)!!
                Face().delete("${TableFace.Columns._productid.name} = $id")
                val dirname = rtn.cursor.getColumnValueString(Columns.dirname.name).toString()
                val filename = rtn.cursor.getColumnValueString(Columns.filename.name).toString()
                val categoryid =
                    rtn.cursor.getColumnValueInt(TableProductRel.Columns._categoryid.name)!!
                var categoryCount = 0
                arrCountCategory.filter { it.first == id }.forEach { categoryCount = it.second }
                procesFile(
                    context, productIdParm = id,
                    categoryidParm = if (categoryCount == 1) categoryid else 0,
                    dirnameParm = dirname, filenameParm = filename, spoof = spoof
                )
            } while (rtn.cursor.moveToNext())
        }
        Logging.d("refreshFaceInfo: ended", null)
        rtn.cursorClose()
        detector.close()
        rtn.rowsaffected=cnt
        rtn.returnValue=true
        return rtn
    }

    fun getDetector(): FaceDetector {
        val highAccuracyOpts =
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .build()
        return FaceDetection.getClient(highAccuracyOpts)
    }

    fun procesFile( context:Context,
                            productIdParm:Int,
                            categoryidParm:Int,
                            dirnameParm:String,
                            filenameParm:String,
                            spoof: Int) {

        // can not load file directly into inputImage - direct abort if too large
        var impPhoto:InputImage?
        //var bm: Bitmap?
        val modelFile = "mobile_face_net.tflite"
        if (File("$dirnameParm/$filenameParm").length()/1024 < 1000) {
            try {
                //bm = BitmapResolver.getBitmap(context.contentResolver, "$dirnameParm/$filenameParm")
                impPhoto = InputImage.fromFilePath(context, Uri.fromFile(File("$dirnameParm/$filenameParm")))
            } catch (_: Exception) {
                //Log.d("ImageError", "Getting Image failed");
                return
            }
        } else {
            val bm = BitmapManager.loadBitmap(context,"$dirnameParm/$filenameParm", 3000)
            impPhoto = InputImage.fromBitmap(bm!!,0)
        }
        //note: detector and tflite can not be outside this function
        val detector = getDetector()
        detector.process(impPhoto).addOnSuccessListener { faces ->
            var tfLite: Interpreter? = null
            try {
                tfLite = Interpreter(loadModelFile(context, modelFile), null)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val listFaces = MediapipeFaceDetector().procesFaces(tfLite!!, impPhoto.bitmapInternal!!, faces)
            tfLite.close()
                if (listFaces != null && listFaces.isNotEmpty()) {
                    if (categoryidMain == 0) {
                        val rtnCategory =
                            tblCategory!!.selectWhere("${TableCategory.Columns.type.name} = ${ConstantsLocal.TYPE_FACE} and ${TableCategory.Columns._mainid.name} = 0")
                        if (rtnCategory.cursor.moveToFirst()) {
                            categoryidMain =
                                rtnCategory.cursor.getColumnValueInt(TableCategory.Columns._id.name, 0)
                        }
                        rtnCategory.cursorClose()
                    }

                    listFaces.forEachIndexed {  _, it ->
                        val mapSub = ContentValues()
                        mapSub.put(TableFace.Columns._categoryid.name, categoryidMain)
                        mapSub.put(TableFace.Columns._productid.name, productIdParm)
                        mapSub.put(TableFace.Columns.seqno.name, 0)
                        mapSub.put(TableFace.Columns.spoof.name, 0)
                        mapSub.put(TableFace.Columns.distance.name, 0.0)
                        mapSub.put(TableFace.Columns.extra.name, it.toString().replace(" ",""))
                        tblFace!!.insertTableRow(mapSub)
                        if (listFaces.size == 1 && categoryidParm > 0) {
                            mapSub.put(TableFace.Columns.spoof.name, spoof)
                            mapSub.put(TableFace.Columns._categoryid.name, categoryidParm)
                            tblFace!!.insertTableRow(mapSub)
                        }
                        mapSub.clear()

                        //if (!rtn.returnValue) {
                        //    return rtn
                        //}
                    }
                }
                listFaces?.clear()
            detector.close()
        }
    }

    fun refreshGPSInfo(context: Context): ReturnValue{
        var rtn = ReturnValue()
        if (!FilesGPS.isOnline(context)){
            rtn.returnValue = false
            rtn.messnr = R.string.mess053_no_connection
        }
        var cnt = 0

        fillArrayCategory(context)

        rtn = getGPSProductRefresh()
        if (rtn.cursor.moveToFirst()) {
            do {
                val dirname =  rtn.cursor.getColumnValueString(Columns.dirname.name).toString()
                val filename =  rtn.cursor.getColumnValueString(Columns.filename.name).toString()
                val id =  rtn.cursor.getColumnValueInt(Columns._id.name)!!
                val gpsInfo = FilesGPS.gpsLocation(context, dirname, filename)
                if (gpsInfo.rtn == 0){
                    cnt++
                    gpsLocations(context, id, dirname, filename, gpsInfo, false, arrCategoryGPS)
                    val map = ContentValues()
                    map.put(Columns._id.name,id)
                    map.put(Columns.gpslocation.name,gpsInfo.address.toString())
                    map.put(Columns.gpsstatus.name,gpsInfo.rtn)
                    updateGPSInfo(map)
                }
            } while (rtn.cursor.moveToNext())
        }
        rtn.cursorClose()
        rtn.rowsaffected=cnt
        rtn.returnValue=true
        return rtn
    }

    private fun refreshSize(id:Int, dirname: String, filename: String, size: Int){
        if (ConstantsLocal.storeFileSize) {
            val file = File("$dirname/$filename")
            if (file.exists()) {
                val sizeLocal = (file.length()/1024).toInt()
                if (sizeLocal != size){
                    val map = ContentValues()
                    map.put(Columns._id.name,id)
                    map.put(Columns.sizekb.name,sizeLocal)
                    updatePrimaryKey(map)
                }
            } else {
                if (size > 0){
                    val map = ContentValues()
                    map.put(Columns._id.name,id)
                    map.put(Columns.sizekb.name,0)
                    updatePrimaryKey(map)
                }
            }
        }
        }

    @Suppress("unused")
    private fun memoryUsed(extra:String){
        val available = Runtime.getRuntime().maxMemory().toFloat()
        val used = Runtime.getRuntime().totalMemory().toFloat()
        val percentAvailable: Float = 100f * (1f - (used  / available))
        //if (percentAvailable <= 5.0f) {
            Logging.d("Memory", "Percent available ($extra): $percentAvailable")
            //return
        //}
    }
}