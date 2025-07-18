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

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.icu.text.DecimalFormatSymbols
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import com.farrusco.projectclasses.R
import com.farrusco.projectclasses.common.ConstantsFixed
import com.farrusco.projectclasses.utils.TagModify
import com.farrusco.projectclasses.widget.money.WidgetMoney

class TextViewMoney : androidx.appcompat.widget.AppCompatTextView {
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
    private lateinit var mAmount1: TextViewExt
    private lateinit var mAmount2: TextViewExt
    private var maxNumberOfDecimalDigits:Int? = null
    private var maxDP = 0
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

        val typedArray: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.TextViewMoney)
        maxNumberOfDecimalDigits = typedArray.getInt(R.styleable.TextViewMoney_maxNumberOfDecimalDigitsView,0)

        typedArray.recycle()

        TagModify.setViewTagValue(this, ConstantsFixed.TagSection.TsForeBack.name, "f")
    }

    var colorView: ConstantsFixed.ColorBasic = ConstantsFixed.ColorBasic.Edit
        set(value) {
            this.setTextColor( value.color )
            field = value
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
            if (this::mAmount1.isInitialized) mAmount1.setTextColor( value.color )
            if (this::mAmount2.isInitialized) mAmount2.setTextColor( value.color )
            if (this::mSeparator.isInitialized) mSeparator.setTextColor( value.color )
            if (this::mValuta.isInitialized) return mValuta.setTextColor( value.color )
        }

    @Suppress("unused")
    fun initAmountView(
        context: Context,
        mValuta: Int?,
        mSeparator: Int?,
        mAmount1: Int,
        mAmount2: Int?,
        maxDP: Int?
    ){
        val mAmount11 = (context as Activity).findViewById<TextViewExt>(mAmount1)
        var mValuta2: TextView? = null
        var mSeparator2: TextView? = null
        var mAmount21: TextViewExt? = null
        if (mValuta != null) mValuta2 = context.findViewById(mValuta)
        if (mSeparator != null) mSeparator2 = context.findViewById(mSeparator)
        if (mAmount2 != null) mAmount21 = context.findViewById(mAmount2)

        initAmountView(mValuta2, mSeparator2, mAmount11, mAmount21, maxDP)
    }

    fun initAmountView(
        mValuta: TextView?,
        mSeparator: TextView?,
        mAmount1: TextViewExt,
        mAmount2: TextViewExt?,
        maxDP: Int?
    ) {

        val currencySymbol = DecimalFormatSymbols.getInstance().currency.symbol
        val decimalSeparator = DecimalFormatSymbols.getInstance().decimalSeparator.toString()

        var useValuta = false
        var useSeparator = false
        if (mValuta != null){
            useValuta = true
            this.mValuta=mValuta
        }
        if (mSeparator != null) {
            useSeparator = true
            if (maxDP == null) {
                if (maxNumberOfDecimalDigits == null) this@TextViewMoney.maxDP =2 else this@TextViewMoney.maxDP =maxNumberOfDecimalDigits!!
            } else{
                this@TextViewMoney.maxDP =maxDP
            }

            this.mSeparator=mSeparator
            if (mAmount2 != null) this.mAmount2=mAmount2
        }
        this.mAmount1=mAmount1
        WidgetMoney.initAmountView(
            mValuta,
            mSeparator,
            mAmount1,
            mAmount2,
            useValuta,
            useSeparator,
            currencySymbol,
            decimalSeparator
        )
    }

    override fun getVisibility(): Int {
        return visibilityExt
    }

    override fun setVisibility(value:Int) {
        visibilityExt=value
    }

    private var visibilityExt: Int
        get(){
            if (this::mAmount1.isInitialized) return mAmount1.visibility
            return VISIBLE
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
        super.setText(textx)
        amountOld = getNumericValue()
        skipEditTagAlways=bSkip
    }

    fun setDBColumn(dBColumn: String?, dBTable: String?): TextViewMoney {
        TagModify.setViewTagValue(this, ConstantsFixed.TagSection.TsDBColumn,dBColumn)
        if (dBTable != null) TagModify.setViewTagValue(this, ConstantsFixed.TagSection.TsDBTable,dBTable)
        return this
    }

    fun setDBColumn(dBColumn: String?, dBTable: String?, groupno: Int): TextViewMoney {
        setDBColumn(dBColumn, dBTable)
        TagModify.setViewTagValue(this, ConstantsFixed.TagSection.TsGroupno,groupno.toString())
        return this
    }

    private fun getNumericValue(): Double {
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

    @SuppressLint("SetTextI18n")
    override fun setText(text: CharSequence?, type: BufferType?) {
        super.setText(text, type)
        if (this::mAmount1.isInitialized) {
            val prijsStr = text.toString().split(".")
            mAmount1.text = prijsStr[0]
            if (this::mAmount2.isInitialized ) {
                if (prijsStr.size > 1)
                    mAmount2.text = (prijsStr[1] + "0".repeat(maxDP)).substring(0,maxDP)
                else
                    mAmount2.text = "0".repeat(maxDP)
            }
            amountOld = getNumericValue()
        }
    }
}