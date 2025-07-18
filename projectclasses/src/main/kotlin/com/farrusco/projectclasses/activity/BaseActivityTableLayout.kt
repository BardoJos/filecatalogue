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
@file:Suppress("unused")

package com.farrusco.projectclasses.activity

import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.Toast
import androidx.activity.result.ActivityResult
import com.farrusco.projectclasses.R
import com.farrusco.projectclasses.common.Constants
import com.farrusco.projectclasses.common.ConstantsFixed
import com.farrusco.projectclasses.messages.ToastExt
import com.farrusco.projectclasses.utils.*
import com.farrusco.projectclasses.utils.ViewUtils.copyViewGroupToContentValues
import com.farrusco.projectclasses.utils.ViewUtils.getTableRow
import com.farrusco.projectclasses.utils.ViewUtils.getTableRowParentLineId
import com.farrusco.projectclasses.utils.ViewUtils.setColorToViewGroup
import com.farrusco.projectclasses.widget.ButtonExt

abstract class BaseActivityTableLayout : BaseActivityTable() {
    private var alDelete: ArrayList<Int>? = null
    private var alEdit: ArrayList<Int>? = null
    private var alNew: ArrayList<Int>? = null

    //@SuppressWarnings("rawtypes")
    abstract fun initTableLayout()
    abstract override val layoutResourceId: Int

    //abstract View newRow();
    fun newRow(idx: Int): View? {
        if (idx < lstTblLayout.size && lstTblLayout[idx].lineId != 0 && lstTblLayout[idx].layoutDbColumns != null
        ) {
            val vi: View = newTableLine(lstTblLayout[idx].lineId)
            newRowInit(vi, lstTblLayout[idx])
            return addTableLine(idx, vi)
        }
        return null
    }

    private fun newRow(): View? {
        if (tableIdx < lstTblLayout.size && lstTblLayout[tableIdx].lineId != 0 && lstTblLayout[tableIdx].layoutDbColumns != null
        ) {
            val vi: View = newTableLine(lstTblLayout[tableIdx].lineId)
            newRowInit(vi, lstTblLayout[tableIdx])
            return addTableLine(tableIdx, vi)
        }
        return null
    }

