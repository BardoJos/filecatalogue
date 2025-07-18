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

package com.farrusco.projectclasses.graphics

import android.graphics.Paint
import android.util.SparseArray
import androidx.core.util.size

class Series {
    private var titleX: String = ""
    private var titleY: String = ""
    private val arrLabelx: SparseArray< Graphaxes>
    private val arrLabely: SparseArray< Graphaxes>
    private lateinit var amounts: Array<FloatArray>
    private var bInitAmount: Boolean
    private var maxXValue = 0f
    private var stepXValue = 0f

    private inner class Graphaxes {
        var label: String? = null
        var lineColor = 0
        var lineAlpha = 0
        var lineStyle: Paint.Style? = null
        var lineStrokewidth = 0f
        var code = 0
    }

    fun setMaxX(maxXValue: Float, stepXValue: Float) {
        this.maxXValue = maxXValue
        this.stepXValue = stepXValue
    }

    fun getMaxXValue(): Float {
        return maxXValue
    }

    fun getMaxXStep(): Float {
        return stepXValue
    }

    fun addPos(
        xas: Boolean, description: String, color: Int, aplha: Int,
        style: Paint.Style, strokewidth: Float, code: Int
    ) {
        bInitAmount = false
        val ga  =  Graphaxes()
        ga.label = description
        ga.lineColor = color
        ga.lineAlpha = aplha
        ga.lineStyle = style
        ga.lineStrokewidth = strokewidth
        ga.code = code
        if (xas) {
            arrLabelx.append(arrLabelx.size, ga)
        } else {
            arrLabely.append(arrLabely.size, ga)
        }
    }

    fun setTitle(xas: Boolean, title: String) {
        if (xas) titleX = title else titleY = title
    }

    fun getTitle(xas: Boolean): String {
        if (xas){
            return titleX
        }
        return titleY
    }

    private fun initAmount() {
        bInitAmount = true
        amounts = Array(getMax(true)) { FloatArray(getMax(false)) }
    }

    private fun addAmount(posX: Int, posY: Int, amount: Float) {
        if (!bInitAmount) initAmount()
        amounts[posX][posY] += amount
    }

    fun setAmount(posx: Int, posy: Int, amount: Float) {
        if (!bInitAmount) initAmount()
        amounts[posx][posy] = amount
    }

    fun getMax(xas: Boolean): Int {
        if (xas){
            for (i in 0 until arrLabelx.size){
                if (arrLabelx[i].equals(0)) return i
            }
            return arrLabelx.size
        }
        for (i in 0 until arrLabely.size){
            if (arrLabely[i].equals(0)) return i
        }
        return arrLabely.size
    }

    fun getAlpha(xas: Boolean, pos: Int): Int {
        return if (xas) arrLabelx[pos].lineAlpha else arrLabely[pos].lineAlpha
    }

    fun getStrokeWidth(xas: Boolean, pos: Int): Float {
        return if (xas) arrLabelx[pos].lineStrokewidth else arrLabely[pos].lineStrokewidth
    }

    fun getDescription(xas: Boolean, pos: Int): String? {
        return if (xas) arrLabelx[pos].label else arrLabely[pos].label
    }

    fun getCode(xas: Boolean, pos: Int): Int {
        return if (xas) arrLabelx[pos].code else arrLabely[pos].code
    }

    fun getColor(xas: Boolean, pos: Int): Int {
        return if (xas) arrLabelx[pos].lineColor else arrLabely[pos].lineColor
    }

    fun getPaintStyle(xas: Boolean, pos: Int): Paint.Style? {
        return if (xas) arrLabelx[pos].lineStyle else arrLabely[pos].lineStyle
    }

    fun getAmount(posx: Int, posy: Int): Float {
        return amounts[posx][posy]
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

    fun initArray(xas: Boolean)  {
        if (xas) {
            arrLabelx.clear()
        } else{
            arrLabely.clear()
        }
    }

    init {
        arrLabelx = SparseArray< Graphaxes>()
        arrLabely = SparseArray< Graphaxes>()
        bInitAmount = false
    }
}