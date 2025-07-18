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
package com.farrusco.filecatalogue.business

import android.database.Cursor
import com.farrusco.filecatalogue.tables.TableOrder
import com.farrusco.filecatalogue.tables.TableOrderLine
import com.farrusco.filecatalogue.tables.TableProduct
import com.farrusco.projectclasses.common.Constants
import com.farrusco.projectclasses.utils.ReturnValue

class OrderLine() : TableOrderLine(){
    // private var contextlocal = context

    fun getTotalAmount(orderid: Int, orderlineid: Int): Double {
        var rtn = 0.0
        var sql = "select sum(%2." + TableProduct.Columns.price.name + " * %1." + Columns.amount.name + " )"
        if ( orderlineid > 0 ){
            sql = sql.replace("sum", "")
            sql +=  " from %1" +
                    " join %2 on %2." + TableProduct.Columns._id.name + " = %1." + Columns._productid +
                    " where %1." + Columns._id.name + " = $orderlineid"
        } else{
            sql +=  " - sum(%3." + TableOrder.Columns.discount.name + ")" +
                    " from %1" +
                    " join %2 on %2." + TableProduct.Columns._id.name + " = %1." + Columns._productid +
                    " join %3 on %3." + TableOrder.Columns._id.name + " = %1." + Columns._orderid +
                    " where %1." + Columns._orderid.name + " = $orderid"
        }
        sql = sql.replace("%1", TABLE_NAME)
        sql = sql.replace("%2", TableProduct.TABLE_NAME)
        sql = sql.replace("%3", TableOrder.TABLE_NAME)
        val cursor: Cursor = Constants.db.rawQuery(sql,null)
        if (cursor.moveToFirst()) {
            if (cursor.getString(0) != null) rtn = java.lang.Double.valueOf(cursor.getString(0))
        }
        cursor.close()
        return rtn
    }

    fun getOrderLineIncl(orderid: Int): ReturnValue {
        val sql =
            "SELECT a.${Columns._id.name}, a.${Columns._orderid.name}, a.${Columns._productid.name}" +
                    ", a.${Columns.amount.name}, ${TableProduct.Columns.price.name}" +
                    ", b.${TableProduct.Columns.code.name}, b.${TableProduct.Columns.title.name}, ${TableProduct.Columns.description.name}" +
                    ", b.${TableProduct.Columns.dateavailable.name}, b.${TableProduct.Columns.blocked.name}"  +
                    ", b.${TableProduct.Columns.dirname.name}, b.${TableProduct.Columns.filename.name}"  +
                    ", b.${TableProduct.Columns.rotation.name} "  +
                    " FROM $tableName a" +
                    " JOIN ${TableProduct.TABLE_NAME} b ON b.${TableProduct.Columns._id.name} = a.${Columns._productid.name}" +
                    " where a.${Columns._orderid.name} = $orderid" +
                    " order by 1"
        return rawQuery(sql,null)
    }
}