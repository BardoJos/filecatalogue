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

import android.widget.TextView
import com.farrusco.filecatalogue.R
import com.farrusco.filecatalogue.business.Help
import com.farrusco.filecatalogue.tables.TableHelp
import com.farrusco.projectclasses.activity.BaseActivityTableLayout
import com.farrusco.projectclasses.common.ConstantsFixed

open class HelpMaint : BaseActivityTableLayout() {
    private lateinit var help: Help
    override val layoutResourceId: Int = R.layout.list_single
    override val mainViewId: Int = R.id.viewMain

    override fun initActivity() {
        helpText = Help().getHelpTitle(className)
        val mTitle: TextView = findViewById(R.id.title)
        mTitle.text = getText(R.string.lbltitle)
        help = Help()
        initTableLayout()
        val rtn = help.getHelp(null)
        fillList(rtn.cursor)
        rtn.cursorClose()
    }

    override fun initTableLayout() {
        addTableLayout(R.id.tblLayout,
            R.layout.line_textview1x,
            help,
            arrayOf(
                TableHelp.Columns.headertrans.name
            ),
            arrayListOf(ConstantsFixed.SAVE_ID, ConstantsFixed.EDIT_ID, ConstantsFixed.DELETE_ID, ConstantsFixed.HELP_ID),
            HelpMaintDetail::class.java)
    }

}