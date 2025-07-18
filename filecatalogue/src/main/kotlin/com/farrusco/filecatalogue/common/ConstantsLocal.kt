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

import android.content.ContentValues
import com.farrusco.filecatalogue.tables.TableCategory
import com.farrusco.projectclasses.utils.StringUtils

object ConstantsLocal {
    const val TYPE_ALL = 0
    const val TYPE_CATEGORY = 1
    const val TYPE_DIRECTORY = 2
    const val TYPE_FILE = 3
    const val TYPE_CREATION = 4
    const val TYPE_AUTOMATED_NEW = 5
    const val TYPE_GPS = 6
    const val TYPE_SEARCH = 7
    const val TYPE_FILE_EXTENSION = 8
    const val TYPE_FACE = 9
    const val TYPE_TRANSLATE = 10

    var dateFormat = ""
    var isPriceUseEnabled = false
    var isSmartCopyCategory = false
    var isOrderEnabled = false

    //var isTcpEnabled = false
    var isReorgEnabled = false
    var isAutoNewEnabled = false
    var isGbWhatsAppEnabled = false
    var isGPSEnabled = false
    var isFileExtensionEnabled = false
    var registration: String? = null
    var isBackdoorOpen = false
    var isAutoRefreshFolder = false
    var isStoreLastQuery = false
    var isStoreQuery = false
    var isScanImageEnabled = false
    var isTranslateTextEnabled = false
    var isScannerEnabled = false
    var isFileFancyScrollEnabled = false
    var dbVersion: Int = -1
    var createMainId: Int = -1
    var extensionMainId: Int = -1
    var createNewId: Int = -1
    var deleteFilePhone = false
    var isSingleListLabelEnabled = true
    var isSearchFilesAvailableEnabled = true
    var isSearchFilesHiddenEnabled = true
    var sortCreationYearDesc = true
    var sortCreationMonthDesc = true
    var storeFileSize = true
    var fileSizeMB = true
    var isFaceRecognitionEnabled = true

    var initProductRel = false
    var arrProductRel: ArrayList<ProductRelLine> = ArrayList()
}

class ProductRelLine {
    var id: Int = 0
    var category: String = ""
}

class CategoryLine {
    var id: Int = 0
    var title: String = ""
    var mainid: Int = 0
    var level: Int = 0
    var seqno: Int = 0
    var type: Int = 0
    var count: Int = 0
    var countCurr: Int = 0
    var checked: Int = 0
    var checkedCurr: Int = 0
    var hasChild: Boolean = false
    val levelTitle: String
        get() {
            return StringUtils.repeat("./", level - 1) + title
        }

    fun setMap(map: ContentValues) {
        if (map.containsKey(TableCategory.Columns._id.name)) id =
            map.getAsInteger(TableCategory.Columns._id.name)
        if (map.containsKey(TableCategory.Columns.title.name)) title =
            map.getAsString(TableCategory.Columns.title.name)
        if (map.containsKey(TableCategory.Columns._mainid.name)) mainid =
            map.getAsInteger(TableCategory.Columns._mainid.name)
        if (map.containsKey(TableCategory.Columns.level.name)) level =
            map.getAsInteger(TableCategory.Columns.level.name)
        if (map.containsKey(TableCategory.Columns.seqno.name)) seqno =
            map.getAsInteger(TableCategory.Columns.seqno.name)
        if (map.containsKey(TableCategory.Columns.type.name)) type =
            map.getAsInteger(TableCategory.Columns.type.name)
        if (map.containsKey("count")) count = map.getAsInteger("count")
        if (map.containsKey("countcurr")) count = map.getAsInteger("countcurr")
        if (map.containsKey("checked")) checked = map.getAsInteger("checked")
        if (map.containsKey("checkedcurr")) checked = map.getAsInteger("checkedcurr")
        if (map.containsKey("haschild")) hasChild = map.getAsBoolean("haschild")
    }
}

class CategoryDetail {
    var categoryid: Int = 0
    var level: Int = 0
    var type: Int = 0
    var checked: Int = 0
    var checkedOld: Int = 0
}

enum class SystemRecordType(val internal: Int) {
    Available(3)
    //,system(5)
}

enum class SystemMainAttr(val internal: Int) {
    //Super(1),
    System(2),
    Category(3),
    Search(4),
    ListFiles(5),
    File(6),
    Order(7),
}

enum class SystemAttr(val internal: Int) {
    Version(1),
    BackupFolder(2),
    FileExtension(3),
    CvsName(4),
    About(5),
    Help(6),
    Tooltip(7),
    Orders(8),
    Price(9),
    GbWhatsApp(10),
    AutoNew(11),
    ReorgDb(12),
    Listfiles(13),
    LastPath(14),
    SalesEMail(15),
    SalesName(16),
    SalesAddress(17),
    StoreLastQuery(18),
    GPSInformation(19),
    DeleteEmptyProduct(20),
    Backups(21),
    DeleteFilePhone(22),
    SmartCopyCategory(23),
    Registration(24),
    StoreQuery(25),
    AutoRefreshFolder(26),
    ScanImage(27),
    TranslateText(28),
    ScanQRCode(29),
    SingleListLabel(30),
    SearchFilesAvailable(31),
    SearchFilesHidden(32),
    SortCreationYearDesc(33),
    SortCreationMonthDesc(34),
    StoreFileSize(35),
    FileSizeMb(36),
    OrderSeqnoDirectory(37),
    OrderSeqnoCreation(38),
    OrderSeqnoExtension(39),
    OrderSeqnoGPS(40),
    FileFancyScroll(41),
    OrderFace(42),
    FaceRecognition(43),
    FaceDistance(44),
}