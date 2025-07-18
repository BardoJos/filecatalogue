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
package com.farrusco.filecatalogue.common

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.activity.result.ActivityResult
import com.farrusco.filecatalogue.R
import com.farrusco.filecatalogue.business.Category
import com.farrusco.filecatalogue.business.Help
import com.farrusco.filecatalogue.tables.TableCategory
import com.farrusco.projectclasses.activity.BaseActivityTableLayout
import com.farrusco.projectclasses.common.Constants
import com.farrusco.projectclasses.common.ConstantsFixed
import com.farrusco.projectclasses.utils.CalcObjects
import com.farrusco.projectclasses.widget.ButtonExt

class SearchFilesSub : BaseActivityTableLayout() {
    override val layoutResourceId: Int = R.layout.search_files_sub
    override val mainViewId: Int = R.id.viewMain

    private lateinit var mTblSearch: TableLayout
    private lateinit var mTxtStatus: TextView
    private lateinit var btnCheck: ButtonExt
    private lateinit var mRowSearch: TableRow
    private lateinit var category: Category
    private val selectmenu = ConstantsFixed.LAST_ID + 1

    override fun initActivity() {
        helpText = Help().getHelpTitle(className)
        category = Category(this)
        initTableLayout()
        fillList()
    }

    override fun initTableLayout() {
        addTableLayout(
            R.id.tblLayout,
            R.layout.line_textview1x,
            category,
            arrayOf(TableCategory.Columns.description.name),
            arrayListOf(ConstantsFixed.HELP_ID),
            SearchFilesSub::class.java
        )
    }

    @SuppressLint("InflateParams")
    private fun fillList() {
        mTblSearch = findViewById(R.id.tblSearch)
        mTxtStatus = findViewById(R.id.txtStatus)
        mTxtStatus.text = intent.getStringExtra("title")
        var id = 0
        if (intent.hasExtra("_id")) {
            id = CalcObjects.stringToInteger(intent.getStringExtra("_id"),0)
        }

        mTxtStatus.text = Category.arrCategoryTitle.find{it.id == id}?.title

        removeAllTableLayoutViews(0)
        val li: LayoutInflater = layoutInflater

        Category.arrCategoryTitle.filter{( it.mainid == id || it.mainid > 0  && it.id == id && it.type == ConstantsLocal.TYPE_DIRECTORY) && it.count>0}.forEach {
            val vi: View = li.inflate(R.layout.line_btn_text, null)
            val colInfo: TextView = vi.findViewById(R.id.colInfo)
            vi.tag = ""
            btnCheck = vi.findViewById(R.id.btnCheck)
            btnCheck.tag = ""

            var title = it.title
            if (it.type == ConstantsLocal.TYPE_DIRECTORY){
                title = if (title.equals(mTxtStatus.text.toString(),true)){
                    "./"
                } else {
                    title.replace(mTxtStatus.text.toString() + "/","./")
                }
            }
            val count: Int
            val checked: Int
            if (it.mainid > 0  && it.id == id && it.type == ConstantsLocal.TYPE_DIRECTORY){
                btnCheck.tag = "curr"
                count = it.countCurr
                checked = it.checkedCurr
            } else {
                checked = it.checked
                count = it.count
            }
            when (checked){
                0 -> btnCheck.setBackgroundResource(R.drawable.btn_check_off)
                1 -> btnCheck.setBackgroundResource(R.drawable.btn_check_on)
                2 -> btnCheck.setBackgroundResource(R.drawable.btn_check_tristate)
                3 -> btnCheck.setBackgroundResource(R.drawable.btn_check_no)
            }

            if (it.mainid == id && it.hasChild) {
                colInfo.setOnClickListener(mClickListenerColSelector)
            }

            colInfo.text = "$title ($count)"
            colInfo.tag = it.id
            btnCheck.setOnClickListener(mClickListenerColCheckBox)
            btnCheck.setOnLongClickListener(mLongClickListenerColCheckBox)
            mTblSearch.addView(vi, TableLayout.LayoutParams())

        }
    }

