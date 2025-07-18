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
package com.farrusco.projectclasses.activity

import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import android.widget.TableLayout
import android.widget.TextView
import androidx.collection.ArrayMap
import com.farrusco.projectclasses.R
import com.farrusco.projectclasses.common.Constants
import com.farrusco.projectclasses.common.ConstantsFixed
import com.farrusco.projectclasses.databases.tables.Tables
import com.farrusco.projectclasses.utils.Logging
import com.farrusco.projectclasses.utils.TagModify
import com.farrusco.projectclasses.utils.ViewUtils
import com.farrusco.projectclasses.widget.EditTextExt
import com.farrusco.projectclasses.widget.EditTextMoney
import com.farrusco.projectclasses.widget.TextViewExt
import com.farrusco.projectclasses.widget.money.WidgetMoney

abstract class BaseActivityTable : BaseActivity() {
    var tableIdx = 0
    lateinit var lstTblLayout: ArrayList<TableDefinition>

    override val groupNo: Int = -1

    fun getTableLayout(idx: Int): TableLayout?  {
        if (idx < lstTblLayout.size) {
            currMenu = lstTblLayout[idx].menuStackId
            return lstTblLayout[idx].tablelayout
        }
        currMenu=-1
        return null
    }

    open fun addTableLine(idx: Int, vi: View, groupno: Int = 0): View {
        if (idx < lstTblLayout.size) {
            val menuIdx = lstTblLayout[idx].menuStackId
            if (lstMenu[menuIdx].alContextMenus != null
                && lstMenu[menuIdx].alContextMenus!!.isNotEmpty()
            ) {
                // each field one setup menu
                val arrFields: ArrayList<View> =
                    ViewUtils.getAllChilds(vi as ViewGroup)
                for (fld in arrFields) {
                        // only valid fields become a popup menu
                        when (fld::class.simpleName){
                            TextViewExt::class.simpleName -> {
                                if ((fld as TextViewExt).contextMenuEnabled){
                                    this.registerForContextMenu(fld)
                                }
                            }
                            EditTextExt::class.simpleName -> {
                                if ((fld as EditTextExt).contextMenuEnabled){
                                    this.registerForContextMenu(fld)
                                }
                            }
                            Spinner::class.qualifiedName -> {
                                // lazy
                            }
                            else -> {
                                //this.registerForContextMenu(fld)
                            }
                        }
                    }
                }
            }
            // add of all missing columns with keys
            addHiddenFields(idx, vi, groupno)
            if (lstTblLayout[idx].tablelayout != null) {
                getTableLayout(idx)!!.addView(vi, TableLayout.LayoutParams())
            }
        return vi
    }

    fun newTableLine(resourceLineId: Int): View {
        val li: LayoutInflater = layoutInflater
        val vi = li.inflate(resourceLineId,null)
        vi.tag = TagModify.setTagValue(vi.tag,ConstantsFixed.TagSection.TsLineId.name,resourceLineId)
        //vi.setOnClickListener( mLongClickItemSelect )
        return vi
    }

    fun updateTableLayout(
        tableLayoutId: Int,
        tableLayout: TableLayout?){

        if (tableLayout != null) {
            for (i in lstTblLayout.indices){
                if (tableLayoutId == lstTblLayout[i].tablelayoutId){
                    lstTblLayout[i].tablelayout = tableLayout
                    break
                }
            }
        }
    }

    fun addTableLayout(
        tableLayoutId: Int,
        layoutLineId: Int = 0,
        table: Tables,
        layoutDbColumns: Array<String>,
        menu: ArrayList<Int>? = null,
        detailActivity: Class<*>? = null
    ) {
        val tablelayout:TableLayout? = findViewById(tableLayoutId)
        addTableLayout(tableLayoutId,tablelayout,layoutLineId,table,layoutDbColumns
            ,menu,detailActivity)
    }

