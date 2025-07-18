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

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.farrusco.projectclasses.R
import com.farrusco.projectclasses.common.ConstantsFixed
import com.farrusco.projectclasses.messages.ToastExt
import com.farrusco.projectclasses.utils.ContentValuesExt
import com.farrusco.projectclasses.utils.ReturnValue
import com.farrusco.projectclasses.utils.ViewUtils

abstract class BaseActivityDetail : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpdatePrevScreen(true)
        addHiddenFields(intent, viewMain  as ViewGroup, groupNo)
        val map = ContentValuesExt.copyBundleContentValues(intent.extras)
        ViewUtils.copyContentValuesToViewGroup(
             map,
             viewMain as ViewGroup,
             groupNo
        )
    }

    private fun addHiddenFields(intent: Intent, vi: View, groupno: Int) {
        val keys = intent.extras?.keySet()
        val arrChildDbNames = ArrayList<String?>()
        val arrChildDBColumns: ArrayList<View> =
            ViewUtils.getChildDBColumns(vi as ViewGroup, groupno)
        for (i in arrChildDBColumns.indices) {
            arrChildDbNames.add(ViewUtils.getDBColumn(arrChildDBColumns[i]))
        }

        if (keys != null) {
            for (key in keys) {
                if (ConstantsFixed.tagSections.contains(",${key.lowercase()},")){
                    continue
                }
                if (!arrChildDbNames.contains(key)
                ) {
                    ViewUtils.addHiddenField(
                        vi,
                        key!!,
                        intent.getStringExtra(key),
                        groupno
                    )
                }
            }
        }
    }

    override fun saveScreen(): ReturnValue {
        val rtn: ReturnValue = validateScreen()
        if (!rtn.returnValue) {
            return rtn
        }

        when (currentModi) {
            ConstantsFixed.ScreenModi.ModeBrowse -> {
                intent.putExtra(
                    ConstantsFixed.TagSection.TsModFlag.name,
                    ConstantsFixed.TagAction.Browse.name
                )
            }
            ConstantsFixed.ScreenModi.ModeEdit -> {
                intent.putExtra(
                    ConstantsFixed.TagSection.TsModFlag.name,
                    ConstantsFixed.TagAction.Edit.name
                )
            }
            ConstantsFixed.ScreenModi.ModeDelete -> {
                intent.putExtra(
                    ConstantsFixed.TagSection.TsModFlag.name,
                    ConstantsFixed.TagAction.Delete.name
                )
            }
            ConstantsFixed.ScreenModi.ModeNew -> {
                intent.putExtra(
                    ConstantsFixed.TagSection.TsModFlag.name,
                    ConstantsFixed.TagAction.New.name
                )
            }
            ConstantsFixed.ScreenModi.ModeInsert -> {
                intent.putExtra(
                    ConstantsFixed.TagSection.TsModFlag.name,
                    ConstantsFixed.TagAction.Insert.name
                )
            }
            else -> {intent.putExtra(
                "?",
                "?"
            )}
        }
        if (ViewUtils.hasChildTag((viewMain as ViewGroup?)!!,
                ConstantsFixed.TagSection.TsUserFlag.name,
                ConstantsFixed.TagAction.Edit.name)){
            intent.putExtra(
                ConstantsFixed.TagSection.TsUserFlag.name,
                ConstantsFixed.TagAction.Edit.name
            )
        }
        if (ViewUtils.hasChildTag((viewMain as ViewGroup?)!!,
                ConstantsFixed.TagSection.TsUserFlag.name,
                ConstantsFixed.TagAction.Delete.name)){
            intent.putExtra(
                ConstantsFixed.TagSection.TsUserFlag.name,
                ConstantsFixed.TagAction.Delete.name
            )
        }
        val cv = ViewUtils.copyViewGroupToContentValues((viewMain as ViewGroup))
        cv.keySet().forEach {
            intent.putExtra(it,cv[it].toString())
        }
        ToastExt().makeText(this, R.string.mess002_saved, Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK, intent)
        return rtn
    }
}