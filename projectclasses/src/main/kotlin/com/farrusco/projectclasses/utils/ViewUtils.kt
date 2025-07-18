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

@file:Suppress("MemberVisibilityCanBePrivate")

package com.farrusco.projectclasses.utils

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.Switch
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.widget.ContentFrameLayout
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly
import androidx.core.view.children
import androidx.core.view.forEach
import com.farrusco.projectclasses.BuildConfig
import com.farrusco.projectclasses.R
import com.farrusco.projectclasses.common.Constants
import com.farrusco.projectclasses.common.ConstantsFixed
import com.farrusco.projectclasses.widget.ButtonExt
import com.farrusco.projectclasses.widget.CheckBoxExt
import com.farrusco.projectclasses.widget.EditTextExt
import com.farrusco.projectclasses.widget.EditTextMoney
import com.farrusco.projectclasses.widget.ImageViewExt
import com.farrusco.projectclasses.widget.RadioButtonExt
import com.farrusco.projectclasses.widget.SpinnerExt
import com.farrusco.projectclasses.widget.SwitchExt
import com.farrusco.projectclasses.widget.TextViewExt
import com.farrusco.projectclasses.widget.TextViewMoney
import com.farrusco.projectclasses.widget.money.WidgetMoney
import com.farrusco.projectclasses.widget.tablayout.TabStripExt
import com.farrusco.projectclasses.widget.wheelpicker.TextWheelPickerView
import java.io.File
import androidx.core.view.isVisible
import androidx.core.view.isNotEmpty

@Suppress("unused")
object ViewUtils {

    enum class LegalFields {
        SwitchExt, Switch, CheckBoxExt, EditTextExt, EditText, SpinnerExt, TextView, TextViewExt, ButtonExt,
        SeekBarExt, RadioButtonExt, ImageView, ImageViewExt, TextWheelPickerView,
        EditTextMoney, TextViewMoney, AppCompatImageView, Invalid
    }

    fun isValidObject(obj: Any): Boolean {
        return enumValues<LegalFields>().find { obj::class.simpleName == it.name } != null
    }

    fun menuIconWithText(context: Context, resource: Int, title: String): CharSequence  {
        val draw = ContextCompat.getDrawable(context, resource)!!
        return menuIconWithText(draw, title)
    }

    private fun menuIconWithText(draw: Drawable, title: String): CharSequence  {
        draw.setBounds(0, 0, 70, 70)
        val sb = SpannableString("    $title")
        val imageSpan = ImageSpan(draw, ImageSpan.ALIGN_BOTTOM)
        sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return sb
    }

    fun validateEditTextNulls(vi: ViewGroup?): ReturnValue {
        var rtn = ReturnValue()
        rtn.returnValue=true
        for (i in 0 until vi!!.childCount) {
            if (vi.getChildAt(i) is ViewGroup) {
                rtn = validateEditTextNulls(vi.getChildAt(i) as ViewGroup)
                if (!rtn.returnValue) return rtn
            }
            when (vi.getChildAt(i).javaClass){
                EditTextExt::class.java -> {
                    if (vi.getChildAt(i).isVisible &&
                        !(vi.getChildAt(i) as EditTextExt).isValid){
                        val fieldName = TagModify.getViewTagValue(vi.getChildAt(i), ConstantsFixed.TagSection.TsMessColumn)
                        if (fieldName.isEmpty()) {
                            rtn.setArrParams(arrayListOf(vi.context.getText(R.string.text).toString()))
                        } else {
                            rtn.setArrParams(arrayListOf(fieldName))
                        }
                        rtn.returnValue = false
                        return rtn
                    }
                }
                SpinnerExt::class.java -> {
                    if (vi.getChildAt(i).isVisible &&
                        !(vi.getChildAt(i) as SpinnerExt).isValid){
                        val fieldName = TagModify.getViewTagValue(vi.getChildAt(i), ConstantsFixed.TagSection.TsMessColumn)
                        if (fieldName.isEmpty()) {
                            rtn.setArrParams(arrayListOf(vi.context.getText(R.string.spinner).toString()))
                        } else {
                            rtn.setArrParams(arrayListOf(fieldName))
                        }
                        rtn.returnValue = false
                        return rtn
                    }
                }
            }
        }
        return rtn
    }

    fun hasChildTag(vi: ViewGroup, tag: String, tagvalue: String = ""): Boolean {

        var namex = ""
        var tagx: Any? = null
        var rtn = false

        val visited: ArrayList<View> = ArrayList()
        val unvisited: MutableList<View> = mutableListOf()
        unvisited.add(vi)
        while (unvisited.isNotEmpty()) {
            val child = unvisited.removeAt(0)
            if (tagvalue.isEmpty()) {
                if (TagModify.getViewTagValue(child, tag).isNotEmpty()) {
                    namex = child::class.simpleName.toString()
                    tagx = if (namex == SpinnerExt::class.simpleName) (child as SpinnerExt).tagX else child.tag
                    rtn = true
                    break
                }
            } else {
                if (TagModify.hasTagValue(child, tag, tagvalue)) {
                    namex = child::class.simpleName.toString()
                    tagx = if (namex == SpinnerExt::class.simpleName) (child as SpinnerExt).tagX else child.tag
                    rtn = true
                    break
                }
            }
            visited.add(child)
            if (child !is ViewGroup) continue
            child.children.forEach { unvisited.add(it) }
        }

        if (BuildConfig.DEBUG && rtn && tag == ConstantsFixed.TagSection.TsUserFlag.name){
            if (tagx == null) tagx="---"
            Logging.d("ViewUtils.hasChildTag","Search: <$tag=$tagvalue> Class: <$namex>, tag: <$tagx>")
        }
        return rtn
    }

    fun View.getBackgroundColor() = (background as? ColorDrawable?)?.color ?: Color.TRANSPARENT

