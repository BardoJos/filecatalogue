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
package com.farrusco.filecatalogue.main

import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.window.layout.WindowMetricsCalculator
import com.farrusco.filecatalogue.BuildConfig
import com.farrusco.filecatalogue.R
import com.farrusco.filecatalogue.business.Help
import com.farrusco.filecatalogue.business.Systeem
import com.farrusco.filecatalogue.common.ConstantsLocal
import com.farrusco.filecatalogue.tables.TableHelp
import com.farrusco.projectclasses.common.Constants
import com.farrusco.projectclasses.databases.DatabaseHelper
import com.farrusco.projectclasses.databases.tables.DBUtils
import com.farrusco.projectclasses.messages.Mess
import com.farrusco.projectclasses.utils.FilesFolders
import net.sqlcipher.database.SQLiteDatabase
import java.io.File
import java.text.SimpleDateFormat

class ApiMainPrepare  : AppCompatActivity() {
/*    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        //Logging.e(getString(R.string.app_name) + " prepare: duplicate startup" )

        //NotificationMan(this).show()

        //finishAffinity()
        //System.exit(0)

      *//*  val intent = Intent(this, ApiMainMenu::class.java).putExtra(this::class.simpleName,1)
        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
        resultLauncher.launch(intent)*//*
    }*/

    public override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(com.farrusco.projectclasses.R.style.AppThemeBase)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_startup)
        ConstantsLocal.dateFormat = (android.text.format.DateFormat.getDateFormat(this) as SimpleDateFormat).toPattern()
        Constants.setBasePath( this )

        //ConstantsLocal.messDialog = Mess(this, getText(R.string.app_name).toString() + "\n" + //getText(R.string.mess026_one_moment).toString())

        if (getText(R.string.AD_MOB_APP_ID) != BuildConfig.AD_MOB_APP_ID){
            finish()
        }

        Constants.APP_NAME = getString(R.string.app_name)
        Constants.SqlDbStructureId = R.raw.dbstructure
        Constants.DATABASE_NAME = BuildConfig.DATABASE_NAME
        @Suppress("RemoveRedundantQualifierName")
        Constants.DATABASE_PW = com.farrusco.filecatalogue.BuildConfig.AD_MOB_APP_ID.reversed()
        Constants.DATABASE_VERSION = BuildConfig.VERSION_CODE
        Constants.ChangeLog = R.raw.changelog
        Constants.SqlReorgId = R.raw.reorg
        Constants.SqlSystemId = R.raw.system
        Constants.SqlSampleBackupId = 0
        Constants.SqlSampleDataId = R.raw.sampledata
        Constants.SqlFileExtensionDataId = 0
        //Constants.localFragmentActivity = this
        Constants.localFragmentManager = supportFragmentManager
        Constants.localLifecycle = lifecycle
        Constants.decimalSeparator = "."
        val windowMetrics = WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(this)
        Constants.currentBounds = windowMetrics.bounds

        if(BuildConfig.DEBUG) {
            StrictMode.enableDefaults()
            StrictMode.allowThreadDiskReads()
            StrictMode.allowThreadDiskWrites()
        }

        if (!FilesFolders.hasFileAccess(getDatabasePath(Constants.DATABASE_NAME).path)){
            createDatabase()
        }

        if (!DBUtils.openOrCreateDatabase(this).returnValue){
            val alertDialog = Mess.buildAlertDialog(this,layoutInflater,
                getString(R.string.sampledb),
                getString(R.string.mess_DoYouWantSampleDb))

            // Setting Positive Yes Button
            alertDialog.setPositiveButton(com.farrusco.projectclasses.R.string.yes
            ) { _, _ ->
                val dbFile: File = getDatabasePath(BuildConfig.DATABASE_NAME)
                if (dbFile.exists()) dbFile.delete()
                createDatabase()
            }
            // Setting Positive Yes Btn
            alertDialog.setNeutralButton(com.farrusco.projectclasses.R.string.no
            ) { dialog, _ ->
                dialog.cancel()
                finish()
            }
            // Setting Positive "Cancel" Btn
            alertDialog.setCancelable(false)
            // Showing Alert Dialog
            alertDialog.show()
            return
        }

        if (!Systeem.validateDatabase(this).returnValue){
            val dbFile: File = getDatabasePath(BuildConfig.DATABASE_NAME)
            if (dbFile.exists()) dbFile.delete()
            createDatabase()
        }
        Constants.helpTableName = TableHelp.TABLE_NAME
        val intent = Intent(this, ApiMainMenu::class.java).putExtra(this::class.simpleName,1)
        resultLauncher.launch(intent)
    }

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        finish()
    }

    private fun createDatabase(){
        val exp = DatabaseHelper(this)
        exp.createDatabase(BuildConfig.ADS)
        Help().createActivities(this)
    }
}