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
package com.farrusco.projectclasses.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import com.farrusco.projectclasses.common.ConstantsFixed
import com.farrusco.projectclasses.utils.TagModify

class SeekBarExt : androidx.appcompat.widget.AppCompatSeekBar {
    private var context1: Context
    var textViewLinkResource: Int = 0

    constructor(context: Context) : super(context) {
        this.context1 = context
        TagModify.setViewTagValue(this, ConstantsFixed.TagSection.TsForeBack.name,   "b")
        this.setOnSeekBarChangeListener(seekbarChangeListener)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        this.context1 = context
        TagModify.setViewTagValue(this, ConstantsFixed.TagSection.TsForeBack.name,   "b")
        this.setOnSeekBarChangeListener(seekbarChangeListener)
    }

    private var seekbarChangeListener: OnSeekBarChangeListener =
        object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // value.setText("SeekBar value is " + progress);

                if (seekBar.tag != null) {
                    val split: Array<String> = seekBar.tag.toString().split(",").toTypedArray()
                    if (progress * 2 + 1 < split.size) {
                        if (textViewLinkResource != 0 ) {
                            val vi =
                                (seekBar.parent as View).findViewById<View>(textViewLinkResource)
                            if (vi != null && vi is TextView) {
                                val textview: TextView = vi
                                textview.text = split[progress * 2 + 1]
                                TagModify.setViewTagValue(
                                    textview,
                                    ConstantsFixed.TagSection.TsDBValue.name,
                                    split[progress * 2]
                                )

                                TagModify.setViewTagValue(
                                    textview,
                                    ConstantsFixed.TagSection.TsUserFlag.name,
                                    ConstantsFixed.TagAction.Edit.name
                                )
                            }
                        }
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        }
}