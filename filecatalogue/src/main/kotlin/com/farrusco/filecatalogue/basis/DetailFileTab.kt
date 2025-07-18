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
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Typeface
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.MediaController
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.text.isDigitsOnly
import androidx.core.view.children
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.viewpager2.widget.ViewPager2
import com.farrusco.filecatalogue.R
import com.farrusco.filecatalogue.business.Category
import com.farrusco.filecatalogue.business.Help
import com.farrusco.filecatalogue.business.OrderLine
import com.farrusco.filecatalogue.business.Product
import com.farrusco.filecatalogue.business.ProductRel
import com.farrusco.filecatalogue.business.Systeem
import com.farrusco.filecatalogue.common.About
import com.farrusco.filecatalogue.common.CategoryDetail
import com.farrusco.filecatalogue.common.CategoryLine
import com.farrusco.filecatalogue.common.ConstantsLocal
import com.farrusco.filecatalogue.common.SystemAttr
import com.farrusco.filecatalogue.tables.TableCategory
import com.farrusco.filecatalogue.tables.TableFace
import com.farrusco.filecatalogue.tables.TableProduct
import com.farrusco.filecatalogue.tables.TableProductRel
import com.farrusco.filecatalogue.utils.PageManager
import com.farrusco.projectclasses.activity.BaseActivityTableLayout
import com.farrusco.projectclasses.common.Constants
import com.farrusco.projectclasses.common.ConstantsFixed
import com.farrusco.projectclasses.common.extensionApplication
import com.farrusco.projectclasses.databases.tables.CursorX.getColumnValueInt
import com.farrusco.projectclasses.databases.tables.DBUtils
import com.farrusco.projectclasses.filepicker.controller.DialogSelectionListener
import com.farrusco.projectclasses.filepicker.model.DialogConfigs
import com.farrusco.projectclasses.filepicker.model.DialogProperties
import com.farrusco.projectclasses.filepicker.utils.Utility
import com.farrusco.projectclasses.filepicker.view.FilePickerDialog
import com.farrusco.projectclasses.graphics.BitmapManager
import com.farrusco.projectclasses.graphics.BitmapResolver
import com.farrusco.projectclasses.graphics.Graphics
import com.farrusco.projectclasses.graphics.face.MediapipeFaceDetector
import com.farrusco.projectclasses.graphics.face.MediapipeFaceDetector.Companion.loadModelFile
import com.farrusco.projectclasses.messages.ToastExt
import com.farrusco.projectclasses.utils.CalcObjects
import com.farrusco.projectclasses.utils.ContentValuesExt
import com.farrusco.projectclasses.utils.FilesFolders
import com.farrusco.projectclasses.utils.FilesGPS
import com.farrusco.projectclasses.utils.FolderProduct
import com.farrusco.projectclasses.utils.Logging
import com.farrusco.projectclasses.utils.ProductDetail
import com.farrusco.projectclasses.utils.ReturnValue
import com.farrusco.projectclasses.utils.StringUtils
import com.farrusco.projectclasses.utils.TagModify
import com.farrusco.projectclasses.utils.TouchImageAdapter
import com.farrusco.projectclasses.utils.ViewUtils
import com.farrusco.projectclasses.widget.ButtonExt
import com.farrusco.projectclasses.widget.EditTextExt
import com.farrusco.projectclasses.widget.EditTextMoney
import com.farrusco.projectclasses.widget.SwitchExt
import com.farrusco.projectclasses.widget.TextViewExt
import com.farrusco.projectclasses.widget.TouchImageView
import com.farrusco.projectclasses.widget.VideoViewExt
import com.farrusco.projectclasses.widget.pdfviewer.PdfRendererView
import com.farrusco.projectclasses.widget.tablayout.FragmentNames
import com.farrusco.projectclasses.widget.tablayout.TabLayoutExt
import com.farrusco.projectclasses.widget.tablayout.TabsPagerAdapterExt
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import org.tensorflow.lite.Interpreter
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.text.DecimalFormat
import java.util.Calendar

class DetailFileTab : BaseActivityTableLayout() {

    //region region 00 - vars
    override val layoutResourceId: Int = R.layout.detail_file_tab
    override val mainViewId: Int = R.id.viewMain

    private lateinit var mCodeTop: TextViewExt
    private lateinit var mTopTitle: TextViewExt
    private lateinit var mCode: EditTextExt
    private lateinit var mSeqno: EditTextExt
    private lateinit var mTitle: EditTextExt
    private lateinit var mOms: EditTextExt
    private lateinit var mRotation: EditTextExt
    private lateinit var mPrijs: EditTextMoney
    private lateinit var mBlock: SwitchExt
    private lateinit var mDateAvailable: TextViewExt
    private lateinit var mDirname: TextViewExt
    private lateinit var mLblSize: TextViewExt
    private lateinit var mSize: TextViewExt
    private lateinit var btnPhoto: ButtonExt
    private lateinit var btnRotateLeft: ButtonExt
    private lateinit var btnPlay: ButtonExt
    private lateinit var btnRotateRight: ButtonExt
    private lateinit var btnCopyPrev: ButtonExt
    private lateinit var btnNext: ButtonExt
    private lateinit var btnPrevious: ButtonExt
    private lateinit var btnLast: ButtonExt
    private lateinit var btnFirst: ButtonExt
    private lateinit var mFilename: TextViewExt
    private lateinit var mCreation: TextViewExt
    private var dirname: String = ""
    private var filename: String = ""
    private var rotation: Float = 0f
    private var systeem: Systeem = Systeem(this)
    private var product: Product = Product(this)
    private var orderLine: OrderLine = OrderLine()
    private lateinit var tblCategory: Category
    private lateinit var tblFace: TableFace
    private var productRel: ProductRel = ProductRel(this)
    
    private lateinit var mTabLayoutFiles: TabLayoutExt

    private lateinit var mTabLayoutFrags: TabLayoutExt
    private lateinit var mTabViewpagerFrags: ViewPager2

    private var mTblLayout2: TableLayout? = null
    private var productid = -1
    private var categoryid = 0
    private lateinit var mSubType: SwitchExt
    private lateinit var mEditText3: EditTextExt
    private lateinit var mGPSLocation: EditTextExt
    private lateinit var mGPSStatus: EditTextExt
    private lateinit var mRowDistance: TableRow
    private lateinit var mDistance: TextViewExt
    private var arrCategoryMut: MutableList<CategoryDetail> = mutableListOf()
    private var pageManager = PageManager()
    private var bSetupTable = false
    private var objects = StringBuffer()
    private lateinit var mViewMain0: LinearLayout
    private lateinit var mViewMain1: LinearLayout
    private lateinit var mViewMain2: LinearLayout
    private lateinit var arrCat: ArrayList<CategoryLine>
    private lateinit var arrTypes: ArrayList<Int>
    private var parentWindow = ""
    private val selectmenu = ConstantsFixed.LAST_ID + 1
    private lateinit var groupMedia: RelativeLayout
    private lateinit var imageView: TouchImageView
    private lateinit var videoView: VideoViewExt
    private var parcelable: Parcelable? = null

    private lateinit var textView: TextView
    private lateinit var textMain: ScrollView

    private lateinit var pdfMain: FrameLayout
    private lateinit var pdfView: PdfRendererView
    private lateinit var pdfProgressBar: ProgressBar

    private var isImage = false
    private var isMediaPlayer = false
    private var isPlaying = false
    private var lastMarginLeft = 0
    private var lastMarginRight = 0
    private var lastPosition = 0

    private var faceData = ""
    private var distance:Double? = 0.0
    private var spoof = 0

    //endregion

