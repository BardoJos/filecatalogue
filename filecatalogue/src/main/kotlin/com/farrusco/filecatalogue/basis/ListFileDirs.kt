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
package com.farrusco.filecatalogue.basis

import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import com.farrusco.filecatalogue.BuildConfig
import com.farrusco.filecatalogue.R
import com.farrusco.filecatalogue.business.Category
import com.farrusco.filecatalogue.business.Help
import com.farrusco.filecatalogue.business.Product
import com.farrusco.filecatalogue.business.Systeem
import com.farrusco.filecatalogue.common.ConstantsLocal
import com.farrusco.filecatalogue.common.SystemAttr
import com.farrusco.filecatalogue.tables.TableProduct
import com.farrusco.filecatalogue.utils.RefreshProducts
import com.farrusco.projectclasses.activity.BaseActivityTableLayout
import com.farrusco.projectclasses.common.ConstantsFixed
import com.farrusco.projectclasses.filepicker.controller.DialogSelectionListener
import com.farrusco.projectclasses.filepicker.model.DialogConfigs
import com.farrusco.projectclasses.filepicker.model.DialogProperties
import com.farrusco.projectclasses.filepicker.view.FilePickerDialog
import com.farrusco.projectclasses.messages.ToastExt
import com.farrusco.projectclasses.utils.CalcObjects
import com.farrusco.projectclasses.utils.Logging
import com.farrusco.projectclasses.utils.ViewUtils
import com.farrusco.projectclasses.widget.ButtonExt
import java.io.File

class ListFileDirs : BaseActivityTableLayout() {

    override val layoutResourceId: Int = R.layout.list_file
    override val mainViewId: Int = R.id.viewMain

    private lateinit var product: Product
    private lateinit var systeem: Systeem
    private lateinit var refreshProducts: RefreshProducts

    private val importId = ConstantsFixed.LAST_ID + 1
    private val exportId = ConstantsFixed.LAST_ID + 2

    override fun initTableLayout() {
        addTableLayout(
            R.id.tblLayout,
            R.layout.listfiledirs_line,
            product,
            arrayOf(TableProduct.Columns._id.toString(), TableProduct.Columns.title.toString()),
            arrayListOf(
                ConstantsFixed.ADD_ID,
                ConstantsFixed.EDIT_ID,
                ConstantsFixed.DELETE_ID,
                ConstantsFixed.HELP_ID
            ),
            DetailFileDirsTab::class.java
        )
    }

    override fun initActivity() {
        helpText = Help().getHelpTitle(className)
        ConstantsLocal.initProductRel = true
        ConstantsLocal.arrProductRel = ArrayList()
        product = Product(this)
        systeem = Systeem(this)
        refreshProducts = RefreshProducts(this, true)

        fillList()
    }

