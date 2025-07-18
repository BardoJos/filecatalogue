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

import com.farrusco.filecatalogue.R
import com.farrusco.filecatalogue.business.Help
import com.farrusco.filecatalogue.tables.TableHelp
import com.farrusco.projectclasses.activity.BaseActivityDetail
import com.farrusco.projectclasses.utils.ViewUtils
import com.farrusco.projectclasses.widget.EditTextExt
import com.farrusco.projectclasses.widget.TextViewExt

open class HelpMaintDetail : BaseActivityDetail() {

    override val layoutResourceId: Int = R.layout.helpmaintdetail
    override val mainViewId: Int = R.id.viewMain

    override fun initActivity() {
        val tblHelp = Help()
        helpText = tblHelp.getHelpTitle(className)
        val mViewText1 = findViewById<TextViewExt>(R.id.viewText1)
        val mEditText2 = findViewById<EditTextExt>(R.id.editText2)
        ViewUtils.setDBColumn(mViewText1,TableHelp.Columns.headertrans.name, TableHelp.TABLE_NAME, true)
        ViewUtils.setDBColumn(mEditText2,TableHelp.Columns.description.name, TableHelp.TABLE_NAME, true)
    }
}