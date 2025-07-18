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
import android.content.res.TypedArray
import android.util.AttributeSet
import com.farrusco.projectclasses.R
import com.farrusco.projectclasses.common.ConstantsFixed
import com.farrusco.projectclasses.utils.TagModify

class SwitchExt : androidx.appcompat.widget.SwitchCompat {

    private var skipEditTagOnce = false
    private var hideText = false
    private var fillDbColumnLabel = true

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

    constructor(context: Context?) : super(context!!) {
        colorView = ConstantsFixed.ColorBasic.Default
        TagModify.setViewTagValue(this, ConstantsFixed.TagSection.TsForeBack.name, "fb")
        this.setOnCheckedChangeListener { _, _ ->
            onCheckedChange()
        }
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initControl(context, attrs)
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initControl(context, attrs)
    }

    private fun initControl(context: Context, attrs: AttributeSet?) {
        val typedArray: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.SwitchExt)
        hideText = typedArray.getBoolean(R.styleable.SwitchExt_hideText, false)
        fillDbColumnLabel = typedArray.getBoolean(R.styleable.SwitchExt_fillDbColumnLabel, false)
        typedArray.recycle()

        colorView = ConstantsFixed.ColorBasic.Default
        if (fillDbColumnLabel){
            TagModify.setViewTagValue(this, ConstantsFixed.TagSection.TsForeBack.name, "fb")
        } else {
            TagModify.setViewTagValue(this, ConstantsFixed.TagSection.TsForeBack.name, "b")
        }
        if (hideText){
            this.text = ""
        }
        this.setOnCheckedChangeListener { _, _ ->
            onCheckedChange()
        }

    }

    var textExt: String
        get() {
            return if(this.isChecked) ConstantsFixed.STRING_TRUE else ConstantsFixed.STRING_FALSE
        }
        set(value) {
            this.isChecked = (value == ConstantsFixed.STRING_TRUE)
        }

    fun onCheckedChange(){
        if (!skipEditTagAlways) {
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
        }
        TagModify.setViewTagValue(
            this,
            ConstantsFixed.TagSection.TsDBValue.name,
            if (isChecked) ConstantsFixed.STRING_TRUE else ConstantsFixed.STRING_FALSE
        )
    }

    fun setChecked(checked: Boolean, init: Boolean){
        if (init) {
            skipEditTagOnce = true
        }
        isChecked = checked
        TagModify.setViewTagValue(this, ConstantsFixed.TagSection.TsDBValue.name, if (checked) ConstantsFixed.STRING_TRUE else ConstantsFixed.STRING_FALSE)
        if (!hideText){
            if (skipEditTagOnce){
                colorView = ConstantsFixed.ColorBasic.Default
            } else {
                this.setTextColor( colorView.color )
            }
        }
        skipEditTagOnce = false
    }
}
