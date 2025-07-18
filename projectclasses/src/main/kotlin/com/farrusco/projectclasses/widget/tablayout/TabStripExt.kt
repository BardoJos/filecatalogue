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
package com.farrusco.projectclasses.widget.tablayout

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.LinearLayout
import com.farrusco.projectclasses.R
import com.farrusco.projectclasses.widget.tablayout.TabLayoutExt.TabColorizer
import com.farrusco.projectclasses.widget.tablayout.TabUtilsExt.getEnd
import com.farrusco.projectclasses.widget.tablayout.TabUtilsExt.getMarginEnd
import com.farrusco.projectclasses.widget.tablayout.TabUtilsExt.getStart
import com.farrusco.projectclasses.widget.tablayout.TabUtilsExt.isLayoutRtl
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 *
 *
 * Forked from Google Samples &gt; SlidingTabsBasic &gt;
 * [SlidingTabStrip](https://developer.android.com/samples/SlidingTabsBasic/src/com.example.android.common/view/SlidingTabLayout.html)
 */
class TabStripExt(context: Context, attrs: AttributeSet?) : LinearLayout(context) {
    private val topBorderThickness: Int
    private val topBorderColor: Int
    private val bottomBorderThickness: Int
    private val bottomBorderColor: Int
    private val borderPaint: Paint
    private val indicatorRectF = RectF()
    private val indicatorWithoutPadding: Boolean
    val isIndicatorAlwaysInCenter: Boolean
    private val indicatorInFront: Boolean
    private val indicatorThickness: Int
    private val indicatorWidth: Int
    private val indicatorGravity: Int
    private val indicatorCornerRadius: Float
    private val indicatorPaint: Paint
    private val dividerThickness: Int
    private val dividerPaint: Paint
    private val dividerHeight: Float
    private val defaultTabColorizer: SimpleTabColorizer
    private val drawDecorationAfterTab: Boolean
    private var lastPosition = 0
    private var selectedPosition = 0
    private var selectionOffset = 0f
    private var indicationInterpolator: TabIndicationInterpolatorExt
    private var customTabColorizer: TabColorizer? = null
    private var tabMode = 0

    init {
        setWillNotDraw(false)
        val density = resources.displayMetrics.density
        val outValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.colorForeground, outValue, true)
        val themeForegroundColor = outValue.data
        var indicatorWithoutPadding = DEFAULT_INDICATOR_WITHOUT_PADDING
        var indicatorInFront = DEFAULT_INDICATOR_IN_FRONT
        var indicatorAlwaysInCenter = DEFAULT_INDICATOR_IN_CENTER
        var indicationInterpolatorId = TabIndicationInterpolatorExt.ID_SMART
        var indicatorGravity = DEFAULT_INDICATOR_GRAVITY
        var tabMode = DEFAULT_TAB_MODE
        var indicatorColor = DEFAULT_SELECTED_INDICATOR_COLOR
        var indicatorColorsId = NO_ID
        var indicatorThickness = (SELECTED_INDICATOR_THICKNESS_DIPS * density).toInt()
        var indicatorWidth = AUTO_WIDTH
        var indicatorCornerRadius = DEFAULT_INDICATOR_CORNER_RADIUS * density
        var overlineColor = setColorAlpha(themeForegroundColor, DEFAULT_TOP_BORDER_COLOR_ALPHA)
        var overlineThickness = (DEFAULT_TOP_BORDER_THICKNESS_DIPS * density).toInt()
        var underlineColor = setColorAlpha(themeForegroundColor, DEFAULT_BOTTOM_BORDER_COLOR_ALPHA)
        var underlineThickness = (DEFAULT_BOTTOM_BORDER_THICKNESS_DIPS * density).toInt()
        var dividerColor = setColorAlpha(themeForegroundColor, DEFAULT_DIVIDER_COLOR_ALPHA)
        var dividerColorsId = NO_ID
        var dividerThickness = (DEFAULT_DIVIDER_THICKNESS_DIPS * density).toInt()
        var drawDecorationAfterTab = DEFAULT_DRAW_DECORATION_AFTER_TAB
        val a = context.obtainStyledAttributes(attrs, R.styleable.TabLayoutExt)
        indicatorAlwaysInCenter = a.getBoolean(
            R.styleable.TabLayoutExt_xtlIndicatorAlwaysInCenter, indicatorAlwaysInCenter
        )
        indicatorWithoutPadding = a.getBoolean(
            R.styleable.TabLayoutExt_xtlIndicatorWithoutPadding, indicatorWithoutPadding
        )
        indicatorInFront = a.getBoolean(
            R.styleable.TabLayoutExt_xtlIndicatorInFront, indicatorInFront
        )
        indicationInterpolatorId = a.getInt(
            R.styleable.TabLayoutExt_xtlIndicatorInterpolation, indicationInterpolatorId
        )
        indicatorGravity = a.getInt(
            R.styleable.TabLayoutExt_xtlIndicatorGravity, indicatorGravity
        )
        tabMode = a.getInt(
            R.styleable.TabLayoutExt_xtlTabMode, tabMode
        )
        indicatorColor = a.getColor(
            R.styleable.TabLayoutExt_xtlIndicatorColor, indicatorColor
        )
        indicatorColorsId = a.getResourceId(
            R.styleable.TabLayoutExt_xtlIndicatorColors, indicatorColorsId
        )
        indicatorThickness = a.getDimensionPixelSize(
            R.styleable.TabLayoutExt_xtlIndicatorThickness, indicatorThickness
        )
        indicatorWidth = a.getLayoutDimension(
            R.styleable.TabLayoutExt_xtlIndicatorWidth, indicatorWidth
        )
        indicatorCornerRadius = a.getDimension(
            R.styleable.TabLayoutExt_xtlIndicatorCornerRadius, indicatorCornerRadius
        )
        overlineColor = a.getColor(
            R.styleable.TabLayoutExt_xtlOverlineColor, overlineColor
        )
        overlineThickness = a.getDimensionPixelSize(
            R.styleable.TabLayoutExt_xtlOverlineThickness, overlineThickness
        )
        underlineColor = a.getColor(
            R.styleable.TabLayoutExt_xtlUnderlineColor, underlineColor
        )
        underlineThickness = a.getDimensionPixelSize(
            R.styleable.TabLayoutExt_xtlUnderlineThickness, underlineThickness
        )
        dividerColor = a.getColor(
            R.styleable.TabLayoutExt_xtlDividerColor, dividerColor
        )
        dividerColorsId = a.getResourceId(
            R.styleable.TabLayoutExt_xtlDividerColors, dividerColorsId
        )
        dividerThickness = a.getDimensionPixelSize(
            R.styleable.TabLayoutExt_xtlDividerThickness, dividerThickness
        )
        drawDecorationAfterTab = a.getBoolean(
            R.styleable.TabLayoutExt_xtlDrawDecorationAfterTab, drawDecorationAfterTab
        )
        a.recycle()
        val indicatorColors =
            if (indicatorColorsId == NO_ID) intArrayOf(indicatorColor) else resources.getIntArray(
                indicatorColorsId
            )
        val dividerColors =
            if (dividerColorsId == NO_ID) intArrayOf(dividerColor) else resources.getIntArray(
                dividerColorsId
            )
        defaultTabColorizer = SimpleTabColorizer()
        defaultTabColorizer.indicatorColors = indicatorColors
        defaultTabColorizer.dividerColors=dividerColors
        topBorderThickness = overlineThickness
        topBorderColor = overlineColor
        bottomBorderThickness = underlineThickness
        bottomBorderColor = underlineColor
        borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        isIndicatorAlwaysInCenter = indicatorAlwaysInCenter
        this.indicatorWithoutPadding = indicatorWithoutPadding
        this.indicatorInFront = indicatorInFront
        this.indicatorThickness = indicatorThickness
        this.indicatorWidth = indicatorWidth
        indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        this.indicatorCornerRadius = indicatorCornerRadius
        this.indicatorGravity = indicatorGravity
        this.tabMode = tabMode
        dividerHeight = DEFAULT_DIVIDER_HEIGHT
        dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        dividerPaint.strokeWidth = dividerThickness.toFloat()
        this.dividerThickness = dividerThickness
        this.drawDecorationAfterTab = drawDecorationAfterTab
        indicationInterpolator = TabIndicationInterpolatorExt.of(indicationInterpolatorId)

    }

    fun setIndicationInterpolator(interpolator: TabIndicationInterpolatorExt) {
        indicationInterpolator = interpolator
        invalidate()
    }

    fun setCustomTabColorizer(customTabColorizer: TabColorizer?) {
        this.customTabColorizer = customTabColorizer
        invalidate()
    }

    fun setSelectedIndicatorColors(vararg colors: Int) {
        // Make sure that the custom colorizer is removed
        customTabColorizer = null
        defaultTabColorizer.indicatorColors=colors
        invalidate()
    }

    fun setDividerColors(vararg colors: Int) {
        // Make sure that the custom colorizer is removed
        customTabColorizer = null
        defaultTabColorizer.dividerColors=colors
        invalidate()
    }

    fun onViewPagerPageChanged(position: Int, positionOffset: Float) {
        selectedPosition = position
        selectionOffset = positionOffset
        if (positionOffset == 0f && lastPosition != selectedPosition) {
            lastPosition = selectedPosition
        }
        invalidate()
    }

    private val tabColorizer: TabColorizer
        get() = if (customTabColorizer != null) customTabColorizer!! else defaultTabColorizer

    override fun onDraw(canvas: Canvas) {
        if (!drawDecorationAfterTab) {
            drawDecoration(canvas)
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        if (drawDecorationAfterTab) {
            drawDecoration(canvas)
        }
    }

    private fun drawDecoration(canvas: Canvas) {
        val height = height
        val width = width
        val tabCount = childCount
        val tabColorizer = tabColorizer
        val isLayoutRtl = isLayoutRtl(this)
        if (indicatorInFront) {
            drawOverline(canvas, 0, width)
            drawUnderline(canvas, 0, width, height)
        }

        // Thick colored underline below the current selection
        if (tabCount > 0) {
            val selectedTab = getChildAt(selectedPosition)
            val selectedStart = getStart(selectedTab, indicatorWithoutPadding)
            val selectedEnd = getEnd(selectedTab, indicatorWithoutPadding)
            var left: Int
            var right: Int
            if (isLayoutRtl) {
                left = selectedEnd
                right = selectedStart
            } else {
                left = selectedStart
                right = selectedEnd
            }
            var color = tabColorizer.getIndicatorColor(selectedPosition)
            var thickness = indicatorThickness.toFloat()
            if (selectionOffset > 0f && selectedPosition < childCount - 1) {
                val nextColor = tabColorizer.getIndicatorColor(selectedPosition + 1)
                if (color != nextColor) {
                    color = blendColors(nextColor, color, selectionOffset)
                }

                // Draw the selection partway between the tabs
                val startOffset = indicationInterpolator.getLeftEdge(selectionOffset)
                val endOffset = indicationInterpolator.getRightEdge(selectionOffset)
                val thicknessOffset = indicationInterpolator.getThickness(selectionOffset)
                val nextTab = getChildAt(selectedPosition + 1)
                val nextStart = getStart(nextTab, indicatorWithoutPadding)
                val nextEnd = getEnd(nextTab, indicatorWithoutPadding)
                if (isLayoutRtl) {
                    left = (endOffset * nextEnd + (1.0f - endOffset) * left).toInt()
                    right = (startOffset * nextStart + (1.0f - startOffset) * right).toInt()
                } else {
                    left = (startOffset * nextStart + (1.0f - startOffset) * left).toInt()
                    right = (endOffset * nextEnd + (1.0f - endOffset) * right).toInt()
                }
                thickness *= thicknessOffset
            }
            drawIndicator(canvas, left, right, height, thickness, color)
        }
        if (!indicatorInFront) {
            drawOverline(canvas, 0, width)
            drawUnderline(canvas, 0, getWidth(), height)
        }

        // Vertical separators between the titles
        drawSeparator(canvas, height, tabCount)
    }

    private fun drawSeparator(canvas: Canvas, height: Int, tabCount: Int) {
        if (dividerThickness <= 0) {
            return
        }
        val dividerHeightPx = (min(max(0.0, dividerHeight.toDouble()), 1.0) * height).toInt()
        val tabColorizer = tabColorizer

        // Vertical separators between the titles
        val separatorTop = (height - dividerHeightPx) / 2
        val separatorBottom = separatorTop + dividerHeightPx
        val isLayoutRtl = isLayoutRtl(this)
        for (i in 0 until tabCount - 1) {
            val child = getChildAt(i)
            val end = getEnd(child)
            val endMargin = getMarginEnd(child)
            val separatorX = if (isLayoutRtl) end - endMargin else end + endMargin
            dividerPaint.color = tabColorizer.getDividerColor(i)
            canvas.drawLine(
                separatorX.toFloat(),
                separatorTop.toFloat(),
                separatorX.toFloat(),
                separatorBottom.toFloat(),
                dividerPaint
            )
        }
    }

    private fun drawIndicator(
        canvas: Canvas, left: Int, right: Int, height: Int, thickness: Float,
        color: Int
    ) {
        if (indicatorThickness <= 0 || indicatorWidth == 0) {
            return
        }
        val center: Float
        val top: Float
        val bottom: Float
        when (indicatorGravity) {
            GRAVITY_TOP -> {
                center = indicatorThickness / 2f
                top = center - thickness / 2f
                bottom = center + thickness / 2f
            }

            GRAVITY_CENTER -> {
                center = height / 2f
                top = center - thickness / 2f
                bottom = center + thickness / 2f
            }

            GRAVITY_BOTTOM -> {
                center = height - indicatorThickness / 2f
                top = center - thickness / 2f
                bottom = center + thickness / 2f
            }

            else -> {
                center = height - indicatorThickness / 2f
                top = center - thickness / 2f
                bottom = center + thickness / 2f
            }
        }
        indicatorPaint.color = color
        if (indicatorWidth == AUTO_WIDTH) {
            indicatorRectF[left.toFloat(), top, right.toFloat()] = bottom
        } else {
            val padding = ((abs((left - right).toDouble()) - indicatorWidth) / 2f).toFloat()
            indicatorRectF[left + padding, top, right - padding] = bottom
        }
        if (indicatorCornerRadius > 0f) {
            canvas.drawRoundRect(
                indicatorRectF, indicatorCornerRadius,
                indicatorCornerRadius, indicatorPaint
            )
        } else {
            canvas.drawRect(indicatorRectF, indicatorPaint)
        }
    }

    private fun drawOverline(canvas: Canvas, left: Int, right: Int) {
        if (topBorderThickness <= 0) {
            return
        }
        // Thin overline along the entire top edge
        borderPaint.color = topBorderColor
        canvas.drawRect(
            left.toFloat(),
            0f,
            right.toFloat(),
            topBorderThickness.toFloat(),
            borderPaint
        )
    }

    private fun drawUnderline(canvas: Canvas, left: Int, right: Int, height: Int) {
        if (bottomBorderThickness <= 0) {
            return
        }
        // Thin underline along the entire bottom edge
        borderPaint.color = bottomBorderColor
        canvas.drawRect(
            left.toFloat(),
            (height - bottomBorderThickness).toFloat(),
            right.toFloat(),
            height.toFloat(),
            borderPaint
        )
    }

    private class SimpleTabColorizer : TabColorizer {
        lateinit var indicatorColors: IntArray
        lateinit var dividerColors: IntArray
        override fun getIndicatorColor(position: Int): Int {
            return indicatorColors[position % indicatorColors.size]
        }

        override fun getDividerColor(position: Int): Int {
            return dividerColors[position % dividerColors.size]
        }

/*        fun setIndicatorColors(vararg colors: Int) {
            indicatorColors = colors
        }

        fun setDividerColors(vararg colors: Int) {
            dividerColors = colors
        }*/
    }

    companion object {
        private const val GRAVITY_BOTTOM = 0
        private const val GRAVITY_TOP = 1
        private const val GRAVITY_CENTER = 2
        private const val AUTO_WIDTH = -1
        private const val DEFAULT_TOP_BORDER_THICKNESS_DIPS = 0
        private const val DEFAULT_TOP_BORDER_COLOR_ALPHA: Byte = 0x26
        private const val DEFAULT_BOTTOM_BORDER_THICKNESS_DIPS = 2
        private const val DEFAULT_BOTTOM_BORDER_COLOR_ALPHA: Byte = 0x26
        private const val SELECTED_INDICATOR_THICKNESS_DIPS = 8
        private const val DEFAULT_SELECTED_INDICATOR_COLOR = -0xcc4a1b
        private const val DEFAULT_INDICATOR_CORNER_RADIUS = 0f
        private const val DEFAULT_DIVIDER_THICKNESS_DIPS = 1
        private const val DEFAULT_DIVIDER_COLOR_ALPHA: Byte = 0x20
        private const val DEFAULT_DIVIDER_HEIGHT = 0.5f
        private const val DEFAULT_INDICATOR_IN_CENTER = false
        private const val DEFAULT_INDICATOR_IN_FRONT = false
        private const val DEFAULT_INDICATOR_WITHOUT_PADDING = false
        private const val DEFAULT_INDICATOR_GRAVITY = GRAVITY_BOTTOM
        private const val DEFAULT_DRAW_DECORATION_AFTER_TAB = false
        const val DEFAULT_TAB_MODE = 3 // 0=auto,1=fixed,2=scrollable

        /**
         * Set the alpha value of the `color` to be the given `alpha` value.
         */
        private fun setColorAlpha(color: Int, alpha: Byte): Int {
            return Color.argb(
                alpha.toInt(),
                Color.red(color),
                Color.green(color),
                Color.blue(color)
            )
        }

        /**
         * Blend `color1` and `color2` using the given ratio.
         *
         * @param ratio of which to blend. 1.0 will return `color1`, 0.5 will give an even blend,
         * 0.0 will return `color2`.
         */
        private fun blendColors(color1: Int, color2: Int, ratio: Float): Int {
            val inverseRation = 1f - ratio
            val r = Color.red(color1) * ratio + Color.red(color2) * inverseRation
            val g = Color.green(color1) * ratio + Color.green(color2) * inverseRation
            val b = Color.blue(color1) * ratio + Color.blue(color2) * inverseRation
            return Color.rgb(r.toInt(), g.toInt(), b.toInt())
        }
    }
}
