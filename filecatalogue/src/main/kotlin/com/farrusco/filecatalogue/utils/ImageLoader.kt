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

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.farrusco.filecatalogue.R
import com.farrusco.filecatalogue.business.Product
import com.farrusco.filecatalogue.common.ObjectFile
import com.farrusco.projectclasses.common.ConstantsFixed
import com.farrusco.projectclasses.graphics.BitmapManager
import com.farrusco.projectclasses.utils.TagModify
import com.farrusco.projectclasses.widget.TextViewExt

open class ImageLoaderExt(
    activity: Activity,
    context: Context,
    noCols: Int,
    isSingleListLabelEnabled: Boolean
) : BaseAdapter() {
    private val mActivity: Activity = activity
    private val mContext: Context = context
    private var imageMaxSize = 50
    private var numColumns = 50
    private var isSingleListLabelEnabled = true

    // private int NUM_ROWS = 50;
    private val product: Product
    private val objectFile = ObjectFile()

    override fun getCount(): Int {
        return objectFile.getSize()
    }

    override fun getItem(position: Int): Any {
        return objectFile.getFilename(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    /**
     * show 1 file with title
     *
     */
    @SuppressLint("InflateParams")
    private fun getViewOne(position: Int, convertView: View?): View {
        val vi: View = if (convertView == null) {
            val li: LayoutInflater = mActivity.layoutInflater
            li.inflate(R.layout.imagetext, null)
        } else {
            convertView
        }

        val tv = vi.findViewById<TextViewExt>(R.id.icon_text)
        tv.textExt = objectFile.getFilename(position)

        val imageView = vi.findViewById<View>(R.id.icon_image) as ImageView
        if (imageView.tag != null && !objectFile.getFilename(position).equals(
                TagModify.getViewTagValue(imageView, ConstantsFixed.TagSection.TsDBValue),
                true
            )
        ) {
            imageView.tag = ""
        }
        TagModify.setViewTagValue(imageView, ConstantsFixed.TagSection.TsGroupno, position)
        TagModify.setViewTagValue(
            imageView,
            ConstantsFixed.TagSection.TsDBValue,
            objectFile.getFilename(position)
        )
        TagModify.setViewTagValue(
            imageView,
            ConstantsFixed.TagSection.TsDBColumnBack,
            objectFile.getId(position)
        )
        TagModify.setViewTagValue(imageView, "rotation", objectFile.getRotation(position))
        displayImage(
            position,
            objectFile.getId(position),
            objectFile.getFilename(position),
            objectFile.getRotation(position),
            imageView
        )

        return vi
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        if (isSingleListLabelEnabled && numColumns == 1) {
            return getViewOne(position, convertView)
        }
        lateinit var imageView: ImageView
        if (convertView == null) {
            imageView = ImageView(mActivity)
            TagModify.setViewTagValue(imageView, ConstantsFixed.TagSection.TsGroupno, position)
            TagModify.setViewTagValue(
                imageView,
                ConstantsFixed.TagSection.TsDBValue,
                objectFile.getFilename(position)
            )
            TagModify.setViewTagValue(
                imageView,
                ConstantsFixed.TagSection.TsDBColumnBack,
                objectFile.getId(position)
            )
            TagModify.setViewTagValue(imageView, "rotation", objectFile.getRotation(position))
        } else {
            imageView = convertView as ImageView
        }
        displayImage(
            position,
            objectFile.getId(position),
            objectFile.getFilename(position),
            objectFile.getRotation(position),
            imageView
        )
        return imageView
    }

    private fun displayImage(
        position: Int, id: Int, url: String?, rotation: Int,
        imageView: ImageView
    ): Boolean {
        if (url != null &&
            url == TagModify.getViewTagValue(imageView, ConstantsFixed.TagSection.TsDBValue.name) &&
            ConstantsFixed.TagAction.Edit.name == TagModify.getViewTagValue(
                imageView,
                ConstantsFixed.TagSection.TsModFlag.name
            )
        ) {
            // error showing file, while file already loaded
            return false
        }
        TagModify.setViewTagValue(
            imageView,
            ConstantsFixed.TagSection.TsModFlag,
            ConstantsFixed.TagAction.Edit
        )
        TagModify.setViewTagValue(imageView, ConstantsFixed.TagSection.TsGroupno, position)
        TagModify.setViewTagValue(imageView, ConstantsFixed.TagSection.TsDBValue, url)
        TagModify.setViewTagValue(imageView, ConstantsFixed.TagSection.TsDBColumnBack, id)

        if (url != null) {
            BitmapManager.loadFileToImageView(
                mActivity, id,
                url, imageMaxSize, rotation.toFloat(), imageView
            )
        }
        return true
    }

    companion object {
        private var inflater: LayoutInflater? = null
    }

    init {
        numColumns = noCols
        this.isSingleListLabelEnabled = isSingleListLabelEnabled
        product = Product(mContext)
        val width = mContext.resources.displayMetrics.widthPixels
        imageMaxSize = (width / numColumns - 10
                - numColumns * 2)
        inflater = activity
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }
}