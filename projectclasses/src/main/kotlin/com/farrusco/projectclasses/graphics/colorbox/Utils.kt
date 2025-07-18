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

@file:Suppress("unused")

package com.farrusco.projectclasses.graphics.colorbox

import android.content.Context
import com.farrusco.projectclasses.R
import com.farrusco.projectclasses.utils.CalcObjects
import androidx.core.graphics.toColorInt

object Utils {

    fun getColorNames(context: Context): Array<String> {
        return context.resources.getStringArray(R.array.colorNames)
    }

    fun translateColor(context: Context, kleur: String, dfltColor: Int): Int{
        var color = dfltColor
        val res = context.resources
        val colorNames = getColorNames(context)
        val ta = res.obtainTypedArray(R.array.colors)

        var col = 0
        for (i in colorNames.indices){
            if (colorNames[i].lowercase() == kleur.lowercase()){
                col = ta.getResourceId(i, 0)
                color =
                    ("#" + Integer.toHexString(res.getColor(col, context.theme))).toColorInt()
                break
            }
        }
        ta.recycle()
        if (col == 0 ) {
            if (CalcObjects.stringToInteger(kleur) != 0) {
                color = CalcObjects.stringToInteger(kleur)
            }
        }
        return color
    }
}
