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
import android.graphics.Color
import android.icu.text.DecimalFormatSymbols
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import com.farrusco.projectclasses.R
import com.farrusco.projectclasses.common.ConstantsFixed
import com.farrusco.projectclasses.utils.TagModify
import com.farrusco.projectclasses.widget.money.WidgetMoney

class EditTextMoney : androidx.appcompat.widget.AppCompatEditText {
    var skipEditTagOnce = false
    var skipEditTagAlways: Boolean = false
        set(value) {
            field = value
            if (value) {
                tag = TagModify.deleteTagSection(tag, ConstantsFixed.TagSection.TsUserFlag.name)
            }
        }

    private lateinit var mValuta: TextView
    private lateinit var mSeparator: TextView
    private lateinit var mAmount1: EditTextExt
    private lateinit var mAmount2: EditTextExt
    private var maxNumberOfDecimalDigits:Int? = null
    private var maxdp = 0
    private var amountOld = 0.0
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        initControl(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initControl(context, attrs)
    }

    constructor(context: Context) : super(context) {
        initControl(context, null)
    }

    private fun initControl(context: Context, attrs: AttributeSet?) {

        val typedArray: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.EditTextMoney)
        maxNumberOfDecimalDigits = typedArray.getInt(R.styleable.EditTextMoney_maxNumberOfDecimalDigits,0)

        typedArray.recycle()
        TagModify.setViewTagValue(this, ConstantsFixed.TagSection.TsForeBack.name, "f")

        addTextChangedListener(errorValidateTextWatcher)
    }

    var visibilityValuta: Int = VISIBLE
        get(){
            if (this::mValuta.isInitialized && field != mValuta.visibility) {
                mValuta.visibility = field
            }
            return field
        }
        set (value) {
            if (this::mValuta.isInitialized) {
                mValuta.visibility = value
            }
            field = value
        }

    var colorView: ConstantsFixed.ColorBasic = ConstantsFixed.ColorBasic.Edit
        set(value) {
            this.setTextColor( value.color )
            field = value
            if (this::mAmount1.isInitialized) {
                mAmount1.setTextColor( value.color )
                if (mAmount1.isEnabled){
                    setBackgroundColor(Color.WHITE)
                    if (value.color != Color.BLUE) {
                        this.setTextColor(ConstantsFixed.ColorBasic.Dark.color)
                        field = ConstantsFixed.ColorBasic.Dark
                    }
                } else {
                    setBackgroundColor(Color.BLACK)
                    if (value.color == Color.BLACK) {
                        this.setTextColor(ConstantsFixed.ColorBasic.Edit.color)
                        field = ConstantsFixed.ColorBasic.Edit
                    }
                }
                if (this::mAmount2.isInitialized) mAmount2.setTextColor( value.color )
                if (this::mSeparator.isInitialized) mSeparator.setTextColor( value.color )
                if (this::mValuta.isInitialized) return mValuta.setTextColor( value.color )
            }
        }

/*    fun initAmountEdit(
        context: Context,
        mValuta: Int?,
        mSeparator: Int?,
        mAmount1: Int,
        mAmount2: Int?,
        maxDP: Int?,
        mEnable: Boolean
    ){
        val mAmount11 = (context as Activity).findViewById<EditTextExt>(mAmount1)
        var mValuta2: TextView? = null
        var mSeparator2: TextView? = null
        var mAmount21: EditTextExt? = null
        if (mValuta != null) mValuta2 = context.findViewById(mValuta)
        if (mSeparator != null) mSeparator2 = context.findViewById(mSeparator)
        if (mAmount2 != null) mAmount21 = context.findViewById(mAmount2)

        initAmountEdit(mValuta2, mSeparator2, mAmount11, mAmount21, maxDP, mEnable)
    }*/

    fun initAmountEdit(
        mValuta: TextView?,
        mSeparator: TextView?,
        mAmount1: EditTextExt,
        mAmount2: EditTextExt?,
        maxDP: Int?,
        mEnable: Boolean
    ) {

        val currencySymbol = DecimalFormatSymbols.getInstance().currency.symbol
        val decimalSeparator = DecimalFormatSymbols.getInstance().decimalSeparator.toString()

        var useValuta = false
        var useSeparator = false
        if (mValuta != null) {
            if (visibilityValuta == View.VISIBLE) {
                useValuta = true
                this.mValuta = mValuta
            } else {
                useValuta = false
                this.mValuta = mValuta
                this.mValuta.visibility = visibilityValuta
            }
        }
        if (mSeparator != null) {
            useSeparator = true
            @Suppress("LiftReturnOrAssignment")
            if (maxDP == null) {
                if (maxNumberOfDecimalDigits == null) maxdp=2 else maxdp =maxNumberOfDecimalDigits!!
            } else{
                maxdp=maxDP
            }

            this.mSeparator=mSeparator
            if (mAmount2 != null) this.mAmount2=mAmount2
        }
        this.mAmount1=mAmount1
        WidgetMoney.initAmountEdit(
            this,
            mValuta,
            mSeparator,
            mAmount1,
            mAmount2,
            useValuta,
            useSeparator,
            currencySymbol,
            decimalSeparator,
            maxdp,
            mEnable
        )
    }

    override fun getVisibility(): Int {
        return visibilityExt
    }

    override fun setVisibility(value:Int) {
        visibilityExt=value
    }

