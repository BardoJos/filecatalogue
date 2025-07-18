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
package com.farrusco.projectclasses.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.farrusco.projectclasses.R
import com.farrusco.projectclasses.common.ConstantsFixed
import com.farrusco.projectclasses.graphics.BitmapManager
import com.farrusco.projectclasses.utils.CalcObjects
import com.farrusco.projectclasses.utils.TagModify
import com.farrusco.projectclasses.widget.validators.Validator
import java.io.File


class ImageViewExt : AppCompatImageView {
    private var skipEditTagOnce = false
    private var errorString: String? = null
    private var emptyErrorString: String? = null
    val bitmap: Bitmap?
        get() {
            return bitmapTmp
        }
    private var bitmapTmp: Bitmap? = null
    private var bitmapLogo: Bitmap? = null
    private lateinit var bitmapManager: BitmapManager
    var validators = ArrayList<Validator>()

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        TagModify.setViewTagValue(this, ConstantsFixed.TagSection.TsForeBack.name, "f")
        initControl(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        TagModify.setViewTagValue(this, ConstantsFixed.TagSection.TsForeBack.name, "f")
        initControl(context, attrs)
    }

    constructor(context: Context) : super(context) {
        TagModify.setViewTagValue(this, ConstantsFixed.TagSection.TsForeBack.name, "f")
        initControl(context, null)
    }

    @SuppressLint("CustomViewStyleable")
    private fun initControl(context: Context, attrs: AttributeSet?) {

        val typedArray: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.EditTextExt)
        errorString = typedArray.getString(R.styleable.EditTextExt_errorString)
        emptyErrorString = typedArray.getString(R.styleable.EditTextExt_emptyErrorString)
        bitmapLogo = BitmapFactory.decodeResource(context.resources, R.drawable.logo)
        bitmapManager = BitmapManager(context, 1)
        typedArray.recycle()
    }

    fun setText(text: String?, init: Boolean) {
        if (init) skipEditTagOnce = true
        //super.setText(formatString(text))
        if (!skipEditTagOnce) {
            tag = TagModify.setTagValue(
                tag,
                ConstantsFixed.TagSection.TsModFlag.name,
                ConstantsFixed.TagAction.Edit.name
            )
        }
        TagModify.setViewTagValue(this, ConstantsFixed.TagSection.TsDBValue, text)
        if (text != null) {
            val file = File(text)
            if (file.exists()) {
                val bm = CalcObjects.decodeFile(file, this.height)!!
                this.setImageBitmap(bm)
                bitmapTmp = bm
            }
        }
    }

    fun getText(): String {
        return TagModify.getViewTagValue(this, ConstantsFixed.TagSection.TsDBValue)
    }

    fun setDBColumn(dBColumn: String?, dBtable: String?): ImageViewExt {
        TagModify.setViewTagValue(this, ConstantsFixed.TagSection.TsDBColumn, dBColumn)
        if (dBtable != null) TagModify.setViewTagValue(
            this,
            ConstantsFixed.TagSection.TsDBTable,
            dBtable
        )
        return this
    }

    fun setDBColumn(dBColumn: String?, dBtable: String?, groupno: Int): ImageViewExt {
        TagModify.setViewTagValue(this, ConstantsFixed.TagSection.TsDBColumn, dBColumn)
        if (dBtable != null) TagModify.setViewTagValue(
            this,
            ConstantsFixed.TagSection.TsDBTable,
            dBtable
        )
        TagModify.setViewTagValue(this, ConstantsFixed.TagSection.TsGroupno, groupno.toString())
        return this
    }

    fun fillGraph(id: Int, filename: String, maxSize: Int, rotation: Float) {
        BitmapManager.loadFileToImageView(context, id, filename, maxSize, rotation, this)
    }


/*
    fun fillGraph(filename: String, maxSize: Int):Boolean {
        var max = min(this.maxHeight,this.maxWidth)
        if (maxSize in (1 until max)){
            max = maxSize
        }

        if (filename.isEmpty()) {
            this.setImageBitmap(bitmapLogo)
            bitmapTmp=bitmapLogo
        } else{
            try {
                val file = File(filename)
                if (file.exists()) {
                    val bitmap = bitmapManager.loadBitmap(filename, max)

                    this.setImageResource(R.drawable.screen_background_black)
                    if (bitmap != null) {
                        this.setImageBitmap(bitmap)
                        bitmapTmp=bitmap
                    }
                }
            } catch (e: Exception) {
                Logging.w("fillGraph",e.toString() )
                //this.imageAlpha=R.drawable.logo
                this.setImageBitmap(bitmapLogo)
                bitmapTmp=bitmapLogo
                return false
            }
        }

        return true
    }

    fun createQRCode(input: String, dimension: Int = 200) {
        val qrCodeWriter = QRCodeWriter()
        try {
            val bitMatrix =
                qrCodeWriter.encode(input, BarcodeFormat.QR_CODE, dimension, dimension)
            val bitmap = Bitmap.createBitmap(dimension, dimension, Bitmap.Config.RGB_565)
            for (x in 0 until (dimension-1)) {
                for (y in 0 until (dimension-1)) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            this.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    */
}