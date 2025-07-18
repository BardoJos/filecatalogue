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

import android.widget.Toast
import androidx.activity.result.ActivityResult
import com.farrusco.filecatalogue.R
import com.farrusco.filecatalogue.business.Category
import com.farrusco.filecatalogue.business.Help
import com.farrusco.filecatalogue.common.ConstantsLocal
import com.farrusco.filecatalogue.tables.TableCategory
import com.farrusco.projectclasses.activity.BaseActivityTableLayout
import com.farrusco.projectclasses.common.Constants
import com.farrusco.projectclasses.common.ConstantsFixed
import com.farrusco.projectclasses.utils.CalcObjects
import com.farrusco.projectclasses.utils.ReturnValue
import com.farrusco.projectclasses.messages.ToastExt

class ListCategory : BaseActivityTableLayout() {
    override val layoutResourceId: Int = R.layout.list_single
    override val mainViewId: Int = R.id.viewMain
    private var category = Category(this)

    override fun initTableLayout() {
        addTableLayout(
            R.id.tblLayout,
            R.layout.line_textview1x,
            category,
            arrayOf(TableCategory.Columns.title.name),
            arrayListOf(ConstantsFixed.HELP_ID,ConstantsFixed.SAVE_ID, ConstantsFixed.ADD_ID, ConstantsFixed.EDIT_ID, ConstantsFixed.DELETE_ID),
            DetailCategory::class.java
        )
    }

    override fun initActivity() {
        helpText = Help().getHelpTitle(className)
        category = Category(this)
        fillList()
        if (Constants.isTooltipEnabled ){
            ToastExt().makeText(
                this,
                R.string.mess064_pressrightbutton4menu, Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun fillList() {
        removeAllTableLayoutViews(0)
        val rtn = category.getCategory(0, 0, arrayListOf(ConstantsLocal.TYPE_CATEGORY, ConstantsLocal.TYPE_FACE), false )
        fillTable(rtn.cursor)
        rtn.cursorClose()
    }

    override fun saveScreen(): ReturnValue {
        val rtn = super.saveScreen()
        if (rtn.returnValue){
            intent.putExtra(Constants.RequestCode, "1")
            setResult(RESULT_OK,intent)
        }
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