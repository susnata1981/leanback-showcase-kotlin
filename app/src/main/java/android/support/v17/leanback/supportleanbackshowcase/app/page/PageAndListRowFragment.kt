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

import android.app.Activity
import android.app.Fragment
import android.os.Bundle
import android.os.Handler
import android.support.v17.leanback.app.BackgroundManager
import android.support.v17.leanback.app.BrowseFragment
import android.support.v17.leanback.app.RowsFragment
import android.support.v17.leanback.supportleanbackshowcase.R
import android.support.v17.leanback.supportleanbackshowcase.app.details.ShadowRowPresenterSelector
import android.support.v17.leanback.supportleanbackshowcase.cards.presenters.CardPresenterSelector
import android.support.v17.leanback.supportleanbackshowcase.models.Card
import android.support.v17.leanback.supportleanbackshowcase.models.CardRow
import android.support.v17.leanback.supportleanbackshowcase.utils.CardListRow
import android.support.v17.leanback.supportleanbackshowcase.utils.Utils
import android.support.v17.leanback.widget.ArrayObjectAdapter
import android.support.v17.leanback.widget.FocusHighlight
import android.support.v17.leanback.widget.HeaderItem
import android.support.v17.leanback.widget.ListRow
import android.support.v17.leanback.widget.ListRowPresenter
import android.support.v17.leanback.widget.OnItemViewClickedListener
import android.support.v17.leanback.widget.PageRow
import android.support.v17.leanback.widget.Presenter
import android.support.v17.leanback.widget.PresenterSelector
import android.support.v17.leanback.widget.Row
import android.support.v17.leanback.widget.RowPresenter
import android.support.v17.leanback.widget.VerticalGridPresenter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.Toast

import com.google.gson.Gson

/**
 * Sample [BrowseFragment] implementation showcasing the use of [PageRow] and
 * [ListRow].
 */
class PageAndListRowFragment : BrowseFragment() {
    private var mBackgroundManager: BackgroundManager? = null

