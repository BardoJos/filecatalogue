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
package com.farrusco.filecatalogue.order

import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.GridView
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TableRow
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.appcompat.widget.AppCompatImageView
import com.farrusco.filecatalogue.BuildConfig
import com.farrusco.filecatalogue.R
import com.farrusco.filecatalogue.basis.DetailFileTab
import com.farrusco.filecatalogue.business.Category
import com.farrusco.filecatalogue.business.Help
import com.farrusco.filecatalogue.business.Product
import com.farrusco.filecatalogue.business.Systeem
import com.farrusco.filecatalogue.common.ConstantsLocal
import com.farrusco.filecatalogue.common.ObjectFile
import com.farrusco.filecatalogue.common.SystemAttr
import com.farrusco.filecatalogue.tables.TableProduct
import com.farrusco.filecatalogue.utils.ImageLoaderExt
import com.farrusco.projectclasses.activity.BaseActivityTableLayout
import com.farrusco.projectclasses.common.Constants
import com.farrusco.projectclasses.common.ConstantsFixed
import com.farrusco.projectclasses.common.ConstantsFixed.Companion.ADD_ID
import com.farrusco.projectclasses.common.ConstantsFixed.Companion.DELETE_ID
import com.farrusco.projectclasses.common.ConstantsFixed.Companion.EDIT_ID
import com.farrusco.projectclasses.common.ConstantsFixed.Companion.GPSINFO_ID
import com.farrusco.projectclasses.common.ConstantsFixed.Companion.HELP_ID
import com.farrusco.projectclasses.common.ConstantsFixed.Companion.SAVE_ID
import com.farrusco.projectclasses.common.ShowHelp
import com.farrusco.projectclasses.common.extensionApplication
import com.farrusco.projectclasses.databases.tables.CursorX.getColumnValueFloat
import com.farrusco.projectclasses.databases.tables.CursorX.getColumnValueInt
import com.farrusco.projectclasses.databases.tables.CursorX.getColumnValueString
import com.farrusco.projectclasses.filepicker.controller.DialogSelectionListener
import com.farrusco.projectclasses.filepicker.model.DialogConfigs
import com.farrusco.projectclasses.filepicker.model.DialogProperties
import com.farrusco.projectclasses.filepicker.utils.Utility
import com.farrusco.projectclasses.filepicker.view.FilePickerDialog
import com.farrusco.projectclasses.graphics.BitmapManager
import com.farrusco.projectclasses.graphics.ImageCreator
import com.farrusco.projectclasses.utils.CalcObjects
import com.farrusco.projectclasses.messages.Mess
import com.farrusco.projectclasses.utils.ProductDetail
import com.farrusco.projectclasses.utils.ReturnValue
import com.farrusco.projectclasses.utils.TagModify
import com.farrusco.projectclasses.messages.ToastExt
import com.farrusco.projectclasses.utils.TouchImageAdapter
import com.farrusco.projectclasses.utils.ViewUtils
import com.farrusco.projectclasses.widget.ButtonExt
import java.io.File

// class ListFiles : BaseActivityTableLayout(), MyCallback {
class ListFiles : BaseActivityTableLayout() {
    override var layoutResourceId: Int = 0
    override val mainViewId: Int = R.id.viewMain
    private var searchWindow = 0
    private var imageLoader: ImageLoaderExt? = null
    private var imageCreator: ImageCreator? = null
    private lateinit var gridview: GridView

    private var blocked = 0
    private var noOfColumns = 0
    private var orderby = 0
    private var parentWindow = ""
    private var positionPhoto = -1
    private var scrollPosition = -1
    private var requestCodeCreate = 0
    private lateinit var category: Category
    private lateinit var product: Product

    private var createImageLoader: CreateImage? = null
    private val finishedHandler = Handler(Looper.getMainLooper())

    private val selectmenu = ConstantsFixed.LAST_ID + 1
    private val importMenu = ConstantsFixed.LAST_ID + 2
    private val exportMenu = ConstantsFixed.LAST_ID + 3
    private val zoomMenu = ConstantsFixed.LAST_ID + 4
    private val objectFile = ObjectFile()

