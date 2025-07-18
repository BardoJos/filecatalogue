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
package com.farrusco.projectclasses.filepicker.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LabeledIntent
import android.graphics.Color
import android.net.Uri
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import com.farrusco.projectclasses.R
import com.farrusco.projectclasses.common.extensionApplication
import com.farrusco.projectclasses.filepicker.model.FileListItem
import java.io.File

class Utility private constructor() {
    init {
        throw RuntimeException("Not allowed instances")
    }

    companion object {

        /*        fun checkStorageAccessPermissions(): Boolean {   //Only for Android M and above.
          *//*          val permission = "android.permission.READ_EXTERNAL_STORAGE"
            val res = context.checkCallingOrSelfPermission(permission)
            return res == PackageManager.PERMISSION_GRANTED*//*
            return true
        }*/
        fun customChooser(context: Context, file: File){

            val uri = Uri.fromFile(file)!!
            val type = extensionApplication(context,file.path).application

            val pm = context.packageManager
            val viewIntent = Intent(Intent.ACTION_VIEW )
            val editIntent = Intent(Intent.ACTION_EDIT)
            // viewIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            // editIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            viewIntent.setDataAndType(uri, type.split("/")[0] + "/*" )
            editIntent.setDataAndType(uri, type.split("/")[0] + "/*" )
            val openInChooser = Intent.createChooser(viewIntent, context.getText(R.string.openappwith)) // "Open in..."

            val forEditing: Spannable = SpannableString(" (" + context.getText(R.string.edit) + ")")
            forEditing.setSpan(
                ForegroundColorSpan(Color.BLUE),
                0,
                forEditing.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            val resInfo = pm.queryIntentActivities(editIntent, 0)
            val extraIntents = arrayOfNulls<Intent>(resInfo.size)
            for (i in resInfo.indices) {
                // Extract the label, append it, and repackage it in a LabeledIntent
                val ri = resInfo[i]
                val packageName = ri.activityInfo.packageName
                val intent = Intent()
                intent.setComponent(ComponentName(packageName, ri.activityInfo.name))
                intent.setAction(Intent.ACTION_EDIT)
                intent.setDataAndType(uri, type)
                val label = TextUtils.concat(ri.loadLabel(pm), forEditing)
                extraIntents[i] = LabeledIntent(intent, packageName, label, ri.icon)
            }

            openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents)
            context.startActivity(openInChooser)
        }

        fun prepareFileListEntries(
            internalList: ArrayList<FileListItem>,
            inter: File,
            filter: ExtensionFilter?
        ): ArrayList<FileListItem> {
            var internalListX: ArrayList<FileListItem> = internalList
            try {
                //Check for each and every directory/file in 'inter' directory.
                //Filter by extension using 'filter' reference.
                if (inter.listFiles(filter) != null) {
                    for (name in inter.listFiles(filter)!!) {
                        //If file/directory can be read by the Application
                        if (name.canRead()) {
                            //Create a row item for the directory list and define properties.
                            val item = FileListItem()
                            item.filename = name.name
                            item.isDirectory = name.isDirectory
                            item.location = name.absolutePath
                            item.time = name.lastModified()
                            //Add row to the List of directories/files
                            internalListX.add(item)
                        }
                    }
                }
                //Sort the files and directories in alphabetical order.
                //See compareTo method in FileListItem class.
                internalListX.sort()
            } catch (e: NullPointerException) {
                e.printStackTrace()
                internalListX = ArrayList()
            }
            return internalListX
        }
    }
}