    private var mRowsAdapter: ArrayObjectAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupUi()
        loadData()
        mBackgroundManager = BackgroundManager.getInstance(activity)
        mBackgroundManager!!.attach(activity.window)
        mainFragmentRegistry.registerFragment(PageRow::class.java,
                PageRowFragmentFactory(mBackgroundManager!!))
    }

    private fun setupUi() {
        headersState = BrowseFragment.HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true
        brandColor = resources.getColor(R.color.fastlane_background)
        title = "Title goes here"
        setOnSearchClickedListener {
            Toast.makeText(
                    activity, getString(R.string.implement_search), Toast.LENGTH_SHORT)
                    .show()
        }

        prepareEntranceTransition()
    }

    private fun loadData() {
        mRowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        adapter = mRowsAdapter

        Handler().postDelayed({
            createRows()
            startEntranceTransition()
        }, 2000)
    }

    private fun createRows() {
        val headerItem1 = HeaderItem(HEADER_ID_1, HEADER_NAME_1)
        val pageRow1 = PageRow(headerItem1)
        mRowsAdapter!!.add(pageRow1)

        val headerItem2 = HeaderItem(HEADER_ID_2, HEADER_NAME_2)
        val pageRow2 = PageRow(headerItem2)
        mRowsAdapter!!.add(pageRow2)

        val headerItem3 = HeaderItem(HEADER_ID_3, HEADER_NAME_3)
        val pageRow3 = PageRow(headerItem3)
        mRowsAdapter!!.add(pageRow3)

        val headerItem4 = HeaderItem(HEADER_ID_4, HEADER_NAME_4)
        val pageRow4 = PageRow(headerItem4)
        mRowsAdapter!!.add(pageRow4)
    }

    private class PageRowFragmentFactory internal constructor(private val mBackgroundManager: BackgroundManager) : BrowseFragment.FragmentFactory<Fragment>() {

        override fun createFragment(rowObj: Any): Fragment {
            val row = rowObj as Row
            mBackgroundManager.drawable = null
            if (row.headerItem.id == HEADER_ID_1) {
                return SampleFragmentA()
            } else if (row.headerItem.id == HEADER_ID_2) {
                return SampleFragmentB()
            } else if (row.headerItem.id == HEADER_ID_3) {
                return SettingsFragment()
            } else if (row.headerItem.id == HEADER_ID_4) {
                return WebViewFragment()
            }

            throw IllegalArgumentException(String.format("Invalid row %s", rowObj))
        }
    }

    class PageFragmentAdapterImpl(fragment: SampleFragmentA) : BrowseFragment.MainFragmentAdapter<SampleFragmentA>(fragment)

    /**
     * Simple page fragment implementation.
     */
    class SampleFragmentA : GridFragment() {
        private val ZOOM_FACTOR = FocusHighlight.ZOOM_FACTOR_SMALL
        private var mAdapter: ArrayObjectAdapter? = null

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setupAdapter()
            loadData()
            mainFragmentAdapter.fragmentHost.notifyDataReady(mainFragmentAdapter)
        }


        private fun setupAdapter() {
            val presenter = VerticalGridPresenter(ZOOM_FACTOR)
            presenter.numberOfColumns = COLUMNS
            gridPresenter = presenter

            val cardPresenter = CardPresenterSelector(activity)
            mAdapter = ArrayObjectAdapter(cardPresenter)
            adapter = mAdapter

            onItemViewClickedListener = OnItemViewClickedListener { itemViewHolder, item, rowViewHolder, row ->
                val card = item as Card
                Toast.makeText(activity,
                        "Clicked on " + card.title,
                        Toast.LENGTH_SHORT).show()
            }
        }

        private fun loadData() {
            val json = Utils.inputStreamToString(resources.openRawResource(
                    R.raw.grid_example))
            val cardRow = Gson().fromJson(json, CardRow::class.java)
            mAdapter!!.addAll(0, cardRow.cards!!)
        }

        companion object {
            private val COLUMNS = 4
        }
    }

    /**
     * Page fragment embeds a rows fragment.
     */
    class SampleFragmentB : RowsFragment() {
        private val mRowsAdapter: ArrayObjectAdapter

        init {
            mRowsAdapter = ArrayObjectAdapter(ShadowRowPresenterSelector())

            adapter = mRowsAdapter
            onItemViewClickedListener = OnItemViewClickedListener { itemViewHolder, item, rowViewHolder, row ->
                Toast.makeText(activity, "Implement click handler", Toast.LENGTH_SHORT)
                        .show()
            }
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            createRows()
            mainFragmentAdapter.fragmentHost.notifyDataReady(mainFragmentAdapter)
        }

        private fun createRows() {
            val json = Utils.inputStreamToString(resources.openRawResource(
                    R.raw.page_row_example))
            val rows = Gson().fromJson(json, Array<CardRow>::class.java)
            for (row in rows) {
                if (row.type == CardRow.TYPE_DEFAULT) {
                    mRowsAdapter.add(createCardRow(row))
                }
            }
        }

        private fun createCardRow(cardRow: CardRow): Row {
            val presenterSelector = CardPresenterSelector(activity)
            val adapter = ArrayObjectAdapter(presenterSelector)
            for (card in cardRow.cards!!) {
                adapter.add(card)
            }

            val headerItem = HeaderItem(cardRow.title)
            return CardListRow(headerItem, adapter, cardRow)
        }
    }

    class SettingsFragment : RowsFragment() {
        private val mRowsAdapter: ArrayObjectAdapter

        init {
            val selector = ListRowPresenter()
            selector.setNumRows(2)
            mRowsAdapter = ArrayObjectAdapter(selector)
            adapter = mRowsAdapter
        }

        override fun onAttach(activity: Activity) {
            super.onAttach(activity)
            Handler().postDelayed({ loadData() }, 200)
        }

        private fun loadData() {
            if (isAdded) {
                val json = Utils.inputStreamToString(resources.openRawResource(
                        R.raw.icon_example))
                val cardRow = Gson().fromJson(json, CardRow::class.java)
                mRowsAdapter.add(createCardRow(cardRow))
                mainFragmentAdapter.fragmentHost.notifyDataReady(
                        mainFragmentAdapter)
            }
        }

        private fun createCardRow(cardRow: CardRow): ListRow {
            val iconCardPresenter = SettingsIconPresenter(activity)
            val adapter = ArrayObjectAdapter(iconCardPresenter)
            for (card in cardRow.cards!!) {
                adapter.add(card)
            }

            val headerItem = HeaderItem(cardRow.title)
            return CardListRow(headerItem, adapter, cardRow)
        }
    }

    class WebViewFragment : Fragment(), BrowseFragment.MainFragmentAdapterProvider {
        private val mMainFragmentAdapter = BrowseFragment.MainFragmentAdapter(this)
        private var mWebview: WebView? = null

        override fun getMainFragmentAdapter(): BrowseFragment.MainFragmentAdapter<*> {
            return mMainFragmentAdapter
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            mainFragmentAdapter.fragmentHost.showTitleView(false)
        }

        override fun onCreateView(
                inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val root = FrameLayout(activity)
            val lp = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT)
            lp.marginStart = 32
            mWebview = WebView(activity)
            mWebview!!.webViewClient = WebViewClient()
            mWebview!!.settings.javaScriptEnabled = true
            root.addView(mWebview, lp)
            return root
        }

        override fun onResume() {
            super.onResume()
            mWebview!!.loadUrl("https://www.google.com/policies/terms")
            mainFragmentAdapter.fragmentHost.notifyDataReady(mainFragmentAdapter)
        }
    }

    companion object {
        private val HEADER_ID_1: Long = 1
        private val HEADER_NAME_1 = "Page Fragment"
        private val HEADER_ID_2: Long = 2
        private val HEADER_NAME_2 = "Rows Fragment"
        private val HEADER_ID_3: Long = 3
        private val HEADER_NAME_3 = "Settings Fragment"
        private val HEADER_ID_4: Long = 4
        private val HEADER_NAME_4 = "User agreement Fragment"
    }
}
