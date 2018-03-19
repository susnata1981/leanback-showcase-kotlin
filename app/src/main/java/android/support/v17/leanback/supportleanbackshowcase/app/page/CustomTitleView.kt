/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package android.support.v17.leanback.supportleanbackshowcase.app.page

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v17.leanback.supportleanbackshowcase.R
import android.support.v17.leanback.widget.TitleViewAdapter
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView

/**
 * Custom title view to be used in [android.support.v17.leanback.app.BrowseFragment].
 */
class CustomTitleView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : RelativeLayout(context, attrs, defStyle), TitleViewAdapter.Provider {
    lateinit private var mTitleView: TextView
    lateinit private var mAnalogClockView: View
    lateinit private var mSearchOrbView: View

    private val mTitleViewAdapter = object : TitleViewAdapter() {
        override fun getSearchAffordanceView(): View {
            return mSearchOrbView
        }

        override fun setTitle(titleText: CharSequence?) {
            this@CustomTitleView.setTitle(titleText)
        }

        override fun setBadgeDrawable(drawable: Drawable?) {
            //CustomTitleView.this.setBadgeDrawable(drawable);
        }

        override fun setOnSearchClickedListener(listener: View.OnClickListener) {
            mSearchOrbView.setOnClickListener(listener)
        }

        override fun updateComponentsVisibility(flags: Int) {
            /*if ((flags & BRANDING_VIEW_VISIBLE) == BRANDING_VIEW_VISIBLE) {
                updateBadgeVisibility(true);
            } else {
                mAnalogClockView.setVisibility(View.GONE);
                mTitleView.setVisibility(View.GONE);
            }*/

            val visibility = if (flags and TitleViewAdapter.SEARCH_VIEW_VISIBLE == TitleViewAdapter.SEARCH_VIEW_VISIBLE)
                View.VISIBLE
            else
                View.INVISIBLE
            mSearchOrbView.visibility = visibility
        }

        private fun updateBadgeVisibility(visible: Boolean) {
            if (visible) {
                mAnalogClockView.visibility = View.VISIBLE
                mTitleView.visibility = View.VISIBLE
            } else {
                mAnalogClockView.visibility = View.GONE
                mTitleView.visibility = View.GONE
            }
        }
    }

    init {
        val root = LayoutInflater.from(context).inflate(R.layout.custom_titleview, this)
        mTitleView = root.findViewById(R.id.title_tv)
        mAnalogClockView = root.findViewById(R.id.clock)
        mSearchOrbView = root.findViewById(R.id.search_orb)
    }

    fun setTitle(title: CharSequence?) {
        if (title != null) {
            mTitleView.text = title
            mTitleView.visibility = View.VISIBLE
            mAnalogClockView.visibility = View.VISIBLE
        }
    }


    fun setBadgeDrawable(drawable: Drawable?) {
        if (drawable != null) {
            mTitleView.visibility = View.GONE
            mAnalogClockView.visibility = View.VISIBLE
        }
    }

    override fun getTitleViewAdapter(): TitleViewAdapter {
        return mTitleViewAdapter
    }
}
