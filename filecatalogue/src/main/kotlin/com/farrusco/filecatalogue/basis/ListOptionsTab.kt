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
import android.content.Intent
import android.database.Cursor
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import androidx.viewpager2.widget.ViewPager2
import com.farrusco.filecatalogue.R
import com.farrusco.filecatalogue.business.Category
import com.farrusco.filecatalogue.business.Help
import com.farrusco.filecatalogue.business.Systeem
import com.farrusco.filecatalogue.common.SystemAttr
import com.farrusco.filecatalogue.common.SystemMainAttr
import com.farrusco.filecatalogue.tables.TableSysteem
import com.farrusco.projectclasses.activity.BaseActivityTableLayout
import com.farrusco.projectclasses.common.Constants
import com.farrusco.projectclasses.common.ConstantsFixed
import com.farrusco.projectclasses.databases.tables.CursorX.getColumnValueInt
import com.farrusco.projectclasses.databases.tables.CursorX.getColumnValueString
import com.farrusco.projectclasses.filepicker.controller.DialogSelectionListener
import com.farrusco.projectclasses.filepicker.model.DialogConfigs
import com.farrusco.projectclasses.filepicker.model.DialogProperties
import com.farrusco.projectclasses.filepicker.view.FilePickerDialog
import com.farrusco.projectclasses.utils.StringUtils
import com.farrusco.projectclasses.utils.TagModify
import com.farrusco.projectclasses.utils.ViewUtils
import com.farrusco.projectclasses.widget.ButtonExt
import com.farrusco.projectclasses.widget.EditTextExt
import com.farrusco.projectclasses.widget.SwitchExt
import com.farrusco.projectclasses.widget.TextViewExt
import com.farrusco.projectclasses.widget.tablayout.TabLayoutExt
import com.farrusco.projectclasses.widget.tablayout.TabsPagerAdapterExt
import java.io.File
import androidx.core.view.isNotEmpty
import com.farrusco.projectclasses.widget.validators.InputFilterMinMax

class ListOptionsTab : BaseActivityTableLayout() {
    override val layoutResourceId: Int = R.layout.list_options_tab
    override val mainViewId: Int = R.id.viewMain
    private lateinit var systeem: Systeem
    private var sizeTextView = 0F
    private var sizeEditText = 0F
    private lateinit var mTabsViewpager: ViewPager2
    private lateinit var mTabLayout: TabLayoutExt
    private var mTblLayout = arrayOfNulls<TableLayout>(6)
    //private var mHandler: Handler? = null

    override fun initActivity() {
        helpText = Help().getHelpTitle(className)
        systeem = Systeem(this)
        sizeTextView = ViewUtils.getDip(R.dimen.font_size_textview, this)
        sizeEditText = ViewUtils.getDip(R.dimen.font_size_edittext, this)

        mTabLayout = findViewById(R.id.tab_layout)
        mTabsViewpager = findViewById(R.id.tabs_viewpager)

        initTabs()
        initTableLayout()
        //mHandler = Handler(Looper.getMainLooper())
        //mHandler!!.postDelayed(runnable, 10)
    }

/*    private val runnable=object : Runnable {
        override fun run() {
            if (!fillList(0)) {
                mHandler!!.postDelayed(this, 100)
            } else {
                mHandler!!.removeCallbacks(this)
            }
        }
    }*/

    override fun initTableLayout() {
        for(idx in 1 until 7){
            val id =  when (idx){
                1 -> R.id.tblLayout1
                2 -> R.id.tblLayout2
                3 -> R.id.tblLayout3
                4 -> R.id.tblLayout4
                5 -> R.id.tblLayout5
                6 -> R.id.tblLayout6
                else -> R.id.tblLayout1
            }

            addTableLayout(
                id,
                R.layout.line_textview1x,
                systeem,
                arrayOf(TableSysteem.Columns.description.name),
                arrayListOf(ConstantsFixed.HELP_ID,ConstantsFixed.SAVE_ID, ConstantsFixed.EDIT_ID),
                null
            )
        }
    }

    private fun initTabs() {

        val adapter =
            TabsPagerAdapterExt(this, Constants.localFragmentManager!!, Constants.localLifecycle!! )

        adapter.setResources(mTabLayout, mTabsViewpager
            ,arrayListOf( R.layout.list_options_tab_frag1, R.layout.list_options_tab_frag2, R.layout.list_options_tab_frag3, R.layout.list_options_tab_frag4
                , R.layout.list_options_tab_frag5, R.layout.list_options_tab_frag6)
            ,arrayListOf(translateOptions(SystemMainAttr.System.name)
                ,translateOptions(SystemMainAttr.Category.name)
                ,translateOptions(SystemMainAttr.Search.name)
                ,translateOptions(SystemMainAttr.ListFiles.name)
                ,translateOptions(SystemMainAttr.File.name)
                ,translateOptions(SystemMainAttr.Order.name)))
        mTabLayout.setOnTabClickListener(object : TabLayoutExt.OnTabClickListener {
            override fun onTabClicked(position: Int) {
                ViewUtils.hideKeyboard(mTabLayout , this@ListOptionsTab)
                fillList(position)
            }
        })
    }

