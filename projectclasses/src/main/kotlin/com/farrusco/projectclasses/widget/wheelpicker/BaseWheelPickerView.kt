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
package com.farrusco.projectclasses.widget.wheelpicker

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import com.farrusco.projectclasses.common.ConstantsFixed
import com.farrusco.projectclasses.utils.TagModify
import com.farrusco.projectclasses.utils.ViewUtils

//abstract
 open class BaseWheelPickerView (
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr),
    WheelPickerRecyclerView.WheelPickerRecyclerViewListener {

    var skipEditTagOnce = false
    var skipEditTagAlways: Boolean = false
        set(value) {
            field = value
            if (value) {
                tag = TagModify.deleteTagSection(tag, ConstantsFixed.TagSection.TsUserFlag.name)
            }
        }
    private lateinit var viWheelPickerRecyclerView: WheelPickerRecyclerView

     var colorView: ConstantsFixed.ColorBasic = ConstantsFixed.ColorBasic.Dark
         set(value) {
             this.getAllChildren(this).forEach {
                 if (it::class.simpleName == ViewUtils.LegalFields.TextView.name){
                     (it as TextView).setTextColor( value.color )
                 }
             }
             field = value
         }
    private fun getAllChildren(v: View): ArrayList<View> {
         val visited: ArrayList<View> = ArrayList()
         val unvisited: MutableList<View> = mutableListOf()
         unvisited.add(v)
         while (unvisited.isNotEmpty()) {
             val child = unvisited.removeAt(0)
             visited.add(child)

             if (child !is ViewGroup) continue

             val childCount = child.childCount
             for (i in 0 until childCount) unvisited.add(child.getChildAt(i))
         }
         return visited
     }
    interface AdapterImp {
        var isCircular: Boolean
        val valueCount: Int
    }

    interface WheelPickerViewListener {
        fun didSelectItem(picker: BaseWheelPickerView, index: Int)
        fun onScrollStateChanged(state: Int) {}
    }

    abstract class ViewHolder<Element : Any>(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun onBindData(data: Element)
    }

    abstract class Adapter<Element : Any, ViewHolder : BaseWheelPickerView.ViewHolder<Element>> :
        RecyclerView.Adapter<ViewHolder>(), AdapterImp {
        open var values: List<Element> = emptyList()
            @SuppressLint("NotifyDataSetChanged")
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        override val valueCount: Int
            get() = values.count()

        override var isCircular: Boolean = false
            @SuppressLint("NotifyDataSetChanged")
            set(value) {
                if (field == value) {
                    return
                }
                field = value
                notifyDataSetChanged()
            }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val value =
                values.getOrNull(if (isCircular) position % values.count() else position) ?: return
            holder.onBindData(value)
        }

        override fun getItemCount(): Int {
            return if (isCircular) Int.MAX_VALUE else values.count()
        }
    }

    var selectedIndex: Int
        set(value) {
            setSelectedIndex(value, false)
        }
        get() {
            val position = recyclerView.currentPosition
            return if (isCircular) {
                val valueCount = (recyclerView.adapter as? AdapterImp)?.valueCount ?: 0
                if (valueCount > 0) position % valueCount else NO_POSITION
            } else {
                position
            }
        }

    fun setText(text: String?, init: Boolean) {
        if (init) skipEditTagOnce = true
        if (text.isNullOrBlank()) {
            selectedIndex = 0
            setUserFlagTag()
        } else {
            setId(text)
        }
    }
    var textExt: String
        get() {
            return (recyclerView.adapter as TextWheelAdapter).values[selectedIndex].id
        }
        set(value) {
            setText(value, false)
        }

    private fun setId(id: String){
        val valueCount = (recyclerView.adapter as? AdapterImp)?.valueCount ?: 0
        for (x in 0 until valueCount){
            if ((recyclerView.adapter as TextWheelAdapter).values[x].id == id){
                selectedIndex = x
                setUserFlagTag()
                break
            }
        }
    }

    private fun setSelectedIndex(index: Int, animated: Boolean, completion: (() -> Unit)? = null) {
        val dstPosition: Int = if (isCircular) {
            index - selectedIndex + recyclerView.currentPosition
        } else {
            index
        }
        if (animated) {
            recyclerView.smoothScrollToCenterPosition(
                dstPosition,
                ignoreHapticFeedback = true,
                completion = completion
            )
        } else {
            recyclerView.scrollToCenterPosition(
                dstPosition,
                ignoreHapticFeedback = true,
                completion = completion
            )
        }
    }

    private val recyclerView: WheelPickerRecyclerView by lazy {
        val recyclerView = WheelPickerRecyclerView(context)
        addView(recyclerView)
        recyclerView.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        recyclerView.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        recyclerView.setWheelListener(this)
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                listener?.onScrollStateChanged(newState)
            }
        })
        recyclerView
    }

    fun <Element : Any, ViewHolder : BaseWheelPickerView.ViewHolder<Element>> setAdapter(adapter: Adapter<Element, ViewHolder>) {
        recyclerView.adapter = adapter
    }

    private var listener: WheelPickerViewListener? = null

    fun setWheelListener(listener: WheelPickerViewListener) {
        this.listener = listener
    }

    override fun setHapticFeedbackEnabled(hapticFeedbackEnabled: Boolean) {
        super.setHapticFeedbackEnabled(hapticFeedbackEnabled)
        recyclerView.isHapticFeedbackEnabled = hapticFeedbackEnabled
    }

    var isCircular: Boolean
        set(value) {
            val selectedIndex = this.selectedIndex
            (recyclerView.adapter as? AdapterImp)?.isCircular = value
            val completion = {
                recyclerView.refreshCurrentPosition()
            }
            if (value) {
                val valueCount = (recyclerView.adapter as? AdapterImp)?.valueCount ?: 0
                if (valueCount > 0) {
                    recyclerView.scrollToCenterPosition(
                        ((Int.MAX_VALUE / 2) / valueCount) * valueCount + selectedIndex,
                        true,
                        completion
                    )
                } else {
                    recyclerView.scrollToCenterPosition(selectedIndex, true, completion)
                }
            } else {
                recyclerView.scrollToCenterPosition(selectedIndex, true, completion)
            }
        }
        get() = (recyclerView.adapter as? AdapterImp)?.isCircular ?: false

    init {
        recyclerView
    }

    // region WheelPickerRecyclerView.WheelPickerRecyclerViewListener
    override fun didSelectItem(position: Int) {
        setUserFlagTag()
        listener?.didSelectItem(this, selectedIndex)
    }
    private fun setUserFlagTag() {
        if (selectedIndex < 0) return
        if (!::viWheelPickerRecyclerView.isInitialized){
            viWheelPickerRecyclerView = ViewUtils.findClass(this,WheelPickerRecyclerView::class.simpleName!!) as WheelPickerRecyclerView
        }
        val dbValue = (viWheelPickerRecyclerView.adapter as TextWheelAdapter).values[selectedIndex].id
        val tagValue = TagModify.getTagValue(tag,ConstantsFixed.TagSection.TsDBValue.name)
        if (tag != null &&
            (dbValue == tagValue ||tagValue.isEmpty())){
            return
        }
        if (tag == null) tag=""
        if (!(skipEditTagAlways || skipEditTagOnce)) {
            tag = TagModify.setTagValue(tag, ConstantsFixed.TagSection.TsUserFlag.name, ConstantsFixed.TagAction.Edit.name)
            colorView = ConstantsFixed.ColorBasic.Modified
        }
        tag = TagModify.setTagValue(tag, ConstantsFixed.TagSection.TsDBValue.name, dbValue)
        skipEditTagOnce=false

    }
    // endregion
}