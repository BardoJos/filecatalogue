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
package com.farrusco.filecatalogue.common

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.appcompat.app.AlertDialog
import com.farrusco.filecatalogue.BuildConfig
import com.farrusco.filecatalogue.R
import com.farrusco.filecatalogue.basis.DetailFileTab
import com.farrusco.filecatalogue.business.Category
import com.farrusco.filecatalogue.business.Help
import com.farrusco.filecatalogue.business.Product
import com.farrusco.filecatalogue.business.Systeem
import com.farrusco.filecatalogue.order.ListFiles
import com.farrusco.filecatalogue.tables.TableCategory
import com.farrusco.filecatalogue.tables.TableProduct
import com.farrusco.filecatalogue.tables.TableFace
import com.farrusco.filecatalogue.tables.TableProductRel
import com.farrusco.filecatalogue.utils.PageManager
import com.farrusco.projectclasses.activity.BaseActivityTableLayout
import com.farrusco.projectclasses.common.Constants
import com.farrusco.projectclasses.common.ConstantsFixed
import com.farrusco.projectclasses.databases.tables.CursorX.getColumnValueInt
import com.farrusco.projectclasses.databases.tables.CursorX.getColumnValueString
import com.farrusco.projectclasses.utils.CalcObjects
import com.farrusco.projectclasses.messages.Mess
import com.farrusco.projectclasses.messages.ToastExt
import com.farrusco.projectclasses.utils.TouchImageAdapter
import com.farrusco.projectclasses.utils.ViewUtils
import com.farrusco.projectclasses.widget.ButtonExt
import com.farrusco.projectclasses.widget.EditTextExt
import com.farrusco.projectclasses.widget.SpinnerExt
import com.farrusco.projectclasses.widget.TextViewExt
import kotlin.math.sqrt


class SearchFiles : BaseActivityTableLayout() {
    override val layoutResourceId = R.layout.search_files
    override val mainViewId = R.id.viewMain

    private lateinit var mTblSearch: TableLayout
    private lateinit var mTxtWildCard: EditTextExt
    private lateinit var btnCheck: ButtonExt

    private lateinit var mBtnSearch: ButtonExt
    private var storeQuery = 0
    private var searchWindow = 0
    private var parentWindow = ""
    private var colsOld = ""
    private var search = ""
    private var seperator = ";"
    private lateinit var mSpSearch: SpinnerExt
    private lateinit var mSpOrderBy: SpinnerExt
    private lateinit var mTxtCols: EditTextExt
    private lateinit var mRowSearch: TableRow
    private lateinit var product: Product
    private lateinit var category: Category
    private lateinit var systeem: Systeem
    private lateinit var tblFace: TableFace

    override fun initTableLayout() {
        addTableLayout(
            R.id.tblLayout,
            R.layout.line_textview1x,
            product,
            arrayOf(TableCategory.Columns.description.name),
            arrayListOf(ConstantsFixed.HELP_ID),
            null
        )
    }

