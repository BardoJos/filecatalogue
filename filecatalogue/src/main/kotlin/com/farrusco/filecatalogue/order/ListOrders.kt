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
package com.farrusco.filecatalogue.order

import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResult
import com.farrusco.filecatalogue.R
import com.farrusco.filecatalogue.business.Help
import com.farrusco.filecatalogue.business.Order
import com.farrusco.filecatalogue.business.Product
import com.farrusco.filecatalogue.tables.TableOrder
import com.farrusco.projectclasses.activity.BaseActivityTableLayout
import com.farrusco.projectclasses.common.Constants
import com.farrusco.projectclasses.common.ConstantsFixed
import com.farrusco.projectclasses.common.ConstantsFixed.Companion
import com.farrusco.projectclasses.messages.ToastExt
import java.io.File

class ListOrders : BaseActivityTableLayout() {
    override val mainViewId = R.id.viewMain
    override val layoutResourceId = R.layout.list_orders
    private lateinit var order: Order
    private var product: Product? = null

    override fun initTableLayout() {
        addTableLayout(
            R.id.tblLayout,
            R.layout.list_orders_lines,
            order,
            arrayOf(TableOrder.Columns._id.name, TableOrder.Columns.buyername.name),
            arrayListOf(ConstantsFixed.HELP_ID,Companion.SAVE_ID, Companion.ADD_ID, Companion.EDIT_ID, Companion.DELETE_ID),
            DetailOrderTab::class.java
        )
    }

    override fun initActivity() {
        helpText = Help().getHelpTitle(className)
        product = Product(this)
        order = Order(this)
        deleteTmp()
        if (!product!!.existsProduct("")) {
            ToastExt().makeText(
                this, R.string.mess051_no_files,
                Toast.LENGTH_LONG
            ).show()
            finish()
        } else {
            fillList()
        }
    }

    private fun fillList() {
        removeAllTableLayoutViews(0)
        val rtn = order.getOrder(0)
        //val cnt = rtn.cursor.count
        fillTable(rtn.cursor)
        rtn.cursorClose()
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            ConstantsFixed.DELETE_ID -> {
                if (!super.onContextItemSelected(item)) return false
                return saveScreen().returnValue
            }
        }
        return super.onContextItemSelected(item)
    }

    override fun resultActivity(result: ActivityResult) {
        fillList()
    }

    private fun deleteTmp() {
        File(Constants.bitmapPath).deleteRecursively()
        // MLKit cache cleanup
        File(Constants.mlkitPath).deleteRecursively()
    }

    override fun onDestroy() {
        super.onDestroy()
        deleteTmp()
    }
}