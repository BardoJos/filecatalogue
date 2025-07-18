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
package com.farrusco.projectclasses.graphics.cropper

import android.graphics.Matrix
import android.graphics.RectF
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.Transformation
import android.widget.ImageView

/**
 * Animation to handle smooth cropping image matrix transformation change, specifically for
 * zoom-in/out.
 */
internal class CropImageAnimation(
  private val imageView: ImageView,
  private val cropOverlayView: CropOverlayView,
) : Animation(), AnimationListener {

  private val startBoundPoints = FloatArray(8)
  private val endBoundPoints = FloatArray(8)
  private val startCropWindowRect = RectF()
  private val endCropWindowRect = RectF()
  private val startImageMatrix = FloatArray(9)
  private val endImageMatrix = FloatArray(9)

  init {
    duration = 300
    fillAfter = true
    interpolator = AccelerateDecelerateInterpolator()
    setAnimationListener(this)
  }

  fun setStartState(boundPoints: FloatArray, imageMatrix: Matrix) {
    reset()
    System.arraycopy(boundPoints, 0, startBoundPoints, 0, 8)
    startCropWindowRect.set(cropOverlayView.cropWindowRect)
    imageMatrix.getValues(startImageMatrix)
  }

  fun setEndState(boundPoints: FloatArray, imageMatrix: Matrix) {
    System.arraycopy(boundPoints, 0, endBoundPoints, 0, 8)
    endCropWindowRect.set(cropOverlayView.cropWindowRect)
    imageMatrix.getValues(endImageMatrix)
  }

  override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
    val animRect = RectF().apply {
      left = (startCropWindowRect.left + (endCropWindowRect.left - startCropWindowRect.left) * interpolatedTime)
      top = (startCropWindowRect.top + (endCropWindowRect.top - startCropWindowRect.top) * interpolatedTime)
      right = (startCropWindowRect.right + (endCropWindowRect.right - startCropWindowRect.right) * interpolatedTime)
      bottom = (startCropWindowRect.bottom + (endCropWindowRect.bottom - startCropWindowRect.bottom) * interpolatedTime)
    }

    val animPoints = FloatArray(8)
    for (i in animPoints.indices) {
      animPoints[i] = (startBoundPoints[i] + (endBoundPoints[i] - startBoundPoints[i]) * interpolatedTime)
    }

    cropOverlayView.apply {
      cropWindowRect = animRect
      setBounds(animPoints, imageView.width, imageView.height)
      invalidate()
    }

    val animMatrix = FloatArray(9)
    for (i in animMatrix.indices) {
      animMatrix[i] = (startImageMatrix[i] + (endImageMatrix[i] - startImageMatrix[i]) * interpolatedTime)
    }

    imageView.apply {
      imageMatrix.setValues(animMatrix)
      invalidate()
    }
  }

  override fun onAnimationStart(animation: Animation) = Unit
  override fun onAnimationEnd(animation: Animation) {
    imageView.clearAnimation()
  }

  override fun onAnimationRepeat(animation: Animation) = Unit
}
