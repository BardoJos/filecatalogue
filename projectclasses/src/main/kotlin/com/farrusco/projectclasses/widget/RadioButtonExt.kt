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
package com.farrusco.projectclasses.widget

import android.content.Context
import android.util.AttributeSet
import com.farrusco.projectclasses.common.ConstantsFixed
import com.farrusco.projectclasses.utils.TagModify

class RadioButtonExt : androidx.appcompat.widget.AppCompatRadioButton {
    var skipEditTagOnce = false
    var skipEditTagAlways: Boolean = false
        set(value) {
            field = value
            if (value) {
                tag = TagModify.deleteTagSection(tag, ConstantsFixed.TagSection.TsUserFlag.name)
            }
        }

    var colorView: ConstantsFixed.ColorBasic = ConstantsFixed.ColorBasic.Default
        set(value) {
            this.setTextColor( value.color )
            field = value
        }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context!!,
        attrs,
        defStyle
    ) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {
        init()
    }

    constructor(context: Context?) : super(context!!) {
        init()
    }

    fun setChecked(checked: Boolean, init: Boolean): RadioButtonExt {
        if (isChecked != checked) {
            if (init) skipEditTagOnce = true
            isChecked = checked
        }
        return this
    }

    init {
        colorView = ConstantsFixed.ColorBasic.Default
    }

    private fun init() {
        TagModify.setViewTagValue(this, ConstantsFixed.TagSection.TsForeBack.name,   "b")
    }

    override fun setOnCheckedChangeListener(listener: OnCheckedChangeListener?) {
        super.setOnCheckedChangeListener(listener)
        checkedChange()
    }

    private fun checkedChange() {
        if (skipEditTagOnce) {
            skipEditTagOnce = false
        } else {
            TagModify.setViewTagValue(
                this,
                ConstantsFixed.TagSection.TsUserFlag.name,
                ConstantsFixed.TagAction.Edit.name
            )
            colorView = ConstantsFixed.ColorBasic.Edit
        }
        TagModify.setViewTagValue(
            this,
            ConstantsFixed.TagSection.TsDBValue.name,
            if (isChecked) ConstantsFixed.STRING_TRUE else ConstantsFixed.STRING_FALSE
        )
    }
}