    //region region 01 - init
    override fun initActivity() {
        helpText = Help().getHelpTitle(className)
        // intent.extras.getBundle("data")
        ConstantsLocal.arrProductRel = ArrayList()
        btnFirst = findViewById(R.id.btnFirst)
        btnLast = findViewById(R.id.btnLast)
        btnNext = findViewById(R.id.btnNext)
        btnPrevious = findViewById(R.id.btnPrevious)

        product = Product(this)
        systeem = Systeem(this)
        orderLine = OrderLine()
        tblCategory  = Category(this)
        tblFace  = TableFace()
        if (intent.hasExtra("parent")) {
            parentWindow = intent.getStringExtra("parent")!!
        }
        //Initialize Face Detector

        fillArrCat()

        if (currentModi == ConstantsFixed.ScreenModi.ModeEdit){
            contextMenuAdd(ConstantsFixed.SAMPLE_ID, getText(R.string.activity_list_category).toString(), R.drawable.tablecontents,4,1)
            if (ConstantsLocal.isScanImageEnabled) {
                contextMenuAdd(ConstantsFixed.OCR_ID, getText(R.string.ocr2clipboard).toString(), R.drawable.ocr,5,1)
            }
            if (ConstantsLocal.isScannerEnabled) {
                contextMenuAdd(ConstantsFixed.QR_ID, getText(R.string.qr2clipboard).toString(), R.drawable.qr_code,6,1)
            }
        }

        contextMenuAdd(ConstantsFixed.PRINT_ID, getText(R.string.print).toString(), R.drawable.print_normal,7,1)
        contextMenuAdd(ConstantsFixed.MAIL_ID, getText(R.string.mail).toString(), R.drawable.mail_normal,8,1)
        contextMenuAdd(ConstantsFixed.WHATSAPP_ID, getText(R.string.whatsapp).toString(), R.drawable.whatsapp,9,1)
        contextMenuAdd(ConstantsFixed.OPEN_APP_ID, getText(R.string.openapp).toString(), R.drawable.open_file,10,1)
        contextMenuAdd(ConstantsFixed.OPEN_APP_WITH_ID, getText(R.string.openappwith).toString(), R.drawable.open_file,11,1)

        mTabLayoutFiles = findViewById(R.id.tab_layout_files)
        mTabLayoutFrags = findViewById(R.id.tab_layout)
        mTabLayoutFiles.alFragmentNames.clear()

        TouchImageAdapter.images.forEachIndexed { index, s ->
            val fragmentNames = FragmentNames()
            fragmentNames.filename = s.filename
            fragmentNames.folder = s.dirname
            fragmentNames.title = s.filename
            fragmentNames.rotation = s.rotation.toInt()
            fragmentNames.fragmentName = "${index+1}-${TouchImageAdapter.images.size}"
            mTabLayoutFiles.alFragmentNames.add(fragmentNames)
        }

        //j mTabViewpagerFiles = findViewById(R.id.tabs_viewpager_files)
        mTabViewpagerFrags = findViewById(R.id.tabs_viewpager_frags)
        mCodeTop = findViewById(R.id.txtCodeTop)
        mTopTitle = findViewById(R.id.txtTopTitle)

        findViewById<ButtonExt>(R.id.btnFirst).setOnClickListener {
            lastPosition = 0
            scrollProduct(lastPosition)
        }
        findViewById<ButtonExt>(R.id.btnLast).setOnClickListener {
            lastPosition = TouchImageAdapter.images.size -1
            scrollProduct(lastPosition)
        }
        findViewById<ButtonExt>(R.id.btnNext).setOnClickListener {
            if (lastPosition + 1< TouchImageAdapter.images.size) {
                scrollProduct(++lastPosition)
            }
        }
        findViewById<ButtonExt>(R.id.btnPrevious).setOnClickListener {
            if (lastPosition - 1 >= 0) {
                scrollProduct(--lastPosition)
            }
        }

        mEditText3 = EditTextExt(this)
        ViewUtils.setDBColumn(mEditText3,"category", null, groupNo)
        mEditText3.visibility = View.GONE
        (viewMain  as ViewGroup).addView(mEditText3)

        initTabs()
        if (currentModi == ConstantsFixed.ScreenModi.ModeNew){
            productid = 0
            btnPrevious.visibility = View.GONE
            btnFirst.visibility = View.GONE
            btnLast.visibility = View.GONE
            btnNext.visibility = View.GONE
            mCodeTop.visibility = View.GONE
            mTopTitle.visibility = View.GONE
            fillProduct (0)
            bSetupTable=true
        } else if (currentModi == ConstantsFixed.ScreenModi.ModeEdit){
            val id = CalcObjects.stringToInteger(intent.getStringExtra(TableProduct.Columns._id.name),0)
            pageManager.backup (mTblLayout2)
            fillProduct (id)
            enableScrollButton (id)
            if (Product(this).getFilesCount() == 1){
                btnPrevious.visibility = View.GONE
                btnFirst.visibility = View.GONE
                btnLast.visibility = View.GONE
                btnNext.visibility = View.GONE
                mCodeTop.visibility = View.GONE
                mTopTitle.visibility = View.GONE
            }
        }

        if (ConstantsLocal.isFileFancyScrollEnabled) {
            findViewById<TableLayout>(R.id.normalScroll).visibility = View.GONE
            myRunnable()
        } else {
            mTabLayoutFiles.visibility = View.GONE
            val id = CalcObjects.stringToInteger(
                intent.getStringExtra(TableProduct.Columns._id.name), 0
            )
            lastPosition = TouchImageAdapter.images.indexOfFirst { it.id == id }
            getProduct(-1, lastPosition )
        }
    }

    private fun initTabs() {
        val adapter =
            TabsPagerAdapterExt(this, Constants.localFragmentManager!!, Constants.localLifecycle!! )

        adapter.setResources(mTabLayoutFrags, mTabViewpagerFrags
            ,arrayListOf( R.layout.detail_file_tab_frag0, R.layout.detail_file_tab_frag1, R.layout.detail_file_tab_frag2 )
            ,arrayListOf( getText(R.string.file).toString()
                , getText(R.string.details).toString(), getText(R.string.category).toString() ))

        mTabLayoutFrags.setOnTabClickListener(object : TabLayoutExt.OnTabClickListener {
            override fun onTabClicked(position: Int) {
                ViewUtils.hideKeyboard(mTabLayoutFrags, this@DetailFileTab)
                when (position) {
                    0 -> fillList0()
                    1 -> fillList1()
                    2 -> fillList2()
                }
            }
        })
    }

    override fun initTableLayout() {
        addTableLayout(
            R.id.tblLayout2,
            R.layout.line_textview,
            tblCategory,
            arrayOf(
                TableCategory.Columns.description.name
            ),
            null,
            null
        )
    }

    //endregion

    fun getProduct(productid: Int, index: Int): ProductDetail {

        val idx = if (productid > 0){
            TouchImageAdapter.images.indexOfFirst { it.id == productid }
        } else {
            index
        }
        val dt = TouchImageAdapter.images[idx]
        if (dt.filename.isEmpty()){
            val rtn = Product(this).getWhere(" _id = $productid")
            if (rtn.cursor.moveToFirst()){
                do{
                    dt.filename=ViewUtils.getValueCursor(rtn.cursor, TableProduct.Columns.filename.name)
                    dt.dirname=ViewUtils.getValueCursor(rtn.cursor, TableProduct.Columns.dirname.name)
                    dt.rotation=ViewUtils.getValueCursor(rtn.cursor, TableProduct.Columns.rotation.name).toFloat()
                } while (rtn.cursor.moveToNext())
            }
            rtn.cursorClose()
        }
        TouchImageAdapter.images[idx] = dt
        return dt
    }

