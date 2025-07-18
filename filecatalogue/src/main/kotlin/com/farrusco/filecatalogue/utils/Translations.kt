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
package com.farrusco.filecatalogue.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.farrusco.filecatalogue.R
import com.farrusco.filecatalogue.business.Category
import com.farrusco.filecatalogue.common.ConstantsLocal
import com.farrusco.projectclasses.common.Constants
import com.farrusco.projectclasses.messages.IToastExt
import com.farrusco.projectclasses.messages.Mess
import com.farrusco.projectclasses.messages.ProgressIndicator
import com.farrusco.projectclasses.messages.ToastExt
import com.farrusco.projectclasses.utils.Logging
import com.farrusco.projectclasses.utils.TranslateText
import com.farrusco.projectclasses.widget.EditTextExt
import java.util.Locale

class Translations(private var context: Context, layoutInflater: LayoutInflater): IToastExt {

    private var category: Category
    private lateinit var translatorx: TranslateText
    private var clipboardManager: ClipboardManager
    private var progressIndicator: ProgressIndicator
    private var layoutInflater: LayoutInflater
    var countries = arrayListOf<String>()
    var dfltLanguageFrom = ""
    var dfltLanguageTo = ""

    init{
        this.layoutInflater = layoutInflater
        this.progressIndicator = ProgressIndicator(context, false)
        category = Category(context)
        clipboardManager = context.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
        getCountries()
    }
    override fun getContext(): Context? {
        return this.context
    }
    fun translateText(fromLanguage: String, toLanguage: String, oms1: EditTextExt, oms2: EditTextExt, tab:Int ) {
        progressIndicator.show()
        if (tab == 1) {
            category.saveTypeSeqno(ConstantsLocal.TYPE_TRANSLATE, 99, "","$fromLanguage,$toLanguage")
        } else {
            category.saveTypeSeqno(ConstantsLocal.TYPE_TRANSLATE, 99, "","$toLanguage,$fromLanguage")
        }
        if (oms1.textExt == "") {
            progressIndicator.dismiss()
            ToastExt().makeText(
                context,R.string.mess039_notnull, Toast.LENGTH_LONG
            ).show()
            return
        }
        if (::translatorx.isInitialized) {
            translatorx.close()
        }

        translatorx = TranslateText(fromLanguage, toLanguage)
        translatorx.downloadModel {
            when (it) {
                true -> {
                    translatorx.translateText(
                        oms1.textExt!!,
                        object : TranslateText.ResultCallback {
                            override fun onSuccess(text: String) {
                                oms2.textExt = text
                                val clip = ClipData.newPlainText("Copied", oms2.textExt)
                                clipboardManager.setPrimaryClip(clip)
                                progressIndicator.dismiss()

                            }
                            override fun onCancelled(mess: String) {
                                progressIndicator.dismiss()
                                Logging.e("processImageBlocks: $mess")
                                showMessage( R.string.mess071_canceltranslation)
                            }
                        })
                }
                false -> {
                    translatorx.close()
                    progressIndicator.dismiss()
                    showMessage( R.string.mess070_notranslation)
                }
            }
        }
    }
    private fun getCountries(){
        var languages = ""
        val rtn = category.loadTypeSeqno(ConstantsLocal.TYPE_TRANSLATE, 99)
        if (rtn.returnValue && rtn.mess.isNotEmpty() && rtn.mess.split(",").count() == 2) {
            languages = rtn.mess
        }
        countries = arrayListOf()
        val languageTo:String
        val languageFrom: String
        if (languages.isNotEmpty()) {
            languageFrom = languages.split(",")[0]
            languageTo = languages.split(",")[1]
        } else {
            languageFrom = "auto"
            languageTo = Locale.getDefault().language
        }
        Constants.languageCountry.sortBy { it.second }
        Constants.languageCountry.forEach {
            countries.add(it.second)
            if (languageTo.equals(it.first,ignoreCase = true)){
                dfltLanguageTo = it.second
            }
            if (languageFrom.equals(it.first,ignoreCase = true)){
                dfltLanguageFrom = it.second
            }
        }
    }

    fun copyClipboard(oms: EditTextExt){
        if (oms.textExt != "") {
            val clip = ClipData.newPlainText("Copied", oms.textExt)
            clipboardManager.setPrimaryClip(clip)
            ToastExt().makeText(
                context,R.string.mess072_all_txt_copy_to_clipboard, Toast.LENGTH_LONG
            ).show()
        } else {
            ToastExt().makeText(
                context,R.string.mess039_notnull, Toast.LENGTH_LONG
            ).show()
        }
    }
    fun clearOms(oms: EditTextExt) {
        if ( oms.textExt != "") {
            val alertDialog = Mess.buildAlertDialog(context,layoutInflater,
                context.getString( R.string.clearing),
                context.getString( R.string.mess073_clearyn))
            with(alertDialog){
                setPositiveButton(
                    com.farrusco.projectclasses.R.string.yes
                ) { _, _ ->
                    oms.textExt = ""
                }
                setNeutralButton(
                    com.farrusco.projectclasses.R.string.no
                ) { dialog, _ ->
                    dialog.cancel()
                }
                show()
            }
        }
    }

    fun closeTranslate() {
        if (::translatorx.isInitialized){
            translatorx.close()
        }
    }
}