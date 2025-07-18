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
package com.farrusco.projectclasses.widget.pdfviewer

import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.Bitmap.createBitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import com.farrusco.projectclasses.common.Constants
import com.farrusco.projectclasses.common.ConstantsFixed
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.min

internal class PdfRendererCore(
    pdfFile: File,
    private val pdfQuality: ConstantsFixed.PdfQuality
) {
    companion object {
        private const val PREFETCH_COUNT = 3
    }

    private var pdfRenderer: PdfRenderer? = null

    init {
        initCache()
        openPdfFile(pdfFile)
    }

    private fun initCache() {
        val cache = File(Constants.pdfCache)
        if (cache.exists())
            cache.deleteRecursively()
        cache.mkdirs()
    }

    private fun getBitmapFromCache(pageNo: Int): Bitmap? {
        val loadPath = File(Constants.pdfCache, pageNo.toString())
        if (!loadPath.exists())
            return null

        return try {
            BitmapFactory.decodeFile(loadPath.absolutePath)
        } catch (e: Exception) {
            null
        }
    }
    fun pageExistInCache(pageNo: Int): Boolean {
        val loadPath = File(Constants.pdfCache, pageNo.toString())

        return loadPath.exists()
    }

    @Throws(IOException::class)
    private fun writeBitmapToCache(pageNo: Int, bitmap: Bitmap) {
        val savePath = File(File(Constants.pdfCache), pageNo.toString())
        savePath.createNewFile()
        val fos = FileOutputStream(savePath)
        bitmap.compress(CompressFormat.PNG, 100, fos)
        fos.flush()
        fos.close()
    }

    private fun openPdfFile(pdfFile: File) {
        try {
            val fileDescriptor =
                ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            pdfRenderer = PdfRenderer(fileDescriptor)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getPageCount(): Int = pdfRenderer?.pageCount ?: 0

    fun renderPage(pageNo: Int, onBitmapReady: ((bitmap: Bitmap?, pageNo: Int) -> Unit)? = null) {
        if (pageNo >= getPageCount())
            return

        CoroutineScope(Dispatchers.IO).launch {
            synchronized(this@PdfRendererCore) {
                buildBitmap(pageNo) { bitmap ->
                    CoroutineScope(Dispatchers.Main).launch { onBitmapReady?.invoke(bitmap, pageNo) }
                }
                onBitmapReady?.let {
                    //prefetchNext(pageNo + 1)
                }
            }
        }
    }

    private fun prefetchNext(pageNo: Int) {
        val countForPrefetch = min(getPageCount(), pageNo + PREFETCH_COUNT)
        for (pageToPrefetch in pageNo until countForPrefetch) {
            renderPage(pageToPrefetch)
        }
    }

    private fun buildBitmap(pageNo: Int, onBitmap: (Bitmap?) -> Unit) {
        var bitmap = getBitmapFromCache(pageNo)
        bitmap?.let {
            onBitmap(it)
            return@buildBitmap
        }

        try {
            val pdfPage = pdfRenderer!!.openPage(pageNo)
            bitmap = createBitmap(
                pdfPage.width * pdfQuality.ratio,
                pdfPage.height * pdfQuality.ratio,
                Bitmap.Config.ARGB_8888
            )
            pdfPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            pdfPage.close()
            writeBitmapToCache(pageNo, bitmap)

            onBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun closePdfRender() {
        if (pdfRenderer != null)
            try {
                pdfRenderer!!.close()
            } catch (e: Exception) {
                Log.e("PdfRendererCore", e.toString())
            }
    }
}