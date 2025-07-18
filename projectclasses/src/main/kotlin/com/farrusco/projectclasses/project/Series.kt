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

package com.farrusco.projectclasses.project

import android.graphics.Paint
import android.util.SparseArray

class Series {
    private var titleX: String? = null
    private var titleY: String? = null
    private val arrLabelx: SparseArray<Graphaxes> = SparseArray()
    private val arrLabely: SparseArray<Graphaxes> = SparseArray()
    private lateinit var amounts: Array<FloatArray>
    private var bInitAmount: Boolean
    private var maxXValue = 0f
    private var maxXStep = 0f

    private inner class Graphaxes {
        var label: String? = null
        var linecolor = 0
        var linealpha = 0
        var linestyle: Paint.Style? = null
        var linestrokewidth = 0f
    }

    fun setMaxX(maxXValue: Float, stepXValue: Float) {
        this.maxXValue = maxXValue
        maxXStep = stepXValue
    }

    fun addPos(
        xas: Boolean, description: String?, color: Int, aplha: Int,
        style: Paint.Style?, strokewidth: Float
    ) {
        bInitAmount = false
        val ga = Graphaxes()
        ga.label = description
        ga.linecolor = color
        ga.linealpha = aplha
        ga.linestyle = style
        ga.linestrokewidth = strokewidth
        if (xas) {
            arrLabelx.append(arrLabelx.size(), ga)
        } else {
            arrLabely.append(arrLabely.size(), ga)
        }
    }

    fun setTitle(xas: Boolean, title: String?) {
        if (xas) titleX = title else titleY = title
    }

    fun getTitle(xas: Boolean): String? {
        return if (xas) titleX else titleY
    }

    private fun initAmount() {
        bInitAmount = true
        amounts = Array(getMax(true)) { FloatArray(getMax(false)) }
    }

    private fun addAmount(posx: Int, posy: Int, amount: Float) {
        if (!bInitAmount) initAmount()
        amounts[posx][posy] += amount
    }

    fun setAmount(posx: Int, posy: Int, amount: Float) {
        if (!bInitAmount) initAmount()
        amounts[posx][posy] = amount
    }

    private fun getMax(xas: Boolean): Int {
        return if (xas) arrLabelx.size() else arrLabely.size()
    }

    fun getAlpha(xas: Boolean, pos: Int): Int {
        return if (xas) arrLabelx[pos].linealpha else arrLabely[pos].linealpha
    }

    fun getStrokeWidth(xas: Boolean, pos: Int): Float {
        return if (xas) arrLabelx[pos].linestrokewidth else arrLabely[pos].linestrokewidth
    }

    fun getDescription(xas: Boolean, pos: Int): String? {
        return if (xas) arrLabelx[pos].label else arrLabely[pos].label
    }

    fun getColor(xas: Boolean, pos: Int): Int {
        return if (xas) arrLabelx[pos].linecolor else arrLabely[pos].linecolor
    }

    fun getPaintStyle(xas: Boolean, pos: Int): Paint.Style? {
        return if (xas) arrLabelx[pos].linestyle else arrLabely[pos].linestyle
    }

    fun getAmount(posx: Int, posy: Int): Float {
        return amounts[posx][posy]
    }

    fun getAmountMax(xas: Boolean, pos: Int): Float {
        var rtn = 0f
        if (xas) {
            for (y in 0 until arrLabely.size()) {
                if (amounts[pos][y] > rtn) rtn = amounts[pos][y]
            }
        } else {
            for (x in 0 until arrLabelx.size()) {
                if (amounts[x][pos] > rtn) rtn = amounts[x][pos]
            }
        }
        return rtn
    }

    fun makeTestdata() {
        // test data
        initAmount()
        val mx = getMax(true)
        for (x in 0 until mx) {
            val my = getMax(false)
            for (y in 0 until my) {
                addAmount(x, y, (x + y).toFloat())
            }
        }
    }

    init {
        bInitAmount = false
    }
}