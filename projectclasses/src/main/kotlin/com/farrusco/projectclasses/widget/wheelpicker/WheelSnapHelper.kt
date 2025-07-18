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

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView

internal class WheelSnapHelper : LinearSnapHelper() {
    override fun findSnapView(layoutManager: RecyclerView.LayoutManager?): View? {
        return (layoutManager as? LinearLayoutManager)?.takeIf { isValidSnap(it) }
            ?.run { super.findSnapView(layoutManager) }
    }

    private fun isValidSnap(linearLayoutManager: LinearLayoutManager): Boolean {
        val firstCompletelyVisibleItemPosition =
            linearLayoutManager.findFirstCompletelyVisibleItemPosition()
        val lastCompletelyVisibleItemPosition =
            linearLayoutManager.findLastCompletelyVisibleItemPosition()
        return firstCompletelyVisibleItemPosition != 0 && lastCompletelyVisibleItemPosition != linearLayoutManager.itemCount - 1
    }
}
