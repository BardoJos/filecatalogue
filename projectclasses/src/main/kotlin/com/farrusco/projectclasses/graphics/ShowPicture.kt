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
package com.farrusco.projectclasses.graphics

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.farrusco.projectclasses.R
import java.io.File

class ShowPhoto : Activity() {
    private var mImgPhoto: ImageViewGesture? = null
    private val gestureDetector: GestureDetector? = null
    private var gestureListener: View.OnTouchListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.show_photo)
        gestureListener = object : View.OnTouchListener {
            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (gestureDetector != null && event != null)  {
                    return  gestureDetector.onTouchEvent(event)
                }
                return false
            }
        }
        mImgPhoto = findViewById<View>(R.id.image) as ImageViewGesture?
        val sFile = intent.getStringExtra("photo")!!
        val file = File(sFile)
        if (file.exists()) {
            mImgPhoto!!.setFilename(intent.getStringExtra("photo"))
        } else {
            finish()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (gestureDetector != null) {
            return gestureDetector.onTouchEvent(event)
        }
        return false
    }
}