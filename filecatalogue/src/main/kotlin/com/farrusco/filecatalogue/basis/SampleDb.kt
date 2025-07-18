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
import com.farrusco.filecatalogue.BuildConfig
import com.farrusco.filecatalogue.business.Help
import com.farrusco.projectclasses.R
import com.farrusco.projectclasses.activity.BaseActivity
import com.farrusco.projectclasses.databases.DatabaseHelper
import com.farrusco.projectclasses.utils.Logging
import com.farrusco.projectclasses.messages.Mess
import com.farrusco.projectclasses.messages.ToastExt

open class SampleDb: BaseActivity() {
    override val layoutResourceId: Int = com.farrusco.filecatalogue.R.layout.activity_empty
    override val mainViewId: Int = com.farrusco.filecatalogue.R.id.viewMain
    private val finishedHandler = Handler(Looper.getMainLooper())
    @Suppress("RemoveRedundantQualifierName")
    private var sampleLoader: SampleDb.SampleLoader? = null
    private var messNo = 0
    override fun initActivity() {
        this.setVisible(false)
        helpText = Help().getHelpTitle(className)
        sampleLoader = SampleLoader(this)

        val alertDialog = Mess.buildAlertDialog(this,layoutInflater,
            getString(R.string.sampledb),
            getString( R.string.mess_DoYouWantSampleDb))

        with(alertDialog){
            setPositiveButton(
                R.string.yes
            ) { _, _ ->
                ToastExt().makeText(
                    this@SampleDb, com.farrusco.filecatalogue.R.string.mess067_taskStart, Toast.LENGTH_LONG).show()
                sampleLoader!!.start()
                this@SampleDb.finish()
            }
            // Setting Positive Yes Btn
            setNeutralButton(
                R.string.no
            ) { dialog, _ ->
                dialog.cancel()
                this@SampleDb.finish()
            }
            show()
        }
    }

    internal inner class SampleLoader(context: Context) : Thread() {
        private val contextX = context
        override fun run() {
            try {
                val exp = DatabaseHelper(contextX)
                messNo = if (exp.createDatabase(BuildConfig.ADS).returnValue) {
                    R.string.mess021_sampledb_ready
                } else {
                    R.string.mess_sampledb_not_ready
                }
            } catch (e: Exception) {
                Logging.d("RefreshGPSLoader", e.toString())
            }
            finishedHandler.post { stopThread() }
        }
    }

    fun stopThread() {
        if (messNo != 0) {
            ToastExt().makeText(this@SampleDb, messNo, Toast.LENGTH_LONG).show()
        } else {
            ToastExt().makeText(this@SampleDb,
                com.farrusco.filecatalogue.R.string.mess068_taskStop, Toast.LENGTH_SHORT).show()
        }
        finish()
    }
}