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

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.viewpager2.widget.ViewPager2
import com.farrusco.filecatalogue.R
import com.farrusco.filecatalogue.business.Help
import com.farrusco.filecatalogue.common.ConstantsLocal
import com.farrusco.filecatalogue.utils.Translations
import com.farrusco.projectclasses.activity.BaseActivity
import com.farrusco.projectclasses.activity.FragmentCreate
import com.farrusco.projectclasses.common.Constants
import com.farrusco.projectclasses.graphics.cropper.CropImage
import com.farrusco.projectclasses.graphics.cropper.CropImageIntentChooser
import com.farrusco.projectclasses.graphics.cropper.CropImageOptions
import com.farrusco.projectclasses.graphics.cropper.CropImageView
import com.farrusco.projectclasses.graphics.cropper.parcelable
import com.farrusco.projectclasses.graphics.imagescanner.CameraActivity
import com.farrusco.projectclasses.messages.ProgressIndicator
import com.farrusco.projectclasses.utils.TextAnalyser
import com.farrusco.projectclasses.utils.ViewUtils
import com.farrusco.projectclasses.widget.ButtonExt
import com.farrusco.projectclasses.widget.EditTextExt
import com.farrusco.projectclasses.widget.SpinnerExt
import com.farrusco.projectclasses.widget.tablayout.TabLayoutExt
import com.farrusco.projectclasses.widget.tablayout.TabsPagerAdapterExt
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

open class ScanImageTab: BaseActivity(), FragmentCreate.OnFragmentAttachListener {

/*    private val handler = CoroutineExceptionHandler { _, exception ->
        Logging.e("ImageToText: $exception")
    }*/
    override val layoutResourceId: Int = com.farrusco.projectclasses.R.layout.scan_photo_tab
    override val mainViewId: Int = com.farrusco.projectclasses.R.id.viewMain

    private lateinit var mOms1: EditTextExt
    private lateinit var mOms2: EditTextExt
    private lateinit var btnPhoto: ButtonExt
    private lateinit var mBtnScan: ButtonExt
    private lateinit var mBtnSave1: ButtonExt
    private lateinit var mBtnSave2: ButtonExt
    private lateinit var btnTranslate1: ImageView
    private lateinit var btnTranslate2: ImageView
    private lateinit var mSpTranslateFrom: SpinnerExt
    private lateinit var mSpTranslateTo: SpinnerExt

    private lateinit var mTabsViewpager: ViewPager2
    private lateinit var mTabLayout: TabLayoutExt
    private lateinit var mTabLayoutExt: TabLayout
    private lateinit var mViewMain0: LinearLayout
    private lateinit var mViewMain1: LinearLayout
    private lateinit var mCropImageView: CropImageView
    private var latestTmpUri: Uri? = null
    //private val selectmenu = ConstantsFixed.LAST_ID + 1
    private lateinit var progressIndicator: ProgressIndicator
    private var toolbar: androidx.appcompat.widget.Toolbar? = null
    private var cropImageUri: Uri? = null
    private lateinit var cropImageOptions: CropImageOptions
    private var mFilename = ""
    private lateinit var translations: Translations

