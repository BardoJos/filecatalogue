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

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.farrusco.projectclasses.R
import com.farrusco.projectclasses.graphics.BitmapManager
import java.io.File
import kotlin.math.roundToInt
import androidx.core.view.isNotEmpty
import androidx.core.content.withStyledAttributes

@Suppress("Unused")
class TabLayoutExt @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : HorizontalScrollView(context, attrs, defStyle) {
    val tabStrip: TabStripExt
    private val titleOffset: Int
    private val tabViewBackgroundResId: Int
    private val tabViewTextAllCaps: Boolean
    private var tabViewTextColors: ColorStateList
    private val tabViewTextSize: Float
    private val tabViewTextHorizontalPadding: Int
    private val tabViewTextMinWidth: Int
    private var viewPager: ViewPager2? = null
    private var onScrollChangeListener: OnScrollChangeListener? = null
    private var tabProvider: TabProvider? = null
    private val internalTabClickListener: InternalTabClickListener?
    private var onTabClickListener: OnTabClickListener? = null
    private var distributeEvenly: Boolean
    private var tabMode = 0
    private var viewPagerPageChangeListener: ViewPager.OnPageChangeListener? = null

    var alFragmentNames: ArrayList<FragmentNames> = ArrayList()

    init {
        // Disable the Scroll Bar
        isHorizontalScrollBarEnabled = false
        val dm = resources.displayMetrics
        val density = dm.density
        var tabBackgroundResId = NO_ID
        var textAllCaps = TAB_VIEW_TEXT_ALL_CAPS
        var textColors: ColorStateList? = null
        var textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, TAB_VIEW_TEXT_SIZE_SP.toFloat(), dm
        )
        var textHorizontalPadding = (TAB_VIEW_PADDING_DIPS * density).toInt()
        var textMinWidth = (TAB_VIEW_TEXT_MIN_WIDTH * density).toInt()
        var distributeEvenly = DEFAULT_DISTRIBUTE_EVENLY
        var customTabLayoutId = NO_ID
        var customTabTextViewId = NO_ID
        var clickable = TAB_CLICKABLE
        var titleOffset = (TITLE_OFFSET_DIPS * density).toInt()

        context.withStyledAttributes(
            attrs, R.styleable.TabLayoutExt, defStyle, 0
        ) {
            tabBackgroundResId = getResourceId(
                R.styleable.TabLayoutExt_xtlDefaultTabBackground, tabBackgroundResId
            )
            textAllCaps = getBoolean(
                R.styleable.TabLayoutExt_xtlDefaultTabTextAllCaps, textAllCaps
            )
            textColors = getColorStateList(
                R.styleable.TabLayoutExt_xtlDefaultTabTextColor
            )!!
            textSize = getDimension(
                R.styleable.TabLayoutExt_xtlDefaultTabTextSize, textSize
            )
            textHorizontalPadding = getDimensionPixelSize(
                R.styleable.TabLayoutExt_xtlDefaultTabTextHorizontalPadding,
                textHorizontalPadding
            )
            textMinWidth = getDimensionPixelSize(
                R.styleable.TabLayoutExt_xtlDefaultTabTextMinWidth, textMinWidth
            )
            customTabLayoutId = getResourceId(
                R.styleable.TabLayoutExt_xtlCustomTabTextLayoutId, customTabLayoutId
            )
            customTabTextViewId = getResourceId(
                R.styleable.TabLayoutExt_xtlCustomTabTextViewId, customTabTextViewId
            )
            distributeEvenly = getBoolean(
                R.styleable.TabLayoutExt_xtlDistributeEvenly, distributeEvenly
            )
            clickable = getBoolean(
                R.styleable.TabLayoutExt_xtlClickable, clickable
            )
            titleOffset = getLayoutDimension(
                R.styleable.TabLayoutExt_xtlTitleOffset, titleOffset
            )

            tabMode = getLayoutDimension(
                R.styleable.TabLayoutExt_xtlTabMode, DEFAULT_TAB_MODE
            )

        }
        this.titleOffset = titleOffset
        tabViewBackgroundResId = tabBackgroundResId
        tabViewTextAllCaps = textAllCaps
        tabViewTextColors = textColors ?: ColorStateList.valueOf(TAB_VIEW_TEXT_COLOR)
        tabViewTextSize = textSize
        tabViewTextHorizontalPadding = textHorizontalPadding
        tabViewTextMinWidth = textMinWidth
        internalTabClickListener = if (clickable) InternalTabClickListener() else null
        this.distributeEvenly = distributeEvenly
        if (customTabLayoutId != NO_ID) {
            setCustomTabView(customTabLayoutId, customTabTextViewId)
        }
        tabStrip = TabStripExt(context, attrs)
        if (distributeEvenly && tabStrip.isIndicatorAlwaysInCenter) {
            throw UnsupportedOperationException(
                "'distributeEvenly' and 'indicatorAlwaysInCenter' both use does not support"
            )
        }

