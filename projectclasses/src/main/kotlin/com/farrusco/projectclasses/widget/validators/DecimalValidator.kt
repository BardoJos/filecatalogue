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

import android.widget.EditText
import com.farrusco.projectclasses.utils.CalcObjects
import java.lang.Double.parseDouble
import java.text.DecimalFormatSymbols

class DecimalValidator(error: String) : Validator(error)  {

    override fun check(e: EditText?): Boolean {
        if (e?.text == null) return false
        val separator: Char = DecimalFormatSymbols.getInstance().decimalSeparator
        val string = e.text.toString()
        try {
            val num = parseDouble(string.replace(separator, '.'))
            return num == CalcObjects.roundTo(num,2)
        } catch (_: NumberFormatException) {
            return false
        }
    }
}