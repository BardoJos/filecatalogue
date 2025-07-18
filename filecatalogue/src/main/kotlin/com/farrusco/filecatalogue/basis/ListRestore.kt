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
package com.farrusco.filecatalogue.basis

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.farrusco.filecatalogue.BuildConfig
import com.farrusco.filecatalogue.R
import com.farrusco.filecatalogue.SplashScreen
import com.farrusco.filecatalogue.business.Help
import com.farrusco.filecatalogue.business.Systeem
import com.farrusco.filecatalogue.common.ConstantsLocal
import com.farrusco.filecatalogue.common.SystemAttr
import com.farrusco.filecatalogue.tables.TableSysteem
import com.farrusco.projectclasses.activity.BaseActivityTableLayout
import com.farrusco.projectclasses.common.Constants
import com.farrusco.projectclasses.common.ConstantsFixed
import com.farrusco.projectclasses.databases.DatabaseHelper
import com.farrusco.projectclasses.utils.DeviceInfoUtils
import com.farrusco.projectclasses.utils.Logging
import com.farrusco.projectclasses.messages.Mess
import com.farrusco.projectclasses.messages.ToastExt
import com.farrusco.projectclasses.utils.ViewUtils
import com.farrusco.projectclasses.widget.ButtonExt
import org.w3c.dom.Document
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.text.SimpleDateFormat

class ListRestore : BaseActivityTableLayout() {
    override val layoutResourceId: Int = R.layout.list_backup_restore
    override val mainViewId: Int = R.id.viewMain
    private var systeem = Systeem(this)

    private var restoreLoader: RestoreLoader? = null
    private var mess = 0
    private var strMess = ""
    private val finishedHandler = Handler(Looper.getMainLooper())
    private var messDialog: Mess? = null
    lateinit var dir: File
    private var sRegistration = ""
    private var nVersion = -1

    override fun initTableLayout() {
        addTableLayout(
            R.id.tblLayout,
            R.layout.line_textview1x,
            systeem,
            arrayOf(TableSysteem.Columns.description.name, TableSysteem.Columns.systeemvalue.name),
            arrayListOf(ConstantsFixed.HELP_ID, ConstantsFixed.DELETE_ID),
            null
        )
    }

    override fun initActivity() {
        helpText = Help().getHelpTitle(className)
        restoreLoader = RestoreLoader(this)
        initTableLayout()
        fillList()
    }

    private fun fillList() {
        val mTblLayout = findViewById<TableLayout>(R.id.tblLayout)
        removeAllTableLayoutViews(0)
        val dirName = systeem.getValue(SystemAttr.BackupFolder)
        dir = if (dirName.isEmpty()){
            File(Constants.dataPath)
        } else {
            File(dirName)
        }
        if (!dir.exists()) {
            return
        }
        var max: Int
        try {
            max = systeem.getValue(SystemAttr.Backups).toInt()
            if (max < 1) max=10
        } catch (_: Exception){
            max = 10
        }

        val li: LayoutInflater = layoutInflater
        for (seqno in 1 until max+1){
            val dbNameExt = Constants.DATABASE_NAME.split(".")[0]
            "xml,db,nopw".split(",").forEach {
                var fileName = dbNameExt
                when (it){
                    "xml", "db" -> fileName += "_${seqno}.$it"
                    "nopw" -> fileName += "-${it}_$seqno.db"
                }
                val file = File(dir, fileName)
                if (file.exists()) {
                    val vi: View = li.inflate(R.layout.line_restore, null)
                    addLine(vi, mTblLayout, seqno, it, file)
                }
            }
        }
        setTableRowMenu(mTblLayout)
    }

    private fun addLine(vi: View, mTblLayout: TableLayout, seqno: Int, backupType: String, file: File){
        val colDescription: TextView = vi.findViewById(R.id.colDescription)

        val attrs = Files.readAttributes(file.toPath(), BasicFileAttributes::class.java)
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val creationDate = formatter.format(attrs.creationTime().toMillis())

        colDescription.text = creationDate
        //registerForContextMenu(colDescription)
        colDescription.tag = "$dir/${file.name}"
        val mBtnRestore = vi.findViewById<ButtonExt>(R.id.btnStartRestore)

        if (BuildConfig.BACKUP.indexOf(',') < 0){
            mBtnRestore.text = buildString {
                append(mBtnRestore.text.toString())
                append(" ($seqno)")
            }
        } else {
            mBtnRestore.text = buildString {
                append(mBtnRestore.text.toString())
                append(" ($seqno $backupType)")}
        }

        mBtnRestore.setOnClickListener(mLocalRestoreListener)
        mBtnRestore.tag = "$seqno,$backupType"
        mTblLayout.addView(vi, TableLayout.LayoutParams())
    }

