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

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle

class ContentValuesExt private constructor() {
    init {
        throw RuntimeException("Not allowed instances")
    }
    companion object {
        fun copyBundleContentValues(bundle: Bundle?): ContentValues {
            val map = ContentValues()
            bundle?.keySet()?.forEach {
                map.put(it, bundle.getString(it))
            }
            return map
        }

/*        fun copyContentValuesIntent(map: ContentValues?): Intent {
            val intent = Intent()
            if (map != null){
                map.keySet().forEach {
                    intent.putExtra(it,map.getAsString(it))
                }
            }
            return intent
        }*/

        fun copyIntentValuesToContentValues(intent: Intent): ContentValues {
            val keys = intent.extras?.keySet()
            val map = ContentValues()
            if (keys != null) {
                for (key in keys) {
                    map.put(key, intent.getIntExtra(key,-1))
                    if (map.getAsInteger(key)<0){
                        map.remove(key)
                        map.put(key, intent.getStringExtra(key))
                    }
                }
            }
            return map
        }

        fun getInteger(map: ContentValues, key: String, default: Int = 0): Int {
            return getString(map, key, "0").toInt()
        }

        fun getString(map: ContentValues, key: String, default: String = ""): String {
            if (map.containsKey(key)){
                return map.getAsString(key)
            }
            return default
        }
    }
}