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
package com.farrusco.projectclasses.widget.money

import android.graphics.Color
import android.icu.text.DecimalFormatSymbols
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import com.farrusco.projectclasses.R
import com.farrusco.projectclasses.widget.EditTextExt
import com.farrusco.projectclasses.widget.EditTextMoney
import com.farrusco.projectclasses.widget.TextViewExt
import com.farrusco.projectclasses.widget.TextViewMoney

class WidgetMoney {

        private lateinit var mAmount0e: EditTextMoney
        private lateinit var mAmount0v: TextViewMoney

        private var isValid = true

/*        val moneyEdit: EditTextMoney?
            get(){
                if (::mAmount0e.isInitialized){
                    return mAmount0e
                }
                isValid = false
                return null
            }
        val moneyView: TextViewMoney?
            get(){
                if (::mAmount0v.isInitialized){
                    return mAmount0v
                }
                isValid = false
                return null
            }*/

/*        fun initRowAmount (tableRow: TableRow, hdrAmount: Int, enabled: Boolean): WidgetMoney{
            if (tablerow.findViewById<View>(hdrAmount)::class.simpleName  == TableLayout::class.simpleName) {
                val mTblAmount = tablerow.findViewById<TableLayout>(hdrAmount)
                return registerFields( mTblAmount.getChildAt(0) as TableRow,enabled)
            }
            return this
        }*/
/*        fun initRowAmount (vi: Any, hdrAmount: Int, enabled: Boolean): WidgetMoney {
            if (vi is Activity){
                if (vi.findViewById<View>(hdrAmount)::class.simpleName == TableLayout::class.simpleName) {
                    val mTblAmount = vi.findViewById<TableLayout>(hdrAmount)
                    return registerFields(mTblAmount.getChildAt(0) as TableRow, enabled)
                }
            }
            if (vi is ViewGroup){
                if (vi.findViewById<View>(hdrAmount)::class.simpleName == TableRow::class.simpleName) {
                    return registerFields(vi.findViewById<TableRow>(hdrAmount),enabled)
                } else if (vi.findViewById<View>(hdrAmount)::class.simpleName == GridLayout::class.simpleName) {
                    return registerFields(vi.findViewById<GridLayout>(hdrAmount),enabled)
                }
            }
            return this
        }*/

        fun registerFields(mHdrAmount: ViewGroup, enabled: Boolean): WidgetMoney {
            //val mHdrAmount = mTblAmount.getChildAt(0) as TableRow
            var mValuta: TextView? = null
            var mSeparator: TextView? = null
            lateinit var mAmount1e: EditTextExt
            var mAmount2e: EditTextExt? = null

            lateinit var mAmount1v: TextViewExt
            var mAmount2v: TextViewExt? = null
            var isEdit = true

            isValid = false
            for (index in 0 until mHdrAmount.childCount){
                val vwName = mHdrAmount.getChildAt(index)::class.simpleName
                when (index){
                    0 -> {
                        if (vwName!!.contains("TextView",true)  || vwName == TextView::class.simpleName){
                            mValuta = mHdrAmount.getChildAt(index) as TextView?
                        }
                    }
                    1 -> {
                        if (vwName == EditTextExt::class.simpleName){
                            isEdit=true
                            mAmount1e = mHdrAmount.getChildAt(index) as EditTextExt
                        } else if (vwName == TextViewExt::class.simpleName){
                            isEdit = false
                            mAmount1v = mHdrAmount.getChildAt(index) as TextViewExt
                        }
                    }
                    2 -> {
                        if (vwName!!.contains("TextView",true) || vwName == TextView::class.simpleName){
                            mSeparator = mHdrAmount.getChildAt(index) as TextView?
                        }
                    }
                    3 -> {
                        if (isEdit && vwName == EditTextExt::class.simpleName){
                            mAmount2e = mHdrAmount.getChildAt(index) as EditTextExt
                        } else if (!isEdit && vwName == TextViewExt::class.simpleName){
                            mAmount2v = mHdrAmount.getChildAt(index) as TextViewExt
                        }
                    }
                    4 -> {
                        if (isEdit){
                            if (vwName == EditTextMoney::class.simpleName){
                                mAmount0e = mHdrAmount.getChildAt(index) as EditTextMoney
                                mAmount0e.initAmountEdit( mValuta, mSeparator, mAmount1e, mAmount2e, null, enabled)
                                isValid = true
                            }
                        } else{
                            if (vwName == TextViewMoney::class.simpleName){
                                mAmount0v = mHdrAmount.getChildAt(index) as TextViewMoney
                                mAmount0v.initAmountView( mValuta, mSeparator, mAmount1v, mAmount2v, null)
                                isValid = true
                            }
                        }
                    }
                }
            }
            return this
        }

