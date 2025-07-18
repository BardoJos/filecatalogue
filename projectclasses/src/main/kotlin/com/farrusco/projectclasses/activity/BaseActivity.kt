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
package com.farrusco.projectclasses.activity

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.database.Cursor
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.farrusco.projectclasses.R
import com.farrusco.projectclasses.common.Constants
import com.farrusco.projectclasses.common.ConstantsFixed
import com.farrusco.projectclasses.common.ShowHelp
import com.farrusco.projectclasses.databases.tables.DBUtils
import com.farrusco.projectclasses.messages.Mess
import com.farrusco.projectclasses.messages.ToastExt
import com.farrusco.projectclasses.utils.CalcObjects
import com.farrusco.projectclasses.utils.Logging
import com.farrusco.projectclasses.utils.ReturnValue
import com.farrusco.projectclasses.utils.TagModify
import com.farrusco.projectclasses.utils.ViewUtils
import kotlin.system.exitProcess

abstract class BaseActivity : AppCompatActivity(), FragmentCreate.OnFragmentAttachListener {
    lateinit var viewMain: View
    lateinit var currView: View
    var currMenu: Int = 0
    var lstMenu: ArrayList<MenuDefinition> = ArrayList()

    private var bOnBackPressedCheck = false
    private var bUpdatePrevScreen = false
    private var toolbar: androidx.appcompat.widget.Toolbar? = null
    abstract val layoutResourceId: Int
    abstract val mainViewId: Int
    open val groupNo: Int= -1
    open var className: String= ""
    open var helpText: String= ""
    val icMenuUpload: Int= R.drawable.ic_menu_upload
    val icMenuZoom: Int= R.drawable.ic_menu_zoom
    val icMenuEdit: Int = 17301566

    var currentModi: ConstantsFixed.ScreenModi? = null

    abstract fun initActivity()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        if (Constants.SqlSystemId == 0){
            // illegal startup
            Logging.e(getString(R.string.app_name) + ": illegal startup" )
            this.finish()
            exitProcess(-1)
        }
        if (!DBUtils.isDatabaseOpen().returnValue){
                // illegal startup
                Logging.d(getString(R.string.app_name) + ": database error" )
                this.finish()
                exitProcess(-1)
        }

        onBackPressedDispatcher.addCallback(this,onBackPressedCallback)

        className = this::class.simpleName.toString()
        //intentMain=IntentExt(intent)
        var action = ""
        if (intent.hasExtra(ConstantsFixed.TagSection.TsModFlag.name)){
            action = CalcObjects.getString(intent.getStringExtra(ConstantsFixed.TagSection.TsModFlag.name))
        }

        currentModi = when (action) {
            "" -> {
                ConstantsFixed.ScreenModi.ModeBrowse
            }
            ConstantsFixed.TagAction.Insert.name -> {
                ConstantsFixed.ScreenModi.ModeInsert
            }
            ConstantsFixed.TagAction.New.name -> {
                ConstantsFixed.ScreenModi.ModeNew
            }
            ConstantsFixed.TagAction.Edit.name -> {
                ConstantsFixed.ScreenModi.ModeEdit
            }
            ConstantsFixed.TagAction.Delete.name -> {
                ConstantsFixed.ScreenModi.ModeDelete
            }
            else -> {
                ConstantsFixed.ScreenModi.ModeBrowse
            }
        }

        supportActionBar?.setDisplayShowHomeEnabled(false) // hide icon
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.setDisplayShowCustomEnabled(true)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        // this one is needed otherwise it will abort with a dialog windows
        setContentView(layoutResourceId)

        viewMain = findViewById(mainViewId)
        toolbar = findViewById(R.id.my_toolbar)
        //toolbar = viewMain.findViewById<androidx.appcompat.widget.Toolbar>(R.id.my_toolbar)
        if (toolbar != null){
            setSupportActionBar(toolbar)
        }

        bOnBackPressedCheck = true
        window.navigationBarColor
        this.requestedOrientation = Constants.orientationActivity

