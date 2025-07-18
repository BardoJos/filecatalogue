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

import android.view.MotionEvent

open class WrapMotionEvent (var event: MotionEvent) {
    val action: Int
        get() = event.action
    val x: Float
        get() = event.x

    open fun getX(pointerIndex: Int): Float {
        verifyPointerIndex(pointerIndex)
        return x
    }

    val y: Float
        get() = event.y

    open fun getY(pointerIndex: Int): Float {
        verifyPointerIndex(pointerIndex)
        return y
    }

    open val pointerCount: Int = 1

    open fun getPointerId(pointerIndex: Int): Int {
        verifyPointerIndex(pointerIndex)
        return 0
    }

    private fun verifyPointerIndex(pointerIndex: Int) {
        require(pointerIndex <= 0) { "Invalid pointer index for Donut/Cupcake" }
    }

    companion object {
        @Suppress("unused")
        fun wrap(event: MotionEvent): WrapMotionEvent {
            return try {
                EclairMotionEvent(event)
            } catch (e: VerifyError) {
                WrapMotionEvent(event)
            }
        }
    }
}