    @SuppressLint("SetTextI18n")
    private fun fillProduct(id: Int){
        if (id == 0) {
            return
        }
        if (productid != id && this::mViewMain1.isInitialized) {
            val rtn = Product(this).getProduct(id)
            if (rtn.cursor.moveToFirst()){

                pageManager.backup (mTblLayout2)

                productid = id
                categoryid = rtn.cursor.getColumnValueInt(TableProduct.Columns._categoryid.name,0)
                ViewUtils.copyCursorToViewGroup(rtn.cursor, (viewMain as ViewGroup?))
                distance = DBUtils.eLookUp(TableFace.Columns.distance.name, TableFace.TABLE_NAME,
                    "${TableFace.Columns._productid.name} = $id " +
                            " and ${TableFace.Columns.seqno.name} = 1" ).toString().toDoubleOrNull()
                if (!ConstantsLocal.isFaceRecognitionEnabled || distance == null){
                    mRowDistance.visibility =  View.GONE
                } else {
                    mRowDistance.visibility =  View.VISIBLE
                    if (distance == 0.0) {
                        mDistance.textExt = "100%"
                    } else {
                        mDistance.textExt = DecimalFormat("0").format(distance!! * 100).toInt().toString() + "%"
                    }
                }
                faceData = ""

                if (ConstantsFixed.ScreenModi.ModeBrowse == currentModi) {
                    ViewUtils.disableChildBrowse((viewMain as ViewGroup))
                } else {
                    // turn off new status
                    productRel.deleteIdType(productid, ConstantsLocal.TYPE_AUTOMATED_NEW)
                }
                filename = mFilename.text.toString()
                dirname = mDirname.text.toString()
                rotation = mRotation.text.toString().toFloat()
                mCreation.textExt = FilesFolders.fileCreationDate("$dirname/$filename")

                arrCategoryMut = tblCategory.getCategoryCodes(productid, arrTypes)

                if (mTblLayout2 == null){
                    bSetupTable = true
                } else {
                    bSetupTable = false
                    PageManager.setupTable(mTblLayout2, arrCategoryMut)
                }
                val idx = TouchImageAdapter.images.indexOfFirst { it.id == productid }

                mCodeTop.text = (idx+1).toString() + "-" + TouchImageAdapter.images.size.toString()
                mTopTitle.text = ViewUtils.getValueCursor(rtn.cursor, TableProduct.Columns.title.name)

                if (ConstantsLocal.fileSizeMB) {
                    val sizeKb = ViewUtils.getValueCursorInt(rtn.cursor,"sizekb",0)
                    if (sizeKb <= 512){
                        mLblSize.textExt = getText(R.string.sizekb).toString()
                        mSize.textExt = sizeKb.toString()
                    } else {
                        mLblSize.textExt = getText(R.string.sizemb).toString()
                        mSize.textExt = DecimalFormat("0.0").format(sizeKb.toDouble() / 1024)
                    }
                }
                PageManager.restore(mTblLayout2)

                val bAccess  = (FilesFolders.hasFileAccess("$dirname/$filename"))
                btnRotateLeft.isEnabled = bAccess
                btnRotateRight.isEnabled = bAccess
                isPlaying = false
                when (File(filename).extension.lowercase()) {
                    "txt", "csv" -> {
                        var text = ""
                        var reader: BufferedReader? = null

                        try {
                            reader = BufferedReader(FileReader("$dirname/$filename"))
                            text = reader.readLines().joinToString("\n")
                        } catch (e: IOException) {
                            Toast.makeText(applicationContext, "Error reading file!", Toast.LENGTH_SHORT).show()
                            Logging.d(e.stackTraceToString())
                        } finally {
                            try {
                                reader?.close()
                            } catch (e: IOException) {
                                //log the exception
                                Logging.d(e.stackTraceToString())
                            }
                            textView.text = text
                        }
                        textMain.visibility = View.VISIBLE
                        groupMedia.visibility = View.GONE
                        pdfMain.visibility = View.GONE
                    }
                    "pdf" -> {
                        pdfView.initWithFile(
                            File("$dirname/$filename"),
                            ConstantsFixed.PdfQuality.NORMAL
                        )
                        pdfMain.visibility = View.VISIBLE
                        groupMedia.visibility = View.GONE
                        textMain.visibility = View.GONE
                    }
                    else -> {
                        enableMedia(dirname, filename, rotation)
                    }
                }
                ViewUtils.hideKeyboard(currentFocus, this)
            }
            rtn.cursorClose()
        }
    }

    private fun fillArrCat(){
        arrTypes = arrayListOf(ConstantsLocal.TYPE_CATEGORY,ConstantsLocal.TYPE_CREATION)
        if (ConstantsLocal.isGPSEnabled) arrTypes.add(ConstantsLocal.TYPE_GPS)
        if (ConstantsLocal.isFaceRecognitionEnabled) arrTypes.add(ConstantsLocal.TYPE_FACE)
        if (ConstantsLocal.isAutoNewEnabled) arrTypes.add(ConstantsLocal.TYPE_AUTOMATED_NEW)
        arrCat = tblCategory.fillCategoryArray(arrTypes, inclSeqno = true, inclCount = false)

    }

    //region region 03 - OnClickListener, openfile

    private val mLocalImgPhotoListener = View.OnClickListener {
        if (isPlaying){
            stopMedia()
        }
        openFile()
    }

    private fun openFile() {

        val properties = DialogProperties()
        properties.dir = DialogConfigs.STORAGE_STORAGE
        properties.errorDir = File(properties.dir)
        properties.root = File(properties.dir)
        btnRotateLeft.isEnabled = false
        btnRotateRight.isEnabled = false
        val lastPath = Systeem(this).getValue(SystemAttr.LastPath)
        if (lastPath.isNotEmpty()){
            properties.offset=File(lastPath)
        } else if (mDirname.text.isNullOrBlank()){
            properties.offset = File(properties.dir)
        } else{
            properties.offset = File(mDirname.text.toString())
        }
        //properties.extensions = arrayOf("jpg","bmp","png","jpeg","gif","mp4","3gp","mkv","ts","pdf","txt","csv")
        properties.extensions = arrayOf("")
        properties.selectionType = DialogConfigs.FILE_SELECT
        properties.selectionMode = DialogConfigs.SINGLE_MODE
        properties.title = getText(R.string.selectfile).toString()

        val dialog =  FilePickerDialog(this,properties)
        if (!mDirname.text.isNullOrBlank()) {
            properties.markfiles=arrayListOf(mDirname.text.toString() + "/" + mFilename.text.toString())
        }

        dialog.setDialogSelectionListener(object : DialogSelectionListener {
            override fun onSelectedFilePaths(lastPath: String?, files: Array<String?>?) {
                //files is the array of paths selected by the App User.
                if (files != null) {
                    for (path in files) {
                        val file = File(path!!)
                        val fileProduct = FolderProduct()
                        filename = file.name
                        dirname = file.absolutePath

                        Systeem(this@DetailFileTab).setValue(SystemAttr.LastPath,
                            dirname,
                            3
                        )
                        dirname = dirname.substring(0, dirname.length - filename.length - 1)
                        mFilename.textExt = filename
                        mDirname.textExt = dirname
                        mSize.textExt = (file.length()/1024).toInt().toString()
                        mRotation.setText("0",false)

                        isImage = file.extension.lowercase() in listOf("gif", "jpeg", "jpg", "raw", "png", "webp", "bmp")
                        isMediaPlayer = file.extension.lowercase() in listOf("mp4","3gp","mkv","ts")

                        val bAccess = FilesFolders.hasFileAccess("$dirname/$filename")
                        if ((isImage || isMediaPlayer ) && bAccess){
                            val rotation = Graphics.getFileRotation ("$dirname/$filename")
                            if (rotation != 0f) mRotation.setText(rotation.toString(),false)
                            btnRotateLeft.isEnabled = true
                            btnRotateRight.isEnabled = true
                            if (isMediaPlayer) btnPlay.visibility = View.VISIBLE
                        }

                        mGPSStatus.textExt = "0"
                        if (bAccess && ConstantsLocal.isGPSEnabled){
                            val gpsinfo =  FilesGPS.gpsLocation(this@DetailFileTab, dirname, filename)
                            mGPSStatus.textExt = gpsinfo.rtn.toString()
                            if (gpsinfo.rtn == 0) {
                                mGPSLocation.textExt = gpsinfo.address.toString()
                            }else{
                                mGPSLocation.textExt = gpsinfo.mess
                            }
                        } else {
                            ToastExt().makeText(this@DetailFileTab, R.string.mess062_noaccess_file, Toast.LENGTH_LONG).show()
                        }
                        enableMedia(dirname, filename, mRotation.text.toString().toFloat())
                        if (productid > 0) {

                            fileProduct.id = productid
                            fileProduct.filename = filename
                            fileProduct.folder = dirname

                            product.fillArrayCategory(this@DetailFileTab)

                            arrCategoryMut.filter {it.type == ConstantsLocal.TYPE_CREATION && it.checked == -1 }
                                .forEach { mut ->
                                    mut.checked = 0
                                    mTblLayout2!!.children.forEach { rw ->
                                        (rw as TableRow).children.filter{
                                            it::class.simpleName == SwitchExt::class.simpleName &&
                                                    it.tag != ""}.forEach{ sw ->
                                            val fieldSwitch = sw as SwitchExt
                                            val id = TagModify.getViewTagValue(fieldSwitch, "catid").toInt()
                                            if (id == mut.categoryid){
                                                fieldSwitch.isChecked = false
                                            }
                                        }
                                    }
                                }
                            product.imageCreationDate(this@DetailFileTab, fileProduct, product.arrCategoryCreation )
                            product.fileExtension(this@DetailFileTab, fileProduct, product.arrFileExtension )

                            mCreation.textExt = fileProduct.creationDateStr()

                            product.gpsLocations(this@DetailFileTab, productid, dirname, filename, null, false, product.arrCategoryGPS)

                            // refresh activity SearchFiles
                            intent.putExtra(Constants.RequestCode,"2")

                            TouchImageAdapter.images.filter { it.id == productid }.forEach { productDetail ->
                                productDetail.dirname  = dirname
                                productDetail.filename = filename
                                productDetail.rotation = Graphics.getFileRotation ("$dirname/$filename")
                                if (productDetail.rotation != 0f){
                                    Logging.d("File: $dirname/$filename Rotation: ${productDetail.rotation}")
                                }
                            }
                        }
                    }
                }
            }

            override fun onCancelPaths(lastPath: String?) {
                if (lastPath != null) {
                    Systeem(this@DetailFileTab).setValue(SystemAttr.LastPath,
                        lastPath,
                        3
                    )
                }
            }
        })
        dialog.show()

    }