    private val mLongClickListenerColCheckBox = View.OnLongClickListener { v ->
        mRowSearch = v.parent as TableRow
        btnCheck = mRowSearch.findViewById<View>(R.id.btnCheck) as ButtonExt
        val colInfo: TextView = mRowSearch.findViewById<View>(R.id.colInfo) as TextView
        val elem = Category.arrCategoryTitle.find{ it.id == colInfo.tag.toString().toInt() }
        if (elem != null){
            setCheckedChild(elem.id)
            when (btnCheck.getBackgroundResource()) {
                R.drawable.btn_check_no -> {
                    if (btnCheck.tag == "curr"){
                        Category.arrCategoryTitle.find{it.id == elem.id}?.checkedCurr=0
                    } else {
                        Category.arrCategoryTitle.find{it.id == elem.id}?.checked=0
                    }
                    btnCheck.setBackgroundResource(R.drawable.btn_check_off)
                }
                else -> {
                    if (btnCheck.tag == "curr"){
                        Category.arrCategoryTitle.find{it.id == elem.id}?.checkedCurr=3
                    } else {
                        Category.arrCategoryTitle.find{it.id == elem.id}?.checked=3
                    }
                    btnCheck.setBackgroundResource(R.drawable.btn_check_no)
                }
            }
        }

        true
    }

    private val mClickListenerColCheckBox = View.OnClickListener { v ->
        mRowSearch = v.parent as TableRow
        btnCheck = mRowSearch.findViewById<View>(R.id.btnCheck) as ButtonExt
        val colInfo: TextView = mRowSearch.findViewById<View>(R.id.colInfo) as TextView
        val elem = Category.arrCategoryTitle.find{ it.id == colInfo.tag.toString().toInt() }
        if (elem != null){
            setCheckedChild( elem.id )
            when (btnCheck.getBackgroundResource()) {
                R.drawable.btn_check_off -> {
                    if (btnCheck.tag == "curr"){
                        Category.arrCategoryTitle.find{it.id == elem.id}?.checkedCurr=1
                    } else {
                        Category.arrCategoryTitle.find{it.id == elem.id}?.checked=1
                    }
                    btnCheck.setBackgroundResource(R.drawable.btn_check_on)
                }
                else -> {
                    Category.arrCategoryTitle.find{it.id == elem.id}?.checkedCurr=0
                    Category.arrCategoryTitle.find{it.id == elem.id}?.checked=0
                    btnCheck.setBackgroundResource(R.drawable.btn_check_off)
                }
            }
        }
    }

    private fun setCheckedChild(id:Int){
        var ok: Boolean
        var mainid = id
        Category.arrCategoryTitle.find{it.id == id}?.checked=0
        do{
            ok = false
            val elem = Category.arrCategoryTitle.find{ it.mainid == mainid && it.checked != 0}
            if (elem == null) {
                if (mainid != id){
                    mainid = id
                    ok = true
                }
            }else{
                Category.arrCategoryTitle.find{it.id == elem.id}?.checked=0
                mainid = elem.id
                ok = true
            }
        } while (ok)
    }

    private val mClickListenerColSelector = View.OnClickListener { v ->
        mRowSearch = v.parent as TableRow
        val colInfo: TextView = mRowSearch.findViewById<View>(R.id.colInfo) as TextView
        btnCheck = mRowSearch.findViewById<View>(R.id.btnCheck) as ButtonExt
        intent.setClass(this@SearchFilesSub, SearchFilesSub::class.java)
        intent.putExtra("_id",  colInfo.tag.toString())
        resultLauncher.launch(intent)
    }

    override fun resultActivity(result: ActivityResult) {
        if (result.resultCode == RESULT_OK) {
            when (CalcObjects.stringToInteger(
                result.data!!.getStringExtra(Constants.RequestCode),0)
            ) {
                9 -> {
                    if (intent.hasExtra("_id")) intent.removeExtra("_id")
                    intent.putExtra(Constants.RequestCode, "9")
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }
            btnCheck = mRowSearch.findViewById<View>(R.id.btnCheck) as ButtonExt
            val colInfo: TextView = mRowSearch.findViewById(R.id.colInfo)
            val id = colInfo.tag.toString().toInt()
            if (Category.arrCategoryTitle.find{it.mainid == id && it.checked > 0 || it.id == id && it.checkedCurr > 0} != null){
                Category.arrCategoryTitle.find{it.id == id}?.checked=2
                btnCheck.setBackgroundResource(R.drawable.btn_check_tristate)
            } else {
                if (btnCheck.getBackgroundResource() == R.drawable.btn_check_tristate){
                    Category.arrCategoryTitle.find{it.id == id}?.checked=0
                    btnCheck.setBackgroundResource(R.drawable.btn_check_off)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        contextMenuAdd(selectmenu,getText(R.string.first_screen).toString(),R.drawable.ic_home_black_24dp,0,1)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            selectmenu -> {
                val intentx = Intent()
                intentx.putExtra(Constants.RequestCode, "9")
                setResult(RESULT_OK, intentx)
                finish()
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
        return false
    }
}