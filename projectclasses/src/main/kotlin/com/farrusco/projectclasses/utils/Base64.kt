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

@Suppress("unused")
object Base64 {
    private val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
        .toCharArray()
    private val toInt = IntArray(128)

    fun encode(buf: ByteArray): String {
        val size = buf.size
        val ar = CharArray((size + 2) / 3 * 4)
        var a = 0
        var i = 0
        while (i < size) {
            val b0:Int = buf[i++].toInt()
            val b1:Int = if (i < size) buf[i++].toInt() else 0
            val b2:Int = if (i < size) buf[i++].toInt() else 0
            val mask = 0x3F
            ar[a++] = ALPHABET[b0 shr 2 and mask]
            ar[a++] = ALPHABET[b0 shl 4 or (b1 and 0xFF shr 4) and mask]
            ar[a++] = ALPHABET[b1 shl 2 or (b2 and 0xFF shr 6) and mask]
            ar[a++] = ALPHABET[b2 and mask]
        }
        when (size % 3) {
            1 -> {
                ar[--a] = '='
                ar[--a] = '='
            }
            2 -> ar[--a] = '='
        }
        return String(ar)
    }

    fun decode(s1: String): ByteArray {
        var s = s1
        if (s.indexOf("==") > 0 && s.length - 3 == s.indexOf("==")) {
            // remove \n
            s = s.substring(0, s.indexOf("==") + 2)
        }
        val delta = if (s.endsWith("==")) 2 else if (s.endsWith("=")) 1 else 0
        val buffer = ByteArray(s.length * 3 / 4 - delta)
        val mask = 0xFF
        var index = 0
        var i = 0
        while (i < s.length) {
            val c0 = toInt[s[i].code]
            val c1 = toInt[s[i + 1].code]
            buffer[index++] = (c0 shl 2 or (c1 shr 4) and mask).toByte()
            if (index >= buffer.size) {
                return buffer
            }
            val c2 = toInt[s[i + 2].code]
            buffer[index++] = (c1 shl 4 or (c2 shr 2) and mask).toByte()
            if (index >= buffer.size) {
                return buffer
            }
            val c3 = toInt[s[i + 3].code]
            buffer[index++] = (c2 shl 6 or c3 and mask).toByte()
            i += 4
        }
        return buffer
    }

    init {
        for (i in ALPHABET.indices) {
            toInt[ALPHABET[i].code] = i
        }
    }
}