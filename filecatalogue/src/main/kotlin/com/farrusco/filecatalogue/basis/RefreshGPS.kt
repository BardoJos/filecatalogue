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

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.farrusco.filecatalogue.R
import com.farrusco.filecatalogue.business.Category
import com.farrusco.filecatalogue.business.Product
import com.farrusco.projectclasses.activity.BaseActivity
import com.farrusco.projectclasses.utils.Logging
import com.farrusco.projectclasses.messages.Mess
import com.farrusco.projectclasses.messages.ToastExt

open class RefreshGPS: BaseActivity() {
    override val layoutResourceId: Int = R.layout.activity_empty
    override val mainViewId: Int = R.id.viewMain

    private lateinit var product: Product
    private var mess = ""
    private val finishedHandler = Handler(Looper.getMainLooper())
    private var refreshGPSLoader: RefreshGPSLoader? = null
    private var context: Context? = null

    override fun initActivity() {
        this.setVisible(false)
        product = Product(this)
        context = this
        refreshGPSLoader = RefreshGPSLoader()
        val alertDialog = Mess.buildAlertDialog(this,layoutInflater,
            getString(R.string.refreshgps),
            getString( R.string.mess_DoYouWantToRefresh))
        with(alertDialog){
            setPositiveButton(
                com.farrusco.projectclasses.R.string.yes
            ) { _, _ ->
                ToastExt().makeText(
                    this@RefreshGPS,R.string.mess067_taskStart, Toast.LENGTH_LONG).show()
                refreshGPSLoader!!.start()
                this@RefreshGPS.finish()
            }

            setNeutralButton(
                com.farrusco.projectclasses.R.string.no
            ) { dialog, _ ->
                dialog.cancel()
                this@RefreshGPS.finish()
            }
            show()
        }
    }

    internal inner class RefreshGPSLoader : Thread() {
        override fun run() {
           try {
               val rtn = product.refreshGPSInfo(this@RefreshGPS)
               Category(this@RefreshGPS).renumLevel()
               mess = if (rtn.returnValue){
                   getText(R.string.mess054_totalUpdated).toString().replace("%0%",rtn.rowsaffected.toString())
               } else {
                   getText(rtn.messnr).toString()
               }
            } catch (e: Exception) {
                Logging.d("RefreshGPSLoader", e.toString())
            }
            finishedHandler.post { stopThread() }
        }
    }

    fun stopThread() {
        refreshGPSLoader == null
        if (mess != "") {
            ToastExt().makeText(this@RefreshGPS, mess, Toast.LENGTH_LONG).show()
        } else {
            ToastExt().makeText(this@RefreshGPS,R.string.mess068_taskStop, Toast.LENGTH_SHORT).show()
        }
        finish()
    }
}