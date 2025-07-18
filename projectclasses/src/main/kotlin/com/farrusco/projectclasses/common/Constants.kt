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
package com.farrusco.projectclasses.common

import android.content.ContentResolver
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Rect
import android.icu.text.DecimalFormatSymbols
import android.net.Uri
import android.webkit.MimeTypeMap
import android.widget.ImageView
import androidx.collection.ArrayMap
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import com.farrusco.projectclasses.BuildConfig
import com.farrusco.projectclasses.R
import com.farrusco.projectclasses.databases.tables.DBStructure
import java.io.File

object Constants {

    const val RequestCode = "requestcode"
    private var basePath: String = ""
    val dataPath: String
        get() = "$basePath/Data"
    val bitmapPath: String
        get() = "$basePath/Bitmap"
    val mlkitPath: String
        get() = "$basePath/image_manager_disk_cache"
    val pdfPath: String
        get() = "$basePath/downloaded_pdf.pdf"
    val pdfCache: String
        get() = "$basePath/___pdf___cache___"
    val resultsPath: String
        get() = "$basePath/Results"

    var isDebuggable: Boolean = BuildConfig.DEBUG
    var isHelpEnabled: Boolean = false
    var isTooltipEnabled: Boolean = false
    var APP_NAME: String? = null

    lateinit var db: net.sqlcipher.database.SQLiteDatabase
    var DATABASE_NAME = "dbname"
    var DATABASE_PW = ""
    var helpTableName = "help"
    var DATABASE_VERSION = 1
    var limit = 0
    var ChangeLog = 0
    var SqlSystemId = 0
    var SqlReorgId = 0
    var SqlSampleDataId = 0
    var SqlSampleBackupId = 0
    var SqlDbStructureId = 0
    var SqlFileExtensionDataId = 0
    //var localFragmentActivity: FragmentActivity? = null
    var localFragmentManager: FragmentManager? = null
    var localLifecycle: Lifecycle? = null

    var decimalSeparator = DecimalFormatSymbols.getInstance().decimalSeparator.toString()
    var dbStructure = ArrayMap<String, DBStructure>()

    // window dimensions
    lateinit var currentBounds: Rect
    var orientationActivity = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT

    fun isDbInitialized(): Boolean {
        return ::db.isInitialized
    }

    fun setBasePath(context: Context){
        basePath = context.externalCacheDir!!.absolutePath
    }

    fun closeSQLiteDatabase() {
        if (::db.isInitialized) {
            db.close()
        }
    }

    // Image
    const val RATIO_4_3_VALUE = 4.0 / 3.0
    const val RATIO_16_9_VALUE = 16.0 / 9.0
    // Navigation
    const val ARG_KEY_URI = "uri"
    //const val ARG_KEY_TEXT = "text"

    /*    // Image

        // File
        const val IMG_FILE_EXT = ".png"
        const val IMG_FILE_DATE_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        const val FILE_DIR_CHILD = "OCR"


        // Firebase Firestore
        const val COLLECTION_NAME = "ocr"
        const val DOC_TITLE_FIELD_KEY = "title"
        const val DOC_BODY_FIELD_KEY = "body"*/

    class SpinnerDetail {
        var id: Int = 0
        var description: String = ""
        var hiddenValues: ArrayList<String>? = null
    }

    var catagoryType = arrayOf(
        "1" to "Line",
        "2" to "Date",
        "3" to "Spinner",
        "4" to "Text",
    )