    override fun onFragmentAttach(link: String?) {
        if (link != null){
            fillList(link.replace("f","").toInt())
        }
    }

    private fun fillList(position: Int): Boolean {
        updateTableLayouts(position)
        if (mTblLayout[position] == null || mTblLayout[position]!!.isNotEmpty()) {
            return false
        }
        removeAllTableLayoutViews(position)
        val rtn = systeem.getUserOptions(position+2)
        if (rtn.cursor.moveToFirst()) {
            do {
                insertRow(rtn.cursor, position)
            } while (rtn.cursor.moveToNext())
        }
        rtn.cursorClose()
/*        if (mHandler != null) {
            mHandler!!.removeCallbacks(runnable)
        }*/
        return true
    }

    private fun updateTableLayouts(position:Int) {
        if (mTblLayout[position] == null) {
            when (position) {
                0 -> {
                    mTblLayout[position] = findViewById(R.id.tblLayout1)
                    updateTableLayout(R.id.tblLayout1, mTblLayout[position])
                }

                1 -> {
                    mTblLayout[position] = findViewById(R.id.tblLayout2)
                    updateTableLayout(R.id.tblLayout2, mTblLayout[position])
                }

                2 -> {
                    mTblLayout[position] = findViewById(R.id.tblLayout3)
                    updateTableLayout(R.id.tblLayout3, mTblLayout[position])
                }

                3 -> {
                    mTblLayout[position] = findViewById(R.id.tblLayout4)
                    updateTableLayout(R.id.tblLayout4, mTblLayout[position])
                }

                4 -> {
                    mTblLayout[position] = findViewById(R.id.tblLayout5)
                    updateTableLayout(R.id.tblLayout5, mTblLayout[position])
                }

                5 -> {
                    mTblLayout[position] = findViewById(R.id.tblLayout6)
                    updateTableLayout(R.id.tblLayout6, mTblLayout[position])
                }
            }
        }
    }

    private fun translateOptions(options: String): String{
        var rtn = options
        when (options) {
            SystemMainAttr.System.name -> rtn = getText(R.string.system).toString()
            SystemMainAttr.Category.name -> rtn = getText(R.string.category).toString()
            SystemMainAttr.Search.name -> rtn = getText(R.string.search).toString()
            SystemMainAttr.ListFiles.name -> rtn = getText(R.string.activity_list_files).toString()
            SystemMainAttr.File.name -> rtn = getText(R.string.file).toString()
            SystemMainAttr.Order.name -> rtn = getText(R.string.order).toString()
        }
        return StringUtils.capitalize(rtn)
    }

