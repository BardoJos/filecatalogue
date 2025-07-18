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
package com.farrusco.projectclasses.widget.validators

import android.text.InputFilter
import android.text.Spanned

@Suppress("unused")
class InputFilterMinMax: InputFilter {
    private var min:Int = 0
    private var max:Int = 0
    private var useFloat = false
    private var minFloat:Float = 0f
    private var maxFloat:Float = 0f
    constructor(min:Int, max:Int) {
        this.min = min
        this.max = max
    }
    constructor(min:Float, max:Float) {
        this.minFloat = min
        this.maxFloat = max
        useFloat = true
    }
    constructor(min:String, max:String) {
        this.min = Integer.parseInt(min)
        this.max = Integer.parseInt(max)
    }
    override fun filter(source:CharSequence?, start:Int, end:Int, dest: Spanned?, dstart:Int, dend:Int): CharSequence? {
        try
        {
            if (useFloat) {
                val input = (dest.toString() + source.toString()).toFloat()
                if (isInRange(minFloat, maxFloat, input))
                    return null
            } else {
                val input = Integer.parseInt(dest.toString() + source.toString())
                if (isInRange(min, max, input))
                    return null
            }
        }
        catch (_:NumberFormatException) {}
        return ""
    }
    private fun isInRange(a:Int, b:Int, c:Int):Boolean {
        return if (b > a) c in a..b else c in b..a
    }
    private fun isInRange(a:Float, b:Float, c:Float):Boolean {
        return if (b > a) c in a..b else c in b..a
    }
}