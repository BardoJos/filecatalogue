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
package com.farrusco.filecatalogue.splash

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View

class OverlayView(context: Context?) : View(context) {
    private var mPaint = Paint()
    private var mBmSplash: Bitmap? = null
    private var mViewWidth = 0
    private var mViewHeight = 0
    override fun onDraw(canvas: Canvas) {
        mPaint.style = Paint.Style.FILL
        mPaint.color = Color.BLACK
        canvas.drawBitmap(
            mBmSplash!!, (mViewWidth / 2 - mBmSplash!!.width / 2).toFloat(), (
                    mViewHeight / 2 - mBmSplash!!.height / 2).toFloat(), mPaint
        )
        super.onDraw(canvas)
    }

    /**
     * For device independence and screen rotations use the current
     * display size rather than any hard coded values
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        mViewWidth = w
        mViewHeight = h
    }
}