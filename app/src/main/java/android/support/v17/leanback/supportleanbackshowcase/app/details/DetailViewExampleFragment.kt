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

package android.support.v17.leanback.supportleanbackshowcase.app.details

import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.support.v17.leanback.app.DetailsFragment
import android.support.v17.leanback.app.DetailsFragmentBackgroundController
import android.support.v17.leanback.supportleanbackshowcase.R
import android.support.v17.leanback.supportleanbackshowcase.cards.presenters.CardPresenterSelector
import android.support.v17.leanback.supportleanbackshowcase.models.Card
import android.support.v17.leanback.supportleanbackshowcase.models.DetailedCard
import android.support.v17.leanback.supportleanbackshowcase.utils.CardListRow
import android.support.v17.leanback.supportleanbackshowcase.utils.Utils
import android.support.v17.leanback.widget.Action
import android.support.v17.leanback.widget.ArrayObjectAdapter
import android.support.v17.leanback.widget.ClassPresenterSelector
import android.support.v17.leanback.widget.DetailsOverviewRow
import android.support.v17.leanback.widget.FullWidthDetailsOverviewRowPresenter
import android.support.v17.leanback.widget.FullWidthDetailsOverviewSharedElementHelper
import android.support.v17.leanback.widget.HeaderItem
import android.support.v17.leanback.widget.ListRow
import android.support.v17.leanback.widget.ListRowPresenter
import android.support.v17.leanback.widget.OnItemViewClickedListener
import android.support.v17.leanback.widget.OnItemViewSelectedListener
import android.support.v17.leanback.widget.Presenter
import android.support.v17.leanback.widget.Row
import android.support.v17.leanback.widget.RowPresenter
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import com.google.gson.Gson
import kotlin.math.log

/**
 * Displays a card with more details using a [DetailsFragment].
 */
class DetailViewExampleFragment : DetailsFragment(), OnItemViewClickedListener, OnItemViewSelectedListener {

    private var mActionBuy: Action? = null
    private var mActionWishList: Action? = null
    private var mActionRelated: Action? = null
    private var mRowsAdapter: ArrayObjectAdapter? = null
    private val mDetailsBackground = DetailsFragmentBackgroundController(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupUi()
        setupEventListeners()
    }

    private fun setupUi() {
        // Load the card we want to display from a JSON resource. This JSON data could come from
        // anywhere in a real world app, e.g. a server.
        val json = Utils
                .inputStreamToString(resources.openRawResource(R.raw.detail_example))
        val data = Gson().fromJson(json, DetailedCard::class.java)

        // Setup fragment
        title = getString(R.string.detail_view_title)

        val rowPresenter = object : FullWidthDetailsOverviewRowPresenter(
                DetailsDescriptionPresenter(activity)) {

            override fun createRowViewHolder(parent: ViewGroup): RowPresenter.ViewHolder {
                // Customize Actionbar and Content by using custom colors.
                val viewHolder = super.createRowViewHolder(parent)

                val actionsView = viewHolder.view.findViewById(R.id.details_overview_actions_background) as View
                actionsView.setBackgroundColor(activity.resources.getColor(R.color.detail_view_actionbar_background))

                val detailsView = viewHolder.view.findViewById(R.id.details_frame) as View
                detailsView.setBackgroundColor(
                        resources.getColor(R.color.detail_view_background))
                return viewHolder
            }
        }

        val mHelper = FullWidthDetailsOverviewSharedElementHelper()
        mHelper.setSharedElementEnterTransition(activity, TRANSITION_NAME)
        rowPresenter.setListener(mHelper)
        rowPresenter.isParticipatingEntranceTransition = false
        prepareEntranceTransition()

        val shadowDisabledRowPresenter = ListRowPresenter()
        shadowDisabledRowPresenter.shadowEnabled = false

        // Setup PresenterSelector to distinguish between the different rows.
        val rowPresenterSelector = ClassPresenterSelector()
        rowPresenterSelector.addClassPresenter(DetailsOverviewRow::class.java, rowPresenter)
        rowPresenterSelector.addClassPresenter(CardListRow::class.java, shadowDisabledRowPresenter)
        rowPresenterSelector.addClassPresenter(ListRow::class.java, ListRowPresenter())
        mRowsAdapter = ArrayObjectAdapter(rowPresenterSelector)

        // Setup action and detail row.
        val detailsOverview = DetailsOverviewRow(data)
        var imageResId = data.getLocalImageResourceId(activity)

        val extras = activity.intent.extras
        if (extras != null && extras.containsKey(EXTRA_CARD)) {
            imageResId = extras.getInt(EXTRA_CARD, imageResId)
        }
        detailsOverview.imageDrawable = resources.getDrawable(imageResId, null)
        val actionAdapter = ArrayObjectAdapter()

        mActionBuy = Action(ACTION_BUY, getString(R.string.action_buy) + data.price!!)
        mActionWishList = Action(ACTION_WISHLIST, getString(R.string.action_wishlist))
        mActionRelated = Action(ACTION_RELATED, getString(R.string.action_related))

        actionAdapter.add(mActionBuy)
        actionAdapter.add(mActionWishList)
        actionAdapter.add(mActionRelated)
        detailsOverview.actionsAdapter = actionAdapter
        mRowsAdapter!!.add(detailsOverview)

        // Setup related row.
        var listRowAdapter = ArrayObjectAdapter(
                CardPresenterSelector(activity))
        for (characterCard in data.characters!!) listRowAdapter.add(characterCard)
        var header = HeaderItem(0, getString(R.string.header_related))
        mRowsAdapter!!.add(CardListRow(header, listRowAdapter, null))

        // Setup recommended row.
        listRowAdapter = ArrayObjectAdapter(CardPresenterSelector(activity))
        for (card in data.recommended!!) listRowAdapter.add(card)
        header = HeaderItem(1, getString(R.string.header_recommended))
        mRowsAdapter!!.add(ListRow(header, listRowAdapter))

        adapter = mRowsAdapter!!
        Handler().postDelayed({ startEntranceTransition() }, 500)
        initializeBackground(data)
    }

    private fun initializeBackground(data: DetailedCard) {
        mDetailsBackground.enableParallax()
        mDetailsBackground.coverBitmap = BitmapFactory.decodeResource(resources,
                R.drawable.background_canyon)
    }

    private fun setupEventListeners() {
        setOnItemViewSelectedListener(this)
        onItemViewClickedListener = this
    }

    override fun onItemClicked(itemViewHolder: Presenter.ViewHolder, item: Any,
                               rowViewHolder: RowPresenter.ViewHolder, row: Row) {
        if (item !is Action) return

        if (item.id == ACTION_RELATED) {
            setSelectedPosition(1)
        } else {
            Toast.makeText(activity, getString(R.string.action_cicked), Toast.LENGTH_LONG)
                    .show()
        }
    }

    override fun onItemSelected(itemViewHolder: Presenter.ViewHolder?, item: Any?,
                                rowViewHolder: RowPresenter.ViewHolder, row: Row) {

        if (mRowsAdapter!!.indexOf(row) > 0) {
            val backgroundColor = resources.getColor(R.color.detail_view_related_background)
            view!!.setBackgroundColor(backgroundColor)
        } else {
            view!!.background = null
        }
    }

    companion object {

        val TRANSITION_NAME = "t_for_transition"
        val EXTRA_CARD = "card"

        private val ACTION_BUY: Long = 1
        private val ACTION_WISHLIST: Long = 2
        private val ACTION_RELATED: Long = 3
    }
}