    private fun fillList() {
        removeAllTableLayoutViews(0)
        val rtn = product.getDirectory(0)
        fillTable(rtn.cursor)
        rtn.cursorClose()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        contextMenuAdd(
            importId,
            getText(R.string.importfile).toString(),
            android.R.drawable.ic_menu_upload,
            11,
            1
        )
        contextMenuAdd(
            exportId,
            getText(R.string.exportfile).toString(),
            android.R.drawable.ic_menu_save,
            12,
            1
        )
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            importId -> {
                val properties = DialogProperties()
                val lastPath = Systeem(this).getValue(SystemAttr.LastPath)
                properties.dir = DialogConfigs.STORAGE_STORAGE
                if (lastPath.isNotEmpty()) {
                    properties.offset = File(lastPath)
                } else properties.offset = File(properties.dir)
                properties.extensions = arrayOf("")
                properties.selectionType = DialogConfigs.FILE_SELECT

                val dialog = FilePickerDialog(this, properties)
                dialog.setTitle(getText(R.string.selectfolder))

                dialog.setDialogSelectionListener(object : DialogSelectionListener {
                    override fun onSelectedFilePaths(lastPath: String?, files: Array<String?>?) {

                        //files is the array of paths selected by the App User.
                        if (files != null) {
                            for (path in files) {
                                Systeem(this@ListFileDirs).setValue(
                                    SystemAttr.LastPath,
                                    File(path.toString()).absolutePath,
                                    3
                                )
                                product.readProductPrices(path!!)
                            }
                        }
                    }

                    override fun onCancelPaths(lastPath: String?) {
                        if (lastPath != null) {
                            Systeem(this@ListFileDirs).setValue(
                                SystemAttr.LastPath,
                                lastPath,
                                3
                            )
                        }
                    }
                })
                dialog.show()
            }

            exportId -> {

                val properties = DialogProperties()
                val lastPath = Systeem(this).getValue(SystemAttr.LastPath)
                properties.dir = DialogConfigs.STORAGE_STORAGE
                if (lastPath.isNotEmpty()) {
                    properties.offset = File(lastPath)
                } else properties.offset = File(properties.dir)
                properties.selectionType = DialogConfigs.DIR_SELECT

                val dialog = FilePickerDialog(this, properties)
                dialog.setTitle(getText(R.string.selectfolder))

                dialog.setDialogSelectionListener(object : DialogSelectionListener {
                    override fun onSelectedFilePaths(lastPath: String?, files: Array<String?>?) {
                        //files is the array of paths selected by the App User.
                        if (files != null) {
                            for (path in files) {
                                Systeem(this@ListFileDirs).setValue(
                                    SystemAttr.LastPath,
                                    path,
                                    3
                                )
                                product.writeProductPrices(path!!)
                            }
                        }
                    }

                    override fun onCancelPaths(lastPath: String?) {
                        if (lastPath != null) {
                            Systeem(this@ListFileDirs).setValue(
                                SystemAttr.LastPath,
                                lastPath,
                                3
                            )
                        }
                    }
                })
                dialog.show()
            }

            else -> return super.onOptionsItemSelected(item)
        }
        return false
    }

    override fun addTableLine(idx: Int, vi: View, groupno: Int): View {
        val vi1 = super.addTableLine(idx, vi, groupno)
        ViewUtils.addHiddenField(vi1 as ViewGroup, "category", "init")
        val mBtnRefresh = vi1.findViewById<ButtonExt>(R.id.btnRefresh)

        mBtnRefresh.setOnClickListener(mLocalBtnRefreshListener)
        return vi1
    }

    private val mLocalBtnRefreshListener = View.OnClickListener {
        if (it.isEnabled) {
            it.isEnabled = false
            if (BuildConfig.LIMIT > 0 && Product(this).getFilesCount() >= BuildConfig.LIMIT) {
                ToastExt().makeText(
                    this,
                    getText(R.string.mess058_demo).toString()
                        .replace("%0%", BuildConfig.LIMIT.toString()), Toast.LENGTH_LONG
                ).show()
                it.isEnabled = true
            } else if (hasChildModificationTag()) {
                ToastExt().makeText(
                    this, R.string.mess066_savefirst, Toast.LENGTH_LONG
                ).show()
                it.isEnabled = true
            } else {
                currView = it.parent as View
                val vi = ViewUtils.getTableRow(currView)!!
                val productid: Int = CalcObjects.objectToInteger(
                    ViewUtils.getChildValue(
                        vi as ViewGroup,
                        TableProduct.Columns._id.name
                    )
                )
                val dirname = ViewUtils.getChildValue(
                    vi as ViewGroup,
                    TableProduct.Columns.dirname.name
                )!!.replace("\n", "")
                if (productid == 0 || dirname.isEmpty()) {
                    Logging.d("ListFileDirs/fillDb", "Id = zero or param is empty")
                    it.isEnabled = true
                } else {
                    refreshProducts.startTask(productid, dirname, it as ButtonExt)
                }
            }
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            ConstantsFixed.EDIT_ID -> {
                if (hasChildModificationTag()) {
                    ToastExt().makeText(this, R.string.mess066_savefirst, Toast.LENGTH_LONG).show()
                    return false
                }

                val vi = ViewUtils.getTableRow(currView)!!
                var code = ViewUtils.getChildValue(vi as ViewGroup, "category").toString()
                if (code == "init") {
                    val productid = CalcObjects.stringToInteger(
                        ViewUtils.getChildValue(
                            vi,
                            TableProduct.Columns._id.name
                        ).toString()
                    )

                    val arrCategoryMut = Category(this).getCategoryCodes(
                        productid,
                        arrayListOf(ConstantsLocal.TYPE_CATEGORY, ConstantsLocal.TYPE_CREATION)
                    )
                    code = ""
                    for (idx in arrCategoryMut.indices) {
                        code += arrCategoryMut[idx].categoryid.toString() + "," + arrCategoryMut[idx].level + "," + arrCategoryMut[idx].checked + ","
                    }
                    if (code != "") {
                        ViewUtils.setChildValue(
                            vi as ViewGroup,
                            "category",
                            code.substring(0, code.length - 1)
                        )
                    }
                }
            }

            ConstantsFixed.DELETE_ID -> {
                // mark deleted row
                super.onContextItemSelected(item)
                // delete row from database
                saveScreen()
                // refresh screen
                fillList()
                return true
            }
        }
        return super.onContextItemSelected(item)
    }

    override fun resultActivity(result: ActivityResult) {
        if (result.resultCode == RESULT_OK) {
            fillList()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ConstantsLocal.initProductRel = false
        ConstantsLocal.arrProductRel = ArrayList()
    }
}
