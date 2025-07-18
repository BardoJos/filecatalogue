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

import android.content.ContentValues
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.core.view.indices
import com.farrusco.filecatalogue.R
import com.farrusco.filecatalogue.business.Category
import com.farrusco.filecatalogue.business.Help
import com.farrusco.filecatalogue.common.ConstantsLocal
import com.farrusco.filecatalogue.tables.TableCategory
import com.farrusco.projectclasses.activity.BaseActivityTableLayout
import com.farrusco.projectclasses.common.Constants
import com.farrusco.projectclasses.common.ConstantsFixed
import com.farrusco.projectclasses.messages.ToastExt
import com.farrusco.projectclasses.utils.CalcObjects
import com.farrusco.projectclasses.utils.ReturnValue
import com.farrusco.projectclasses.utils.TagModify
import com.farrusco.projectclasses.utils.ViewUtils
import com.farrusco.projectclasses.widget.EditTextExt

class DetailCategory : BaseActivityTableLayout() {

    override val layoutResourceId: Int = R.layout.detail_category
    override val mainViewId: Int = R.id.viewMain

    private lateinit var mTitle: EditTextExt
    private lateinit var mSeqno: EditTextExt
    private lateinit var category: Category
    private var categoryId = 0
    private var typeId = 0
    override fun initTableLayout() {
        addTableLayout(R.id.tblLayout,
            R.layout.seqno_text_button,
            category,
            arrayOf(
                TableCategory.Columns.seqno.name,
                TableCategory.Columns.title.name
            ),
            arrayListOf(ConstantsFixed.HELP_ID,ConstantsFixed.SAVE_ID, ConstantsFixed.ADD_ID, ConstantsFixed.EDIT_ID, ConstantsFixed.DELETE_ID),
            DetailCategory::class.java)
    }
    override fun initActivity() {
        helpText = Help().getHelpTitle(className)
        category = Category(this)
        mSeqno = findViewById(R.id.txtSeqno)

        ViewUtils.setDBColumn(mSeqno,TableCategory.Columns.seqno.toString())
        mTitle = findViewById(R.id.txtTitle)
        ViewUtils.setDBColumn(mTitle,TableCategory.Columns.title.toString(), TableCategory.TABLE_NAME, true)

        if (intent.hasExtra(TableCategory.Columns._id.name)) {
            val tmp = intent.getStringExtra(TableCategory.Columns._id.name)
            categoryId = Integer.valueOf(tmp!!)
            val rtn = category.getCategory(categoryId, -1,  arrayListOf(0), false)
            if (rtn.cursor.moveToFirst()) {
                ViewUtils.copyCursorToViewGroup(rtn.cursor, viewMain as ViewGroup, groupNo)
            }
            rtn.cursorClose()
        }

        if (intent.hasExtra(TableCategory.Columns.type.name)) {
            val tmp = intent.getStringExtra(TableCategory.Columns.type.name)
            typeId = Integer.valueOf(tmp!!)
        } else {
            typeId = ConstantsLocal.TYPE_CATEGORY
        }
        fillList()
    }
    private fun fillList() {
        removeAllTableLayoutViews(0)
        if (intent.hasExtra(TableCategory.Columns._id.name)){
            val rtn = category.getCategory(0, categoryId, arrayListOf(0), false)
            fillTable(rtn.cursor)
            rtn.cursorClose()
        }
    }
    override fun onContextItemSelected(item: MenuItem): Boolean {
        val detailActivity: Class<*>? = lstTblLayout[tableIdx].detailActivity

        when (item.itemId) {
            ConstantsFixed.EDIT_ID -> {
                if (null != detailActivity) {
                    val rtn = saveScreen()
                    if (!rtn.returnValue) {
                        return false
                    }
                }
            }
        }
        return super.onContextItemSelected(item)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val detailActivity: Class<*>? = lstTblLayout[tableIdx].detailActivity

        when (item.itemId) {
            ConstantsFixed.ADD_ID -> {
                lstTblLayout[tableIdx].detailActivity = null
                val rtn = super.onOptionsItemSelected(item)

                val vi = lstTblLayout[tableIdx].tablelayout!!.getChildAt(lstTblLayout[tableIdx].tablelayout!!.childCount-1) as ViewGroup
                ViewUtils.setChildDBColumnValue(vi, TableCategory.Columns._id.name,"0")
                // in case of new parent the category is zero
                ViewUtils.setChildDBColumnValue(vi, TableCategory.Columns._mainid.name, categoryId.toString())
                ViewUtils.setChildDBColumnValue(vi, TableCategory.Columns.type.name, typeId.toString() )
                lstTblLayout[tableIdx].detailActivity = detailActivity
                return rtn
            }
            ConstantsFixed.EDIT_ID -> {
                if (null != detailActivity) {
                    val rtn = saveScreen()
                    if (!rtn.returnValue) {
                        return false
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
    override fun saveScreen(): ReturnValue {
        var rtn = ReturnValue()
        if (!hasChildModificationTag()){
            rtn.returnValue = true
            return rtn
        }
        rtn = validateScreen()
        if (!rtn.returnValue) {
            return rtn
        }
        val locValues = ContentValues()
        if (TagModify.getViewTagValue(mTitle,ConstantsFixed.TagSection.TsUserFlag.name) != "" ||
            TagModify.getViewTagValue(mSeqno,ConstantsFixed.TagSection.TsUserFlag.name) != "" ||
            categoryId == 0) {
            locValues.put(TableCategory.Columns._id.name, categoryId)
            locValues.put(ViewUtils.getDBColumn(mTitle)!!, mTitle.text.toString())
            locValues.put(ViewUtils.getDBColumn(mSeqno)!!, mSeqno.text.toString())
            locValues.put(TableCategory.Columns.description.name, mTitle.text.toString())

            if (categoryId>0)
                rtn = category.updatePrimaryKey(locValues)
            else {
                locValues.put(TableCategory.Columns.type.name, typeId)
                locValues.put(TableCategory.Columns._mainid.name, intent.getStringExtra(TableCategory.Columns._mainid.name))
                rtn = category.insertPrimaryKey(locValues)
                categoryId = rtn.id
            }
            if (!rtn.returnValue) {
                return rtn
            }
        }

        for(idx in lstTblLayout[tableIdx].tablelayout!!.indices) {
            val row = lstTblLayout[tableIdx].tablelayout!!.getChildAt(idx) as TableRow
            ViewUtils.setChildDBColumnValue(row, TableCategory.Columns._mainid.name, categoryId.toString())
        }

        rtn = super.saveScreen()
        if (!rtn.returnValue) {
            return rtn
        }
        ViewUtils.removeChildTag(viewMain as ViewGroup, arrayOf( ConstantsFixed.TagSection.TsUserFlag.name))

        intent.putExtra(Constants.RequestCode, "1")
        ToastExt().makeText(this, R.string.mess002_saved, Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK,intent)
        return rtn
    }
    override fun resultActivity(result: ActivityResult) {
        if (result.resultCode == RESULT_OK) {
            when (CalcObjects.stringToInteger(result.data!!.getStringExtra(Constants.RequestCode),0)) {
                1 -> {
                    fillList()
                    intent.putExtra(Constants.RequestCode, "1")
                }
            }
        }
    }
}