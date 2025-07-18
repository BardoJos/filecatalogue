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
package com.farrusco.projectclasses.filepicker.view

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.*
import com.farrusco.projectclasses.R
import com.farrusco.projectclasses.filepicker.controller.DialogSelectionListener
import com.farrusco.projectclasses.filepicker.controller.NotifyItemChecked
import com.farrusco.projectclasses.filepicker.controller.adapters.FileListAdapter
import com.farrusco.projectclasses.filepicker.model.DialogConfigs
import com.farrusco.projectclasses.filepicker.model.DialogConfigs.STORAGE_SDCARD
import com.farrusco.projectclasses.filepicker.model.DialogConfigs.STORAGE_STORAGE
import com.farrusco.projectclasses.filepicker.model.DialogProperties
import com.farrusco.projectclasses.filepicker.model.FileListItem
import com.farrusco.projectclasses.filepicker.model.MarkedItemList
import com.farrusco.projectclasses.filepicker.utils.ExtensionFilter
import com.farrusco.projectclasses.filepicker.utils.Utility
import com.farrusco.projectclasses.filepicker.widget.MaterialCheckbox
import com.farrusco.projectclasses.messages.ToastExt
import com.farrusco.projectclasses.widget.ButtonExt
import java.io.File
import androidx.core.view.isVisible

