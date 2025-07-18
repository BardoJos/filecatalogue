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

import android.content.ContentValues
import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.children
import androidx.core.view.indices
import androidx.viewpager2.widget.ViewPager2
import com.farrusco.filecatalogue.BuildConfig
import com.farrusco.filecatalogue.R
import com.farrusco.filecatalogue.business.Category
import com.farrusco.filecatalogue.business.Help
import com.farrusco.filecatalogue.business.OrderLine
import com.farrusco.filecatalogue.business.Product
import com.farrusco.filecatalogue.business.ProductRel
import com.farrusco.filecatalogue.business.Systeem
import com.farrusco.filecatalogue.common.CategoryDetail
import com.farrusco.filecatalogue.common.CategoryLine
import com.farrusco.filecatalogue.common.ConstantsLocal
import com.farrusco.filecatalogue.common.SystemAttr
import com.farrusco.filecatalogue.tables.TableCategory
import com.farrusco.filecatalogue.tables.TableProduct
import com.farrusco.filecatalogue.utils.PageManager
import com.farrusco.projectclasses.activity.BaseActivityTableLayout
import com.farrusco.projectclasses.common.Constants
import com.farrusco.projectclasses.common.ConstantsFixed
import com.farrusco.projectclasses.databases.tables.DBUtils
import com.farrusco.projectclasses.databases.tables.Tables
import com.farrusco.projectclasses.filepicker.controller.DialogSelectionListener
import com.farrusco.projectclasses.filepicker.model.DialogConfigs
import com.farrusco.projectclasses.filepicker.model.DialogProperties
import com.farrusco.projectclasses.filepicker.view.FilePickerDialog
import com.farrusco.projectclasses.utils.CalcObjects
import com.farrusco.projectclasses.utils.FilesFolders
import com.farrusco.projectclasses.utils.FolderProduct
import com.farrusco.projectclasses.messages.Mess
import com.farrusco.projectclasses.utils.ReturnValue
import com.farrusco.projectclasses.utils.StringUtils
import com.farrusco.projectclasses.utils.TagModify
import com.farrusco.projectclasses.messages.ToastExt
import com.farrusco.projectclasses.utils.ContentValuesExt
import com.farrusco.projectclasses.utils.ViewUtils
import com.farrusco.projectclasses.widget.ButtonExt
import com.farrusco.projectclasses.widget.EditTextExt
import com.farrusco.projectclasses.widget.SwitchExt
import com.farrusco.projectclasses.widget.TextViewExt
import com.farrusco.projectclasses.widget.tablayout.TabLayoutExt
import com.farrusco.projectclasses.widget.tablayout.TabsPagerAdapterExt
import java.io.File
import androidx.core.view.isNotEmpty

class DetailFileDirsTab : BaseActivityTableLayout() {

    override val layoutResourceId: Int = R.layout.detail_filedirs_tab
    override val mainViewId: Int = R.id.viewMain

    private var orderLine: OrderLine = OrderLine()
    private lateinit var category: Category
    private var productRel: ProductRel = ProductRel(this)
    private lateinit var mTabsViewpager: ViewPager2
    private lateinit var mTabLayout: TabLayoutExt
    private var mTblLayout1: TableLayout? = null
    private var mTblLayout2: TableLayout? = null
    private var messDialog: Mess? = null
    private var productid: Int = 0
    private var categoryid: Int = 0
    private var mType = 0
    private lateinit var mSubType: SwitchExt

    private var arrCategoryMut: MutableList<CategoryDetail> = mutableListOf()
    private lateinit var  arrCategoryDesc: ArrayList<CategoryLine>
    private var arrFolderProduct: MutableList<FolderProduct> = mutableListOf()

    private var pageManager = PageManager()

    private lateinit var mEditText3: EditTextExt

    private lateinit var product: Product
    private lateinit var systeem: Systeem
    private var mainid: Int = 0

    private lateinit var mBtnDir: ButtonExt
    private lateinit var mTitle: EditTextExt
    private lateinit var mDescription: EditTextExt
    private lateinit var mLocation: TextViewExt
    private lateinit var mSwRecursive: SwitchExt
    private var bTblLayout2Init = false