    override fun initActivity() {
        helpText = Help().getHelpTitle(className)
        product = Product(this)
        category = Category(this)
        systeem = Systeem(this)
        category = Category(this)
        tblFace = TableFace()
        category.execSQL("update ${TableCategory.TABLE_NAME} set title = seqno where title is null"+
                " and ${TableCategory.Columns.type.name} = ${ConstantsLocal.TYPE_SEARCH}" +
                " and ${TableCategory.Columns.seqno.name} < 99")
        TouchImageAdapter.images = ArrayList()

        if (!product.existsProduct("")) {
            ToastExt().makeText(
                this, R.string.mess051_no_files,
                Toast.LENGTH_LONG
            ).show()
        }

        // category list (red/yellow collapse) reset
        PageManager.arrColor = HashMap()

        mBtnSearch = findViewById(R.id.btnSearch)
        if (Constants.isTooltipEnabled){
            mBtnSearch.setTooltip(R.string.tip009_search)
        }
        mBtnSearch.setOnClickListener(mClickListenerBtnSearch)
        mTxtWildCard = findViewById(R.id.txtWildCard)
        mTxtWildCard.skipEditTagAlways = true
        mTxtWildCard.setMaxLength = 50

        mSpSearch = findViewById(R.id.spSearch)
        mSpSearch.skipEditTagAlways =true
        mSpOrderBy = findViewById(R.id.spOrderBy)
        mSpOrderBy.skipEditTagAlways = true

        mTxtCols = findViewById(R.id.txtCols)
        mTxtCols.skipEditTagAlways = true
        mTxtCols.setMaxLength = 2
        colsOld = systeem.getValue(SystemAttr.Listfiles)
        mTxtCols.textExt = colsOld
        if (ConstantsLocal.isStoreQuery ){
            storeQuery = CalcObjects.objectToInteger(systeem.getValue(SystemAttr.StoreQuery) )
        }

        searchWindow = -1
        if (intent.hasExtra("parent")) {
            parentWindow = intent.getStringExtra("parent")!!
        }
        if (intent.hasExtra("labelres")) {
            this.title = intent.getStringExtra("labelres")
            searchWindow = if (this.title.equals(getString(R.string.activity_file_order))) {
                -1
            } else {
                0
            }
        }

        val spinnerArrayAdapter: ArrayAdapter<CharSequence> = ArrayAdapter
            .createFromResource(
                this, R.array.search_files,
                R.layout.spinner_item
            )
        mSpSearch.adapter = spinnerArrayAdapter

        val spinnerArrayAdapterSort: ArrayAdapter<CharSequence> = ArrayAdapter
            .createFromResource(
                this, if(ConstantsLocal.storeFileSize) R.array.sort_filesIncl else R.array.sort_files,
                R.layout.spinner_item
            )
        mSpOrderBy.adapter = spinnerArrayAdapterSort

        initTableLayout()
        initList()
        loadSearch(99)
        fillList()
    }

    private fun initList(){

        category = Category(this)
        val arrTypes = arrayListOf(ConstantsLocal.TYPE_CATEGORY, ConstantsLocal.TYPE_DIRECTORY,
            ConstantsLocal.TYPE_FILE, ConstantsLocal.TYPE_CREATION)
        if (ConstantsLocal.isFileExtensionEnabled) arrTypes.add(ConstantsLocal.TYPE_FILE_EXTENSION)
        if (ConstantsLocal.isGPSEnabled) arrTypes.add(ConstantsLocal.TYPE_GPS)
        if (ConstantsLocal.isAutoNewEnabled) arrTypes.add(ConstantsLocal.TYPE_AUTOMATED_NEW)
        if (ConstantsLocal.isFaceRecognitionEnabled) arrTypes.add(ConstantsLocal.TYPE_FACE)

        Category.arrCategoryTitle.clear()
        category.fillCategoryArray(arrTypes,true)
    }

    @SuppressLint("InflateParams")
    private fun fillList() {
        mTblSearch = findViewById(R.id.tblLayout)
        mTblSearch.removeAllViews()

        Category.arrCategoryTitle.filter{it.mainid==0 && it.count>0}.forEach { it ->
           // first level processing
           val li = layoutInflater
           val vi = li.inflate(R.layout.line_btn_text, null)
           val id = it.id
           vi.tag = ""
           btnCheck = vi.findViewById(R.id.btnCheck)
           btnCheck.setOnClickListener(mClickListenerColCheckBox)
           btnCheck.setOnLongClickListener(mLongClickListenerColCheckBox)
           btnCheck.tag = ""

            when (it.checked){
                0 -> btnCheck.setBackgroundResource(R.drawable.btn_check_off)
                1 -> btnCheck.setBackgroundResource(R.drawable.btn_check_on)
                2 -> btnCheck.setBackgroundResource(R.drawable.btn_check_tristate)
                3 -> btnCheck.setBackgroundResource(R.drawable.btn_check_no)
            }

           val colInfo: TextViewExt = vi.findViewById(R.id.colInfo)
           var title = it.title

           title += " (${it.count})"
           if (it.hasChild) {
               colInfo.setOnClickListener(mClickListenerColSelector)
           }
           var strTag = ","
            Category.arrCategoryTitle.forEach {
               if (it.mainid == id ) strTag+="${it.id},"
           }
           vi.tag =  strTag.substring(0,strTag.length-1)
           colInfo.text = title
           colInfo.tag = id

           mTblSearch.addView(vi, TableLayout.LayoutParams())

        }
    }

