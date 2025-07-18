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
package com.farrusco.projectclasses.common

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.util.Linkify
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.farrusco.projectclasses.R
import com.farrusco.projectclasses.messages.ToastExt

class ShowHelp : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.showhelpactivity)

        val intent: Intent = intent
        val aboutText: TextView = findViewById<View>(R.id.about_text) as TextView
        aboutText.text = intent.getStringExtra("helptext")
        // Make links
        Linkify.addLinks(aboutText, Linkify.EMAIL_ADDRESSES or Linkify.WEB_URLS)
        if (aboutText.text.isEmpty()) {
            this.finish()
            ToastExt().makeText(this, R.string.mess008_nodata, Toast.LENGTH_LONG).show()
        }
    }
}
