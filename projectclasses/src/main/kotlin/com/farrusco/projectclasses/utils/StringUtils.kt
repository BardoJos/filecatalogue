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

package com.farrusco.projectclasses.utils

import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import java.text.Normalizer
import java.util.Locale

object StringUtils {
    private const val EMPTY = ""
    private const val PAD_LIMIT = 8192
    private val PADDING = arrayOfNulls<String>(Character.MAX_VALUE.code)
    private val handler = CoroutineExceptionHandler { _, exception ->
        Log.e("ImageToText", "$exception")
    }

    fun isEmpty(str: String?): Boolean {
        return str.isNullOrEmpty()
    }

    fun isEmpty(str: String?, dflt: String?): String? {
        return if (str.isNullOrEmpty()) dflt else str
    }

    fun repeat(str: String?, repeat: Int): String? {
        // Performance tuned for 2.0 (JDK1.4)
        if (str == null) {
            return null
        }
        if (repeat <= 0) {
            return EMPTY
        }
        val inputLength = str.length
        if (repeat == 1 || inputLength == 0) {
            return str
        }
        if (inputLength == 1 && repeat <= PAD_LIMIT) {
            return padding(repeat, str[0])
        }
        val outputLength = inputLength * repeat
        return when (inputLength) {
            1 -> {
                val ch = str[0]
                val output1 = CharArray(outputLength)
                var i = repeat - 1
                while (i >= 0) {
                    output1[i] = ch
                    i--
                }
                String(output1)
            }
            2 -> {
                val ch0 = str[0]
                val ch1 = str[1]
                val output2 = CharArray(outputLength)
                var i = repeat * 2 - 2
                while (i >= 0) {
                    output2[i] = ch0
                    output2[i + 1] = ch1
                    i--
                    i--
                }
                String(output2)
            }
            else -> {
                val buf = StringBuffer(outputLength)
                var i = 0
                while (i < repeat) {
                    buf.append(str)
                    i++
                }
                buf.toString()
            }
        }
    }

    private fun padding(repeat: Int, padChar: Char): String {
        // be careful of synchronization in this method
        // we are assuming that get and set from an array index is atomic
        var pad = PADDING[padChar.code]
        if (pad == null) {
            pad = padChar.toString()
        }
        while (pad.length < repeat) {
            pad += pad
        }
        PADDING[padChar.code] = pad
        return pad.substring(0, repeat)
    }

    @JvmOverloads
    fun rightPad(str: String?, size: Int, padChar: Char = ' '): String? {
        if (str == null) {
            return null
        }
        val pads = size - str.length
        if (pads <= 0) {
            return str // returns original String when possible
        }
        return if (pads > PAD_LIMIT) {
            rightPad(str, size, padChar.toString())
        } else str + padding(pads, padChar)
    }

    private fun rightPad(str: String?, size: Int, mpadStr: String): String? {
        var padStr = mpadStr
        if (str == null) {
            return null
        }
        if (isEmpty(padStr)) {
            padStr = " "
        }
        val padLen = padStr.length
        val strLen = str.length
        val pads = size - strLen
        if (pads <= 0) {
            return str // returns original String when possible
        }
        if (padLen == 1 && pads <= PAD_LIMIT) {
            return rightPad(str, size, padStr[0])
        }
        return when {
            pads == padLen -> {
                str + padStr
            }
            pads < padLen -> {
                str + padStr.substring(0, pads)
            }
            else -> {
                val padding = CharArray(pads)
                val padChars = padStr.toCharArray()
                for (i in 0 until pads) {
                    padding[i] = padChars[i % padLen]
                }
                str + String(padding)
            }
        }
    }

    @JvmOverloads
    fun leftPad(str: String?, size: Int, padChar: Char = ' '): String? {
        if (str == null) {
            return null
        }
        val pads = size - str.length
        if (pads <= 0) {
            return str // returns original String when possible
        }
        return if (pads > PAD_LIMIT) {
            leftPad(str, size, padChar.toString())
        } else padding(pads, padChar) + str
    }


