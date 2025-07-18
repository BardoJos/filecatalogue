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
package com.farrusco.projectclasses.widget.wheelpicker

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.farrusco.projectclasses.R
import com.farrusco.projectclasses.common.ConstantsFixed
import com.farrusco.projectclasses.utils.TagModify

class TextWheelViewHolder(private val textView: TextView) :
    BaseWheelPickerView.ViewHolder<TextWheelPickerView.Item>(textView) {
    override fun onBindData(data: TextWheelPickerView.Item) {
        textView.text = data.text
        textView.isEnabled = data.isEnabled
    }
}

class TextWheelAdapter :
    BaseWheelPickerView.Adapter<TextWheelPickerView.Item, TextWheelViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextWheelViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.wheel_picker_item, parent, false) as TextView
        return TextWheelViewHolder(view)
    }
}

class TextWheelPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseWheelPickerView(context, attrs, defStyleAttr) {
    data class Item(val id: String, val text: CharSequence, val isEnabled: Boolean = true)

    private val highlightView: View = run {
        val view = View(context)
        view.background = ContextCompat.getDrawable(context, R.drawable.text_wheel_highlight_bg)
        view
    }

    private var isHighlightingVisible: Boolean
        set(value) {
            highlightView.visibility = if (value) View.VISIBLE else View.INVISIBLE
        }
        get() {
            return highlightView.visibility == View.VISIBLE
        }

    fun setDBColumn(dBColumn: String?, dBTable: String?): TextWheelPickerView {
        TagModify.setViewTagValue(this, ConstantsFixed.TagSection.TsDBColumn, dBColumn)
        if (dBTable != null) TagModify.setViewTagValue(this, ConstantsFixed.TagSection.TsDBTable,dBTable)
        return this
    }

    init {
        colorView = ConstantsFixed.ColorBasic.Edit
        TagModify.setViewTagValue(this, ConstantsFixed.TagSection.TsForeBack.name, "f")
        addView(highlightView)
        (highlightView.layoutParams as? LayoutParams)?.apply {
            width = ViewGroup.LayoutParams.MATCH_PARENT
            height = context.resources.getDimensionPixelSize(R.dimen.text_wheel_picker_item_height)
            gravity = Gravity.CENTER_VERTICAL
        }

        attrs?.let {
            context.theme.obtainStyledAttributes(
                it,
                R.styleable.TextWheelPickerView,
                defStyleAttr,
                0
            ).apply {
                for (i in 0 until indexCount) {
                    when (getIndex(i)) {
                        R.styleable.TextWheelPickerView_highlighting_visible -> {
                            isHighlightingVisible = getBoolean(
                                R.styleable.TextWheelPickerView_highlighting_visible,
                                true
                            )
                        }
                    }
                }
            }
        }
    }
}
