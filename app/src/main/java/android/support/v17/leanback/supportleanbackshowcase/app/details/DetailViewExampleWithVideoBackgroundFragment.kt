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

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v17.leanback.app.DetailsFragment
import android.support.v17.leanback.app.DetailsFragmentBackgroundController
import android.support.v17.leanback.media.MediaPlayerAdapter
import android.support.v17.leanback.media.MediaPlayerGlue
import android.support.v17.leanback.supportleanbackshowcase.R
import android.support.v17.leanback.supportleanbackshowcase.R.id.details_overview_actions_background
import android.support.v17.leanback.supportleanbackshowcase.app.media.PlaybackSeekDiskDataProvider
import android.support.v17.leanback.supportleanbackshowcase.app.media.VideoMediaPlayerGlue
import android.support.v17.leanback.supportleanbackshowcase.app.wizard.WizardExampleActivity
import android.support.v17.leanback.supportleanbackshowcase.cards.presenters.CardPresenterSelector
import android.support.v17.leanback.supportleanbackshowcase.models.Card
import android.support.v17.leanback.supportleanbackshowcase.models.DetailedCard
import android.support.v17.leanback.supportleanbackshowcase.models.Movie
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
import android.support.v4.app.ActivityOptionsCompat
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import com.google.gson.Gson

/**
 * Displays a card with more details using a [DetailsFragment].
 */
class DetailViewExampleWithVideoBackgroundFragment : DetailsFragment(), OnItemViewClickedListener, OnItemViewSelectedListener {

    private var mActionPlay: Action? = null
    private var mActionRent: Action? = null
    private var mActionWishList: Action? = null
    private var mActionRelated: Action? = null
    private var mRowsAdapter: ArrayObjectAdapter? = null
    private var data: DetailedCard? = null
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
        data = Gson().fromJson(json, DetailedCard::class.java)

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
        var imageResId = data!!.getLocalImageResourceId(activity)

        val extras = activity.intent.extras
        if (extras != null && extras.containsKey(EXTRA_CARD)) {
            imageResId = extras.getInt(EXTRA_CARD, imageResId)
        }
        detailsOverview.imageDrawable = resources.getDrawable(imageResId, null)
        val actionAdapter = ArrayObjectAdapter()

        mActionPlay = Action(ACTION_PLAY, getString(R.string.action_play))
        mActionRent = Action(ACTION_RENT, getString(R.string.action_rent))
        mActionWishList = Action(ACTION_WISHLIST, getString(R.string.action_wishlist))
        mActionRelated = Action(ACTION_RELATED, getString(R.string.action_related))

        actionAdapter.add(mActionRent)
        actionAdapter.add(mActionWishList)
        actionAdapter.add(mActionRelated)
        detailsOverview.actionsAdapter = actionAdapter
        mRowsAdapter!!.add(detailsOverview)

        // Setup related row.
        var listRowAdapter = ArrayObjectAdapter(
                CardPresenterSelector(activity))
        for (characterCard in data!!.characters!!) listRowAdapter.add(characterCard)
        var header = HeaderItem(0, getString(R.string.header_related))
        mRowsAdapter!!.add(CardListRow(header, listRowAdapter, null))

        // Setup recommended row.
        listRowAdapter = ArrayObjectAdapter(CardPresenterSelector(activity))
        for (card in data!!.recommended!!) listRowAdapter.add(card)
        header = HeaderItem(1, getString(R.string.header_recommended))
        mRowsAdapter!!.add(ListRow(header, listRowAdapter))

        adapter = mRowsAdapter!!
        Handler().postDelayed({ startEntranceTransition() }, 500)
        initializeBackground()
    }

    private fun initializeBackground() {
        mDetailsBackground.enableParallax()

        val playerGlue = MediaPlayerGlue(activity)
        mDetailsBackground.setupVideoPlayback(playerGlue)

        playerGlue.setTitle(data!!.title + " (Trailer)")
        playerGlue.setArtist(data!!.description)
        playerGlue.setVideoUrl(data!!.trailerUrl)
    }

    private fun playMainVideoOnBackground() {
        val playerGlue = VideoMediaPlayerGlue(
                activity, MediaPlayerAdapter(activity))

        mDetailsBackground.setupVideoPlayback(playerGlue)
        playerGlue.title = data!!.title + " (Main Video)"
        playerGlue.subtitle = data!!.description
        playerGlue.playerAdapter.setDataSource(Uri.parse(data!!.videoUrl))
        PlaybackSeekDiskDataProvider.setDemoSeekProvider(playerGlue)

        mDetailsBackground.switchToVideo()
    }

    private fun setupEventListeners() {
        setOnItemViewSelectedListener(this)
        onItemViewClickedListener = this
    }

    private fun startWizardActivityForPayment() {
        val intent = Intent(activity,
                WizardExampleActivity::class.java)

        // Prepare extras which contains the Movie and will be passed to the Activity
        // which is started through the Intent.
        val extras = Bundle()
        val json = Utils.inputStreamToString(
                resources.openRawResource(R.raw.wizard_example))
        val movie = Gson().fromJson(json, Movie::class.java)
        extras.putSerializable("movie", movie)
        intent.putExtras(extras)


        val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(activity)
                .toBundle()
        startActivityForResult(intent,
                DetailViewExampleWithVideoBackgroundActivity.BUY_MOVIE_REQUEST, bundle)
    }

    override fun onItemClicked(itemViewHolder: Presenter.ViewHolder, item: Any,
                               rowViewHolder: RowPresenter.ViewHolder, row: Row) {
        if (item !is Action) return
        val id = item.id

        if (id == ACTION_RENT) {
            startWizardActivityForPayment()
        } else if (item.id == ACTION_PLAY) {
            playMainVideoOnBackground()
        } else if (item.id == ACTION_RELATED) {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, returnIntent: Intent) {
        if (requestCode == DetailViewExampleWithVideoBackgroundActivity.BUY_MOVIE_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                val actionAdapter = (adapter.get(0) as DetailsOverviewRow).actionsAdapter as ArrayObjectAdapter

                actionAdapter.add(0, mActionPlay)
                actionAdapter.remove(mActionRent)
                title = title.toString() + " (Owned)"

                val watchNow = returnIntent
                        .getBooleanExtra(WizardExampleActivity.WATCH_NOW,
                                false)

                if (watchNow) {
                    // Leave a delay for playing the video in order to focus on the video fragment
                    // after coming back from Wizard activity
                    Handler().postDelayed({ playMainVideoOnBackground() }, 500)
                }
            }
        }
    }

    companion object {

        val TRANSITION_NAME = "t_for_transition"
        val EXTRA_CARD = "card"

        private val ACTION_PLAY: Long = 1
        private val ACTION_RENT: Long = 2
        private val ACTION_WISHLIST: Long = 3
        private val ACTION_RELATED: Long = 4
    }
}