    override fun initActivity() {
        helpText = Help().getHelpTitle(className)
        product = Product(this)
        systeem = Systeem(this)
        category  = Category(this)

        mainid = DBUtils.eLookUpInt(TableCategory.Columns._id.name, TableCategory.TABLE_NAME,
            "${TableCategory.Columns._mainid.name} = 0 " +
                    " and ${TableCategory.Columns.type.name} = ${ConstantsLocal.TYPE_DIRECTORY} " +
                    " and ${TableCategory.Columns.level.name} = 1" )

        arrCategoryDesc = category.fillCategoryArray(arrayListOf(ConstantsLocal.TYPE_CATEGORY),
            inclSeqno = true,
            inclCount = false
        )

        orderLine = OrderLine()
        messDialog = Mess(this, getText(R.string.mess027_please_wait).toString())

        mTabLayout = findViewById(R.id.tab_layout)
        mTabsViewpager = findViewById(R.id.tabs_viewpager)
        productid = CalcObjects.stringToInteger(intent.getStringExtra(TableProduct.Columns._id.name),0)
        categoryid = CalcObjects.stringToInteger(intent.getStringExtra(TableProduct.Columns._categoryid.name),0)
        mType = CalcObjects.stringToInteger(intent.getStringExtra(TableProduct.Columns.type.name),0)
        if (intent.hasExtra("category")) {
            val code = CalcObjects.getString(intent.getStringExtra("category"))
            arrCategoryMut = pageManager.putCodeInCategoryMut(code)
        }

        mEditText3 = EditTextExt(this)
        ViewUtils.setDBColumn(mEditText3, "category", null, groupNo)
        mEditText3.visibility = View.GONE
        (viewMain  as ViewGroup).addView(mEditText3)
        initTabs()
    }

    override fun initTableLayout() {
        addTableLayout(
            R.id.tblLayout1,
            R.layout.line_textview,
            category,
            arrayOf(
                TableCategory.Columns.description.name
            ),
            null,
            null
        )
        addTableLayout(
            R.id.tblLayout2,
            R.layout.line_textview,
            category,
            arrayOf(
                TableCategory.Columns.description.name
            ),
            null,
            null
        )
    }

    private fun initTabs() {

        val adapter =
            TabsPagerAdapterExt(this, Constants.localFragmentManager!!, Constants.localLifecycle!! )
        adapter.setResources(mTabLayout, mTabsViewpager
            ,arrayListOf( R.layout.detail_filedirs_tab_frag0, R.layout.detail_filedirs_tab_frag1, R.layout.detail_filedirs_tab_frag2 )
            ,arrayListOf(getText(R.string.details).toString(),getText(R.string.category).toString(),getText(R.string.folder).toString()  ))
        adapter.isUserInputEnabled = false
        mTabLayout.setOnTabClickListener(object : TabLayoutExt.OnTabClickListener {
            override fun onTabClicked(position: Int) {
                when (position) {
                    0 -> fillList0()
                    1 -> {
                        // hide keyboard
                        //val imm=getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        //imm.hideSoftInputFromWindow(currentFocus?.windowToken, InputMethodManager.SHOW_FORCED)

                        ViewUtils.hideKeyboard(mTabLayout , this@DetailFileDirsTab)
                        fillList1()
                    }
                    2 -> {
                        // hide keyboard
                        ViewUtils.hideKeyboard(mTabLayout , this@DetailFileDirsTab)
                        fillList2()
                    }
                }
            }
        })
    }

