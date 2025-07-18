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
import android.widget.Toast
import com.farrusco.projectclasses.common.Constants
import com.farrusco.projectclasses.common.ConstantsFixed
import com.farrusco.projectclasses.utils.TagModify
import com.farrusco.projectclasses.messages.ToastExt

class ButtonExt : androidx.appcompat.widget.AppCompatButton {
    private var tooltip: String? = null
    private var resourceid = 0

    constructor(context: Context) : super(context) {
        if (Constants.isTooltipEnabled && this.contentDescription != null){
            tooltip=this.contentDescription.toString()
            setOnLongClickListener(mLocalBtnSaveLongListener)
        }
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        if (Constants.isTooltipEnabled && this.contentDescription != null){
            tooltip=this.contentDescription.toString()
            setOnLongClickListener(mLocalBtnSaveLongListener)
        }
    }

    fun setTooltip(tooltip: Int): ButtonExt {
        if (tooltip > 0) {
            this.tooltip = context.getText(tooltip).toString()
            setOnLongClickListener(mLocalBtnSaveLongListener)
        }
        return this
    }

    var textExt: String?
        get() {
            return TagModify.getViewTagValue(this, ConstantsFixed.TagSection.TsDBValue)
        }
        set(value) {
            TagModify.setViewTagValue(this, ConstantsFixed.TagSection.TsDBValue,value)
        }

    fun setText(text: String?) {
        textExt = text
    }

    override fun setBackgroundResource(resid: Int) {
        super.setBackgroundResource(resid)
        resourceid = resid
    }

    fun getBackgroundResource(): Int {
        return resourceid
    }

    private val mLocalBtnSaveLongListener = OnLongClickListener {
        ToastExt().makeText(this.context, "Tip: $tooltip", Toast.LENGTH_SHORT).show()
        true
    }
}