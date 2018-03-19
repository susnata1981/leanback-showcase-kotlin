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

package android.support.v17.leanback.supportleanbackshowcase.app

import android.content.Intent
import android.os.Bundle
import android.support.v17.leanback.app.BrowseFragment
import android.support.v17.leanback.supportleanbackshowcase.R
import android.support.v17.leanback.supportleanbackshowcase.app.cards.CardExampleActivity
import android.support.v17.leanback.supportleanbackshowcase.app.details.DetailViewExampleActivity
import android.support.v17.leanback.supportleanbackshowcase.app.details.DetailViewExampleWithVideoBackgroundActivity
import android.support.v17.leanback.supportleanbackshowcase.app.dialog.DialogExampleActivity
import android.support.v17.leanback.supportleanbackshowcase.app.grid.GridExampleActivity
import android.support.v17.leanback.supportleanbackshowcase.app.grid.VideoGridExampleActivity
import android.support.v17.leanback.supportleanbackshowcase.app.media.MusicExampleActivity
import android.support.v17.leanback.supportleanbackshowcase.app.media.VideoExampleActivity
import android.support.v17.leanback.supportleanbackshowcase.app.media.VideoExampleWithExoPlayerActivity
import android.support.v17.leanback.supportleanbackshowcase.app.page.PageAndListRowActivity
import android.support.v17.leanback.supportleanbackshowcase.app.room.controller.overview.LiveDataRowsActivity
import android.support.v17.leanback.supportleanbackshowcase.app.rows.DynamicVideoRowsActivity
import android.support.v17.leanback.supportleanbackshowcase.app.settings.SettingsExampleActivity
import android.support.v17.leanback.supportleanbackshowcase.app.wizard.WizardExampleActivity
import android.support.v17.leanback.supportleanbackshowcase.cards.presenters.CardPresenterSelector
import android.support.v17.leanback.supportleanbackshowcase.models.Card
import android.support.v17.leanback.supportleanbackshowcase.models.CardRow
import android.support.v17.leanback.supportleanbackshowcase.models.Movie
import android.support.v17.leanback.supportleanbackshowcase.utils.Utils
import android.support.v17.leanback.widget.ArrayObjectAdapter
import android.support.v17.leanback.widget.ListRow
import android.support.v17.leanback.widget.ListRowPresenter
import android.support.v17.leanback.widget.OnItemViewClickedListener
import android.support.v17.leanback.widget.OnItemViewSelectedListener
import android.support.v17.leanback.widget.Presenter
import android.support.v17.leanback.widget.PresenterSelector
import android.support.v17.leanback.widget.Row
import android.support.v17.leanback.widget.RowPresenter
import android.support.v4.app.ActivityOptionsCompat

import com.google.gson.Gson


class MainFragment : BrowseFragment() {

    private var mRowsAdapter: ArrayObjectAdapter? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setupUIElements()
        setupRowAdapter()
        setupEventListeners()
    }

    private fun setupRowAdapter() {
        mRowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        createRows()
        adapter = mRowsAdapter
    }

    private fun createRows() {
        val json = Utils
                .inputStreamToString(resources.openRawResource(R.raw.launcher_cards))
        val rows = Gson().fromJson(json, Array<CardRow>::class.java)
        for (row in rows) {
            mRowsAdapter!!.add(createCardRow(row))
        }
    }

    private fun createCardRow(cardRow: CardRow): ListRow {
        val presenterSelector = CardPresenterSelector(activity)
        val listRowAdapter = ArrayObjectAdapter(presenterSelector)
        for (card in cardRow.cards!!) {
            listRowAdapter.add(card)
        }
        return ListRow(listRowAdapter)
    }

    private fun setupUIElements() {
        title = getString(R.string.browse_title)
        badgeDrawable = resources.getDrawable(R.drawable.title_android_tv, null)
        headersState = BrowseFragment.HEADERS_DISABLED
        isHeadersTransitionOnBackEnabled = false
        brandColor = resources.getColor(R.color.fastlane_background)
    }

    private fun setupEventListeners() {
        onItemViewClickedListener = ItemViewClickedListener()
//        onItemViewSelectedListener = ItemViewSelectedListener()
    }

    private inner class ItemViewClickedListener : OnItemViewClickedListener {

        override fun onItemClicked(itemViewHolder: Presenter.ViewHolder, item: Any,
                                   rowViewHolder: RowPresenter.ViewHolder, row: Row) {
            var intent: Intent? = null
            val card = item as Card
            val id = card.id
            when (id) {
                0 -> {
                    intent = Intent(activity.baseContext,
                            CardExampleActivity::class.java)
                }
                1 -> intent = Intent(activity.baseContext,
                        PageAndListRowActivity::class.java)
                2 -> {
                    intent = Intent(activity.baseContext,
                            GridExampleActivity::class.java)
                }
                3 -> {
                    intent = Intent(activity.baseContext,
                            VideoGridExampleActivity::class.java)
                }
                4 -> {
                    intent = Intent(activity.baseContext,
                            DetailViewExampleActivity::class.java)
                }
                5 -> {
                    intent = Intent(activity.baseContext,
                            DetailViewExampleWithVideoBackgroundActivity::class.java)
                }
                6 -> {
                    intent = Intent(activity.baseContext,
                            VideoExampleActivity::class.java)
                }
                7 -> {
                    intent = Intent(activity.baseContext,
                            VideoExampleWithExoPlayerActivity::class.java)
                }
                8 -> {
                    intent = Intent(activity.baseContext,
                            MusicExampleActivity::class.java)
                }
                9 -> {
                    // Let's create a new Wizard for a given Movie. The movie can come from any sort
                    // of data source. To simplify this example we decode it from a JSON source
                    // which might be loaded from a server in a real world example.
                    intent = Intent(activity.baseContext,
                            WizardExampleActivity::class.java)

                    // Prepare extras which contains the Movie and will be passed to the Activity
                    // which is started through the Intent/.
                    val extras = Bundle()
                    val json = Utils.inputStreamToString(
                            resources.openRawResource(R.raw.wizard_example))
                    val movie = Gson().fromJson(json, Movie::class.java)
                    extras.putSerializable("movie", movie)
                    intent.putExtras(extras)
                }// Finally, start the wizard Activity.
                10 -> {
                    intent = Intent(activity.baseContext,
                            SettingsExampleActivity::class.java)
                    startActivity(intent)
                    return
                }
                11 -> {
                    intent = Intent(activity.baseContext,
                            DialogExampleActivity::class.java)
                }
                12 -> {
                    intent = Intent(activity.baseContext,
                            DynamicVideoRowsActivity::class.java)
                    startActivity(intent)
                    return
                }
                13 -> {
                    intent = Intent(activity.baseContext,
                            LiveDataRowsActivity::class.java)
                    startActivity(intent)
                    return
                }
                else -> {
                }
            }
            if (intent != null) {
                val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(activity)
                        .toBundle()
                startActivity(intent, bundle)
            }
        }
    }

    private inner class ItemViewSelectedListener : OnItemViewSelectedListener {

        override fun onItemSelected(itemViewHolder: Presenter.ViewHolder, item: Any,
                                    rowViewHolder: RowPresenter.ViewHolder, row: Row) {

        }
    }
}