    private fun fillList0() {
        if (!this::mDescription.isInitialized
            && findViewById<EditTextExt>(R.id.txtDescription) != null) {
            mDescription = findViewById(R.id.txtDescription)
            mBtnDir = findViewById(R.id.btnDir)
            mTitle = findViewById(R.id.txtTitle)
            mDescription = findViewById(R.id.txtDescription)
            mLocation = findViewById(R.id.txtLocation)
            mSwRecursive = findViewById(R.id.swRecursive)

            mBtnDir.setOnClickListener(mLocalBtnDirListener)

            ViewUtils.setDBColumn(mTitle,TableProduct.Columns.title.name, TableCategory.TABLE_NAME, true)
            mTitle.setMessColumn(TableProduct.Columns.title.name)

            ViewUtils.setDBColumn(mDescription,TableProduct.Columns.description.name, TableCategory.TABLE_NAME, true)
            mDescription.setMessColumn(TableProduct.Columns.description.name)

            ViewUtils.setDBColumn(mSwRecursive,TableProduct.Columns.recursive.name)

            mSwRecursive.setOnCheckedChangeListener { _, isChecked ->
                mSwRecursive.onCheckedChange()
                if (isChecked){
                    arrFolderProduct = FilesFolders().getFolder(mLocation.text.toString(), true)
                } else {
                    arrFolderProduct.clear()
                }
                category.fillFolder( categoryid, arrFolderProduct)
                bTblLayout2Init = false
            }

            ViewUtils.setDBColumn(mLocation,TableProduct.Columns.dirname.name)
            val map = ContentValuesExt.copyBundleContentValues(intent.extras)
            ViewUtils.copyContentValuesToViewGroup( map,(viewMain as ViewGroup?)!!)
            messDialog?.dismissDialog()

            arrFolderProduct = FilesFolders().getFolder(mLocation.text.toString(), mSwRecursive.isChecked)
            category.fillFolder( categoryid, arrFolderProduct)
        }
    }

    private fun fillList1() {
        updateTableLayouts()

        if (mTblLayout1 == null
            || findViewById<SwitchExt>(R.id.colSubTypeId) == null
            || this::mSubType.isInitialized) {
            return
        }

        val textSize = ViewUtils.getDip(com.farrusco.projectclasses.R.dimen.normal_35sp, this)
        mSubType = findViewById(R.id.colSubTypeId)
        mTblLayout1?.removeAllViews()
        arrCategoryDesc.forEach{
            val row = TableRow(this)

            val fieldSwitch = SwitchExt(this)
            fieldSwitch.layoutParams = mSubType.layoutParams
            fieldSwitch.setTextColor(mSubType.currentTextColor)
            fieldSwitch.textSize=textSize
            fieldSwitch.skipEditTagAlways = true
            fieldSwitch.id = R.id.colSubTypeId
            fieldSwitch.setBackgroundResource(R.drawable.cell_underline)

            fieldSwitch.text = it.levelTitle
            fieldSwitch.isChecked = false
            // fieldSwitch.setOnClickListener(mLocalSubTypeListener)
            TagModify.setViewTagValue(fieldSwitch, "catid", it.id)
            TagModify.setViewTagValue(fieldSwitch, "lvl", it.level)
            TagModify.setViewTagValue(fieldSwitch, "type", it.type)

            fieldSwitch.skipEditTagAlways = false

            if (ConstantsFixed.ScreenModi.ModeBrowse == currentModi) {
                if (fieldSwitch.isEnabled) fieldSwitch.isEnabled = false
            }
            if (it.hasChild){
                // group level disable switch
                fieldSwitch.isEnabled = false
                val fieldText = TextViewExt(this)
                fieldText.setBackgroundResource(R.drawable.cell_underline)
                fieldText.colorView = ConstantsFixed.ColorBasic.Edit
                TagModify.setViewTagValue(fieldText, "type", it.type)
                TagModify.setViewTagValue(fieldText, "lvl", it.level)
                TagModify.setViewTagValue(fieldText, "catid", it.id)
                val spanString = SpannableString(fieldSwitch.text)
                //spanString.setSpan(UnderlineSpan(), 0, spanString.length, 0)
                spanString.setSpan(StyleSpan(Typeface.BOLD), 0, spanString.length, 0)
                spanString.setSpan(StyleSpan(Typeface.ITALIC), 0, spanString.length, 0)
                fieldText.text=spanString
                fieldText.textSize=textSize
                PageManager.treeCollapse(fieldText, mTblLayout1!!)
                row.addView(fieldText)
            } else{
                row.addView(fieldSwitch)
            }

            mTblLayout1?.addView(row, TableLayout.LayoutParams())
        }

        PageManager.setupTable(mTblLayout1, arrCategoryMut)
    }