    fun disableChildBrowse(vi: ViewGroup){
        val arrChilds = getAllChildren(vi)
        if (arrChilds.isNotEmpty()) {
            arrChilds.forEach {
                when (it::class.simpleName) {
                        LegalFields.EditTextExt.name ->
                            (it as EditTextExt).isEnabled = false
                        LegalFields.EditText.name ->
                            (it as EditText).isEnabled = false
                        LegalFields.CheckBoxExt.name ->
                            (it as CheckBoxExt).isEnabled = false
                        LegalFields.RadioButtonExt.name ->
                            (it as RadioButton).isEnabled = false
                        LegalFields.SwitchExt.name ->
                            (it as SwitchExt).isEnabled = false
                        LegalFields.Switch.name ->
                            (it as Switch).isEnabled = false
                        LegalFields.TextWheelPickerView.name ->
                            (it as TextWheelPickerView).isEnabled = false
                        LegalFields.SpinnerExt.name ->
                            (it as Spinner).isEnabled = false
                        LegalFields.EditTextMoney.name ->
                            (it as EditTextMoney).isEnabled = false
                    }
            }
        }
    }

    fun resetChildTagUserFlag(vi: ViewGroup, tagToFind: String) {
        resetChildTag(vi, ConstantsFixed.TagSection.TsUserFlag.name, tagToFind)
    }

    fun resetChildTagModFlag(vi: ViewGroup, tagToFind: String) {
        resetChildTag(vi, ConstantsFixed.TagSection.TsModFlag.name, tagToFind)
    }

    fun removeChildTag(vi: ViewGroup, sectionsToFind: Array<String>) {
        val arrChilds = getAllChildren(vi)
        sectionsToFind.forEach { section ->
            if (TagModify.hasTagSection( vi, section)) {
                TagModify.deleteViewTagSection(vi,section)
            }
        }
        if (arrChilds.isNotEmpty()) {
            arrChilds.forEach {
                sectionsToFind.forEach { section ->
                    if (TagModify.hasTagSection( it,section)) {
                        TagModify.deleteViewTagSection(it,section)
                    }
                    if (TagModify.hasTagSection( it,section)) {
                        TagModify.deleteViewTagSection(
                            it,
                            section
                        )
                    }
                }
                resetChildColor(it)
            }
        }
    }

    private fun resetChildTag(vi: ViewGroup, sectionToFind: String, tagToFind: String) {
        val arrChilds = getAllChildren(vi)
        if (TagModify.hasTagValue( vi, sectionToFind, tagToFind)) {
            TagModify.deleteViewTagSection(vi,sectionToFind)
        }
        if (arrChilds.isNotEmpty()) {
            arrChilds.forEach {
                if (TagModify.hasTagValue( it,sectionToFind, tagToFind)) {
                    TagModify.deleteViewTagSection(it,sectionToFind)
                }
                if (TagModify.hasTagSection( it,sectionToFind)) {
                    TagModify.deleteViewTagSection(
                        it,
                        sectionToFind
                    )
                }
                resetChildColor(it)
            }
        }
    }

    private fun resetChildColor(vi: View) {
        when (vi::class.simpleName) {
            LegalFields.TextViewExt.name ->
                (vi as TextViewExt).colorView =
                    ConstantsFixed.ColorBasic.Default
            LegalFields.TextView.name ->
                (vi as TextView).setTextColor(ConstantsFixed.ColorBasic.Default.color)
            LegalFields.EditText.name ->
                (vi as EditText).setTextColor(ConstantsFixed.ColorBasic.Dark.color)
            LegalFields.EditTextExt.name ->
                (vi as EditTextExt).setTextColor(ConstantsFixed.ColorBasic.Dark.color)
            LegalFields.CheckBoxExt.name ->
                (vi as CheckBoxExt).setTextColor(ConstantsFixed.ColorBasic.Default.color)
            LegalFields.RadioButtonExt.name ->
                (vi as RadioButton).setTextColor(ConstantsFixed.ColorBasic.Dark.color)
            LegalFields.SwitchExt.name ->
                (vi as SwitchExt).colorView =
                    ConstantsFixed.ColorBasic.Default
            LegalFields.Switch.name ->
                (vi as Switch).setTextColor(ConstantsFixed.ColorBasic.Default.color)
            LegalFields.TextWheelPickerView.name ->
                (vi as TextWheelPickerView).colorView = ConstantsFixed.ColorBasic.Dark
            LegalFields.SpinnerExt.name -> {
                if ((vi as Spinner).isNotEmpty()) {
                    try {
                        (vi.getChildAt(0) as TextView).setTextColor(
                            Color.BLACK
                        )
                    } catch (e: Exception) {
                        //INFO TODO lazy
                    }
                }
            }
            LegalFields.EditTextMoney.name -> {
                if ((vi as EditTextMoney).isEnabled) {
                    vi.colorView =
                        ConstantsFixed.ColorBasic.Dark
                } else {
                    vi.colorView =
                        ConstantsFixed.ColorBasic.Default
                }
            }
        }
    }

    fun setChildTags(vi: ViewGroup, tagmod: String, tagvalue: String?) {
        val arrChilds = getAllChildren(vi)
        if (arrChilds.isNotEmpty()) {
            arrChilds.forEach { TagModify.setViewTagValue(it, tagmod, tagvalue) }
        }
    }

    private fun setViewText(vi: View, text: String){
        when (vi::class.simpleName) {
            LegalFields.EditText.name -> {
                (vi as EditText).setText(text)
            }
            LegalFields.EditTextExt.name -> {
                (vi as EditTextExt).setText(text)
            }
            LegalFields.TextView.name -> {
                (vi as TextView).text = text
            }
            LegalFields.TextViewExt.name -> {
                (vi as TextViewExt).setTextFormat(text)
            }
            LegalFields.CheckBoxExt.name -> {
                (vi as CheckBoxExt).text = text
            }
            LegalFields.EditTextMoney.name -> {
                (vi as EditTextMoney).textExt = text
            }
            LegalFields.TextViewMoney.name -> {
                (vi as TextViewMoney).textExt = text
            }
            LegalFields.TextWheelPickerView.name -> {
                (vi as TextWheelPickerView).setText(text, true)
            }
            else -> {
                TagModify.setViewTagValue(vi, ConstantsFixed.TagSection.TsDBValue, text)
            }
        }
    }

