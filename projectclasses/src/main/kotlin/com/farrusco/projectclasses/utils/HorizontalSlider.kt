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
package com.farrusco.projectclasses.utils

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.ProgressBar
import kotlin.math.roundToLong

class HorizontalSlider : ProgressBar {
    private var listener: OnProgressChangeListener? = null

    interface OnProgressChangeListener {
        fun onProgressChanged(v: View?, progress: Int)
    }

    constructor(
        context: Context?, attrs: AttributeSet?,
        defStyle: Int
    ) : super(context, attrs, defStyle)

    constructor(
        context: Context?, attrs: AttributeSet?
    ) : super(context, attrs, android.R.attr.progressBarStyleHorizontal)

    constructor(context: Context?) : super(context)

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.action
        if (action == MotionEvent.ACTION_DOWN
            || action == MotionEvent.ACTION_MOVE
        ) {
            val mouse = event.x - padding
            val width = (width - 2 * padding).toFloat()
            var progress = (max.toFloat() * (mouse / width)).roundToLong()
            if (progress < 0) progress = 0
            if (listener != null) listener!!.onProgressChanged(this, progress.toInt())
        }
        return true
    }

    companion object {
        private const val padding = 2
    }
}