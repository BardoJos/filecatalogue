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
package com.farrusco.projectclasses.messages

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AlertDialog
import com.farrusco.projectclasses.R

class ProgressIndicator(ctx: Context, private val isCancelable: Boolean = true) : AlertDialog(ctx) {

    override fun show() {
        super.show()
        //val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        setContentView(R.layout.progress_indicator)
        //val view = ProgressIndicatorBinding.inflate(inflater)
        //setContentView(view.root)
    }

    override fun onStart() {
        super.onStart()
        this.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        this.setCancelable(isCancelable)
    }

    override fun setCancelable(flag: Boolean) {
        super.setCancelable(isCancelable)
    }
}