    private val mLocalRestoreListener = View.OnClickListener {
        val mRowSearch = it.parent as TableRow
        val mBtnRestore = mRowSearch.findViewById<ButtonExt>(R.id.btnStartRestore)

        val alertDialog = Mess.buildAlertDialog(this,layoutInflater,
            getString(R.string.restore),
            getString( R.string.mess035_DoYouWantToRestore))

        with(alertDialog){
            setPositiveButton(
                com.farrusco.projectclasses.R.string.yes
            ) { _, _ ->
                messDialog = Mess(this@ListRestore)
                val arrTag = mBtnRestore.tag.toString().split(",")
                restoreLoader!!.setSeqno(arrTag[0].toInt(), arrTag[1])
                restoreLoader!!.start()
            }
            // Setting Positive Yes Btn
            setNeutralButton(
                com.farrusco.projectclasses.R.string.no
            ) { dialog, _ ->
                dialog.cancel()
            }
            show()
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (item.itemId == ConstantsFixed.DELETE_ID) {
            val vi = ViewUtils.getTableRow(currView)!!
            val colDescription: TextView = vi.findViewById(R.id.colDescription)
            val fileName = colDescription.tag.toString()
            if (fileName.isNotEmpty()){
                val file = File(fileName)
                if (file.exists()) {
                    file.delete()
                    fillList()
                }
            }
            return true
        }
        return super.onContextItemSelected(item)
    }

    private fun validateDatabase(doc: Document) {
        // ConstantsLocal.registration != DeviceInfoUtils.getIMEI(this)
        // ConstantsLocal.isBackdoorOpen
        try {
            val nlDatabase = doc.getElementsByTagName("database")
            if (nlDatabase.length > 0) {
                val nlTable = doc.getElementsByTagName("table")
                val j1 = nlTable.length
                for (i1 in 0 until j1) {
                    val strTable = nlTable.item(i1).attributes.item(0).nodeValue
                    if (strTable.toString().lowercase() == TableSysteem.TABLE_NAME.lowercase()) {
                        val nlRow = nlTable.item(i1).childNodes
                        val j2 = nlRow.length
                        for (i2 in 0 until j2) {
                            val nlCol = nlRow.item(i2).childNodes
                            val j3 = nlCol.length
                            var bVersion = false
                            var bRegistration = false
                            for (i3 in 0 until j3) {
                                if (nlCol.item(i3).firstChild == null
                                    || nlCol.item(i3).firstChild.nodeValue == null
                                    || nlCol.item(i3).firstChild.nodeValue == "null") {
                                    continue
                                }
                                val colName = nlCol.item(i3).attributes.item(0).nodeValue?:""
                                val value = nlCol.item(i3).firstChild?.nodeValue?:""
                                if (colName == TableSysteem.Columns.systeemkey.name){
                                    when (value){
                                        SystemAttr.Version.internal.toString() -> bVersion = true
                                        SystemAttr.Registration.internal.toString() -> bRegistration = true
                                    }
                                } else if (colName == TableSysteem.Columns.systeemvalue.name){
                                    if (value != "") {
                                        if (bVersion){
                                            // Constants.DATABASE_VERSION
                                            nVersion = value.toInt()
                                        } else if (bRegistration){
                                            sRegistration = value
                                        }
                                    }
                                }
                            }
                        }
                        return
                    }
                }
            }
        } catch (t: Throwable) {
            Logging.e("Error restoring", t.toString())
            strMess = getString( R.string.mess024_restore_error)
        }
    }

    internal inner class RestoreLoader(contextp: Context) : Thread() {
        //private var exp: DatabaseHelper = DatabaseHelper(context)
        var context = contextp
        var seqno = 1
        private var backupType = "xml"
        @JvmName("setSeqno_int")
        fun setSeqno(seqno: Int, backupType: String) {
            this.seqno = seqno
            this.backupType = backupType
        }

        override fun run() {
            try {
                val exp = DatabaseHelper(context)
                val dbNameExt = Constants.DATABASE_NAME.split(".")[0]
                var fileName = dbNameExt
                when (backupType){
                    "xml", "db" -> fileName += "_${seqno}.$backupType"
                    "nopw" -> fileName += "-${backupType}_$seqno.db"
                }
                val file = File(dir, fileName)
                when (backupType){
                    "db" -> {
                        file.copyTo(File(Constants.db.path!!),true)
                        ActivityCompat.finishAffinity(this@ListRestore)
                        val intent = Intent(applicationContext, SplashScreen::class.java)
                        ContextCompat.startActivity(this@ListRestore, intent, null)
                    }
                    "nopw" -> {
                        DatabaseHelper(context).importDatabaseEncrypted(seqno, dir.path)
                    }
                    "xml" -> {
                        val doc = if (ConstantsLocal.isBackdoorOpen) {
                            exp.getDocument(fileName, dir, "")
                        } else {
                            exp.getDocument(
                                fileName,
                                dir,
                                BuildConfig.AD_MOB_APP_ID.reversed()
                            )
                        }
                        if (doc == null) {
                            strMess = getString(R.string.mess024_restore_error)
                        } else {
                            var bOk = true
                            if (!ConstantsLocal.isBackdoorOpen){
                                validateDatabase(doc)
                                if (strMess!=""){
                                    bOk = false
                                } else if (nVersion != Constants.DATABASE_VERSION){
                                    strMess = getString( R.string.mess048_restore_error_version)
                                    bOk = false
                                } else if (sRegistration != DeviceInfoUtils.getIMEI(this@ListRestore)){
                                    strMess = getString(R.string.mess049_restore_error_registration)
                                    bOk = false
                                }
                            }
                            if (bOk){
                                strMess = exp.importDatabase(doc).mess
                            }
                        }
                    }
                }

            } catch (e: Exception) {
                strMess = buildString {
                    append(getString(com.farrusco.projectclasses.R.string.mess025_Error))
                    append(e.message)
                }
            } finally {
                messDialog?.dismissDialog()
            }
            finishedHandler.post { stopThread() }
        }
    }

    fun stopThread() {
        if (strMess != "") ToastExt().makeText(this@ListRestore, strMess, Toast.LENGTH_LONG)
            .show() else if (mess > 0) ToastExt().makeText(this@ListRestore, mess, Toast.LENGTH_LONG).show()

    }

    override fun onDestroy() {
        if (restoreLoader != null) restoreLoader = null
        super.onDestroy()
    }

}