    private val mLocalCopyPrevListener = View.OnClickListener {
        if (mTblLayout2 == null) {
            return@OnClickListener
        }
        val currIdx = TouchImageAdapter.images.indexOfFirst { it.id == productid }

        if (mSeqno.textExt != null
            && mSeqno.textExt!!.isDigitsOnly()
            && mSeqno.textExt!!.toInt()>0
            && mSeqno.textExt!!.toInt() < TouchImageAdapter.images.size+1
            && mSeqno.textExt!!.toInt() != currIdx+1){
            // OK
        } else {
            ToastExt().makeText(this, R.string.mess059_rangeerror, Toast.LENGTH_SHORT).show()
            return@OnClickListener
        }
        val idx = mSeqno.textExt!!.toInt()
        val id = TouchImageAdapter.images[idx-1].id
        val rtn = productRel.getProductRelProductId(id)
        var bCopy = false
        if (rtn.cursor.moveToFirst()){
            do {
                val categoryid = rtn.cursor.getColumnValueInt(TableProductRel.Columns._categoryid.name)
                mTblLayout2!!.children.forEach { rw ->
                    (rw as TableRow).children.filter{
                        it::class.simpleName == SwitchExt::class.simpleName &&
                                it.tag != ""}.forEach{ sw ->
                        val fieldSwitch = sw as SwitchExt
                        val catId = TagModify.getViewTagValue(fieldSwitch, "catid").toInt()
                        val type = TagModify.getViewTagValue(fieldSwitch, "type").toInt()
                        if (type == ConstantsLocal.TYPE_CATEGORY && catId == categoryid){
                            bCopy = true
                            fieldSwitch.isChecked = true
                            arrCategoryMut.find { it.categoryid == catId }?.checked = -1
                        }
                    }
                }

            } while (rtn.cursor.moveToNext())
            if (bCopy) {
                ToastExt().makeText(this, R.string.mess060_copydone, Toast.LENGTH_SHORT).show()
            } else {
                ToastExt().makeText(this, R.string.mess061_noupdate, Toast.LENGTH_SHORT).show()
            }
        }
        rtn.cursorClose()
    }

    private val mLocalRotateLeft = View.OnClickListener { rotateImage(ConstantsFixed.ROTATELEFT_ID) }
    private val mLocalRotateRight = View.OnClickListener { rotateImage(ConstantsFixed.ROTATERIGHT_ID) }

    //endregion

    private fun scrollProduct(idx: Int): Boolean{
        if (idx >=0 && idx < TouchImageAdapter.images.size && saveScreen().returnValue) {

            // hide keyboard
            if (this.currentFocus != null){
                ViewUtils.showKeyboard(this.currentFocus!!, this)
            }
            val id = TouchImageAdapter.images[idx].id
            fillProduct(id)

            enableScrollButton(id)
            return true
        }
         return false
    }

    private fun enableScrollButton(id: Int){
        val idx = TouchImageAdapter.images.indexOfFirst { it.id == id }
        btnPrevious.isEnabled=idx>0
        btnFirst.isEnabled=idx>0
        btnLast.isEnabled=(idx+1 < TouchImageAdapter.images.size)
        btnNext.isEnabled=(idx+1 < TouchImageAdapter.images.size)
    }

    //region region 11 - fillList 0, 1 and 2

    @SuppressLint("ClickableViewAccessibility")
    fun fillList0() {
        if (this::mViewMain0.isInitialized) return
        if (findViewById<LinearLayout>(R.id.viewMain0) == null) return
        mViewMain0 = findViewById(R.id.viewMain0)

        imageView = findViewById(R.id.imageView)
        videoView = findViewById(R.id.videoView)
        videoView.setMediaController(MediaController(this))

        textView = findViewById(R.id.textView)
        textMain = findViewById(R.id.textMain)

        groupMedia = findViewById(R.id.groupMedia)
        lastMarginLeft = groupMedia.marginLeft
        lastMarginRight = groupMedia.marginRight
        videoView.setOnCompletionListener(mOnCompletionListener)

        videoView.setOnKeyListener { _, keyCode, _ ->
            when (keyCode){
                KeyEvent.KEYCODE_MEDIA_STOP -> {
                    if (videoView.isPlaying) {
                        groupMedia.visibility = View.GONE
                    }
                    true
                }
                else -> {
                    true
                }
            }
        }

        pdfMain = findViewById(R.id.pdfMain)
        pdfView = findViewById(R.id.pdfView)
        pdfProgressBar = findViewById(R.id.pdfProgressBar)

        if (mTabLayoutFiles.visibility != View.GONE) {
            mTabLayoutFiles.setViewPager(null)
            mTabLayoutFiles.setOnTabClickListener(object : TabLayoutExt.OnTabClickListener {
                override fun onTabClicked(position: Int) {
                    ViewUtils.hideKeyboard(mTabLayoutFiles, this@DetailFileTab)
                    if (scrollProduct(position)) {
                        getProduct(-1, position)
                        mTabLayoutFiles.setTabAt( position )
                    }
                }
            })
        }

        btnPhoto = findViewById(R.id.btnPhoto)
        btnPhoto.setOnClickListener(mLocalImgPhotoListener)
        btnCopyPrev = findViewById(R.id.btnCopyPrev)
        mSeqno = findViewById(R.id.txtSeqno)
        mSeqno.textExt = "1"
        mSeqno.skipEditTagAlways = true
        btnPlay = findViewById(R.id.btnPlay)
        btnPlay.setOnClickListener {
            if (isPlaying){
                stopMedia()
            } else {
                playMedia(dirname, filename)
            }
        }

        if (ConstantsLocal.isSmartCopyCategory &&
            currentModi == ConstantsFixed.ScreenModi.ModeEdit &&
            Product(this).getFilesCount() > 1){
            btnCopyPrev.setOnClickListener(mLocalCopyPrevListener)
        } else {
            btnCopyPrev.visibility = View.GONE
            mSeqno.visibility = View.GONE
        }

        btnRotateLeft = findViewById(R.id.btnRotateLeft)
        btnRotateLeft.setOnClickListener(mLocalRotateLeft)
        btnRotateRight = findViewById(R.id.btnRotateRight)
        btnRotateRight.setOnClickListener(mLocalRotateRight)
        if (ConstantsFixed.ScreenModi.ModeBrowse == currentModi) {
            btnPhoto.visibility = View.GONE
            btnCopyPrev.visibility = View.GONE
            mSeqno.visibility = View.GONE
        }
    }

