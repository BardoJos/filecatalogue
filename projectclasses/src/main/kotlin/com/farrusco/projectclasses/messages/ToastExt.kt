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

import android.annotation.SuppressLint
import android.content.Context
import android.text.SpannableStringBuilder
import android.text.style.RelativeSizeSpan
import android.widget.Toast
import com.farrusco.projectclasses.R

class ToastExt {
    private lateinit var toast: Toast
    fun show(): ToastExt {
        if (::toast.isInitialized){
            toast.show()
        }
        return this
    }
    @SuppressLint("ResourceType")
    fun makeText(context: Context, message:String, duration: Int = Toast.LENGTH_SHORT): ToastExt {
        val toastText = SpannableStringBuilder(message)
        val span = try {
            context.getText(R.dimen.toast_span).toString().toFloat()
        } catch (_: Exception) { 1f}
            toastText.setSpan(RelativeSizeSpan(span), 0, toastText.length, 0)
            toast = Toast.makeText(
                context, toastText,
                duration
        )
        return this
    }
    fun makeText(context: Context, message:Int, duration: Int = Toast.LENGTH_SHORT): ToastExt {
        val locMess = context.getText(message).toString()
        makeText(context,locMess, duration)
        return this
    }
    fun makeText(context: Context, message:CharSequence, duration: Int = Toast.LENGTH_SHORT): ToastExt {
        makeText(context,message.toString(), duration)
        return this
    }
}
