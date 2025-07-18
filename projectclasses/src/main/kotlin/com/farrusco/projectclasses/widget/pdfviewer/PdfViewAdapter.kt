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
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.farrusco.projectclasses.R
import com.farrusco.projectclasses.databinding.ListItemPdfPageBinding

internal class PdfViewAdapter(
    private val renderer: PdfRendererCore,
    private val pageSpacing: Rect,
    private val enableLoadingForPages: Boolean
) :
    RecyclerView.Adapter<PdfViewAdapter.PdfPageViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfPageViewHolder {
        return PdfPageViewHolder(
            ListItemPdfPageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return renderer.getPageCount()
    }

    override fun onBindViewHolder(holder: PdfPageViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class PdfPageViewHolder(itemView: ListItemPdfPageBinding) : RecyclerView.ViewHolder(itemView.root) {
        fun bind(position: Int) {
            handleLoadingForPage(position)
            val pageView = itemView.findViewById<ImageView>(R.id.pageView)
            val containerView = itemView.findViewById<FrameLayout>(R.id.container_view)
            val pdfViewPageLoadingProgress = itemView.findViewById<ProgressBar>(R.id.pdf_view_page_loading_progress)

            pageView.setImageBitmap(null)
            //itemView.pageView.setImageBitmap(null)
            renderer.renderPage(position) { bitmap: Bitmap?, pageNo: Int ->
                if (pageNo != position)
                    return@renderPage
                bitmap?.let {
                    containerView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                        height =
                            (containerView.width.toFloat() / ((bitmap.width.toFloat() / bitmap.height.toFloat()))).toInt()
                        this.topMargin = pageSpacing.top
                        this.leftMargin = pageSpacing.left
                        this.rightMargin = pageSpacing.right
                        this.bottomMargin = pageSpacing.bottom
                    }
                    pageView.setImageBitmap(bitmap)
                    pageView.animation = AlphaAnimation(0F, 1F).apply {
                        interpolator = LinearInterpolator()
                        duration = 300
                    }

                    pdfViewPageLoadingProgress.visibility = View.GONE
                }
            }
        }

        private fun handleLoadingForPage(position: Int) {
            val pdfViewPageLoadingProgress = itemView.findViewById<ProgressBar>(R.id.pdf_view_page_loading_progress)

            if (!enableLoadingForPages) {
                pdfViewPageLoadingProgress.visibility = View.GONE
                return
            }

            if (renderer.pageExistInCache(position)) {
                pdfViewPageLoadingProgress.visibility = View.GONE
            } else {
                pdfViewPageLoadingProgress.visibility = View.VISIBLE
            }
        }
    }
}