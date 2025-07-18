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
package com.farrusco.projectclasses.barcodescanner.ui.custom

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.farrusco.projectclasses.R

@SuppressLint("CustomViewStyleable")
class ViewFinderOverlay(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val boxPaint: Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.barcode_reticle_stroke)
        style = Paint.Style.STROKE
        strokeWidth = context.resources.getDimensionPixelOffset(R.dimen.normal_4sp).toFloat()
    }

    private val scrimPaint: Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.barcode_reticle_background)
    }

    private val eraserPaint: Paint = Paint().apply {
        strokeWidth = boxPaint.strokeWidth
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private val boxCornerRadius: Float =
        context.resources.getDimensionPixelOffset(R.dimen.normal_9sp).toFloat()

    private var boxRect: RectF? = null

    private var runAnimation = true
    private lateinit var handler: Handler
    private var refreshRunnable = Runnable{
        refreshView()
    }
    private var isGoingDown = true
    private var mPosY = 0f
    private val paint = Paint()
    private var myCanvas = Canvas()

    init {
        val typedArray: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.BarcodeScanning)
        runAnimation = typedArray.getBoolean(R.styleable.BarcodeScanning_isAnimated, true)
        typedArray.recycle()
    }

    fun setViewFinder() {
        val overlayWidth = width.toFloat()
        val overlayHeight = height.toFloat()
        val boxWidth = overlayWidth * 80 / 100
        val boxHeight = overlayHeight * 36 / 100
        val cx = overlayWidth / 2
        val cy = overlayHeight / 2
        boxRect = RectF(cx - boxWidth / 2, cy - boxHeight / 2, cx + boxWidth / 2, cy + boxHeight / 2)

        handler = Handler(Looper.getMainLooper())

        paint.color = Color.RED
        paint.strokeWidth = 5.0f
        mPosY = boxRect!!.top + 8

        invalidate()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        boxRect?.let {
            // Draws the dark background scrim and leaves the box area clear.
            canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), scrimPaint)
            // As the stroke is always centered, so erase twice with FILL and STROKE respectively to clear
            // all area that the box rect would occupy.
            eraserPaint.style = Paint.Style.FILL
            canvas.drawRoundRect(it, boxCornerRadius, boxCornerRadius, eraserPaint)
            eraserPaint.style = Paint.Style.STROKE
            canvas.drawRoundRect(it, boxCornerRadius, boxCornerRadius, eraserPaint)
            // Draws the box.
            canvas.drawRoundRect(it, boxCornerRadius, boxCornerRadius, boxPaint)

            if (runAnimation) {
                canvas.drawLine(
                    boxRect!!.left + 8,
                    mPosY,
                    boxRect!!.right - 8,
                    mPosY,
                    paint
                )
                handler.postDelayed(refreshRunnable, 0)
            }
        }
        myCanvas=canvas
    }

    private fun refreshView() {
        //Update new position of the line
        if (isGoingDown) {
            mPosY += 5
            if (mPosY > boxRect!!.bottom - 8) {
                mPosY = boxRect!!.bottom - 8
                isGoingDown = false
            }
        } else {
            //We invert the direction of the animation
            mPosY -= 5
            if (mPosY < boxRect!!.top + 8) {
                mPosY = boxRect!!.top + 8
                isGoingDown = true
            }
        }
        this.invalidate()
    }

}