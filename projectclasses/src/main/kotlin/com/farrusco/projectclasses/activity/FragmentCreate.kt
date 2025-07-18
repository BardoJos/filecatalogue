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
package com.farrusco.projectclasses.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

open class FragmentCreate : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val resource = arguments?.getInt("resource")!!
        return inflater.inflate(resource, container, false)
    }

    interface OnFragmentAttachListener {
        fun onFragmentAttach(link: String?)
    }

    override fun onStart() {
        super.onStart()
        if (activity is OnFragmentAttachListener) {
            // signal parent fragment is ready to use
            val listener = activity as OnFragmentAttachListener
            listener.onFragmentAttach(this.tag)
        } else {
            throw ClassCastException(
                activity.toString()
                        + " must implement OnFragmentAttachListener"
            )
        }
    }

    override fun onResume() {
        super.onResume()
        if (activity is OnFragmentAttachListener) {
            // signal parent fragment is ready to use
            val listener = activity as OnFragmentAttachListener
            listener.onFragmentAttach(this.tag)
        } else {
            throw ClassCastException(
                activity.toString()
                        + " must implement OnFragmentAttachListener"
            )
        }
    }
}