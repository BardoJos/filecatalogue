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

import com.farrusco.filecatalogue.R
import com.farrusco.filecatalogue.business.Category
import com.farrusco.filecatalogue.business.Help
import com.farrusco.filecatalogue.common.ConstantsLocal
import com.farrusco.filecatalogue.tables.TableCategory
import com.farrusco.projectclasses.activity.BaseActivityTableLayout
import com.farrusco.projectclasses.common.ConstantsFixed

class ListQuery : BaseActivityTableLayout() {

    override val layoutResourceId = R.layout.list_query
    override val mainViewId = R.id.viewMain

    private lateinit var category: Category
    override fun initTableLayout() {
        addTableLayout(R.id.tblLayout,
            R.layout.seqno_text,
            category,
            arrayOf(
                TableCategory.Columns.seqno.name,
                TableCategory.Columns.title.name
            ),
            arrayListOf(ConstantsFixed.HELP_ID,ConstantsFixed.SAVE_ID, ConstantsFixed.EDIT_ID),
            ListQuery::class.java)
    }

    override fun initActivity() {
        helpText = Help().getHelpTitle(className)
        category = Category(this)
        category.syncQuery()
        val rtn = category.getWhere("${TableCategory.Columns.type.name} = ${ConstantsLocal.TYPE_SEARCH}" +
                " and ${TableCategory.Columns.seqno.name} < 99",TableCategory.Columns.seqno.name)
        fillList(rtn.cursor)
        rtn.cursorClose()
    }
}