open class FilePickerDialog

    (context: Context, private var properties: DialogProperties) : Dialog(context), AdapterView.OnItemClickListener {
    private var listViewx: ListView? = null

    private var dname: TextView? = null
    private var dirPath: TextView? = null
    private var title: TextView? = null
    private var callbacks: DialogSelectionListener? = null
    private var internalList: ArrayList<FileListItem>
    private var filter = ExtensionFilter(properties)
    private var mFileListAdapter: FileListAdapter? = null
    private var select: ButtonExt? = null
    private var titleStr: String? = null
    private var positiveBtnNameStr: String? = null
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var swsdcard: Switch
    init {
        internalList = ArrayList()
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_main)
        listViewx = findViewById(R.id.fileList)
        select = findViewById(R.id.btnSelect)
        swsdcard = findViewById(R.id.swsdcard)
        if (properties.offset.toString().startsWith(STORAGE_SDCARD)){
            swsdcard.visibility = View.VISIBLE
            swsdcard.isChecked = true
        } else if (File(STORAGE_SDCARD).isDirectory) {
            swsdcard.visibility = View.VISIBLE
        } else {
            swsdcard.visibility = View.GONE
        }
        if (swsdcard.isVisible){
            swsdcard.setOnCheckedChangeListener { _, _ ->
                fillFileList()
            }
        } else {
            swsdcard.visibility = View.GONE
        }

        val size: Int = MarkedItemList.fileCount
        if (size == 0) {
            select!!.isEnabled = false
            val color =
                context.resources.getColor(R.color.colorAccent, context.theme)
            select!!.setTextColor(
                Color.argb(
                    128,
                    Color.red(color),
                    Color.green(color),
                    Color.blue(color)
                )
            )
        }
        dname = findViewById<View>(R.id.dname) as TextView
        title = findViewById<View>(R.id.title) as TextView
        dirPath = findViewById<View>(R.id.dir_path) as TextView
        select!!.setOnClickListener {

            var lastPath:String?=""
            if (dirPath != null){
                lastPath = dirPath!!.text.toString()
            }
            val paths: Array<String?> = MarkedItemList.selectedPaths
            if (callbacks != null) {
                callbacks!!.onSelectedFilePaths(lastPath,paths)
            }
            dismiss()
        }
        mFileListAdapter = FileListAdapter(internalList, context, properties)
        mFileListAdapter!!.setNotifyItemCheckedListener(object : NotifyItemChecked {
            override fun notifyCheckBoxIsClicked() {
                /*  Handler function, called when a checkbox is checked ie. a file is
                 *  selected.
                 */
                positiveBtnNameStr =
                    if (positiveBtnNameStr == null) context.resources.getString(R.string.choose_button_label) else positiveBtnNameStr

                if (MarkedItemList.fileCount == 0) {
                    select!!.isEnabled = false
                    val color =
                        context.resources.getColor(R.color.colorAccent, context.theme)
                    select!!.setTextColor(
                        Color.argb(
                            128,
                            Color.red(color),
                            Color.green(color),
                            Color.blue(color)
                        )
                    )
                    select!!.text = positiveBtnNameStr
                } else {
                    select!!.isEnabled = true
                    val color =
                        context.resources.getColor(R.color.colorAccent, context.theme)
                    select!!.setTextColor(color)
                    val buttonlabel = "$positiveBtnNameStr (" + MarkedItemList.fileCount + ") "
                    select!!.text = buttonlabel
                }
                if (properties.selectionMode == DialogConfigs.SINGLE_MODE) {
                    /*  If a single file has to be selected, clear the previously checked
                     *  checkbox from the list.
                     */
                    mFileListAdapter!!.notifyDataSetChanged()
                }
            }
        })
        listViewx!!.adapter = mFileListAdapter

        markFiles(properties.markfiles)
        setTitle(properties.title)

        fillFileList()
    }

    private fun setTitle() {
        if (title == null || dname == null) {
            return
        }
        if (titleStr != null) {
            if (title!!.visibility != View.VISIBLE) {
                title!!.visibility = View.VISIBLE
            }
            title!!.text = titleStr
            if (dname!!.isVisible) {
                dname!!.visibility = View.GONE
            }
        } else {
            if (this.title!!.isVisible) {
                title!!.visibility = View.GONE
            }
            if (dname!!.visibility != View.VISIBLE) {
                dname!!.visibility = View.VISIBLE
            }
        }
    }
    private fun fillFileList(){
        val dir = if (swsdcard.isChecked) {
            STORAGE_SDCARD
        } else {
            STORAGE_STORAGE
        }
        val currLoc = File(dir)
        properties.root = File(dir)
        // java.lang.NullPointerException
        dname!!.text = currLoc.name
        setTitle()
        dirPath!!.text = currLoc.absolutePath
        internalList.clear()

        internalList =
            Utility.prepareFileListEntries(
                internalList,
                currLoc,
                filter
            )
        mFileListAdapter!!.notifyDataSetChanged()
    }
    override fun onStart() {
        super.onStart()
        positiveBtnNameStr =
            if (positiveBtnNameStr == null) context.resources.getString(R.string.choose_button_label) else positiveBtnNameStr
        select!!.text = positiveBtnNameStr
/*        if (Utility.checkStorageAccessPermissions(
                context
            )
        ) {*/
            val currLoc: File
            internalList.clear()
            if (properties.offset.isDirectory && validateOffsetPath()) {
                currLoc = File(properties.offset.absolutePath)
                val parent = FileListItem()
                parent.filename=context.getString(R.string.label_parent_dir)
                parent.isDirectory=true
                parent.location= currLoc.parentFile?.absolutePath
                parent.time=currLoc.lastModified()
                internalList.add(parent)
            } else if (properties.root.exists() && properties.root.isDirectory) {
                currLoc = File(properties.root.absolutePath)
            } else {
                currLoc = File(properties.errorDir.absolutePath)
            }
            dname!!.text = currLoc.name
            dirPath!!.text = currLoc.absolutePath
            setTitle()
            internalList =
                Utility.prepareFileListEntries(
                    internalList,
                    currLoc,
                    filter
                )
            mFileListAdapter!!.notifyDataSetChanged()
            listViewx!!.onItemClickListener = this
        //}
    }

    private fun validateOffsetPath(): Boolean {
        val offsetpath: String = properties.offset.absolutePath
        val rootpath: String = properties.root.absolutePath
        return offsetpath != rootpath && offsetpath.contains(rootpath)
    }

    override fun onItemClick(adapterView: AdapterView<*>?, view: View, i: Int, l: Long) {
        if (internalList.size > i) {
            val fitem: FileListItem = internalList[i]
            if (fitem.isDirectory) {
                if (File(fitem.location!!).canRead()) {
                    val currLoc = File(fitem.location!!)
                    dname!!.text = currLoc.name
                    setTitle()
                    dirPath!!.text = currLoc.absolutePath
                    internalList.clear()
                    if (currLoc.name != properties.root.name) {
                        val parent = FileListItem()
                        parent.filename=context.getString(R.string.label_parent_dir)
                        parent.isDirectory=true
                        parent.location = currLoc.parentFile?.absolutePath
                        parent.time=currLoc.lastModified()
                        internalList.add(parent)
                    }
                    internalList =
                        Utility.prepareFileListEntries(
                            internalList,
                            currLoc,
                            filter
                        )
                    mFileListAdapter!!.notifyDataSetChanged()
                } else {
                    ToastExt().makeText(context, R.string.error_dir_access, Toast.LENGTH_SHORT).show()
                }
            } else {
                val fmark: MaterialCheckbox =
                    view.findViewById<View>(R.id.file_mark) as MaterialCheckbox
                fmark.performClick()
            }
        }
    }

    fun setDialogSelectionListener(callbacks: DialogSelectionListener?) {
        this.callbacks = callbacks
    }

    override fun setTitle(titleStr: CharSequence?) {
        if (titleStr != null) {
            this.titleStr = titleStr.toString()
        } else {
            this.titleStr = null
        }
        setTitle()
    }

    private fun markFiles(paths: List<String?>?) {
        if (!paths.isNullOrEmpty()) {
            if (properties.selectionMode == DialogConfigs.SINGLE_MODE) {
                val temp = File(paths[0]!!)
                when (properties.selectionType) {
                    DialogConfigs.DIR_SELECT -> if (temp.exists() && temp.isDirectory) {
                        val item = FileListItem()
                        item.filename=temp.name
                        item.isDirectory=temp.isDirectory
                        item.isMarked=true
                        item.time=temp.lastModified()
                        item.location=temp.absolutePath
                        MarkedItemList.addSelectedItem(item)
                    }
                    DialogConfigs.FILE_SELECT -> if (temp.exists() && temp.isFile) {
                        val item = FileListItem()
                        item.filename=temp.name
                        item.isDirectory=temp.isDirectory
                        item.isMarked=true
                        item.time=temp.lastModified()
                        item.location=temp.absolutePath
                        MarkedItemList.addSelectedItem(item)
                    }
                    DialogConfigs.FILE_AND_DIR_SELECT -> if (temp.exists()) {
                        val item = FileListItem()
                        item.filename=temp.name
                        item.isDirectory=temp.isDirectory
                        item.isMarked=true
                        item.time=temp.lastModified()
                        item.location=temp.absolutePath
                        MarkedItemList.addSelectedItem(item)
                    }
                }
            } else {
                for (path in paths) {
                    when (properties.selectionType) {
                        DialogConfigs.DIR_SELECT -> {
                            val temp = File(path!!)
                            if (temp.exists() && temp.isDirectory) {
                                val item = FileListItem()
                                item.filename=temp.name
                                item.isDirectory=temp.isDirectory
                                item.isMarked=true
                                item.time=temp.lastModified()
                                item.location=temp.absolutePath
                                MarkedItemList.addSelectedItem(item)
                            }
                        }
                        DialogConfigs.FILE_SELECT -> {
                            val temp = File(path!!)
                            if (temp.exists() && temp.isFile) {
                                val item = FileListItem()
                                item.filename=temp.name
                                item.isDirectory=temp.isDirectory
                                item.isMarked=true
                                item.time=temp.lastModified()
                                item.location=temp.absolutePath
                                MarkedItemList.addSelectedItem(item)
                            }
                        }
                        DialogConfigs.FILE_AND_DIR_SELECT -> {
                            val temp = File(path!!)
                            if (temp.exists() && (temp.isFile || temp.isDirectory)) {
                                val item = FileListItem()
                                item.filename=temp.name
                                item.isDirectory=temp.isDirectory
                                item.isMarked=true
                                item.time=temp.lastModified()
                                item.location=temp.absolutePath
                                MarkedItemList.addSelectedItem(item)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun show() {
/*        if (!Utility.checkStorageAccessPermissions(
                context
            )
        ) {
            ToastExt().makeText(context, R.string.error_dir_access, Toast.LENGTH_SHORT).show()
        } else {*/
            super.show()
            positiveBtnNameStr =
                if (positiveBtnNameStr == null) context.resources.getString(R.string.choose_button_label) else positiveBtnNameStr
            select!!.text = positiveBtnNameStr
            val size: Int = MarkedItemList.fileCount
            if (size == 0) {
                select!!.text = positiveBtnNameStr
            } else {
                val buttonlabel = "$positiveBtnNameStr ($size) "
                select!!.text = buttonlabel
            }
        //}
    }

    override fun dismiss() {
        MarkedItemList.clearSelectionList()
        internalList.clear()
        super.dismiss()
    }

}