        initActivity()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        this.requestedOrientation = Constants.orientationActivity
    }

    open fun addHiddenFields(cursor: Cursor, vi: View, groupNo: Int) {
        val arrChildDBNames1 = ArrayList<String?>()
        val arrChildDBColumns: ArrayList<View> =
            ViewUtils.getChildDBColumns(vi as ViewGroup, groupNo)

        for (i in arrChildDBColumns.indices) {
            arrChildDBNames1.add( ViewUtils.getDBColumn(arrChildDBColumns[i]))
        }
        for (i in 0 until cursor.columnCount) {
            if (!arrChildDBNames1.contains(cursor.getColumnName(i))) {
                ViewUtils.addHiddenField(vi, cursor.getColumnName(i), cursor.getString(i), groupNo)
            }
        }
    }

    override fun onFragmentAttach(link: String?) {
        // DO NOT DELETE this one. it is use to compile
        // onFragmentAttach method
        // link: f0 - fx = fragment sequence number
    }

    open fun validateScreen(): ReturnValue {
        if (!validateEditTextNulls().returnValue) {
            ToastExt().makeText(this, R.string.mess003_notnull, Toast.LENGTH_LONG).show()
            return ReturnValue().setReturnvalue(false).setShowmessage(false)
        }
        return ReturnValue().setReturnvalue(true)
    }

    open fun saveScreen(): ReturnValue {
        val rtn: ReturnValue = validateScreen()
        if (!rtn.returnValue) {
            if (rtn.messnr > 0) {
                ToastExt().makeText(this, rtn.messnr, Toast.LENGTH_LONG)
                    .show()
            } else {
                ToastExt().makeText(
                    this,
                    R.string.mess033_validateerror,
                    Toast.LENGTH_LONG
                ).show()
            }
            return ReturnValue().setReturnvalue(false).setShowmessage(false)
        }
        return rtn
    }

    var fieldClickListenerMenu = View.OnClickListener { v ->
        currView = v
        openContextMenu(v)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            ConstantsFixed.SAVE_ID -> {
                saveScreen()
                true
            }
            ConstantsFixed.HELP_ID -> {
                showHelp()
                true
            }
            ConstantsFixed.MAIL_ID -> {
                sendMail()
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    fun setUpdatePrevScreen(updatePrevScreen: Boolean) {
        bUpdatePrevScreen = updatePrevScreen
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            //showing dialog and then closing the application..
            exitOnBackPressed()
        }
    }

    open fun exitOnBackPressed(){
        if (bOnBackPressedCheck && hasChildModificationTag()) {
            val alertDialog = Mess.buildAlertDialog(this,layoutInflater,
                resources.getString(if (bUpdatePrevScreen) R.string.update else R.string.save),
                getString( R.string.mess034_DoYouWantToSave))
            alertDialog.setCancelable(true)
            with(alertDialog){
                setPositiveButton(
                    R.string.yes
                ) { _, _ ->
                    if (saveScreen().returnValue) {
                        setResult(RESULT_OK,  intent)
                        finish()
                    }
                }
                setNegativeButton(
                    R.string.no
                ) { _ , _ ->
                    setResult(RESULT_CANCELED,  Intent())
                    finish()
                }
                setNeutralButton(
                    R.string.cancel
                ) { _ , _ ->
                    setResult(RESULT_CANCELED,  Intent())
                }
                show()
            }
        } else {
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    open fun dialogModification(): Int{
        var rtn = 0
        if (hasChildModificationTag()) {
            val alertDialog = Mess.buildAlertDialog(this,layoutInflater,
                resources.getString(if (bUpdatePrevScreen) R.string.update else R.string.save),
                getString( R.string.mess034_DoYouWantToSave))
            alertDialog.setCancelable(true)
            with(alertDialog){
                setPositiveButton(
                    R.string.yes
                ) { _, _ ->
                    rtn = -1
                }
                setNegativeButton(
                    R.string.no
                ) { _ , _ ->
                    rtn = 0
                }
                setNeutralButton(
                    R.string.cancel
                ) { _ , _ ->
                    rtn = 1
                }
                show()
            }
        } else {
            rtn = -1
        }
        return rtn
    }

    fun hasChildModificationTag(): Boolean {
        if (ViewUtils.hasChildTag(viewMain as ViewGroup, ConstantsFixed.TagSection.TsUserFlag.name, ConstantsFixed.TagAction.Edit.name)) return true
        return ViewUtils.hasChildTag(viewMain as ViewGroup, ConstantsFixed.TagSection.TsUserFlag.name, ConstantsFixed.TagAction.Delete.name)
    }

    fun validateEditTextNulls(): ReturnValue {
        return ViewUtils.validateEditTextNulls(viewMain as ViewGroup?)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.clear()
        if (currMenu < lstMenu.size
            && currMenu >=0
            && lstMenu[currMenu].alTopMenus != null
            && lstMenu[currMenu].alTopMenus!!.isNotEmpty()
        ) {
            var mi: MenuSelectorItem
            for (i in lstMenu[currMenu].alTopMenus!!.indices) {
                mi = lstMenu[currMenu].alTopMenus!![i]
                val spanString = SpannableString(mi.title)
                spanString.setSpan(
                    ForegroundColorSpan(Color.WHITE),
                    0,
                    spanString.length,
                    0
                )
                spanString.setSpan(
                    BackgroundColorSpan(Color.BLACK),
                    0,
                    spanString.length,
                    0
                )
                when (mi.button) {
                    ConstantsFixed.ADD_ID -> if (menu.findItem(mi.button) == null) {
                        menu.add(Menu.NONE, mi.button, mi.seqno, ViewUtils.menuIconWithText(this,mi.icon, mi.title.toString()))
                    }
                    ConstantsFixed.SAVE_ID -> if (menu.findItem(mi.button) == null) {
                        menu.add(Menu.NONE, mi.button, mi.seqno, ViewUtils.menuIconWithText(this,
                            mi.icon, mi.title.toString())).isEnabled=hasChildModificationTag()

                    }
                    ConstantsFixed.HELP_ID -> {
                        if (isHelpEnabledExt) {
                            menu.add(
                                Menu.NONE,
                                ConstantsFixed.HELP_ID,
                                mi.seqno,
                                ViewUtils.menuIconWithText(
                                    this,
                                    R.drawable.help_normal,
                                    "Help"
                                )
                            )
                        }
                    }
                    else -> {
                        menu.add(Menu.NONE, mi.button, mi.seqno, ViewUtils.menuIconWithText(this, mi.icon, mi.title.toString())).isEnabled=mi.enable
                    }
                }

            }
        } else {
            if (isHelpEnabledExt) {
                menu.add(
                    Menu.NONE,
                    ConstantsFixed.HELP_ID,
                    99,
                    ViewUtils.menuIconWithText(
                        this,
                        R.drawable.help_normal,
                        "Help"
                    )
                )
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }

    fun contextMenuAdd(button: Int, title: String?, icon: Int, type: Int = 2) {
        val mi  = MenuSelectorItem()
        val fragmentDefinition = MenuDefinition()
        mi.button = button
        mi.title = title
        mi.icon = icon
        mi.enable = true
        when (type) {
            1 -> {
                fragmentDefinition.alTopMenus!!.add(mi)
            }
            2 -> {
                fragmentDefinition.alContextMenus!!.add(mi)
            }
            else -> {
                fragmentDefinition.alContextMenus!!.add(mi)
                fragmentDefinition.alTopMenus!!.add(mi)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        onPrepareOptionsMenu(menu)
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        toolbar?.inflateMenu(R.menu.menu_main)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
       when (item.itemId) {
           ConstantsFixed.SAVE_ID -> saveScreen()
           ConstantsFixed.HELP_ID -> {
               showHelp()
           }
           ConstantsFixed.MAIL_ID -> {
               sendMail()
           }
       }
       return false
    }

   open fun initContextMenu() {
       currMenu=addMenu()
   }

 /*  fun initMenu(menuIdx:Int){
       val fragmentDefinition = MenuDefinition()
       fragmentDefinition.alContextMenus = ArrayList()
       fragmentDefinition.alTopMenus = ArrayList()

       if (lstMenu.isEmpty()) {
           lstMenu.add(fragmentDefinition)
       }
       val plus = menuIdx - lstMenu.size
       (1..plus).forEach { _ ->
           lstMenu.add(fragmentDefinition)
       }
   }*/

   fun addMenu(): Int{
       val fragmentDefinition = MenuDefinition()
       fragmentDefinition.alContextMenus = ArrayList()
       fragmentDefinition.alTopMenus = ArrayList()
       lstMenu.add(fragmentDefinition)
       currMenu = lstMenu.size-1
       return currMenu
   }

   open fun sendMail(){
       ToastExt().makeText(this, R.string.mess046_NoMailApp, Toast.LENGTH_LONG).show()
   }

   fun setContextMenu(alContextMenusParam: ArrayList<MenuSelectorItem>?, type: Int = 2, fragment: Int = 0) {
       when (type) {
           1 -> {
               lstMenu[fragment].alTopMenus = alContextMenusParam
           }
           2 -> {
               lstMenu[fragment].alContextMenus = alContextMenusParam
           }
           else -> {
               lstMenu[fragment].alTopMenus = alContextMenusParam
               lstMenu[fragment].alContextMenus = alContextMenusParam
           }
       }
   }

   fun contextMenuAdd(button: Int, title: String?, icon: Int, seqno: Int, type: Int) {

       if (lstMenu.isEmpty()) {
           val fragmentDefinition = MenuDefinition()
           fragmentDefinition.alContextMenus = ArrayList()
           fragmentDefinition.alTopMenus = ArrayList()
           lstMenu.add(fragmentDefinition)
           currMenu = lstMenu.size-1
       }

       val mi = MenuSelectorItem()
       mi.button = button
       mi.title = title
       mi.icon = icon
       mi.seqno = seqno
       mi.enable = true

       when (type) {
           1 -> {
               lstMenu[currMenu].alTopMenus!!.add(mi)
           }
           2 -> {
               lstMenu[currMenu].alContextMenus!!.add(mi)
           }
           else -> {
               lstMenu[currMenu].alContextMenus!!.add(mi)
               lstMenu[currMenu].alTopMenus!!.add(mi)
           }
       }
   }

   fun contextMenuMove(button: Int, pos: Int, type: Int = 2){
       var localMenus1: ArrayList<MenuSelectorItem> = ArrayList()
       val localMenus2: ArrayList<MenuSelectorItem> = ArrayList()
       when (type) {
           1 -> {
               localMenus1 = lstMenu[currMenu].alTopMenus!!
           }
           2 -> {
               localMenus1 = lstMenu[currMenu].alContextMenus!!
           }
       }
       var mi  = MenuSelectorItem()
       var ok = false
       for (i in localMenus1.indices){
           if (localMenus1[i].button == button){
               mi = localMenus1[i]
               localMenus1.removeAt(i)
               break
           }
       }

       for (i in localMenus1.indices){
           if (i == pos){
               ok = true
               localMenus2.add(mi)
           }
           localMenus2.add(localMenus1[i])
       }
       if (!ok){
           localMenus2.add(mi)
       }

       when (type) {
           1 -> {
               lstMenu[currMenu].alTopMenus = localMenus2
           }
           2 -> {
               lstMenu[currMenu].alContextMenus = localMenus2
           }
       }
   }

    fun contextMenuDelete(button: Int, type: Int = 2){
        when (type) {
            1 -> {
                lstMenu[currMenu].alTopMenus!!.removeIf{x -> x.button == button}
            }
            2 -> {
                lstMenu[currMenu].alContextMenus!!.removeIf{x -> x.button == button}
            }
        }
    }

    fun contextMenuEnable(button: Int, enable: Boolean, type: Int = 2){
        when (type) {
            1 -> {
                lstMenu[currMenu].alTopMenus!!.filter { x -> x.button == button}.forEach { it.enable = enable }
            }
            2 -> {
                lstMenu[currMenu].alContextMenus!!.filter { x -> x.button == button}.forEach { it.enable = enable }
            }
            3 -> {
                lstMenu[currMenu].alTopMenus!!.filter { x -> x.button == button}.forEach { it.enable = enable }
                lstMenu[currMenu].alContextMenus!!.filter { x -> x.button == button}.forEach { it.enable = enable }
            }
        }
    }

   fun contextMenuAdd(menu: ArrayList<Int>, type: Int = 2 ): ArrayList<MenuSelectorItem>? {
       if (menu.isEmpty()) {
           return when (type) {
               2 -> {
                   lstMenu[currMenu].alContextMenus
               }
               else -> {
                   lstMenu[currMenu].alTopMenus
               }
           }
       }
       for (aMenu in menu) {
           val mi = MenuSelectorItem()
           mi.button = aMenu
           mi.enable = true
           when (mi.button) {
               ConstantsFixed.ADD_ID -> {
                   if (type == 2) continue
                   mi.title = getText(R.string.add).toString()
                   mi.icon = R.drawable.add_normal
                   mi.seqno = 2
               }
               ConstantsFixed.SAVE_ID -> {
                   if (type == 2) continue
                   mi.title = getText(R.string.save).toString()
                   mi.icon = R.drawable.save_normal
                   mi.seqno = 4
               }
               ConstantsFixed.DELETE_ID -> {
                   mi.title = getText(R.string.delete).toString()
                   mi.icon = R.drawable.delete_normal
                   mi.seqno = 5
               }
               ConstantsFixed.BROWSE_ID -> {
                   mi.title = getText(R.string.browse).toString()
                   mi.icon = R.drawable.detail_normal
                   mi.seqno = 1
               }
               ConstantsFixed.EDIT_ID -> {
                   mi.title = getText(R.string.edit).toString()
                   mi.icon = R.drawable.edit_normal
                   mi.seqno = 3
               }
               ConstantsFixed.HELP_ID -> {
                   if (!isHelpEnabledExt) continue
                   mi.title = getText(R.string.help).toString()
                   mi.icon = R.drawable.help_normal
                   mi.seqno = 99
               }
               ConstantsFixed.MAIL_ID -> {
                   mi.title = getText(R.string.mail).toString()
                   mi.icon = R.drawable.mail_normal
                   mi.seqno = 6
               }
               else -> {
                   mi.title = ""
                   mi.icon = 0
               }
           }
           when (type) {
               1 -> {
                   if (!menuExists(mi.button, lstMenu[currMenu].alTopMenus)) lstMenu[currMenu].alTopMenus!!.add(mi)
               }
               2 -> {
                   if (!menuExists(mi.button, lstMenu[currMenu].alContextMenus)) lstMenu[currMenu].alContextMenus!!.add(mi)
               }
               else -> {
                   if (!menuExists(mi.button, lstMenu[currMenu].alTopMenus)) lstMenu[currMenu].alTopMenus!!.add(mi)
                   if (!menuExists(mi.button, lstMenu[currMenu].alContextMenus)) lstMenu[currMenu].alContextMenus!!.add(mi)
               }
           }
       }

       return when (type) {
           2 -> {
               lstMenu[currMenu].alContextMenus
           }
           else -> {
               lstMenu[currMenu].alTopMenus
           }
       }
   }

   fun menuExists(menuItem: Int, type: Int): Boolean{
        when (type) {
            1 -> {
                return lstMenu[currMenu].alTopMenus!!.count{x -> x.button == menuItem} > 0
            }
            2 -> {
                return lstMenu[currMenu].alContextMenus!!.count{x -> x.button == menuItem} > 0
             }
        }
        return false
    }

   private fun menuExists(menuItem: Int, menu: ArrayList<MenuSelectorItem>?): Boolean{
       if (menu != null) {
           for (aMenu in menu) {
               if (aMenu.button == menuItem) {
                   return true
               }
           }
       }
       return false
   }

   fun contextMenuRemove(vararg menu: Int, type: Int): ArrayList<MenuSelectorItem>? {
       if (menu.isNotEmpty() ) {
           for (aMenu in menu) {
               if ((type == 1 || type == 3)){
                   for (x in lstMenu[currMenu].alTopMenus!!.indices.reversed()) {
                       if (lstMenu[currMenu].alTopMenus!![x].button == aMenu) {
                           lstMenu[currMenu].alTopMenus!!.removeAt(x)
                           break
                       }
                   }
               }
               if ((type == 2 || type == 3)){
                   for (x in lstMenu[currMenu].alContextMenus!!.indices.reversed()) {
                       if (lstMenu[currMenu].alContextMenus!![x].button == aMenu) {
                           lstMenu[currMenu].alContextMenus!!.removeAt(x)
                           break
                       }
                   }
               }
           }
       }
       return when (type) {
           2 -> {
               lstMenu[currMenu].alContextMenus
           }
           else -> {
               lstMenu[currMenu].alTopMenus
           }
       }
   }

   override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
       super.onCreateContextMenu(menu, v, menuInfo)

       var mi: MenuSelectorItem
       // setContextMenu();
       if (currMenu < lstMenu.size && lstMenu[currMenu].alContextMenus != null && v != null) {
           var bCurrViewOk = false

           if (!setCurrView(v)) {
               return
           }

           // leave current view without goto parent
           if (!TagModify.hasTagValue(
                   currView,
                   ConstantsFixed.TagSection.TsUserFlag.name,
                   ConstantsFixed.TagAction.Delete.name
               )
           ) {
               bCurrViewOk = true
           }
           if (menu != null) {
               // menu popup each row

               for (i in lstMenu[currMenu].alContextMenus!!.indices) {
                   mi = lstMenu[currMenu].alContextMenus!![i]
                   when (mi.button) {
                       ConstantsFixed.BROWSE_ID,ConstantsFixed.EDIT_ID, ConstantsFixed.DELETE_ID -> if (bCurrViewOk && menu.findItem(mi.button) == null) {
                           menu.add(Menu.NONE, mi.button, Menu.NONE, ViewUtils.menuIconWithText(this,mi.icon, mi.title.toString()))
                       }
                       ConstantsFixed.SAVE_ID -> if (bCurrViewOk && menu.findItem(mi.button) == null) {
                           menu.add(Menu.NONE, mi.button, Menu.NONE, ViewUtils.menuIconWithText(this,mi.icon, mi.title.toString())).isEnabled=hasChildModificationTag()
                       }
                       ConstantsFixed.HELP_ID -> continue
                       else -> if (mi.icon > 0) {
                           menu.add(Menu.NONE, mi.button, Menu.NONE, ViewUtils.menuIconWithText(this,mi.icon, mi.title.toString()))
                       } else {
                           menu.add(Menu.NONE, mi.button, Menu.NONE,  mi.title)
                       }
                   }
               }
           }
       }
   }

   open fun createPopupMenu(v: View): PopupMenu {
       //val wrapper = ContextThemeWrapper(this, R.style.BasePopupMenu)
       val popupMenu = PopupMenu(this, v)
       //popupMenu.menu.clear()

       var mi: MenuSelectorItem
       if (currMenu >= 0 && lstMenu[currMenu].alContextMenus != null ) {
           var bCurrViewOk = false
           if (!setCurrView(v)) {
               return popupMenu
           }

           if (!TagModify.hasTagValue(
                   currView,
                   ConstantsFixed.TagSection.TsUserFlag.name,
                   ConstantsFixed.TagAction.Delete.name
               )
           ) {
               bCurrViewOk = true
           }
           if (popupMenu.menu != null) {
               // one line 1 menu popup
               for (i in lstMenu[currMenu].alContextMenus!!.indices) {
                   mi = lstMenu[currMenu].alContextMenus!![i]
                   when (mi.button) {
                       ConstantsFixed.BROWSE_ID,ConstantsFixed.EDIT_ID, ConstantsFixed.DELETE_ID -> if (bCurrViewOk && popupMenu.menu.findItem(mi.button) == null) {
                           popupMenu.menu.add(Menu.NONE, mi.button, mi.seqno, ViewUtils.menuIconWithText(this,mi.icon, mi.title.toString()))
                       }
                       ConstantsFixed.SAVE_ID -> if (bCurrViewOk && popupMenu.menu.findItem(mi.button) == null) {
                           popupMenu.menu.add(Menu.NONE, mi.button, mi.seqno, ViewUtils.menuIconWithText(this,mi.icon, mi.title.toString())).isEnabled=hasChildModificationTag()
                       }
                       ConstantsFixed.HELP_ID -> continue
                       else -> if (mi.icon > 0) {
                           popupMenu.menu.add(Menu.NONE, mi.button, mi.seqno, ViewUtils.menuIconWithText(this,mi.icon, mi.title.toString()))
                       } else {
                           popupMenu.menu.add(Menu.NONE, mi.button, mi.seqno,  mi.title)
                       }
                   }
               }
           }
       }
       return popupMenu
   }

    private fun setCurrView(v: View): Boolean {
        if (ViewUtils.isValidObject(v)) {
            currView = v
            return true
        }
        Logging.d("BaseActivity/setCurrView unknown: ${v::class.simpleName}")
        return false
    }

   inner class MenuSelectorItem {
       var title: String? = null
       var icon = 0
       var button = 0
       var seqno = 0
       var enable = true
   }

   private val isHelpEnabledExt: Boolean
   get(){
       return Constants.isHelpEnabled && helpText.isNotEmpty()
   }

   fun showHelp() {
       if (helpText.isNotEmpty()){
           val intent = Intent(this, ShowHelp::class.java)
           intent.putExtra("title", (this as Activity).title)
           val helpDescription = helpText
           if (helpDescription != ""){
               intent.putExtra("helptext", helpDescription)
           } else {
               intent.putExtra("helptext", (this as Activity).title)
           }
           resultLauncher.launch(intent)
       }
   }

   var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
           result: ActivityResult -> resultActivity(result)
   }

   open fun resultActivity(result: ActivityResult){
   }

   inner class MenuDefinition {
       var alTopMenus: java.util.ArrayList<MenuSelectorItem>? = null
       var alContextMenus: java.util.ArrayList<MenuSelectorItem>? = null
   }
}
