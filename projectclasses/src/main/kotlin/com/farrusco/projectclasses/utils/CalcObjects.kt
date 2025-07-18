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

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.text.DecimalFormat
import java.text.NumberFormat
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.roundToLong

@Suppress("unused")
object CalcObjects {

    fun isInteger(input: String): Boolean {
        return try {
            input.toInt()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun stringToInteger(input: String?, default: Int=0): Int {
        if (input == null) return default
        return try {
            input.toInt()
        } catch (e: Exception) {
            0
        }
    }

    fun getString(input: String?, default:String = ""): String {
        return try {
            input!!
        } catch (e: Exception) {
            default
        }
    }

    fun stringToInteger(input: String): Int {
        return try {
            input.toInt()
        } catch (e: Exception) {
            0
        }
    }

    fun objectToString(obj: Any?): String {
        if (obj == null) return ""
        if (obj.javaClass == Int::class.java) {
            return "" + objectToInteger(obj)
        } else if (obj is Double) {
            return "" + objectToDouble(obj)
        }
        return obj.toString()
    }

    fun objectToLong(obj: Any?): Long {
        if (obj == null) return 0
        return if (obj.javaClass == String::class.java) {
            Integer.valueOf(obj.toString()).toLong()
        } else obj.toString().toLong()
    }

    fun objectToFloat(obj: Any?): Float {
        if (obj == null) return 0f
        return obj.toString().toFloat()
    }

    fun objectToInteger(obj: Any?): Int {
        return try {
            if (obj == null) return 0
            if (obj.javaClass == String::class.java) {
                return stringToInteger(obj.toString())
            }
            if (obj.javaClass == Long::class.java) {
                stringToInteger(obj.toString())
            } else obj.toString().toInt()
        } catch (e: Exception) {
            0
        }
    }

    fun objectToDouble(obj: Any?): Double {
        if (obj == null) return 0.0
        var d: Double? = null
        if (obj is Double) {
            d = obj
        }
        return if (obj is String) {
            stringToDouble(obj.toString())
        } else d!!
    }

    fun formatAmount(amount: Double): String {
        val formatter: NumberFormat = DecimalFormat("#,###,##0.00")
        return formatter.format(amount)
    }

    fun formatAmount(amount: String?): String {
        return formatAmount(stringToDouble(amount))
    }

    fun roundTo(amount: Double, numFractionDigits: Int): Double {
        val factor = 10.0.pow(numFractionDigits.toDouble())
        return (amount * factor).roundToInt() / factor
    }

    fun stringToDouble(param: String?): Double {
        try {
           return param.toString().toDouble()
        } catch (_: Exception) {
        }
        return 0.0
    }

    fun decodeFile(f: File?, imageMaxSize: Int): Bitmap? {
        var b: Bitmap? = null
        try {
            if (imageMaxSize > 0) {
                // Decode image size
                val o = BitmapFactory.Options()
                o.inJustDecodeBounds = true
                BitmapFactory.decodeStream(FileInputStream(f), null, o)
                var scale = 1.0
                if (o.outHeight > imageMaxSize || o.outWidth > imageMaxSize) {
                    scale = 2.0.pow(
                        ((ln(
                            imageMaxSize / max(o.outHeight, o.outWidth)
                                .toDouble()
                        ) / ln(0.5)).roundToLong().toInt()).toDouble()
                    )
                }

                // Decode with inSampleSize
                val o2 = BitmapFactory.Options()
                o2.inSampleSize = scale.toInt()
                b = BitmapFactory.decodeStream(FileInputStream(f), null, o2)
            } else {
                b = BitmapFactory.decodeStream(FileInputStream(f), null, null)
            }
        } catch (_: FileNotFoundException) {
        }
        return b
    }

    fun decodeFile4(pf: File?, imageMaxSize: Int): Bitmap? {
        var bitmap: Bitmap?
        var newBitmap: Bitmap? = null
        val op = BitmapFactory.Options()
        op.inJustDecodeBounds = true
        try {
            BitmapFactory.decodeStream(FileInputStream(pf), null, op)
        } catch (_: FileNotFoundException) {
        }
        op.inJustDecodeBounds = false
        op.inSampleSize = 4
        op.inPreferredConfig = Bitmap.Config.ARGB_8888
        // imageID = cursor.getInt(columnIndex);
        @Suppress("LiftReturnOrAssignment")
        try {
            bitmap = BitmapFactory.decodeStream(FileInputStream(pf), null, op)
            // f = null
        } catch (e: Exception) {
            bitmap = null
        }
        try {
            if (bitmap != null) {
                // make square of picture without scaling
                val min = min(bitmap.height, bitmap.width)
                newBitmap = Bitmap.createBitmap(
                    bitmap, (bitmap.width - min) / 2, (bitmap.height - min) / 2,
                    min, min
                )
                bitmap.recycle()
                newBitmap =
                    Bitmap.createScaledBitmap(newBitmap, imageMaxSize, imageMaxSize, true)
            }
        } catch (_: Exception) {
        }
        return newBitmap
    }
}