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
package com.farrusco.projectclasses.widget.validators

import android.widget.EditText

@Suppress("unused")
class AndValidator : OrValidator {
    constructor() : super()
    constructor(error: String) : super(error)

    override fun check(e: EditText?): Boolean {
        for (v in validators) {
            try {
                if (!v.check(e)) {
                    error2 = v.getErrorMessage(e)
                    return false
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                error2 = v.getErrorMessage(e)
                return false
            }
        }
        return true
    }
}