    private fun translateOptions(cursor: Cursor): String?{

        when (cursor.getColumnValueInt(TableSysteem.Columns.systeemkey.name)) {
            SystemAttr.Version.internal -> return getText(R.string.systemattr_01).toString()
            SystemAttr.BackupFolder.internal -> return getText(R.string.systemattr_02).toString()
            SystemAttr.FileExtension.internal -> return getText(R.string.systemattr_03).toString()
            SystemAttr.CvsName.internal -> return getText(R.string.systemattr_04).toString()
            SystemAttr.About.internal -> return getText(R.string.systemattr_05).toString()
            SystemAttr.Help.internal -> return getText(R.string.systemattr_06).toString()
            SystemAttr.Tooltip.internal -> return getText(R.string.systemattr_07).toString()
            SystemAttr.Orders.internal -> return getText(R.string.systemattr_08).toString()
            SystemAttr.Price.internal -> return getText(R.string.systemattr_09).toString()
            SystemAttr.GbWhatsApp.internal -> return getText(R.string.systemattr_10).toString()
            SystemAttr.AutoNew.internal -> return getText(R.string.systemattr_11).toString()
            SystemAttr.ReorgDb.internal -> return getText(R.string.systemattr_12).toString()
            SystemAttr.Listfiles.internal -> return getText(R.string.systemattr_13).toString()
            SystemAttr.LastPath.internal -> return getText(R.string.systemattr_14).toString()
            SystemAttr.SalesEMail.internal -> return getText(R.string.systemattr_15).toString()
            SystemAttr.SalesName.internal -> return getText(R.string.systemattr_16).toString()
            SystemAttr.SalesAddress.internal -> return getText(R.string.systemattr_17).toString()
            SystemAttr.StoreLastQuery.internal -> return getText(R.string.systemattr_18).toString()
            SystemAttr.GPSInformation.internal -> return getText(R.string.systemattr_19).toString()
            SystemAttr.DeleteEmptyProduct.internal -> return getText(R.string.systemattr_20).toString()
            SystemAttr.Backups.internal -> return getText(R.string.systemattr_21).toString()
            SystemAttr.DeleteFilePhone.internal -> return getText(R.string.systemattr_22).toString()
            SystemAttr.SmartCopyCategory.internal -> return getText(R.string.systemattr_23).toString()
            SystemAttr.Registration.internal -> return getText(R.string.systemattr_24).toString()
            SystemAttr.StoreQuery.internal -> return getText(R.string.systemattr_25).toString()
            SystemAttr.AutoRefreshFolder.internal -> return getText(R.string.systemattr_26).toString()
            SystemAttr.ScanImage.internal -> return getText(R.string.systemattr_27).toString()
            SystemAttr.TranslateText.internal -> return getText(R.string.systemattr_28).toString()
            SystemAttr.ScanQRCode.internal -> return getText(R.string.systemattr_29).toString()
            SystemAttr.SingleListLabel.internal -> return getText(R.string.systemattr_30).toString()
            SystemAttr.SearchFilesAvailable.internal -> return getText(R.string.systemattr_31).toString()
            SystemAttr.SearchFilesHidden.internal -> return getText(R.string.systemattr_32).toString()
            SystemAttr.SortCreationYearDesc.internal -> return getText(R.string.systemattr_33).toString()
            SystemAttr.SortCreationMonthDesc.internal -> return getText(R.string.systemattr_34).toString()
            SystemAttr.OrderSeqnoDirectory.internal -> return getText(R.string.systemattr_37).toString()
            SystemAttr.OrderSeqnoCreation.internal -> return getText(R.string.systemattr_38).toString()
            SystemAttr.OrderSeqnoExtension.internal -> return getText(R.string.systemattr_39).toString()
            SystemAttr.OrderSeqnoGPS.internal -> return getText(R.string.systemattr_40).toString()
            SystemAttr.FileFancyScroll.internal -> return getText(R.string.filefancyscroll_41).toString()
            SystemAttr.OrderFace.internal -> return getText(R.string.systemattr_42).toString()
            SystemAttr.FaceRecognition.internal -> return getText(R.string.systemattr_43).toString()
            SystemAttr.FaceDistance.internal -> return getText(R.string.systemattr_44).toString()
            }
        return cursor.getColumnValueString(TableSysteem.Columns.description.name)
    }

    private fun insertRow(cursor: Cursor, position: Int) {
        when (cursor.getColumnValueString(TableSysteem.Columns.inputtype.name)!!){
            "1" -> insertRowTv(cursor, position) // text
            "2" -> insertRowSwitch(cursor, position) // yes/no
            "3" -> insertRowNv(cursor, position)  // numeric
            "4" -> insertRowMl(cursor, position) // multiline
            "5" -> insertRowFs(cursor, position) // folder select
            "6" -> insertRowDc(cursor, position) // decimal input
        }
    }

    private fun insertRowSwitch(cursor: Cursor, position: Int) {

        val vi = layoutInflater.inflate(R.layout.line_optionsw, null ) as TableRow
        val fieldSwitch = vi.findViewById<SwitchExt>(R.id.colSwitch)

        TagModify.setViewTagValue(fieldSwitch,ConstantsFixed.TagSection.TsDBColumn.name,TableSysteem.Columns.description.name)
        TagModify.setViewTagValue(fieldSwitch,ConstantsFixed.TagSection.TsDBColumnBack.name,TableSysteem.Columns.systeemvalue.name)
        TagModify.setViewTagValue(fieldSwitch,ConstantsFixed.TagSection.TsGroupno.name,0)
        TagModify.setViewTagValue(fieldSwitch,ConstantsFixed.TagSection.TsModFlag, ConstantsFixed.TagAction.Edit.name )

        ViewUtils.copyCursorToViewGroup(cursor, vi, 0)
        fieldSwitch.text = translateOptions(cursor)
        mTblLayout[position]?.addView(vi, TableLayout.LayoutParams())
    }