        // Make sure that the Tab Strips fills this View
        isFillViewport = !tabStrip.isIndicatorAlwaysInCenter
        addView(tabStrip, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        if ( onScrollChangeListener != null) {
            onScrollChangeListener!!.onScrollChanged(l, oldl)
        }
    }

    /*override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

        this.mCurrentTab = position
        this.mCurrentPositionOffset = positionOffset
        scrollToCurrentTab()
        invalidate()
    }*/

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (tabStrip.isIndicatorAlwaysInCenter && tabStrip.isNotEmpty()) {
            val firstTab = tabStrip.getChildAt(0)
            val lastTab = tabStrip.getChildAt(tabStrip.childCount - 1)
            val start = (w - TabUtilsExt.getMeasuredWidth(firstTab)) / 2 - TabUtilsExt.getMarginStart(firstTab)
            val end = (w - TabUtilsExt.getMeasuredWidth(lastTab)) / 2 - TabUtilsExt.getMarginEnd(lastTab)
            tabStrip.minimumWidth = tabStrip.measuredWidth
            @Suppress("DEPRECATION")
            ViewCompat.setPaddingRelative(this, start, paddingTop, end, paddingBottom)
            clipToPadding = false
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        // Ensure first scroll
        if (changed && viewPager != null) {
            scrollToTab(viewPager!!.currentItem, 0f)
        }
    }

    /**
     * Set the behavior of the Indicator scrolling feedback.
     *
     * @param interpolator [. . sTabLayoutExt. SmartTabIndicationInterpolator][com]
     */
    fun setIndicationInterpolator(interpolator: TabIndicationInterpolatorExt?) {
        if (interpolator != null) {
            tabStrip.setIndicationInterpolator(interpolator)
        }
    }

    /**
     * Set the custom [TabColorizer] to be used.
     *
     * If you only require simple customisation then you can use
     * [.setSelectedIndicatorColors] and [.setDividerColors] to achieve
     * similar effects.
     */
    fun setCustomTabColorizer(tabColorizer: TabColorizer?) {
        tabStrip.setCustomTabColorizer(tabColorizer)
    }

    /**
     * Set the color used for styling the tab text. This will need to be called prior to calling
     * [.setViewPager] otherwise it will not get set
     *
     * @param color to use for tab text
     */
    fun setDefaultTabTextColor(color: Int) {
        tabViewTextColors = ColorStateList.valueOf(color)
    }

    /**
     * Sets the colors used for styling the tab text. This will need to be called prior to calling
     * [.setViewPager] otherwise it will not get set
     *
     * @param colors ColorStateList to use for tab text
     */
    fun setDefaultTabTextColor(colors: ColorStateList) {
        tabViewTextColors = colors
    }

    /**
     * Set the same weight for tab
     */
    fun setDistributeEvenly(distributeEvenly: Boolean) {
        this.distributeEvenly = distributeEvenly
    }

    /**
     * Sets the colors to be used for indicating the selected tab. These colors are treated as a
     * circular array. Providing one color will mean that all tabs are indicated with the same color.
     */
    fun setSelectedIndicatorColors(vararg colors: Int) {
        tabStrip.setSelectedIndicatorColors(*colors)
    }

    /**
     * Sets the colors to be used for tab dividers. These colors are treated as a circular array.
     * Providing one color will mean that all tabs are indicated with the same color.
     */
    fun setDividerColors(vararg colors: Int) {
        tabStrip.setDividerColors(*colors)
    }

