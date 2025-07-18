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
package com.farrusco.filecatalogue.tables

import android.database.SQLException
import com.farrusco.projectclasses.R
import com.farrusco.projectclasses.databases.tables.Tables
import com.farrusco.projectclasses.utils.Logging
import com.farrusco.projectclasses.utils.ReturnValue

@Suppress("EnumEntryName")
open class TableOrderLine: Tables(TABLE_NAME, Columns.entries.joinToString(",")) {

    companion object{
        const val TABLE_NAME = "POS_ORDERLINE"
    }
    enum class Columns { _id, _orderid, _productid, amount }
    private fun deleteTableRow(
        orderid: Int?,
        productid: Int?
    ): ReturnValue {
        var rtn = ReturnValue()
        try {
            var where = ""
             if (productid != null) {
                where += " ${Columns._productid.name} = $productid and"
            }
            if (orderid != null) {
                where += " ${Columns._orderid.name} = $orderid and"
            }
            if (where === "") {
                Logging.e("Error delete $TABLE_NAME", "no condition")
                rtn.setReturnvalue(false).setMessnr(R.string.mess032_notsaved)
            }
            where = where.substring(0, where.length - 3)
            Logging.d("Delete from $TABLE_NAME where $where")
            rtn = delete(where)
        } catch (e: SQLException) {
            Logging.e("Error delete $TABLE_NAME", e.toString())
            rtn.setReturnvalue(false).setMessnr(R.string.mess032_notsaved)
        }
        return rtn
    }

    fun deleteNotProduct(): ReturnValue {

        return try {
            val where = " not exists (select 1 from ${TableProduct.TABLE_NAME} " +
                    " where ${TableProduct.Columns._id.name} = $TABLE_NAME.${Columns._productid.name})"
            delete(where)
        } catch (e: SQLException) {
            Logging.e("Error delete $TABLE_NAME", e.toString())
            val rtn = ReturnValue()
            rtn.returnValue = false
            rtn
        }
    }

    fun deleteProduct(productid: Int?): ReturnValue {
        return deleteTableRow(null, productid)
    }

    fun deleteOrderLineTotal(orderid: Int?): ReturnValue {
        return deleteTableRow(orderid, null)
    }
}