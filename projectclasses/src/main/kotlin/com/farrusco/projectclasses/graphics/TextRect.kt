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
package com.farrusco.projectclasses.graphics

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import com.farrusco.projectclasses.utils.Logging

class TextRect(paint: Paint) {

    // maximum number of lines; this is a fixed number in order
    // to use a predefined array to avoid ArrayList (or something
    // similar) because filling it does involve allocating memory
    private val maxLines = 256

    // those members are stored per instance to minimize
    // the number of allocations to avoid triggering the
    // GC too much
    private var metrics: Paint.FontMetricsInt? = null
    private var paint: Paint? = null
    private val starts = IntArray(maxLines)
    private val stops = IntArray(maxLines)
    private var lines = 0
    var textHeight: Int = 0
    private val bounds = Rect()
    private var text: String? = null
    private var wasCut = false
    fun prepare(
        text: String,
        maxWidth: Int,
        maxHeight: Int
    ): Int {
        lines = 0
        textHeight = 0
        this.text = text
        wasCut = false

        // get maximum number of characters in one line
        paint!!.getTextBounds(
            "i",
            0,
            1,
            bounds
        )
        val maximumInLine = maxWidth / bounds.width()
        val length = text.length
        if (length > 0) {
            val lineHeight: Int = -metrics!!.ascent + metrics!!.descent
            var start = 0
            var stop = maximumInLine.coerceAtMost(length)
            while (true) {

                // skip LF and spaces
                while (start < length) {
                    val ch = text[start]
                    if (ch != '\n' && ch != '\r' && ch != '\t' && ch != ' ') break
                    ++start
                }
                var o = stop + 1
                while (stop in ((start + 1) until o)) {
                    o = stop
                    var lowest = text.indexOf("\n", start)
                    paint!!.getTextBounds(
                        text,
                        start,
                        stop,
                        bounds
                    )
                    if (lowest in start until stop ||
                        bounds.width() > maxWidth
                    ) {
                        --stop
                        if (lowest < start ||
                            lowest > stop
                        ) {
                            val blank = text.lastIndexOf(" ", stop)
                            val hyphen = text.lastIndexOf("-", stop)
                            if (blank > start &&
                                (hyphen < start || blank > hyphen)
                            ) lowest = blank else if (hyphen > start) lowest = hyphen
                        }
                        if (lowest in start..stop
                        ) {
                            val ch = text[stop]
                            if (ch != '\n' &&
                                ch != ' '
                            ) ++lowest
                            stop = lowest
                        }
                        continue
                    }
                    break
                }
                if (start >= stop) break
                var minus = 0

                // cut off lf or space
                if (stop < length) {
                    val ch = text[stop - 1]
                    if (ch == '\n' ||
                        ch == ' '
                    ) minus = 1
                }
                if (textHeight + lineHeight > maxHeight) {
                    wasCut = true
                    break
                }
                starts[lines] = start
                stops[lines] = stop - minus
                if (++lines > maxLines) {
                    wasCut = true
                    break
                }
                if (textHeight > 0) textHeight  += metrics!!.leading
                textHeight += lineHeight
                if (stop >= length) break
                start = stop
                stop = length
            }
        }
        if (textHeight == 0) {
            Logging.d("TextRect/prepare: TextHeight is zero. Set default to $maxHeight")
            textHeight = maxHeight
        }
        return textHeight
    }

    /**
     * Draw prepared text at given position.
     *
     * @param canvas - canvas to draw text into
     * @param left - left corner
     * @param top - top corner
     */
    fun draw(
        canvas: Canvas,
        left: Int,
        top: Int
    ) {
        if (textHeight == 0) {
            Logging.d("TextRect/draw: TextHeight is zero")
            return
        }
        val before: Int = -metrics?.ascent!!
        val after: Int = metrics!!.descent + metrics!!.leading
        var y = top
        --lines
        for (n in 0..lines) {
            y += before
            val t: String = if (wasCut && n == lines && stops[n] - starts[n] > 3) text!!.substring(
                starts[n],
                stops[n] - 3
            ) + "..." else text!!.substring(
                starts[n], stops[n]
            )
            canvas.drawText(
                t,
                left.toFloat(),
                y.toFloat(),
                paint!!
            )
            y += after
        }
    }

    init {
        metrics = paint.fontMetricsInt
        this.paint = paint
    }
}