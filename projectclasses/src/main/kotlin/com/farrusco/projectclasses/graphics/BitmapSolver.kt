package com.farrusco.projectclasses.graphics

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import com.farrusco.projectclasses.utils.FilesFolders
import com.farrusco.projectclasses.utils.Logging
import java.io.File
import java.io.IOException

object BitmapResolver {
    private const val TAG = "BitmapResolver"
    private fun getBitmapLegacy(contentResolver: ContentResolver, fileUri: Uri): Bitmap? {
        try {
            @Suppress("DEPRECATION")
            return MediaStore.Images.Media.getBitmap(contentResolver, fileUri)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    @SuppressLint("ObsoleteSdkInt")
    @RequiresApi(Build.VERSION_CODES.P)
    private fun getBitmapImageDecoder(contentResolver: ContentResolver, fileUri: Uri): Bitmap? {
        try {
            /*
            val bitmapOptions = BitmapFactory.Options()
            bitmapOptions.inPreferredConfig=Bitmap.Config.ARGB_8888
            return BitmapFactory.decodeStream(contentResolver.openInputStream(fileUri), null, bitmapOptions)
            */

            //val input = contentResolver.openInputStream(fileUri);
            //return BitmapFactory.decodeStream(input).copy(Bitmap.Config.ARGB_8888,true);
           //val bitmap = MediaStore.Images.Media.getBitmap(c.getContentResolver() , Uri.parse(paths))

            return ImageDecoder.decodeBitmap( ImageDecoder.createSource(contentResolver, fileUri)) {
                    decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.isMutableRequired = true
                }
        } catch (e: IOException) {
            Logging.e("fileUri: $fileUri")
            e.printStackTrace()
        }
        return null
    }

    @SuppressLint("ObsoleteSdkInt")
    fun getBitmap(contentResolver: ContentResolver, filename: String?): Bitmap? {
        if (filename == null) {
            Logging.i(TAG, "returning null because filename was null")
            return null
        }
        if (!FilesFolders.hasFileAccess(filename)){
            Logging.i(TAG, "no access filename")
            return null
        }
        val fileUri = File(filename).toUri()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getBitmapImageDecoder(contentResolver, fileUri)
        } else {
            getBitmapLegacy(contentResolver, fileUri)
        }
    }

    fun getBitmapFromUri(contentResolver: ContentResolver, uri: Uri): Bitmap {
        val parcelFileDescriptor =
            contentResolver.openFileDescriptor(uri, "r")
        val fileDescriptor = parcelFileDescriptor!!.fileDescriptor
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor.close()
        return image
    }

    fun rotateBitmap(
        bitmap: Bitmap, rotationDegrees: Int, flipX: Boolean, flipY: Boolean
    ): Bitmap {
        val matrix = Matrix()

        // Rotate the image back to straight.
        matrix.postRotate(rotationDegrees.toFloat())

        // Mirror the image along the X or Y axis.
        matrix.postScale(if (flipX) -1.0f else 1.0f, if (flipY) -1.0f else 1.0f)
        val rotatedBitmap =
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

        // Recycle the old bitmap if it has changed.
        if (rotatedBitmap != bitmap) {
            bitmap.recycle()
        }
        return rotatedBitmap
    }

    fun getCropBitmapByCPU(source: Bitmap, cropRectF: RectF): Bitmap {
        val resultBitmap = createBitmap(cropRectF.width().toInt(), cropRectF.height().toInt())
        val canvas = Canvas(resultBitmap)
        // draw background
        val paint = Paint(Paint.FILTER_BITMAP_FLAG)
        paint.color = Color.WHITE
        canvas.drawRect(
            RectF(0f, 0f, cropRectF.width(), cropRectF.height()),
            paint
        )

        val matrix = Matrix()
        matrix.postTranslate(-cropRectF.left, -cropRectF.top)
        canvas.drawBitmap(source, matrix, paint)
        if ( !source.isRecycled) {
            source.recycle()
        }
        return resultBitmap
    }

    fun getResizedBitmap(bm: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
        val width = bm.width
        val height = bm.height
        val scaleWidth = (newWidth.toFloat()) / width
        val scaleHeight = (newHeight.toFloat()) / height
        // CREATE A MATRIX FOR THE MANIPULATION
        val matrix = Matrix()
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight)

        // "RECREATE" THE NEW BITMAP
        val resizedBitmap = Bitmap.createBitmap(
            bm, 0, 0, width, height, matrix, false
        )
        bm.recycle()
        return resizedBitmap
    }

/*
    fun bitmapToNV21ByteArray(bitmap: Bitmap): ByteArray {
        val argb = IntArray(bitmap.width * bitmap.height )
        bitmap.getPixels(argb, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        val yuv = ByteArray(bitmap.height * bitmap.width + 2 * ceil(bitmap.height / 2.0).toInt()
                * ceil(bitmap.width / 2.0).toInt())
        encodeYUV420SP( yuv, argb, bitmap.width, bitmap.height)
        return yuv
    }
    private fun encodeYUV420SP(yuv420sp: ByteArray, argb: IntArray, width: Int, height: Int) {
        val frameSize = width * height
        var yIndex = 0
        var uvIndex = frameSize
        var R: Int
        var G: Int
        var B: Int
        var Y: Int
        var U: Int
        var V: Int
        var index = 0
        for (j in 0 until height) {
            for (i in 0 until width) {
                R = argb[index] and 0xff0000 shr 16
                G = argb[index] and 0xff00 shr 8
                B = argb[index] and 0xff shr 0
                Y = (66 * R + 129 * G + 25 * B + 128 shr 8) + 16
                U = (-38 * R - 74 * G + 112 * B + 128 shr 8) + 128
                V = (112 * R - 94 * G - 18 * B + 128 shr 8) + 128
                yuv420sp[yIndex++] = (if (Y < 0) 0 else if (Y > 255) 255 else Y).toByte()
                if (j % 2 == 0 && index % 2 == 0) {
                    yuv420sp[uvIndex++] = (if (V < 0) 0 else if (V > 255) 255 else V).toByte()
                    yuv420sp[uvIndex++] = (if (U < 0) 0 else if (U > 255) 255 else U).toByte()
                }
                index++
            }
        }
    }
*/

}