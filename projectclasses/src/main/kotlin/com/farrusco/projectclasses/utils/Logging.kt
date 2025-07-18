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

import android.util.Log
import com.farrusco.projectclasses.BuildConfig
import com.farrusco.projectclasses.common.Constants

object Logging {
    private const val LOG_FORMAT = "%1\$s\n%2\$s"

    fun d(message: String?, vararg args: Any?) {
        if (BuildConfig.DEBUG) {
            log(Log.DEBUG, null, message, *args)
        }
    }

    fun i(message: String?, vararg args: Any?) {
        log(Log.INFO, null, message, *args)
    }

    fun w(message: String?, vararg args: Any?) {
        log(Log.WARN, null, message, *args)
    }

    fun e(ex: Throwable?) {
        log(Log.ERROR, ex, null)
    }

    fun e(message: String?, vararg args: Any?) {
        log(Log.ERROR, null, message, *args)
    }

    fun e(ex: Throwable?, message: String?, vararg args: Any?) {
        log(Log.ERROR, ex, message, *args)
    }

    private fun log(priority: Int, ex: Throwable?, message: String?, vararg args: Any?) {
        if (!BuildConfig.DEBUG) return
        var messageLocal = message
        if (args.isNotEmpty()) {
            for(obj in args) {
                messageLocal+="\n" + obj.toString()
            }
        }
        val log: String? = if (ex == null) {
            messageLocal
        } else {
            val logMessage = messageLocal ?: ex.message
            val logBody = Log.getStackTraceString(ex)
            String.format(
                LOG_FORMAT,
                logMessage,
                logBody
            )
        }
        Log.println(priority, Constants.APP_NAME, log!!)
    }
}