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

import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator

abstract class TabIndicationInterpolatorExt {
    abstract fun getLeftEdge(offset: Float): Float
    abstract fun getRightEdge(offset: Float): Float
    open fun getThickness(offset: Float): Float {
        return 1f //Always the same thickness by default
    }

    class IndicationInterpolatorExt @JvmOverloads constructor(factor: Float = DEFAULT_INDICATOR_INTERPOLATION_FACTOR) :
        TabIndicationInterpolatorExt() {
        private val leftEdgeInterpolator: Interpolator = AccelerateInterpolator(factor)
        private val rightEdgeInterpolator: Interpolator = DecelerateInterpolator(factor)

        override fun getLeftEdge(offset: Float): Float {
            return leftEdgeInterpolator.getInterpolation(offset)
        }

        override fun getRightEdge(offset: Float): Float {
            return rightEdgeInterpolator.getInterpolation(offset)
        }

        override fun getThickness(offset: Float): Float {
            return 1f / (1.0f - getLeftEdge(offset) + getRightEdge(offset))
        }

        companion object {
            private const val DEFAULT_INDICATOR_INTERPOLATION_FACTOR = 3.0f
        }
    }

    class LinearIndicationInterpolatorExt : TabIndicationInterpolatorExt() {
        override fun getLeftEdge(offset: Float): Float {
            return offset
        }

        override fun getRightEdge(offset: Float): Float {
            return offset
        }
    }

    companion object {
        private val SMART: TabIndicationInterpolatorExt = IndicationInterpolatorExt()
        private val LINEAR: TabIndicationInterpolatorExt = LinearIndicationInterpolatorExt()
        const val ID_SMART = 0
        private const val ID_LINEAR = 1
        fun of(id: Int): TabIndicationInterpolatorExt {
            return when (id) {
                ID_SMART -> SMART
                ID_LINEAR -> LINEAR
                else -> throw IllegalArgumentException("Unknown id: $id")
            }
        }
    }
}