    fun addTableLayout(
        tableLayoutId: Int,
        tableLayout: TableLayout?,
        layoutLineId: Int = 0,
        table: Tables,
        layoutDbColumns: Array<String>,
        menu: ArrayList<Int>? = null,
        detailActivity: Class<*>? = null
    ): Boolean {
        for (i  in lstTblLayout.indices){
            if (tableLayoutId == lstTblLayout[i].tablelayoutId)
                return false
        }
        for (i in layoutDbColumns.indices){
            layoutDbColumns[i] = layoutDbColumns[i].replace(" ","")
        }
        val tableDefinition = TableDefinition()

        tableDefinition.tablelayoutId = tableLayoutId
        tableDefinition.tablelayout = tableLayout

        tableDefinition.lineId = layoutLineId
        tableDefinition.table = table
        tableDefinition.layoutDbColumns = layoutDbColumns
        tableDefinition.detailActivity = detailActivity
        if (menu != null && menu.isNotEmpty()) {
            // init array
            tableDefinition.menuStackId = addMenu()
            lstMenu[tableDefinition.menuStackId].alContextMenus = contextMenuAdd(menu,2)

            menu.removeIf{x -> x == ConstantsFixed.EDIT_ID}
            menu.removeIf{x -> x == ConstantsFixed.DELETE_ID}
            lstMenu[tableDefinition.menuStackId].alTopMenus = contextMenuAdd(menu,1)

            if (detailActivity == null) {
                // no menu if there is no detail screen
                lstMenu[tableDefinition.menuStackId].alContextMenus = contextMenuRemove(ConstantsFixed.EDIT_ID, type=2 )
                lstMenu[tableDefinition.menuStackId].alContextMenus = contextMenuRemove(ConstantsFixed.BROWSE_ID, type=2)
            }
        }
        lstTblLayout.add(tableDefinition)
        return true

    }
    open fun addHiddenFields(idx: Int, vi: View, groupNo: Int = this.groupNo) {
        if (lstTblLayout[idx].table != null) {
            val arrChildDBNames = ArrayList<String?>()
            val arrChildDBColumns: ArrayList<View> =
                ViewUtils.getChildDBColumns(vi as ViewGroup, groupNo)
            for (i in arrChildDBColumns.indices) {
                if (ViewUtils.getDBColumnFore(arrChildDBColumns[i]) != null) arrChildDBNames.add(ViewUtils.getDBColumnFore(arrChildDBColumns[i]))
                if (ViewUtils.getDBColumnBack(arrChildDBColumns[i]) != null) arrChildDBNames.add(ViewUtils.getDBColumnBack(arrChildDBColumns[i]))
            }
            // add unknown columns to row (parent is table row)
            if (arrChildDBColumns.isEmpty()){
                Logging.e ("BaseActivityTable/addHiddenFields","Cannot find fields for group: $groupNo")
            } else {
                val tableRow = arrChildDBColumns[0].parent
                for (obj in lstTblLayout[idx].table!!.dbColumns) {
                    if (!arrChildDBNames.contains(obj)) {
                        ViewUtils.addHiddenField((tableRow as ViewGroup), obj, "", groupNo)
                    }
                }
            }
        }
    }

    override fun addHiddenFields(cursor: Cursor, vi: View, groupNo: Int) {
        val arrChildDBNames = ArrayList<String?>()
        val arrChildDBColumns: ArrayList<View> =
            ViewUtils.getChildDBColumns(vi as ViewGroup, groupNo)
        for (i in arrChildDBColumns.indices) {
            arrChildDBNames.add( ViewUtils.getDBColumn(arrChildDBColumns[i]))
        }
        // test good or not
        if (arrChildDBColumns.isNotEmpty()){
            val tableRow = arrChildDBColumns[0].parent
            for (i in 0 until cursor.columnCount) {
                if (!arrChildDBNames.contains(cursor.getColumnName(i))) {
                    ViewUtils.addHiddenField((tableRow as ViewGroup), cursor.getColumnName(i), cursor.getString(i), groupNo)
                }
            }
        }
    }