    /**
     * Set the [ViewPager.OnPageChangeListener]. When using [TabLayoutExt] you are
     * required to set any [ViewPager.OnPageChangeListener] through this method. This is so
     * that the layout can update it's scroll position correctly.
     *
     * @see ViewPager.setOnPageChangeListener
     */
    fun setOnPageChangeListener(listener: ViewPager.OnPageChangeListener?) {
        viewPagerPageChangeListener = listener
    }

    /**
     * Set [OnScrollChangeListener] for obtaining values of scrolling.
     *
     * @param listener the [OnScrollChangeListener] to set
     */
    fun setOnScrollChangeListener(listener: OnScrollChangeListener?) {
        onScrollChangeListener = listener
    }

    /**
     * Set [OnTabClickListener] for obtaining click event.
     *
     * @param listener the [OnTabClickListener] to set
     */
    fun setOnTabClickListener(listener: OnTabClickListener?) {
        onTabClickListener = listener
    }

    /**
     * Set the custom layout to be inflated for the tab views.
     *
     * @param layoutResId Layout id to be inflated
     * @param textViewId id of the [android.widget.TextView] in the inflated view
     */
    fun setCustomTabView(layoutResId: Int, textViewId: Int) {
        tabProvider = SimpleTabProvider(context, layoutResId, textViewId)
    }

    /**
     * Set the custom layout to be inflated for the tab views.
     *
     * @param provider [TabProvider]
     */
    fun setCustomTabView(provider: TabProvider?) {
        tabProvider = provider
    }

