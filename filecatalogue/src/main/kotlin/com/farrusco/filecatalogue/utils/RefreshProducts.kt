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

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.farrusco.filecatalogue.R
import com.farrusco.filecatalogue.business.Product
import com.farrusco.filecatalogue.business.ProductRel
import com.farrusco.filecatalogue.business.Systeem
import com.farrusco.filecatalogue.common.SystemAttr
import com.farrusco.projectclasses.messages.ToastExt
import com.farrusco.projectclasses.utils.Logging
import com.farrusco.projectclasses.utils.ReturnValue
import com.farrusco.projectclasses.widget.ButtonExt

class RefreshProducts(context: Context, showMess: Boolean) {

    private val mContext: Context = context
    private val mShowMess = showMess
    private var bRefresh = false
    private var refreshLoader: RefreshLoader? = null
    private val finishedHandler = Handler(Looper.getMainLooper())
    private var product: Product = Product(context)
    private var systeem: Systeem = Systeem(context)
    private lateinit var buttonRefresh: ButtonExt

    private var rtnFilDb = ReturnValue()

    private var mess = 0
    private var strMess = ""
    fun startTask(productid:Int, dirname: String, buttonRefresh: ButtonExt?){
        if (mShowMess){
            ToastExt().makeText(
                mContext,R.string.mess067_taskStart, Toast.LENGTH_LONG).show()
        }
        if (buttonRefresh != null) {
            this.buttonRefresh = buttonRefresh
        }
        refreshLoader = RefreshLoader()
        refreshLoader!!.dirname = dirname
        refreshLoader!!.productid = productid
        refreshLoader!!.start()
    }

    internal inner class RefreshLoader : Thread() {
        var productid = 0
        var dirname = ""

        override fun run() {
            rtnFilDb = fillDb(productid, dirname)
            bRefresh=false
            finishedHandler.post { stopThread() }
        }
    }

    private fun stopThread() {
        if (::buttonRefresh.isInitialized){
            buttonRefresh.isEnabled = true
        }
        if (mShowMess) {
            if (strMess != "") ToastExt().makeText(mContext, strMess, Toast.LENGTH_LONG)
                .show() else if (mess > 0) ToastExt().makeText(mContext, mess, Toast.LENGTH_LONG).show()
            else ToastExt().makeText( mContext, R.string.mess068_taskStop, Toast.LENGTH_SHORT).show()
        } else {
            if (strMess != "") ToastExt().makeText(mContext, strMess, Toast.LENGTH_LONG)
                .show() else if (mess > 0) ToastExt().makeText(mContext, mess, Toast.LENGTH_LONG).show()
        }
    }

    private fun fillDb(productid:Int, dirname: String): ReturnValue {

        val rtnProduct = product.visitAllDirsAndFiles(mContext, productid)
        if (!rtnProduct.returnValue){
            rtnProduct.cursorClose()
            Logging.i("ListFileDirs/fillDb","no data productid: $productid")
            return rtnProduct
        }
        val filename = "$dirname/" + systeem.getValue(SystemAttr.CvsName)
        val product = Product(mContext)
        product.readProductPrices(filename)
        if (rtnProduct.rowsaffected > 0){
            Logging.d("ListFileDirs/fillDb","Total inserted: ${rtnProduct.rowsaffected}")
        }

        // link orphan product to folder
        ProductRel(mContext).linkProduct()
        rtnProduct.cursorClose()
        return rtnProduct
    }
}