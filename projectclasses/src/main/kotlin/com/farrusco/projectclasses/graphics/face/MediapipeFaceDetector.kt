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
package com.farrusco.projectclasses.graphics.face

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import com.farrusco.projectclasses.graphics.BitmapResolver
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import org.koin.core.annotation.Single
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

// Utility class for interacting with Media pipe's Face Detector
// See https://ai.google.dev/edge/mediapipe/solutions/vision/face_detector/android
@Single
open class MediapipeFaceDetector() {

    fun procesFile( context:Context, dirname:String, filename:String): ArrayList<String>? {
        //Load model
        var listFaces: ArrayList<String>? = null
        var tfLite: Interpreter? = null
        var modelFile = "mobile_face_net.tflite"

        var bm: Bitmap? = BitmapResolver.getBitmap(context.contentResolver, "$dirname/$filename")
        if (bm == null) {
            return null
        }
        //val bm = BitmapManager.loadBitmap( "$dirname/$filename", 0)
        try {
            tfLite = Interpreter(loadModelFile(context, modelFile), null)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val impPhoto = InputImage.fromBitmap(bm, 0)
        //faceData = ""
        val highAccuracyOpts =
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .build()
        var detector = FaceDetection.getClient(highAccuracyOpts)
        detector.process(impPhoto).addOnSuccessListener { faces ->
            listFaces = procesFaces(tfLite!!, bm, faces)
        }
        return listFaces
    }

    fun procesFaces( tfLite: Interpreter, bm:Bitmap, faces: List<Face?>?): ArrayList<String>? {
        if (faces == null || faces.isEmpty()) {
            return null
        }

        val face = faces[0]

        //val bm = (imageView.drawable as BitmapDrawable).bitmap
        val boundingBox = RectF(face!!.boundingBox)
        val croppedFace =
            BitmapResolver.getCropBitmapByCPU(bm, boundingBox)
        val scaled = BitmapResolver.getResizedBitmap(croppedFace, 112, 112)
        val outputMap = digitizeImage(scaled, tfLite)
        if (outputMap.isEmpty()) {
            // no face
            return null
        }
        var listFaces = ArrayList<String>()
        outputMap.forEach {
            val itValue = it.value as Array<*>
            itValue.forEach {
                val faceData = (it as FloatArray).asList().toString().substring(1).dropLast(1)
                listFaces.add(faceData)
            }
        }
        return listFaces
    }

    companion object {
        private var imageMean: Float = 128.0f
        private var imageStd: Float = 128.0f
        private var inputSize: Int = 112
        private var outputSize: Int = 192 //Output size of model
        private var isModelQuantized: Boolean = false

        fun digitizeImage(bitmap: Bitmap, tfLite: Interpreter ): MutableMap<Int, Any> {
            //var facePreview: ImageView
            //facePreview.setImageBitmap(bitmap)
            //Create ByteBuffer to store normalized image
            val imgData = ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3 * 4)
            imgData.order(ByteOrder.nativeOrder())
            var intValues = IntArray(inputSize * inputSize)
            //get pixel values from Bitmap to normalize
            bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            imgData.rewind()
            for (i in 0 until inputSize) {
                for (j in 0 until inputSize) {
                    val pixelValue = intValues[i * inputSize + j]
                    if (isModelQuantized) {
                        // Quantized model
                        imgData.put(((pixelValue shr 16) and 0xFF).toByte())
                        imgData.put(((pixelValue shr 8) and 0xFF).toByte())
                        imgData.put((pixelValue and 0xFF).toByte())
                    } else { // Float model
                        imgData.putFloat((((pixelValue shr 16) and 0xFF) - imageMean) / imageStd)
                        imgData.putFloat((((pixelValue shr 8) and 0xFF) - imageMean) / imageStd)
                        imgData.putFloat(((pixelValue and 0xFF) - imageMean) / imageStd)
                    }
                }
            }
            //imgData is input to our model
            val inputArray = arrayOf<Any>(imgData)
//output of model will be stored in this variable
            val outputMap: MutableMap<Int, Any> = HashMap()
            outputMap[0] = Array(1) { FloatArray(outputSize) }
            tfLite.runForMultipleInputsOutputs(inputArray, outputMap) //Run model
            return outputMap
        }

        fun loadModelFile(context: Context, modelFile: String): MappedByteBuffer {
            val fileDescriptor = context.assets.openFd(modelFile)
            val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = fileDescriptor.startOffset
            val declaredLength = fileDescriptor.declaredLength
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        }

    }
}
