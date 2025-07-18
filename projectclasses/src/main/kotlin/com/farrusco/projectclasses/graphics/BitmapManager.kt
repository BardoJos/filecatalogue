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
@file:Suppress("unused")

package com.farrusco.projectclasses.graphics

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.farrusco.projectclasses.R
import com.farrusco.projectclasses.common.Constants
import com.farrusco.projectclasses.common.extensionApplication
import com.farrusco.projectclasses.utils.Logging
import com.farrusco.projectclasses.messages.ToastExt
import com.farrusco.projectclasses.utils.ViewUtils
import com.farrusco.projectclasses.widget.ImageViewExt
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import androidx.core.graphics.get

class BitmapManager(private val context: Context, noOfCols: Int)  {

    private var imageMaxSize = 50
    private var noCols = 50

    companion object {
        lateinit var bitmapSquare: Bitmap
        fun screenShot(view: View): Bitmap {
            val bitmap = createBitmap(view.width, view.height)
            val canvas = Canvas(bitmap)
            view.draw(canvas)
            return bitmap
        }

        fun loadFileToImageView(context: Context, filename: String, rotation: Float): ImageViewExt {
            val imv = ImageViewExt(context)
            Glide.with(context)
                .load(filename)
                .centerCrop()
                .error(R.drawable.logo)
                .into(imv)
            imv.rotation = rotation
            return imv
        }

        fun loadFileToBitmap(context: Context, filename: String, rotation: Float): Bitmap {
            val imv = ImageViewExt(context)
            Glide.with(context)
                .load(filename)
                .centerCrop()
                .error(R.drawable.logo)
                .into(imv)
            imv.rotation = rotation
            return imv.bitmap!!
        }

        fun setDeleteImage(context: Context, imageMaxSize: Int, imageView: ImageView){
            Glide.with(context)
                .load(android.R.drawable.ic_delete)
                .centerCrop()
                .override(imageMaxSize)
                .error(R.drawable.logo)
                .into(imageView)
        }

        fun loadFileToImageView(context: Context, id: Int, filename: String, imageMaxSize: Int, rotation: Float, imageView: ImageView) {
            if (!File(filename).exists()){
                setDeleteImage(context, imageMaxSize, imageView)
                return
            }
            val extApp = extensionApplication(context, filename)
            if (extApp.resourceIconEmpty != 0) {
                val fileTmp = File(filename).nameWithoutExtension + "_$id." +
                        if (VERSION.SDK_INT >= VERSION_CODES.R) {
                            "webp"
                        } else {
                            "jpg"
                        }

                val dirMk = File(Constants.bitmapPath)
                val file = File("${Constants.bitmapPath}/$fileTmp")
                if (!dirMk.exists()) {
                    dirMk.mkdirs()
                    if (!dirMk.exists()) return
                } else if (file.exists()){
                    //file.delete()
                    Glide.with(context)
                        .load(file.absolutePath)
                        .centerCrop()
                        .override(imageMaxSize)
                        .error(R.drawable.logo)
                        .into(imageView)
                    return
                }

                // failsafe in case bitmap was not created
                if (!createThumbnailsCommon(context, File(filename), file, imageMaxSize)){
                    return
                }

                Glide.with(context)
                    .load(file.absolutePath)
                    //.centerCrop() // fault
                    .override(imageMaxSize)
                    .error(R.drawable.logo)
                    .into(imageView)
                imageView.rotation = 0f
                file.deleteOnExit()
                return
            }
            if (imageMaxSize>0){
                Glide.with(context)
                    .load(filename)
                    .centerCrop()
                    .override(imageMaxSize)
                    .error(R.drawable.logo)
                    .into(imageView)
            } else {
                Glide.with(context)
                    .load(filename)
                    .centerCrop()
                    .error(R.drawable.logo)
                    .into(imageView)
            }
            imageView.rotation = rotation
        }

        fun createThumbnails(context: Context, id: Int, fileMain: File, imageMaxSize: Int) {
            if (!fileMain.exists()) {
                // android.R.drawable.ic_delete
                // setDeleteImage(context, imageMaxSize, imageView)
                return
            }
            val extApp = extensionApplication(context,fileMain.path)
            if (extApp.resourceIconEmpty != 0) {
                val fileTmp = fileMain.nameWithoutExtension + "_$id." +
                    if (VERSION.SDK_INT >= VERSION_CODES.R) {
                        "webp"
                    } else {
                        "jpg"
                    }
                val dirMk = File(Constants.bitmapPath)
                val file = File("${Constants.bitmapPath}/$fileTmp")
                if (!dirMk.exists()) {
                    dirMk.mkdirs()
                    if (!dirMk.exists()) return
                } else if (file.exists()){
                    //file.delete()
                    return
                }
                //  textSize, maxWidth, maxHeight, left, top
                createThumbnailsCommon(context, fileMain, file, imageMaxSize)
            }
        }

        private fun createThumbnailsCommon(context: Context, fileMain: File, fileCreate: File, imageMaxSize: Int): Boolean {

            val extApp = extensionApplication(context,fileMain.path)
            val res: Int = extApp.resourceIconEmpty
            var bm = BitmapFactory.decodeResource(context.resources, res).copy(Bitmap.Config.ARGB_8888, true)
            bm = bm.scale(imageMaxSize, imageMaxSize)
            bm[bm.height / 5, bm.width / 5] // color white

            val paint         = Paint()
            paint.color       = Color.BLACK
            paint.isFakeBoldText = true
            paint.strokeWidth = ViewUtils.getDip(R.dimen.normal_2sp, context)
            paint.style       = Paint.Style.STROKE
            paint.isAntiAlias = true
            paint.textSize    = ViewUtils.getDip(R.dimen.normal_30sp, context)
            paint.style       = Paint.Style.FILL
            // no effect : paint.textAlign = Paint.Align.CENTER
            val textRect      = TextRect(paint)
            val canvas        = Canvas(bm)
            textRect.prepare(fileMain.nameWithoutExtension.replace("-","_"), bm.width - 20 - (extApp.resourceIconEmptyEdge * 2), bm.height/2)
            textRect.draw(canvas, 5 + extApp.resourceIconEmptyEdge, bm.height/2)

            try {
                if (!fileCreate.createNewFile()) {
                    return false
                }
            } catch (e: Exception){
                return false
            }

            try {
                val fos = FileOutputStream(fileCreate)
                if (bm.height > 5312 && bm.width > 2988 || bm.height > 2988 && bm.width > 5312){
                    if (VERSION.SDK_INT >= VERSION_CODES.R) {
                        bm.compress(Bitmap.CompressFormat.WEBP_LOSSY, 100, fos)
                    } else {
                        bm.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                    }
                } else {
                    try{
                        // faster save image to file but single row is ---> NOT DONE
/*                        val buffer = ByteBuffer.allocate(bm.getByteCount())
                        bm.copyPixelsToBuffer(buffer)
                        fos.write(buffer.array())*/
                        bm.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                    } catch(e: Exception){ // outOf memory exception
                        if (VERSION.SDK_INT >= VERSION_CODES.R) {
                            bm.compress(Bitmap.CompressFormat.WEBP_LOSSY, 80, fos)
                        } else {
                            bm.compress(Bitmap.CompressFormat.JPEG, 80, fos)
                        }
                    }
                }
                fos.close()
                //MediaScannerConnection.scanFile(context,  {fileCreate}, null, null)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }

            bm.recycle()
            fileCreate.deleteOnExit()
            return true
        }

        fun bitmapToFile(bitmap: Bitmap, dir: String, fileNameToSave: String): File? {
            // File name like "image.png"
            //create a file to write bitmap data
            return try {
                val file = File(dir + File.separator + fileNameToSave)
                file.createNewFile()
                //Convert bitmap to byte array
                val bos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 95, bos) // YOU can also save it in JPEG
                val bitmapData = bos.toByteArray()
                bos.close()
                //write the bytes in file
                val fos = FileOutputStream(file)
                fos.write(bitmapData)
                fos.flush()
                fos.close()
                file
            } catch (e: Exception) {
                Logging.e(Constants.APP_NAME + "/bitmapToFile:\n" + e.stackTraceToString())
                null // it will return null
            }
        }

        fun loadBitmap(context: Context, filename: String, max: Int):Bitmap? {
            if (filename.isNotEmpty()) {

                try {
                    var bitmap = BitmapResolver.getBitmap(context.contentResolver, filename)
                    if (bitmap == null){
                        Logging.d("fillGraph","File not found or no access: $filename" )
                        bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.logo)
                    }
                    return scaleBitmap(bitmap, max)
                } catch (e: Exception) {
                    Logging.w("fillGraph",e.toString() )
                    return null
                }
            }
            return null
        }