    /**
     * Sets the associated view pager. Note that the assumption here is that the pager content
     * (number of tabs and tab titles) does not change after this call has been made.
     */
    fun setViewPager(viewPager: ViewPager2?) {
        tabStrip.removeAllViews()
        this.viewPager = viewPager
        if (viewPager == null) {
            populateTabStripNames()
        } else {
            viewPager.registerOnPageChangeCallback(InternalViewPagerListener())
            if ( viewPager.adapter != null){
                //viewPager.addOnPageChangeListener(InternalViewPagerListener())
                populateTabStrip()
            } else {
                populateTabStripNames()
            }
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean{
        if (tabMode == DEFAULT_TAB_FIXED) {
            return false
        }
        return super.onTouchEvent(ev)
    }

    fun getTabAt(position: Int): View {
        return tabStrip.getChildAt(position)
    }

    /*  look for:  mTabsViewpager.currentItem = ?*/
    fun setTabAt(position: Int)  {
        if (position >= 0 && position < alFragmentNames.size ) {
            tabStrip.onViewPagerPageChanged( position, 0f)
            scrollToTab(position, 0f)
        }
    }

    /*  look for:  mTabsViewpager.currentItem = ?*/
/*    fun setTabAt(position: Int): Int {
        if (position >= 0 && position < alFragmentNames.size && position != lastPosition) {
            //lastPosition = position
            tabStrip.onViewPagerPageChanged( position, 0f)
            lastPosition = if (scrollToTab(position, 0f)) position else -1
        }
        return lastPosition
    }*/
    private fun createDefaultTabViewFile(title: CharSequence?, filename: String?, rotation: Float?):  View {
        val imageView = ImageView(context)
        val imageMaxSize = 100
        imageView.tooltipText = title

        imageView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT
        )
        val file = File(filename!!)
        BitmapManager.createThumbnails(context,0,file,imageMaxSize)
        Glide.with(context)
            .load(file.absolutePath)
            .override(imageMaxSize)
            .centerCrop()
            .error(R.drawable.logo)
            .into(imageView)
        imageView.rotation = rotation!!.toFloat()

        imageView.setPadding(
            tabViewTextHorizontalPadding, 0,
            tabViewTextHorizontalPadding, 0
        )
        if (tabViewTextMinWidth > 0) {
            imageView.minimumWidth  = tabViewTextMinWidth
        }
        file.deleteOnExit()
        return imageView
    }
    /**
     * Create a default view to be used for tabs. This is called if a custom tab view is not set via
     * [.setCustomTabView].
     */
    private fun createDefaultTabViewText(title: CharSequence?): TextView {
        val textView = TextView(context)
        textView.gravity = Gravity.CENTER
        textView.text = title
        textView.setTextColor(tabViewTextColors)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, tabViewTextSize)
        textView.typeface = Typeface.DEFAULT_BOLD
        textView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT
        )
        if (tabViewBackgroundResId != NO_ID) {
            textView.setBackgroundResource(tabViewBackgroundResId)
        } else {
            // If we're running on Honeycomb or newer, then we can use the Theme's
            // selectableItemBackground to ensure that the View has a pressed state
            val outValue = TypedValue()
            context.theme.resolveAttribute(
                android.R.attr.selectableItemBackground,
                outValue, true
            )
            textView.setBackgroundResource(outValue.resourceId)
        }
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            // If we're running on ICS or newer, enable all-caps to match the Action Bar tab style
            textView.isAllCaps = tabViewTextAllCaps
        //}
        textView.setPadding(
            tabViewTextHorizontalPadding, 0,
            tabViewTextHorizontalPadding, 0
        )
        if (tabViewTextMinWidth > 0) {
            textView.minWidth = tabViewTextMinWidth
        }
        return textView
    }

    private fun populateTabStrip() {
        val adapter = viewPager!!.adapter
        for (i in 0 until adapter!!.itemCount) {
            val tabView = if (tabProvider == null) createDefaultTabViewText(
                alFragmentNames[i].fragmentName
            ) else tabProvider!!.createTabView(tabStrip, i, adapter)
            //val tabView = tabProvider!!.createTabView(tabStrip, i, adapter)
            checkNotNull(tabView) { "tabView is null." }
            if (distributeEvenly) {
                val lp = tabView.layoutParams as LinearLayout.LayoutParams
                lp.width = 0
                lp.weight = 1f
            }
            if (internalTabClickListener != null) {
                tabView.setOnClickListener(internalTabClickListener)
            }
            tabStrip.addView(tabView)
            if (i == viewPager!!.currentItem) {
                tabView.isSelected = true
            }
        }
    }
    private fun populateTabStripNames() {
        alFragmentNames.forEachIndexed { index, s ->
            val tabView = createDefaultTabViewText( s.fragmentName )
            //val tabView =  if (alFragmentNames[index].filename.isEmpty()) {
             //   createDefaultTabViewText( s.fragmentName )
            //} else {
            //    createDefaultTabViewFile( s.fragmentName, "${s.folder}/${s.filename}", s.rotation.toFloat() )
            //}
            checkNotNull(tabView) { "tabView is null." }
            if (distributeEvenly) {
                val lp = tabView.layoutParams as LinearLayout.LayoutParams
                lp.width = 0
                lp.weight = 1f
            }
            if (internalTabClickListener != null) {
                tabView.setOnClickListener(internalTabClickListener)
            }
            tabStrip.addView(tabView)
            if (viewPager != null && index == viewPager?.currentItem) {
                tabView.isSelected = true
            }
        }
    }
    private fun scrollToTab(tabIndex: Int, positionOffset: Float) {
        val tabStripChildCount = tabStrip.childCount

        if (tabStripChildCount == 0 || tabIndex < 0 || tabIndex >= tabStripChildCount) {
            return
        }
        val isLayoutRtl = TabUtilsExt.isLayoutRtl(this)
        val selectedTab = tabStrip.getChildAt(tabIndex)
        val widthPlusMargin = TabUtilsExt.getWidth(selectedTab) + TabUtilsExt.getMarginHorizontally(selectedTab)
        var extraOffset = (positionOffset * widthPlusMargin).toInt()
        /*        fix tabs - not working well
                 if (tabStrip.isIndicatorAlwaysInCenter) {
                    var tabIndex1 = 1
                    val selectedTab2 = tabStrip.getChildAt(tabIndex1)
                    if (0f < positionOffset && positionOffset < 1f) {
                        val nextTab = tabStrip.getChildAt(tabIndex1 + 1)
                        val selectHalfWidth =
                            TabUtilsExt.getWidth(selectedTab2) / 2 + TabUtilsExt.getMarginEnd(selectedTab2)
                        val nextHalfWidth = TabUtilsExt.getWidth(nextTab) / 2 + TabUtilsExt.getMarginStart(nextTab)
                        extraOffset = Math.round(positionOffset * (selectHalfWidth + nextHalfWidth))
                    }
                    val firstTab = tabStrip.getChildAt(0)
                    var x: Int
                    if (isLayoutRtl) {
                        val first = TabUtilsExt.getWidth(firstTab) + TabUtilsExt.getMarginEnd(firstTab)
                        val selected = TabUtilsExt.getWidth(selectedTab2) + TabUtilsExt.getMarginEnd(selectedTab2)
                        x = TabUtilsExt.getEnd(selectedTab2) - TabUtilsExt.getMarginEnd(selectedTab2) - extraOffset
                        x -= (first - selected) / 2
                    } else {
                        val first = TabUtilsExt.getWidth(firstTab) + TabUtilsExt.getMarginStart(firstTab)
                        val selected = TabUtilsExt.getWidth(selectedTab2) + TabUtilsExt.getMarginStart(selectedTab2)
                        x = TabUtilsExt.getStart(selectedTab2) - TabUtilsExt.getMarginStart(selectedTab2) + extraOffset
                        x -= (first - selected) / 2
                    }
                    scrollTo(x, 0)
                    return
                }*/
        if (tabStrip.isIndicatorAlwaysInCenter) {
            if (0f < positionOffset && positionOffset < 1f) {
                val nextTab = tabStrip.getChildAt(tabIndex + 1)
                val selectHalfWidth =
                    TabUtilsExt.getWidth(selectedTab) / 2 + TabUtilsExt.getMarginEnd(selectedTab)
                val nextHalfWidth = TabUtilsExt.getWidth(nextTab) / 2 + TabUtilsExt.getMarginStart(nextTab)
                extraOffset = (positionOffset * (selectHalfWidth + nextHalfWidth)).roundToInt()
            }
            val firstTab = tabStrip.getChildAt(0)
            var x: Int
            var first = firstTab.width
            var selected = selectedTab.width
            if (isLayoutRtl) {
                first += TabUtilsExt.getMarginEnd(firstTab)
                selected += TabUtilsExt.getMarginEnd(selectedTab)
                x = TabUtilsExt.getEnd(selectedTab) - TabUtilsExt.getMarginEnd(selectedTab) - extraOffset
            } else {
                first += TabUtilsExt.getMarginStart(firstTab)
                selected += TabUtilsExt.getMarginStart(selectedTab)
                x = TabUtilsExt.getStart(selectedTab) - TabUtilsExt.getMarginStart(selectedTab) + extraOffset
            }
            x -= (first - selected) / 2
            scrollTo(x, 0)
            return
        }
        var x: Int
        if (titleOffset == TITLE_OFFSET_AUTO_CENTER) {
            if (0f < positionOffset && positionOffset < 1f) {
                val nextTab = tabStrip.getChildAt(tabIndex + 1)
                val selectHalfWidth =
                    TabUtilsExt.getWidth(selectedTab) / 2 + TabUtilsExt.getMarginEnd(selectedTab)
                val nextHalfWidth = TabUtilsExt.getWidth(nextTab) / 2 + TabUtilsExt.getMarginStart(nextTab)
                extraOffset = (positionOffset * (selectHalfWidth + nextHalfWidth)).roundToInt()
            }
            if (isLayoutRtl) {
                x = -TabUtilsExt.getWidthWithMargin(selectedTab) / 2 + width / 2
                x -= TabUtilsExt.getPaddingStart(this)
            } else {
                x = TabUtilsExt.getWidthWithMargin(selectedTab) / 2 - width / 2
                x += TabUtilsExt.getPaddingStart(this)
            }
        } else {
            x = if (isLayoutRtl) {
                if (tabIndex > 0 || positionOffset > 0) titleOffset else 0
            } else {
                if (tabIndex > 0 || positionOffset > 0) -titleOffset else 0
            }
        }
        val start = TabUtilsExt.getStart(selectedTab)
        val startMargin = TabUtilsExt.getMarginStart(selectedTab)
        x += if (isLayoutRtl) {
            start + startMargin - extraOffset - width + TabUtilsExt.getPaddingHorizontally(
                this
            )
        } else {
            start - startMargin + extraOffset
        }
        scrollTo(x, 0)
    }


    /**
     * Allows complete control over the colors drawn in the tab layout. Set with
     * [.setCustomTabColorizer].
     */
    interface TabColorizer {
        /**
         * @return return the color of the indicator used when `position` is selected.
         */
        fun getIndicatorColor(position: Int): Int

        /**
         * @return return the color of the divider drawn to the right of `position`.
         */
        fun getDividerColor(position: Int): Int
    }

    /**
     * Interface definition for a callback to be invoked when the scroll position of a view changes.
     */
    interface OnScrollChangeListener {
        fun onScrollChanged(scrollX: Int, oldScrollX: Int)
    }

    /**
     * Interface definition for a callback to be invoked when a tab is clicked.
     */
    interface OnTabClickListener {
        fun onTabClicked(position: Int)
    }

    /**
     * Create the custom tabs in the tab layout. Set with
     * [setCustomTabView(com..sTabLayoutExt.tabLayoutExt.TabProvider)][.]
     */
    interface TabProvider {
        /**
         * @return Return the View of `position` for the Tabs
         */
        fun createTabView(container: ViewGroup?, position: Int, adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>?): View?
    }

    private class SimpleTabProvider(
        context: Context,
        private val tabViewLayoutId: Int,
        private val tabViewTextViewId: Int
    ) : TabProvider {
        private val inflater: LayoutInflater = LayoutInflater.from(context)

        override fun createTabView(
            container: ViewGroup?,
            position: Int,
            adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>?
        ): View? {
            var tabView: View? = null
            var tabTitleView: TextView? = null
            if (tabViewLayoutId != NO_ID) {
                tabView = inflater.inflate(tabViewLayoutId, container, false)
            }
            if (tabViewTextViewId != NO_ID && tabView != null) {
                tabTitleView = tabView.findViewById<View>(tabViewTextViewId) as TextView
            }
            if (tabTitleView == null && TextView::class.java.isInstance(tabView)) {
                tabTitleView = tabView as TextView?
            }
            if (tabTitleView != null) {
                //tabTitleView.text = adapter!!.getPageTitle(position)
                tabTitleView.text = container?.accessibilityPaneTitle
            }
            return tabView
        }
    }

    private inner class InternalViewPagerListener : ViewPager2.OnPageChangeCallback() {
        private var scrollState = 0
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            val tabStripChildCount = tabStrip.childCount
            if (tabStripChildCount == 0 || position < 0 || position >= tabStripChildCount) {
                return
            }
            tabStrip.onViewPagerPageChanged(position, positionOffset)
            scrollToTab(position, positionOffset)
            if (viewPagerPageChangeListener != null) {
                viewPagerPageChangeListener!!.onPageScrolled(
                    position,
                    positionOffset,
                    positionOffsetPixels
                )
            }
        }

        override fun onPageScrollStateChanged(state: Int) {
            scrollState = state
            if (viewPagerPageChangeListener != null) {
                viewPagerPageChangeListener!!.onPageScrollStateChanged(state)
            }
        }

        override fun onPageSelected(position: Int) {
            if (scrollState == ViewPager.SCROLL_STATE_IDLE) {
                tabStrip.onViewPagerPageChanged(position, 0f)
                scrollToTab(position, 0f)
            }
            var i = 0
            val size = tabStrip.childCount
            while (i < size) {
                tabStrip.getChildAt(i).isSelected = position == i
                i++
            }
            if (viewPagerPageChangeListener != null) {
                viewPagerPageChangeListener!!.onPageSelected(position)
            }
        }
    }

    private inner class InternalTabClickListener : OnClickListener {
        override fun onClick(v: View) {
            for (i in 0 until tabStrip.childCount) {
                if (v === tabStrip.getChildAt(i)) {
                    if (onTabClickListener != null) {
                        onTabClickListener!!.onTabClicked(i)
                    }
                    viewPager?.currentItem = i
                    return
                }
            }
        }
    }

    companion object {
        private const val DEFAULT_DISTRIBUTE_EVENLY = false
        private const val TITLE_OFFSET_DIPS = 24
        private const val TITLE_OFFSET_AUTO_CENTER = -1
        private const val TAB_VIEW_PADDING_DIPS = 16
        private const val TAB_VIEW_TEXT_ALL_CAPS = true
        private const val TAB_VIEW_TEXT_SIZE_SP = 12
        private const val TAB_VIEW_TEXT_COLOR = -0x4000000
        private const val TAB_VIEW_TEXT_MIN_WIDTH = 0
        private const val TAB_CLICKABLE = true
        private const val DEFAULT_TAB_MODE = 0
        private const val DEFAULT_TAB_AUTO = 0
        private const val DEFAULT_TAB_FIXED = 1
        private const val DEFAULT_TAB_SCROLLABLE = 2
    }
}
