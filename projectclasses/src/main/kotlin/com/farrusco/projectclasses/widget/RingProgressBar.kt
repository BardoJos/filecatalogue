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
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import com.farrusco.projectclasses.R

class RingProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {
    private val paint: Paint = Paint()
    private var width = 0
    private var height = 0
    private var result = 0
    private var padding = 0f
    private var ringColor: Int
    var ringProgressColor: Int
    var textColor: Int
    var textSize: Float
    var ringWidth: Float

    var max: Int = 0
        set(value) {
            require(value >= 0) { "The max progress of 0" }
            field = value
        }
    private var progress: Int
    private var textIsShow: Boolean
    var style: Int // 0,1
    private var mOnProgressListener: OnProgressListener? = null
    private var centre = 0
    private var radius = 0

    init {

        result = dp2px(100)

        val mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.RingProgressBar)
        ringColor = mTypedArray.getColor(R.styleable.RingProgressBar_ringColor, Color.BLACK)
        ringProgressColor = mTypedArray.getColor(
            R.styleable.RingProgressBar_ringProgressColor,
            Color.WHITE
        )
        textColor = mTypedArray.getColor(R.styleable.RingProgressBar_textColor, Color.BLACK)
        textSize = mTypedArray.getDimension(R.styleable.RingProgressBar_textSize, 16f)
        ringWidth = mTypedArray.getDimension(R.styleable.RingProgressBar_ringWidth, 5f)
        max = mTypedArray.getInteger(R.styleable.RingProgressBar_max, 100)
        textIsShow = mTypedArray.getBoolean(R.styleable.RingProgressBar_textIsShow, true)
        style = mTypedArray.getInt(R.styleable.RingProgressBar_style, 0)
        progress = mTypedArray.getInteger(R.styleable.RingProgressBar_progress, 0)
        padding = mTypedArray.getDimension(R.styleable.RingProgressBar_ringPadding, 5f)
        mTypedArray.recycle()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        centre = getWidth() / 2
        radius = (centre - ringWidth / 2).toInt()
        drawCircle(canvas)
        drawTextContent(canvas)
        drawProgress(canvas)
    }

    private fun drawCircle(canvas: Canvas) {
        paint.color = ringColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = ringWidth
        paint.isAntiAlias = true
        canvas.drawCircle(centre.toFloat(), centre.toFloat(), radius.toFloat(), paint)
    }

    private fun drawTextContent(canvas: Canvas) {
        paint.strokeWidth = 0f
        paint.color = textColor
        paint.textSize = textSize
        paint.typeface = Typeface.DEFAULT
        val percent = (progress.toFloat() / max.toFloat() * 100).toInt()
        val textWidth = paint.measureText("$percent%")
        if (textIsShow && percent != 0 && style == STROKE) {
            canvas.drawText("$percent%", centre - textWidth / 2, centre + textSize / 2, paint)
        }
    }

    private fun drawProgress(canvas: Canvas) {
        paint.strokeWidth = ringWidth
        paint.color = ringProgressColor

        val strokeOval = RectF(
            (centre - radius).toFloat(), (centre - radius).toFloat(), (centre + radius).toFloat(),
            (
                    centre + radius).toFloat()
        )
        val fillOval = RectF(
            centre - radius + ringWidth + padding,
            centre - radius + ringWidth + padding, centre + radius - ringWidth - padding,
            centre + radius - ringWidth - padding
        )
        when (style) {
            STROKE -> {
                paint.style = Paint.Style.STROKE
                paint.strokeCap = Paint.Cap.ROUND
                canvas.drawArc(strokeOval, -90f, (360 * progress / max).toFloat(), false, paint)
            }

            FILL -> {
                paint.style = Paint.Style.FILL_AND_STROKE
                paint.strokeCap = Paint.Cap.ROUND
                if (progress != 0) {
                    canvas.drawArc(fillOval, -90f, (360 * progress / max).toFloat(), true, paint)
                }
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        width = if (widthMode == MeasureSpec.AT_MOST) {
            result
        } else {
            widthSize
        }

        height = if (heightMode == MeasureSpec.AT_MOST) {
            result
        } else {
            heightSize
        }

        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        width = w
        height = h
    }

    @Synchronized
    fun getProgress(): Int {
        return progress
    }

    @Synchronized
    fun setProgress(progress: Int) {
        var progressx = progress
        require(progressx >= 0) { "The progress of 0" }
        if (progressx > max) {
            progressx = max
        }
        if (progressx <= max) {
            progressx = progress
            postInvalidate()
        }
        if (progressx == max) {
            if (mOnProgressListener != null) {
                mOnProgressListener!!.progressToComplete()
            }
        }
    }

    private fun dp2px(dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density + 0.5f).toInt()
    }

    interface OnProgressListener {
        fun progressToComplete()
    }

    fun setOnProgressListener(mOnProgressListener: OnProgressListener?) {
        this.mOnProgressListener = mOnProgressListener
    }

    companion object {
        const val STROKE = 0
        const val FILL = 1
    }
}