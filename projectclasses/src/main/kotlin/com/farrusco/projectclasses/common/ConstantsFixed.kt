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

import android.graphics.Color
import android.view.Menu

class ConstantsFixed private constructor() {
    init {
        throw RuntimeException("Not allowed instances")
    }

    companion object {

        const val TCPIP_ENABLED = "tcpip_enabled"
        const val CRYPTION_SECRET_KEY = "cryption_secret"
        const val CRYPTION_MASTER_KEY = "cryption_master"
        const val CRYPTION_IP = "cryption_ip"
        const val CRYPTION_PORT = "cryption_port"
        const val STRING_TRUE = "-1"
        const val STRING_FALSE = "0"
        const val popupmenu = "popupmenu"
        const val ignore = "ignore"
        const val SHARED_PREF_IS_DATA_STORED_KEY = "is_data_stored"
        const val SERIALIZED_DATA_FILENAME = "image_data"

        //const val TagActionError = "Error"

        const val SAMPLE_ID = Menu.FIRST + 1
        const val ABOUT_ID = Menu.FIRST + 2
        const val SITE_ID = Menu.FIRST + 3
        const val LOG_ID = Menu.FIRST + 4
        const val OCR_ID = Menu.FIRST + 5
        const val QR_ID = Menu.FIRST + 6

        const val SAVE_ID = Menu.FIRST + 100
        const val ADD_ID = Menu.FIRST + 101
        const val EDIT_ID = Menu.FIRST + 102
        const val DELETE_ID = Menu.FIRST + 103
        const val BROWSE_ID = Menu.FIRST + 104
        const val HELP_ID = Menu.FIRST + 105
        const val MAIL_ID = Menu.FIRST + 106
        const val PRINT_ID = Menu.FIRST + 107
        const val TCPID_ID = Menu.FIRST + 108
        const val WHATSAPP_ID = Menu.FIRST + 109
        const val ROTATERIGHT_ID = Menu.FIRST + 110
        const val ROTATELEFT_ID = Menu.FIRST + 111
        const val GPSINFO_ID = Menu.FIRST + 114
        const val REFRESH_ID = Menu.FIRST + 115
        const val OPEN_APP_ID = Menu.FIRST + 116
        const val OPEN_APP_WITH_ID = Menu.FIRST + 117
        const val LAST_ID = Menu.FIRST + 118

        var tagSections: String = ""
            get(){
                if (field.isEmpty()){
                    TagSection.entries.forEach {
                        field += "$it,"
                    }
                    field = "," + field.lowercase()
                }
                return field
            }

    }

    enum class TagSection {
        TsForeBack,
        TsDBColumn,
        TsDBTable,
        TsMessColumn,
        TsDBColumnBack,
        TsDBTableBack,
        TsDBValue,
        TsGroupno,
        TsModFlag,
        TsUserFlag,
        TsLineId
        //ErrorFlag
    }

    //var x = ConstantsFixed.ColorBasic.ColorEdit.color
    enum class ColorBasic(val color: Int) {
        Default(Color.WHITE),
        Edit(Color.YELLOW), // used by switch
        Modified(Color.BLUE),
        Collapse(Color.RED),
        Text(Color.WHITE),
        Dark(Color.BLACK),
    }

    enum class TagAction {
        Browse,
        New,
        Insert,
        Delete,
        Edit
    }

    enum class ScreenModi {
        ModeNew, ModeInsert, ModeEdit, ModeBrowse, ModeDelete
    }

    enum class FixedPixel {
        CENTER, TOP_LEFT, BOTTOM_RIGHT
    }

    internal enum class ImageActionState {
        NONE, DRAG, ZOOM, FLING, ANIMATE_ZOOM
    }

    enum class PdfEngine(val value: Int) {
        INTERNAL(100),
        GOOGLE(200)
    }

    enum class PdfQuality(val ratio: Int) {
        FAST(1),
        NORMAL(2),
        ENHANCED(3)
    }
}