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
import android.content.ComponentName
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.util.Linkify
import android.view.View
import android.widget.TextView
import com.farrusco.projectclasses.R

abstract class AboutActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.aboutactivity)
        setTitle(R.string.about)
        val aboutText: TextView = findViewById<View>(R.id.about_text) as TextView
        val comp = ComponentName(this, AboutActivity::class.java)
        var version: String
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val pInfo: PackageInfo = packageManager.getPackageInfo(comp.packageName,
                    PackageManager.PackageInfoFlags.of(0))
                version =  pInfo.versionName.toString()
            } else{
                val pInfo: PackageInfo = packageManager.getPackageInfo(comp.packageName, 0)
                version = pInfo.versionName.toString()
            }
        }catch (_: Exception) {
            version = ""
        }

        if (intent.hasExtra("title")) {
            title = intent.getStringExtra("title").toString()
        }
        if (intent.hasExtra("about")){
            aboutText.text = intent.getStringExtra("about").toString()
        }else {
            aboutText.text = String.format(
                resources.getString(resourceAbout),
                resources.getString(R.string.app_name), version
                        + " Database v" + Constants.DATABASE_VERSION
            )
        }
        Linkify.addLinks(aboutText, Linkify.EMAIL_ADDRESSES or Linkify.WEB_URLS)
    }

    abstract val resourceAbout: Int
}