    private val mLongClickListenerColCheckBox = View.OnLongClickListener { v ->
        mRowSearch = v.parent as TableRow
        btnCheck = mRowSearch.findViewById<View>(R.id.btnCheck) as ButtonExt
        val colInfo: TextView = mRowSearch.findViewById<View>(R.id.colInfo) as TextView
        setCheckedChild(colInfo.tag.toString().toInt())
        when (btnCheck.getBackgroundResource()) {
            R.drawable.btn_check_no -> {
                btnCheck.setBackgroundResource(R.drawable.btn_check_off)
                btnCheck.tag = ""
            }
            else -> {
                btnCheck.setBackgroundResource(R.drawable.btn_check_no)
                var tree = (""
                        + (mRowSearch.parent.parent as LinearLayout)
                    .tag)
                if (tree.isNotEmpty()) tree = ",$tree"
                tree = colInfo.tag.toString() + tree
                btnCheck.tag = tree
                Category.arrCategoryTitle.find{it.id == colInfo.tag.toString().toInt()}?.checked=3
            }
        }
        true
    }

    private val mClickListenerColCheckBox = View.OnClickListener { v ->
        mRowSearch = v.parent as TableRow
        btnCheck = mRowSearch.findViewById<View>(R.id.btnCheck) as ButtonExt
        val colInfo: TextView = mRowSearch.findViewById<View>(R.id.colInfo) as TextView
        setCheckedChild(colInfo.tag.toString().toInt())
        when (btnCheck.getBackgroundResource()) {
            R.drawable.btn_check_off -> {
                btnCheck.setBackgroundResource(R.drawable.btn_check_on)
                var tree = (""
                        + (mRowSearch.parent.parent as LinearLayout)
                    .tag)
                if (tree.isNotEmpty()) tree = ",$tree"
                tree = colInfo.tag.toString() + tree
                btnCheck.tag = tree
                Category.arrCategoryTitle.find{it.id == colInfo.tag.toString().toInt()}?.checked=1
            }
            else -> {
                btnCheck.setBackgroundResource(R.drawable.btn_check_off)
                btnCheck.tag = ""
            }
        }
    }

    private fun setCheckedChild(mainIdStart:Int){
        Category.arrCategoryTitle.filter{it.id == mainIdStart}.forEach { it1 ->
            it1.checked = 0
            it1.checkedCurr = 0
            Category.arrCategoryTitle.filter{it.mainid == it1.id}.forEach {it2 ->
                it2.checked = 0
                it2.checkedCurr = 0
                setCheckedChild(it2.id)
            }
        }
    }

    private val mClickListenerColSelector = View.OnClickListener { v ->
        mRowSearch = v.parent as TableRow
        val colInfo: TextView = mRowSearch.findViewById(R.id.colInfo)
        btnCheck = mRowSearch.findViewById(R.id.btnCheck)
        val intentX = Intent()
        intentX.setClass(this@SearchFiles, SearchFilesSub::class.java)
        intentX.putExtra("_id", colInfo.tag.toString())
        intentX.putExtra("title", colInfo.text.toString())
        intentX.putExtra(
            "subid", ""
                    + (mRowSearch.parent
                .parent as LinearLayout).tag
        )
        resultLauncher.launch(intentX)
    }