    private fun fillList2() {
        updateTableLayouts()

        if (mTblLayout2 == null || bTblLayout2Init){
            return
        }
        val textSize = ViewUtils.getDip(com.farrusco.projectclasses.R.dimen.normal_35sp, this)
        mSubType = findViewById(R.id.colSubTypeId)
        mTblLayout2?.removeAllViews()
        if (!mSwRecursive.isChecked) {
            val row = TableRow(this)
            val fieldTextView = TextView(this)
            fieldTextView.text = getString(R.string.empty)
            row.addView(fieldTextView)
            mTblLayout2?.addView(row, TableLayout.LayoutParams())
            return
        }
        bTblLayout2Init = true

        arrFolderProduct.forEachIndexed { index, folderProduct ->
            val row = TableRow(this)
            val fieldSwitch = SwitchExt(this)
            fieldSwitch.layoutParams = mSubType.layoutParams
            fieldSwitch.setTextColor(mSubType.currentTextColor)
            fieldSwitch.textSize=textSize
            fieldSwitch.skipEditTagAlways = true
            fieldSwitch.id = R.id.colSubTypeId
            fieldSwitch.setBackgroundResource(R.drawable.cell_underline)
            ///mLocation.text
            var fldr = folderProduct.folder
            if (fldr.startsWith(mLocation.text)){
                fldr = "." + fldr.removePrefix(mLocation.text)
            }
            fieldSwitch.text = fldr
            //fieldSwitch.text = it.folder
            fieldSwitch.isChecked = folderProduct.isChecked
            TagModify.setViewTagValue(fieldSwitch, "row", index+1)
            TagModify.setViewTagValue(fieldSwitch, "catid", folderProduct.id)
            TagModify.setViewTagValue(fieldSwitch, "folder", folderProduct.folder)
            TagModify.setViewTagValue(fieldSwitch, "lvl", folderProduct.level)
            TagModify.setViewTagValue(fieldSwitch, "type", ConstantsLocal.TYPE_DIRECTORY)

            fieldSwitch.skipEditTagAlways = false

            if (ConstantsFixed.ScreenModi.ModeBrowse == currentModi) {
                if (fieldSwitch.isEnabled) fieldSwitch.isEnabled = false
            }
            row.addView(fieldSwitch)
            mTblLayout2?.addView(row, TableLayout.LayoutParams())
        }
    }

    private fun updateTableLayouts(){
        if (mTblLayout1 == null) {
            mTblLayout1 = findViewById(R.id.tblLayout1)
            updateTableLayout(R.id.tblLayout1, mTblLayout1)
        }
        if (mTblLayout2 == null) {
            mTblLayout2 = findViewById(R.id.tblLayout2)
            updateTableLayout(R.id.tblLayout2, mTblLayout2)
        }
    }

    override fun onFragmentAttach(link: String?) {
        if (link != null){
            when (link){
                "f0" -> fillList0()
                "f1" -> fillList1()
                "f2" -> fillList2()
            }
        }
    }

