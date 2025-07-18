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
package com.farrusco.filecatalogue.utils

import android.view.View
import android.widget.TableLayout
import android.widget.TableRow
import androidx.core.view.children
import androidx.core.view.indices
import androidx.core.view.size
import com.farrusco.filecatalogue.common.CategoryDetail
import com.farrusco.filecatalogue.common.ConstantsLocal
import com.farrusco.projectclasses.common.ConstantsFixed
import com.farrusco.projectclasses.utils.TagModify
import com.farrusco.projectclasses.widget.SwitchExt
import com.farrusco.projectclasses.widget.TextViewExt

class PageManager {
    inner class FreezeRow {
        lateinit var color: ConstantsFixed.ColorBasic
        var vis: Int = 0
    }

    fun backup(mTblLayout2: TableLayout?): Boolean{

        if (mTblLayout2 == null) return false
        arrColor = HashMap()
        for(idx in mTblLayout2.indices){
            val row = mTblLayout2.getChildAt(idx) as TableRow
            val id = TagModify.getViewTagValue(row.getChildAt(0), "catid").toInt()
            val freezeRow = FreezeRow()
            if (row.getChildAt(0)::class.simpleName == TextViewExt::class.simpleName){
                freezeRow.color = (row.getChildAt(0) as TextViewExt).colorView
            } else if (row.getChildAt(0)::class.simpleName == SwitchExt::class.simpleName){
                freezeRow.color = (row.getChildAt(0) as SwitchExt).colorView
            }
            freezeRow.vis = row.visibility
            arrColor.putIfAbsent(id,freezeRow)
        }
        return true
    }

    fun putCodeInCategoryMut (code: String): MutableList<CategoryDetail>{
        val arrCategoryMut: MutableList<CategoryDetail> = mutableListOf()
        val arrCategory = code.split(",").toTypedArray()
        arrCategoryMut.clear()
        if (arrCategory.size>2) {
            for (i in arrCategory.indices step 3) {
                val category = CategoryDetail()
                category.categoryid = arrCategory[i].toInt()
                category.level = arrCategory[i + 1].toInt()
                category.checked = arrCategory[i + 2].toInt()
                category.checkedOld = category.checked
                arrCategoryMut.add(category)
            }
        }
        return arrCategoryMut
    }

