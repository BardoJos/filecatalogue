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

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.farrusco.projectclasses.R
import com.farrusco.projectclasses.common.extensionApplication
import com.farrusco.projectclasses.graphics.BitmapResolver
import com.farrusco.projectclasses.graphics.Graphics
import com.farrusco.projectclasses.widget.TouchImageView
import java.io.File

@Suppress("unused")
class TouchImageAdapter: PagerAdapter() {

    fun getBitmap(contentResolver: ContentResolver, position: Int): Bitmap? {
        val filename = "${images[position].dirname}/${images[position].filename}"
        if (!FilesFolders.hasFileAccess(filename)){
            return null
        }
        val file = File(filename)
        if (file.exists()) {
            val bitmap = BitmapResolver.getBitmap(contentResolver, filename)
            if (bitmap != null){
                return Graphics.rotateBitmap(bitmap, images[position].rotation)
            }
            Logging.i("fillGraph","File not found: $filename" )
        } else{
            Logging.i("fillGraph","File not found: $filename" )
        }
        return null
    }

    override fun getCount(): Int {
        return images.size
    }

    override fun getItemPosition(`object`: Any): Int {
        return super.getItemPosition(`object`)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): View {

        return TouchImageView(container.context).apply {
            if (images[position].filename.isEmpty() ) {
                val bm = BitmapFactory.decodeResource(resources, R.drawable.logo)
                setImageBitmap(bm)
            } else {
                val filename = "${images[position].dirname}/${images[position].filename}"
                val extApp = extensionApplication(container.context,filename)
                if (extApp.resourceIcon == 0){
                    val mH = this.maxHeight
                    val mW = this.maxWidth
                    Glide.with(container.context)
                        .asBitmap()
                        .load(filename)
                        .error(R.drawable.logo)
                        .into(object : CustomTarget<Bitmap>(){
                            override fun onResourceReady(
                                resource: Bitmap,
                                transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                            ) {
                                if (resource.height > 4500 || resource.width > 4500){
                                    val maxSize = 4500
                                    var max = Integer.min(mH, mW)
                                    if (maxSize < max){
                                        max = maxSize
                                    }
                                    setImageBitmap(Graphics.rotateBitmap(Graphics.resizeBitmap(resource,max), images[position].rotation))
                                } else {
                                    setImageBitmap(Graphics.rotateBitmap(resource, images[position].rotation))
                                }
                            }
                            override fun onLoadCleared(placeholder: Drawable?) {
                                // this is called when imageView is cleared on lifecycle call or for
                                // some other reason.
                                // if you are referencing the bitmap somewhere else too other than this imageView
                                // clear it here as you can no longer have the bitmap
                            }
                        })
                    TagModify.setViewTagValue(this,"_id", images[position].id )
                    TagModify.setViewTagValue(this,"position", position )
                } else {
                    val bm = BitmapFactory.decodeResource(resources, extApp.resourceIcon)
                    setImageBitmap(bm)
                }
            }

            container.addView(this, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        }
    }

    override fun destroyItem(container: ViewGroup, position: Int, objectAny: Any) {
        container.removeView(objectAny as View)
    }

    override fun isViewFromObject(view: View, objectAny: Any): Boolean {
        return view === objectAny
    }

    companion object {
        lateinit var images: ArrayList<ProductDetail>
    }
}

class ProductDetail{
    var id:Int = 0
    var rotation:Float = 0f
    var filename:String = ""
    var dirname:String = ""
}
