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
import android.graphics.*
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.exifinterface.media.ExifInterface
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.farrusco.projectclasses.R
import com.farrusco.projectclasses.common.extensionApplication
import com.farrusco.projectclasses.utils.FilesFolders
import com.farrusco.projectclasses.widget.TouchImageView
import com.farrusco.projectclasses.widget.ViewPagerExt
import java.io.File
import java.io.FileInputStream
import java.text.DecimalFormat
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import androidx.core.graphics.scale

class Graphics {
    private val series: Series = Series()
    fun drawRadar(bitmap: Bitmap) {
        //val bDrawCircle = true
        val canvas = Canvas(bitmap)
        val height = canvas.height
        val width = canvas.width
        val middlexpos = height.coerceAtMost(width) / 2
        var middleypos = middlexpos - 10
        val radius = middlexpos - 20
        val steps: Double = series.getMaxXStep().toDouble()
        var path: Path
        // look for xml background
        if (series.getMax(true) == 0 || series.getMax(false) == 0) return
        // draw lines not less 3 because is not rounded (straight line)
        val axesstep: Int = 360 / series.getMax(true).coerceAtLeast(3)
        val high: Double = series.getMaxXValue().toDouble()
        var paint = Paint()
        paint.color = Color.BLACK
        paint.strokeWidth = 1f
        paint.style = Paint.Style.STROKE
        paint.isAntiAlias = true
        val df = DecimalFormat("#0.0")
        var minutes = 0f
        // max length description 
        val maxlen = 100

        // draw lines of clock
        var textRect: TextRect
        val hand = radius - maxlen + 20
        middleypos = middleypos.coerceAtMost(hand + 35)
        val m: Int = series.getMax(true)
/*        if (bDrawCircle){
            canvas.drawCircle(middlexpos.toFloat(),middleypos.toFloat(),
                hand.toFloat(),paint)
        }*/
        for (i in 0 until m) {
            val angle = Math.toRadians((90 - minutes).toDouble())
            val xpos = (middlexpos + hand * cos(angle)).toFloat()
            val ypos = (middleypos - hand * sin(angle)).toFloat()
            var xposcorr = 0
            var yposcorr = 0
            var tmpMaxlen = maxlen
            canvas.drawLine(middlexpos.toFloat(), middleypos.toFloat(), xpos, ypos, paint)
            // determine label outside radar

            paint.textAlign = Paint.Align.RIGHT
            when {
                minutes == 0f -> {
                    paint.textAlign = Paint.Align.CENTER
                    tmpMaxlen = width
                    yposcorr = -30
                }
                minutes == 90f -> {
                    paint.textAlign = Paint.Align.LEFT
                    xposcorr = 5
                }
                minutes == 180f -> {
                    paint.textAlign = Paint.Align.CENTER
                    tmpMaxlen = width
                    yposcorr = 10
                }
                minutes == 270f -> {
                    paint.textAlign = Paint.Align.RIGHT
                    xposcorr = -5
                }
                minutes < 90 -> {
                    paint.textAlign = Paint.Align.LEFT
                    xposcorr = 5
                    yposcorr = -5
                }
                minutes < 180 -> {
                    paint.textAlign = Paint.Align.LEFT
                    xposcorr = 5
                    yposcorr = -5
                }
                minutes < 270 -> {
                    paint.textAlign = Paint.Align.RIGHT
                    xposcorr = -5
                    yposcorr = -5
                }
                else -> {
                    paint.textAlign = Paint.Align.RIGHT
                    xposcorr = -5
                    yposcorr = -5
                }
            }
            paint.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
            textRect = TextRect(paint)
            textRect.prepare(series.getDescription(true, i)!!, tmpMaxlen, 16)
            textRect.draw(
                canvas, (xpos + xposcorr).toInt(),
                (ypos + yposcorr).toInt()
            )
            minutes += axesstep.toFloat()
        }
        paint = Paint()
        paint.color = Color.BLACK
        paint.strokeWidth = 1f
        paint.style = Paint.Style.STROKE
        paint.isAntiAlias = true

        // draw lines radar every step
        var dblStep = steps
        var step = hand / (high / steps)
        while (step <= hand) {
            path = Path()
            var ax = 0f
            while (ax <= 360) {
                val angle = Math.toRadians((90 - ax).toDouble())
                val xpos = (middlexpos + step * cos(angle)).toFloat()
                val ypos = (middleypos - step * sin(angle)).toFloat()
                if (ax == 0f) {
                    path.moveTo(xpos, ypos)
                    canvas.drawText(df.format(dblStep), xpos, ypos, paint)
                    dblStep += steps
                } else {
                    path.lineTo(xpos, ypos)
                }
                ax += axesstep.toFloat()
            }

/*            if (bDrawCircle){
                canvas.drawCircle(middlexpos.toFloat(),middleypos.toFloat(),
                    step.toFloat(),paint)
            } else {*/
                canvas.drawPath(path, paint)
//            }
            step += (hand / (high / steps))
        }

        // draw graph series
        paint = Paint()
        paint.color = Color.RED
        paint.strokeWidth = 1f
        paint.style = Paint.Style.STROKE
        paint.isAntiAlias = true
        var bottomline = (middleypos + hand + 20).toFloat()
        run {
            var y = 0
            val my: Int = series.getMax(false)
            while (y < my) {
                paint.color = series.getColor(false, y)
                paint.alpha = series.getAlpha(false, y)
                paint.style = series.getPaintStyle(false, y)
                paint.strokeWidth = series.getStrokeWidth(false, y)

                path = Path()
                minutes = 0f
                var xposstart = 0f
                var yposstart = 0f
                val mx: Float = series.getMax(true).toFloat()
                for (x in 0 until mx.toInt()) {
                    val angle = Math.toRadians((90 - minutes).toDouble())
                    // voorkomen dat de lijn buiten grafiek gaat lopen
                    // 'my' is niet goed moet 5f zijn ipv 3

                    // coerceAtMost: deze zorgt dat de waarde de maximum waarde niet overschrijdt
                    val pos: Float = (hand / (series.getMaxXValue() )
                            * series.getAmount(x, y).coerceAtMost(high.toFloat()))
                    val xpos = (middlexpos + pos * cos(angle)).toFloat()
                    val ypos = (middleypos - pos * sin(angle)).toFloat()
                    if (x == 0) {
                        path.moveTo(xpos, ypos)
                        xposstart = xpos
                        yposstart = ypos
                    } else {
                        path.lineTo(xpos, ypos)
                    }
                    if (paint.style != Paint.Style.FILL) {
                        canvas.drawCircle(xpos, ypos, 5f + (y * 5), paint)
                    }
                    minutes += axesstep.toFloat()
                }
                val painty = Paint()
                painty.color = series.getColor(false, y)
                painty.alpha = series.getAlpha(false, y)
                painty.style = series.getPaintStyle(false, y)
                painty.strokeWidth = series.getStrokeWidth(false, y)
                path.lineTo(xposstart, yposstart)
                canvas.drawPath(path, painty)
                if (painty.style != Paint.Style.FILL) {
                    canvas.drawCircle(xposstart, yposstart, 5f + (y * 5), painty)
                }
                y++
            }
        }

        // teken data
        val marginHorz = 2
        textRect = TextRect(paint)
        val sizeLedgend = Point()
        sizeLedgend.x = width - 160
        sizeLedgend.y = 5
        drawLedgend(canvas, sizeLedgend)

        bottomline += 20f
        val horzlabel: Float = ((width - 200) / series.getMax(false)).toFloat()
        paint.color = Color.BLACK
        canvas.drawText(
            series.getTitle(false), marginHorz.toFloat(),
            bottomline, paint
        )
        val my: Int = series.getMax(false)
        for (y in 0 until my) {
            val desc: String = series.getDescription(false, y)!!
            // canvas.drawText(desc, 175 + y
            // * horzlabel, bottomline, paint);
            textRect.prepare(desc, horzlabel.toInt(), 16)
            textRect.draw(
                canvas, (175 + y * horzlabel).toInt(),
                bottomline.toInt() - 10
            )
        }
        bottomline += 5f
        canvas.drawLine(
            marginHorz.toFloat(),
            bottomline,
            (width - marginHorz).toFloat(),
            bottomline,
            paint
        )
        bottomline += 13f

        for (x in 0 until series.getMax(true)) {
            canvas.drawText(
                series.getDescription(true, x)!!, marginHorz.toFloat(), bottomline,
                paint
            )
            for (y1 in 0 until series.getMax(false)) {
                val value: Float = series.getAmount(x, y1)
                canvas.drawText(
                    DecimalFormat("#0.00").format(value.toDouble()), 175
                            + y1 * horzlabel, bottomline, paint
                )
            }
            bottomline += 13f
        }
    }

