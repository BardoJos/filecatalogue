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
package com.farrusco.projectclasses.filepicker.controller.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.farrusco.projectclasses.R
import com.farrusco.projectclasses.filepicker.controller.NotifyItemChecked
import com.farrusco.projectclasses.filepicker.model.DialogConfigs
import com.farrusco.projectclasses.filepicker.model.DialogProperties
import com.farrusco.projectclasses.filepicker.model.FileListItem
import com.farrusco.projectclasses.filepicker.model.MarkedItemList
import com.farrusco.projectclasses.filepicker.widget.MaterialCheckbox
import com.farrusco.projectclasses.filepicker.widget.OnCheckedChangeListener
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.view.isVisible

class FileListAdapter(
    private val listItem: ArrayList<FileListItem>,
    private val context: Context,
    private val properties: DialogProperties
) : BaseAdapter() {
    private var notifyItemChecked: NotifyItemChecked? = null

    override fun getCount(): Int {
        return listItem.size
    }

    override fun getItem(i: Int): FileListItem {
        return listItem[i]
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    @SuppressLint("SetTextI18n")
    override fun getView(i: Int, viewp: View?, viewGroup: ViewGroup): View {
        var view: View? = viewp
        val holder: ViewHolder
        if (view == null) {
            view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_file_list_item, viewGroup, false)
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            holder = view.tag as ViewHolder
        }
        val item: FileListItem = listItem[i]
        if (MarkedItemList.hasItem(item.location)) {
            val animation: Animation =
                AnimationUtils.loadAnimation(context, R.anim.marked_item_animation)
            view!!.animation = animation
        } else {
            val animation: Animation =
                AnimationUtils.loadAnimation(context, R.anim.unmarked_item_animation)
            view!!.animation = animation
        }
        if (item.isDirectory ) {
            holder.typeIcon.setImageResource(R.mipmap.ic_type_folder)
            holder.typeIcon.setColorFilter(
                context.resources.getColor(
                    R.color.colorPrimary,
                    context.theme
                )
            )
            if (properties.selectionType == DialogConfigs.FILE_SELECT) {
                holder.fMark.visibility = View.INVISIBLE
            } else {
                holder.fMark.visibility = View.VISIBLE
            }
        } else {
            holder.typeIcon.setImageResource(R.mipmap.ic_type_file)
            holder.typeIcon.setColorFilter(
                context.resources.getColor(
                    R.color.colorAccent,
                    context.theme
                )
            )
            if (properties.selectionType == DialogConfigs.DIR_SELECT) {
                holder.fMark.visibility = View.INVISIBLE
            } else {
                holder.fMark.visibility = View.VISIBLE
            }
        }
        holder.typeIcon.contentDescription = item.filename
        holder.name.text = item.filename
        val sDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val sTime = SimpleDateFormat("hh:mm aa", Locale.getDefault())
        val date = Date(item.time)
        if (i == 0 && item.filename!!.startsWith(context.getString(R.string.label_parent_dir))) {
            holder.type.setText(R.string.label_parent_directory)
        } else {
            holder.type.text = context.getString(R.string.last_edit) +
                    sDate.format(date) + ", " +
                    sTime.format(date)
        }
        if (holder.fMark.isVisible) {
            if (i == 0 && item.filename!!
                    .startsWith(context.getString(R.string.label_parent_dir))
            ) {
                holder.fMark.visibility = View.INVISIBLE
            }
            if (MarkedItemList.hasItem(item.location)) {
                holder.fMark.setChecked(true)
            } else {
                holder.fMark.setChecked(false)
            }
        }
        holder.fMark.setOnCheckedChangedListener(object :
            OnCheckedChangeListener {
            override fun onCheckedChanged(checkbox: MaterialCheckbox?, isChecked: Boolean) {
                item.isMarked=isChecked
                if (item.isMarked) {
                    if (properties.selectionMode == DialogConfigs.MULTI_MODE) {
                        MarkedItemList.addSelectedItem(item)
                    } else {
                        MarkedItemList.addSingleFile(item)
                    }
                } else {
                    MarkedItemList.removeSelectedItem(item.location)
                }
                notifyItemChecked!!.notifyCheckBoxIsClicked()
            }
        })
        return view
    }

    private inner class ViewHolder(itemView: View) {
        var typeIcon: ImageView = itemView.findViewById(R.id.image_type)
        var name: TextView = itemView.findViewById(R.id.fname)
        var type: TextView = itemView.findViewById(R.id.ftype)
        var fMark: MaterialCheckbox = itemView.findViewById(R.id.file_mark)
    }

    fun setNotifyItemCheckedListener(notifyItemChecked: NotifyItemChecked?) {
        this.notifyItemChecked = notifyItemChecked
    }
}