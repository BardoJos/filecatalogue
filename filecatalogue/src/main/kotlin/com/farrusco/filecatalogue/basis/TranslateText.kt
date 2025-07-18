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

import android.content.ClipboardManager
import android.widget.ImageView
import com.farrusco.filecatalogue.R
import com.farrusco.filecatalogue.business.Help
import com.farrusco.filecatalogue.utils.Translations
import com.farrusco.projectclasses.activity.BaseActivityTableLayout
import com.farrusco.projectclasses.common.Constants
import com.farrusco.projectclasses.utils.ViewUtils
import com.farrusco.projectclasses.widget.ButtonExt
import com.farrusco.projectclasses.widget.EditTextExt
import com.farrusco.projectclasses.widget.SpinnerExt
import java.util.Locale

class TranslateText: BaseActivityTableLayout() {
    override fun initTableLayout() {
        TODO("Not yet implemented")
    }

    override val layoutResourceId: Int = R.layout.activity_translatetext
    override val mainViewId: Int = R.id.viewMain

    private lateinit var mOms1: EditTextExt
    private lateinit var mOms2: EditTextExt
    private lateinit var mBtnSave1: ButtonExt
    private lateinit var mBtnSave2: ButtonExt
    private lateinit var btnTranslate1: ImageView
    private lateinit var btnTranslate2: ImageView
    private lateinit var mSpTranslateFrom: SpinnerExt
    private lateinit var mSpTranslateTo: SpinnerExt

    private lateinit var clipboardManager: ClipboardManager
    private lateinit var translations: Translations

    override fun initActivity(){
        helpText = Help().getHelpTitle(className)
        translations = Translations(this,layoutInflater)

        clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        fillList()
    }

    fun fillList() {

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

    public override fun onDestroy() {
        translations.closeTranslate()
        super.onDestroy()
    }

}