    fun setChildDelete(context: Context, vi: ViewGroup, blnAll: Boolean?) {
        val txtDelete = "** " + context.getString(R.string.delete) + " **"
        val arrChilds = getAllChildren(vi)
        if (arrChilds.isNotEmpty()) {
            for (row in 0 until arrChilds.count()) {
                if (arrChilds[row].isVisible) {
                    // move value to background because text 'delete' is set
                    //
                    with (arrChilds[row]){
                        val dbcolumn = TagModify.getViewTagValue(this,ConstantsFixed.TagSection.TsDBColumn.name )
                        TagModify.setViewTagValue(this,ConstantsFixed.TagSection.TsForeBack.name, "b" )
                        TagModify.setViewTagValue(this,ConstantsFixed.TagSection.TsDBColumnBack.name, dbcolumn )
                        TagModify.deleteViewTagSection(this,ConstantsFixed.TagSection.TsDBColumn.name )
                    }

                    when (arrChilds[row]::class.simpleName) {
                        LegalFields.EditText.name -> {
                            with (arrChilds[row] as EditText){
                                TagModify.setViewTagValue(this,ConstantsFixed.TagSection.TsDBValue.name, this.text )
                                this.setText( txtDelete)
                            }
                            if (!blnAll!!) break
                        }
                        LegalFields.EditTextExt.name -> {
                            with (arrChilds[row] as EditTextExt){
                                TagModify.setViewTagValue(this,ConstantsFixed.TagSection.TsDBValue.name, this.text )
                                this.setText( txtDelete)
                            }
                            if (!blnAll!!) break
                        }
                        LegalFields.TextView.name -> {
                            with (arrChilds[row] as TextView){
                                TagModify.setViewTagValue(this,ConstantsFixed.TagSection.TsDBValue.name, this.text )
                                this.text = txtDelete
                            }
                            if (!blnAll!!) break
                        }
                        LegalFields.TextViewExt.name -> {
                            with (arrChilds[row] as TextViewExt){
                                TagModify.setViewTagValue(this,ConstantsFixed.TagSection.TsDBValue.name, this.text )
                                this.text = txtDelete
                            }
                            if (!blnAll!!) break
                        }
                    }
                }
            }
            arrChilds.forEach {
                TagModify.setViewTagValue(it, ConstantsFixed.TagSection.TsUserFlag.name,
                    ConstantsFixed.TagAction.Delete.name)
            }
        }
        setChildFocusable(vi,false)
    }

    private fun setChildText(vi: ViewGroup, text: String?, blnAll: Boolean?) {
        val arrChilds = getAllChildren(vi)
        if (arrChilds.isNotEmpty()) {
            arrChilds.forEach {
                if (it.isVisible) {
                    when (it::class.simpleName) {
                        LegalFields.EditText.name -> {
                            (it as EditText).setText(text)
                            if (!blnAll!!) return
                        }
                        LegalFields.EditTextExt.name -> {
                            (it as EditTextExt).setText(text)
                            if (!blnAll!!) return
                        }
                        LegalFields.TextView.name -> {
                            (it as TextView).text = text
                            if (!blnAll!!) return
                        }
                        LegalFields.TextViewExt.name -> {
                            (it as TextViewExt).text = text
                            if (!blnAll!!) return
                        }
                        else -> {
                            // lazy
                        }
                    }
                }
            }
        }
    }

    fun setFirstChildText(vi: ViewGroup, text: String?) {
        setChildText(vi, text, false)
    }

    private fun setChildFocusable(vi: ViewGroup, blnFocusAble: Boolean) {
        val arrChilds = getAllChildren(vi)
        if (arrChilds.isNotEmpty()) {
            arrChilds.forEach {
                if (it.visibility == 0) {
                    when (it::class.simpleName) {
                        LegalFields.EditTextExt.name -> if (blnFocusAble) {
                            (it as EditTextExt).isFocusableInTouchMode = true
                            it.isEnabled = true
                        } else {
                            (it as EditTextExt).isFocusable = false
                            it.isEnabled = false
                        }
                        LegalFields.EditText.name -> if (blnFocusAble) {
                            (it as EditText).isFocusableInTouchMode = true
                            it.isEnabled = true
                        } else {
                            (it as EditText).isFocusable = false
                            it.isEnabled = false
                        }
                        LegalFields.CheckBoxExt.name -> if (blnFocusAble) {
                            (it as CheckBoxExt).isFocusableInTouchMode = true
                            it.isEnabled = true
                        } else {
                            (it as CheckBoxExt).isFocusable = false
                            it.isEnabled = false
                        }
                        LegalFields.SwitchExt.name -> if (blnFocusAble) {
                            (it as SwitchExt).isFocusableInTouchMode = true
                            it.isEnabled = true
                        } else {
                            (it as SwitchExt).isFocusable = false
                            it.isEnabled = false
                        }
                        LegalFields.RadioButtonExt.name -> if (blnFocusAble) {
                            (it as RadioButton).isFocusableInTouchMode = true
                            it.isEnabled = true
                        } else {
                            (it as RadioButton).isFocusable = false
                            it.isEnabled = false
                        }
                        LegalFields.SpinnerExt.name -> if (blnFocusAble) {
                            (it as SpinnerExt).isFocusableInTouchMode = true
                            it.isEnabled = true
                        } else {
                            (it as SpinnerExt).isFocusable = false
                            it.isEnabled = false
                        }
                        else -> {
                            // lazy
                        }
                    }
                }
            }
        }
    }

