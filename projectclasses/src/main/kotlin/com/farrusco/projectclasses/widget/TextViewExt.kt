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

import android.app.DatePickerDialog
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.util.AttributeSet
import com.farrusco.projectclasses.R
import com.farrusco.projectclasses.common.ConstantsFixed
import com.farrusco.projectclasses.utils.TagModify
import java.text.SimpleDateFormat
import java.util.*

class TextViewExt : androidx.appcompat.widget.AppCompatTextView {

    private var dateShort = false
    var colorChange = true
    var contextMenuEnabled = false
    private var displayFormat: String? = null
    var minDate: Long? = null
    private var maxDate: Long? = null

    private var year = 0
    private var monthOfYear = 0
    private var dayOfMonth = 0


    var colorView: ConstantsFixed.ColorBasic = ConstantsFixed.ColorBasic.Default
        set(value) {
            if (colorChange) {
                if (contextMenuEnabled && value.color == Color.YELLOW){
                    this.setTextColor( Color.BLACK )
                    field = ConstantsFixed.ColorBasic.Dark
                } else{
                    this.setTextColor( value.color )
                    field = value
                }
            }
        }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        if (tag == null) {
            TagModify.setViewTagValue(this, ConstantsFixed.TagSection.TsForeBack.name, "f")
        }
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        if (tag == null) {
            TagModify.setViewTagValue(this, ConstantsFixed.TagSection.TsForeBack.name, "f")
        }
        init(context, attrs)
    }
    constructor(context: Context) : super(context) {
        if (tag == null) {
            TagModify.setViewTagValue(this, ConstantsFixed.TagSection.TsForeBack.name, "f")
        }
        init(context, null)
    }

    init {
        if (tag == null || !tag.toString().contains(ConstantsFixed.ignore,true)) {
            colorView = ConstantsFixed.ColorBasic.Default
        }
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        // text(label) between edit fields in one line needs to skipped
        if (tag != null && tag.toString().contains(ConstantsFixed.ignore,true)) {
            return
        }

        val typedArray: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.TextViewExt)

        val color = typedArray.getColor(R.styleable.TextViewExt_color,0)
        contextMenuEnabled = typedArray.getBoolean(
            R.styleable.TextViewExt_textContextMenu,
            false
        )
        if (color != 0) this.setTextColor( color )
        colorChange = typedArray.getBoolean(R.styleable.TextViewExt_colorChange,true)
        displayFormat = (android.text.format.DateFormat.getDateFormat(context) as SimpleDateFormat).toPattern()

        if (typedArray.getInt(
                R.styleable.TextViewExt_textformat,
                TEST_REGEXP
            ) and TEST_DATESHORT != 0) {
            dateShort=true
            if (contextMenuEnabled) {
                this.setTextColor(Color.BLACK)
                showDatePicker()
            }
        }
        typedArray.recycle()
    }

    internal inner class TextDisplayer(tv: TextViewExt, v: String? ) : Runnable {
        private var value: String? = v
        private var textview: TextViewExt = tv
        override fun run() {
            if (value != null) textview.textExt = value else textview.textExt = ""
        }
    }

    var textExt: String?
        get() {
            return this.text.toString()
        }
        set(value) {
            year=0
            this.text = value
        }


    companion object {
        private const val TEST_REGEXP = 0x0001
        private const val TEST_DATESHORT = 0x0800
    }

    fun setTextFormat(textx: String?) {
        year=0
        if (textx == null){
            this.text = ""
        } else if (dateShort && displayFormat != "") {
            if (textx.length == 10){
                var sep = "-"
                if (textx.indexOf(sep) < 0) sep = "/"
                if (textx.indexOf(sep) < 0) sep = ":"
                if (textx.substring(4,5) == sep && textx.substring(7,8) == sep){
                    // 2023-01-25
                    // 0123456789
                    year = textx.substring(0,4).toInt()
                    monthOfYear = textx.substring(5,7).toInt()
                    dayOfMonth = textx.substring(8,10).toInt()
                } else if (textx.substring(2,3) == sep && textx.substring(5,6) == sep){
                    // 25-01-2023
                    // 0123456789
                    year = textx.substring(6,10).toInt()
                    monthOfYear = textx.substring(3,5).toInt()
                    dayOfMonth = textx.substring(0,2).toInt()
                } else {
                    this.text = textx
                    return
                }
                val sdf = SimpleDateFormat(displayFormat, Locale.getDefault())
                val cal = Calendar.getInstance()
                cal.set(year,monthOfYear-1,dayOfMonth)
                this.text = sdf.format(cal.time)
            } else {
                this.text = textx
            }
        } else{
            this.text = textx
        }
    }

    fun setDBColumn(dBColumn: String?, dBTable: String?): TextViewExt {
        TagModify.setViewTagValue(this, ConstantsFixed.TagSection.TsDBColumn,dBColumn)
        if (dBTable != null) TagModify.setViewTagValue(this, ConstantsFixed.TagSection.TsDBTable,dBTable)
        return this
    }

    fun setDBColumn(dBColumn: String?, dBTable: String?, groupno: Int): TextViewExt {
        setDBColumn(dBColumn, dBTable)
        TagModify.setViewTagValue(this, ConstantsFixed.TagSection.TsGroupno,groupno.toString())
        return this
    }

    private fun showDatePicker() {
        this.text = SimpleDateFormat(displayFormat,Locale.getDefault()).format(System.currentTimeMillis())

        val cal = Calendar.getInstance()

        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            this.year = year
            this.monthOfYear = monthOfYear
            this.dayOfMonth = dayOfMonth
            cal.set(year,monthOfYear,dayOfMonth)
            val sdf = SimpleDateFormat(displayFormat, Locale.getDefault())
            this.text = sdf.format(cal.time)
            this.setTextColor(Color.BLUE)
            TagModify.setViewTagValue(this,ConstantsFixed.TagSection.TsUserFlag, ConstantsFixed.TagAction.Edit.name)
        }
        this.setOnClickListener {
            val dialog = DatePickerDialog(
                this.context, R.style.Project_DatePickerStyle, dateSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            try{
                if (this.year > 0 && this.monthOfYear > 0 && this.dayOfMonth > 0) {
                    dialog.datePicker.updateDate(this.year, this.monthOfYear - 1, this.dayOfMonth)
                }
            } catch (_:Exception) {
                // lazy
            }
            if (minDate != null){
                dialog.datePicker.minDate = minDate!!
            }
            if (maxDate != null){
                dialog.datePicker.maxDate = maxDate!!
            }
            dialog.show()
        }
    }

}