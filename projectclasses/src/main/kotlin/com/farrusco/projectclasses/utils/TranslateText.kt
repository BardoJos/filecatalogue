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

package com.farrusco.projectclasses.utils

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions

class TranslateText (fromLanguage: String, toLanguage: String) {

    interface ResultCallback {
        fun onSuccess(text: String)
        fun onCancelled(mess: String)
    }

    private var translator: Translator
    init {
        // Create an  translator:
        TranslateLanguage.getAllLanguages()
        val options: TranslatorOptions = TranslatorOptions.Builder()
            .setSourceLanguage(fromLanguage)
            .setTargetLanguage(toLanguage)
            .build()
        translator = Translation.getClient(options)

        val modelManager = RemoteModelManager.getInstance()
        modelManager.getDownloadedModels(TranslateRemoteModel::class.java)
            .addOnSuccessListener { _ ->
                // ...
            }
            .addOnFailureListener {
                // Error.
            }

    }
    fun downloadModel(downloadOnFinish: (Boolean) -> Unit) {
/*        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()*/
        val conditions = DownloadConditions.Builder()
            //.requireWifi()
            .build()

        translator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                downloadOnFinish(true)
            }
            .addOnFailureListener { exception ->
                Logging.e("downloadModel: ${exception.message}")
                downloadOnFinish(false)
            }
    }
    fun translateText( text: String, callback: ResultCallback ) {
        val rtn = ReturnValue()

        // Model downloaded successfully. Okay to start translating.
        // (Set a flag, unHide the translation UI, etc.)
        translator.translate(text)
            .addOnSuccessListener { translatedText ->
                // Translation successful.
                //rtn.mess = translatedText
                callback.onSuccess(translatedText)
            }
            .addOnFailureListener { exception ->
                // Model couldn’t be downloaded or other internal error.
                rtn.mess = exception.message.toString()
                rtn.returnValue = false
                callback.onCancelled(exception.message.toString())
            }
        //activity.getLifecycle().addObserver(translator)


    }
    fun close() {
        translator.close()
    }
}