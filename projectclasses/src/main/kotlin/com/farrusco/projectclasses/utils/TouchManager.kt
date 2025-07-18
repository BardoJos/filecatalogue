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
import com.farrusco.projectclasses.graphics.Vector2D

class TouchManager(private val maxNumberOfTouchPoints: Int) {
    private var points = arrayOf(Vector2D(),Vector2D())
    private var previousPoints = arrayOf(Vector2D(),Vector2D())
    private fun isPressed(index: Int): Boolean {
        return !points[index].init
    }

    fun moveDelta2D(index: Int): Vector2D {
        if (isPressed(index)) {
            val previous =
                if (!previousPoints[index].init) previousPoints[index] else points[index]
            return Vector2D.subtract(points[index], previous)
        }
        return Vector2D()
    }

    fun moveDelta2D(): Vector2D {
        val allPreviousPoints: Array<Vector2D?> = arrayOfNulls(
            maxNumberOfTouchPoints
        )
        val result: Vector2D
        // Get the collection of all the previousPoints
        for (i in 0 until maxNumberOfTouchPoints){
            if (isPressed(i)) {
                allPreviousPoints[i] =
                    if (!previousPoints[i].init) previousPoints[i] else points[i]
            }
        }
        val currentPoint: Vector2D
        val previousPoint: Vector2D

        // Get the averaged previousPoint
        var totalX = 0f
        var totalY  = 0f
        var n  = 0f
        for (i in 0 until maxNumberOfTouchPoints) if (isPressed(i)) {
            totalX += allPreviousPoints[i]!!.x
            totalY += allPreviousPoints[i]!!.y
            n++
        }
        previousPoint = Vector2D().setX((totalX / n), (totalY / n))

        // Get the averaged currentPoint
        totalX = 0f
        totalY = 0f
        n = 0f
        for (i in 0 until maxNumberOfTouchPoints) if (isPressed(i)) {
            totalX += points[i].x
            totalY += points[i].y
            n++
        }
        currentPoint = Vector2D().setX((totalX / n) , (totalY / n))
        result = Vector2D.subtract(currentPoint, previousPoint)
        return result
    }

    fun getPoint2D(index: Int): Vector2D {
        if (!points[index].init) {
            return points[index]
        }
        return Vector2D()
    }

    fun getPreviousPoint2D(index: Int): Vector2D {
        if (!previousPoints[index].init) {
            return previousPoints[index]
        }
        return Vector2D()
    }

    fun getVector2D(indexA: Int, indexB: Int): Vector2D {
        return Companion.getVector2D(points[indexA], points[indexB])
    }

    fun getPreviousVector2D(indexA: Int, indexB: Int): Vector2D {
        return if (previousPoints[indexA].init || previousPoints[indexB].init)
            Companion.getVector2D(
            points[indexA],
            points[indexB]
        ) else Companion.getVector2D(
            previousPoints[indexA],
            previousPoints[indexB]
        )
    }

    fun update(event: MotionEvent) {
        val actionCode = event.action and MotionEvent.ACTION_MASK
        if (actionCode == MotionEvent.ACTION_POINTER_UP
            || actionCode == MotionEvent.ACTION_UP
        ) {
            val index = event.action shr MotionEvent.ACTION_POINTER_INDEX_SHIFT
            // init here after has value of true then circle disappears
            points[index].new()
            previousPoints[index].new()

        } else {
            for (i in 0 until maxNumberOfTouchPoints) {
                var pointerCount = event.pointerCount
                if (pointerCount>2) pointerCount=2

                if (i < pointerCount) {
                    val index = event.getPointerId(i)
                    val newPoint = Vector2D().setX(
                        event.getX(i),
                        event.getY(i)
                    )
                    if (points[index].init){
                        points[index].setX(newPoint)
                    }else {
                        if (previousPoints[index].init) {
                            previousPoints[index].setX(Vector2D().setX(newPoint))
                        } else {
                            previousPoints[index].setX(points[index])
                        }

                        // if (Vector2D.subtract(points[index],
                        // newPoint).getLength() < 64)
                        if (Vector2D.subtract(points[index], newPoint)
                                .getLength() < 1000
                        ) points[index].setX(newPoint)
                    }
                } else {
                    points[i] = getVector2D(0,0)
                    previousPoints[i].setX( points[i])
                }
            }
        }
    }

    companion object {
        private fun getVector2D(a: Vector2D?, b: Vector2D?): Vector2D {
            if (a == null || b == null) throw RuntimeException("can't do this on nulls")
            return Vector2D.subtract(b, a)
        }
    }

    init {
        points = arrayOf(Vector2D(),Vector2D())
        previousPoints = arrayOf(Vector2D(),Vector2D())
    }
}