    var languageCountry = arrayOf(
        "auto" to "Auto",
        "af" to "Afrikaans",
        "ar" to "Arabic",
        "be" to "Belarusian",
        "bg" to "Bulgarian",
        "bn" to "Bengali",
        "ca" to "Catalan",
        "cs" to "Czech",
        "cy" to "Welsh",
        "da" to "Danish",
        "de" to "German",
        "el" to "Greek",
        "en" to "English",
        "eo" to "Esperanto",
        "es" to "Spanish",
        "et" to "Estonian",
        "fa" to "Persian",
        "fi" to "Finnish",
        "fr" to "French",
        "ga" to "Irish",
        "gl" to "Galician",
        "gu" to "Gujarati",
        "he" to "Hebrew",
        "hi" to "Hindi",
        "hr" to "Croatian",
        "ht" to "Haitian",
        "hu" to "Hungarian",
        "id" to "Indonesian",
        "is" to "Icelandic",
        "it" to "Italian",
        "ja" to "Japanese",
        "ka" to "Georgian",
        "kn" to "Kannada",
        "ko" to "Korean",
        "lt" to "Lithuanian",
        "lv" to "Latvian",
        "mk" to "Macedonian",
        "mr" to "Marathi",
        "ms" to "Malay",
        "mt" to "Maltese",
        "nl" to "Dutch",
        "no" to "Norwegian",
        "pl" to "Polish",
        "pt" to "Portuguese",
        "ro" to "Romanian",
        "ru" to "Russian",
        "sk" to "Slovak",
        "sl" to "Slovenian",
        "sq" to "Albanian",
        "sv" to "Swedish",
        "sw" to "Swahili",
        "ta" to "Tamil",
        "te" to "Telugu",
        "th" to "Thai",
        "tl" to "Tagalog",
        "tr" to "Turkish",
        "uk" to "Ukrainian",
        "ur" to "Urdu",
        "vi" to "Vietnamese",
        "zh" to "Chinese"
    )

}

internal data class ZoomVariables(
    var scale: Float,
    var focusX: Float,
    var focusY: Float,
    var scaleType:
    ImageView.ScaleType?
)

class extensionApplication(context: Context, path: String) {
    var resourceIcon: Int = 0
    var resourceIconEmpty: Int = 0
    var resourceIconEmptyEdge: Int = 0
    var application: String = ""

    private fun getFileType(path: String, context: Context): String {
        val file = File(path)
        val fileUri: Uri = Uri.fromFile(file)
        val mimeType = if (ContentResolver.SCHEME_CONTENT == fileUri.scheme) {
            context.contentResolver.getType(fileUri)
        } else {
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension.lowercase())
        }
        if (mimeType == null) return ""
        return mimeType
    }

    init {
        if (File(path).exists()) {
            val extension = File(path).extension
            application = getFileType(path, context)

            if (extension.isEmpty()) {
                resourceIcon = R.drawable.file_question
                resourceIconEmpty = R.drawable.file_question_empty
            } else if (extension.startsWith("doc", true)) {
                resourceIcon = R.drawable.file_word
                resourceIconEmpty = R.drawable.file_word_empty
                //application = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            } else if (extension.startsWith("xls", true)) {
                resourceIcon = R.drawable.file_excel
                resourceIconEmpty = R.drawable.file_excel_empty
                //application = "application/vnd.ms-excel"
            } else if (extension.startsWith("ppt", true)) {
                resourceIcon = R.drawable.file_ppt
                resourceIconEmpty = R.drawable.file_ppt_empty
            } else {
                when (extension.lowercase()) {
                    "gif", "jpeg", "jpg", "raw", "png", "webp", "bmp", "mp4" -> {
                        //resourceIcon = 0
                        //resourceIconEmpty = 0
                    }

                    "csv" -> {
                        resourceIcon = R.drawable.file_csv
                        resourceIconEmpty = R.drawable.file_csv_empty
                    }

                    "bak" -> {
                        resourceIcon = R.drawable.file_bak
                        resourceIconEmpty = R.drawable.file_bak_empty
                    }

                    "wav", "mp3" -> {
                        resourceIcon = R.drawable.file_audio
                        resourceIconEmpty = R.drawable.file_audio_empty
                        //application = "audio/x-wav"
                    }

                    "zip", "rar", "7z" -> {
                        resourceIcon = R.drawable.file_zip
                        resourceIconEmpty = R.drawable.file_zip_empty
                        //application = "application/zip"
                    }

                    "pdf" -> {
                        resourceIcon = R.drawable.file_pdf
                        resourceIconEmpty = R.drawable.file_pdf_empty
                        resourceIconEmptyEdge = 10
                        //application = "application/pdf"
                    }

                    "rtf" -> {
                        resourceIcon = R.drawable.file_rtf
                        resourceIconEmpty = R.drawable.file_rtf_empty
                        //application = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                    }

                    "txt" -> {
                        resourceIcon = R.drawable.file_txt
                        resourceIconEmpty = R.drawable.file_txt_empty
                        resourceIconEmptyEdge = 10
                        //application = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                    }

                    else -> {
                        resourceIcon = R.drawable.file_question
                        resourceIconEmpty = R.drawable.file_question_empty
                    }
                }
            }
        } else {
            application = ""
        }
    }
}