    override fun initTableLayout() {
        addTableLayout(
            R.id.tblLayout,
            R.layout.line_textview1x,
            product,
            arrayOf(TableProduct.Columns.filename.name),
            arrayListOf(ADD_ID, EDIT_ID, DELETE_ID, HELP_ID),
            DetailFileTab::class.java
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        noOfColumns = CalcObjects.stringToInteger(
            intent.getStringExtra("noofcolumns"),
            0
        )
        if (noOfColumns < 1 || noOfColumns > 99) noOfColumns = 1
        layoutResourceId = if (ConstantsLocal.isSingleListLabelEnabled && noOfColumns == 1) R.layout.list_file else R.layout.list_files

        super.onCreate(savedInstanceState)
    }

    override fun initActivity() {

        helpText = Help().getHelpTitle(className)
        registerForContextMenu(findViewById(R.id.viewMain))

        deleteTmp()
        //gallery = findViewById<ViewPager>(R.id.gallery)

        blocked = CalcObjects.stringToInteger(
            intent.getStringExtra("blocked"), 9
        )
        searchWindow = CalcObjects.stringToInteger(
            intent.getStringExtra("search"), 0
        )
        requestCodeCreate = CalcObjects.stringToInteger(
            intent.getStringExtra(Constants.RequestCode), 0
        )

        if (intent.hasExtra("parent")) {
            parentWindow = intent.getStringExtra("parent")!!
        }
        orderby = CalcObjects.stringToInteger(
            intent.getStringExtra("orderby"), 0
        )
        category = Category(this)
        product = Product(this)

        if (ConstantsLocal.isSingleListLabelEnabled && noOfColumns == 1){
            initTableLayout()
            fillList2()
        } else {
            gridview = findViewById(R.id.gridview)
            gridview.isFastScrollEnabled = true
            gridview.numColumns = noOfColumns
            gridview.clipToPadding = false
            gridview.onItemLongClickListener = mLongClickItemSelect
            //gridview.onItemClickListener = mClickItemSelect
            fillList()
        }

        if (intent.hasExtra("labelres")) {
            this.title =
                getString(
                    intent.getStringExtra("labelres")!!
                        .toInt()
                )
        }
        contextMenuAdd(ConstantsFixed.OPEN_APP_ID, getText(R.string.openapp).toString(), R.drawable.open_file,2,2)
        contextMenuAdd(ConstantsFixed.OPEN_APP_WITH_ID, getText(R.string.openappwith).toString(), R.drawable.open_file,3,2)

    }

    // click does not function at a gridview with file
    private val mLongClickItemSelect =
        OnItemLongClickListener { _, v, _, _ ->
            if (TagModify.getViewTagValue(v,ConstantsFixed.TagSection.TsUserFlag.name) != ConstantsFixed.TagAction.Delete.name){
                val popupMenu: PopupMenu = if (v is TableRow) {
                    createPopupMenu(v.getChildAt(0))
                } else {
                    createPopupMenu(v)
                }
                popupMenu.show()
                popupMenu.setOnMenuItemClickListener { item ->
                    onContextItemSelected(item)
                    //}
                    true
                }
            }
            false
        }

    private fun fillList() {
        val count = loadImages()

        imageLoader = ImageLoaderExt(
            this,
            applicationContext,
            noOfColumns,
            ConstantsLocal.isSingleListLabelEnabled
        )

        gridview.adapter = imageLoader

        if (BuildConfig.LIMIT > 0){
            ToastExt().makeText(
                this@ListFiles,
                getText(R.string.mess058_demo).toString().replace("%0%", BuildConfig.LIMIT.toString()), Toast.LENGTH_LONG
            ).show()
        } else if (Constants.isTooltipEnabled){
            if (count > 0){
                val txt = getText(R.string.mess057_presslong4menu).toString().replace("%0%",count.toString())
                ToastExt().makeText( this@ListFiles, txt, Toast.LENGTH_LONG ).show()
            } else {
                ToastExt().makeText(
                    this@ListFiles,
                    R.string.mess055_presslong4menu, Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun fillList2() {
        removeAllTableLayoutViews(0)
        val rtn = createResult()
        fillTable(rtn.cursor)
        rtn.cursorClose()
        setTableRowMenu(getTableLayout(0))
    }

    private fun deleteTmp() {
        File(Constants.bitmapPath).deleteRecursively()
        // MLKit cache cleanup
        File(Constants.mlkitPath).deleteRecursively()
    }
    override fun exitOnBackPressed(){
        ViewUtils.removeChildTag(viewMain as ViewGroup, arrayOf( ConstantsFixed.TagSection.TsUserFlag.name))
        super.exitOnBackPressed()
    }
    override fun onDestroy() {

        createImageLoader = null
        imageCreator?.destroyClass()
        imageCreator?.stopThread()
        imageCreator = null
        imageLoader = null

        super.onDestroy()
        deleteTmp()
        if (ConstantsLocal.isSingleListLabelEnabled && noOfColumns == 1) {
            return
        }
        val grid = gridview
        val count: Int = grid.childCount
        var v: ImageView
        for (i in 0 until count) {
            try {
                v = grid.getChildAt(i) as ImageView
                v.drawable.callback = null
            } catch (_: Exception) {}
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        if (parentWindow == "detailordertab") {
            contextMenuAdd(
                selectmenu,
                getText(R.string.select).toString(),
                android.R.drawable.ic_menu_save,0,
                2
            )
        }

        contextMenuAdd(EDIT_ID, getText(R.string.edit).toString(), R.drawable.edit_normal, 4, 2)
        contextMenuAdd(
            zoomMenu,
            getText(R.string.details).toString(),
            android.R.drawable.ic_menu_zoom,1,
            2
        )
        contextMenuAdd(DELETE_ID, getText(R.string.delete).toString(), R.drawable.delete_normal, 6, 2)

        if (parentWindow != "detailordertab") {
            contextMenuAdd(ADD_ID, getText(R.string.add).toString(), R.drawable.add_normal,2, 1)
            contextMenuAdd(
                importMenu,
                getText(R.string.importfile).toString(),
                R.drawable.ic_menu_upload,10,
                1
            )
            contextMenuAdd(
                exportMenu,
                getText(R.string.exportfile).toString(),
                R.drawable.ic_menu_save,11,
                1
            )
        }
        if (Constants.isHelpEnabled) {
            contextMenuAdd(
                HELP_ID,
                getText(R.string.help).toString(),
                R.drawable.help_normal,12,
                1
            )
        }

        if (parentWindow == "detailordertab") {
            contextMenuMove(selectmenu, 0, 2)
            contextMenuMove(zoomMenu, 1, 2)
            contextMenuDelete(SAVE_ID,1)
            contextMenuDelete(ADD_ID,1)
            contextMenuDelete(GPSINFO_ID,1)
        }
        return true
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val id: Int
        when (currView) {
            is AppCompatImageView -> {
                id = ViewUtils.getDBColumnBack(currView)!!.toInt()
                positionPhoto = ViewUtils.getGroupno(currView)
            }
            is ImageView -> {
                id = ViewUtils.getDBColumnBack(currView)!!.toInt()
                positionPhoto = ViewUtils.getGroupno(currView)
            }
            is TableRow -> {
                id = ViewUtils.getChildValue(
                    currView as ViewGroup,
                    ConstantsFixed.TagSection.TsDBColumnBack.name
                )!!.toInt()
                positionPhoto = ViewUtils.getChildValue(
                    currView as ViewGroup,
                    ConstantsFixed.TagSection.TsGroupno.name
                )!!.toInt()
            }
            is ButtonExt -> {
                id = ViewUtils.getChildValue((currView.parent as ViewGroup),TableProduct.Columns._id.name)!!.toInt()
                positionPhoto = 0
            }
            else -> {
                return false
            }
        }

        scrollPosition = if (::gridview.isInitialized) gridview.firstVisiblePosition else -1
        val intentX = Intent()
        when (item.itemId) {
            EDIT_ID -> {
                val map = ContentValues()
                map.put(TableProduct.Columns._id.name, id)
                map.put(ConstantsFixed.TagSection.TsModFlag.name, ConstantsFixed.TagAction.Edit.name)

                intentX.setClass(this@ListFiles, DetailFileTab::class.java)
                intentX.putExtra(TableProduct.Columns._id.name, id.toString())
                intentX.putExtra(
                    ConstantsFixed.TagSection.TsModFlag.name,
                    ConstantsFixed.TagAction.Edit.name
                )
                if (parentWindow != ""){
                    intentX.putExtra("parent",parentWindow)
                }
                intentX.putExtra(Constants.RequestCode,"2")
                resultLauncher.launch(intentX)
            }
            ConstantsFixed.OPEN_APP_ID,  ConstantsFixed.OPEN_APP_WITH_ID -> {
                openApp(item.itemId)
            }
            zoomMenu -> {
                intentX.setClass(this@ListFiles, DetailFileTab::class.java)
                intentX.putExtra(TableProduct.Columns._id.name, id.toString())
                intentX.putExtra(
                    ConstantsFixed.TagSection.TsModFlag.name,
                    ConstantsFixed.TagAction.Browse.name
                )
                if (parentWindow != ""){
                    intentX.putExtra("parent",parentWindow)
                }
                intentX.putExtra(Constants.RequestCode,"2")
                resultLauncher.launch(intentX)
            }
            selectmenu -> {
                val filename =
                    TagModify.getViewTagValue(currView, ConstantsFixed.TagSection.TsDBValue.name)
                intentX.putExtra(TableProduct.Columns._id.name, id.toString())
                intentX.putExtra("filename", filename)
                intentX.putExtra(Constants.RequestCode,"9")
                setResult(RESULT_OK, intentX)
                finish()
            }
            DELETE_ID -> {
                val alertDialog = Mess.buildAlertDialog(this,layoutInflater,
                    getString(R.string.delete),
                    getString( R.string.mess004_deleteyn))

                with(alertDialog){
                    setPositiveButton(
                        com.farrusco.projectclasses.R.string.yes
                    ) { _, _ ->
                        val filename =
                            TagModify.getViewTagValue(currView, ConstantsFixed.TagSection.TsDBValue.name)
                        if (ConstantsLocal.deleteFilePhone){
                            if (filename.isNotEmpty()){
                                val file = File(filename)
                                if (file.exists()) {
                                    file.delete()
                                }
                            }
                        }

                        val map = ContentValues()
                        map.put(TableProduct.Columns._id.name, id)
                        product.deletePrimaryKey(map)
                        // prevent popup menu
                        TagModify.setViewTagValue(currView, ConstantsFixed.TagSection.TsUserFlag.name, ConstantsFixed.TagAction.Delete)
                        // no save by top-class
                        TagModify.deleteViewTagSection(currView, ConstantsFixed.TagSection.TsModFlag.name)
                        BitmapManager.setDeleteImage(context, currView.width, (currView  as ImageView))

                        //BitmapManager.loadFileToImageView(context, filename, currView.width, 0f, (currView  as ImageView))
                    }
                    // Setting Positive Yes Btn
                    setNeutralButton(
                        com.farrusco.projectclasses.R.string.no
                    ) { dialog, _ ->
                        dialog.cancel()
                    }
                    show()
                }
                return true
            }
            else -> return super.onContextItemSelected(item)
        }

        return false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            HELP_ID -> {
                intent.setClass(this@ListFiles, ShowHelp::class.java)
                var help = ""
                help += if (searchWindow == -1) {
                    getText(R.string.help_listfiles_long).toString()
                } else {
                    getText(R.string.help_listfiles).toString() + "\n\n"
                }
                help += getText(R.string.mess020_imexportequals).toString()
                intent.putExtra("helptext", help)
                startActivity(intent)
            }
            ADD_ID -> {
                if (BuildConfig.LIMIT > 0 && Product(this).getFilesCount() >= BuildConfig.LIMIT ){
                    ToastExt().makeText(
                        this@ListFiles,
                        getText(R.string.mess058_demo).toString().replace("%0%", BuildConfig.LIMIT.toString()), Toast.LENGTH_LONG
                    ).show()
                } else{
                    //return super.onOptionsItemSelected(item);
                    intent.setClass(this@ListFiles, DetailFileTab::class.java)
                    intent.putExtra(TableProduct.Columns._id.name, "0")
                    intent.putExtra(
                        ConstantsFixed.TagSection.TsModFlag.name,
                        ConstantsFixed.TagAction.New.name
                    )

                    intent.putExtra(Constants.RequestCode,"5")
                    resultLauncher.launch(intent)
                }
            }
            importMenu -> {
                // FileRequest.File(this, 103).isMultiple(false).setMimeType(ConstantsFixed.FILE_TYPE_FILE_ALL).pick()
                val properties = DialogProperties()
                properties.dir = DialogConfigs.STORAGE_STORAGE
                properties.errorDir = File(properties.dir)
                properties.root =  File(properties.dir)
                val lastPath = Systeem(this).getValue(SystemAttr.LastPath)
                if (lastPath.isNotEmpty()){
                    properties.offset=File(lastPath)
                } else properties.offset = File(properties.dir)
                properties.extensions = arrayOf("")
                properties.selectionType = DialogConfigs.FILE_SELECT
                properties.selectionMode = DialogConfigs.SINGLE_MODE
                properties.title = getText(R.string.selectfolder).toString()

                val dialog = FilePickerDialog(this, properties)
                //dialog.markFiles(arrayListOf(mLocation.text.toString()))
                dialog.setTitle(getText(R.string.selectfolder))

                dialog.setDialogSelectionListener(object : DialogSelectionListener {
                    override fun onSelectedFilePaths(lastPath:String?,files: Array<String?>?) {
                        //files is the array of paths selected by the App User.
                        if (files != null) {
                            for (path in files) {
                                Systeem(this@ListFiles).setValue(SystemAttr.LastPath,
                                    File(path!!).absolutePath,
                                    3
                                )
                                //if (!mLocation.text.toString().equals(path.toString())){
                                //    mLocation.setText(path.toString())
                                product.readProductPrices(path.toString())
                                //}
                            }
                        }
                    }

                    override fun onCancelPaths(lastPath: String?) {
                        if (lastPath != null) {
                            Systeem(this@ListFiles).setValue(SystemAttr.LastPath,
                                lastPath,
                                3
                            )
                        }
                    }
                })
                dialog.show()
            }
            exportMenu -> {
                val properties = DialogProperties()
                properties.dir = DialogConfigs.STORAGE_STORAGE
                properties.errorDir = File(properties.dir)
                properties.root = File(properties.dir)
                val lastPath = Systeem(this).getValue(SystemAttr.LastPath)
                if (lastPath.isNotEmpty()) {
                    properties.offset = File(lastPath)
                } else {
                    properties.offset = File(properties.dir)
                }
                properties.extensions = arrayOf("")
                properties.selectionType = DialogConfigs.DIR_SELECT
                properties.selectionMode = DialogConfigs.SINGLE_MODE
                properties.title = getText(R.string.selectfolder).toString()

                val dialog = FilePickerDialog(this, properties)
                dialog.setTitle(getText(R.string.selectfolder))

                dialog.setDialogSelectionListener(object : DialogSelectionListener {
                    override fun onSelectedFilePaths(lastPath: String?, files: Array<String?>?) {
                        //files is the array of paths selected by the App User.
                        if (files != null) {
                            for (path in files) {
                                Systeem(this@ListFiles).setValue(SystemAttr.LastPath,
                                    File(path.toString()).absolutePath,
                                    3
                                )
                                product.writeProductPrices(path.toString())
                            }
                        }
                    }

                    override fun onCancelPaths(lastPath: String?) {
                        if (lastPath != null) {
                            Systeem(this@ListFiles).setValue(SystemAttr.LastPath,
                                lastPath,
                                3
                            )
                        }
                    }
                })
                dialog.show()
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return false
    }

    private fun openApp(itemId: Int) {
        val filename =
            TagModify.getViewTagValue(currView, ConstantsFixed.TagSection.TsDBValue.name)
        val file = File(filename)
        try {
            if (file.exists()) {
                val uri = Uri.fromFile(file)!!
                val intent = Intent(Intent.ACTION_VIEW)
                if (itemId == ConstantsFixed.OPEN_APP_ID) {
                    val extApp = extensionApplication(this,file.path)
                    intent.setDataAndType(uri, extApp.application)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                } else {
                    Utility.customChooser(this, file)
                }
            }
        } catch(_: ActivityNotFoundException) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setData(Uri.fromFile(file))
            val chooser = Intent.createChooser(intent, getText(R.string.openapp).toString() + ": " + file.name )
            startActivity(chooser)
        } catch (_: Exception) {  }
    }

    private fun createResult(): ReturnValue {

        var strWhere = ""
        //var sOrderProduct = ""
        var sOrder = ""
        val select = "SELECT ${TableProduct.TABLE_NAME}." + TextUtils.join(", ${TableProduct.TABLE_NAME}.",product.dbColumns) + " FROM ${TableProduct.TABLE_NAME}"
        when (orderby){
            //sort_files
            0 ->{
                sOrder =
                    " order by #table#.filelastmodified desc, #table#._id"
            }
            1 -> {
                sOrder =
                    " order by #table#.filelastmodified, #table#._id"
            }
            2 ->{
                sOrder = " order by dirname, filename, #table#._id"
            }
            3 ->{
                sOrder = " order by #table#.code, #table#._id"
            }
            4 ->{
                sOrder = " order by #table#.dateavailable, #table#._id"
            }
            5 ->{
                sOrder = " order by #table#.sizekb desc, #table#._id"
            }
            6 ->{
                sOrder = " order by #table#.sizekb, #table#._id"
            }
        }
        sOrder = sOrder.replace("#table#",TableProduct.TABLE_NAME)

        lateinit var rtn: ReturnValue

        if (requestCodeCreate == 2) {
            strWhere = " WHERE pos_product.type = ${ConstantsLocal.TYPE_FILE} AND"
            if (blocked < 9) {
                strWhere += " pos_product.blocked = $blocked AND"
            }
            if (intent.hasExtra("where1")) {
                strWhere += " " + intent.getStringExtra("where1") + " AND"
            }
            if (intent.hasExtra("where2")) {
                strWhere += " " + intent.getStringExtra("where2") + " AND"
            }
            if (intent.hasExtra("where3")) {
                strWhere += " " + intent.getStringExtra("where3") + " AND"
            }
            strWhere = strWhere.substring(0, strWhere.length - 3)
            val sql =
                "$select $strWhere $sOrder"
            rtn = Product(this).rawQuery(sql, null)
        } else if (searchWindow == -1) {
            if (blocked < 9) {
                strWhere = " pos_product.blocked = $blocked AND"
            }
            if (intent.hasExtra("where")) {
                strWhere += " " + intent.getStringExtra("where") + " AND"
            }
            strWhere = (" WHERE pos_product.type = " + ConstantsLocal.TYPE_FILE
                    + " AND" + strWhere)
            strWhere = strWhere.substring(0, strWhere.length - 3)
            val sql =
                "$select $strWhere $sOrder "
            // " order by pos_product.title, pos_product._id"
            rtn = Product(this).rawQuery(sql, null)
        } else {
            var sql =
                ("$select WHERE pos_product.type = "
                        + ConstantsLocal.TYPE_FILE + " AND")
            if (intent.hasExtra("where1")) {
                sql += " " + intent.getStringExtra("where1") + " AND "
            }
            if (intent.hasExtra("where2")) {
                sql += intent.getStringExtra("where2")
            }
            if (intent.hasExtra("where3")) {
                sql += intent.getStringExtra("where3")
            }
            if (sql.endsWith("AND", false)) {
                sql = sql.substring(0, sql.length - 4)
            }

            sql += " $sOrder"
            rtn = Product(this).rawQuery(sql, null)
        }
        return rtn
    }

    private fun loadImages(): Int {

        imageCreator = ImageCreator( this )

        val width = this.resources.displayMetrics.widthPixels

        // single picture list with text, so small icon
        val imageMaxSize = if (ConstantsLocal.isSingleListLabelEnabled && noOfColumns == 1) (width / 4 - 18) else (width / noOfColumns - 10  - noOfColumns * 2)

        ObjectFile.initArray()

        val rtn = createResult()
        val count = rtn.cursor.count
        TouchImageAdapter.images = ArrayList()
        if (rtn.cursor.count == 0) {
            rtn.cursorClose()
            ToastExt().makeText(
                this@ListFiles,
                getText(R.string.mess008_nodata), Toast.LENGTH_LONG
            ).show()
            finish()
        } else {
            rtn.cursor.moveToFirst()
            var nLimit = BuildConfig.LIMIT
            do {
                val productDetail = ProductDetail()
                // title will be added to file (left/bottom)
                productDetail.id = rtn.cursor.getColumnValueInt(TableProduct.Columns._id.name)!!
                productDetail.rotation = rtn.cursor.getColumnValueFloat(TableProduct.Columns.rotation.name)
                var filename = rtn.cursor.getColumnValueString(TableProduct.Columns.filename.name)
                if (filename == null){
                    productDetail.filename = ""
                    productDetail.dirname = ""
                    filename=""
                } else {
                    productDetail.filename =  filename
                    productDetail.dirname = rtn.cursor.getColumnValueString(TableProduct.Columns.dirname.name)!!
                    filename = "${productDetail.dirname}/${productDetail.filename}"
                }
                objectFile.add(
                    productDetail.id,
                    filename,
                    rtn.cursor.getColumnValueString(TableProduct.Columns.title.name)!!,
                    rtn.cursor.getColumnValueInt(TableProduct.Columns._categoryid.name,0),
                    productDetail.rotation.toInt()
                )
                TouchImageAdapter.images.add(productDetail)

                imageCreator!!.queuePhoto( productDetail.id, File(filename), filename,imageMaxSize, productDetail.rotation)

                nLimit--
            } while (nLimit != 0 && rtn.cursor.moveToNext())
            rtn.cursorClose()
        }
        return count
    }

    override fun copyCursorToViewGroup(cursor: Cursor, v: ViewGroup?) {
// copy values from cursor to view and add hidden column if necessary
        super.copyCursorToViewGroup(cursor, v)
        if (!ConstantsLocal.isSingleListLabelEnabled || noOfColumns > 1) {
            objectFile.add(
                ViewUtils.getValueCursor( cursor, TableProduct.Columns._id.name).toInt()
                , ViewUtils.getValueCursor( cursor, TableProduct.Columns.dirname.name) + "/" + ViewUtils.getValueCursor( cursor, TableProduct.Columns.filename.name)
                , ViewUtils.getValueCursor( cursor, TableProduct.Columns.title.name)
                , ViewUtils.getValueCursor( cursor, TableProduct.Columns._categoryid.name).toInt()
                , ViewUtils.getValueCursor( cursor, TableProduct.Columns.rotation.name).toInt() )
        }
    }

    override fun resultActivity(result: ActivityResult) {
        if (result.resultCode == RESULT_OK) {
            when (CalcObjects.stringToInteger(
                result.data!!.getStringExtra(Constants.RequestCode),0)
            ) {
                2, 3, 0 -> {
                    if (result.resultCode == R.layout.list_order_lines) {
                        setResult(RESULT_OK, null)
                        finish()
                    }
                    if (ConstantsLocal.isSingleListLabelEnabled && noOfColumns == 1) {
                        fillList2()
                    } else {
                        // list is updated
                        imageLoader = null
                        if (result.resultCode == 4) {
                            finish()
                        } else {
                            fillList()
                            if (scrollPosition >= 0) {
                                gridview.setSelection(scrollPosition)
                            }
                        }
                    }
                }
                4, 5 -> {
                    if (ConstantsLocal.isSingleListLabelEnabled && noOfColumns == 1) {
                        fillList2()
                    } else {
                        // list is updated
                        imageLoader = null
                        fillList()
                        if (scrollPosition >= 0) {
                            gridview.setSelection(scrollPosition)
                        }
                    }
                }
                9 -> {
                    // return from detail and goto order line
                    val productId = result.data!!.getStringExtra(TableProduct.Columns._id.name)
                    intent.putExtra(Constants.RequestCode, "9")
                    intent.putExtra(TableProduct.Columns._id.name, productId)
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }
        }
        super.resultActivity(result)
    }

    internal inner class CreateImage(var context: Context) : Thread() {
        var id = 1
        var file:File? = null
        private var imageMaxSize = 0

        override fun run() {
            try {
                BitmapManager.createThumbnails(context,id,file!!,imageMaxSize)
            } catch (_: Exception) {
                //strMess = getString(com.farrusco.project classes.R.string.mess025_Error) + e.message
            } finally {
               // messDialog?.dismissDialog()
            }
            finishedHandler.post { stopThread() }
        }
    }

    fun stopThread() {
        imageCreator?.stopThread()
    }
}