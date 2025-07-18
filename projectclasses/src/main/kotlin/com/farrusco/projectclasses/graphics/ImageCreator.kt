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

import android.content.Context
import java.io.File
import java.util.Stack
import java.util.concurrent.atomic.AtomicBoolean

class ImageCreator(private val context: Context)  {

    private var photoLoaderThread: PhotosLoader
    var photosQueue: PhotosQueue
    var pause = AtomicBoolean(false)
    private val pauseLock = Object()

    var cache: HashMap<String, Int> = HashMap ()
    fun queuePhoto(id: Int, file: File, title: String, maxSize: Int, rotation: Float) {
        // This ImageView may be used for other images before. So there may be
        // some old tasks in the queue. We need to discard them.
        photosQueue.clean(id)
        val p = PhotoToLoad(id, file, title, maxSize, rotation)
        synchronized(photosQueue.photosToLoad) {
            photosQueue.photosToLoad.push(p)
            synchronized(pauseLock) {
                try {
                    pauseLock.notifyAll()
                } catch (_: InterruptedException) {
                    //Logging.i("queuePhoto/InterruptedException",e.toString())
                }
            }
        }

        // start thread if it's not started yet
        if (photoLoaderThread.state == Thread.State.NEW) photoLoaderThread.start()
    }

    // Task for the queue
    inner class PhotoToLoad(
        var id: Int,
        var file: File,
        var title: String,
        var maxSize: Int,
        var rotation: Float
    )

    fun stopThread() {
        photoLoaderThread.interrupt()
    }

    // stores list of photos to download
    inner class PhotosQueue {
        val photosToLoad = Stack<PhotoToLoad>()

        // removes all instances of this ImageView
        fun clean(id: Int) {
            var i = 0
            try {
                while (i < photosToLoad.size) {
                    if (photosToLoad[i].id  == id) photosToLoad.removeAt(i) else ++i
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

                        if (!cache.containsKey(photoToLoad.file.absolutePath)) {
                            BitmapManager.createThumbnails(context,photoToLoad.id,photoToLoad.file,photoToLoad.maxSize)
                            cache[photoToLoad.file.absolutePath] = 1

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


    companion object {
        //private var inflater: LayoutInflater? = null

    }

    init {
        photoLoaderThread = PhotosLoader()
        photosQueue = PhotosQueue()

        // Make the background thead low priority. This way it will not affect
        // the UI performance
        photoLoaderThread.priority = Thread.NORM_PRIORITY - 1
        if (photoLoaderThread.state == Thread.State.NEW) photoLoaderThread.start()
    }
    fun destroyClass() {
        photoLoaderThread.interrupt()
    }
}