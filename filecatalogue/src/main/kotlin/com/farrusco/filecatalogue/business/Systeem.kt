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
package com.farrusco.filecatalogue.business

import android.content.ContentValues
import android.content.Context
import com.farrusco.filecatalogue.BuildConfig
import com.farrusco.filecatalogue.common.ConstantsLocal
import com.farrusco.filecatalogue.common.SystemAttr
import com.farrusco.filecatalogue.tables.TableCategory
import com.farrusco.filecatalogue.tables.TableFace
import com.farrusco.filecatalogue.tables.TableHelp
import com.farrusco.filecatalogue.tables.TableOrder
import com.farrusco.filecatalogue.tables.TableOrderLine
import com.farrusco.filecatalogue.tables.TableProduct
import com.farrusco.filecatalogue.tables.TableProductRel
import com.farrusco.filecatalogue.tables.TableSysteem
import com.farrusco.projectclasses.common.Constants
import com.farrusco.projectclasses.databases.DatabaseHelper
import com.farrusco.projectclasses.databases.tables.DBUtils
import com.farrusco.projectclasses.utils.DeviceInfoUtils
import com.farrusco.projectclasses.utils.ReturnValue
import java.util.Calendar

class Systeem(val context: Context): TableSysteem(){

    fun getValue(key: SystemAttr): String {
        return super.getValue(key.internal)
    }

    fun setValue(key: SystemAttr, value: String?, type: Int): ReturnValue{
        return super.setValue(key.internal,
            key.name,
            value,
            type
        )
    }

    fun getProperties(){
        val rtn = getSysteem(null)
        ConstantsLocal.isAutoNewEnabled = true
        if (rtn.cursor.moveToFirst()){
            do {
                val map = rtn.cursorToMap()
                if (map.getAsInteger(Columns._mainid.name) > 0){
                    when (map.getAsInteger(Columns.systeemkey.name)) {
                        SystemAttr.Version.internal -> ConstantsLocal.dbVersion =
                            map.getAsInteger(Columns.systeemvalue.name)

                        SystemAttr.Registration.internal -> ConstantsLocal.registration =
                            map.getAsString(Columns.systeemvalue.name)

                        SystemAttr.GbWhatsApp.internal -> ConstantsLocal.isGbWhatsAppEnabled =
                            map.getAsInteger(Columns.systeemvalue.name) == -1

                        SystemAttr.Price.internal -> ConstantsLocal.isPriceUseEnabled =
                            map.getAsInteger(Columns.systeemvalue.name) == -1

                        SystemAttr.Tooltip.internal -> Constants.isTooltipEnabled =
                            map.getAsInteger(Columns.systeemvalue.name) == -1

                        SystemAttr.Help.internal -> Constants.isHelpEnabled =
                            map.getAsInteger(Columns.systeemvalue.name) == -1

                        SystemAttr.Orders.internal -> ConstantsLocal.isOrderEnabled =
                            map.getAsInteger(Columns.systeemvalue.name) == -1

                        SystemAttr.ReorgDb.internal -> ConstantsLocal.isReorgEnabled =
                            map.getAsInteger(Columns.systeemvalue.name) == -1

                        SystemAttr.AutoNew.internal -> ConstantsLocal.isAutoNewEnabled =
                            map.getAsInteger(Columns.systeemvalue.name) == -1

                        SystemAttr.GPSInformation.internal -> ConstantsLocal.isGPSEnabled =
                            map.getAsInteger(Columns.systeemvalue.name) == -1

                        SystemAttr.SmartCopyCategory.internal -> ConstantsLocal.isSmartCopyCategory =
                            map.getAsInteger(Columns.systeemvalue.name) == -1

                        SystemAttr.AutoRefreshFolder.internal -> ConstantsLocal.isAutoRefreshFolder =
                            map.getAsInteger(Columns.systeemvalue.name) == -1

                        SystemAttr.StoreLastQuery.internal -> ConstantsLocal.isStoreLastQuery =
                            map.getAsInteger(Columns.systeemvalue.name) == -1

                        SystemAttr.FileExtension.internal -> ConstantsLocal.isFileExtensionEnabled =
                            map.getAsInteger(Columns.systeemvalue.name) == -1

                        SystemAttr.StoreQuery.internal -> ConstantsLocal.isStoreQuery =
                            map.getAsInteger(Columns.systeemvalue.name) > 0

                        SystemAttr.ScanImage.internal -> ConstantsLocal.isScanImageEnabled =
                            map.getAsInteger(Columns.systeemvalue.name) == -1

                        SystemAttr.FaceRecognition.internal -> ConstantsLocal.isFaceRecognitionEnabled =
                            map.getAsInteger(Columns.systeemvalue.name) == -1

                        SystemAttr.ScanQRCode.internal -> ConstantsLocal.isScannerEnabled =
                            map.getAsInteger(Columns.systeemvalue.name) == -1

                        SystemAttr.FileFancyScroll.internal -> ConstantsLocal.isFileFancyScrollEnabled =
                            map.getAsInteger(Columns.systeemvalue.name) == -1

                        SystemAttr.SingleListLabel.internal -> ConstantsLocal.isSingleListLabelEnabled =
                            map.getAsInteger(Columns.systeemvalue.name) == -1

                        SystemAttr.SearchFilesAvailable.internal -> ConstantsLocal.isSearchFilesAvailableEnabled =
                            map.getAsInteger(Columns.systeemvalue.name) == -1

                        SystemAttr.SearchFilesHidden.internal -> ConstantsLocal.isSearchFilesHiddenEnabled =
                            map.getAsInteger(Columns.systeemvalue.name) == -1

                        SystemAttr.TranslateText.internal -> ConstantsLocal.isTranslateTextEnabled =
                            map.getAsInteger(Columns.systeemvalue.name) == -1

                        SystemAttr.DeleteFilePhone.internal -> ConstantsLocal.deleteFilePhone =
                            map.getAsInteger(Columns.systeemvalue.name) == -1

                        SystemAttr.SortCreationYearDesc.internal -> ConstantsLocal.sortCreationYearDesc =
                            map.getAsInteger(Columns.systeemvalue.name) == -1

                        SystemAttr.SortCreationMonthDesc.internal -> ConstantsLocal.sortCreationMonthDesc =
                            map.getAsInteger(Columns.systeemvalue.name) == -1

                        SystemAttr.StoreFileSize.internal -> ConstantsLocal.storeFileSize =
                            map.getAsInteger(Columns.systeemvalue.name) == -1

                        SystemAttr.FileSizeMb.internal -> ConstantsLocal.fileSizeMB =
                            map.getAsInteger(Columns.systeemvalue.name) == -1

                        SystemAttr.OrderSeqnoDirectory.internal ->
                            updateCategorySeqno(ConstantsLocal.TYPE_DIRECTORY, map.getAsInteger(Columns.systeemvalue.name))

                        SystemAttr.OrderSeqnoCreation.internal ->
                            updateCategorySeqno(ConstantsLocal.TYPE_CREATION, map.getAsInteger(Columns.systeemvalue.name))

                        SystemAttr.OrderSeqnoExtension.internal ->
                            updateCategorySeqno(ConstantsLocal.TYPE_FILE_EXTENSION, map.getAsInteger(Columns.systeemvalue.name))

                        SystemAttr.OrderSeqnoGPS.internal ->
                            updateCategorySeqno(ConstantsLocal.TYPE_GPS, map.getAsInteger(Columns.systeemvalue.name))

                        SystemAttr.OrderFace.internal ->
                            updateCategorySeqno(ConstantsLocal.TYPE_FACE, map.getAsInteger(Columns.systeemvalue.name))
                    }
                }
            } while (rtn.cursor.moveToNext())
        }
        rtn.cursorClose()
        if ( !ConstantsLocal.isSearchFilesAvailableEnabled && !ConstantsLocal.isSearchFilesHiddenEnabled ){
            ConstantsLocal.isSearchFilesAvailableEnabled = true
            updateSysteemKey(SystemAttr.SearchFilesAvailable.internal, "-1")
        }

        setBackdoorOpen()
    }
    private fun updateCategorySeqno(type: Int, value: Int){
        val category = Category(context)
        val id = DBUtils.eLookUpInt(TableCategory.Columns._id.name, TableCategory.TABLE_NAME,
            "${TableCategory.Columns.type.name} = $type" )
        if (id > 0) {
            val map = ContentValues()
            map.put(TableCategory.Columns._id.name, id)
            map.put(TableCategory.Columns.seqno.name, value)
            category.updatePrimaryKey(map)
        }
    }

