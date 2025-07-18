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

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.net.toUri
import androidx.window.layout.WindowMetricsCalculator
import com.farrusco.filecatalogue.BuildConfig
import com.farrusco.filecatalogue.R
import com.farrusco.filecatalogue.business.Help
import com.farrusco.filecatalogue.business.Product
import com.farrusco.filecatalogue.business.Systeem
import com.farrusco.filecatalogue.common.About
import com.farrusco.filecatalogue.common.ConstantsLocal
import com.farrusco.filecatalogue.common.SystemAttr
import com.farrusco.filecatalogue.tables.TableProduct
import com.farrusco.filecatalogue.tables.TableSysteem
import com.farrusco.filecatalogue.utils.RefreshProducts
import com.farrusco.projectclasses.common.Constants
import com.farrusco.projectclasses.common.ConstantsFixed
import com.farrusco.projectclasses.databases.DatabaseHelper
import com.farrusco.projectclasses.databases.tables.CursorX.getColumnValueInt
import com.farrusco.projectclasses.databases.tables.CursorX.getColumnValueString
import com.farrusco.projectclasses.databases.tables.DBUtils
import com.farrusco.projectclasses.messages.Mess
import com.farrusco.projectclasses.messages.ToastExt
import com.farrusco.projectclasses.utils.CalcObjects.stringToInteger
import com.farrusco.projectclasses.utils.Cryption
import com.farrusco.projectclasses.utils.DeviceInfoUtils
import com.farrusco.projectclasses.utils.Logging
import com.farrusco.projectclasses.utils.ViewUtils
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.tabs.TabLayout
import java.io.File
import java.util.Locale
import kotlin.system.exitProcess

class ApiMainMenu : AppCompatActivity() {
    private lateinit var listview: ListView
    private lateinit var mTopToolbar: Toolbar
    private lateinit var mAdViewBottom: AdView
    private lateinit var handlerHelp: Handler
    private lateinit var runnableHelp: Runnable
    private lateinit var handlerDB: Handler
    private lateinit var runnableDB: Runnable
    private lateinit var systeem: Systeem
    private var reorgdb = ""
    private var listQuery = ""
    private var scanImage = ""
    private var scanQR = ""
    private var translateText = ""
    private var faceRecognition = ""
    private var order = ""
    private var help = ""