    private fun leftPad(str: String?, size: Int, mpadStr: String): String? {
        var padStr = mpadStr
        if (str == null) {
            return null
        }
        if (isEmpty(padStr)) {
            padStr = " "
        }
        val padLen = padStr.length
        val strLen = str.length
        val pads = size - strLen
        if (pads <= 0) {
            return str // returns original String when possible
        }
        if (padLen == 1 && pads <= PAD_LIMIT) {
            return leftPad(str, size, padStr[0])
        }
        return when {
            pads == padLen -> {
                padStr + str
            }
            pads < padLen -> {
                padStr.substring(0, pads) + str
            }
            else -> {
                val padding = CharArray(pads)
                val padChars = padStr.toCharArray()
                for (i in 0 until pads) {
                    padding[i] = padChars[i % padLen]
                }
                String(padding) + str
            }
        }
    }

    @JvmOverloads
    fun center(mstr: String?, size: Int, mpadStr: String = " "): String? {
        var str = mstr
        var padStr = mpadStr
        if (str == null || size <= 0) {
            return str
        }
        if (isEmpty(padStr)) {
            padStr = " "
        }
        val strLen = str.length
        val pads = size - strLen
        if (pads <= 0) {
            return str
        }
        str = leftPad(str, strLen + pads / 2, padStr)
        str = rightPad(str, size, padStr)
        return str
    }

    fun limit(str: String?, size: Int): String? {
        return if (str == null || str.length < size) {
            str
        } else str.substring(0, size)
    }

    fun capitalize(s: String): String {
        return if (s.isEmpty()) s else s.substring(0, 1).uppercase(Locale.getDefault()) + s.substring(1)
            .lowercase(Locale.getDefault())
    }

    private fun wordwrap(word: String, max: Int): String{
        val arrWords = word.split(" ")
        var rtn = ""
        var tmp = ""
        arrWords.forEachIndexed { index, s ->
            if (s == "\n"){
                rtn += "\n"
                tmp = ""
            } else if (index+1 < arrWords.size && (tmp.length + arrWords[index+1].length) >= max) {
                rtn += "$s\n"
                tmp = ""
            } else {
                rtn += "$s "
                tmp += "$s "
            }
        }
        if (rtn != ""){
            rtn = rtn.substring(0, rtn.length-1)
        }
        return rtn
    }

    fun html2String(s: String, max: Int): String {
        var word = s.replace("\n","")
        if (word.indexOf("&")>=0 && word.indexOf(";")>=0) {
            word = word.replace("&nbsp;"," ", true)
                .replace("&ensp;",repeat(" ", 2)!!, true)
                .replace("&emsp;",repeat(" ", 4)!!, true)
                .replace("&quot;","\"", true)
                .replace("&amp;","&", true)
                .replace("&lt;","<", true)
                .replace("&gt;",">", true)
                .replace("&circ;","ˆ", true)
                .replace("&apos;","'", true)
                .replace("&tilde;","˜", true)
                .replace("&lsquo;","‘", true)
                .replace("&rsquo;","’", true)
                .replace("&ldquo;","“", true)
                .replace("&rdquo;","”", true)
                .replace("&permil;","‰", true)
                .replace("&euro;","€", true)
        }

        val arr = word.split("<br>")
        var result = ""
        arr.forEach {
            if (result != "") result += "\n"
            result += wordwrap(it, max)
        }
        return result
    }

    fun bytes2HexString(bytes: ByteArray?, isUpperCase: Boolean = true): String {
        if (bytes == null) return ""
        val hexDigits: CharArray = charArrayOf(
            '0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'
        )
        val len = bytes.size
        if (len <= 0) return ""
        val ret = CharArray(len shl 1)
        var i = 0
        var j = 0
        while (i < len) {
            ret[j++] = hexDigits[bytes[i].toInt() shr 4 and 0x0f]
            ret[j++] = hexDigits[bytes[i].toInt() and 0x0f]
            i++
        }
        return if (isUpperCase) String(ret).uppercase() else String(ret).lowercase()
    }
    fun removeDiacriticalMarks(string: String?): String {
        return Normalizer.normalize(string, Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
    }
}