    @SuppressLint("InflateParams")
    // numeric
    private fun insertRowNv(cursor: Cursor, position: Int) {
        val vi = layoutInflater.inflate(R.layout.line_optionsnv, null )  as ViewGroup
        TagModify.setViewTagValue(vi,ConstantsFixed.TagSection.TsModFlag, ConstantsFixed.TagAction.Edit.name )
        val fieldTextView = vi.findViewById<TextViewExt>(R.id.tvLabel)
        val fieldEditText = vi.findViewById<EditTextExt>(R.id.tvValue)
        TagModify.setViewTagValue(fieldTextView,ConstantsFixed.TagSection.TsDBColumn.name,TableSysteem.Columns.description.name)
        TagModify.setViewTagValue(fieldEditText,ConstantsFixed.TagSection.TsDBColumn.name,TableSysteem.Columns.systeemvalue.name)
        fieldEditText.setMaxLength = 2
        when (cursor.getColumnValueInt(TableSysteem.Columns.systeemkey.name)){
            SystemAttr.StoreQuery.internal -> fieldEditText.setMaxLength = 1
        }
        ViewUtils.copyCursorToViewGroup(cursor, vi, 0)
        fieldTextView.text = translateOptions(cursor)
        mTblLayout[position]?.addView(vi, TableLayout.LayoutParams())
    }

    private fun insertRowDc(cursor: Cursor, position: Int) {
        val vi = layoutInflater.inflate(R.layout.line_optionsdc, null )  as ViewGroup
        TagModify.setViewTagValue(vi,ConstantsFixed.TagSection.TsModFlag, ConstantsFixed.TagAction.Edit.name )
        val fieldTextView = vi.findViewById<TextViewExt>(R.id.tvLabel)
        val fieldEditText = vi.findViewById<EditTextExt>(R.id.tvValue)
        TagModify.setViewTagValue(fieldTextView,ConstantsFixed.TagSection.TsDBColumn.name,TableSysteem.Columns.description.name)
        TagModify.setViewTagValue(fieldEditText,ConstantsFixed.TagSection.TsDBColumn.name,TableSysteem.Columns.systeemvalue.name)
        fieldEditText.setMaxLength = 5
        when (cursor.getColumnValueInt(TableSysteem.Columns.systeemkey.name)){
            SystemAttr.FaceDistance.internal -> {
                fieldEditText.filters = arrayOf( InputFilterMinMax(0f, 1.0f))
            }
        }
        ViewUtils.copyCursorToViewGroup(cursor, vi, 0)
        fieldTextView.text = translateOptions(cursor)
        mTblLayout[position]?.addView(vi, TableLayout.LayoutParams())
    }

    private fun insertRowTv(cursor: Cursor, position: Int) {
        val vi = layoutInflater.inflate(R.layout.line_optiontv, null )  as ViewGroup
        val fieldTextView = vi.findViewById<TextViewExt>(R.id.colTextViewExt)
        val fieldEditText = vi.findViewById<EditTextExt>(R.id.colEditText)

        TagModify.setViewTagValue(fieldTextView,ConstantsFixed.TagSection.TsDBColumn.name,TableSysteem.Columns.description.name)
        TagModify.setViewTagValue(fieldEditText,ConstantsFixed.TagSection.TsModFlag, ConstantsFixed.TagAction.Edit.name )
        TagModify.setViewTagValue(fieldEditText,ConstantsFixed.TagSection.TsDBColumn.name,TableSysteem.Columns.systeemvalue.name)
        ViewUtils.setTextMaxLength(fieldEditText,TableSysteem.TABLE_NAME)

        ViewUtils.copyCursorToViewGroup(cursor, vi, 0)
        fieldTextView.text = translateOptions(cursor)
        mTblLayout[position]?.addView(vi, TableLayout.LayoutParams())
    }

    @SuppressLint("InflateParams")
    private fun insertRowMl(cursor: Cursor, position: Int) {
        val vi = layoutInflater.inflate(R.layout.line_optionsml, null) as ViewGroup
        TagModify.setViewTagValue(vi,ConstantsFixed.TagSection.TsModFlag, ConstantsFixed.TagAction.Edit.name )
        val fieldTextView = vi.findViewById<TextViewExt>(R.id.tvLabel)
        val fieldEditText = vi.findViewById<EditTextExt>(R.id.tvValue)
        TagModify.setViewTagValue(fieldTextView,ConstantsFixed.TagSection.TsDBColumn.name,TableSysteem.Columns.description.name)
        TagModify.setViewTagValue(fieldEditText,ConstantsFixed.TagSection.TsDBColumn.name,TableSysteem.Columns.systeemvalue.name)
        ViewUtils.setTextMaxLength(fieldEditText,TableSysteem.TABLE_NAME)
        ViewUtils.copyCursorToViewGroup(cursor, vi, 0)
        fieldTextView.text = translateOptions(cursor)
        mTblLayout[position]?.addView(vi, TableLayout.LayoutParams())
    }

