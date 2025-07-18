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

import android.content.Context
import com.farrusco.filecatalogue.common.ConstantsLocal
import com.farrusco.filecatalogue.common.ProductRelLine
import com.farrusco.filecatalogue.tables.TableCategory
import com.farrusco.filecatalogue.tables.TableProductRel
import com.farrusco.projectclasses.utils.ReturnValue
import com.farrusco.projectclasses.utils.ViewUtils

class ProductRel(private val context: Context): TableProductRel(){

    //private var arrProductRel: ArrayList<ProductRelLine> = ArrayList()

    fun deleteIdType(id: Int, type:Int): Boolean{
        var categoryid = 0
        val category = Category(context)
        val rtn = category.getWhere("${TableCategory.Columns.type.name} = $type")
        if (rtn.cursor.moveToFirst()){
            categoryid = ViewUtils.getValueCursor(rtn.cursor, TableCategory.Columns._id.name).toInt()
        }
        rtn.cursorClose()
        return deleteTableRow(0, id, categoryid)
    }

    fun insertProductRelMultiple (productid: Int?, categoryid: Int): ReturnValue {
        if (ConstantsLocal.initProductRel && ConstantsLocal.arrProductRel.isEmpty()) {
            val rtn = getCategoryFromProduct(0)
            ConstantsLocal.arrProductRel = ArrayList()
            if (rtn.cursor.moveToFirst()){
                do{
                    val pdl = ProductRelLine()
                    pdl.id = ViewUtils.getValueCursorInt(rtn.cursor, Columns._productid.name)
                    pdl.category = "," + ViewUtils.getValueCursor(rtn.cursor, Columns._categoryid.name) + ","
                    ConstantsLocal.arrProductRel.add(pdl)
                } while (rtn.cursor.moveToNext())
            }
            rtn.cursorClose()
        }
        if (ConstantsLocal.arrProductRel.isEmpty()) {
            return insertProductRel(productid , categoryid )
        }
        val pdl = ConstantsLocal.arrProductRel.find{it.id == productid && it.category.contains(",$categoryid,") }
        if (pdl == null){
            return insertProductRel(productid , categoryid )
        }
        return ReturnValue().setReturnvalue(false)
    }
}