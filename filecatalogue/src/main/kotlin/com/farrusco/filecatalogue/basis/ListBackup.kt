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

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.*
import com.farrusco.filecatalogue.BuildConfig
import com.farrusco.filecatalogue.R
import com.farrusco.filecatalogue.business.Help
import com.farrusco.filecatalogue.business.Systeem
import com.farrusco.filecatalogue.common.ConstantsLocal
import com.farrusco.filecatalogue.common.SystemAttr
import com.farrusco.filecatalogue.tables.TableSysteem
import com.farrusco.projectclasses.activity.BaseActivityTableLayout
import com.farrusco.projectclasses.common.Constants
import com.farrusco.projectclasses.common.ConstantsFixed
import com.farrusco.projectclasses.databases.DatabaseHelper
import com.farrusco.projectclasses.messages.Mess
import com.farrusco.projectclasses.messages.ToastExt
import com.farrusco.projectclasses.utils.ViewUtils
import com.farrusco.projectclasses.widget.ButtonExt
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.text.SimpleDateFormat

class ListBackup : BaseActivityTableLayout() {
    override val layoutResourceId: Int = R.layout.list_backup_restore
    override val mainViewId: Int = R.id.viewMain
    private var systeem = Systeem(this)

    private var backupLoader: BackupLoader? = null
    private var mess = ""
    private var strMess = ""
    private val finishedHandler = Handler(Looper.getMainLooper())
    private var messDialog: Mess? = null
    private lateinit var dir: File

    override fun initTableLayout() {
        addTableLayout(
            R.id.tblLayout,
            R.layout.line_backup,
            systeem,
            arrayOf(TableSysteem.Columns.description.name, TableSysteem.Columns.systeemvalue.name),
            arrayListOf( ConstantsFixed.DELETE_ID, ConstantsFixed.HELP_ID),
            null
        )
    }

    override fun initActivity() {
        helpText = Help().getHelpTitle(className)
        initTableLayout()
        fillList()
    }

    @SuppressLint("InflateParams")
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
            dir.mkdirs()
            if (!dir.exists()) return
        }
        val li: LayoutInflater = layoutInflater
        var max: Int
        try {
            max = systeem.getValue(SystemAttr.Backups).toInt()
            if (max < 1) max=10
        } catch (_: Exception){
            max = 10
        }
        for (seqno in 1 until max+1){
            BuildConfig.BACKUP.split(",").forEach {
                addLine(mTblLayout, li, it, seqno)
            }
        }
        setTableRowMenu(mTblLayout)
    }

    @SuppressLint("SetTextI18n")
    private fun addLine(mTblLayout: TableLayout, li: LayoutInflater, backupType: String, seqno: Int){
        val vi: View = li.inflate(R.layout.line_backup, null)
        val dbNameExt = Constants.DATABASE_NAME.split(".")[0]
        val colDescription: TextView = vi.findViewById(R.id.colDescription)
        val mBtnBackup = vi.findViewById<ButtonExt>(R.id.btnStartBackup)
        val btnMenu = vi.findViewWithTag<ButtonExt>(ConstantsFixed.popupmenu)
        var fileName = dbNameExt
        when (backupType){
            "xml", "db" -> fileName += "_${seqno}.$backupType"
            "nopw" -> fileName += "-${backupType}_$seqno.db"
        }
        val file = File(dir, fileName)

        if (file.exists()) {
            colDescription.tag = "$dir/${file.name}"

            val attrs = Files.readAttributes(file.toPath(), BasicFileAttributes::class.java)
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val creationDate = formatter.format(attrs.creationTime().toMillis())

            colDescription.text = creationDate
            if (btnMenu != null) btnMenu.isEnabled = true
        } else{
            if (BuildConfig.BACKUP.indexOf(',') < 0){
                colDescription.text = getText(R.string.empty).toString().replace(")"," $seqno)")
            } else {
                colDescription.text = getText(R.string.empty).toString().replace(")"," $seqno $backupType)")
            }
            colDescription.tag = ""
            if (btnMenu != null) btnMenu.isEnabled = false
        }

        if (BuildConfig.BACKUP.indexOf(',') < 0){
            mBtnBackup.text = mBtnBackup.text.toString() + " ($seqno)"
        } else {
            mBtnBackup.text = mBtnBackup.text.toString() + " ($seqno $backupType)"
        }

        mBtnBackup.setOnClickListener(mLocalBackupListener)
        mBtnBackup.tag = "$seqno,$backupType"
        mTblLayout.addView(vi, TableLayout.LayoutParams())
    }

    private val mLocalBackupListener = View.OnClickListener {

        val mRowSearch = it.parent as TableRow
        val mBtnBackup = mRowSearch.findViewById<ButtonExt>(R.id.btnStartBackup)
        val alertDialog = Mess.buildAlertDialog(this,layoutInflater,
            getString(R.string.backup),
            getString( R.string.mess_DoYouWantToBackup))
        with(alertDialog){
            setPositiveButton(
                com.farrusco.projectclasses.R.string.yes
            ) { _, _ ->
                messDialog = Mess(this@ListBackup, getText(R.string.mess026_one_moment).toString())
                backupLoader = BackupLoader(this@ListBackup)
                val arrTag = mBtnBackup.tag.toString().split(",")
                backupLoader!!.setSeqno(arrTag[0].toInt(), arrTag[1], dir)
                backupLoader!!.start()
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

    internal inner class BackupLoader(contextp: Context) : Thread() {
        var context: Context = contextp
        var seqno = 1
        var xml = "xml"
        private lateinit var dir: File
        @JvmName("setSeqno_int")
        fun setSeqno(seqno: Int, xml: String, dir: File) {
            this.seqno = seqno
            this.xml = xml
            this.dir = dir
        }

        override fun run() {
            try {
                val exp = DatabaseHelper(context)
                when (xml){
                    "xml" -> {
                        mess = if (ConstantsLocal.isBackdoorOpen){
                            exp.export(seqno, dir.path, "")
                        } else{
                            exp.export(seqno, dir.path, BuildConfig.AD_MOB_APP_ID.reversed())
                        }
                    }
                    "nopw" -> {
                        exp.exportDatabaseUnencrypted(seqno, dir.path)
                    }
                    "db" ->{
                        exp.exportDb(seqno, dir.path)
                    }
                }
            } catch (e: Exception) {
                strMess = getString(com.farrusco.projectclasses.R.string.mess025_Error) + e.message
            } finally {
                messDialog?.dismissDialog()
            }
            finishedHandler.post { stopThread() }
        }
    }

    fun stopThread() {
        if (strMess != "") ToastExt().makeText(this@ListBackup, strMess, Toast.LENGTH_LONG)
            .show() else if (mess != "") ToastExt().makeText(this@ListBackup, mess, Toast.LENGTH_LONG).show()
        fillList()
    }

    override fun onDestroy() {
        if (backupLoader != null) backupLoader = null
        super.onDestroy()
    }
}