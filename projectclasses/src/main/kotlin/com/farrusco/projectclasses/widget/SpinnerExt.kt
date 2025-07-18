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
@file:Suppress("unused")

package com.farrusco.projectclasses.widget

import android.content.Context
import android.database.Cursor
import android.util.AttributeSet
import android.util.SparseArray
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.isVisible
import com.farrusco.projectclasses.R
import com.farrusco.projectclasses.common.Constants
import com.farrusco.projectclasses.common.ConstantsFixed
import com.farrusco.projectclasses.databases.tables.CursorX.getColumnValueInt
import com.farrusco.projectclasses.databases.tables.CursorX.getColumnValueString
import com.farrusco.projectclasses.utils.CalcObjects
import com.farrusco.projectclasses.utils.TagModify
import com.farrusco.projectclasses.utils.ViewUtils

class SpinnerExt : androidx.appcompat.widget.AppCompatSpinner {
    private var list: ArrayList<String>? = null
    private var arrList: SparseArray<Any>? = null
    private var arrSpinnerDetail: MutableList<Constants.SpinnerDetail> = mutableListOf()
    private var currIndex: Int = -1
    private var context1: Context
    private var skipEditTagOnce = false
    val isValid: Boolean
        get() {
            if (!this.isVisible || skipEditTagAlways ){
                return true
            }
            if (list != null){
                if (this.selectedItemPosition >= 0 && this.selectedItemPosition < list!!.size) {
                    return true
                }
            }
            return false
        }

    var skipEditTagAlways: Boolean = false
        set(value) {
            field = value
            if (value) {
                tag = TagModify.deleteTagSection(tag, ConstantsFixed.TagSection.TsUserFlag.name)
            }
        }
    var colorView: ConstantsFixed.ColorBasic = ConstantsFixed.ColorBasic.Edit
        set(value) {
            if (this.children.count() > 0){
                this.children.forEach {
                    if (it::class.simpleName == ViewUtils.LegalFields.TextView.name){
                        (it as TextView).setTextColor(value.color)
                    }
                }
                (this.getChildAt(0) as TextView).setTextColor(value.color)
            }
            field = value
        }

    constructor(context: Context) : super(context) {
        this.context1 = context
        TagModify.setViewTagValue(this, ConstantsFixed.TagSection.TsForeBack.name,   "b")
        onItemSelectedListener = mSpinnerOnItemSelectedListener
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        this.context1 = context
        TagModify.setViewTagValue(this, ConstantsFixed.TagSection.TsForeBack.name,   "b")
        onItemSelectedListener = mSpinnerOnItemSelectedListener
    }

    init {
        this.context1 = context
        TagModify.setViewTagValue(this, ConstantsFixed.TagSection.TsForeBack.name,   "b")
    }

    fun setSelectionId(id: Int) {
        // problem function: can end application when changed
        setSelection(-1)
        if (arrSpinnerDetail.isEmpty()) return
        arrSpinnerDetail.forEachIndexed { index, spinnerDetail ->
            if ( spinnerDetail.id == id) {
                setSelection(index)
                currIndex = index
            }
        }
    }

    val listId: Int
        get() = CalcObjects.objectToInteger(getListColumn(currIndex, 0))

    fun getListId(pos: Int):Int{
        return getListColumn( pos,0).toString().toInt()
    }

    var tagX: Any? = null

    private fun getListColumn(location: Int, column: Int): Any? {
        if (location < 0 || location >= arrSpinnerDetail.size  ) {
            return null
        }
        val spinnerDetail = arrSpinnerDetail[location]
        when (column) {
            0 -> return spinnerDetail.id
            1 -> return spinnerDetail.description
            else -> {
                if (spinnerDetail.hiddenValues == null) return null
                if (column+2 >= spinnerDetail.hiddenValues!!.size)
                return spinnerDetail.hiddenValues!![column-2]
            }
        }
        return null
    }

    fun getText():String{
        if (getChildAt(0) == null) {
            return TagModify.getViewTagValue(this, ConstantsFixed.TagSection.TsDBValue)
        }
        return getListColumn(currIndex, 0).toString()
    }

    fun setText(text: String?, init: Boolean){
        if (init) skipEditTagOnce = true
        var mText = text
        if (text == null) mText=""

        if (getChildAt(0) != null){
            (getChildAt(0) as TextView).text = mText
        }
        if (!init && !skipEditTagAlways) {
            TagModify.setViewTagValue(this,ConstantsFixed.TagSection.TsUserFlag,ConstantsFixed.TagAction.Edit.name )
            colorView = ConstantsFixed.ColorBasic.Modified
        }
    }

    fun setModified(){
        TagModify.setViewTagValue(this,ConstantsFixed.TagSection.TsUserFlag,ConstantsFixed.TagAction.Edit.name )
        colorView = ConstantsFixed.ColorBasic.Modified
    }

    fun setItemTextSelected(text: String?){
        setSelection(-1)
        if (list != null) {
            for (i in list!!.indices) {
                if (text.equals(list!![i], true)) {
                    setSelection(i)
                    currIndex = i
                    break
                }
            }
        }
    }