    @SuppressLint("InflateParams", "SetTextI18n")
    private fun insertRowFs(cursor: Cursor, position: Int) {

        val vi = layoutInflater.inflate(R.layout.line_selectfolder, null) as ViewGroup
        TagModify.setViewTagValue(vi,ConstantsFixed.TagSection.TsModFlag, ConstantsFixed.TagAction.Edit.name )

        val fieldTextView = vi.findViewById<TextViewExt>(R.id.colFolder)

        TagModify.setViewTagValue(fieldTextView,ConstantsFixed.TagSection.TsDBColumn.name,TableSysteem.Columns.description.name)

        fieldTextView.text = translateOptions(cursor)
        TagModify.setViewTagValue(fieldTextView,ConstantsFixed.TagSection.TsDBColumn.name,TableSysteem.Columns.systeemvalue.name)

        val mBtnDir = vi.findViewById<ButtonExt>(R.id.btnSelectFolder)
        val lastPath = Systeem(this).getValue(SystemAttr.BackupFolder)
        if (lastPath.isEmpty()){
            mBtnDir.text = getText(R.string.lblbackup).toString()+ " " + getText(R.string.directory)
        }else{
            mBtnDir.text = getText(R.string.reset).toString() + " " + getText(R.string.lblbackup)
        }
        mBtnDir.setOnClickListener(mLocalBtnDirListener)

        ViewUtils.copyCursorToViewGroup(cursor, vi, 0)
        mTblLayout[position]?.addView(vi, TableLayout.LayoutParams())
    }

    private val mLocalBtnDirListener = View.OnClickListener { //openFile();

        val lastPath = Systeem(this).getValue(SystemAttr.BackupFolder)
        val mRowSearch = it.parent as TableRow
        val mColFolder = mRowSearch.findViewById<TextViewExt>(R.id.colFolder)
        val mBtnDir = mRowSearch.findViewById<ButtonExt>(R.id.btnSelectFolder)
        if (lastPath.isNotEmpty()){
            mColFolder.text = ""
            Systeem(this).setValue(
                SystemAttr.BackupFolder,
                "",
                3
            )
            mBtnDir.text = buildString {
                append(getString(R.string.lblbackup))
                append(" ")
                append(getString(R.string.directory))
            }
        } else {
            Systeem(this).getProperties()
            val properties = DialogProperties()
            properties.dir = DialogConfigs.STORAGE_STORAGE
            properties.errorDir = File(properties.dir)
            properties.root =  File(properties.dir)
            if (lastPath.isNotEmpty()){
                properties.offset= File(lastPath)
            } else if (mColFolder.text.isNullOrBlank()){
                properties.offset = File(properties.dir)
            } else{
                properties.offset = File(mColFolder.text.toString())
            }
            properties.extensions = arrayOf("")
            properties.selectionType = DialogConfigs.DIR_SELECT
            properties.selectionMode = DialogConfigs.SINGLE_MODE
            properties.title = getText(R.string.selectfolder).toString()

            val dialog =  FilePickerDialog(this,properties)
            dialog.setTitle(getText(R.string.selectfolder))

            dialog.setDialogSelectionListener(object : DialogSelectionListener {
                override fun onSelectedFilePaths(lastPath:String?, files: Array<String?>?) {
                    if (files != null) {
                        for (path in files) {
                            if (mColFolder.text.toString() != path.toString()){
                                mColFolder.text = path.toString()
                                Systeem(this@ListOptionsTab).setValue(
                                    SystemAttr.BackupFolder,
                                    path,
                                    3
                                )
                                mBtnDir.text = buildString {
                                    append(getString(R.string.reset))
                                    append(" ")
                                    append(getString(R.string.lblbackup))
                                }
                                break
                            }
                        }
                    }
                    //files is the array of paths selected by the App User.

                }

                override fun onCancelPaths(lastPath: String?) {
                    if (lastPath != null) {
                        Systeem(this@ListOptionsTab).setValue(
                            SystemAttr.BackupFolder,
                            lastPath,
                            3
                        )
                    }
                }
            })
            dialog.show()
        }
    }

    override fun exitOnBackPressed() {
        val rtn = saveScreen()
        if (rtn.returnValue){
            Category(this).syncQuery()
            Systeem(this).getProperties()
            setResult(RESULT_OK, Intent())
            finish()
        }
    }

}