    private fun drawLedgend(canvas: Canvas, size: Point): Point {
        val paint = Paint()
        paint.style = Paint.Style.STROKE
        var bottomline = size.y + 13
        paint.strokeWidth = 1f
        paint.color = Color.BLACK
        paint.isAntiAlias = true
        val textRect = TextRect(paint)
        val my: Int = series.getMax(false)
        for (y in 0 until my) {
            val paintsample = Paint()
            paintsample.color = series.getColor(false, y)
            paintsample.alpha = series.getAlpha(false, y)
            paintsample.style = Paint.Style.FILL
            canvas.drawRect(
                (size.x + 5).toFloat(), (bottomline - 9).toFloat(), (size.x + 25).toFloat(),
                bottomline.toFloat(), paintsample
            )
            textRect.prepare(series.getDescription(false, y)!!, 100, 16)
            textRect.draw(canvas, size.x + 30, bottomline - 10)
            bottomline += 13
        }
        // teken vierkant rondom legend
        paint.color = Color.BLACK
        canvas.drawRect(
            size.x.toFloat(),
            size.y.toFloat(),
            (size.x + 140).toFloat(),
            (bottomline - 10).toFloat(),
            paint
        )
        size.x += 140
        size.y = bottomline - 10
        return size
    }

    fun getSeries(): Series {
        return series
    }