    fun systeemRegistration(){
        val id = DBUtils.eLookUp(Columns._id.name, tableName,
            "${Columns.systeemkey.name} = " + SystemAttr.Registration.internal).toString().toInt()
        val map = ContentValues()
        map.put(Columns.systeemkey.name, SystemAttr.Registration.internal)
        map.put(Columns.systeemvalue.name, DeviceInfoUtils.getIMEI(context) )
        if (id > 0) {
            map.put(Columns._id.name, id)
            updatePrimaryKey(map)
        } else {
            insertPrimaryKey(map)
        }
    }

    private fun setBackdoorOpen() {
        ConstantsLocal.isBackdoorOpen = false
        try {
            if (BuildConfig.LIMIT < 1 && // backdoor not valid in demo mode
                !ConstantsLocal.isGbWhatsAppEnabled &&
                Constants.isHelpEnabled &&
                !Constants.isTooltipEnabled &&
                !ConstantsLocal.isReorgEnabled &&
                (Calendar.getInstance()
                    .get(Calendar.DAY_OF_MONTH) + Calendar.getInstance()
                    .get(Calendar.HOUR_OF_DAY)) == getValue(SystemAttr.Backups).toInt()
            ) {
                // backdoor for registration database in an another mobile
                // backup this database without registration
                if (ConstantsLocal.registration != null) {
                    ConstantsLocal.registration = null
                    deleteSysteemKey(SystemAttr.Registration.internal)
                }
                ConstantsLocal.isBackdoorOpen = true
            }
        } catch (_: Exception) {
        }
    }

    companion object{

        fun validateDatabase(context: Context): ReturnValue{
            if (!Constants.isDbInitialized()){
                val rtn = ReturnValue()
                rtn.returnValue = false
                return rtn
            }
            val arrCheck = ArrayList<String>()
            arrCheck.add(TABLE_NAME)
            arrCheck.add(TableCategory.TABLE_NAME)
            arrCheck.add(TableHelp.TABLE_NAME)
            arrCheck.add(TableOrder.TABLE_NAME)
            arrCheck.add(TableOrderLine.TABLE_NAME)
            arrCheck.add(TableProduct.TABLE_NAME)
            arrCheck.add(TableProductRel.TABLE_NAME)
            arrCheck.add(TableFace.TABLE_NAME)
            return DatabaseHelper(context).checkDatabase(arrCheck)
        }
    }
}