    private val mOnCompletionListener = MediaPlayer.OnCompletionListener {
        imageView.visibility = View.VISIBLE
        videoView.visibility = View.GONE
        btnPlay.background = ContextCompat.getDrawable(this, R.drawable.button_play)
    }

    fun fillList1() {
        if (this::mViewMain1.isInitialized) return
        if (findViewById<LinearLayout>(R.id.viewMain1) == null) return
        mViewMain1 = findViewById(R.id.viewMain1)

        mTitle = findViewById(R.id.txtTitle)
        ViewUtils.setDBColumn(mTitle,TableProduct.Columns.title.name, product.tableName, true)

        mOms = findViewById(R.id.txtOms)
        ViewUtils.setDBColumn(mOms,TableProduct.Columns.description.name, product.tableName, true)

        mCode = findViewById(R.id.txtCode)
        ViewUtils.setDBColumn(mCode,TableProduct.Columns.code.name, product.tableName, true)

        mBlock = findViewById(R.id.colBlok)
        ViewUtils.setDBColumn(mBlock,TableProduct.Columns.blocked.name)
        mBlock.setChecked(checked = false, init = true)

        mPrijs = ViewUtils.findMoneyId(mViewMain1, EditTextMoney::class.simpleName!!, R.id.hdrPrijs) as  EditTextMoney
        ViewUtils.setDBColumn(mPrijs,TableProduct.Columns.price.name)
        if (!ConstantsLocal.isPriceUseEnabled) mPrijs.visibility=View.GONE

        mRotation = findViewById(R.id.txtRotation)
        ViewUtils.setDBColumn(mRotation,TableProduct.Columns.rotation.name)

        mGPSLocation = findViewById(R.id.txtGPSLocation)
        ViewUtils.setDBColumn(mGPSLocation,TableProduct.Columns.gpslocation.name)

        mGPSStatus = findViewById(R.id.txtGPSStatus)
        ViewUtils.setDBColumn(mGPSStatus,TableProduct.Columns.gpsstatus.name)

        ViewUtils.setDBColumn(mPrijs,TableProduct.Columns.price.name)

        if (!ConstantsLocal.isPriceUseEnabled){
            val lblPrijs:TextViewExt = findViewById(R.id.lblPrijs)
            lblPrijs.visibility =  View.GONE
            mPrijs.visibility = View.GONE
        }
        mDateAvailable = findViewById(R.id.txtDateAvailable)
        ViewUtils.setDBColumn(mDateAvailable,TableProduct.Columns.dateavailable.name)
        mDateAvailable.minDate = Calendar.getInstance().timeInMillis

        mFilename = findViewById(R.id.txtFilename)
        mFilename.colorView = ConstantsFixed.ColorBasic.Text
        mFilename.colorChange = false
        ViewUtils.setDBColumn(mFilename, TableProduct.Columns.filename.name)

        mDirname = findViewById(R.id.txtDirname)
        mDirname.colorView = ConstantsFixed.ColorBasic.Text
        mDirname.colorChange = false
        ViewUtils.setDBColumn(mDirname, TableProduct.Columns.dirname.name)
        if (ConstantsLocal.storeFileSize) {
            mLblSize = findViewById(R.id.lblSize)
            mSize = findViewById(R.id.txtSize)
            mSize.colorView = ConstantsFixed.ColorBasic.Text
            mSize.colorChange = false
            ViewUtils.setDBColumn(mSize, TableProduct.Columns.sizekb.name)
            if (ConstantsLocal.fileSizeMB) {
                mLblSize.textExt = getText(R.string.sizemb).toString()
            }
        } else {
            findViewById<TableRow>(R.id.rowSize).visibility= View.GONE
        }

        mCreation = findViewById(R.id.txtCreation)
        mCreation.colorView = ConstantsFixed.ColorBasic.Text
        mCreation.colorChange = false
        ViewUtils.setDBColumn(mCreation,TableProduct.Columns.filelastmodified.name)

        mRowDistance = findViewById(R.id.rowDistance)
        mDistance = findViewById(R.id.txtDistance)
        mRowDistance.visibility= View.GONE

        val id = CalcObjects.stringToInteger(intent.getStringExtra(TableProduct.Columns._id.name),0)
        fillProduct (id)
    }

    fun fillList2(init: Boolean = true) {
        if (init){
            if (this::mViewMain2.isInitialized) return
            if (findViewById<LinearLayout>(R.id.viewMain2) == null) return
            mViewMain2 = findViewById(R.id.viewMain2)

            updateTableLayouts()

            if (mTblLayout2 == null
                || findViewById<SwitchExt>(R.id.colSubTypeId) == null
                || this::mSubType.isInitialized) {
                return
            }
        } else {
            if (!this::mViewMain2.isInitialized) return
        }

        mSubType = findViewById(R.id.colSubTypeId)
        mTblLayout2?.removeAllViews()
        val li = LayoutInflater.from(this)
        arrCat.filterNot { it.type == ConstantsLocal.TYPE_CREATION
                || it.type == ConstantsLocal.TYPE_GPS
                || it.type == ConstantsLocal.TYPE_AUTOMATED_NEW }.forEach {
            val sTag = "catid=${it.id};lvl=${it.level};type=${it.type}"

            if (it.hasChild){
                val mTblRow = li.inflate(R.layout.line_textswitch, mTblLayout2, false)
                val mTxt = mTblRow.findViewById<TextViewExt>(R.id.colTypeId)
                val mSw = mTblRow.findViewById<SwitchExt>(R.id.colSubTypeId)
                mSw.skipEditTagAlways = true
                if (it.type == ConstantsLocal.TYPE_GPS || it.type == ConstantsLocal.TYPE_CREATION ){
                    mSw.isEnabled = false
                } else if (ConstantsFixed.ScreenModi.ModeBrowse == currentModi) {
                    if (mSw.isEnabled) mSw.isEnabled = false
                }
                val spanString = SpannableString(it.levelTitle)
                spanString.setSpan(StyleSpan(Typeface.BOLD), 0, spanString.length, 0)
                spanString.setSpan(StyleSpan(Typeface.ITALIC), 0, spanString.length, 0)

                mTxt.text=spanString
                mTxt.tag=sTag
                mSw.tag=sTag
                PageManager.treeCollapse(mTxt, mTblLayout2!!)
                mSw.text = ""
                mSw.isChecked = false
                mSw.skipEditTagAlways = false
                mTblLayout2?.addView(mTblRow, TableLayout.LayoutParams())

            } else {
                val mTblRow = li.inflate(R.layout.line_switch, mTblLayout2, false)
                val mSw:SwitchExt = mTblRow.findViewById(R.id.colSubTypeId)
                mSw.skipEditTagAlways = true
                mSw.id = R.id.colSubTypeId
                mSw.text = it.levelTitle
                mSw.isChecked = false
                mSw.tag=sTag
                if (ConstantsFixed.ScreenModi.ModeBrowse == currentModi) {
                    if (mSw.isEnabled) mSw.isEnabled = false
                }
                mSw.skipEditTagAlways = false
                mTblLayout2?.addView(mTblRow, TableLayout.LayoutParams())
            }
        }
        if (bSetupTable || !init){
            // first time the layout2 was not available
            bSetupTable = false
            PageManager.setupTable(mTblLayout2, arrCategoryMut)
        }
        PageManager.treeColor(mTblLayout2!!)
        PageManager.restore(mTblLayout2)
    }
    //endregion

