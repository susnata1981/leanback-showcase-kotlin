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

package android.support.v17.leanback.supportleanbackshowcase.app.grid

import android.os.Bundle
import android.os.Handler
import android.support.v17.leanback.app.VerticalGridFragment
import android.support.v17.leanback.supportleanbackshowcase.R
import android.support.v17.leanback.supportleanbackshowcase.cards.presenters.CardPresenterSelector
import android.support.v17.leanback.supportleanbackshowcase.models.CardRow
import android.support.v17.leanback.supportleanbackshowcase.utils.Utils
import android.support.v17.leanback.widget.ArrayObjectAdapter
import android.support.v17.leanback.widget.FocusHighlight
import android.support.v17.leanback.widget.PresenterSelector
import android.support.v17.leanback.widget.VerticalGridPresenter

import com.google.gson.Gson

/**
 * An example how to use leanback's [VerticalGridFragment].
 */
class GridExampleFragment : VerticalGridFragment() {

    private var mAdapter: ArrayObjectAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getString(R.string.grid_example_title)
        setupRowAdapter()
    }

    private fun setupRowAdapter() {
        val gridPresenter = VerticalGridPresenter(ZOOM_FACTOR)
        gridPresenter.numberOfColumns = COLUMNS
        setGridPresenter(gridPresenter)

        val cardPresenterSelector = CardPresenterSelector(activity)
        mAdapter = ArrayObjectAdapter(cardPresenterSelector)
        adapter = mAdapter

        prepareEntranceTransition()
        Handler().postDelayed({
            createRows()
            startEntranceTransition()
        }, 1000)
    }

    private fun createRows() {
        val json = Utils.inputStreamToString(resources
                .openRawResource(R.raw.grid_example))
        val row = Gson().fromJson(json, CardRow::class.java)
        mAdapter!!.addAll(0, row.cards!!)
    }

    companion object {

        private val COLUMNS = 4
        private val ZOOM_FACTOR = FocusHighlight.ZOOM_FACTOR_MEDIUM
    }
}