    fun setFocusFirstChild(vi: ViewGroup): Boolean {
        val arrChilds = getAllChildren(vi)
        if (arrChilds.isNotEmpty()) {
            arrChilds.forEach {
                if (it.visibility == 0) {
                    when (it::class.simpleName) {
                        LegalFields.EditText.name -> if ((it as EditText).isEnabled) {
                            it.requestFocus()
                            return true
                        }
                        LegalFields.EditTextExt.name -> if ((it as EditTextExt).isEnabled) {
                            it.requestFocus()
                            return true
                        }
                        LegalFields.CheckBoxExt.name -> if ((it as CheckBoxExt).isEnabled) {
                            it.requestFocus()
                            return true
                        }
                        LegalFields.SwitchExt.name -> if ((it as Switch).isEnabled) {
                            it.requestFocus()
                            return true
                        }
                        LegalFields.RadioButtonExt.name -> if ((it as RadioButton).isEnabled) {
                            it.requestFocus()
                            return true
                        }
                        LegalFields.SpinnerExt.name -> if ((it as SpinnerExt).isEnabled) {
                            it.requestFocus()
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    private fun getChildTag(
        vi: ViewGroup,
        tagSection: String?
    ): ArrayList<View> {
        val obj1 = ArrayList<View>()
        val arrChilds = getAllChildren(vi)
        if (arrChilds.isNotEmpty()) {
            arrChilds.forEach {
                if (isValidObject(it) ) {
                    if (it.tag == null || !"${ConstantsFixed.ignore},${ConstantsFixed.popupmenu}".contains( it.tag.toString() )){
                        if (tagSection == null || TagModify.hasTagSection(it, tagSection)) {
                            obj1.add(it)
                        }
                    }
                }
            }
        }
        return obj1
    }

    private fun getChildDBColumns(vi: ViewGroup): ArrayList<View> {
        return getChildDBColumns(vi, -1)
    }

    fun getChildDBColumns(
        vi: ViewGroup,
        groupno: Int
    ): ArrayList<View> {
        val obj = ArrayList<View>()
        val arrChilds = getAllChildren(vi,true)
        if (arrChilds.isNotEmpty()) {
            arrChilds.forEach {
                if (!(getDBColumn(it).isNullOrEmpty() && getDBColumnBack(it).isNullOrEmpty())
                    && (groupno <= 0 || getGroupno(it) == groupno)
                ) {
                    obj.add(it)
                }
            }
        }
        return obj
    }

    private fun getAllChildren(v: View, dbonly:Boolean = false): ArrayList<View> {
        val visited: ArrayList<View> = ArrayList()
        return getAllChildrenRecursive(v,dbonly ,visited)
    }

    private fun getAllChildrenRecursive(
        v: View,
        dbonly: Boolean = false,
        visited: ArrayList<View>
    ): ArrayList<View> {
        var visitedRtn = visited
        if (dbonly) {
            if (TagModify.hasTagSection(v, ConstantsFixed.TagSection.TsDBColumn.name) ||
                TagModify.hasTagSection(v, ConstantsFixed.TagSection.TsDBColumnBack.name)
            ) {
                visitedRtn.add(v)
            }
        } else {
            visitedRtn.add(v)
        }

        if (v is ViewGroup) {
            v.children.forEach {
                if (it is ViewGroup) {
                    visitedRtn = getAllChildrenRecursive(it, dbonly, visitedRtn)
                } else {
                    if (dbonly) {
                        if (TagModify.hasTagSection(
                                it,
                                ConstantsFixed.TagSection.TsDBColumn.name
                            ) ||
                            TagModify.hasTagSection(
                                it,
                                ConstantsFixed.TagSection.TsDBColumnBack.name
                            )
                        ) {
                            visitedRtn.add(it)
                        }
                    } else if (it::class.simpleName == LegalFields.TextWheelPickerView.name ||
                        it::class.simpleName == LegalFields.SpinnerExt.name ||
                        it.parent::class.simpleName == TabStripExt::class.simpleName) {
                        // skip
                    } else {
                        visitedRtn.add(it)
                    }
                }
            }
        }

        return visitedRtn
    }

    fun findViewWithTag(vi: View, tag: String): View? {
        if (vi.tag != null && vi.tag.toString().contains(tag)) return vi
        if (vi is ViewGroup) {
            for (c in vi.children) {
                if (c.tag != null && c.tag.toString().contains(tag)) return c
                if (c is ViewGroup) return findViewWithTag(c, tag)
            }
        }
        return null
    }

    fun getTableLayout(vi: View): View? {
        var viParent = vi
        while (viParent.javaClass != TableLayout::class.java) {
            if (viParent.parent == null) return null
            viParent = viParent.parent as View
        }
        return viParent
    }

    private fun getTableRowParent(vi: View, findTag: String? = null): TableRow? {
        if (vi is TableRow) {
            if (findTag == null || TagModify.hasTagSection(vi, findTag)) {
                return vi
            }
        }
        if (vi.parent == null || vi.parent::class.simpleName == ContentFrameLayout::class.simpleName) {
            if (findTag == null) {
                Logging.d("getTableRowParent: cannot find tablerow")
            } else {
                Logging.d("getTableRowParent: Forget to add $findTag at row")
            }
            return null
        }
        return getTableRowParent(vi.parent as View, findTag)
    }

    fun getTableRow(vi: View): TableRow? {
        return getTableRowParent(vi)
    }

    fun getTableRowParentLineId(vi: View): TableRow? {
        return getTableRowParent(vi,ConstantsFixed.TagSection.TsLineId.name)
    }

    fun findClass(vi: View, cls: String): View? {
        if (vi::class.simpleName == cls) return vi
        if (vi is ViewGroup) {
            for (c in vi.children) {
                if (c::class.simpleName == cls) return c
                if (c is ViewGroup) return findClass(c, cls)
            }
        }
        return null
    }

    fun findMoneyId(vi: View, className: String, id: Int): View? {
        if (vi is ViewGroup) {
            val tr = vi.findViewById<View>(id) as TableRow
            tr.forEach {
                if (it::class.simpleName.equals(className,true)){
                    return it
                }
            }
        }
        return null
    }

    private fun findClassId(vi: View, id: Int): View? {
        if (vi.id == id) return vi
        if (vi is ViewGroup) {
            for (c in vi.children) {
                if (c.id == id) return c
                if (c is ViewGroup) return findClassId(c, id)
            }
        }
        return null
    }

    fun findClassParent(vi: View, cls: String): View? {
        if (vi::class.simpleName == cls) return vi
        if (vi.parent != null) {
            return findClassParent(vi.parent as View, cls)
        }
        return null
    }

    fun getChildDBColumnValue(vi: ViewGroup, dbcolumn: String): String? {
        return getChildDBColumnValue(vi, dbcolumn, -1)
    }

    private fun getChildDBColumnValue(vi: ViewGroup, dbcolumn: String, groupNo: Int): String? {
        // get values from hidden fields
        val arrDB = getChildDBColumns(vi, groupNo)
        arrDB.forEach {
            if (getDBColumn(it) == dbcolumn) {
                return getDBValueView(it)
            }
        }
        Logging.w(
            "getChildDBColumnValue","Column: $dbcolumn, group: $groupNo not found."
        )
        return null
    }

    fun getChildValue(vi: ViewGroup, dbcolumn: String): String? {
        return getChildValue(vi, dbcolumn, -1)
    }

    fun getChildValue(vi: ViewGroup, dbcolumn: String, groupNo: Int): String? {
        // get value from hidden fields
        val arrDB = getChildDBColumns(vi, groupNo)
        if (arrDB.isNotEmpty()){
            arrDB.forEach {
                if (getDBColumn(it) != null && getDBColumn(it) == dbcolumn) {
                    return getStringView(it)
                }
            }
        }
        Logging.e(
            "getChildValue","Column: $dbcolumn, group: $groupNo not found.")
        return null
    }

    fun setChildValue(vi: ViewGroup, dbcolumn: String, value: String?) {
        // set value to hidden fields
        val arrDB = getChildDBColumns(vi, -1)
        arrDB.forEach {
            if (getDBColumn(it) == dbcolumn) {
                return setViewString(it, value, false, label = false)
            }
        }
        Logging.e(
            "setChildValue","Column: $dbcolumn not found.")
    }

    fun setChildDBColumnValue(vi: ViewGroup, dbcolumn: String, value: String?) {
        setChildDBColumnValue(vi, dbcolumn, value, -1)
    }

    private fun setChildDBColumnValue(vi: ViewGroup, dbcolumn: String, value: String?, groupNo: Int) {
        // set value to hidden fields
        val arrDB = getChildDBColumns(vi, groupNo)
        arrDB.forEach {
            if (getDBColumn(it) == dbcolumn) {
                setViewString(it, value, false, label = false)
                return
            }
        }
        Logging.w(
            "setChildDBColumnValue",
            "Column: %$dbcolumn, value: $value not found."
        )
    }

    fun getAllChilds(vi: ViewGroup): ArrayList<View> {
        return getChildTag(vi, null)
    }

/*    fun copyCursorToBundle(cursor: Cursor, bundle: Bundle?)
    {
        val arrChildDBColumns = cursor.columnNames
        for (x in arrChildDBColumns.indices) {
            bundle?.putString(arrChildDBColumns[x], cursor.getString(x))
        }
    }*/

    fun copyCursorToContentValues(cursor: Cursor, values: ContentValues) {
        val arrChildDBColumns = cursor.columnNames
        for (x in arrChildDBColumns.indices) {
            values.put(arrChildDBColumns[x], cursor.getString(x))
        }
    }

    fun copyCursorToViewGroup(cursor: Cursor, v: ViewGroup?, groupno: Int = -1) {
        if (v != null){
            val values = ContentValues()
            copyCursorToContentValues(cursor, values)
            copyContentValuesToViewGroup(values, v, groupno)
            TagModify.setViewTagValue(v, ConstantsFixed.TagSection.TsModFlag, ConstantsFixed.TagAction.Edit.name )
        }
    }

    fun addHiddenField(v: ViewGroup, dbcolumn: String, dbValue: Any?, groupno: Int = -1){
        if (ConstantsFixed.tagSections.contains(",${dbcolumn.lowercase()},")){
            // this cannot be a dbcolumn
            return
        }
        val test = TextViewExt(v.context)
        TagModify.setViewTagValue(test, ConstantsFixed.TagSection.TsDBColumn.name,dbcolumn)
        if (groupno > 0) {
            TagModify.setViewTagValue(test, ConstantsFixed.TagSection.TsGroupno.name, groupno.toString())
        }
        test.visibility = View.GONE
        test.text = dbValue?.toString() ?: ""
        TagModify.setViewTagValue(test, ConstantsFixed.TagSection.TsDBValue.name, dbValue)
        v.addView(test)
    }

/*    fun copyBundleToViewGroup(bundle: Bundle?, v: ViewGroup, groupno: Int = -1) {
        if (bundle != null){
            copyContentValuesToViewGroup(ContentValuesExt.copyBundleContentValues(bundle), v, groupno)
        }
    }*/

    fun copyContentValuesToViewGroup(values: ContentValues, v: ViewGroup, groupno: Int = -1) {
        val obj = getChildDBColumns(v, groupno)
        if (obj.isEmpty()) {
            if (v !is ScrollView) {
                values.valueSet().forEach { valSet ->
                    valSet.key
                    valSet.value
                    //INFO TODO scroll view cannot contain more than one element
                    addHiddenField(v, valSet.key, valSet.value, groupno)
                }
            }
            return
        }

        if (values.containsKey(ConstantsFixed.TagSection.TsUserFlag.name)) {
            val mUserFlag = values.getAsString(ConstantsFixed.TagSection.TsUserFlag.name)
            TagModify.setViewTagValue(v, ConstantsFixed.TagSection.TsUserFlag, mUserFlag )
        }

        values.valueSet().forEach { valSet ->
            valSet.key
            valSet.value
            var bOk = false
            obj.forEach { dbCol ->
                //val tag = TagModify.getViewTagValue(dbCol, ConstantsFixed.TagSection.TsForeBack.name)
                if ((valSet.key.equals(getDBColumnFore(dbCol),true) ||
                            valSet.key.equals(getDBColumnBack(dbCol),true))
                    && (groupno <= 0 || getGroupno(dbCol) == groupno)
                ) {
                    bOk = true
                    if (valSet.key.equals(getDBColumnFore(dbCol),true)) {
                        try {
                            setViewString(dbCol, valSet.value?.toString(), true, label = true)
                        } catch (e: Exception) {
                            Logging.d("copyContentValuesToViewGroup error dbColumn: ${valSet.key}")
                        }
                    }
                    if (valSet.key.equals(getDBColumnBack(dbCol),true))  {
                        setViewTag(dbCol, valSet.value?.toString())
                        when (dbCol::class.simpleName){

                            CheckBox::class.simpleName -> {
                                (dbCol as CheckBox).isChecked = valSet.value.toString() == ConstantsFixed.STRING_TRUE
                            }
                            CheckBoxExt::class.simpleName -> {
                                (dbCol as CheckBoxExt).setChecked(valSet.value.toString() == ConstantsFixed.STRING_TRUE, true)
                            }
                            SwitchExt::class.simpleName -> {
                                if(valSet.value != null){
                                    (dbCol as SwitchExt).setChecked(valSet.value.toString() == ConstantsFixed.STRING_TRUE, true)
                                }
                            }
                            RadioButtonExt::class.simpleName -> {(dbCol as RadioButtonExt).setChecked(valSet.value.toString() == ConstantsFixed.STRING_TRUE, true)}
                            SeekBar::class.simpleName -> {(dbCol as SeekBar).progress = CalcObjects.objectToInteger(valSet.value)}

                            SpinnerExt::class.simpleName ->  {
                                if (valSet.value.toString().isEmpty()){
                                    if (!(dbCol as SpinnerExt).skipEditTagAlways){
                                        dbCol.clearItem(true)
                                    }
                                } else if (valSet.value.toString().isDigitsOnly()) {
                                    (dbCol as SpinnerExt).setSelectionId(
                                        CalcObjects.objectToInteger(valSet.value)
                                    )
                                } else{
                                    (dbCol as SpinnerExt).setItemTextSelected(valSet.value.toString())
                                }
                            }
                            Spinner::class.simpleName ->  {
                                @Suppress("UNCHECKED_CAST")
                                val pos = ((dbCol as Spinner).adapter as ArrayAdapter<String>).getPosition(valSet.value.toString())
                                dbCol.setSelection(pos)
                            }
                            ImageView::class.simpleName -> {
                                val file = File(valSet.value.toString())
                                if (file.exists()) {
                                    val bm = CalcObjects.decodeFile(file, (dbCol as ImageView).height)!!
                                    dbCol.setImageBitmap(bm)
                                }
                            }
                        }
                    }
                }
            }
            if (!bOk && v !is ScrollView) {
                //INFO TODO scroll view cannot contain more than one element
                addHiddenField(v, valSet.key, valSet.value, groupno)
            }
        }

    }

/*    fun setColorToViewGroup(bundle: Bundle?, v: ViewGroup, groupno: Int = -1) {
        if (bundle != null){
            setColorToViewGroup(ContentValuesExt.copyBundleContentValues(bundle), v, groupno)
        }
    }*/
    /*
        make fields white to indicate that this row is modified
     */
    fun setColorToViewGroup(values: ContentValues, v: ViewGroup, groupno: Int = -1) {
        val obj = getChildDBColumns(v, groupno)
        if (obj.isEmpty()) return

        var mModFlag = ConstantsFixed.TagAction.Edit.name
        var mUserFlag = ""
        if (values.containsKey(ConstantsFixed.TagSection.TsModFlag.name)) {
            mModFlag = values.getAsString(ConstantsFixed.TagSection.TsModFlag.name)!!
        }

        if (values.containsKey(ConstantsFixed.TagSection.TsUserFlag.name)) {
            mUserFlag = values.getAsString(ConstantsFixed.TagSection.TsUserFlag.name)
        }
        // in case of "NEW" or user "EDIT" color whole line of table white
        if (mModFlag == ConstantsFixed.TagAction.New.name || mUserFlag == ConstantsFixed.TagAction.Edit.name){
            obj.forEach {
                when (it.javaClass){
                    EditTextExt::class.java -> (it as EditTextExt).colorView = ConstantsFixed.ColorBasic.Edit
                    TextViewExt::class.java -> (it as TextViewExt).colorView = ConstantsFixed.ColorBasic.Edit
                }
            }
        }
    }

    private fun getStringView(vi: View): String {
        return when (vi::class.simpleName) {
            LegalFields.EditTextExt.name -> (vi as EditTextExt).textExt.toString()
            LegalFields.EditText.name -> (vi as EditText).text.toString()
            LegalFields.TextViewExt.name -> (vi as TextViewExt).textExt.toString()
            LegalFields.TextView.name -> (vi as TextView).text.toString()
            LegalFields.CheckBoxExt.name -> (vi as CheckBoxExt).textExt
            LegalFields.SwitchExt.name -> (vi as SwitchExt).textExt
            LegalFields.Switch.name -> (vi as Switch).isChecked.toString()
            LegalFields.RadioButtonExt.name -> (vi as RadioButton).text.toString()
            LegalFields.SpinnerExt.name -> (vi as SpinnerExt).listId.toString()
            LegalFields.TextWheelPickerView.name -> (vi as TextWheelPickerView).textExt
            LegalFields.ImageViewExt.name -> (vi as ImageViewExt).getText()
            LegalFields.EditTextMoney.name -> (vi as EditTextMoney).textExt.toString()
            LegalFields.TextViewMoney.name -> (vi as TextViewMoney).textExt.toString()
            LegalFields.ButtonExt.name -> (vi as ButtonExt).textExt.toString()
            else -> {
                ""
            }
        }
    }

    private fun getDBValueView(vi: View): String {
        val tag = TagModify.getViewTagValue(vi, ConstantsFixed.TagSection.TsForeBack.name)
        if (tag in listOf("b","fb")) {
            return TagModify.getViewTagValue(vi, ConstantsFixed.TagSection.TsDBValue)
        }
        return getStringView(vi)
    }

    fun getValueCursor(cursor: Cursor, name: String): String {
        val i = cursor.getColumnIndex(name)
        if (i < 0) return ""
        return try{
            cursor.getString(i)
        } catch (e: Exception) {
            ""
        }
    }

    fun getValueCursorInt(cursor: Cursor, name: String, dflt: Int = 0): Int {
        val rtn = getValueCursor(cursor, name)
        if (rtn == "") return dflt
        return CalcObjects.stringToInteger(rtn)
    }

    fun getValueCursorFloat(cursor: Cursor, name: String, dflt: Float = 0f): Float {
        val rtn = getValueCursor(cursor, name)
        if (rtn == "") return dflt
        return CalcObjects.objectToFloat(rtn)
    }

    private fun setViewString(vi: View, value: String?, init: Boolean, label: Boolean) {
        try {
            when (vi::class.simpleName) {
                LegalFields.EditTextExt.name -> {
                    (vi as EditTextExt).setText(value, init)
                }
                LegalFields.ButtonExt.name -> (vi as ButtonExt).setText(value)
                LegalFields.EditText.name -> (vi as EditText).setText(value)
                LegalFields.TextView.name -> (vi as TextView).text = value
                LegalFields.TextViewExt.name -> (vi as TextViewExt).setTextFormat(value)
                LegalFields.CheckBoxExt.name -> {
                    if (label){ // label text not internal value
                        (vi as CheckBoxExt).text = value
                    } else{
                        (vi as CheckBoxExt).textExt = StringUtils.isEmpty(value, "0").toString()
                    }
                }
                LegalFields.RadioButtonExt.name -> (vi as RadioButton).text = value
                LegalFields.SwitchExt.name -> {
                    if (label){ // label text not internal value
                        (vi as SwitchExt).text = value
                    } else{
                        (vi as SwitchExt).textExt = StringUtils.isEmpty(value, "0").toString()
                    }
                } // label text not internal value
                LegalFields.EditTextMoney.name -> (vi as EditTextMoney).setText(value, init)
                LegalFields.TextWheelPickerView.name -> (vi as TextWheelPickerView).setText(value, init)
                LegalFields.TextViewMoney.name -> {
                    (vi as TextViewMoney).textExt = value
                }
                LegalFields.SpinnerExt.name -> {
                    if ((vi as Spinner).getChildAt(0) != null) {
                        (vi.getChildAt(0) as TextView).text = value
                    } else {
                        @Suppress("UNCHECKED_CAST") val pos =
                            ((vi as SpinnerExt).adapter as ArrayAdapter<String>).getPosition(value)
                        if (pos >= 0) {
                            (vi as Spinner).setSelection(pos)
                        }
                    }
                }
                LegalFields.ImageViewExt.name -> (vi as ImageViewExt).setText(value, true)
                else -> {
                    Logging.i("ViewUtils.setViewString", "Unknown: " + vi::class.simpleName)
                    // throw IllegalArgumentException("setViewString/View is invalid")
                }
            }
        } catch (e: Exception) {
            if (e.message != null && e.message!!.contains("Only the original thread that created", true)) {
                // Only the original thread that created a view hierarchy can touch its views.
                when (vi::class.simpleName) {
                    LegalFields.TextViewExt.name -> {
                        (vi as TextViewExt).TextDisplayer(vi, value).run()
                    }
                else -> {
                    Logging.d(Constants.APP_NAME + "/ViewUtils.setViewString(1):\n" + e.stackTraceToString())
                    }
                }
            } else {
                Logging.d(Constants.APP_NAME + "/ViewUtils.setViewString(2):\n" + e.stackTraceToString())
            }
        }
    }

    fun setTextMaxLength(vi: View, tableName: String) {
        if (vi::class.simpleName == LegalFields.EditTextExt.name && (vi as EditTextExt).setMaxLength == 0) {
            val column = getDBColumn(vi)
            if (column != null) {
                val key = "$tableName.$column".lowercase()
                if (Constants.dbStructure.containsKey(key)){
                    val value = Constants.dbStructure.getValue(key)
                    if (value != null && value.len > 0) {
                        vi.setMaxLength = value.len
                    }
                }
            }
        }
    }

    private fun setViewTag(vi: View, value: String?) {
        val action = ConstantsFixed.TagSection.TsDBValue
        when (vi::class.simpleName) {
            LegalFields.EditTextExt.name -> (vi as EditTextExt).tag =
                TagModify.setTagValue(vi.tag, action.name, value)
            LegalFields.EditText.name -> (vi as EditText).tag =
                TagModify.setTagValue(vi.tag, action.name, value)
            LegalFields.TextView.name -> (vi as TextView).tag =
                TagModify.setTagValue(vi.tag, action.name, value)
            LegalFields.TextViewExt.name -> (vi as TextViewExt).tag =
                TagModify.setTagValue(vi.tag, action.name, value)
            LegalFields.CheckBoxExt.name -> (vi as CheckBoxExt).tag =
                TagModify.setTagValue(vi.tag, action.name, value)
            LegalFields.SwitchExt.name -> (vi as SwitchExt).tag =
                TagModify.setTagValue(vi.tag, action.name, value)
            LegalFields.RadioButtonExt.name -> (vi as RadioButtonExt).tag =
                TagModify.setTagValue(vi.tag, action.name, value)
            LegalFields.SpinnerExt.name -> {
                TagModify.setViewTagValue(vi, action.name, value)
            }
            LegalFields.ImageViewExt.name -> {
                TagModify.setViewTagValue(vi, action.name, value)
            }
            LegalFields.TextWheelPickerView.name -> {
                TagModify.setViewTagValue(vi, action.name, value)
            }
            else -> {
                Logging.i("ViewUtils.setViewTag","Unknown: " + vi::class.simpleName)
            }
        }
    }

    fun getDBColumn(vi: Any?): String? {
        if (vi != null) {
            val tag = TagModify.getViewTagValue(vi, ConstantsFixed.TagSection.TsForeBack.name)
            return if (tag in listOf("b","fb")) {
                getDBColumnBack(vi)
            } else{
                getDBColumnFore(vi)
            }
        }
        return null
    }

    fun getDBColumnFore(vi: Any?): String? {
        if (vi != null) {
            return TagModify.getViewTagValue(vi, ConstantsFixed.TagSection.TsDBColumn.name)
        }
        return null
    }


    fun getDBColumnBack(vi: Any?): String? {
        if (vi != null) {
            return TagModify.getViewTagValue(vi, ConstantsFixed.TagSection.TsDBColumnBack.name)
        }
        return null
    }

    fun getGroupno(vi: Any?): Int {
        if (vi != null) {
            val group: String =
                TagModify.getViewTagValue(vi, ConstantsFixed.TagSection.TsGroupno.name)
            return CalcObjects.stringToInteger(group)
        }
        return 0
    }

    fun setDBColumn(vi: View?, dbcolumn: String?, tableName: String? = null, setLength: Boolean = false): View? {
        if (vi != null) {
            if ( !dbcolumn.isNullOrEmpty()) {
                setDBColumn(vi, dbcolumn, tableName, 0)
            }
            moneyRegister(vi)
            if (setLength && !tableName.isNullOrEmpty()) {
                setTextMaxLength(vi, tableName)
            }
        }
        return vi
    }

    fun moneyRegister(vi: View?){
        if (vi != null) {
            when (vi::class.simpleName){
                LegalFields.EditTextMoney.name -> {
                    // this call take care of displaying the correct value
                    WidgetMoney().registerFields(vi.parent as TableRow,true)
                }
                LegalFields.TextViewMoney.name -> {
                    // this call take care of displaying the correct value
                    WidgetMoney().registerFields(vi.parent as TableRow,false)
                }
            }
        }
    }

    fun setDBColumn(vi: View?, dbcolumn: String?, dbtable: String?, groupno: Int?): View? {
        if (vi != null) {
            val tag = TagModify.getViewTagValue(vi, ConstantsFixed.TagSection.TsForeBack.name)
            if (tag in listOf("b","fb")) {
                TagModify.setViewTagValue(vi, ConstantsFixed.TagSection.TsDBColumnBack, dbcolumn)
                if (dbtable != null) TagModify.setViewTagValue(vi, ConstantsFixed.TagSection.TsDBTableBack, dbtable)
            } else {
                TagModify.setViewTagValue(vi, ConstantsFixed.TagSection.TsDBColumn, dbcolumn)
                if (dbtable != null) TagModify.setViewTagValue(vi, ConstantsFixed.TagSection.TsDBTable, dbtable)
            }
            moneyRegister(vi)
            if (groupno != null && groupno >= 0) {
                TagModify.setViewTagValue(vi, ConstantsFixed.TagSection.TsGroupno, groupno)
            }
        }
        return vi
    }

    fun setDBValue(vi: View?, dbValue: String?): View? {
        if (vi != null) {
            val tag = TagModify.getViewTagValue(vi, ConstantsFixed.TagSection.TsForeBack.name)
            if (tag in listOf("b","fb")) {
                TagModify.setViewTagValue(vi, ConstantsFixed.TagSection.TsDBValue, dbValue)
            } else {
                if (dbValue.isNullOrEmpty()) {
                    setViewText(vi, "")
                } else {
                    setViewText(vi, dbValue)
                }
            }
        }
        return vi
    }

    @JvmOverloads
    fun copyViewGroupToView(v1: ViewGroup, v2: ViewGroup, groupno: Int = 0) {
        val map = copyViewGroupToContentValues(v1, groupno)
        copyContentValuesToViewGroup(map, v2, groupno)
    }

    @JvmOverloads
    fun copyViewGroupToIntent(v: ViewGroup, intent: Intent, groupno: Int = 0) {
        if (TagModify.hasTagSection(v, ConstantsFixed.TagSection.TsModFlag.name)){
            val tg = TagModify.getViewTagValue(v, ConstantsFixed.TagSection.TsModFlag.name)
            intent.putExtra(ConstantsFixed.TagSection.TsModFlag.name, tg)
        }
        if (hasChildTag(v, ConstantsFixed.TagSection.TsUserFlag.name, ConstantsFixed.TagAction.Edit.name)){
            intent.putExtra(ConstantsFixed.TagSection.TsUserFlag.name, ConstantsFixed.TagAction.Edit.name)
        }
        val obj = getChildDBColumns(v, groupno)
        obj.forEach {
            var dbcolumn = getDBColumn(it).toString()
            val value: String
            val tag = TagModify.getViewTagValue(it, ConstantsFixed.TagSection.TsForeBack.name)
            if (tag in listOf("b","fb")) {
                dbcolumn = TagModify.getViewTagValue(it, ConstantsFixed.TagSection.TsDBColumnBack)
                value = getDBValueView(it)
            } else {
                value = getStringView(it)
            }
            intent.putExtra(dbcolumn, value)
        }
    }

    fun copyViewGroupToContentValues(v: ViewGroup, groupno: Int = 0): ContentValues {
        val contentvalues = ContentValues()
        if (TagModify.hasTagSection(v, ConstantsFixed.TagSection.TsModFlag.name)){
            val tg = TagModify.getViewTagValue(v, ConstantsFixed.TagSection.TsModFlag.name)
            contentvalues.put(ConstantsFixed.TagSection.TsModFlag.name, tg)
        }
        if (hasChildTag(v, ConstantsFixed.TagSection.TsUserFlag.name, ConstantsFixed.TagAction.Edit.name)){
            contentvalues.put(ConstantsFixed.TagSection.TsUserFlag.name, ConstantsFixed.TagAction.Edit.name)
        }
        val obj = getChildDBColumns(v, groupno)
        obj.forEach {
            var dbcolumn = getDBColumn(it).toString()
            val value: String
            val tag = TagModify.getViewTagValue(it, ConstantsFixed.TagSection.TsForeBack.name)
            if (tag in listOf("b","fb")) {
                dbcolumn = TagModify.getViewTagValue(it, ConstantsFixed.TagSection.TsDBColumnBack)
                value = getDBValueView(it)
            } else {
                value = getStringView(it)
            }
            if (value != "" && value.isDigitsOnly()) {
                try {
                    contentvalues.put(dbcolumn, value.toInt())
                } catch (e: Exception){
                    Logging.d("${Constants.APP_NAME}/copyViewGroupToContentValues digits error: $dbcolumn  tag: ${it.tag}" )
                    contentvalues.put(dbcolumn, value)
                }
            } else {
                contentvalues.put(dbcolumn, value)
            }
        }
        return contentvalues
    }

    fun showKeyboard(v: View, context: Context) {
        v.requestFocus()
        val imm: InputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(v, 0)
    }
    fun hideKeyboard(v: View?, context: Context) {
        if (v != null) {
            v.requestFocus()
            val imm: InputMethodManager =
                context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }

    fun getDip(dimen: Int, context: Context): Float {
        val pixel = context.resources.getDimension(dimen)
        return pixel / context.resources.displayMetrics.density
    }

    fun hideSoftKeyboard(mContext: Context) {
        if (mContext is Activity) {
            val inputMethodManager =
                mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (mContext.currentFocus != null) inputMethodManager.hideSoftInputFromWindow(
                mContext.currentFocus!!.windowToken, 0
            )
        }
    }
}