    @SuppressLint("VisibleForTests")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_startup)
        if (!intent.hasExtra("ApiMainPrepare") || !Constants.isDbInitialized()){
            // google engine is trying again to startup an illegal module
            finish()
        }
        reorgdb = getText(R.string.activity_reorgdb).toString()
        listQuery = getText(R.string.activity_list_query).toString()
        order = getText(R.string.activity_list_order).toString()
        help = getText(R.string.help).toString()
        scanImage = getText(R.string.scanimage).toString()
        scanQR = getText(R.string.scanqr).toString()
        translateText = getText(R.string.translatetext).toString()
        faceRecognition = getText(R.string.refreshface).toString()
        validateDatabase()
    }

    @SuppressLint("VisibleForTests")
    private fun onCreateContinue(createdatabase: Boolean) {

        setContentView(R.layout.activity_main)

        if (createdatabase){
            val exp = DatabaseHelper(this)
            if (!exp.createDatabase(BuildConfig.ADS).returnValue){
                finish()
            }
        }
        systeem = Systeem(this)
        systeem.getProperties()

        try {
            if (checkSelfPermission(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1
                )
            }
        } catch (e: Exception) {
            ToastExt().makeText(
                this,
                getText(R.string.mess049_external_read_error).toString() + e.message.toString(),
                Toast.LENGTH_LONG
            ).show()
            finishAndRemoveTask()
        }
        try {
            if (checkSelfPermission(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1
                )
            }
        } catch (e: Exception) {
            ToastExt().makeText(
                this,
                getText(R.string.mess050_external_write_error).toString() + e.message.toString(),
                Toast.LENGTH_LONG
            ).show()
            finishAndRemoveTask()
        }
/*        try {
            if (checkSelfPermission(
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1
                )
            }
        } catch (e: Exception) {
            ToastExt().makeText(
                this,
                getText(R.string.mess050_external_write_error).toString() + e.message.toString(),
                Toast.LENGTH_LONG
            ).show()
            finishAndRemoveTask()
        }*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            try {
                if (checkSelfPermission(
                        Manifest.permission.ACCESS_MEDIA_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissions(
                        arrayOf(Manifest.permission.ACCESS_MEDIA_LOCATION), 1
                    )
                }
            } catch (e: Exception) {
                ToastExt().makeText(
                    this,
                    getText(R.string.mess065_external_read_error).toString() + e.message.toString(),
                    Toast.LENGTH_LONG
                ).show()
                finishAndRemoveTask()
            }
            try {
                if (checkSelfPermission(
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissions(
                        arrayOf(Manifest.permission.READ_MEDIA_IMAGES), 1
                    )
                }
            } catch (e: Exception) {
                ToastExt().makeText(
                    this,
                    getText(R.string.mess065_external_read_error).toString() + e.message.toString(),
                    Toast.LENGTH_LONG
                ).show()
                finishAndRemoveTask()
            }
        }

        listview = findViewById(R.id.list)

        Locale.getDefault().language // nl
        mTopToolbar = findViewById(R.id.my_toolbar)
        setSupportActionBar(mTopToolbar)

        mAdViewBottom = findViewById(R.id.adViewBottom)

        if(BuildConfig.ADS == -1) {
            var ok = false
            try{
                MobileAds.initialize(this)
                val requestConfiguration = MobileAds.getRequestConfiguration()
                    .toBuilder()
                    .build()
                MobileAds.setRequestConfiguration(requestConfiguration)
                ok = true
            } catch (_: Exception){
                Logging.d("CustomApplication/AdRequest", "Ad failed to load in main1")
            }
            if (ok){
                try{
                    val adRequest = AdRequest.Builder().build()
                    mAdViewBottom.loadAd(adRequest)
                } catch (_: Exception){
                    Logging.i("CustomApplication/AdRequest", "Ad failed to load in main2")
                }
            }

        }else{
            mAdViewBottom.visibility = View.GONE
        }

        try {
            Systeem(this).getProperties()
            if (!ConstantsLocal.isBackdoorOpen) {
                if (ConstantsLocal.registration.isNullOrEmpty()) {
                    Systeem(this).systeemRegistration()
                } else if (ConstantsLocal.registration != DeviceInfoUtils.getIMEI(this)) {
                    // this prevent to use another database with a different mobile
                    ToastExt().makeText(
                        this,
                        R.string.mess052_fatal_db_error,
                        Toast.LENGTH_LONG
                    ).show()
                    val dbfile: File = getDatabasePath(BuildConfig.DATABASE_NAME)
                    if (dbfile.exists()) dbfile.delete()
                    finish()
                }
            }
        } catch (_: Exception) {
            ToastExt().makeText(
                this,
                R.string.mess052_fatal_db_error,
                Toast.LENGTH_LONG
            ).show()
            val dbfile: File = getDatabasePath(BuildConfig.DATABASE_NAME)
            if (dbfile.exists()) dbfile.delete()
            finish()
        }

        dbstructureLoader()

        if (Constants.isHelpEnabled){
            try {
                helpLoader()
            } catch (_: Exception){
                Constants.isHelpEnabled=false
            }
        }

        if (ConstantsLocal.isAutoRefreshFolder){
            photosLoader(this)
        }

        var path = intent.getStringExtra("com.android.apis.Path")

        // in case of smart user added manually photos to the database
        Product(this).cleanupDemo()

        if (path == null) {
            path = "Project"
        }

        listview.isTextFilterEnabled = true
        setFragment(path)

        listview.onItemClickListener = awesomeOnClickListener

        val mTabLayout = findViewById<TabLayout>(R.id.tablayout)
        // use tag to filter activities because text is translated
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.base).setIcon(R.drawable.ic_school_black_24dp).setTag("base"))
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.project).setIcon(R.drawable.ic_home_black_24dp).setTag("project"))
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.system).setIcon(R.drawable.ic_settings_black_24dp).setTag("system"))

        mTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab!!.icon!!.setTint(Color.WHITE)
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (ConstantsLocal.isBackdoorOpen){
                    tab!!.icon!!.setTint(Color.GREEN)
                } else {
                    tab!!.icon!!.setTint(Color.RED)
                }
                setFragment(tab.tag.toString())
            }
        })
        listview.visibility = View.VISIBLE
        mTabLayout.getTabAt(1)!!.select()

    }
/*    private fun dumpAllThreads() {
        val ps = PrintStream("dumpAllThreads.txt")
        ps.println("--------------------------------")
        ps.print("Timestamp: ")
        ps.println(System.currentTimeMillis())
        //v.printStackTrace(ps)
        ps.println("Dump all threads")
        for ((key, value) in Thread.getAllStackTraces()) {
            ps.println(key)
            if (value.size > 0) {
                for (ste in value) {
                    ps.print('\t')
                    ps.println(ste)
                }
            } else {
                ps.println("\t[no stack trace]")
            }
        }
        ps.println("================================")
        ps.flush()
    }*/

    private fun helpLoader()
    {
        handlerHelp = Handler(Looper.getMainLooper())
        runnableHelp = object: Runnable
        {
            override fun run()
            {
                try {
                    Help().createActivities(this@ApiMainMenu)
                    handlerHelp.removeCallbacks(this)
                } catch (e: NullPointerException) {
                    Logging.d(Constants.APP_NAME + "/helpLoader:\n" + e.stackTraceToString())
                } catch (e: InterruptedException) {
                    Logging.d(Constants.APP_NAME + "/helpLoader:\n" + e.stackTraceToString())
                }
            }
        }
        handlerHelp.post(runnableHelp)
    }

    private fun dbstructureLoader()
    {
        handlerDB = Handler(Looper.getMainLooper())
        runnableDB = object: Runnable
        {
            override fun run()
            {
                try {
                    opendb()
                    DBUtils.getDBStructure()
                    handlerDB.removeCallbacks(this)
                } catch (e: NullPointerException) {
                    Logging.d(Constants.APP_NAME + "/dbstructureLoader:\n" + e.stackTraceToString())
                } catch (e: InterruptedException) {
                    Logging.d(Constants.APP_NAME + "/dbstructureLoader:\n" + e.stackTraceToString())
                }
            }
        }
        handlerDB.post(runnableDB)
    }

    private fun validateDatabase() {
        val value = try {
            DBUtils.eLookUp(
                TableSysteem.Columns.systeemvalue.name,
                TableSysteem.TABLE_NAME,
                TableSysteem.Columns.systeemkey.name + "=${SystemAttr.Version.internal}"
            ).toString().toInt()
        } catch (_: Exception){
            -2
        }
        if (value == Constants.DATABASE_VERSION) {
            onCreateContinue(false)
        } else {
            val alertDialog = Mess.buildAlertDialog(
                this@ApiMainMenu, layoutInflater,
                resources.getString(R.string.database),
                getString(R.string.mess052_fatal_db_error)
            )
            with(alertDialog) {
                setPositiveButton(
                    R.string.yes
                ) { _, _ ->
                    onCreateContinue(true)
                }
                setNeutralButton(
                    R.string.no
                ) { _, _ ->
                    finish()
                }
                show()
            }
        }
    }

    @Suppress("unused")
    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        //resultActivity(result)
        //if (result.resultCode == Activity.RESULT_OK) {
        //    val data: Intent? = result.data
        //}
    }

    private fun setFragment(path: String) {
        listview.adapter =
            SimpleAdapter(
                this,
                getData(path),
                R.layout.navigation_item,
                arrayOf("title"),
                intArrayOf(R.id.textView)
            )
    }

    // List<? extends Map<String, ?>>
    @SuppressLint("QueryPermissionsNeeded")
    private fun getData(prefix: String): MutableList<Map<String, *>> {
        val cat = getText(R.string.packagename).toString()
        val myData: MutableList<Map<String, *>> = mutableListOf()
        val mainIntent = Intent( "$cat.$prefix", null)
        val pm = packageManager
        //val list: List<ResolveInfo>
/*        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) {
            // Build.VERSION_CODES.TIRAMISU
            list = pm.queryIntentActivities(mainIntent, PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong()))
        } else{*/
        //    list = pm.queryIntentActivities(mainIntent, 0)
        //}
        val list = pm.queryIntentActivities(mainIntent, 0)
        list.forEach {
            var bContinue = true
            val labelSeq = it.loadLabel(pm)
            val label = it.activityInfo.name

            if (!Constants.isHelpEnabled  && labelSeq.indexOf(help,0,true) >= 0) bContinue=false
            if (bContinue && !ConstantsLocal.isReorgEnabled         && labelSeq.indexOf(reorgdb,0,true) >= 0) bContinue=false
            if (bContinue && !ConstantsLocal.isOrderEnabled         && labelSeq.indexOf(order,0,true) >= 0) bContinue=false
            if (bContinue && !ConstantsLocal.isStoreQuery           && labelSeq.indexOf(listQuery,0,true) >= 0) bContinue=false
            if (bContinue && !ConstantsLocal.isScanImageEnabled     && labelSeq.indexOf(scanImage,0,true) >= 0) bContinue=false
            if (bContinue && !ConstantsLocal.isScannerEnabled       && labelSeq.indexOf(scanQR,0,true) >= 0) bContinue=false
            if (bContinue && !ConstantsLocal.isTranslateTextEnabled && labelSeq.indexOf(translateText,0,true) >= 0) bContinue=false
            if (bContinue && !ConstantsLocal.isFaceRecognitionEnabled && labelSeq.indexOf(faceRecognition,0,true) >= 0) bContinue=false

            if (bContinue && !Constants.isDebuggable){
                if (label.indexOf(reorgdb,0,true) >= 0) bContinue=false
            }

            if (bContinue) {
                addItem(
                    myData,
                    labelSeq.toString(),
                    it.activityInfo.labelRes,
                    activityIntent(
                        it.activityInfo.applicationInfo.packageName,
                        it.activityInfo.name
                    )
                )
            }
        }
        //Collections.sort(myData, sDisplayNameComparator)
        return myData
    }

    private fun activityIntent(pkg: String?, componentName: String?): Intent {
        val result = Intent()
        result.setClassName(pkg!!, componentName!!)
        return result
    }

    private fun addItem(
        data: MutableList<Map<String, *>>, name: String, labelRes: Int,
        intent: Intent
    ) {
        val temp: MutableMap<String, Any> = HashMap()
        temp["title"] = name
        temp["intent"] = intent
        if (labelRes > 0) temp["labelres"] = labelRes
        data.add(temp)
    }

    //@SuppressWarnings("rawtypes")
    private val awesomeOnClickListener =
        OnItemClickListener { parent, _, position, _ ->
            //view.preventDoubleClick()
            opendb()
            parent.isEnabled = false
            parent.postDelayed( { parent.isEnabled = true }, 1000)

            val map = listview.getItemAtPosition(position) as Map<*, *>
            val intent = map["intent"] as Intent?
            if (intent!!.component!!.className.indexOf("$") > 0) {
                var className = intent.component!!.className
                className = intent.component!!.className
                    .substring(0, className.indexOf("$"))
                intent.setClassName(this@ApiMainMenu, className)
                val x = stringToInteger(map["labelres"].toString())
                if (x > 0) intent.putExtra("labelres", x.toString())
            }
            startActivity(intent)
        }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.clear()

        menu.add(Menu.NONE, ConstantsFixed.ABOUT_ID, Menu.NONE, ViewUtils.menuIconWithText(this,
            R.drawable.about_100, getText(R.string.about).toString()))
        menu.add(Menu.NONE, ConstantsFixed.LOG_ID, Menu.NONE, ViewUtils.menuIconWithText(this,
            R.drawable.available_updates, getText(R.string.changelog).toString()))
        menu.add(Menu.NONE, ConstantsFixed.SITE_ID, Menu.NONE, ViewUtils.menuIconWithText(this,
            R.drawable.website_48, getText(R.string.site).toString()))

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // prevent turning screen in sub-screen only SplashScreen is allowed
        Constants.orientationActivity = newConfig.orientation + 6
        if (newConfig.orientation==2) {
            Constants.orientationActivity = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }else{
            Constants.orientationActivity = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        }
        val windowMetrics = WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(this)
        Constants.currentBounds = windowMetrics.bounds
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            ConstantsFixed.ABOUT_ID -> {
                startActivity(Intent(this, About::class.java))
            }
            ConstantsFixed.LOG_ID -> {
                val logDialog = Mess.buildLogDialog(this,layoutInflater,
                    getString(R.string.changelog_title))
                with(logDialog){
                    setPositiveButton(
                        com.farrusco.projectclasses.R.string.ok
                    ) { dialog, _ ->
                        dialog.cancel()
                    }
                    setCancelable(false)
                    show()
                }
            }
            ConstantsFixed.SITE_ID -> {
                val codedString = "Pw0Rj3mlIZY=\n]MsRQew/pDuiRgkwIOWkf5BwXvcofjjdFdYVRaFsQDFMuBUqgEz2GGqPZOfY/oa9m\n"
                val decryptedData = Cryption(Cryption.TRANSFORMATION_DES).decrypt(codedString, "31711574",true)
                val appData = decryptedData.toUri()

                val intent = Intent()
                intent.action = Intent.ACTION_VIEW
                intent.data = appData
                intent.putExtra(
                    "helptext", String.format(
                        getText(R.string.about_text1).toString(),
                        resources.getString(R.string.app_name),
                        BuildConfig.DATABASE_NAME + " Database v" + BuildConfig.VERSION_CODE
                    )
                )
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        if (::mAdViewBottom.isInitialized) {
            mAdViewBottom.resume()
        }
    }

    override fun onPause() {
        if (::mAdViewBottom.isInitialized) {
            mAdViewBottom.pause()
        }
        super.onPause()
    }

    override fun onDestroy() {
        if (::mAdViewBottom.isInitialized) {
            mAdViewBottom.destroy()
        }
        Constants.localFragmentManager = null
        Constants.closeSQLiteDatabase()
        super.onDestroy()
        exitProcess(0)
    }

    //companion object {
        //private var animate = false
        //lateinit var localFragmentManager1: FragmentManager
        //lateinit var localLifecycle1: Lifecycle
/*    private val sDisplayNameComparator: Comparator<Map<*, *>> =
            object : Comparator<Map<*, *>> {
                private val collator = Collator.getInstance()
                override fun compare(map1: Map<*, *>, map2: Map<*, *>): Int {
                    return collator.compare(map1["title"], map2["title"])
                }
            }*/

    private fun opendb(){
        if (!DBUtils.openOrCreateDatabase(this@ApiMainMenu).returnValue){
            // illegal close of database
            Logging.e( "opendb: illegal close of database" )
            this.finish()
        }
    }

    private fun photosLoader(context: Context)
    {
        val refreshProducts = RefreshProducts(this, false)
        val product = Product(context)
        val rtn = product.getDirectory(0)
        if (rtn.cursor.moveToFirst()){
            do {
                val productid = rtn.cursor.getColumnValueInt(TableProduct.Columns._id.name)!!
                val dirname = rtn.cursor.getColumnValueString(TableProduct.Columns.dirname.name)!!
                refreshProducts.startTask(productid, dirname, null)
            } while (rtn.cursor.moveToNext() )
        }
        rtn.cursorClose()

    }
}