    companion object{
        fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
            val matrix = Matrix()
            matrix.postRotate(angle)
            return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        }
/*
        fun rotateGifDrawable(source: GifDrawable, angle: Float): GifDrawable? {
            val matrix = Matrix()
            matrix.postRotate(angle)
            return GifDrawable. .createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        }
*/

        fun fillTouchImageView(context: Context, filename: String, rotation: Float,
                               touchImageView: TouchImageView){
            val mH = touchImageView.maxHeight
            val mW = touchImageView.maxWidth
            val extApp = extensionApplication(context,filename)
            if (extApp.resourceIcon > 0) {
                val bm = BitmapFactory.decodeResource(touchImageView.resources, extApp.resourceIcon)
                touchImageView.setImageBitmap(bm)
                return
            }
 /*           if (File(filename).extension.equals("gif",true)){

            }*/
            //var imageSet = false
            if (!File(filename).exists()){
                var width = touchImageView.width
                if (width == 0){
                    width = (touchImageView.parent as ViewPagerExt).width
                }
                BitmapManager.setDeleteImage(context, width, (touchImageView  as ImageView))
                return
            }
            Glide.with(context)
                .asBitmap()
                .load(filename)
                .error(R.drawable.logo)
                .into(object : CustomTarget<Bitmap>(){
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                    ) {
                        //imageSet = true
                        if (resource.height > 4500 || resource.width > 4500){
                            val maxSize = 4500
                            var max = Integer.min(mH, mW)
                            if (maxSize < max){
                                max = maxSize
                            }
                            touchImageView.setImageBitmap(rotateBitmap(resizeBitmap(resource,max), rotation))
                        } else {
                            touchImageView.setImageBitmap(rotateBitmap(resource, rotation))
                        }
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {
                        // this is called when imageView is cleared on lifecycle call or for
                        // some other reason.
                        // if you are referencing the bitmap somewhere else too other than this imageView
                        // clear it here as you can no longer have the bitmap
                    }
                })
/*            if(!imageSet){
                touchImageView.setImageResource(R.drawable.logo)
            }*/
        }

        fun getFileRotation (filename: String): Float{
            var rotation = 0f
            val file = File(filename)
            if (FilesFolders.hasFileAccess(file.path)){
                val fs = FileInputStream(filename)
                val exif = ExifInterface(fs)
                when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> rotation = 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> rotation = 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> rotation = 270f
                }
                fs.close()
            }
            return rotation
        }

        fun resizeBitmap(bitmap: Bitmap, max: Int):Bitmap {
            var aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
            val newHeight = (max * aspectRatio).roundToInt()
            // incase of an very wide photo
            aspectRatio = bitmap.height.toFloat() / bitmap.width.toFloat()
            val newWidth = (max * aspectRatio).roundToInt()

            return if (newHeight > max){
                bitmap.scale(max, newWidth, false)
            } else{
                bitmap.scale(newHeight, max, false)
            }
        }
    }
}