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
package com.farrusco.projectclasses.widget.tablayout

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.farrusco.projectclasses.activity.FragmentCreate

class TabsPagerAdapterExt(pFragmentActivity: FragmentActivity?, pFm: FragmentManager, pLifecycle: Lifecycle) {
    var alResources: ArrayList<Int> = arrayListOf(0)
    //var alFragmentNames: ArrayList<String> = arrayListOf("")
    var fm = pFm
    var lifecycle = pLifecycle
    var fragmentActivity = pFragmentActivity
    private lateinit var mlTabViewer: ViewPager2
    //private late init var mlTabViewer1: ViewPager
    private lateinit var mlTabLayout: TabLayoutExt
    var isUserInputEnabled: Boolean = true
        get(){
            if (!::mlTabViewer.isInitialized) return field
            return mlTabViewer.isUserInputEnabled
        }
        set(value){
            if (::mlTabViewer.isInitialized) {
                mlTabViewer.isUserInputEnabled = value
            } else {
                field = value
            }
        }
    fun setResources(mTabLayout: TabLayoutExt, mTabViewer: ViewPager2,
                     apResources: ArrayList<Int>, apFragmentNames: ArrayList<String>){
        alResources = apResources
        //alFragmentNames = apFragmentNames
        mTabLayout.alFragmentNames.clear()
        apFragmentNames.forEach {
            val fragmentNames = FragmentNames()
            fragmentNames.fragmentName = it
            mTabLayout.alFragmentNames.add(fragmentNames)
        }

        mTabViewer.offscreenPageLimit = alResources.size
        mTabViewer.isUserInputEnabled = isUserInputEnabled

        /*        mTabLayout.setSelectedIndicatorColors(Color.BLUE)
                mTabLayout.setBackgroundColor(Color.BLACK)
                mTabLayout.setDefaultTabTextColor(Color.WHITE)*/

        // Set different Text Color for Tabs for when are selected or not
        //tab_layout.setTabTextColors(R.color.normalTabTextColor, R.color.selectedTabTextColor)

        //if (mTabLayout.getTabMode() != TabStripExt.DEFAULT_TAB_MODE) {
        //    mTabLayout.tabMode = mTabLayout.getTabMode()
        //}

        // Set Tabs in the center
        //tab_layout.tabGravity = TabLayout.GRAVITY_CENTER

        // Show all Tabs in screen
        //mTabLayout.tabMode = TabLayout.MODE_FIXED

        // Scroll to see all Tabs
        //tab_layout.tabMode = TabLayout.MODE_SCROLLABLE

        // Set Tab icons next to the text, instead of above the text
        //mTabLayout.isInlineLabel = true
        if (fragmentActivity == null){
            mTabViewer.adapter = object : FragmentStateAdapter(fm, lifecycle) {
                override fun createFragment(position: Int): Fragment {val bundle = Bundle()
                    if (position >= alResources.size) {
                        return FragmentCreate()
                    }
                    bundle.putInt("resource", alResources[position])
                    val mFragment = FragmentCreate()
                    mFragment.arguments = bundle
                    return mFragment
                }

                override fun getItemCount(): Int {
                    return alResources.size
                }
            }
        } else {
            mTabViewer.adapter = object : FragmentStateAdapter(fragmentActivity!!) {
                override fun createFragment(position: Int): Fragment {val bundle = Bundle()
                    if (position >= alResources.size) {
                        return FragmentCreate()
                    }
                    bundle.putInt("resource", alResources[position])
                    val mFragment = FragmentCreate()
                    mFragment.arguments = bundle
                    return mFragment
                }

                override fun getItemCount(): Int {
                    return alResources.size
                }
            }
        }
        mTabLayout.setViewPager(mTabViewer)
        mlTabViewer = mTabViewer
        mlTabLayout = mTabLayout

    }
/*
    fun setPos(position: Int){
        mlTabViewer.currentItem = position
    }

    fun getPos( ) :Int {
        return mlTabViewer.currentItem
    }*/

}