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

import android.annotation.SuppressLint
import android.app.Fragment
import android.os.Bundle
import android.support.v17.leanback.app.BrowseFragment
import android.support.v17.leanback.supportleanbackshowcase.R
import android.support.v17.leanback.transition.TransitionHelper
import android.support.v17.leanback.widget.ObjectAdapter
import android.support.v17.leanback.widget.OnChildLaidOutListener
import android.support.v17.leanback.widget.OnItemViewClickedListener
import android.support.v17.leanback.widget.OnItemViewSelectedListener
import android.support.v17.leanback.widget.Presenter
import android.support.v17.leanback.widget.Row
import android.support.v17.leanback.widget.RowPresenter
import android.support.v17.leanback.widget.VerticalGridPresenter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * A fragment for rendering items in a vertical grids.
 */
open class GridFragment : Fragment(), BrowseFragment.MainFragmentAdapterProvider {

    /**
     * Returns the object adapter.
     */
    /**
     * Sets the object adapter for the fragment.
     */
    var adapter: ObjectAdapter? = null
        set(adapter) {
            field = adapter
            updateAdapter()
        }
    /**
     * Returns the grid presenter.
     */
    /**
     * Sets the grid presenter.
     */
    var gridPresenter: VerticalGridPresenter? = null
        set(gridPresenter) {
            if (gridPresenter == null) {
                throw IllegalArgumentException("Grid presenter may not be null")
            }
            field = gridPresenter
            this.gridPresenter!!.onItemViewSelectedListener = mViewSelectedListener
            if (onItemViewClickedListener != null) {
                this.gridPresenter!!.onItemViewClickedListener = onItemViewClickedListener
            }
        }
    private var mGridViewHolder: VerticalGridPresenter.ViewHolder? = null
    private var mOnItemViewSelectedListener: OnItemViewSelectedListener? = null
    /**
     * Returns the item clicked listener.
     */
    /**
     * Sets an item clicked listener.
     */
    var onItemViewClickedListener: OnItemViewClickedListener? = null
        set(listener) {
            field = listener
            if (gridPresenter != null) {
                gridPresenter!!.onItemViewClickedListener = onItemViewClickedListener
            }
        }
    private var mSceneAfterEntranceTransition: Any? = null
    private var mSelectedPosition = -1
    private val mMainFragmentAdapter = object : BrowseFragment.MainFragmentAdapter<GridFragment>(this) {
        override fun setEntranceTransitionState(state: Boolean) {
            this@GridFragment.setEntranceTransitionState(state)
        }
    }

    private val mViewSelectedListener = OnItemViewSelectedListener { itemViewHolder, item, rowViewHolder, row ->
        val position = mGridViewHolder!!.gridView.selectedPosition
        if (DEBUG) Log.v(TAG, "grid selected position " + position)
        gridOnItemSelected(position)
        if (mOnItemViewSelectedListener != null) {
            mOnItemViewSelectedListener!!.onItemSelected(itemViewHolder, item,
                    rowViewHolder, row)
        }
    }

    private val mChildLaidOutListener = OnChildLaidOutListener { parent, view, position, id ->
        if (position == 0) {
            showOrHideTitle()
        }
    }

    /**
     * Sets an item selection listener.
     */
    fun setOnItemViewSelectedListener(listener: OnItemViewSelectedListener) {
        mOnItemViewSelectedListener = listener
    }

    private fun gridOnItemSelected(position: Int) {
        if (position != mSelectedPosition) {
            mSelectedPosition = position
            showOrHideTitle()
        }
    }

    private fun showOrHideTitle() {
        if (mGridViewHolder!!.gridView.findViewHolderForAdapterPosition(mSelectedPosition) == null) {
            return
        }
        if (!mGridViewHolder!!.gridView.hasPreviousViewInSameRow(mSelectedPosition)) {
            mMainFragmentAdapter.getFragmentHost().showTitleView(true)
        } else {
            mMainFragmentAdapter.getFragmentHost().showTitleView(false)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.grid_fragment, container, false)
    }

    @SuppressLint("RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val gridDock = view.findViewById(R.id.browse_grid_dock) as ViewGroup
        mGridViewHolder = gridPresenter!!.onCreateViewHolder(gridDock)
        gridDock.addView(mGridViewHolder!!.view)
        mGridViewHolder!!.gridView.setOnChildLaidOutListener(mChildLaidOutListener)

        mSceneAfterEntranceTransition = TransitionHelper.createScene(gridDock) { setEntranceTransitionState(true) }

        mainFragmentAdapter.fragmentHost.notifyViewCreated(mMainFragmentAdapter)
        updateAdapter()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        mGridViewHolder = null
    }

    override fun getMainFragmentAdapter(): BrowseFragment.MainFragmentAdapter<*> {
        return mMainFragmentAdapter
    }

    /**
     * Sets the selected item position.
     */
    fun setSelectedPosition(position: Int) {
        mSelectedPosition = position
        if (mGridViewHolder != null && mGridViewHolder!!.gridView.adapter != null) {
            mGridViewHolder!!.gridView.setSelectedPositionSmooth(position)
        }
    }

    private fun updateAdapter() {
        if (mGridViewHolder != null) {
            gridPresenter!!.onBindViewHolder(mGridViewHolder!!, adapter)
            if (mSelectedPosition != -1) {
                mGridViewHolder!!.gridView.selectedPosition = mSelectedPosition
            }
        }
    }

    internal fun setEntranceTransitionState(afterTransition: Boolean) {
        gridPresenter!!.setEntranceTransitionState(mGridViewHolder!!, afterTransition)
    }

    companion object {
        private val TAG = "VerticalGridFragment"
        private val DEBUG = false
    }
}
