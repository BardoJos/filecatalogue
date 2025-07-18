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
package com.farrusco.projectclasses.databases

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteOpenHelper
import com.farrusco.projectclasses.R
import com.farrusco.projectclasses.common.Constants
import com.farrusco.projectclasses.databases.tables.DBUtils
import com.farrusco.projectclasses.utils.Cryption
import com.farrusco.projectclasses.utils.Logging
import com.farrusco.projectclasses.utils.ReturnValue
import org.w3c.dom.Document
import org.xml.sax.InputSource
import java.io.*
import java.nio.ByteBuffer
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.system.exitProcess

open class DatabaseHelper(var context: Context) : SQLiteOpenHelper(
    context,
    Constants.DATABASE_NAME,
    null,
    Constants.DATABASE_VERSION
) {

    private var xmlBuilder: XmlBuilder? = null

    override fun onCreate(db: SQLiteDatabase) {
        try {
            Constants.db = db
        } catch (t: Throwable) {
            Logging.e("DatabaseHelper.onCreate(db)", t.toString())
            return
        }
        try {
            dropTables()
            processRawResource(Constants.SqlDbStructureId)
            processRawResource(Constants.SqlSystemId)
            if (Constants.SqlSampleDataId != 0) {
                processRawResource(Constants.SqlSampleDataId)
            }
            if (Constants.SqlFileExtensionDataId != 0) {
                processRawResource(Constants.SqlFileExtensionDataId)
            }
        } catch (t: Throwable) {
            Logging.e("DatabaseHelper.onCreate(Sql)", t.toString())
        }
    }

    private fun createStructureDatabase(): ReturnValue {
        val rtn = ReturnValue()
        try {
            processRawResource(Constants.SqlDbStructureId)
            processRawResource(Constants.SqlSystemId)
            processRawResource(Constants.SqlFileExtensionDataId)
        } catch (t: Throwable) {
            rtn.returnValue=false
            rtn.mess="DatabaseHelper.createStructureDatabase"
            Logging.e("${rtn.mess}\n$t")
        }
        return rtn
    }

    open fun createSampleDatabase(): ReturnValue {
        val rtn = ReturnValue()
        try {
            if (Constants.SqlSampleDataId != 0) {
                processRawResource(Constants.SqlSampleDataId)
            } else {
                rtn.mess="DatabaseHelper.createSampleDatabase"
                Logging.d(rtn.mess)
                rtn.returnValue=false
            }
            if (Constants.SqlFileExtensionDataId != 0) {
                processRawResource(Constants.SqlFileExtensionDataId)
            }
        } catch (t: Throwable) {
            rtn.mess="DatabaseHelper.createSampleDatabase"
            Logging.d("${rtn.mess}\n$t")
            rtn.returnValue=false
        }
        return rtn
    }

    open fun createSampleBackupDatabase(): ReturnValue {
        val rtn = ReturnValue()
        try {
            if (Constants.SqlSampleBackupId != 0) {
                importXML2Database(Constants.SqlSampleBackupId)
            } else if (Constants.SqlSampleDataId != 0) {
                processRawResource(Constants.SqlSampleDataId)
                if (Constants.SqlFileExtensionDataId != 0) {
                    processRawResource(Constants.SqlFileExtensionDataId)
                }
            } else {
                rtn.mess="DatabaseHelper.sampleDatabase"
                rtn.returnValue=false
            }
        } catch (t: Throwable) {
            rtn.mess="DatabaseHelper.sampleDatabase"
            Logging.e("${rtn.mess}\n$t")
            rtn.returnValue=false
        }
        return rtn
    }
    fun exportDatabaseUnencrypted(seqno: Int, dirName: String){
        val dbNameExt = Constants.DATABASE_NAME.split(".")[0]
        val file = File("$dirName/${dbNameExt}-nopw_$seqno.db")
        if (file.exists()){
            file.delete()
        }
        Constants.db.execSQL("ATTACH DATABASE '${file.absolutePath}' as $dbNameExt KEY '';")
        Constants.db.execSQL("SELECT sqlcipher_export('$dbNameExt');")
        Constants.db.execSQL("DETACH DATABASE $dbNameExt;")
    }

    open fun importDatabaseEncrypted(seqno:Int, dirName: String): Boolean {

        val dbNameExt = Constants.DATABASE_NAME.split(".")[0]
        val unencryptedDatabase = File("$dirName/${dbNameExt}-nopw_$seqno.db")
        val encryptedDatabase = File(Constants.db.path!!)

        try {
            Constants.db.close()
            encryptedDatabase.delete()

            val database = SQLiteDatabase.openOrCreateDatabase(unencryptedDatabase.path, "", null)
            database.execSQL(
                    "ATTACH DATABASE '${encryptedDatabase.absolutePath}' AS encrypted KEY '${Constants.DATABASE_PW}'"
            )
            database.execSQL("select sqlcipher_export('encrypted')")
            database.execSQL("DETACH DATABASE encrypted")
            database.close()

            Constants.db = SQLiteDatabase.openOrCreateDatabase(
                encryptedDatabase.path, "", null,
            )

        } catch (_: IOException) {
            return false
        }
        return true
    }

    private fun processRawResource(resource: Int) {
        if (resource == 0) return
        var s: String? = null
        var step = 1
        try {
            val rawResource = context.resources.openRawResource(resource)
            step++
            val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            val doc = builder.parse(rawResource, null)
            rawResource.close()
            val statements = doc.getElementsByTagName("statement")
            step++

            val j = statements.length

            for (i in 0 until j) {
/*                var tableName = ""
                if (statements.item(i).hasAttributes() && statements.item(i).attributes.getNamedItem("table") != null){
                    tableName = (statements.item(i).attributes.getNamedItem("table") as Attr).value
                    Logging.d( "processRawResource", "Table: tableName")
                }*/
                s = statements.item(i).childNodes.item(0).nodeValue
                if (s != null) Constants.db.execSQL(s)
            }
        } catch (t: Throwable) {
            Logging.e( "DatabaseHelper.processRawResource($step)", "$resource\n$s\n$t")
            exitProcess(-1)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        dropTables()
        onCreate(Constants.db)
    }

    fun createDatabase(ads: Int): ReturnValue{
        var rtn = DBUtils.openOrCreateDatabase(context)
        if (rtn.returnValue){
            if (Constants.isDbInitialized()) {
                rtn = createStructureDatabase()
                if (rtn.returnValue){
                    return if (ads == 0){
                        createSampleBackupDatabase()
                    } else {
                        createSampleDatabase()
                    }
                }
            }
        }
        return rtn
    }

    fun checkDatabase(arrCheck: ArrayList<String>): ReturnValue {
        val rtn = ReturnValue()
        val arrTable = getTables()
        val m = arrCheck.size
        for (i in 0 until m) {
            if (!arrTable.contains(arrCheck[i])) {
                rtn.mess="checkDatabase: table does not exist: " + arrCheck[i]
                rtn.returnValue=false
                Logging.d(rtn.mess)
                break
            }
        }
        return rtn
    }

    @Suppress("unused")
    private fun emptyTables() {
        // get the tables
        val arrTable = getTables()
        arrTable.forEach {
            Logging.d("emptyTables", "table name $it")
            Constants.db.execSQL("delete from $it")
        }
    }

    private fun dropTables(): ReturnValue {
        val rtn = ReturnValue()
        // get the tables
        var arrTable = getTables()
        arrTable.forEach {
            Logging.d("dropTables", "table name $it" )
            Constants.db.execSQL("DROP TABLE IF EXISTS $it")
        }
        arrTable = getViews()
        arrTable.forEach {
            Logging.d("dropViews", "view name $it")
            Constants.db.execSQL("DROP VIEW IF EXISTS $it")
        }
        return rtn
    }

    open fun reorgDatabase(): ReturnValue {
        val rtn = ReturnValue()
        try {
            if (Constants.SqlReorgId != 0) {
                processRawResource(Constants.SqlReorgId)
                processRawResource(Constants.SqlSystemId)
            }
        } catch (t: Throwable) {
            rtn.returnValue = false
            rtn.mess = "DatabaseHelper.reorgDatabase"
            Logging.d("${rtn.mess}\n$t")
        }
        return rtn
    }

    fun getDocument(filename: String,dir: File, secretKey: String): Document? {
        Logging.i(
            "import database - ${Constants.DATABASE_NAME} importFileNamePrefix=$filename "
        )
        try {
            val inpstr = readFromFile(dir, filename)
            val cryp = if (secretKey == "") {
                inpstr
            } else {
                Cryption(Cryption.TRANSFORMATION_DES).decrypt(inpstr, secretKey, true)
            }
            val builderFactory = DocumentBuilderFactory.newInstance()
            val docBuilder = builderFactory.newDocumentBuilder()
            return docBuilder.parse(InputSource(StringReader(cryp)))
        } catch (t: Throwable) {
            Logging.e("Error restoring", t.toString())
        }
        return null
    }

/*
    @Throws(IOException::class)
    fun importDatabase(importFileNamePrefix: String,dir: File, secretKey: String): ReturnValue {
        Logging.i(
            "import database - ${Constants.DATABASE_NAME} importFileNamePrefix=${importFileNamePrefix}"
        )
        // ByteBuffer buff = ByteBuffer.wrap(xmlString.getBytes());
*/
/*        val inp: InputStream = FileInputStream(file)
        inp.close()
        *//*

        var rtn = ReturnValue()
        try {
            val inps = readFromFile(dir, "$importFileNamePrefix.xml")
            val cryp = if (secretKey == ""){
                inps
            } else{
                Cryption(Cryption.TRANSFORMATION_DES).decrypt(inps,secretKey,true)
            }
            val builderFactory = DocumentBuilderFactory.newInstance()
            val docBuilder  = builderFactory.newDocumentBuilder()
            val doc: Document =  docBuilder.parse(InputSource( StringReader(cryp)))
            rtn = importDatabase(doc)
        } catch (t: Throwable) {
            Logging.e("Error restoring\n$t")
            rtn.returnValue = false
            rtn.messnr = R.string.mess024_restore_error
        }

        return rtn
    }
*/

    private fun importXML2Database(resource: Int): ReturnValue {
        var rtn = ReturnValue()
        if (resource == 0){
            rtn.returnValue = false
            rtn.messnr=R.string.mess024_restore_error
            return rtn
        }
        try {
            val rawResource = context.resources.openRawResource(resource)
            val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            val doc = builder.parse(rawResource, null)
            rawResource.close()
            rtn = importDatabase(doc)
        } catch (t: Throwable) {
            Logging.e("Error restoring\n$t")
            rtn.returnValue = false
            rtn.messnr=R.string.mess024_restore_error
        }
        return rtn
    }

    fun importDatabase(doc: Document): ReturnValue {
        var bDrop = true
        val rtn = ReturnValue()
        try {
            val nlDatabase = doc.getElementsByTagName("database")
            val j = nlDatabase.length
            for (i in 0 until j) {
                val nlTable = doc.getElementsByTagName("table")
                if (bDrop  && nlTable.length > 0){
                    dropTables()
                    try {
                        processRawResource(Constants.SqlDbStructureId)
                        processRawResource(Constants.SqlSystemId)
                        processRawResource(Constants.SqlFileExtensionDataId)
                    } catch (t: Throwable) {
                        rtn.returnValue = false
                        rtn.messnr=R.string.mess024_restore_error
                        Logging.e("Error restoring $i\n$t")
                        return rtn
                    }
                    bDrop = false
                }
                val j1 = nlTable.length
                for (i1 in 0 until j1) {
                    val strTable = nlTable.item(i1).attributes.item(0).nodeValue
                    Constants.db.delete(strTable, null, null)
                    val arrColumns = "," + getColumnNames(strTable) + ","
                    // reset auto sequence
                    // db.delete("sqlite_sequence","name = '"
                    // + strTable + "'",null);
                    val nlRow = nlTable.item(i1).childNodes
                    val j2 = nlRow.length
                    for (i2 in 0 until j2) {
                        val map = ContentValues()
                        val nlCol = nlRow.item(i2).childNodes
                        val j3 = nlCol.length
                        for (i3 in 0 until j3) {

                            if (nlCol.item(i3).firstChild == null
                                || nlCol.item(i3).firstChild.nodeValue == null
                                || nlCol.item(i3).firstChild.nodeValue == "null") {
                                continue
                            }

                            val colName = nlCol.item(i3).attributes.item(0).nodeValue
                            if (arrColumns.indexOf(",$colName,",0,true) < 0) {
                                // in case a column does not exists skip this one
                                continue
                            }
                            val value = nlCol.item(i3).firstChild.nodeValue
                            map.put(colName,value.replace("&amp;","&").replace("&quot;","\"").replace("&apos;","'").replace("&lt;","<").replace("&gt;",">"))
                        }

                        try {
                            Constants.db.insert(strTable, null, map)
                        } catch (e: SQLException) {
                            rtn.returnValue = false
                            rtn.messnr=R.string.mess024_restore_error
                            Logging.e("Error writing $strTable\n$e")
                            return rtn
                        }
                    }
                }
            }
        } catch (t: Throwable) {
            rtn.returnValue = false
            Logging.e("Error restoring\n$t")
            rtn.messnr=R.string.mess024_restore_error
            return rtn
        }

        rtn.messnr=R.string.mess_fileimported
        return rtn
    }

    @SuppressLint("Range")
    private fun getTables(): ArrayList<String> {
        val arrTable = ArrayList<String>()

        // get the tables
        val sql =
            "select name, type from sqlite_master WHERE type ='table' AND name NOT LIKE 'sqlite_%'"
        val c = Constants.db.rawQuery(sql, null)
        if (c.moveToFirst()) {
            do {
                val tableName = c.getString(c.getColumnIndex("name"))
                // skip metadata, sequence, and uidx (unique indexes)
                if (tableName != "android_metadata" && tableName != "systeem" && !tableName.endsWith(
                        "_idx"
                    )
                    && !tableName.startsWith("uidx")
                ) {
                    arrTable.add(tableName.uppercase(Locale.getDefault()))
                    //arrTable.add(tableName.toUpperCase())
                }
            } while (c.moveToNext())
        }
        c.close()
        return arrTable
    }

    @SuppressLint("Range")
    private fun getViews(): ArrayList<String> {
        val arrTable = ArrayList<String>()
        // get the tables
        val sql =
            "select name, type from sqlite_master WHERE type = 'view' AND name NOT LIKE 'sqlite_%'"
        val c: Cursor = Constants.db.rawQuery(sql, arrayOfNulls<String>(0))
        if (c.moveToFirst()) {
            c.moveToPosition(-1)
            while (c.moveToNext()) {
                val tableName = c.getString(c.getColumnIndex("name"))
                // skip metadata, sequence, and uidx (unique indexes)
                if (tableName != "android_metadata" && tableName != "systeem" && !tableName.endsWith(
                        "_idx"
                    )
                    && !tableName.startsWith("uidx")
                ) {
                    arrTable.add(tableName.uppercase(Locale.getDefault()))
                }
            }
        }
        c.close()
        return arrTable
    }

    @Throws(IOException::class)
    fun exportDb( seqNo: Int, path: String) {
        val dbNameExt = Constants.DATABASE_NAME.split(".")[0]
        Logging.i(
            context.getString(R.string.activity_projectclasses_code),
            "exporting full database - ${Constants.DATABASE_NAME} seqno=$seqNo"
        )
        val inp = File(Constants.db.path!!)
        val out = File(  "$path/${dbNameExt}_$seqNo.db")
        inp.copyTo(out,true)
    }

    @Throws(IOException::class)
    fun export( seqNo: Int, path: String, secretKey: String): String {
        val dbNameExt = Constants.DATABASE_NAME.split(".")[0]
        Logging.i(
            context.getString(R.string.activity_projectclasses_code),
            "exporting database - ${Constants.DATABASE_NAME} seqno=$seqNo"
        )
        xmlBuilder = XmlBuilder()
        xmlBuilder!!.start(Constants.DATABASE_NAME)

        // get the tables
        val arrTable = getTables()
        val y = arrTable.size
        for (i in 0 until y) {
            Logging.d(
                context.getString(R.string.activity_projectclasses_code),
                "table name " + arrTable[i]
            )
            exportTable(arrTable[i])
        }
        val xmlString: String = xmlBuilder!!.end()
        val mess = if (secretKey == ""){
            writeToFile(File(path), xmlString, "${dbNameExt}_$seqNo.xml")
        } else {
            writeToFile(File(path), Cryption(Cryption.TRANSFORMATION_DES).encrypt(xmlString, secretKey,true), "${dbNameExt}_$seqNo.xml")
        }

/*        val inp = File(Constants.db.path)
        val out = File(dir.path + "/" + exportFileNamePrefix.replace(".db","") + ".db")
        inp.copyTo(out,true)*/

        if (mess == ""){
            Logging.i(
                Constants.APP_NAME,
                "exporting database complete")
            return context.getText(R.string.mess_exportcomplete).toString()
        }
        Logging.i(
            Constants.APP_NAME,
            "exporting failed: $mess" )
        return mess
    }

    private fun getColumnNames(tableName: String): String {
        var sColumns = ""
        val cursor = Constants.db.rawQuery("select * from $tableName limit 1", null)
        cursor.columnNames.forEach {
            sColumns += "$it,"
        }
        cursor.close()
        if (sColumns != "") sColumns=sColumns.substring(0,sColumns.length-1)
        return sColumns
    }


    @Throws(IOException::class)
    private fun exportTable(tableName: String) {
        Logging.d(
            context.getString(R.string.activity_projectclasses_code),
            "exporting table - $tableName"
        )
        xmlBuilder!!.openTable(tableName)
        val sql = "select * from $tableName"
        val c: Cursor = Constants.db.rawQuery(sql,null)
        if (c.moveToFirst()) {
            val cols = c.columnCount
            do {
                xmlBuilder!!.openRow()
                for (i in 0 until cols) {
                    var value = c.getString(i)
                    // in XML is forbidden
                    // "   &quot;
                    // '   &apos;
                    // <   &lt;
                    // >   &gt;
                    // &   &amp;
                    if (value != null){
                        value = value.replace("\"","&quot;").replace("'","&apos;").replace("<","&lt;").replace(">","&gt;").replace("&","&amp;")
                    }
                    // test if (tableName == "wm_system") Logging.d("$tableName: ${c.getColumnName(i)}, $value")
                    xmlBuilder!!.addColumn(c.getColumnName(i), value)
                }
                xmlBuilder!!.closeRow()
            } while (c.moveToNext())
        }
        c.close()
        xmlBuilder!!.closeTable()
    }

    @Throws(IOException::class)
    private fun writeToFile(dir: File, xmlString: String, exportFileName: String): String{

        // File dir = new File(Environment.getExternalStorageDirectory(),
        // Constants.DATASUBDIRECTORY);

        dir.mkdirs()
        if (!dir.exists()) {
            if (!dir.mkdirs()) return context.getText(R.string.mess005_nocreatefolder).toString()
        }
        val file = File(dir, exportFileName)
        if (file.exists()){
            file.delete()
        }
        try {
            if (!file.createNewFile()) {
                return context.getText(R.string.mess032_notsaved).toString()
            }

        } catch (e: Exception){
            return e.message.toString()
        }
        val buff = ByteBuffer.wrap(xmlString.toByteArray())
        //val buff = ByteBuffer.wrap(output.copyOfRange(0, compressedDataLength))
        val fos = FileOutputStream(file)
        val channel = fos.channel
        try {
            channel.write(buff)
        } finally {
            fos.close()
            channel.close()
        }

        /* ZIP the XML file and delete XML
           val files = listOf(File("$dir/$exportFileName"))
           val destination = File("$dir/${fileNameWithoutExtension}.zip")
           FilesFolders.archive(files, destination)
           file.delete()
           */
        return ""
    }

    private fun readFromFile(dir: File, importFileName: String): String{

        if (!dir.exists()) {
            return context.getText(R.string.mess005_nocreatefolder).toString()
        }
        val file = File(dir, importFileName)
        if (!file.exists()){
            return context.getText(R.string.not_file).toString()
        }

        val fis = FileInputStream(file)
        val channel = fis.channel
        val fileContent = StringBuffer("")
        val buffer = ByteArray(8192)
        var size1 = fis.read(buffer)
        while (size1  != -1) {
            fileContent.append(String(buffer, 0, size1))
            size1 = fis.read(buffer)
        }
        fis.close()
        channel.close()

        return fileContent.toString()
    }

/*
    private fun readFromFilex(dir: File, importFileName: String): String{

        if (!dir.exists()) {
            return context.getText(R.string.mess005_nocreatefolder).toString()
        }
        val file = File(dir, importFileName)
        if (!file.exists()){
            return context.getText(R.string.not_file).toString()
        }

        val fis = FileInputStream(file)
        val channel = fis.channel

        val fileContent = StringBuffer("")
        val buffer = ByteArray(8192)
        var size1 = fis.read(buffer)
        while (size1  != -1) {
            fileContent.append(String(buffer, 0, size1))
            size1 = fis.read(buffer)
        }
        fis.close()
        channel.close()
        return fileContent.toString()
    }
*/

}