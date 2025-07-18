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
package com.farrusco.projectclasses.messages

import android.content.Context
import android.os.Looper
import android.widget.Toast
import com.farrusco.projectclasses.utils.Logging

fun interface IToastExt {
    fun getContext(): Context?
    fun showMessage(message: String, duration: Int = Toast.LENGTH_LONG) {
        val mThread: Thread = object : Thread() {
            override fun run() {
                try {
                    Looper.prepare()
                    ToastExt().makeText(getContext()!!,message,duration).show()
                    Looper.loop()
                } catch (error: Exception) {
                    Logging.e("IShowMessage: ${error.message}")
                }
            }
        }
        mThread.start()
    }
    fun showMessage(message:Int, duration: Int = Toast.LENGTH_LONG) {
        val mThread: Thread = object : Thread() {
            override fun run() {
                try {
                    Looper.prepare()
                    ToastExt().makeText(getContext()!!,message,duration).show()
                    Looper.loop()
                } catch (error: Exception) {
                    Logging.e("IShowMessage: ${error.message}")
                }
            }
        }
        mThread.start()
    }
}