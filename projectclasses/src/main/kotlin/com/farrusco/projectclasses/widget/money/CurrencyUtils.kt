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
package com.farrusco.projectclasses.widget.money

import android.content.Context
import java.io.IOException
import java.nio.charset.StandardCharsets

@Suppress("unused")
object CurrencyUtils {

    @JvmStatic
    fun getJsonFromAssets(context: Context): String? {
        val jsonString: String = try {
            val cujs = context.assets.open("currencies.json")
            val size = cujs.available()
            val buffer = ByteArray(size)
            cujs.read(buffer)
            cujs.close()
            String(
                buffer,
                StandardCharsets.UTF_8
            )
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
        return jsonString
    }
}