    companion object {

        var arrColor = HashMap<Int,FreezeRow>()

        fun treeColor(mTblLayout2: TableLayout?){

            if (mTblLayout2 == null) return
            for(idx in mTblLayout2.indices) {
                val row = mTblLayout2.getChildAt(idx) as TableRow
                row.visibility = View.VISIBLE
                if (row.getChildAt(0)::class.simpleName == SwitchExt::class.simpleName){
                    val fieldSwitch = row.getChildAt(0) as SwitchExt
                    if (TagModify.getViewTagValue(fieldSwitch,ConstantsFixed.TagSection.TsUserFlag.name) != ""){
                        fieldSwitch.colorView = ConstantsFixed.ColorBasic.Edit
                    } else {
                        fieldSwitch.colorView = ConstantsFixed.ColorBasic.Default
                    }
                }
            }

            var idx = 0
            //var arrLevelColor=  arrayListOf<String>()
            while(idx < mTblLayout2.size){
                val row = mTblLayout2.getChildAt(idx) as TableRow
                if (row.getChildAt(0)::class.simpleName == TextViewExt::class.simpleName){
                    var fieldText = row.getChildAt(0) as TextViewExt
                    if (fieldText.colorView == ConstantsFixed.ColorBasic.Collapse){
                        val level = TagModify.getViewTagValue(fieldText,"lvl").toInt()
                        idx++
                        // all sub items are to hide
                        while(idx < mTblLayout2.size ){
                            var levelSub=0
                            val row2 = mTblLayout2.getChildAt(idx) as TableRow
                            if (row2.getChildAt(0)::class.simpleName == TextViewExt::class.simpleName){
                                fieldText = row2.getChildAt(0) as TextViewExt
                                levelSub = TagModify.getViewTagValue(fieldText,"lvl").toInt()
                            } else if (row2.getChildAt(0)::class.simpleName == SwitchExt::class.simpleName){
                                val fieldSwitch = row2.getChildAt(0) as SwitchExt
                                levelSub = TagModify.getViewTagValue(fieldSwitch,"lvl").toInt()
                            }
                            if (levelSub>level) {
                                row2.visibility = View.GONE
                                idx++
                            } else {
                                // go up 1 row because it will be increased later
                                idx--
                                break
                            }
                        }
                    }
                }
                idx++
            }
        }

        fun restore(mTblLayout2: TableLayout?){

            if (mTblLayout2 == null || mTblLayout2.children.count() == 0 || arrColor.isEmpty()) return
            for(idx in mTblLayout2.indices){
                val row = mTblLayout2.getChildAt(idx) as TableRow
                val id = TagModify.getViewTagValue(row.getChildAt(0), "catid").toInt()
                if (arrColor.containsKey(id)){
                    val freezeRow = arrColor.getValue(id)
                    if (row.getChildAt(0)::class.simpleName == TextViewExt::class.simpleName){
                        (row.getChildAt(0) as TextViewExt).colorView = freezeRow.color
                    } else if (row.getChildAt(0)::class.simpleName == SwitchExt::class.simpleName){
                        (row.getChildAt(0) as SwitchExt).colorView = ConstantsFixed.ColorBasic.Default
                    }
                    row.visibility = freezeRow.vis
                }
            }
            //colorGreen(mTblLayout2)

        }

        fun treeCollapse(fieldText: TextViewExt, mTblLayout2: TableLayout) {
            fieldText.setOnClickListener {
                when(fieldText.colorView) {
                    ConstantsFixed.ColorBasic.Edit -> fieldText.colorView =
                        ConstantsFixed.ColorBasic.Collapse
                    else -> fieldText.colorView = ConstantsFixed.ColorBasic.Edit
                }
                treeColor( mTblLayout2)
            }
        }

        fun setupTable(mTblLayout2: TableLayout?, arrCategoryMut: MutableList<CategoryDetail>){

            if (mTblLayout2 == null) return

            mTblLayout2.children.forEach { rw ->
                (rw as TableRow).children.forEach { item ->
                    if (item::class.simpleName == TextViewExt::class.simpleName){
                        val fieldText = item as TextViewExt
                        fieldText.colorView = ConstantsFixed.ColorBasic.Edit
                    } else if (item::class.simpleName == SwitchExt::class.simpleName){
                        val fieldSwitch = item as SwitchExt
                        val categoryid = TagModify.getViewTagValue(fieldSwitch as View, "catid").toInt()
                        val type = TagModify.getViewTagValue(fieldSwitch as View, "type").toInt()

                        fieldSwitch.skipEditTagAlways=true
                        fieldSwitch.colorView = ConstantsFixed.ColorBasic.Default
                        if (type != ConstantsLocal.TYPE_DIRECTORY){
                            val lineCategoryMut = arrCategoryMut.find{ it.categoryid == categoryid}
                            if (lineCategoryMut == null){
                                fieldSwitch.isChecked = false
                            } else{
                                fieldSwitch.isChecked = (lineCategoryMut.checked == -1)
                            }
                        }
                        fieldSwitch.skipEditTagAlways=false
                        if (ConstantsLocal.isAutoNewEnabled &&
                            fieldSwitch.isEnabled &&
                            TagModify.getViewTagValue(fieldSwitch as View, "type").toInt() == ConstantsLocal.TYPE_AUTOMATED_NEW){
                            if (fieldSwitch.isChecked) fieldSwitch.isChecked = false
                            fieldSwitch.visibility = View.GONE
                        }

                    }
                }
            }

            // collapse CREATION level
            for(idx in  mTblLayout2.indices) {
                val row = mTblLayout2.getChildAt(idx) as TableRow
                if (row.getChildAt(0)::class.simpleName == TextViewExt::class.simpleName) {
                    val fieldText = row.getChildAt(0) as TextViewExt
                    val type = TagModify.getViewTagValue(fieldText, "type").toInt()
                    val level = TagModify.getViewTagValue(fieldText,"lvl").toInt()
                    if (level <= 2 && type == ConstantsLocal.TYPE_CREATION){
                        fieldText.colorView = ConstantsFixed.ColorBasic.Collapse
                        treeColor( mTblLayout2)
                        //break
                    }
                }
            }

            if (ConstantsLocal.isGPSEnabled){
                // collapse GPS level
                for(idx in  mTblLayout2.indices) {
                    val row = mTblLayout2.getChildAt(idx) as TableRow
                    if (row.getChildAt(0)::class.simpleName == TextViewExt::class.simpleName) {
                        val fieldText = row.getChildAt(0) as TextViewExt
                        val type = TagModify.getViewTagValue(fieldText, "type").toInt()
                        //val level = TagModify.getViewTagValue(fieldText,"lvl").toInt()
                        if ( type == ConstantsLocal.TYPE_GPS){
                            fieldText.colorView = ConstantsFixed.ColorBasic.Collapse
                            treeColor( mTblLayout2)
                            //break
                        }
                    }
                }
            }

            if (ConstantsLocal.isFileExtensionEnabled){
                // collapse extension level
                for(idx in  mTblLayout2.indices) {
                    val row = mTblLayout2.getChildAt(idx) as TableRow
                    if (row.getChildAt(0)::class.simpleName == TextViewExt::class.simpleName) {
                        val fieldText = row.getChildAt(0) as TextViewExt
                        val type = TagModify.getViewTagValue(fieldText, "type").toInt()
                        val level = TagModify.getViewTagValue(fieldText,"lvl").toInt()
                        if (level <= 2 && type == ConstantsLocal.TYPE_FILE_EXTENSION){
                            fieldText.colorView = ConstantsFixed.ColorBasic.Collapse
                            treeColor( mTblLayout2)
                            //break
                        }
                    }
                }
            }

        }
 /*
        fun colorGreen(mTblLayout2: TableLayout?){
            if (mTblLayout2 == null || Category.arrCategoryTitle.size < 1) return

            var mainids = ","
            for (idx in mTblLayout2.indices) {
                val row = mTblLayout2.getChildAt(idx) as TableRow
                row.children.filter { it::class.simpleName == SwitchExt::class.simpleName && (it as SwitchExt).isChecked }
                    .forEach { item ->
                        val fieldSwitch = item as SwitchExt
                        val categoryid =
                            TagModify.getViewTagValue(fieldSwitch as View, "catid").toInt()
                        mainids += ",$categoryid,"
                        var categoryid1 = categoryid
                        do {
                            val tmp =
                                Category.arrCategoryTitle.find { it.id == categoryid1 }?.mainid
                            mainids += "$categoryid1,"
                            if (tmp == null || tmp == 0) {
                                break
                            }
                            categoryid1 = tmp
                        } while (true)

                    }
            }

            for (idx in mTblLayout2.indices) {
                val row = mTblLayout2.getChildAt(idx) as TableRow
                row.children.filter { it::class.simpleName == TextViewExt::class.simpleName }
                    .forEach { item ->
                        val textViewExt = item as TextViewExt
                        val categoryid2 =
                            TagModify.getViewTagValue(textViewExt as View, "catid").toInt()
                        if (mainids.contains(",$categoryid2,")) {
                            textViewExt.colorView = ConstantsFixed.ColorBasic.CollapseActive
                            mainids = mainids.replace(",$categoryid2,", ",")
                            if (mainids == ",") return
                        }
                }
            }
        }
 */
    }
}