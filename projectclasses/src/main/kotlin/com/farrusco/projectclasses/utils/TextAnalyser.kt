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

import android.content.Context
import android.net.Uri
import com.farrusco.projectclasses.BuildConfig
import com.farrusco.projectclasses.messages.IToastExt
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

typealias CameraTextAnalyzerListener = (text: String) -> Unit

class TextAnalyser (
    private val textListener: CameraTextAnalyzerListener,
    private var context: Context,
    private val fromFile: Uri,
): IToastExt {
    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    fun analyseImage() {
        try {
            val img = InputImage.fromFilePath(context, fromFile)
            recognizer.process(img)
                .addOnSuccessListener { visionText ->
                    Logging.d(visionText.text)
                    textListener(visionText.text)
                }
                .addOnFailureListener { e ->
                    Logging.e(e.localizedMessage)
                    if(BuildConfig.DEBUG){
                        textListener(e.localizedMessage!!)
                    } else {
                        showMessage( e.localizedMessage!!)
                    }

                }
        } catch (e: Exception) {
            Logging.e(e.localizedMessage)
            showMessage( e.localizedMessage!!)
        }
    }

    override fun getContext(): Context? {
        return context
    }
}