    private val mLocalBtnDirListener = View.OnClickListener { //openFile();

        val properties = DialogProperties()
        properties.dir = DialogConfigs.STORAGE_STORAGE
        properties.errorDir = File(properties.dir)
        properties.root = File(properties.dir)
        val lastPath = Systeem(this).getValue(SystemAttr.LastPath)
        if (lastPath.isNotEmpty()){
            properties.offset=File(lastPath)
        } else if (mLocation.text.isNullOrBlank()){
            properties.offset = File(properties.dir)
        } else{
            val filename = File(mLocation.text.toString())
            properties.offset = filename
            val defaultFolder = "/" + filename.absolutePath.split("/")[1]
            properties.dir = defaultFolder
            properties.errorDir = File(defaultFolder)
            properties.root = File(defaultFolder)
        }
        properties.extensions = arrayOf("")
        properties.selectionType = DialogConfigs.DIR_SELECT
        properties.selectionMode = DialogConfigs.SINGLE_MODE
        properties.title = getText(R.string.selectfolder).toString()

        val dialog =  FilePickerDialog(this,properties)
        //dialog.markFiles(arrayListOf(mLocation.text.toString()))
        dialog.setTitle(getText(R.string.selectfolder))

        dialog.setDialogSelectionListener(object : DialogSelectionListener {
            override fun onSelectedFilePaths(lastPath:String?, files: Array<String?>?) {
                //files is the array of paths selected by the App User.
                if (files != null) {
                    for (path in files) {
                        Systeem(this@DetailFileDirsTab).setValue(SystemAttr.LastPath,
                            path,
                            3
                        )
                        if (mLocation.text.toString() != path.toString()){
                            mLocation.text = path.toString()
                            TagModify.setViewTagValue(mLocation, ConstantsFixed.TagSection.TsUserFlag, ConstantsFixed.TagAction.Edit.name)
                            // delete old tree
                            arrFolderProduct = FilesFolders().getFolder(path.toString(), mSwRecursive.isChecked)
                            category.fillFolder( categoryid, arrFolderProduct)

                            bTblLayout2Init = false
                            fillList2()
                        }
                    }
                }
            }

            override fun onCancelPaths(lastPath: String?) {
                if (lastPath != null) {
                    Systeem(this@DetailFileDirsTab).setValue(SystemAttr.LastPath,
                        lastPath,
                        3
                    )
                }
            }
        })
        dialog.show()
    }

    override fun saveScreen(): ReturnValue {

        var rtn = validateScreen()
        if (!rtn.returnValue) {
            return rtn
        }

        if (arrFolderProduct.isEmpty()) {
            arrFolderProduct = ArrayList()
            val fp = FolderProduct()
            fp.folder = mLocation.text.toString()
            arrFolderProduct.add(fp)
        }

        val cv = ViewUtils.copyViewGroupToContentValues((viewMain as ViewGroup), groupNo)
        cv.keySet().forEach {
            intent.putExtra(it,cv[it].toString())
        }
        intent.putExtra(TableProduct.Columns._id.name, productid.toString())
        intent.putExtra(TableProduct.Columns.dirname.name, mLocation.textExt)

        // update folder
        if (mTblLayout2 != null && mTblLayout2!!.isNotEmpty()) {
            mTblLayout2?.children?.forEach {
                //val row = mTblLayout2!!.getChildAt(idx) as TableRow
                if ((it as TableRow).getChildAt(0)::class.simpleName == SwitchExt::class.simpleName) {
                    val fieldSwitch = (it).getChildAt(0) as SwitchExt
                    val fldr = TagModify.getViewTagValue(fieldSwitch, "folder")
                    arrFolderProduct.find { fit -> fit.folder == fldr }?.isChecked = fieldSwitch.isChecked
                }
            }
        }

        // to be sure all data is ready for update check subcategory
        category.updSubCategory(productid, categoryid, mTitle.textExt.toString(), arrFolderProduct)

        // duplicate update; fast enough; no optimisation
        categoryid = arrFolderProduct[0].id
        if ( currentModi == ConstantsFixed.ScreenModi.ModeInsert ){
            intent.putExtra(TableProduct.Columns._categoryid.name, arrFolderProduct[0].id.toString())
            intent.putExtra(TableProduct.Columns.type.name,ConstantsLocal.TYPE_DIRECTORY.toString())
            rtn = product.insertPrimaryKey(ContentValuesExt.copyBundleContentValues(intent.extras))
            productid = rtn.id
        } else {
            intent.putExtra(TableProduct.Columns._categoryid.name, arrFolderProduct[0].id.toString())
            rtn = product.updatePrimaryKey(ContentValuesExt.copyBundleContentValues(intent.extras))
        }

        if (rtn.returnValue) {
            val map = ContentValues()
            map.put(TableCategory.Columns._id.name, arrFolderProduct[0].id.toString())
            map.put(TableCategory.Columns.title.name, mLocation.textExt)
            rtn = category.updatePrimaryKey(map)
        }

        if (!rtn.returnValue) {
            if (rtn.messnr == R.string.mess058_demo) {
                ToastExt().makeText(
                    this,
                    getText(R.string.mess058_demo).toString()
                        .replace("%0%", BuildConfig.LIMIT.toString()), Toast.LENGTH_LONG
                ).show()
            } else {
                ToastExt().makeText(this,rtn.messnr, Toast.LENGTH_LONG).show()
            }

            return rtn
        }

        // update category
        for(idx in  mTblLayout1!!.indices) {
            val row = mTblLayout1!!.getChildAt(idx) as TableRow
            if (row.getChildAt(0)::class.simpleName == SwitchExt::class.simpleName) {
                val fieldSwitch = row.getChildAt(0) as SwitchExt
                val isChecked = (if (fieldSwitch.isChecked) -1 else 0)
                val id = TagModify.getViewTagValue(fieldSwitch, "catid").toInt()
                arrCategoryMut.find { it.categoryid == id }?.checked = isChecked
            }
        }

        arrCategoryMut.forEach {
            if (currentModi == ConstantsFixed.ScreenModi.ModeInsert){
                if (it.checked == -1) {
                    productRel.insertProductRel(productid, it.categoryid)
                }
            } else {
                if (it.checkedOld == -1) {
                    if (it.checked == 0) {
                        productRel.deleteProductRel(productid, it.categoryid)
                    }
                } else {
                    if (it.checked == -1) {
                        productRel.insertProductRel(productid, it.categoryid)
                    }
                }
            }
        }

        category.renumLevel()

        // link orphan product to folder
        // repair for all directories
        productRel.linkProduct()

        var update = ""
        arrCategoryMut.forEach{
            update += "${it.categoryid},${it.level},${it.checked},"
        }
        if (update.isNotEmpty()){
            update = update.substring(0,update.length-1)
        }
        mEditText3.setText(update)
        intent.putExtra(
            ConstantsFixed.TagSection.TsModFlag.name,
            intent.getStringExtra(ConstantsFixed.TagSection.TsModFlag.name)
        )
        intent.removeExtra(ConstantsFixed.TagSection.TsUserFlag.name)
        intent.putExtra("category",update)
        currentModi = ConstantsFixed.ScreenModi.ModeEdit
        ToastExt().makeText(this, R.string.mess002_saved, Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK, intent)
        return rtn
    }