    fun newRowInit(vi: View, tableDefinition: TableDefinition) {
        val arrChilds: ArrayList<View> =
            ViewUtils.getAllChilds(vi as ViewGroup)
        var cnt = 0
        if (tableDefinition.layoutDbColumns == null) return

        var columnIdx = 0
        arrChilds.filterNot {it is TextView && it.tag != null &&
                it.tag.toString().equals(getText(R.string.ignore).toString(),true) }.forEach {

            if (columnIdx < tableDefinition.layoutDbColumns!!.size) {
                var bSet = false
                if (it is EditTextMoney){
                    WidgetMoney().registerFields(it.parent as ViewGroup, it.isEnabled)
                }

                val tag = TagModify.getViewTagValue(it, ConstantsFixed.TagSection.TsForeBack.name)
                if (tag == "b" || tag =="fb" || tag =="f" ){
                    if (!TagModify.hasTagSection (it, ConstantsFixed.TagSection.TsDBColumnBack.name )){
                        val dbColumns = tableDefinition.layoutDbColumns!![columnIdx].split(",")
                        bSet = true
                        if (dbColumns.size > 1){
                            TagModify.setViewTagValue( it, ConstantsFixed.TagSection.TsDBColumn.name, dbColumns[0])
                            TagModify.setViewTagValue( it, ConstantsFixed.TagSection.TsDBColumnBack.name, dbColumns[1])
                        } else{
                            when (tag){
                                "f" ->{
                                    TagModify.setViewTagValue( it, ConstantsFixed.TagSection.TsDBColumn.name, tableDefinition.layoutDbColumns!![columnIdx])
                                }
                                "b" ->{
                                    TagModify.setViewTagValue( it, ConstantsFixed.TagSection.TsDBColumnBack.name, tableDefinition.layoutDbColumns!![columnIdx])
                                }
                                "fb" -> {
                                    TagModify.setViewTagValue( it, ConstantsFixed.TagSection.TsDBColumn.name, tableDefinition.layoutDbColumns!![columnIdx])

                                    columnIdx++
                                    if (columnIdx < tableDefinition.layoutDbColumns!!.size) {
                                        // incase of switch/checkbox these items contains 2 db columns, first description, second value
                                        TagModify.setViewTagValue(
                                            it,
                                            ConstantsFixed.TagSection.TsDBColumnBack.name,
                                            tableDefinition.layoutDbColumns!![columnIdx]
                                        )
                                        cnt++
                                    }
                                }
                            }

                            if (tableDefinition.layoutDbColumnsLen.containsKey(tableDefinition.layoutDbColumns!![columnIdx])){
                                ViewUtils.setTextMaxLength(it,tableDefinition.table!!.tableName)
                            }

                        }
                    }
                }
                if (!bSet) {
                    // set column name and possible max input length
                    ViewUtils.setDBColumn(it, tableDefinition.layoutDbColumns!![columnIdx], tableDefinition.table?.tableName, true)
                }
                cnt++
                columnIdx++
            }
        }

        if (arrChilds.isNotEmpty()){
            val tableRow = arrChilds[0].parent
            for (i in cnt until tableDefinition.layoutDbColumns!!.size) {
                ViewUtils.addHiddenField((tableRow as ViewGroup), tableDefinition.layoutDbColumns!![i], "", groupNo)
            }
        }
    }

    inner class TableDefinition {
        var tablelayoutId = 0
        var tablelayout: TableLayout? = null
        var lineId = 0
        var layoutDbColumns:Array<String>? = null
            set (value){
                field = value
                if (table != null){
                    value!!.forEach {
                        if (Constants.dbStructure.containsKey("${table!!.tableName}.$it".lowercase())){
                            val valueStruct = Constants.dbStructure.getValue("${table!!.tableName}.$it".lowercase())
                            if (valueStruct != null && valueStruct.len > 0){
                                layoutDbColumnsLen[it] = valueStruct.len
                            }
                        }
                    }
                }
            }
        var layoutDbColumnsLen: ArrayMap<String, Int> = ArrayMap()
        var table: Tables? = null
        var detailActivity: Class<*>? = null
        var menuStackId = 0
    }
}