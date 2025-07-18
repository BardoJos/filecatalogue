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
package com.farrusco.filecatalogue.basis

import android.content.Context
import android.graphics.Canvas
import android.view.KeyEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.farrusco.filecatalogue.R

class Zoom(context: Context) : View(context) {
    private val image = ContextCompat.getDrawable(context, R.drawable.ic_menu_save)
    private var zoomController = 20
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (image != null){
            image.setBounds(
                width / 2 - zoomController,
                height / 2 - zoomController,
                width / 2 + zoomController,
                height / 2 + zoomController
            )
            image.draw(canvas)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            zoomController += 10
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            zoomController -= 10
        }
        if (zoomController < 10) {
            zoomController = 10
        }
        invalidate()
        return true
    }

    init {
        isFocusable = true
    }
}