    open fun newRowAfterAdd(vi: View?){
        if (vi != null){
            TagModify.setViewTagValue(vi,ConstantsFixed.TagSection.TsUserFlag, ConstantsFixed.TagAction.Edit.name)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        lstTblLayout = ArrayList()
        super.onCreate(savedInstanceState)
        setUpdatePrevScreen(false)
    }

    open fun saveRowNew(): ReturnValue {
        val viTableRow = getTableRowParentLineId(currView)
        val cv = copyViewGroupToContentValues(viTableRow as ViewGroup)
        val rtn = lstTblLayout[tableIdx].table!!.insertPrimaryKey(cv)
        if (!rtn.returnValue) {
            (currView as ViewGroup).tag = null
        } else {
            val pks: Array<String> = lstTblLayout[tableIdx].table!!.primaryKeys
            if (pks.size == 1) {
                TagModify.setViewTagValue(currView as ViewGroup, pks[0], "" + rtn.id)
                // set child with dbcolumn=pks[0] dbvalue=rtn.id
                ViewUtils.setChildDBColumnValue(viTableRow,pks[0],rtn.id.toString())
            }
        }
        return rtn
    }

    open fun saveRowEdit(): ReturnValue {
        val viTableRow = getTableRow(currView)
        val cv = ContentValuesExt.copyBundleContentValues(intent.extras)
        cv.putAll(copyViewGroupToContentValues(viTableRow as ViewGroup, 0))
        return lstTblLayout[tableIdx].table!!.updatePrimaryKey(cv)
    }

    fun fillList(cursor: Cursor) {
        removeAllTableLayoutViews(0)
        fillTable(cursor)
    }

    fun fillTable(cursor: Cursor) {
        if (cursor.moveToFirst()) {
           do {
                val vi = newRow()
                if (vi != null) {
                    copyCursorToViewGroup(cursor, vi as ViewGroup?)
                    setRowMenu(vi, "RowMenu ${cursor.position}")
                }
            } while (cursor.moveToNext())
        }
    }

    open fun copyCursorToViewGroup(cursor: Cursor, v: ViewGroup?) {
        // copy values from cursor to view and add hidden column if necessary
        ViewUtils.copyCursorToViewGroup(cursor, v)
    }

    fun removeAllTableLayoutViews(idx: Int) {
        if (lstTblLayout.size <= idx) initTableLayout()
        if (lstTblLayout.size <= idx) {
            Logging.d(
                resources.getString(R.string.app_name),
                "removeAllTableLayoutViews has no tablelayout"
            )
            return
        }
        if (lstTblLayout[idx].tablelayout != null) {
            lstTblLayout[idx].tablelayout!!.removeAllViews()
        }
    }

    open fun validateRowNew(vi: View?): ReturnValue {
        if (! ViewUtils.validateEditTextNulls(vi as ViewGroup?).returnValue) {
            ToastExt().makeText(this, R.string.mess003_notnull, Toast.LENGTH_LONG).show()
            return ReturnValue().setReturnvalue(false)
        }
        return ReturnValue().setReturnvalue(true)
    }

    open fun validateRowEdit(vi: View?): ReturnValue {
        if (! ViewUtils.validateEditTextNulls(vi as ViewGroup?).returnValue) {
            ToastExt().makeText(this, R.string.mess003_notnull, Toast.LENGTH_LONG).show()
            return ReturnValue().setReturnvalue(false)
        }
        return ReturnValue().setReturnvalue(true)
    }

    open fun validateRowDelete(vi: View?): ReturnValue {
        return ReturnValue()
    }

    private fun fillModList(tableLayout: TableLayout?) {
        if (tableLayout == null) return
        var vi: View
        alDelete = ArrayList()
        alEdit = ArrayList()
        alNew = ArrayList()
        val j: Int = tableLayout.childCount
        for (row in 0 until j) {
            vi = tableLayout.getChildAt(row)

            when {
                ViewUtils.hasChildTag(
                    vi as ViewGroup,
                    ConstantsFixed.TagSection.TsUserFlag.name,
                    ConstantsFixed.TagAction.Delete.name
                ) -> {
                    alDelete!!.add(row)
                }
                // exception: get only modified rows
                ViewUtils.hasChildTag(
                    vi,
                    ConstantsFixed.TagSection.TsModFlag.name,
                    ConstantsFixed.TagAction.Edit.name
                ) && ViewUtils.hasChildTag(
                    vi,
                    ConstantsFixed.TagSection.TsUserFlag.name,
                    ConstantsFixed.TagAction.Edit.name
                ) -> {
                    alEdit!!.add(row)
                }
                ViewUtils.hasChildTag(
                    vi,
                    ConstantsFixed.TagSection.TsModFlag.name,
                    ConstantsFixed.TagAction.New.name
                ) -> {
                    alNew!!.add(row)
                }
            }
        }
    }

    override fun validateScreen(): ReturnValue {
        var rtn = ReturnValue()
        if (!ViewUtils.hasChildTag(viewMain as ViewGroup,
                ConstantsFixed.TagSection.TsUserFlag.name,
                ConstantsFixed.TagAction.Edit.name)){
            rtn.returnValue = true
            return rtn
        }
        rtn = validateEditTextNulls()
        if (!rtn.returnValue) {
            var ms = getString(R.string.mess003_notnull)
            @Suppress("LiftReturnOrAssignment")
            if (rtn.arrParams.isNotEmpty() && rtn.arrParams[0] != ""){
                ms = ms.replace("%0%",rtn.arrParams[0])
            } else {
                ms = getString(R.string.error_field_must_not_be_empty)
            }
            ToastExt().makeText(this, ms, Toast.LENGTH_LONG).show()
            rtn.showMessage=false
            return rtn
        }

        var tblLayout: TableLayout
        for (i in 0 until lstTblLayout.size ) {
            tableIdx = i
            if (lstTblLayout[i].tablelayout == null) continue
            tblLayout = lstTblLayout[i].tablelayout!!
            var vi: View
            fillModList(tblLayout)
            if (alDelete != null) {
                for (row in alDelete!!) {
                    vi = tblLayout.getChildAt(row)
                    rtn = validateRowDelete(vi)
                    if (!rtn.returnValue ) {
                        return rtn
                    }
                }
            }
            if (alNew != null) {
                for (row in alNew!!) {
                    vi = tblLayout.getChildAt(row)
                    rtn = validateRowNew(vi)
                    if (!rtn.returnValue) {
                        return rtn
                    }
                }
            }
            if (alEdit != null) {
                for (row in alEdit!!) {
                    vi = tblLayout.getChildAt(row)
                    rtn = validateRowEdit(vi)
                    if (!rtn.returnValue) {
                        return rtn
                    }
                }
            }
        }
        return rtn
    }

    override fun saveScreen(): ReturnValue  {
        var rtn: ReturnValue = validateScreen()
        var saveddata = false
        if (!rtn.returnValue ) {
            if (rtn.messnr > 0) ToastExt().makeText(this, rtn.messnr, Toast.LENGTH_LONG)
                .show() else ToastExt().makeText(
                this,
                R.string.mess033_validateerror,
                Toast.LENGTH_LONG
            ).show()
            return ReturnValue().setReturnvalue(false).setShowmessage(false)
        }
        try {
            var tblLayout: TableLayout
            Constants.db.beginTransaction()
            for (i in lstTblLayout.indices ) {
                tableIdx = i
                if (lstTblLayout[i].tablelayout == null) continue
                tblLayout = lstTblLayout[tableIdx].tablelayout!!
                fillModList(tblLayout)
                var vi: View
                if (alDelete != null) {
                    for (row in alDelete!!) {
                        vi = tblLayout.getChildAt(row)
                        currView=vi
                        rtn = deleteRow()
                        if (!rtn.returnValue) {
                            break
                        }
                        saveddata=true
                    }
                }
                if (alNew != null) {
                    for (row in alNew!!) {
                        vi = tblLayout.getChildAt(row)
                        currView=vi
                        rtn = saveRowNew()
                        if (!rtn.returnValue) {
                            break
                        }
                        saveddata=true
                    }
                }
                if (alEdit != null) {
                    for (row in alEdit!!) {
                        vi = tblLayout.getChildAt(row)
                        currView=vi
                        rtn = saveRowEdit()
                        if (!rtn.returnValue ) {
                            break
                        }
                        saveddata=true
                    }
                }
                if (rtn.returnValue) {
                    if (alDelete != null) {
                        for (row in alDelete!!.indices.reversed()) {
                            tblLayout.removeViewAt(alDelete!![row])
                        }
                    }
                    if (alNew != null) {
                        for (row in alNew!!) {
                            vi = tblLayout.getChildAt(row)
                            ViewUtils.removeChildTag(vi as ViewGroup, arrayOf(ConstantsFixed.TagSection.TsModFlag.name,ConstantsFixed.TagSection.TsUserFlag.name))
                            vi.tag = TagModify.setTagValue(vi.tag,ConstantsFixed.TagSection.TsModFlag.name,ConstantsFixed.TagAction.Edit.name)
                        }
                    }
                    if (alEdit != null) {
                        for (row in alEdit!!) {
                            vi = tblLayout.getChildAt(row)
                            ViewUtils.removeChildTag(vi as ViewGroup, arrayOf(ConstantsFixed.TagSection.TsModFlag.name,ConstantsFixed.TagSection.TsUserFlag.name))
                            vi.tag = TagModify.setTagValue(vi.tag,ConstantsFixed.TagSection.TsModFlag.name,ConstantsFixed.TagAction.Edit.name)
                        }
                    }
                } else if (!rtn.showMessage) {
                    if (rtn.messnr > 0) {
                        ToastExt().makeText(
                            this,
                            this.getString(rtn.messnr, rtn.arrParams),
                            Toast.LENGTH_LONG
                        )
                            .show()
                    } else if (rtn.mess.isNotEmpty()) {
                        ToastExt().makeText(this, rtn.mess, Toast.LENGTH_LONG).show()
                    } else {
                        ToastExt().makeText(this, R.string.mess032_notsaved, Toast.LENGTH_LONG).show()
                    }
                    rtn = ReturnValue().setReturnvalue(false).setShowmessage(false)
                }
            }
            if (rtn.returnValue) {
                if (saveddata){
                    ToastExt().makeText(this, R.string.mess002_saved, Toast.LENGTH_SHORT).show()
                }
                Constants.db.setTransactionSuccessful()
            } else {
                if (rtn.messnr > 0) {
                    ToastExt().makeText(this, rtn.messnr, Toast.LENGTH_LONG).show()
                    rtn.setShowmessage(false)
                } else if (rtn.mess.isNotEmpty()) {
                    ToastExt().makeText(this, rtn.mess, Toast.LENGTH_LONG).show()
                    rtn.setShowmessage(false)
                }
            }
        } catch (e: Exception) {
            ToastExt().makeText(this, R.string.mess032_notsaved, Toast.LENGTH_LONG).show()
            rtn.setShowmessage(false)
        } finally {
            Constants.db.endTransaction()
        }

        return rtn
    }

    open fun insertRow(cursor: Cursor?, init: Boolean): View? {
        Logging.d(resources.getString(R.string.app_name),"BaseActivityTableLayout/insertRow","Forgot to define override function")
        return null
    }

    override fun addTableLine(idx: Int, vi: View, groupno: Int): View {
        if (idx >= lstTblLayout.size) initTableLayout()
        setRowMenu(vi)
        return super.addTableLine(idx, vi, groupno)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val vi: View?
        val intent: Intent
        var detailActivity: Class<*>? = null
        if (tableIdx < lstTblLayout.size){
            detailActivity = lstTblLayout[tableIdx].detailActivity
        }
        when (item.itemId) {
            ConstantsFixed.ADD_ID -> {
                if (detailActivity != null) {
                    // insert row at return
                    intent = Intent(this, detailActivity)
                    intent.putExtra(
                        ConstantsFixed.TagSection.TsModFlag.name,
                        ConstantsFixed.TagAction.Insert.name
                    )
                    resultLauncher.launch(intent)
                } else {
                    vi = newRow()
                    if (null == vi) {
                        Logging.d("onOptionsItemSelected","New row is empty")
                    } else {
                        ViewUtils.setFocusFirstChild(vi as ViewGroup)
                        ViewUtils.setChildTags(vi,ConstantsFixed.TagSection.TsModFlag.name,ConstantsFixed.TagAction.New.name)
                    }
                    newRowAfterAdd(vi)
                }
                return true
            }
            ConstantsFixed.SAVE_ID -> saveScreen()
            ConstantsFixed.HELP_ID -> {
                showHelp()
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return false
    }

    fun setTableRowMenu(tablelayout: TableLayout?){
        if (tablelayout != null) {
            val j: Int = tablelayout.childCount
            for (row in 0 until j) {
                val vi = tablelayout.getChildAt(row)
                setRowMenu(vi, "RowMenu $row")
            }
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun setRowMenu(vi: View, description: String = "") {
        val btnMenu = vi.findViewWithTag<ButtonExt>(ConstantsFixed.popupmenu) ?: return
        btnMenu.contentDescription = description
        btnMenu.setOnClickListener {
            val popupMenu = createPopupMenu(btnMenu)
            popupMenu.show()
            popupMenu.setOnMenuItemClickListener { item ->
                onContextItemSelected(item)
                true
            }
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (tableIdx >= lstTblLayout.size ) {
            return super.onContextItemSelected(item)
        }
        val vi: View
        val intent: Intent
        val detailActivity: Class<*>? = lstTblLayout[tableIdx].detailActivity
        return when (item.itemId) {
            ConstantsFixed.ADD_ID -> {
                vi = insertRow(null, false)!!
                ViewUtils.setFocusFirstChild(vi as ViewGroup)
                if (detailActivity != null) {
                    intent = Intent(this, detailActivity)
                    intent.putExtra(
                        ConstantsFixed.TagSection.TsModFlag.name,
                        ConstantsFixed.TagAction.New.name
                    )
                    resultLauncher.launch(intent)
                }
                return true
            }
            ConstantsFixed.BROWSE_ID -> {
                if (null != detailActivity) {
                    intent = Intent(this, detailActivity)
                    // lazy: duplicatie modflag
                    intent.putExtra(
                        ConstantsFixed.TagSection.TsModFlag.name,
                        ConstantsFixed.TagAction.Browse.name
                    )
                    vi = getTableRow(currView)!!
                    val cv = copyViewGroupToContentValues(vi as ViewGroup)
                    cv.keySet().forEach {
                        intent.putExtra(it,cv[it].toString())
                    }

                    if (intent.hasExtra(ConstantsFixed.TagSection.TsUserFlag.name)) {
                        // child windows does not need to know if it is edit before
                        intent.removeExtra(ConstantsFixed.TagSection.TsUserFlag.name)
                    }
                    resultLauncher.launch(intent)
                    return true
                }
                super.onContextItemSelected(item)
            }
            ConstantsFixed.EDIT_ID -> {
                if (null != detailActivity) {
                    intent = Intent(this, detailActivity)
                    vi = getTableRow(currView)!!
                    val cv = copyViewGroupToContentValues(vi as ViewGroup)
                    cv.keySet().forEach {
                        intent.putExtra(it, cv[it].toString())
                    }

                    intent.putExtra(
                        ConstantsFixed.TagSection.TsModFlag.name,
                        ConstantsFixed.TagAction.Edit.name
                    )
                    if (intent.hasExtra(ConstantsFixed.TagSection.TsUserFlag.name)){
                       // child windows does not need to know if it is edit before
                        intent.removeExtra(ConstantsFixed.TagSection.TsUserFlag.name)
                    }
                    resultLauncher.launch(intent)
                    return true
                }
                super.onContextItemSelected(item)
            }
            ConstantsFixed.DELETE_ID -> {
/*                if (lstTblLayout.count() != 1){
                    return false
                }*/

                val tblLayout = lstTblLayout[tableIdx].tablelayout!!
                vi = getTableRowParentLineId(currView)!!
                if (TagModify.hasTagValue(vi,
                        ConstantsFixed.TagSection.TsModFlag.name,
                        ConstantsFixed.TagAction.New.name) ||
                    ViewUtils.hasChildTag(
                        vi as ViewGroup,
                        ConstantsFixed.TagSection.TsModFlag.name,
                        ConstantsFixed.TagAction.New.name
                    )
                ) {
                    // delete row if not in database
                    tblLayout.removeView(vi)
                } else {
                    // mark row for deletion
                    ViewUtils.setChildDelete(this,vi,false)
                }

                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    open fun deleteRow(): ReturnValue {
        val viTableRow = getTableRow(currView)
        if (lstTblLayout[tableIdx].table == null || viTableRow!!.tag == null) {
            return ReturnValue().setReturnvalue(false)
        }

        val values = copyViewGroupToContentValues(viTableRow)
        return lstTblLayout[tableIdx].table!!.deletePrimaryKey(values)
    }

    override fun resultActivity(result: ActivityResult) {
    //override var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            if (result.data != null && result.data!!.hasExtra(ConstantsFixed.TagSection.TsModFlag.name)){
                var viTableRow: ViewGroup? = null
                when (result.data!!.getStringExtra(ConstantsFixed.TagSection.TsModFlag.name)) {
                    ConstantsFixed.TagAction.Edit.name -> {
                        try {
                            val vi = getTableRow(currView)
                            if (vi != null) viTableRow = vi
                        } catch (e:Exception){
                            // skip
                        }
                    }
                    ConstantsFixed.TagAction.Insert.name -> {
                        viTableRow = newRow() as ViewGroup
                        viTableRow.tag = TagModify.setTagValue(viTableRow.tag,ConstantsFixed.TagSection.TsModFlag.name,ConstantsFixed.TagAction.New.name)
                    }
                    ConstantsFixed.TagAction.New.name -> {
                        try {
                            val vi = getTableRow(currView)
                            if (vi != null) viTableRow = vi
                        } catch (e:Exception){
                            // skip
                        }
                    }
                }
                if (viTableRow != null){
                    val map = ContentValuesExt.copyBundleContentValues(intent.extras)
                    ViewUtils.copyContentValuesToViewGroup(map, viTableRow)
                    //setColorToViewGroup(result.data!!.extras, viTableRow)
                    setColorToViewGroup(map, viTableRow)
                }
            }
        }
    }
}