    private val pickImageGallery = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        onPickImageResult(uri)
    }

    //region region 01 - init
    override fun initActivity() {
        helpText = Help().getHelpTitle(className)
        progressIndicator = ProgressIndicator(this, false)
        translations = Translations(this,layoutInflater)

        toolbar = findViewById(com.farrusco.projectclasses.R.id.my_toolbar)
        if (toolbar != null){
            setSupportActionBar(toolbar)
        }

        mTabLayout = findViewById(R.id.tab_layout)
        mTabsViewpager = findViewById(R.id.tabs_viewpager)

        val dir = File(cacheDir.path)
        if (dir.exists()) {
            dir.deleteRecursively()
        }
        dir.mkdirs()

        initTabs()
        mTabsViewpager.currentItem = 0

    }

    private fun initTabs() {
        val adapter =
            TabsPagerAdapterExt(this, Constants.localFragmentManager!!, Constants.localLifecycle!!)

        adapter.setResources(
            mTabLayout,
            mTabsViewpager,
            arrayListOf(
                R.layout.scan_photo_tab_frag0,
                R.layout.scan_photo_tab_frag1
            ),
            arrayListOf(
                getText(R.string.file).toString(),
                getText(R.string.text).toString()
            )
        )
        mTabLayout.setOnTabClickListener(object : TabLayoutExt.OnTabClickListener {
            override fun onTabClicked(position: Int) {
                mTabsViewpager.isUserInputEnabled = (position > 0)

                // hide keyboard
                //val imm=getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                //imm.hideSoftInputFromWindow(currentFocus?.windowToken, InputMethodManager.SHOW_FORCED)
                if (currentFocus != null) {
                    ViewUtils.hideKeyboard(currentFocus!!, this@ScanImageTab)
                }
                when (position) {
                    0 -> fillList0()
                    1 -> fillList1()
                }
            }
        })
        if (!ConstantsLocal.isTranslateTextEnabled) {
            //mTabsViewpager.currentItem
            (mTabLayout.getTabAt(2) as LinearLayout).visibility = View.GONE
        }
    }

    override fun onFragmentAttach(link: String?) {
        if (link != null){
            when (link){
                "f0" -> fillList0()
                "f1" -> fillList1()
            }
        }
    }

    //endregion

    //region region 11 - fill list 0, 1 and 2

    fun fillList0() {
        if (this::mViewMain0.isInitialized) return
        if (findViewById<LinearLayout>(R.id.viewMain0) == null) return
        mViewMain0 = findViewById(R.id.viewMain0)

        mCropImageView = findViewById(R.id.cropImageView)
        mTabLayoutExt = findViewById(R.id.tab_layoutext)

        mBtnScan = findViewById(R.id.btnScan)
        mBtnScan.isEnabled = false
        mBtnScan.setOnClickListener {
            progressIndicator.show()
            analyzeImage()
        }

        btnPhoto = findViewById(R.id.btnPhoto)
        btnPhoto.setOnClickListener {
            selectFile(null)
        }
        if (intent.hasExtra("filename")){
            mFilename = intent.getStringExtra("filename")!!
            openSource(Sourcex.OCR)
        }
    }
    private fun selectFile(savedInstanceState: Bundle?){
        val bundle = intent.getBundleExtra(CropImage.CROP_IMAGE_EXTRA_BUNDLE)
        cropImageUri = bundle?.parcelable(CropImage.CROP_IMAGE_EXTRA_SOURCE)
        cropImageOptions =
            bundle?.parcelable(CropImage.CROP_IMAGE_EXTRA_OPTIONS) ?: CropImageOptions()

        if (savedInstanceState == null) {
            if (cropImageUri == null || cropImageUri == Uri.EMPTY) {
                when {
                    cropImageOptions.showIntentChooser -> showIntentChooser()
                    cropImageOptions.imageSourceIncludeGallery &&
                            cropImageOptions.imageSourceIncludeCamera ->
                        showImageSourceDialog(::openSource)
                    cropImageOptions.imageSourceIncludeGallery ->
                        pickImageGallery.launch("image/*")
                    cropImageOptions.imageSourceIncludeCamera ->
                        openCamera()
                    else -> finish()
                }
            } else {
                mCropImageView.setImageUriAsync(cropImageUri)
            }
        } else {
            latestTmpUri = savedInstanceState.getString("bundle_key_tmp_uri")?.toUri()
        }
    }

    open fun showImageSourceDialog(openSource: (Sourcex) -> Unit) {
        AlertDialog.Builder(this)
            .setCancelable(false)
            .setOnKeyListener { _, keyCode, keyEvent ->
                if (keyCode == KeyEvent.KEYCODE_BACK && keyEvent.action == KeyEvent.ACTION_UP) {
                    setResult(RESULT_CANCELED)
                    finish()
                }
                true
            }
            .setTitle(com.farrusco.projectclasses.R.string.pick_image_chooser_title)
            .setItems(
                if (mFilename == "") {
                    arrayOf(
                        getString(com.farrusco.projectclasses.R.string.pick_image_camera),
                        getString(com.farrusco.projectclasses.R.string.pick_image_gallery),
                    )
                } else {
                    arrayOf(
                        getString(com.farrusco.projectclasses.R.string.pick_image_camera),
                        getString(com.farrusco.projectclasses.R.string.pick_image_gallery),
                        mFilename,
                    )
                },
            ) { _, position -> openSource(
                    when(position){
                        0 -> Sourcex.CAMERA
                        1 -> Sourcex.GALLERY
                        else -> Sourcex.OCR
                    }
                ) }
            .show()
    }
    private fun openSource(source: Sourcex) {
        when (source) {
            Sourcex.CAMERA -> openCamera()
            Sourcex.GALLERY -> pickImageGallery.launch("image/*")
            Sourcex.OCR -> {
                cropImageUri = Uri.fromFile(File(mFilename))
                mCropImageView.setImageUriAsync(cropImageUri)
                mBtnScan.isEnabled = true
            }
        }
    }

    private fun openCamera() {
        val intent = Intent(this, CameraActivity::class.java)
        resultLauncher.launch(intent)
    }

    private fun showIntentChooser() {
        val ciIntentChooser = CropImageIntentChooser(
            activity = this,
            callback = object : CropImageIntentChooser.ResultCallback {
                override fun onSuccess(uri: Uri?) {
                    onPickImageResult(uri)
                }

                override fun onCancelled() {
                    setResult(RESULT_CANCELED)
                    finish()
                }
            },
        )
        cropImageOptions.let { options ->
            options.intentChooserTitle
                ?.takeIf { title ->
                    title.isNotBlank()
                }
                ?.let { icTitle ->
                    ciIntentChooser.setIntentChooserTitle(icTitle)
                }
            options.intentChooserPriorityList
                ?.takeIf { appPriorityList -> appPriorityList.isNotEmpty() }
                ?.let { appsList ->
                    ciIntentChooser.setupPriorityAppsList(appsList)
                }
            val cameraUri: Uri? = if (options.imageSourceIncludeCamera) getTmpFileUri() else null
            ciIntentChooser.showChooserIntent(
                includeCamera = options.imageSourceIncludeCamera,
                includeGallery = options.imageSourceIncludeGallery,
                cameraImgUri = cameraUri,
            )
        }
    }

    private fun getTmpFileUri(): Uri {
        val tmpFile = File.createTempFile("tmp_image_file", ".png", cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }

        return tmpFile.toUri()
    }

    fun fillList1() {
        if (this::mViewMain1.isInitialized) return
        if (findViewById<LinearLayout>(R.id.viewMain1) == null) return
        mViewMain1 = findViewById(R.id.viewMain1)

        mOms1 = findViewById(R.id.txtOms1)
        mOms1.skipEditTagAlways = true
        mOms2 = findViewById(R.id.txtOms2)
        mOms2.skipEditTagAlways = true

        mSpTranslateFrom = findViewById(R.id.sp_translatefrom)
        mSpTranslateFrom.skipEditTagAlways = true
        mSpTranslateFrom.fillSpinner(translations.countries)

        mSpTranslateTo = findViewById(R.id.sp_translateto)
        mSpTranslateTo.skipEditTagAlways = true
        mSpTranslateTo.fillSpinner(translations.countries)

        btnTranslate1 = findViewById(R.id.image_arrow_down)
        btnTranslate2 = findViewById(R.id.image_arrow_up)

        mSpTranslateFrom.setItemTextSelected(translations.dfltLanguageFrom)
        mSpTranslateTo.setItemTextSelected(translations.dfltLanguageTo)

        btnTranslate1.setOnClickListener {
            var mFrom = Constants.languageCountry[mSpTranslateFrom.selectedItemPosition].first
            var mTo = Constants.languageCountry[mSpTranslateTo.selectedItemPosition].first
            if (mFrom == "auto"){
                mFrom = Locale.getDefault().language
            }
            if (mTo == "auto"){
                mTo = Locale.getDefault().language
            }
            if (currentFocus != null){
                ViewUtils.hideKeyboard(currentFocus!!, this)
            }
            translations.translateText(mFrom, mTo, mOms1, mOms2,1 )
        }
        btnTranslate2.setOnClickListener {
            var mTo = Constants.languageCountry[mSpTranslateFrom.selectedItemPosition].first
            var mFrom = Constants.languageCountry[mSpTranslateTo.selectedItemPosition].first
            if (mFrom == "auto"){
                mFrom = Locale.getDefault().language
            }
            if (mTo == "auto"){
                mTo = Locale.getDefault().language
            }
            if (currentFocus != null){
                ViewUtils.hideKeyboard(currentFocus!!, this)
            }
            translations.translateText(mFrom, mTo, mOms2, mOms1,2 )
        }

        mBtnSave1 = findViewById(R.id.imagecopy_up)
        mBtnSave1.setOnClickListener {
            translations.copyClipboard(mOms1)
        }
        mBtnSave1.setOnLongClickListener {
            translations.clearOms(mOms1)
            true
        }
        mBtnSave2 = findViewById(R.id.imagecopy_down)
        mBtnSave2.setOnClickListener {
            translations.copyClipboard(mOms2)
        }
        mBtnSave2.setOnLongClickListener {
            translations.clearOms(mOms2)
            true
        }
    }

    //endregion
    override fun resultActivity(result: ActivityResult) {
        if (result.resultCode == RESULT_OK) {
            val file = result.data?.getStringExtra(Constants.ARG_KEY_URI)
            if (file != null) {
                onPickImageResult(File(file).toUri())
            }
        }
    }
    fun onPickImageResult(resultUri: Uri?) {
        if (resultUri != null) {
            mTabsViewpager.currentItem = 0
            if (!::mCropImageView.isInitialized){
                mCropImageView = findViewById(R.id.cropImageView)
            }
            mCropImageView.setImageUriAsync(resultUri)
            mBtnScan.isEnabled = true
        } else {
            mBtnScan.isEnabled = false
        }
    }

    //@OptIn(DelicateCoroutinesApi::class)
    private fun analyzeImage() {
        mTabsViewpager.currentItem = 1
        val img = mCropImageView.getCroppedImage()
        val dir = File(Constants.resultsPath)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val file = File.createTempFile("cropped", ".png", dir)

        FileOutputStream(file).use { out ->
            img?.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        @Suppress("OPT_IN_USAGE")
        GlobalScope.launch(Dispatchers.IO) {
            TextAnalyser({ scanResult ->
                if (scanResult.isEmpty()) {
                    progressIndicator.dismiss()
                    Toast.makeText(
                        this@ScanImageTab,
                        getString(R.string.all_txt_no_text_detected),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    progressIndicator.dismiss()
                    mOms1.textExt = ""
                    var bPrefix = true
                    scanResult.forEach {
                        if (it == '\n' && bPrefix){
                            // lazy
                        } else {
                            bPrefix=false
                            mOms1.textExt +=it
                        }
                    }
                    progressIndicator.dismiss()
                }
            }, this@ScanImageTab, Uri.fromFile(file)).analyseImage()
        }
    }

    public override fun onDestroy() {
        val dir = File(cacheDir.path)
        if (dir.exists()) {
            dir.deleteRecursively()
        }
        translations.closeTranslate()
        super.onDestroy()
    }
    enum class Sourcex { CAMERA, GALLERY, OCR }

}