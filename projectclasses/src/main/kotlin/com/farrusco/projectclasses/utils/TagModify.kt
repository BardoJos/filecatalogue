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

import android.view.View
import com.farrusco.projectclasses.common.ConstantsFixed
import com.farrusco.projectclasses.widget.SpinnerExt

object TagModify {
    // setTagView
    fun setViewTagValue(vi: Any, prefix: String, value: Any?) {
        if (prefix == ConstantsFixed.TagSection.TsDBColumn.name) {
            // posible WidgetMoney().registerFields
            ViewUtils.moneyRegister(vi as View)
        }
        var tag = ""
        val strValue = value?.toString() ?: ""
        if (vi is SpinnerExt) {
            if (null != vi.tagX) {
                tag = vi.tagX.toString()
            }
            vi.tagX = setTagValue(tag, prefix, strValue)
        } else {
            if (null != (vi as View).tag) {
                tag = vi.tag.toString()
            }
            vi.tag = setTagValue(tag, prefix, strValue)
        }
    }

    fun deleteTagSection(tag: Any?, prefix: String): String {
        val tagm = tag.toString()
        if (tagm.isEmpty()) return ""
        var tagx = ""
        var found = false
        val tags = tagm.split(";").toTypedArray()
        tags.forEach {
            if (it.startsWith("$prefix=")) {
                found = true
            } else {
                tagx += if (tagx.isNotEmpty()) ";" else ""
                tagx += it
            }
        }
        if (found) {
            return tagx
        }
        return tagm
    }

    fun deleteViewTagSection(vi: Any, prefix: String) {
        val tagm = if (vi is SpinnerExt) {
            vi.tagX.toString()
        } else {
            (vi as View).tag.toString()
        }
        if (tagm.isEmpty()) return

        var tagx = ""
        var found = false
        val tags = tagm.split(";").toTypedArray()
        tags.forEach {
            if (it.startsWith("$prefix=")) {
                found = true
            } else {
                tagx += if (tagx.isNotEmpty()) ";" else ""
                tagx += it
            }
        }
        if (found) {
            if (vi is SpinnerExt) {
                vi.tagX = tagx
            } else {
                (vi as View).tag = tagx
            }
        }
    }

    fun setViewTagValue(vi: Any, prefix: ConstantsFixed.TagSection, value: Any?)  {
        if (vi is SpinnerExt) {
            vi.tagX = setTagValue(vi.tagX, prefix.name, value)
        } else {
            (vi as View).tag = setTagValue(vi.tag, prefix.name, value)
        }
    }
    fun setTagValue(tag: Any?, prefix: String, value: Any?): String {
        var mTag = ""
        if (tag != null) mTag=tag.toString()
        var tagx = ""
        var found = false
        val tags = mTag.split(";").toTypedArray()
        tags.forEach {
            tagx += if (tagx.isNotEmpty()) ";" else ""
            if (it.startsWith("$prefix=")) {
                found = true
                tagx += "$prefix=$value"
            } else {
                tagx += it
            }
        }
        if (!found) {
            tagx += (if (tagx.isNotEmpty()) ";" else "") + prefix + "=" + value
        }
        return tagx
    }

    fun hasTagValue(vi: Any, section: String, value: String): Boolean {
        if (vi is SpinnerExt){
            if (vi.tagX == null) return false
            return hasTagValue(vi.tagX.toString(), section, value)
        }
        if ((vi as View).tag == null) return false
        return hasTagValue(vi.tag.toString(), section, value)
    }

    private fun hasTagValue(tag: String, section: String, value: String): Boolean {
        return tag.indexOf("$section=$value") >= 0
    }

    fun hasTagSection(vi: Any, tag: Any): Boolean {
        val tagm = if (vi is SpinnerExt) {
            vi.tagX.toString()
        } else {
            if ((vi as View).tag == null) return false
            vi.tag.toString()
        }
        return tagm.indexOf("$tag=") >= 0
    }

    fun getViewTagValue(vi: Any, prefix: ConstantsFixed.TagSection): String {
        return getViewTagValue(vi, prefix.name)
    }

    fun getViewTagValue(vi: Any, section: String): String {
        val tagm = if (vi is SpinnerExt) {
            vi.tagX
        } else {
            (vi as View).tag
        }
        return getTagValue(tagm, section)
    }

    fun getTagValue(tag: Any?, section: String): String {
        if (tag == null) {
            return ""
        }
        if (tag.toString().indexOf("$section=") < 0) {
            return ""
        }
        val tags = tag.toString().split(";").toTypedArray()
        tags.forEach {
            if (it.startsWith("$section=")) {
                return it.substring(section.length + 1)
            }
        }
        return ""
    }
}