        fun scaleBitmap(bitmap: Bitmap?, max: Int):Bitmap? {
            if (bitmap == null) {
                return null
            }
            try {
                if (max <= 0){
                    return bitmap
                }
                var aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
                val newHeight = (max * aspectRatio).roundToInt()
                // in case of an very wide photo
                aspectRatio = bitmap.height.toFloat() / bitmap.width.toFloat()
                val newWidth = (max * aspectRatio).roundToInt()

                if (newHeight > max){
                    return bitmap.scale(max, newWidth, false)
                }
                return bitmap.scale(newHeight, max, false)
            } catch (e: Exception) {
                Logging.w("fillGraph", e.toString())
            }
            return null
        }
    }

    fun calcBitmap(filename: String?, title: String?, rotation: Int): Bitmap? {

        var bitmapNew: Bitmap? = null
        var bitmap: Bitmap? = null

        if (!filename.isNullOrEmpty()){
            try {
                val f = File(filename)
                if (f.exists()) {
                    val op: BitmapFactory.Options = BitmapFactory.Options()
                    op.inJustDecodeBounds = false
                    // resize picture to number of columns
                    op.inSampleSize = if (noCols == 2) 3 else 4
                    // op.inScaled = true;
                    op.inPreferredConfig = Bitmap.Config.ARGB_8888
                    try {
                        bitmap = BitmapResolver.getBitmap(context.contentResolver, filename)
                        val bAccess = (bitmap != null)
                        if (!bAccess){
                            bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.logo)
                        }
                        try {
                            if (rotation.toFloat()!=0f){
                                Logging.d("calcBitmap","file: $filename rotation: $rotation")
                                val matrix = Matrix()
                                matrix.postRotate(rotation.toFloat())
                                bitmap = Bitmap.createBitmap(
                                    bitmap!!, 0, 0,
                                    bitmap.width, bitmap.height,
                                    matrix, true
                                )
                            }

                        } catch(e: Exception){
                            // lazy
                        }

                    } catch (e: OutOfMemoryError) {
                        ToastExt().makeText(
                            context, R.string.mess017_lowmemory,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {

                Logging.d("calcBitmap","file: $filename url not valid")
            }
        }

        try {
            if (bitmap == null) {
                Logging.d("calcBitmap","file: $filename cannot convert to bitmap")
            } else {
                // build square without scale
                val min: Int = bitmap.height.coerceAtMost(bitmap.width)
                bitmapNew = Bitmap.createBitmap(
                    bitmap,
                    (bitmap.width - min) / 2,
                    (bitmap.height - min) / 2, min, min
                )
                bitmapNew = bitmapNew.scale(imageMaxSize, imageMaxSize)
            }
        } catch (e: OutOfMemoryError) {
            ToastExt().makeText(
                context, R.string.mess017_lowmemory,
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            ToastExt().makeText(
                context,
                "" + context.getText(R.string.error_url_not_valid) + e.message,
                Toast.LENGTH_SHORT
            ).show()
        }
        if (bitmap == null) {
            return null
        }
        val bw = bitmap.width
        val bh = bitmap.height
        bitmap.recycle()
        if (bitmapNew == null) {
            bitmapNew = bitmapSquare
        }
        if (title != null) {
            val paint = Paint()
            paint.color = Color.BLACK
            paint.isFakeBoldText = true
            paint.strokeWidth=ViewUtils.getDip(R.dimen.normal_2sp, context)
            paint.style = Paint.Style.STROKE
            paint.isAntiAlias = true
            paint.textSize = ViewUtils.getDip(R.dimen.font_size_textview, context)
            paint.style = Paint.Style.FILL
            val textRect = TextRect(paint)
            val canvas = Canvas(bitmapNew)
            //canvas.drawText(title, 1, bitmapNew.getHeight() - 10, paint);
            textRect.prepare(title, bw, bh)
            textRect.draw(canvas, 1, bitmapNew.height - textRect.textHeight)
        }
        return bitmapNew
    }

    init {
        noCols=noOfCols
        imageMaxSize = (Constants.currentBounds.width() / noCols - 10 - noCols * 2)
        bitmapSquare = createBitmap(imageMaxSize, imageMaxSize, Bitmap.Config.RGB_565)
        val c = Canvas(bitmapSquare)
        val p = Paint()
        p.color = Color.WHITE
        p.strokeWidth=ViewUtils.getDip(R.dimen.normal_2sp, context)
        c.drawLine(0f, 0f, 0f, bitmapSquare.width.toFloat(), p)
        c.drawLine(0f, 0f, bitmapSquare.height.toFloat(), 0f, p)
        c.drawLine(
            bitmapSquare.width.toFloat(), bitmapSquare.height.toFloat(),
            bitmapSquare.height.toFloat(), 0f, p
        )
    }



//endregion


}