    override fun validateScreen(): ReturnValue {
        val rtn = super.validateScreen()
        if (!rtn.returnValue) return rtn
        if (productid == 0) return rtn
        if (StringUtils.isEmpty(mDescription.text.toString())) {
            val txt = getText(R.string.mess003_notnull).toString().replace("%0%",getText(R.string.description).toString())
            ToastExt().makeText(this, txt, Toast.LENGTH_LONG).show()
            rtn.returnValue=false
            return rtn
        }
        if (StringUtils.isEmpty(mLocation.text.toString())) {
            val txt = getText(R.string.mess003_notnull).toString().replace("%0%",getText(R.string.directory).toString())
            ToastExt().makeText(this, txt, Toast.LENGTH_LONG).show()
            rtn.returnValue=false
            return rtn
        }
        val arrFiles = FilesFolders().getFolder(mLocation.text.toString(), mSwRecursive.isChecked)
        val rtnProd = product.getProductDirectory()
        var bOverlap = false
        if (rtnProd.cursor.moveToFirst()) {
            do {
                val id = Tables.getInt(rtnProd.cursor, TableProduct.Columns._id.name)
                if (id != productid){
                    val dirname = Tables.getString(rtnProd.cursor, TableProduct.Columns.dirname.name)
                    val recursive = Tables.getString(rtnProd.cursor, TableProduct.Columns.recursive.name)
                    val arrFiles2 = FilesFolders().getFolder(dirname,recursive=="-1")
                    arrFiles.forEach { it1 ->
                        if (arrFiles2.find{it.folder.equals(it1.folder,true) && !bOverlap} != null){
                            bOverlap = true
                        }
                    }
                }
            } while (!bOverlap && rtnProd.cursor.moveToNext())
        }
        rtnProd.cursorClose()
        if (bOverlap) {
            ToastExt().makeText(this, R.string.mess056_overlapfolder, Toast.LENGTH_LONG).show()
            rtn.returnValue = false
        }
        return rtn
    }
}