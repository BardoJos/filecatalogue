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

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.graphics.values
import com.farrusco.projectclasses.R
import com.farrusco.projectclasses.common.ConstantsFixed
import com.farrusco.projectclasses.utils.*
import java.io.File
import kotlin.math.*
import androidx.core.graphics.scale

open class ImageViewGesture : androidx.appcompat.widget.AppCompatImageView, View.OnTouchListener {
    private var touchRotation = true
    private var skipEditTagOnce = false
    var bitmap: Bitmap? = null
    private var widthX:Int = 0
    private var heightX:Int = 0
    private var canvasWidth:Int = 0
    private var canvasHeight:Int = 0

    // These matrices will be used to move and zoom image
    private var matrixOrg = Matrix()
    private var savedMatrix = Matrix()

    // Remember some things for zooming
    var start: PointF = PointF()
    //var oldDist = 1f
    private var _draw = false
    private var _drawCircle = false
    private var _filename: String? = null
    private var touchManager: TouchManager = TouchManager(2)
    private val position = Vector2D()
    private var angle = 0.0f

    // Debug helpers to draw lines between the two touch points
    private var vca: Vector2D = Vector2D()
    private var vcb: Vector2D = Vector2D()
    private var vpa: Vector2D = Vector2D()
    private var vpb: Vector2D = Vector2D()
    private var isInitialized = false
    private var imageScaleType: ScaleType? = null
    private var marginStart = 0f
    private var marginTop = 0f
    private var mScaleGestureDetector: ScaleGestureDetector
    private var mGestureDetector: GestureDetector
    private var mScaleFactor = 1f
    private var bitmapManager: BitmapManager
    private var imageActionState = ConstantsFixed.ImageActionState.NONE
    private val dragLEFT = 0
    private val dragRIGHT = 1
    private val dragTOP = 2
    private val dragDOWN = 3

    @SuppressLint("ClickableViewAccessibility")
    constructor(context: Context) : super(context) {
        @Suppress("LeakingThis")
        setOnTouchListener(this)
        mScaleGestureDetector = ScaleGestureDetector(
            context,
            ScaleListener()
        )
        mGestureDetector = GestureDetector(context, GestureListener())
        bitmapManager=BitmapManager(context,1)
    }