    private fun updateTableLayouts(){
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)

        if (parentWindow == "detailordertab") {
            contextMenuAdd(selectmenu,getText(R.string.select).toString(),android.R.drawable.ic_menu_zoom,0,1)
        }
        if (currentModi == ConstantsFixed.ScreenModi.ModeEdit || currentModi == ConstantsFixed.ScreenModi.ModeNew){
            contextMenuAdd(ConstantsFixed.SAVE_ID, getText(R.string.save).toString(), R.drawable.save_normal,1,1)
        }
        contextMenuAdd(ConstantsFixed.HELP_ID, getText(R.string.help).toString(), R.drawable.help_normal,2,1)
        if (ConstantsLocal.isGPSEnabled){
            contextMenuAdd(ConstantsFixed.GPSINFO_ID, getText(R.string.gpsinfo).toString(), R.drawable.gps_location,3,1)
        }

        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean{
        val file = File("$dirname/$filename")
        if (parentWindow == "detailordertab") {
            contextMenuEnable(selectmenu, productid > 0, 1)
        }

        if (file.exists()){
            contextMenuEnable(ConstantsFixed.OPEN_APP_ID, true, 1)
            contextMenuEnable(ConstantsFixed.OPEN_APP_WITH_ID, true, 1)
            if (ConstantsLocal.isGPSEnabled) {
                contextMenuEnable(ConstantsFixed.GPSINFO_ID, mGPSStatus.text.toString() == "0", 1)
            }
        } else {
            contextMenuEnable(ConstantsFixed.OPEN_APP_ID, false, 1)
            contextMenuEnable(ConstantsFixed.OPEN_APP_WITH_ID, false, 1)
            contextMenuEnable(ConstantsFixed.GPSINFO_ID, false, 1)
        }

        return super.onPrepareOptionsMenu(menu)
    }

    private fun rotateImage(direction: Int) {
        var rot = mRotation.textExt.toString().toFloat()
        when (direction) {
            ConstantsFixed.ROTATELEFT_ID -> {
                rot -= 90f
                if (rot < 0f) rot=270f
            }
            ConstantsFixed.ROTATERIGHT_ID -> {
                rot += 90f
                if (rot>=360f) rot-=360f
            }
        }
        mRotation.textExt = rot.toString()
        if (File(filename).extension.equals("pdf",true)){
            pdfMain.rotation = rot
            return
        }
        if (File(filename).extension.equals("txt",true)){
            textView.rotation = rot
            return
        }
        if (isPlaying) {
            stopMedia()
        }
        drawMedia(dirname, filename, rot)
    }

    private fun enableMedia(dirname: String, filename: String, rotation: Float){
        groupMedia.visibility = View.GONE
        val file = File("$dirname/$filename")
        if (!file.exists()) {
            return
        }
        groupMedia.visibility = View.VISIBLE
        isImage = file.extension.lowercase() in listOf("gif", "jpeg", "jpg", "raw", "png", "webp", "bmp")
        isMediaPlayer = file.extension.lowercase() in listOf("mp4","3gp","mkv","ts")
        if (isMediaPlayer) {
            videoView.stopPlayback()
            btnPlay.background = ContextCompat.getDrawable(this, R.drawable.button_play)
            btnPlay.visibility = View.VISIBLE
            imageView.visibility = View.GONE
        } else {
            btnPlay.visibility = View.GONE
            videoView.visibility = View.GONE
            imageView.visibility = View.VISIBLE
        }
        isPlaying = false
        val bAccess = (isImage || isMediaPlayer)
        btnRotateLeft.isEnabled = bAccess
        btnRotateRight.isEnabled = bAccess
        drawMedia(dirname, filename, rotation)
    }

    private fun drawMedia(dirname: String, filename: String, rotation: Float){
        Graphics.fillTouchImageView(this, "$dirname/$filename", rotation,imageView)
        if (parcelable == null) {
            parcelable = imageView.onSaveInstanceState()
        } else {
            imageView.onRestoreInstanceState(parcelable!!)
        }
    }

    private fun playMedia(dirname: String, filename: String){
        isPlaying = true
        btnPlay.background = ContextCompat.getDrawable(this, R.drawable.button_pause)
        videoView.setVideoURI("${dirname}/${filename}".toUri())
        videoView.rotation = mRotation.textExt.toString().toFloat()
        videoView.visibility = View.VISIBLE
        imageView.visibility = View.GONE
        videoView.start()
    }