    fun setItemSelected(pos: Int) {
        //if (currPos == 0) currPos = pos
        if (skipEditTagOnce){
            skipEditTagOnce=false
            //return
        } else {
            if (currIndex >= 0 && currIndex != pos && !skipEditTagAlways
                && !TagModify.hasTagValue(this,ConstantsFixed.TagSection.TsModFlag.name,ConstantsFixed.TagAction.New.name)) {
                //TagModify.setViewTagValue(this,ConstantsFixed.TagSection.TsModFlag,ConstantsFixed.TagAction.Edit.name )
                TagModify.setViewTagValue(this,ConstantsFixed.TagSection.TsUserFlag,ConstantsFixed.TagAction.Edit.name )
                colorView = ConstantsFixed.ColorBasic.Modified
            }
        }

        val tag = TagModify.getViewTagValue(this, ConstantsFixed.TagSection.TsForeBack.name)
        if (tag =="b" || tag == "fb") {

            TagModify.setViewTagValue(
                this,
                ConstantsFixed.TagSection.TsDBValue,
                getListColumn(pos, 0)
            )
        }
        currIndex = pos
    }

    fun clearItem(init: Boolean){
        this.post {
            if (init) {
                skipEditTagOnce = true
            } else if (!skipEditTagAlways) {
                TagModify.setViewTagValue(
                    this,
                    ConstantsFixed.TagSection.TsUserFlag,
                    ConstantsFixed.TagAction.Edit.name
                )
                skipEditTagOnce = true
            }
            setItemSelected(0)
            /*            currIndex = -1
                        this.setText(null,true)
                        val errorText = (this.getSelectedView() as TextView)
                        errorText.setTextColor(Color.RED)
                        errorText.error = "The field must not be empty"
                        errorText.text = "The field must not be empty"*/
        }
    }

    fun setDBColumn(dbcolumn: String?, dbtable: String?, groupno: Int): SpinnerExt {
        ViewUtils.setDBColumn(this, dbcolumn, dbtable, groupno)
        return this
    }

    fun fillSpinner(data: MutableList<Constants.SpinnerDetail>){
        arrSpinnerDetail = mutableListOf()
        skipEditTagOnce = true
        list = ArrayList()
        data.forEach {
            arrSpinnerDetail.add(it)
            list!!.add(it.description)
        }
        val dataAdapter = ArrayAdapter(context, R.layout.spinner_item, list as ArrayList<String>)
        dataAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        adapter = dataAdapter
    }

    fun fillSpinner(data: ArrayList<String>) {
        // sample:  val data = Array(10) { i -> (i+1).toString() }
        arrSpinnerDetail = mutableListOf()
        skipEditTagOnce = true
        currIndex = -1
        list = ArrayList()
        for ((x, i) in data.indices.withIndex()) {
            list!!.add(data[i])

            val spinnerDetail = Constants.SpinnerDetail()
            spinnerDetail.id = x
            spinnerDetail.description = data[i]
            arrSpinnerDetail.add(spinnerDetail)
        }
        val dataAdapter = ArrayAdapter(context, R.layout.spinner_item, list as ArrayList<String>)
        dataAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        adapter = dataAdapter
    }

    fun fillSpinner(cursor: Cursor): List<Int> {
        val listId: MutableList<Int> = mutableListOf()
        currIndex = -1
        list = ArrayList()
        arrList = SparseArray()
        if (cursor.moveToFirst()) {
            currIndex = 0
            var x = 0
            val columnCount = cursor.columnCount
            do {
                val obj = arrayOfNulls<Any>(columnCount)
                // listId.add(cursor.getInt(0));
                list!!.add(cursor.getString(1))
                for (i in 0 until columnCount) {
                    when (cursor.getType(i)) {
                        Cursor.FIELD_TYPE_NULL -> obj[i] = null
                        Cursor.FIELD_TYPE_INTEGER  -> obj[i] = cursor.getInt(i)
                        Cursor.FIELD_TYPE_FLOAT -> obj[i] = cursor.getFloat(i)
                        else -> {
                            obj[i] = cursor.getString(i)
                        }
                    }
                }
                arrList!!.put(x++, obj)
                listId.add(cursor.getInt(0))
            } while (cursor.moveToNext())
        }
        val dataAdapter = ArrayAdapter(context, R.layout.spinner_item, list as ArrayList<String>
        )
        dataAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        adapter = dataAdapter
        return listId
    }

    fun fillSpinner(cursor: Cursor, keyName:String, descriptionName: String) {
        arrSpinnerDetail = mutableListOf()
        currIndex = -1
        list = ArrayList()
        if (cursor.moveToFirst()) {
            //currIndex = 0
            do {
                val spinnerDetail = Constants.SpinnerDetail()
                spinnerDetail.id = cursor.getColumnValueInt(keyName)!!
                spinnerDetail.description = cursor.getColumnValueString(descriptionName)!!
                arrSpinnerDetail.add(spinnerDetail)
                list!!.add(spinnerDetail.description )
            } while (cursor.moveToNext())
        }

        val dataAdapter = ArrayAdapter(context, R.layout.spinner_item, list as ArrayList<String>)
        dataAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        adapter = dataAdapter
    }

    private var mSpinnerOnItemSelectedListener: OnItemSelectedListener = object : OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, v: View, pos: Int, id: Long) {
            try {
                setItemSelected(pos)
            } catch (e: NullPointerException) {
                // skip
            }
        }

        override fun onNothingSelected(arg0: AdapterView<*>?) {}
    }
}