/*    var enabledExt: Boolean
        get(){
            if (this::mAmount1.isInitialized) return mAmount1.isEnabled
            return false
        }
        set(value){
            if (this::mValuta.isInitialized) mValuta.isEnabled=value
            if (this::mSeparator.isInitialized) mSeparator.isEnabled=value
            if (this::mAmount1.isInitialized) mAmount1.isEnabled=value
            if (this::mAmount2.isInitialized) mAmount2.isEnabled=value
        }*/

    private var visibilityExt: Int
        get(){
            if (this::mAmount1.isInitialized) return mAmount1.visibility
            return View.VISIBLE
        }
        set(value){
            if (this::mValuta.isInitialized) mValuta.visibility=value
            if (this::mSeparator.isInitialized) mSeparator.visibility=value
            if (this::mAmount1.isInitialized) mAmount1.visibility=value
            if (this::mAmount2.isInitialized) mAmount2.visibility=value
        }

    var textExt: String?
        get() {
            return getNumericValue().toString()
        }
        set(value) {
            super.setText(value)
        }

    fun setText(textx: String?, init: Boolean) {
        val bSkip = skipEditTagAlways
        if (init) {
            skipEditTagAlways = true
        }
        // when focus changed this is also triggered and color blue with edit tag
        // so test value
        if (textx == null || this.text == null || textx != this.text.toString()){
            super.setText(textx)
        }
        amountOld = getNumericValue()
        skipEditTagAlways=bSkip
    }

    fun setDBColumn(dBColumn: String?, dBtable: String?): EditTextMoney {
        TagModify.setViewTagValue(this, ConstantsFixed.TagSection.TsDBColumn,dBColumn)
        if (dBtable != null) TagModify.setViewTagValue(this, ConstantsFixed.TagSection.TsDBTable,dBtable)
        return this
    }

    fun setDBColumn(dBColumn: String?, dBtable: String?, groupno: Int): EditTextMoney {
        setDBColumn(dBColumn, dBtable)
        TagModify.setViewTagValue(this, ConstantsFixed.TagSection.TsGroupno,groupno.toString())
        return this
    }

    private val errorValidateTextWatcher: TextWatcher = object : TextWatcher {
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            if (s.isNotEmpty() && error != null) {
                error = null
            }
            if (skipEditTagAlways){
                tag = TagModify.deleteTagSection(tag, ConstantsFixed.TagSection.TsUserFlag.name)
            } else if (skipEditTagOnce) {
                skipEditTagOnce = false
                tag = TagModify.deleteTagSection(tag, ConstantsFixed.TagSection.TsUserFlag.name)
            } else {
                if (amountOld != getNumericValue()){
                    tag = TagModify.setTagValue(tag, ConstantsFixed.TagSection.TsUserFlag.name,ConstantsFixed.TagAction.Edit.name)
                }
            }
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: Editable) {}
    }

    fun getNumericValue(): Double {
        try {
            if (this::mAmount1.isInitialized) {
                var price = mAmount1.text.toString()
                if (this::mAmount2.isInitialized ) {
                    price += "." + mAmount2.text.toString()
                }
                return price.toDouble()
            }
            return text.toString().toDouble()
        } catch (_: Exception){
            return 0.0
        }
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        super.setText(text, type)
        getText()?.length?.let { setSelection(it) }
        if (this::mAmount1.isInitialized) {
            val prijsStr = text.toString().split(".")
            mAmount1.setText(prijsStr[0],true)
            if (this::mAmount2.isInitialized ) {
                if (prijsStr.size > 1)
                    mAmount2.setText((prijsStr[1] + "0".repeat(maxdp)).substring(0,maxdp), true)
                else
                    mAmount2.setText("0".repeat(maxdp), true)
            }
            amountOld = getNumericValue()
        }
    }
}