    private fun stopMedia(){
        isPlaying = false
        imageView.visibility = View.VISIBLE
        videoView.visibility = View.GONE
        videoView.stopPlayback()
        btnPlay.background = ContextCompat.getDrawable(this, R.drawable.button_play)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            ConstantsFixed.GPSINFO_ID -> {
                if (mGPSLocation.textExt.isNullOrEmpty()){
                    ToastExt().makeText(this, R.string.mess008_nodata, Toast.LENGTH_SHORT).show()
                } else{
                    val intent = Intent(this, About::class.java)
                    intent.putExtra("title", getText(R.string.gpsinfo))
                    var tmp = mGPSLocation.textExt!!.replace(",","\n,")
                    tmp = tmp.replace("subadminarea=",getText(R.string.gpssubAdminarea).toString()+"=",true)
                    tmp = tmp.replace("adminarea=",getText(R.string.gpsadminarea).toString()+"=",true)
                    tmp = tmp.replace("sub-admin=",getText(R.string.gpssubAdminarea).toString()+"=",true)
                    tmp = tmp.replace("admin=",getText(R.string.gpsadminarea).toString()+"=",true)
                    tmp = tmp.replace("locality=",getText(R.string.gpslocality).toString()+"=",true)
                    tmp = tmp.replace("thoroughfare=",getText(R.string.gpsthoroughfare).toString()+"=",true)
                    tmp = tmp.replace("postalcode=",getText(R.string.gpspostalcode).toString()+"=",true)
                    tmp = tmp.replace("countrycode=",getText(R.string.gpscountrycode).toString()+"=",true)
                    tmp = tmp.replace("countryname=",getText(R.string.gpscountryname).toString()+"=",true)
                    tmp = tmp.replace("featurename=",getText(R.string.gpsfeaturename).toString()+"=",true)
                    tmp = tmp.replace("feature=",getText(R.string.gpsfeaturename).toString()+"=",true)
                    tmp = tmp.replace("phone=",getText(R.string.gpsphone).toString()+"=",true)
                    tmp = tmp.replace("extras=",getText(R.string.gpsextras).toString()+"=",true)

                    intent.putExtra("about", tmp)
                    startActivity(intent)
                }
            }
            ConstantsFixed.SAMPLE_ID -> {
                val intent = Intent(this, ListCategory::class.java)
                intent.putExtra(Constants.RequestCode, "11")
                resultLauncher.launch(intent)
            }
            ConstantsFixed.MAIL_ID -> {
                return createList(1)
            }
            ConstantsFixed.PRINT_ID -> {
                return createList(2)
            }
            ConstantsFixed.OCR_ID -> {
                val intent = Intent(this, ScanImageTab::class.java)
                intent.putExtra("filename", "$dirname/$filename")
                startActivity(intent)
            }
            ConstantsFixed.QR_ID -> {
                val intent = Intent(this, ScannerMainActivity::class.java)
                startActivity(intent)
            }
            ConstantsFixed.WHATSAPP_ID -> {
                return createList(3)
            }
            ConstantsFixed.OPEN_APP_ID, ConstantsFixed.OPEN_APP_WITH_ID -> {
                openApp(item.itemId)
                return true
            }
            selectmenu -> {
                val intentX = Intent()
                intentX.putExtra(TableProduct.Columns._id.name, productid.toString())
                intentX.putExtra("filename", "$dirname/$filename")
                intentX.putExtra(Constants.RequestCode, "9")
                setResult(RESULT_OK, intentX)
                finish()
            } else -> {
                return super.onOptionsItemSelected(item)
            }
        }
        return true
    }

    override fun resultActivity(result: ActivityResult) {
        if (result.resultCode == RESULT_OK) {
            when (CalcObjects.stringToInteger(result.data!!.getStringExtra(Constants.RequestCode), 0)) {
                1 -> {
                    tblCategory.initArray() // force reload category array
                    val strRel: MutableList<Int> = mutableListOf()
                    val arrRelMod: ArrayList<Pair<Int,Boolean>> = arrayListOf()
                    mTblLayout2!!.children.forEach { rw ->
                        (rw as TableRow).children.filter{
                            it::class.simpleName == SwitchExt::class.simpleName &&
                                    it.tag != ""}.forEach{ sw ->
                            val fieldSwitch = sw as SwitchExt
                            val catId = TagModify.getViewTagValue(fieldSwitch, "catid").toInt()
                            if (TagModify.hasTagValue(fieldSwitch,ConstantsFixed.TagSection.TsUserFlag.name, ConstantsFixed.TagAction.Edit.name)){
                                // save user setting which are not saved
                                arrRelMod.add(Pair(catId,fieldSwitch.isChecked))
                            } else {
                                if (fieldSwitch.isChecked){
                                    // save default settings
                                    strRel.add(catId)
                                }
                            }
                        }
                    }
                    // refill array
                    arrCategoryMut = tblCategory.fillCategoryMut(strRel.toList(), arrTypes)
                    // put array on screen
                    fillArrCat()
                    fillList2(false)
                    
                    // set user modified category
                    if (arrRelMod.isNotEmpty()){
                        mTblLayout2!!.children.forEach { rw ->
                            (rw as TableRow).children.filter{
                                it::class.simpleName == SwitchExt::class.simpleName &&
                                        it.tag != ""}.forEach{ sw ->
                                val fieldSwitch = sw as SwitchExt
                                val catId = TagModify.getViewTagValue(fieldSwitch, "catid").toInt()
                                arrRelMod.filter { it.first == catId }.forEach {
                                    fieldSwitch.isChecked = it.second
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun saveScreen(): ReturnValue {

        var rtn = ReturnValue()
        if (ConstantsFixed.ScreenModi.ModeBrowse == currentModi) {
            rtn.returnValue = true
            return rtn
        }

        if (!ViewUtils.hasChildTag(viewMain as ViewGroup,
                ConstantsFixed.TagSection.TsUserFlag.name,
                ConstantsFixed.TagAction.Edit.name)){
            rtn.returnValue = true
            return rtn
        }
        rtn = validateScreen()
        if (!rtn.returnValue) {
            return rtn
        }
        if (!pageManager.backup(mTblLayout2)){
            Logging.d(resources.getString(R.string.app_name),"DetailFileTab/saveScreen/pageManager/mTblLayout2 == null")
            rtn.returnValue = false
            return rtn
        }

        mTblLayout2!!.children.forEach { rw ->
            (rw as TableRow).children.filter{
                    it::class.simpleName == SwitchExt::class.simpleName &&
                    it.tag != ""}.forEach{ sw ->
                val fieldSwitch = sw as SwitchExt
                val id = TagModify.getViewTagValue(fieldSwitch, "catid").toInt()
                val chk = (if (fieldSwitch.isChecked) -1 else 0)
                arrCategoryMut.find { it.categoryid == id }?.checked = chk
            }
        }
        var update = ""
        arrCategoryMut.forEach{
            update += "${it.categoryid},${it.level},${it.checked},"
        }
        if (update.isNotEmpty()){
            update = update.substring(0,update.length-1)
        }
        val locContentValues = ContentValuesExt.copyBundleContentValues(intent.extras!!)
        locContentValues.put(
            ConstantsFixed.TagSection.TsModFlag.name,
            intent.getStringExtra(ConstantsFixed.TagSection.TsModFlag.name)
        )

        locContentValues.putAll(ViewUtils.copyViewGroupToContentValues((viewMain as ViewGroup?)!!,  groupNo ))
        locContentValues.put(TableProduct.Columns.type.name, ConstantsLocal.TYPE_FILE)
        locContentValues.put(TableProduct.Columns._id.name, productid)
        if (locContentValues.getAsString(ConstantsFixed.TagSection.TsModFlag.name) == ConstantsFixed.TagAction.Edit.name &&
            ViewUtils.hasChildTag(viewMain as ViewGroup,
                ConstantsFixed.TagSection.TsUserFlag.name,
                ConstantsFixed.TagAction.Edit.name
            )){
            rtn = product.updatePrimaryKey(locContentValues)
        } else if (locContentValues.getAsString(ConstantsFixed.TagSection.TsModFlag.name) == ConstantsFixed.TagAction.New.name) {
            rtn = product.insertPrimaryKey(locContentValues)
            productid = rtn.id
            intent.putExtra(TableProduct.Columns._id.name,productid.toString())
            intent.putExtra(
                ConstantsFixed.TagSection.TsModFlag.name,
                intent.getStringExtra(ConstantsFixed.TagSection.TsModFlag.name))
        }
        if (!rtn.returnValue) {
            return rtn
        }

        rtn = updateProductRel()
        if (!rtn.returnValue) {
            return rtn
        }

        procesFile()

        ViewUtils.removeChildTag(viewMain as ViewGroup, arrayOf( ConstantsFixed.TagSection.TsUserFlag.name))

        PageManager.restore(mTblLayout2)
        PageManager.treeColor(mTblLayout2!!)
        ToastExt().makeText(this, R.string.mess002_saved, Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK, intent)
        return rtn
    }

    private fun procesFile() {
        var categoryid = 0
        var categoryCount = 0
        faceData = ""

        arrCategoryMut.filter {it.type == ConstantsLocal.TYPE_FACE && it.checked == -1 }
            .forEach { mut ->
                mut.checkedOld
                categoryid = mut.categoryid
                categoryCount++
            }
        tblFace.delete("${TableFace.Columns._productid.name} = $productid")

        procesFile( this,productIdParm=productid,
            categoryidParm=if(categoryCount==1) categoryid else 0,
            dirnameParm=dirname,filenameParm=filename,
            spoof=spoof)
    }

    private fun procesFile( context:Context,
                            productIdParm:Int,
                            categoryidParm:Int,
                            dirnameParm:String,
                            filenameParm:String,
                            spoof: Int) {
        //Load model

        val modelFile = "mobile_face_net.tflite"
        tblFace.delete("${TableFace.Columns._productid.name} = $productIdParm")

        var bm: Bitmap? = if (File("$dirnameParm/$filenameParm").length()/1024 < (10 * 1024)) {
            try {
                BitmapResolver.getBitmap(context.contentResolver, "$dirnameParm/$filenameParm")
            } catch (_: Exception) {
                //Log.d("ImageError", "Getting Image failed");
                return
            }
        } else {
            BitmapManager.loadBitmap(context,"$dirnameParm/$filenameParm", 3000)
        }
        if (bm == null){
            return
        }
        val impPhoto = InputImage.fromBitmap(bm, 0)

        val highAccuracyOpts =
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .build()
        var detector = FaceDetection.getClient(highAccuracyOpts)

        detector.process(impPhoto).addOnSuccessListener { faces ->
            var tfLite: Interpreter? = null
            try {
                tfLite = Interpreter(loadModelFile(context, modelFile), null)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val listFaces = MediapipeFaceDetector().procesFaces(tfLite!!, bm, faces)
            tfLite.close()
            if (listFaces != null && listFaces.isNotEmpty()) {
                var categoryidMain = 0
                val rtnCategory =
                    tblCategory.selectWhere("${TableCategory.Columns.type.name} = ${ConstantsLocal.TYPE_FACE} and ${TableCategory.Columns._mainid.name} = 0")
                if (rtnCategory.cursor.moveToFirst()) {
                    categoryidMain =
                        rtnCategory.cursor.getColumnValueInt(TableCategory.Columns._id.name, 0)
                }

                listFaces.forEachIndexed {  _, it ->
                    val mapSub = ContentValues()
                    mapSub.put(TableFace.Columns._categoryid.name, categoryidMain)
                    mapSub.put(TableFace.Columns._productid.name, productIdParm)
                    mapSub.put(TableFace.Columns.seqno.name, 0)
                    mapSub.put(TableFace.Columns.spoof.name, 0)
                    mapSub.put(TableFace.Columns.distance.name, 0.0)
                    mapSub.put(TableFace.Columns.extra.name, it.toString().replace(" ",""))
                    tblFace.insertTableRow(mapSub)
                    if (listFaces.size == 1 && categoryidParm > 0) {
                        mapSub.put(TableFace.Columns.spoof.name, spoof)
                        mapSub.put(TableFace.Columns._categoryid.name, categoryidParm)
                        tblFace.insertTableRow(mapSub)
                    }
                }
            }
            detector.close()
        }
    }

    private fun updateProductRel(): ReturnValue {
        val rtn = ReturnValue()
        if (productid < 1){
            Logging.d("DetailPhoto/updateProductRel","Id = zero")
            rtn.returnValue = false
            return rtn
        }
        var bOk = false
        arrCategoryMut.filter{it.checkedOld == -1 && it.checked == 0}.forEach {
            productRel.deleteProductRel(productid, it.categoryid)
            // refresh activity SearchPhotos
            bOk = true
        }

        arrCategoryMut.filter{it.checkedOld == 0 && it.checked == -1}.forEach { catDet ->
            productRel.insertProductRel(productid, catDet.categoryid)
            // refresh activity SearchPhotos
            bOk = true
        }
        if (bOk) {
            intent.putExtra(Constants.RequestCode,"2")
        }
        return rtn
    }

    //region region 51 - export (list,mail)
    private fun createList(typeList: Int): Boolean {
        // 1=mail, 2=print, 3=whatsapp
        val dir = File(Constants.dataPath)
        if (typeList==2){
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    ToastExt().makeText(this, getText(R.string.mess005_nocreatefolder).toString() + dir.path, Toast.LENGTH_SHORT).show()
                    return false
                }
            }
        }
        objects = StringBuffer()
        objects.append(
            (StringUtils.rightPad("" + getText(R.string.file), 10)) + ": $productid"
        )
        objects.append(
            ("\n"
                    + StringUtils.rightPad("" + getText(R.string.date), 10)) + ": "
                    + mDateAvailable.text
        )
        objects.append("\n")

        if (ConstantsLocal.isPriceUseEnabled) {
            objects.append(
                ("\n\n"
                        + StringUtils.rightPad("" + getText(R.string.code), 10)
                        + StringUtils.rightPad(
                    "" + getText(R.string.description),
                    30
                )
                        + StringUtils.leftPad("" + getText(R.string.price), 10))
            )
            objects.append("\n" + StringUtils.repeat("-", 67))
        } else {
            objects.append(
                ("\n\n"
                        + StringUtils.rightPad("" + getText(R.string.code), 10)
                        + StringUtils.rightPad(
                    "" + getText(R.string.description),
                    37
                ))
            )
            objects.append("\n" + StringUtils.repeat("-", 47))
        }

        objects.append("\n" + StringUtils.repeat("-", 67))
        if (mOms.text.toString().isNotEmpty()) {
            objects.append((("\n\n"
                    + StringUtils.rightPad("" + getText(R.string.rem), 8)
                    ) + ": " + mOms.text.toString())
            )
        }
        //rtn.cursorClose()
        when (typeList){
            1 -> {
                sendMailPhoto()
            }
            2 -> {
                val file = File(dir, (getText(R.string.file).toString() + "_${productid}.txt"))
                try {
                    writeToFile(file )
                    ToastExt().makeText(
                        this,
                        (getText(R.string.mess006_listready).toString() + getText(R.string.file).toString()+ "_"
                             + productid.toString() + ".txt"),
                        Toast.LENGTH_LONG
                    ).show()
                } catch (e: IOException) {
                    ToastExt().makeText(
                        this,
                        getText(R.string.mess007_listerr).toString() + e.message.toString(),
                        Toast.LENGTH_LONG
                    ).show()
                    e.printStackTrace()
                }
            }
            3 ->{
                sendWhatsApp()
            }
        }
        return true
    }

    @Throws(IOException::class)
    private fun writeToFile(file: File ) {
        val fwrite = FileWriter(file, false)
        fwrite.write(this.objects.toString())
        fwrite.close()
    }

    private fun sendMailPhoto() {

        val emailCC = systeem.getValue(SystemAttr.SalesEMail)

        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.type = "text/plain"
        //emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(emailTo))
        emailIntent.putExtra(
            Intent.EXTRA_SUBJECT, getText(R.string.file).toString() +  "_" + productid.toString())
        emailIntent.putExtra(
            Intent.EXTRA_TEXT,
            this.objects.toString()
        )
        emailIntent.putExtra(Intent.EXTRA_CC, arrayOf(emailCC))
        try {
            val file = File("$dirname/$filename")
            if (file.exists()){
                emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
            }
        } catch (_: Exception) {}
        startActivity(Intent.createChooser(emailIntent, "Send mail..."))
    }

    private fun openApp(itemId: Int) {
        val rtn = Product(this).getProduct(productid)
        if (rtn.cursor.count == 1) {
            if (rtn.cursor.moveToFirst()) {
                val file = File("$dirname/$filename")
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
                } catch (_: Exception) {
                }
            }
        }
        rtn.cursorClose()
    }

    private fun sendWhatsApp(): Boolean{
        try {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, objects.toString())
                //putExtra("jid", "${phone}@s.whatsapp.net")
                type = "text/plain"
                if (ConstantsLocal.isGbWhatsAppEnabled) {
                    setPackage("com.gbwhatsapp")
                } else {
                    setPackage("com.whatsapp")
                }
            }
            val rtn = Product(this).getProduct(productid)
            if (rtn.cursor.count == 1){
                if (rtn.cursor.moveToFirst()) {
                    try{
                        val file = File("$dirname/$filename")
                        if (file.exists()) {
                            sendIntent.putExtra(
                                Intent.EXTRA_STREAM,
                                Uri.fromFile(file)
                            )
                        }
                    } catch (_: Exception) {}
                }
            }
            rtn.cursorClose()
            startActivity(sendIntent)
            return true
        }catch (e: Exception){
            e.printStackTrace()

            if (ConstantsLocal.isGbWhatsAppEnabled) {
                startActivity(Intent(Intent.ACTION_VIEW,
                    "https://androidapksfree.com/gb-whatsapp/com-gbwhatsapp".toUri()))
            } else{
                val appPackageName = "com.whatsapp"
                try {
                    startActivity(Intent(Intent.ACTION_VIEW,
                        "market://details?id=$appPackageName".toUri()))
                } catch (_ :ActivityNotFoundException) {
                    startActivity(Intent(
                        Intent.ACTION_VIEW,
                        "https://play.google.com/store/apps/details?id=$appPackageName".toUri()))
                }
            }
        }
        return false
    }

    private fun myRunnable() {
        val runnable = object : Runnable {
            override fun run() {
                if (!isFinishing) {
                    // first time force refresh photo at the right spot
                    val id = CalcObjects.stringToInteger(
                        intent.getStringExtra(TableProduct.Columns._id.name), 0
                    )
                    val idx = TouchImageAdapter.images.indexOfFirst { it.id == id }
                     mTabLayoutFiles.setTabAt(idx)
                }
            }
        }
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(runnable,5000)
    }
}