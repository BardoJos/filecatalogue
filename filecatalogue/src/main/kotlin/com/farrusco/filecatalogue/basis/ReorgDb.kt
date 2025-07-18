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

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.farrusco.filecatalogue.business.Category
import com.farrusco.filecatalogue.business.Help
import com.farrusco.filecatalogue.business.OrderLine
import com.farrusco.filecatalogue.business.Product
import com.farrusco.filecatalogue.business.ProductRel
import com.farrusco.filecatalogue.business.Systeem
import com.farrusco.filecatalogue.common.ConstantsLocal
import com.farrusco.filecatalogue.common.SystemAttr
import com.farrusco.filecatalogue.tables.TableProduct
import com.farrusco.projectclasses.R
import com.farrusco.projectclasses.activity.BaseActivity
import com.farrusco.projectclasses.databases.DatabaseHelper
import com.farrusco.projectclasses.databases.tables.CursorX.getColumnValueString
import com.farrusco.projectclasses.utils.Logging
import com.farrusco.projectclasses.messages.Mess
import com.farrusco.projectclasses.messages.ToastExt
import java.io.File

class ReorgDb: BaseActivity()  {
    override val layoutResourceId: Int = com.farrusco.filecatalogue.R.layout.activity_empty
    override val mainViewId: Int = com.farrusco.filecatalogue.R.id.viewMain
    private val finishedHandler = Handler(Looper.getMainLooper())
    private var reorgLoader: ReorgLoader? = null
    private var messNo = 0
    override fun initActivity() {
        helpText = Help().getHelpTitle(className)
        this.setVisible(false)
        reorgLoader = ReorgLoader()
        reorgDatabase()
    }

    private fun reorgDatabase(){
        val alertDialog = Mess.buildAlertDialog(this,layoutInflater,
            getString(R.string.reorg),
            getString( R.string.mess036_DoYouWantToReorg))

        with(alertDialog){
            setPositiveButton(
                R.string.yes
            ) { _, _ ->
                ToastExt().makeText(
                    this@ReorgDb, com.farrusco.filecatalogue.R.string.mess067_taskStart, Toast.LENGTH_LONG).show()
                reorgLoader!!.start()
                this@ReorgDb.finish()
            }
            // Setting Positive Yes Btn
            setNeutralButton(
                R.string.no
            ) { dialog, _ ->
                dialog.cancel()
                this@ReorgDb.finish()
            }
            show()
        }
    }
    private fun startReorg(){
        val tag = "reorgDatabase"
        Logging.d(tag, ">>>>>>>>>>>>> start <<<<<<<<<<<<")

        val exp = DatabaseHelper(this)
        if (!exp.reorgDatabase().returnValue) {
            messNo = R.string.mess_reorgdb_not_ready
            return
        }

        val tblSysteem = Systeem(this)
        val tblProduct = Product(this)
        val tblProductRel = ProductRel(this)
        val tblOrderLine = OrderLine()
        val bDelete = (tblSysteem.getValue(SystemAttr.DeleteEmptyProduct) != "0")
        // delete empty of not existing file(s)
        if (bDelete) {
            var where = ""
            val rtn = tblProduct.getProduct(0)
            if (rtn.cursor.moveToFirst()) {
                do {
                    val filename =
                        rtn.cursor.getColumnValueString(TableProduct.Columns.filename.name)
                    val dirname = rtn.cursor.getColumnValueString(TableProduct.Columns.dirname.name)
                    val id = rtn.cursor.getColumnValueString(TableProduct.Columns._id.name)
                    if (filename.isNullOrEmpty() || !File("$dirname/$filename").isFile) {
                        where += "$id,"
                    }
                } while (rtn.cursor.moveToNext())
            }
            rtn.cursorClose()
            if (where.isNotEmpty()) {
                tblProduct.delete(
                    TableProduct.Columns._id.name + " in (${
                        where.substring(
                            0,
                            where.length - 1
                        )
                    })"
                )
            }
        }
        // delete duplicate rows

        tblProduct.delete(" ${TableProduct.Columns.type.name} = ${ConstantsLocal.TYPE_FILE} " +
                "and exists(select 1 from ${TableProduct.TABLE_NAME} b" +
                " where ${TableProduct.TABLE_NAME}.${TableProduct.Columns.filename.name} = b.${TableProduct.Columns.filename.name}" +
                " and ${TableProduct.TABLE_NAME}.${TableProduct.Columns.dirname.name} = b.${TableProduct.Columns.dirname.name}" +
                " and ${TableProduct.TABLE_NAME}.${TableProduct.Columns._id.name} < b.${TableProduct.Columns._id.name} " +
                " and ${TableProduct.TABLE_NAME}.${TableProduct.Columns.type.name}  = b.${TableProduct.Columns.type.name} )")

        tblOrderLine.deleteNotProduct()
        tblProductRel.deleteNotProduct()

        Category(this).renumLevel()

        Systeem(this).systeemRegistration()

        Logging.d(tag, ">>>>>>>>>>>>> stop <<<<<<<<<<<<<")
        messNo  = R.string.mess022_reorgdb_ready
    }
    internal inner class ReorgLoader : Thread() {
        override fun run() {
            try {
                startReorg()
            } catch (e: Exception) {
                Logging.d("RefreshGPSLoader", e.toString())
            }
            finishedHandler.post { stopThread() }
        }
    }

    fun stopThread() {
        if (messNo != 0) {
            ToastExt().makeText(this@ReorgDb, messNo, Toast.LENGTH_LONG).show()
        } else {
            ToastExt().makeText(this@ReorgDb,
                com.farrusco.filecatalogue.R.string.mess068_taskStop, Toast.LENGTH_SHORT).show()
        }
        finish()
    }
}