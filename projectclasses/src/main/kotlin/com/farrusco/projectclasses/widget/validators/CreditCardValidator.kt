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
import java.util.regex.Pattern

class CreditCardValidator(error: String?) : Validator(error) {
    private var validatedType = -1
    override fun check(e: EditText?): Boolean {
        var number = e?.text.toString()
        val m = Pattern.compile("[^\\d\\s.-]").matcher(number)
        if (m.find()) return false
        val matcher = Pattern.compile("[\\s.-]").matcher(number)
        number = matcher.replaceAll("")
        validatedType = -1
        for (type in CARDS) {
            if (validate(number, type)) {
                validatedType = type
                return true
            }
        }
        return false
    }

    // Check that cards start with proper digits for
    // selected card type and are also the right length.    
    private fun validate(number: String, type: Int): Boolean {
        when (type) {
            MASTERCARD -> if (number.length != 16 || number.substring(0, 2)
                    .toInt() < 51 || number.substring(0, 2).toInt() > 55
            ) {
                return false
            }
            VISA -> if (number.length != 13 && number.length != 16 ||
                number.substring(0, 1).toInt() != 4
            ) {
                return false
            }
            AMEX -> if (number.length != 15 ||
                number.substring(0, 2).toInt() != 34 &&
                number.substring(0, 2).toInt() != 37
            ) {
                return false
            }
            DISCOVER -> if (number.length != 16 ||
                number.substring(0, 5).toInt() != 6011
            ) {
                return false
            }
            DINERS -> if (number.length != 14 ||
                number.substring(0, 2).toInt() != 36 &&
                number.substring(0, 2).toInt() != 38 &&
                number.substring(0, 3).toInt() < 300 ||
                number.substring(0, 3).toInt() > 305
            ) {
                return false
            }
        }
        return luhnValidate(number)
    }

    // The Luhn algorithm is basically a CRC type
    // system for checking the validity of an entry.
    // All major credit cards use numbers that will
    // pass the Luhn check. Also, all of them are based
    // on MOD 10.
    private fun luhnValidate(numberString: String): Boolean {
        val charArray = numberString.toCharArray()
        val number = IntArray(charArray.size)
        var total = 0
        for (i in charArray.indices) {
            number[i] = Character.getNumericValue(charArray[i])
        }
        run {
            var i = number.size - 2
            while (i > -1) {
                number[i] *= 2
                if (number[i] > 9) number[i] -= 9
                i -= 2
            }
        }
        for (i in number.indices) total += number[i]
        return total % 10 == 0
    }

    companion object {
        const val MASTERCARD = 0
        const val VISA = 1
        const val AMEX = 2
        const val DISCOVER = 3
        const val DINERS = 4
        val CARDS = intArrayOf(MASTERCARD, VISA, AMEX, DISCOVER, DINERS)
    }
}