    companion object {

        fun initAmountView(
            mValuta: TextView?, mSeparator: TextView?
            , mAmount1: TextViewExt, mAmount2: TextViewExt?
            , useValuta: Boolean, useSeparator: Boolean
            , valutaSymbol: String?, separatorSymbol: String?
        ){

            mAmount1.setBackgroundResource(R.drawable.edittext_absolute)

            mAmount1.setBackgroundResource(R.drawable.edittext_empty)
            mAmount1.setTextColor( Color.YELLOW)
            //android:theme="@style/Project.TextViewStyle"
            mAmount1.isEnabled = false
            if (mAmount2 != null) {
                mAmount2.setTextColor( Color.YELLOW)
                mAmount2.isEnabled = false
                mAmount2.setBackgroundResource(R.drawable.edittext_empty)
            }
            if (useValuta) {
                mValuta!!.setTextColor( Color.YELLOW)
                mValuta.setBackgroundResource(R.drawable.edittext_empty)
                (mValuta.layoutParams as ViewGroup.MarginLayoutParams).marginStart = 7
            } else {
                (mAmount1.layoutParams as ViewGroup.MarginLayoutParams).marginStart = 7
            }
            if (useSeparator) {
                mSeparator!!.setTextColor( Color.YELLOW)
                mSeparator.setBackgroundResource(R.drawable.edittext_empty)
                (mAmount2!!.layoutParams as ViewGroup.MarginLayoutParams).marginEnd = 7
            } else {
                (mAmount1.layoutParams as ViewGroup.MarginLayoutParams).marginEnd = 7
            }

            if (useValuta)
            {
                if (mValuta != null){
                    if (valutaSymbol == null)
                        mValuta.text = DecimalFormatSymbols.getInstance().currency.symbol
                    else
                        mValuta.text = valutaSymbol
                }

                if (useSeparator) {
                    if (separatorSymbol == null)
                        mSeparator!!.text = DecimalFormatSymbols.getInstance().decimalSeparator.toString()
                    else
                        mSeparator!!.text = separatorSymbol
                }else{
                    mAmount1.setBackgroundResource(R.drawable.edittext_decimal)
                }
            } else{
                if (useSeparator) {
                    if (separatorSymbol == null)
                        mSeparator!!.text = DecimalFormatSymbols.getInstance().decimalSeparator.toString()
                    else
                        mSeparator!!.text = separatorSymbol
                    mAmount1.setBackgroundResource( R.drawable.edittext_currency)
                }else{
                    mAmount1.setBackgroundResource(R.drawable.edittext_gradient)
                }
            }
        }

        fun initAmountEdit(mAmount0: EditTextMoney, mValuta: TextView?, mSeparator: TextView?
                       , mAmount1: EditTextExt, mAmount2: EditTextExt?
                       , useValuta: Boolean, useSeparator: Boolean
                       , valutaSymbol: String?, separatorSymbol: String?, maxDP: Int = 2
                       , enabled: Boolean){

            var bNotChange = true
            mAmount1.setBackgroundResource(R.drawable.edittext_absolute)

            if (enabled) {
                mAmount1.doOnTextChanged { _, _, _, _ ->
                    if (mAmount2 != null) mAmount2.colorView = mAmount1.colorView
                    mValuta?.setTextColor(mAmount1.colorView.color)
                    mSeparator?.setTextColor(mAmount1.colorView.color)
                    bNotChange = false
                }
                mAmount2?.doOnTextChanged { _, _, _, _ ->
                    mAmount1.colorView = mAmount2.colorView
                    mValuta?.setTextColor(mAmount2.colorView.color)
                    mSeparator?.setTextColor(mAmount2.colorView.color)
                    bNotChange=false
                }
                mAmount1.setOnFocusChangeListener { view, hasFocus ->
                    if (!hasFocus) {
                        if ((view as EditTextExt).text != null) {
                            val tmp = view.text.toString().replaceFirst("^0+(?!$)".toRegex(), "")
                            if (view.text.toString() != tmp) view.setText(tmp,true)
                            if (mAmount2 == null) {
                                mAmount0.setText(tmp, bNotChange)
                            } else{
                                if (mAmount2.text.isNullOrEmpty()) {
                                    mAmount0.setText("$tmp.0", bNotChange)
                                } else {
                                    mAmount0.setText("$tmp." + mAmount2.text, bNotChange)
                                }
                            }
                        }
                    }
                }
                mAmount2?.setOnFocusChangeListener { view, hasFocus ->
                    if (!hasFocus) {
                        val amount = (view as EditTextExt).text
                        if (amount.isNullOrEmpty()){
                            if (!mAmount1.text.isNullOrEmpty()){
                                view.setText("0".repeat(maxDP))
                            }
                        } else {
                            if (("0$amount").length < maxDP+1){
                                val tmp = amount.toString() + "0".repeat(maxDP)
                                view.setText(tmp.substring(0,maxDP))
                            }
                        }
                        if (mAmount1.text.isNullOrEmpty()){
                            mAmount0.setText( "0." + view.text, bNotChange)

                        } else {
                            mAmount0.setText(mAmount1.text.toString() + "." + view.text,bNotChange)
                        }
                    }
                }

            } else {
                mAmount1.setBackgroundResource(R.drawable.edittext_empty)
                mAmount1.setTextColor( Color.YELLOW)
                //android:theme="@style/Project.TextViewStyle"
                mAmount1.isEnabled = false
                if (mAmount2 != null) {
                    mAmount2.setTextColor( Color.YELLOW)
                    mAmount2.isEnabled = false
                    mAmount2.setBackgroundResource(R.drawable.edittext_empty)
                }
                if (useValuta) {
                    mValuta!!.setTextColor( Color.YELLOW)
                    mValuta.setBackgroundResource(R.drawable.edittext_empty)
                    (mValuta.layoutParams as ViewGroup.MarginLayoutParams).marginStart = 7
                } else {
                    (mAmount1.layoutParams as ViewGroup.MarginLayoutParams).marginStart = 7
                }
                if (useSeparator) {
                    mSeparator!!.setTextColor( Color.YELLOW)
                    mSeparator.setBackgroundResource(R.drawable.edittext_empty)
                    (mAmount2!!.layoutParams as ViewGroup.MarginLayoutParams).marginEnd = 7
                } else {
                    (mAmount1.layoutParams as ViewGroup.MarginLayoutParams).marginEnd = 7
                }
            }
            if (useValuta)
            {
                if (mValuta != null){
                    if (valutaSymbol == null)
                        mValuta.text = DecimalFormatSymbols.getInstance().currency.symbol
                    else
                        mValuta.text = valutaSymbol
                }

                if (useSeparator) {
                    if (separatorSymbol == null)
                        mSeparator!!.text = DecimalFormatSymbols.getInstance().decimalSeparator.toString()
                    else
                        mSeparator!!.text = separatorSymbol
                }else{
                    mAmount1.setBackgroundResource(R.drawable.edittext_decimal)
                }
            } else{
                if (useSeparator) {
                    if (separatorSymbol == null)
                        mSeparator!!.text = DecimalFormatSymbols.getInstance().decimalSeparator.toString()
                    else
                        mSeparator!!.text = separatorSymbol
                    mAmount1.setBackgroundResource( R.drawable.edittext_currency)
                }else{
                    mAmount1.setBackgroundResource(R.drawable.edittext_gradient)
                }
            }
        }
    }
}