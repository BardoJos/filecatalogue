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
package com.farrusco.filecatalogue

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.farrusco.filecatalogue.main.ApiMainPrepare

class SplashScreen : AppCompatActivity() {

    private var mHandler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_startup)
        mHandler = Handler(Looper.getMainLooper())
        mHandler!!.postDelayed(runnable, 1000)
    }

    private val runnable: Runnable = Runnable {
        if (!isFinishing) {
            startActivity(Intent(this, ApiMainPrepare::class.java))
            finish()
        }
    }

    public override fun onDestroy() {
        if (mHandler != null) {
            mHandler!!.removeCallbacks(runnable)
        }
        super.onDestroy()
    }
}