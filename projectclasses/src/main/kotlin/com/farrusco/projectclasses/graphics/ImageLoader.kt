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
package com.farrusco.projectclasses.graphics

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.widget.ImageView
import com.farrusco.projectclasses.common.ConstantsFixed
import com.farrusco.projectclasses.utils.TagModify
import java.util.Stack
import java.util.concurrent.atomic.AtomicBoolean

class ImageLoader(
    activity: Activity,
    mContext: Context,
    noCols: Int
)  {

    private val mActivity: Activity

    // private int NUM_ROWS = 50;
    //private var bitmap: Bitmap? = null
    private var photoLoaderThread: PhotosLoader
    var photosQueue: PhotosQueue
    var pause = AtomicBoolean(false)
    private val pauseLock = Object()
    var bitmapManager: BitmapManager

    var cache: HashMap<String, CacheBitmap> = HashMap ()

    inner class CacheBitmap{
        private var bmp: Bitmap? = null
        var bitmap: Bitmap?
            get(){
                if (isValid && bmp != null) return bmp
                return BitmapManager.bitmapSquare
            }
            set(value) {
                bmp = value
            }
        var isValid: Boolean = false
    }

    /*
        fun queuePhoto(url: String?, title: String, rotation: Int,
            imageView: ImageView
        ) {
            // This ImageView may be used for other images before. So there may be
            // some old tasks in the queue. We need to discard them.
            photosQueue.clean(imageView)
            val p = PhotoToLoad(url, title, imageView, rotation)
            synchronized(photosQueue.photosToLoad) {
                photosQueue.photosToLoad.push(p)
                synchronized(pauseLock) {
                    try {
                        pauseLock.notifyAll()
                    } catch (e: InterruptedException) {
                        //Logging.i("queuePhoto/InterruptedException",e.toString())
                    }
                }
            }

            // start thread if it's not started yet
            if (photoLoaderThread.state == Thread.State.NEW) photoLoaderThread.start()
        }
    */

    // Task for the queue
    inner class PhotoToLoad(
        var fileName: String?,
        var title: String,
        var imageView: ImageView,
        var rotation: Int
    )

    fun stopThread() {
        photoLoaderThread.interrupt()
    }

    // stores list of photos to download
    inner class PhotosQueue {
        val photosToLoad = Stack<PhotoToLoad>()

        // removes all instances of this ImageView
        fun clean(image: ImageView) {
            var i = 0
            try {
                while (i < photosToLoad.size) {
                    if (photosToLoad[i].imageView === image) photosToLoad.removeAt(i) else ++i
                }
            } catch (_: ArrayIndexOutOfBoundsException) {
                // someone was faster
            }
        }
    }

    inner class PhotosLoader : Thread() {

        override fun run() {
            try {
                while (true) {
                    // thread waits until there are any images to load in the
                    // queue
                    if (photosQueue.photosToLoad.isEmpty()){
                        pause = AtomicBoolean(true)
                        synchronized(pauseLock) {
                            //if (pause.get()) {
                            try {
                                pauseLock.wait()
                            } catch (_: InterruptedException) {
                                //return true
                            }
                        }
                    }
                    if (photosQueue.photosToLoad.isNotEmpty()) {
                        var photoToLoad: PhotoToLoad
                        synchronized(photosQueue.photosToLoad) {
                            photoToLoad = photosQueue.photosToLoad.pop()
                        }

                        if (cache.containsKey(photoToLoad.fileName.toString())) {
                            val cacheBitmap = cache[photoToLoad.fileName.toString()]
                            val bd = BitmapDisplayer(photoToLoad.imageView, cacheBitmap!!.bitmap)
                            mActivity.runOnUiThread(bd)
                        } else {
                            val bmp: Bitmap? = bitmapManager.calcBitmap(
                                photoToLoad.fileName,
                                photoToLoad.title,
                                photoToLoad.rotation
                            )
                            val cacheBitmap = CacheBitmap()
                            cacheBitmap.bitmap = bmp
                            cacheBitmap.isValid = bmp != null
                            cache[photoToLoad.fileName.toString()] = cacheBitmap
                            val tag = TagModify.getViewTagValue(photoToLoad.imageView,ConstantsFixed.TagSection.TsDBValue.name)

                            if (tag == photoToLoad.fileName) {
                                val bd = BitmapDisplayer(photoToLoad.imageView, bmp)
                                // Activity a = (Activity) mContext;
                                // a=(Activity)new ImageView(mContext).getContext();
                                mActivity.runOnUiThread(bd)
                            }
                        }
                    }
                    if (interrupted()) break
                }
            } catch (_: NullPointerException) {
                // allow thread to exit
            } catch (_: InterruptedException) {
                // allow thread to exit
            }
        }
    }

    // Used to display bitmap in the UI thread
    internal inner class BitmapDisplayer(imageViewx: ImageView, b: Bitmap?) : Runnable {
        private var bitmap: Bitmap? = b
        var imageview: ImageView = imageViewx
        override fun run() {
            if (bitmap != null) imageview.setImageBitmap(bitmap) else imageview.setImageBitmap(
                BitmapManager.bitmapSquare
            )
        }
    }


    companion object {
        private var inflater: LayoutInflater? = null

    }

    init {
        bitmapManager = BitmapManager(mContext, noCols)
        mActivity = activity
        // .getString(R.string.listphotos_horz));
        inflater = activity
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        photoLoaderThread = PhotosLoader()
        photosQueue = PhotosQueue()

        // Make the background thead low priority. This way it will not affect
        // the UI performance
        photoLoaderThread.priority = Thread.NORM_PRIORITY - 1
        if (photoLoaderThread.state == Thread.State.NEW) photoLoaderThread.start()
    }
}