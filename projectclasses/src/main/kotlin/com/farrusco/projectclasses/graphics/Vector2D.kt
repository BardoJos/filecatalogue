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

import kotlin.math.atan2
import kotlin.math.sqrt

open class Vector2D {
    var init = true
    var x:Float = 0.0f
    var y:Float = 0.0f

    @Suppress("ConvertSecondaryConstructorToPrimary")
    constructor() {
        init = true
        x = 0.0f
        y = 0.0f
    }

    fun getLength(): Float {
        return sqrt(x * x + y * y)
    }

    fun new(): Vector2D {
        init=true
        x = 0f
        y = 0f
        return this
    }

    fun setX(other: Vector2D): Vector2D {
        init=other.init
        x = other.x
        y = other.y
        return this
    }

    fun setX(xp: Float, yp: Float): Vector2D {
        init=false
        this.x = xp
        this.y = yp
        return this
    }

    fun add(value: Vector2D): Vector2D {
        init=false
        x += value.x
        y += value.y
        return this
    }

    override fun toString(): String {
        return String.format( "(%.4d, %.4d)", x, y)
    }

    companion object {
        fun subtract(lhs: Vector2D, rhs: Vector2D): Vector2D {
            val v = Vector2D()
            v.setX(lhs.x - rhs.x, lhs.y - rhs.y)
            return v
        }

        fun getDistance(lhs: Vector2D, rhs: Vector2D): Float {
            val delta = subtract(lhs, rhs)
            return delta.getLength()
        }

        fun getSignedAngleBetween(a: Vector2D, b: Vector2D): Float {
            val na = getNormalized(a)
            val nb = getNormalized(b)

            return (atan2(nb.y, nb.x) - atan2(na.y, na.x))
        }

        private fun getNormalized(v: Vector2D): Vector2D {
            val l = v.getLength()
            if (l == 0.0f) return Vector2D()
            return v.setX(v.x / l, v.y / l)
        }
    }
}