    @SuppressLint("ClickableViewAccessibility")
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        @Suppress("LeakingThis")
        setOnTouchListener(this)
        mScaleGestureDetector = ScaleGestureDetector(
            context,
            ScaleListener()
        )
        mGestureDetector = GestureDetector(context, GestureListener())
        bitmapManager=BitmapManager(context,1)
    }

    @SuppressLint("ClickableViewAccessibility")
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        @Suppress("LeakingThis")
        setOnTouchListener(this)
        mScaleGestureDetector = ScaleGestureDetector(
            context,
            ScaleListener()
        )
        // mScaleGestureDetector.setQuickScaleEnabled(true);
        mGestureDetector = GestureDetector(context, GestureListener())
        bitmapManager=BitmapManager(context,1)
    }

    override fun setImageBitmap(bitmap: Bitmap) {
        this.bitmap = bitmap
        widthX = bitmap.width
        heightX = bitmap.height
        // centerX = width >> 1;
    }

    fun setFilename(filename: String?) {
        try {
            matrixOrg = Matrix()
            savedMatrix = Matrix()
            showImage(filename)
        } catch (e: Exception) {
            // lazy
        }
    }

    fun fillGraph(filename: String, max: Int):Boolean {
        this.imageAlpha=R.drawable.logo
        if (filename.isNotEmpty()) {
            try {
                var bitmap = BitmapResolver.getBitmap(context.contentResolver, filename)
                if (bitmap == null){
                    Logging.i("fillGraph","File not found or no access: $filename" )
                    bitmap = BitmapFactory.decodeResource(resources, R.drawable.logo)
                }

                this.setImageResource(R.drawable.screen_background_black)
                var aspectRatio = bitmap!!.width.toFloat() / bitmap.height.toFloat()
                val newHeight = (max * aspectRatio).roundToInt()
                // incase of an very wide photo
                aspectRatio = bitmap.height.toFloat() / bitmap.width.toFloat()
                val newWidth = (max * aspectRatio).roundToInt()

                val bitmapNew = if (newHeight > max){
                    bitmap.scale(max, newWidth, false)
                } else{
                    bitmap.scale(newHeight, max, false)
                }

                if (imageScaleType!=null){
                    isInitialized = false
                }
                this.setImageBitmap(bitmapNew)
                //this.setImageBitmap(bm)
                this.setImageResource(0)
            } catch (e: Exception) {
                Logging.w("fillGraph",e.toString() )
                return false
            }
        }
        return true
    }

    @SuppressLint("DrawAllocation", "CanvasSize")
    override fun onDraw(canvasx: Canvas) {
        super.onDraw(canvasx)
        canvasWidth=canvasx.width
        canvasHeight=canvasx.height

        if (bitmap == null) return
        if (!isInitialized) {
            if (imageScaleType!=null){
                when (imageScaleType){
                    ScaleType.CENTER -> {
                        position.setX(canvasx.width/2f+marginStart, canvasx.height/2f+marginTop)
                    }
                    ScaleType.CENTER_INSIDE -> {
                        mScaleFactor=1f
                        position.setX(canvasx.width/2f+marginStart, canvasx.height/2f+marginTop)
                    }
                    else -> position.setX((widthX / 2f), (heightX / 2f))
                }
            } else{
                position.setX((widthX / 2f), (heightX / 2f))
            }
            isInitialized = true
        }
        val paint = Paint()
        matrixOrg.reset()
        matrixOrg.postTranslate(-widthX / 2.0f, -heightX / 2.0f)
        matrixOrg.postRotate(getDegreesFromRadians(angle))

        // matrix.postScale(scale, scale);
        matrixOrg.postScale(mScaleFactor, mScaleFactor)
        val values = matrixOrg.values()
        val width: Float = values[Matrix.MSCALE_X] * bitmap!!.width
        val height: Float = values[Matrix.MSCALE_Y] * bitmap!!.height

        if (position.x < -(width / 2f)) {
            position.x = (-width / 2f)
        } else if (position.x > canvasx.width + width / 2f) {
            position.x=(canvasx.width + width / 2f)
        }
        if (position.y < -(height / 2f)) {
            position.y=(-height / 2f)
        } else if (position.y > canvasx.height + height / 2) {
            position.y=(canvasx.height + height / 2f)
        }

        // height - canvasx.height - matrixOrg.values()[Matrix.MTRANS_Y]
        if (position.x < canvasx.width/2f+marginStart && width < canvasx.width){
            position.x = canvasx.width/2f+marginStart
        }
        if (position.y < canvasx.height/2f+marginTop && height < canvasx.height){
            position.y = canvasx.height/2f+marginTop
        }

        matrixOrg.postTranslate(position.x, position.y)
        canvasx.drawBitmap(bitmap!!, matrixOrg, paint)
        if (_drawCircle) {
            try {
                if (!vca.init) {
                    paint.color = -0xff8100
                    canvasx.drawCircle(vca.x, vca.y, 64f, paint)
                }
                if (!vcb.init) {
                    paint.color = -0x810000
                    canvasx.drawCircle(vcb.x, vcb.y, 64f, paint)
                }
                if (!vpa.init && !vpb.init) {
                    paint.color = -0x10000
                    canvasx.drawLine(
                        vpa.x, vpa.y,
                        vpb.x, vpb.y, paint
                    )
                }
                if (!vca.init && !vcb.init) {
                    paint.color = -0xff0100
                    canvasx.drawLine(
                        vca.x, vca.y,
                        vcb.x, vcb.y, paint
                    )
                }
            } catch (e: NullPointerException) {
                // Just being lazy here...
            }
        }
        super.onDraw(canvasx)
    }

    private fun showImage(filename: String?) {
        _filename = filename
        if (filename == null || widthX == 0 && heightX == 0) {
            _draw = true
            return
        }
        val file = File(filename)
        var bm: Bitmap?
        if (file.exists()) {
            bm = CalcObjects.decodeFile(file, -1)
            if (bm == null) bm = BitmapFactory.decodeResource(
                resources,
                R.drawable.logo
            )
        } else {
            bm = BitmapFactory.decodeResource(resources, R.drawable.logo)
        }
        val imageWidth = bm!!.width.toFloat()
        val imageHeight = bm.height.toFloat()
        // float newHeight = imageHeight / (imageWidth / width);
        val newWidth = widthX.toFloat()
        val newHeight = heightX.toFloat()
        val scaleWidth = newWidth / imageWidth
        val scaleHeight = newHeight / imageHeight
        setImageBitmap(bm)
        // scale to fit
        val scale = scaleWidth.coerceAtMost(scaleHeight)
        var y = (imageHeight * scale).toInt()
        y = (heightX - y) / 2
        var x = (imageWidth * scale).toInt()
        x = (widthX - x) / 2
        matrix.postScale(scale, scale)
        // center photo
        matrix.postTranslate(x.toFloat(), y.toFloat())
        imageMatrix = matrix
        scaleType = ScaleType.MATRIX
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        widthX = MeasureSpec.getSize(widthMeasureSpec)
        heightX = MeasureSpec.getSize(heightMeasureSpec)
        if (widthX > 0 && heightX > 0 && _draw) {
            _draw = false
            showImage(_filename)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        var w = widthX
        var h = heightX
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
            if (widthX < heightX) {
                w = heightX
                h = widthX
            }
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            if (widthX > heightX) {
                w = heightX
                h = widthX
            }
        }
        widthX = w
        heightX = h
        matrixOrg = Matrix()
        savedMatrix = Matrix()
        showImage(_filename)
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (bitmap == null) {
            imageActionState = ConstantsFixed.ImageActionState.NONE
            return false
        }

        mScaleGestureDetector.onTouchEvent(event)
        vca = Vector2D()
        vcb = Vector2D()
        vpa = Vector2D()
        vpb = Vector2D()
        try {
            touchManager.update(event)
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    vca = touchManager.getPoint2D(0)
                    //vca[mScaleGestureDetector.focusX.toDouble()] =
                    //    mScaleGestureDetector.focusY.toDouble()
                    vca.setX(mScaleGestureDetector.focusX, mScaleGestureDetector.focusY)
                    vca = touchManager.getPoint2D(0)
                    vpa = touchManager.getPreviousPoint2D(0)
                    position.add(touchManager.moveDelta2D(0))
                    imageActionState = ConstantsFixed.ImageActionState.DRAG
                }
                MotionEvent.ACTION_MOVE -> {
                    if (imageActionState == ConstantsFixed.ImageActionState.DRAG) {

                        vca = touchManager.getPoint2D(0)
                        vpa = touchManager.getPreviousPoint2D(0)
                        vcb = touchManager.getPoint2D(1)
                        vpb = touchManager.getPreviousPoint2D(1)
                        position.add(touchManager.moveDelta2D())
                        // float currentDistance = current.getLength();
                        // float previousDistance = previous.getLength();

                        // if (previousDistance != 0) {
                        // scale *= currentDistance / previousDistance;
                        // }
                        if (touchRotation) {
                            angle -= Vector2D.getSignedAngleBetween(
                                touchManager.getVector2D(0, 1),
                                touchManager.getPreviousVector2D(0, 1)
                            )
                        }
                        invalidate()
                        return true
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP ->
                    imageActionState = ConstantsFixed.ImageActionState.NONE
            }
            invalidate()
        } catch (t: Throwable) {
            // So lazy...
        }
        return true
    }

    private fun distance(point2: MotionEvent?, point1: PointF?): Float {
        var x = point1!!.x -point2!!.x
        if (x < 0) {
            x = -x
        }
        var y = point1.y -point2.y
        if (y < 0) {
            y = -y
        }
        return sqrt((x * x + y * y).toDouble()).toFloat()
    }

    private fun canDrag(matrix: Matrix, direction: Int): Boolean {
        val x = FloatArray(4)
        val y = FloatArray(4)
        val widthScreen = canvasWidth
        val heightScreen = canvasHeight
        getFourPoint(matrix, x, y)
        if ((x[0] > 0 || x[2] > 0 || x[1] < widthScreen || x[3] < widthScreen) && (y[0] > 0 || y[1] > 0 || y[2] < heightScreen || y[3] < heightScreen)) {
            return false
        }
        if (direction == dragLEFT) {
            if (x[1] < widthScreen || x[3] < widthScreen) {
                return false
            }
        } else if (direction == dragRIGHT) {
            if (x[0] > 0 || x[2] > 0) {
                return false
            }
        } else if (direction == dragTOP) {
            if (y[2] < heightScreen || y[3] < heightScreen) {
                return false
            }
        } else if (direction == dragDOWN) {
            if (y[0] > 0 || y[1] > 0) {
                return false
            }
        } else {
            return false
        }
        return true
    }

    private fun getFourPoint(matrix: Matrix, x: FloatArray?, y: FloatArray?) {
        val f = FloatArray(9)
        matrix.getValues(f)
        x!![0] = f[Matrix.MSCALE_X] * 0 + f[Matrix.MSKEW_X] * 0+ f[Matrix.MTRANS_X]
        y!![0] = f[Matrix.MSKEW_Y] * 0 + f[Matrix.MSCALE_Y] * 0+ f[Matrix.MTRANS_Y]
        x[1] = f[Matrix.MSCALE_X] * bitmap!!.width + f[Matrix.MSKEW_X] * 0+ f[Matrix.MTRANS_X]
        y[1] = f[Matrix.MSKEW_Y] * bitmap!!.width + f[Matrix.MSCALE_Y] * 0+ f[Matrix.MTRANS_Y]
        x[2] = f[Matrix.MSCALE_X] * 0 + f[Matrix.MSKEW_X] * bitmap!!.height + f[Matrix.MTRANS_X]
        y[2] = f[Matrix.MSKEW_Y] * 0 + f[Matrix.MSCALE_Y] * bitmap!!.height + f[Matrix.MTRANS_Y]
        x[3] = f[Matrix.MSCALE_X] * bitmap!!.width + f[Matrix.MSKEW_X]* bitmap!!.height + f[Matrix.MTRANS_X]
        y[3] = f[Matrix.MSKEW_Y] * bitmap!!.width + f[Matrix.MSCALE_Y]* bitmap!!.height + f[Matrix.MTRANS_Y]
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            mScaleFactor *= detector.scaleFactor

            // Don't let the object get too small or too large.
            mScaleFactor = max(0.1f, min(mScaleFactor, 5.0f))
            invalidate()
            return true
        }
    }

    internal inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        private var currentGestureDetected: String? = null

        // Override s all the callback methods of
        // GestureDetector.SimpleOnGestureListener
        override fun onSingleTapUp(ev: MotionEvent): Boolean {
            currentGestureDetected = ev.toString()
            return true
        }

        override fun onShowPress(ev: MotionEvent) {
            currentGestureDetected = ev.toString()
        }

        override fun onLongPress(ev: MotionEvent) {
            currentGestureDetected = ev.toString()
        }

        override fun onScroll(
            e1: MotionEvent?, e2: MotionEvent,
            distanceX: Float, distanceY: Float
        ): Boolean {
            currentGestureDetected = "$e1  $e2"
            return true
        }

        override fun onDown(ev: MotionEvent): Boolean {
            currentGestureDetected = ev.toString()
            return true
        }

        private fun onSwipeRight() {}
        private fun onSwipeLeft() {}
        private fun onSwipeTop() {}
        private fun onSwipeBottom() {}
        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            currentGestureDetected = "$e1  $e2"
            val result = false
            try {
                val diffY: Float = e2.y - (e1?.y ?: 0f)
                val diffX: Float = e2.x - (e1?.x ?: 0f)
                if (abs(diffX) > abs(diffY)) {
                    if (abs(diffX) > SWIPE_THRESHOLD
                        && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD
                    ) {
                        if (diffX > 0) {
                            onSwipeRight()
                        } else {
                            onSwipeLeft()
                        }
                    }
                    return true
                } else if (abs(diffY) > SWIPE_THRESHOLD
                    && abs(velocityY) > SWIPE_VELOCITY_THRESHOLD
                ) {
                    if (diffY > 0) {
                        onSwipeBottom()
                    } else {
                        onSwipeTop()
                    }
                }
                return true
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
            return result
        }
    }

    fun setText(text: String?, init: Boolean) {
        if (init) skipEditTagOnce = true
        //super.setText(formatString(text))
        if (!skipEditTagOnce){
            tag = TagModify.setTagValue(tag, ConstantsFixed.TagSection.TsModFlag.name,ConstantsFixed.TagAction.Edit.name)
        }
        TagModify.setViewTagValue(this, ConstantsFixed.TagSection.TsDBValue,text)
    }

    fun setDBColumn(dBColumn: String?, dBtable: String?): ImageViewGesture {
        TagModify.setViewTagValue(this, ConstantsFixed.TagSection.TsDBColumn,dBColumn)
        if (dBtable != null) TagModify.setViewTagValue(this, ConstantsFixed.TagSection.TsDBTable,dBtable)
        return this
    }

    fun setDBColumn(dBColumn: String?, groupno: Int): ImageViewGesture {
        TagModify.setViewTagValue(this, ConstantsFixed.TagSection.TsDBColumn,dBColumn)
        TagModify.setViewTagValue(this, ConstantsFixed.TagSection.TsGroupno,groupno.toString())
        return this
    }

    companion object {
        // We can be in one of these 3 states
        const val NONE = 0
        //const val DRAG = 1
        //const val ZOOM = 2
        private const val SWIPE_THRESHOLD = 500
        private const val SWIPE_VELOCITY_THRESHOLD = 500
        private fun getDegreesFromRadians(angle: Float): Float {
            return (angle * 180.0f / Math.PI).toFloat()
        }
    }
}