    private val mClickListenerBtnSearch = View.OnClickListener {
        val intentX = Intent()
        intentX.setClass(this, ListFiles::class.java)

        val sbWhere = StringBuilder()
        var blocked = 9
        if (ConstantsLocal.isSearchFilesAvailableEnabled){
            if (!ConstantsLocal.isSearchFilesHiddenEnabled){
                blocked = 0
            }
        } else {
            blocked = if (ConstantsLocal.isSearchFilesHiddenEnabled){
                -1
            } else {
                // conflict
                2
            }
        }
        if (blocked != 9){
            sbWhere.append(" " + TableProduct.Columns.blocked + " = $blocked AND")
        }

        intentX.putExtra("orderby", mSpOrderBy.selectedItemPosition.toString())

        var tmpWhere1 = ""

        val product = TableProduct(this)
        if (mTxtWildCard.text != null) {
            if (mTxtWildCard.text.toString() != "*") {
                when (mSpSearch.selectedItemPosition) {
                    0 -> tmpWhere1 += " " + TableProduct.TABLE_NAME + "." + TableProduct.Columns.code + " like "
                    1 -> tmpWhere1 += " " + TableProduct.TABLE_NAME + "." + TableProduct.Columns.title + " like "
                    2 -> tmpWhere1 += " " + TableProduct.TABLE_NAME + "." + TableProduct.Columns.description + " like "
                }
                tmpWhere1 += "'" + mTxtWildCard.text + "' and"
                tmpWhere1 = tmpWhere1.replace('*', '%')
            }
        }

        // face selection
        val tmpWhere2 = createIdsWhereX(1) // checked category
        val tmpWhere3 = createIdsWhereX(3) // exclude category

        if (searchWindow == 0) {
            if (tmpWhere2.isEmpty() && tmpWhere3.isEmpty()) {
                ToastExt().makeText(
                    this@SearchFiles,
                    R.string.mess009_noselection,
                    Toast.LENGTH_LONG
                ).show()
                return@OnClickListener
            }
            if (tmpWhere1.isNotEmpty()) {
                intentX.putExtra(
                    "where1",
                    tmpWhere1.substring(0, tmpWhere1.length - 4)
                )
            }
            intent.putExtra(
                "where2",
                tmpWhere2.substring(0, tmpWhere2.length - 4)
            )
            intent.putExtra(
                "where3",
                tmpWhere3.substring(0, tmpWhere3.length - 4)
            )
            if (tmpWhere1.isNotEmpty()) {
                sbWhere.append(tmpWhere1)
            }
            if (tmpWhere2.isNotEmpty()) {
                sbWhere.append(tmpWhere2)
            }
            if (tmpWhere3.isNotEmpty()) {
                sbWhere.append(tmpWhere3)
            }
        } else {
            if (tmpWhere1.isNotEmpty()) {
                sbWhere.append(tmpWhere1)
                intentX.putExtra(
                    "where",
                    tmpWhere1.substring(0, tmpWhere1.length - 4)
                )
            }
            if (tmpWhere2.isNotEmpty()) {
                sbWhere.append(tmpWhere2)
                tmpWhere1 += tmpWhere2
                intentX.putExtra(
                    "where",
                    tmpWhere1.substring(0, tmpWhere1.length - 4)
                )
            }
            if (tmpWhere3.isNotEmpty()) {
                sbWhere.append(tmpWhere3)
                tmpWhere1 += tmpWhere3
                if (intentX.hasExtra("where")) intentX.removeExtra("where")
                intentX.putExtra(
                    "where",
                    tmpWhere1.substring(0, tmpWhere1.length - 4)
                )
            }
        }
        var strWhere = sbWhere.toString()
        if (strWhere.length> 4){
            strWhere = strWhere.substring(0,strWhere.length - 4)
        }

        systeem.updateSysteemKey(SystemAttr.Listfiles.internal, mTxtCols.text.toString())

        if (product.existsProduct(strWhere)){
            saveSearch(99, "")
            if (mTxtCols.text.toString() != colsOld) {
                colsOld=mTxtCols.text.toString()
                systeem.setValue(SystemAttr.Listfiles,
                    mTxtCols.text.toString(),
                    3
                )
            }
            if (parentWindow == "detailordertab"){
                intentX.putExtra("parent", "detailordertab")
            }
            intentX.putExtra("noofcolumns", mTxtCols.text.toString())
            intentX.putExtra("search", searchWindow.toString())
            intentX.putExtra("blocked", blocked.toString())
            resultLauncher.launch(intentX)
        } else {
            ToastExt().makeText(
                this@SearchFiles, R.string.mess008_nodata,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val rtn = super.onPrepareOptionsMenu(menu)
        val cursCat = category.getWhere("${TableCategory.Columns.type.name} = ${ConstantsLocal.TYPE_SEARCH}" +
                " and ${TableCategory.Columns.seqno.name} < 99",
                TableCategory.Columns.seqno.name)

        if (ConstantsLocal.isStoreQuery && cursCat.cursor.moveToFirst()){
            menu.addSubMenu(Menu.NONE, 1000, 10,getText(R.string.searchload1).toString())
            val themeMenu1 = menu.findItem(1000).subMenu
            menu.addSubMenu(Menu.NONE, 2000, 20,getText(R.string.searchsave1).toString())
            val themeMenu2 = menu.findItem(2000).subMenu
            if (themeMenu1 != null && themeMenu2 != null) {
                themeMenu1.clear()
                themeMenu2.clear()
                do {
                    val seqno = cursCat.cursor.getColumnValueInt(TableCategory.Columns.seqno.name)!!
                    if (seqno <= storeQuery){
                        val title = cursCat.cursor.getColumnValueString(TableCategory.Columns.title.name)
                        val descr = cursCat.cursor.getColumnValueString(TableCategory.Columns.description.name,"")
                        if (descr!!.isNotEmpty()){
                            themeMenu1.add(Menu.NONE, 1000 + seqno, Menu.NONE, title)
                        }
                        themeMenu2.add(Menu.NONE, 2000 + seqno, Menu.NONE, title)
                    }
                } while (cursCat.cursor.moveToNext())
            }
        }
        cursCat.cursorClose()
        return rtn
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val rtn = super.onCreateOptionsMenu(menu)
        contextMenuAdd(ConstantsFixed.REFRESH_ID, getText(R.string.searchreset).toString(), R.drawable.button_refresh, 1, 1)

        if (BuildConfig.LIMIT > 0 && Product(this).getFilesCount() >= BuildConfig.LIMIT ){
            // no add
        } else {
            contextMenuAdd(ConstantsFixed.ADD_ID,
                getText(R.string.addfile).toString(),
                R.drawable.add_normal,2,1)
        }
        return rtn
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            in 1001..1098 -> {
                loadSearch(item.itemId-1000)
            }
            in 2001..2098 -> {
                showEditTextDialog(item.itemId-2000)
            }
            ConstantsFixed.ADD_ID -> {
                intent.setClass(this@SearchFiles, DetailFileTab::class.java)
                intent.putExtra(TableProduct.Columns._id.name, "0")
                intent.putExtra(
                    ConstantsFixed.TagSection.TsModFlag.name,
                    ConstantsFixed.TagAction.New.name
                )
                intent.putExtra(Constants.RequestCode,"5")
                resultLauncher.launch(intent)
            }
            ConstantsFixed.REFRESH_ID -> {
                // hide keyboard
                //val imm=getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                //imm.hideSoftInputFromWindow(currentFocus?.windowToken, InputMethodManager.SHOW_FORCED)
                if (currentFocus != null){
                    ViewUtils.showKeyboard(currentFocus!!, this@SearchFiles)
                }
                mTxtWildCard.textExt = "*"
                mSpSearch.setSelection(0)
                mSpOrderBy.setSelection(0)
                Category.arrCategoryTitle.filter{it.checked > 0}.forEach {
                    it.checked = 0
                }

                initList()
                loadSearch(99)
                fillList()
            }
            else -> return super.onOptionsItemSelected(item)
            }
        return true
    }

    private fun showEditTextDialog(seqno:Int){
        val rtn = category.getWhere("${TableCategory.Columns.type.name}=${ConstantsLocal.TYPE_SEARCH}"
                + " AND ${TableCategory.Columns.seqno} = $seqno")
        var title = ""
        if (rtn.cursor.moveToFirst()){
            title = ViewUtils.getValueCursor(rtn.cursor, TableCategory.Columns.title.name)
        }
        rtn.cursorClose()
        val builder = Mess.showEditTextDialog(this, layoutInflater
            , getText(R.string.searchsave1).toString()
            , "", title)
        with(builder) {
            setPositiveButton(getText(R.string.save)){ dialog, _ ->
                val value = (dialog as AlertDialog).findViewById<EditTextExt>(R.id.et_editText)?.textExt
                if (value.isNullOrEmpty()){
                    saveSearch(seqno, seqno.toString())
                } else {
                    saveSearch(seqno, value)
                }
            }
            setNeutralButton(getText(R.string.cancel)){ _, _ ->
            }
            show()
        }

    }

    private fun saveSearch(seqno:Int, title:String){
        if (!ConstantsLocal.isStoreLastQuery && seqno == 99){
            return
        }
        search = mSpSearch.selectedItemPosition.toString() + seperator +
                mTxtWildCard.textExt.toString().replace(seperator,",") + seperator + mTxtCols.textExt +
                "$seperator${mSpOrderBy.selectedItemPosition}"
        Category.arrCategoryTitle.filter{it.checked>0}.forEach {
            search += "$seperator${it.id}$seperator${it.checked}"
        }

        category.saveTypeSeqno(ConstantsLocal.TYPE_SEARCH, seqno, title, search)

        if (seqno<99){
            ToastExt().makeText(this, R.string.mess002_saved, Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadSearch(seqno:Int){
        if (!ConstantsLocal.isStoreLastQuery && seqno == 99){
            return
        }
        val rtn = category.loadTypeSeqno(ConstantsLocal.TYPE_SEARCH, seqno)
        if (rtn.returnValue && rtn.mess.isNotEmpty()){
            search = rtn.mess
            //search
            Category.arrCategoryTitle.filter { it.checked > 0 }.forEach {
                it.checked = 0
            }
            mSpSearch.setSelection(0)
            mTxtWildCard.textExt = "*"
            mTxtCols.textExt = colsOld
            mSpOrderBy.setSelection(0)
            try {
                val arrSearch = search.split(seperator)
                mSpSearch.setSelection(arrSearch[0].toInt())
                mTxtWildCard.textExt = arrSearch[1]
                mTxtCols.textExt = arrSearch[2]
                mSpOrderBy.setSelection(arrSearch[3].toInt())
                for (idx in 4 until arrSearch.size - 1 step 2) {
                    if (arrSearch[idx + 1].toInt() == 1 || arrSearch[idx + 1].toInt() == 3){
                        Category.arrCategoryTitle.filter { it.id == arrSearch[idx].toInt() && it.count > 0 }.forEach {
                            it.checked = arrSearch[idx + 1].toInt()
                        }
                    }
                }
            } catch(_: Exception) {
                // lazy
            }

            var found:Boolean
            do {
                found = false
                Category.arrCategoryTitle.filter { it.checked > 0 }.forEach { main ->
                    Category.arrCategoryTitle.filter { it.id == main.mainid && it.checked == 0}.forEach{
                        it.checked = 2
                        found = true
                    }
                }
            } while (found)
            fillList()
        } else {
            if (seqno != 99) {
                ToastExt().makeText(this, R.string.mess008_nodata, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun resultActivity(result: ActivityResult){

        if (result.resultCode == RESULT_OK) {
            intent = result.data
            when (CalcObjects.stringToInteger(
                result.data!!.getStringExtra(Constants.RequestCode),0)
            ) {
                9 -> {
                    // this is used within order and selection of product
                    if (result.data!!.hasExtra(TableProduct.Columns._id.name)){
                        val productid = result.data!!.getStringExtra(TableProduct.Columns._id.name)
                        intent.putExtra(Constants.RequestCode, "9")
                        intent.putExtra(TableProduct.Columns._id.name, productid)
                        setResult(RESULT_OK, intent)
                        finish()
                    }
                }
            }
            if (result.data != null && result.data!!.hasExtra("filename")){
                // user has selected a file
                val intentX = Intent()
                intentX.putExtra(TableProduct.Columns._id.name,result.data!!.getStringExtra(TableProduct.Columns._id.name))
                intentX.putExtra("filename",result.data!!.getStringExtra("filename"))
                intentX.putExtra(Constants.RequestCode,result.data!!.getStringExtra(Constants.RequestCode))
                setResult(result.resultCode, intentX)
                finish()
            }
            if (this::mRowSearch.isInitialized){
                val colInfo: TextView = mRowSearch.findViewById(R.id.colInfo)
                btnCheck = mRowSearch.findViewById<View>(R.id.btnCheck) as ButtonExt
                val id = colInfo.tag.toString().toInt()
                if (Category.arrCategoryTitle.find{it.mainid == id && it.checked > 0 || it.id == id && it.checkedCurr > 0} != null){
                    Category.arrCategoryTitle.find{it.id == id}?.checked=2
                    btnCheck.setBackgroundResource(R.drawable.btn_check_tristate)
                } else {
                    if (btnCheck.getBackgroundResource() == R.drawable.btn_check_tristate){
                        Category.arrCategoryTitle.find{it.id == id}?.checked=0
                        btnCheck.setBackgroundResource(R.drawable.btn_check_off)
                    }
                }
            }

            if (intent != null && CalcObjects.stringToInteger(intent.getStringExtra(Constants.RequestCode),0) == 2){
                fillList()
            }
        }
    }

    private fun createIdsWhereX(checked:Int): String {

        var selectProducts = ","
        var selectCategory = ""
        var mainId = 0
        Category.arrCategoryTitle.filter{ it.mainid == 0 && it.type == ConstantsLocal.TYPE_FACE}.forEach {
            mainId = it.id
            //subSelectCategory += "${it.id},"
        }
        Category.arrCategoryTitle.filter{ (it.checked == checked ||
                it.checkedCurr == checked) &&
                it.type == ConstantsLocal.TYPE_FACE}.forEach { itMain1 ->
            // find single faces
            //itMain.id = TableProductRel.Columns._categoryid
            //TableFace.Columns._categoryid = itMain.id
            var subSelectCategory = "${itMain1.id},"
            subSelectCategory = createIdsWhereChild(itMain1.id, subSelectCategory)
            var arFaceMain: MutableList<Pair<Int, String>> = ArrayList()
            var arFaceScan: MutableList<Pair<Int, String>> = ArrayList()
            if (itMain1.mainid == 0) {
                // select all faces
                var rtn = tblFace.selectWhere("${TableFace.Columns._productid.name} > 0")
                if (rtn.cursor.moveToFirst()){
                    do {
                        val id = rtn.cursor.getColumnValueString(TableFace.Columns._productid.name)
                        if (!selectProducts.contains(",$id,")){
                            selectProducts += "$id,"
                        }
                    } while (rtn.cursor.moveToNext())
                }
                if (selectProducts.length > 1){
                    rtn.cursorClose()
                    selectProducts = selectProducts.drop(1)
                    var where = " ("
                    if(checked == 3) {
                        where = " not "
                    }
                    where += " pos_product._id in (" +
                            selectProducts.substring(0,selectProducts.length-1)  + ")) and"
                    return where
                }
            }

            //subSelectCategory += "$mainId,"

            tblFace.resetDistance()
            var where = "${TableFace.Columns._categoryid.name} = $mainId" +
                    " and not _productid in (select _productid from pos_face where _categoryid != $mainId)"
            if (subSelectCategory.isNotEmpty()){
                where += " or "
                where += " ${TableFace.Columns._categoryid.name} in (" + subSelectCategory.substring(0,subSelectCategory.length-1) + ")"
            }
            var rtn = tblFace.selectWhere(where)
            if (rtn.cursor.moveToFirst()){
                do {
                    val map = rtn.cursorToMap()
                    if (map.getAsInteger(TableFace.Columns._categoryid.name) == mainId ){
                        arFaceMain.add(
                            Pair(rtn.cursor.getColumnValueInt(TableFace.Columns._productid.name)!!,
                                rtn.cursor.getColumnValueString(TableFace.Columns.extra.name)!! )   )
                    } else {
                        arFaceScan.add(
                            Pair(rtn.cursor.getColumnValueInt(TableFace.Columns._productid.name)!!,
                                rtn.cursor.getColumnValueString(TableFace.Columns.extra.name)!! )   )
                    }
                } while (rtn.cursor.moveToNext())
            }

            rtn.cursorClose()
            var distanceLocal: Float
            var distance =  1.0f
            var distanceSysteem = systeem.getValue(SystemAttr.FaceDistance).toFloat()
            if (distanceSysteem < 1.0f && distanceSysteem >= 0.0f){
                distance = distanceSysteem
            }
            arFaceMain.forEach { itMain2 ->
                val extraMain = itMain2.second.toString().split(",").map { it.toFloat() }.toFloatArray()
                arFaceScan.forEach { itScan ->
                    val extraScan = itScan.second.toString().split(",").map { it.toFloat() }.toFloatArray()
                    val nearest = findNearest( extraMain, extraScan)
                    if (nearest[0]?.second != null) {
                        //val name = nearest[0]!!.first
                        distanceLocal = nearest[0]!!.second
                        if (distanceLocal < distance) {
                            val map = ContentValues()
                            map.put(TableFace.Columns.distance.name, distanceLocal)
                            map.put(TableFace.Columns.seqno.name, 1)
                            if (!selectProducts.contains(",${itMain2.first},")){
                                //If distance between Closest found face is more than 1.000 ,then output UNKNOWN face.
                                selectProducts += "${itMain2.first},"
                                tblFace.updateWhere("${TableFace.Columns._productid.name} = ${itMain2.first}",map)
                            }
                            if (!selectProducts.contains(",${itScan.first},")){
                                //If distance between Closest found face is more than 1.000 ,then output UNKNOWN face.
                                selectProducts += "${itScan.first},"
                                tblFace.updateWhere("${TableFace.Columns._productid.name} = ${itScan.first}",map)
                            }
                        }
                        //    recoName.text = name
                        //else recoName.text = "Unknown"

                    }
                }
            }
        }

        Category.arrCategoryTitle.filter{ it.checked == checked || it.checkedCurr == checked}.forEach { itMain ->
            selectCategory += "${itMain.id},"
            selectCategory = createIdsWhereChild(itMain.id, selectCategory)
        }
        selectProducts = selectProducts.drop(1)
        if (selectCategory.isEmpty() && selectProducts.isEmpty()){
            return ""
        }
        var where = " ("
        if(checked == 3) {
            where = " not ("
        }
        var or = ""
        if (selectProducts.isNotEmpty()){
            where += " pos_product._id in (" +
                    selectProducts.substring(0,selectProducts.length-1)  + ")"
            or = " or"
        }
        if (selectCategory.isNotEmpty()) {
            where += "$or pos_product._id in (select " +
                    TableProductRel.Columns._productid +
                    " from " + TableProductRel.TABLE_NAME +
                    " where " + TableProductRel.Columns._categoryid +
                    " in (" + selectCategory.substring(0,selectCategory.length-1)  + ")) "
        }

        where += ") and"
        return where
    }

    private fun findNearest(faceMain: FloatArray, emb: FloatArray): List<Pair<String?, Float>?> {
        val neighbourList: MutableList<Pair<String?, Float>?> = ArrayList()
        var ret: Pair<String?, Float>? = null //to get closest match
        var prevRet: Pair<String?, Float>? = null //to get second closest match

        //arFace.forEach { fs ->
            faceMain.forEach { _ ->
                // val knownEmb = fse.arrExtra
                var distance = 0f
                for (i in emb.indices) {
                    val diff = emb[i] - faceMain[i]
                    distance += diff * diff
                }
                distance = sqrt(distance.toDouble()).toFloat()
                @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
                if (ret == null || distance < ret!!.second) {
                    prevRet = ret
                    ret = Pair("name", distance)
                }
            }
        //}
        if (prevRet == null) prevRet = ret
        neighbourList.add(ret)
        neighbourList.add(prevRet)

        return neighbourList
    }

    private fun createIdsWhereChild(id:Int, where: String): String {
        var tmpWhere = where
        Category.arrCategoryTitle.filter{it.mainid == id && it.count > 0}.forEach {
            tmpWhere += "${it.id},"
            tmpWhere = createIdsWhereChild(it.id, tmpWhere)
        }
        return tmpWhere
    }

}