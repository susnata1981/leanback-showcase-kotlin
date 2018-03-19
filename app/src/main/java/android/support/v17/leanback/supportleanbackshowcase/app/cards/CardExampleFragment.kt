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

package android.support.v17.leanback.supportleanbackshowcase.app.cards

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v17.leanback.app.BrowseFragment
import android.support.v17.leanback.supportleanbackshowcase.R
import android.support.v17.leanback.supportleanbackshowcase.app.details.DetailViewExampleActivity
import android.support.v17.leanback.supportleanbackshowcase.app.details.DetailViewExampleFragment
import android.support.v17.leanback.supportleanbackshowcase.app.details.ShadowRowPresenterSelector
import android.support.v17.leanback.supportleanbackshowcase.cards.presenters.CardPresenterSelector
import android.support.v17.leanback.supportleanbackshowcase.models.Card
import android.support.v17.leanback.supportleanbackshowcase.models.CardRow
import android.support.v17.leanback.supportleanbackshowcase.utils.CardListRow
import android.support.v17.leanback.supportleanbackshowcase.utils.Utils
import android.support.v17.leanback.widget.ArrayObjectAdapter
import android.support.v17.leanback.widget.DividerRow
import android.support.v17.leanback.widget.HeaderItem
import android.support.v17.leanback.widget.ImageCardView
import android.support.v17.leanback.widget.OnItemViewClickedListener
import android.support.v17.leanback.widget.Presenter
import android.support.v17.leanback.widget.PresenterSelector
import android.support.v17.leanback.widget.Row
import android.support.v17.leanback.widget.RowPresenter
import android.support.v17.leanback.widget.SectionRow
import android.support.v4.app.ActivityOptionsCompat
import android.view.View
import android.widget.ImageView
import android.widget.Toast

import com.google.gson.Gson

/**
 * This fragment will be shown when the "Card Examples" card is selected at the home menu. It will
 * display multiple card types.
 */
class CardExampleFragment : BrowseFragment() {

    private var mRowsAdapter: ArrayObjectAdapter? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupUi()
        setupRowAdapter()
    }

    private fun setupUi() {
        headersState = BrowseFragment.HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true
        title = getString(R.string.card_examples_title)
        setOnSearchClickedListener {
            Toast.makeText(activity, getString(R.string.implement_search),
                    Toast.LENGTH_LONG).show()
        }
        onItemViewClickedListener = OnItemViewClickedListener { viewHolder, item, viewHolder1, row ->
            if (item !is Card) return@OnItemViewClickedListener
            if (viewHolder.view !is ImageCardView) return@OnItemViewClickedListener

            val imageView = (viewHolder.view as ImageCardView).mainImageView
            val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
                    imageView, DetailViewExampleFragment.TRANSITION_NAME).toBundle()
            val intent = Intent(activity.baseContext,
                    DetailViewExampleActivity::class.java)
            val imageResId = item.getLocalImageResourceId(context)
            intent.putExtra(DetailViewExampleFragment.EXTRA_CARD, imageResId)
            startActivity(intent, bundle)
        }

        prepareEntranceTransition()
    }

    private fun setupRowAdapter() {
        mRowsAdapter = ArrayObjectAdapter(ShadowRowPresenterSelector())
        adapter = mRowsAdapter
        Handler().postDelayed({
            createRows()
            startEntranceTransition()
        }, 500)
    }

    private fun createRows() {
        val json = Utils
                .inputStreamToString(resources.openRawResource(R.raw.cards_example))
        val rows = Gson().fromJson(json, Array<CardRow>::class.java)
        for (row in rows) {
            mRowsAdapter!!.add(createCardRow(row))
        }
    }

    private fun createCardRow(cardRow: CardRow): Row {
        when (cardRow.type) {
            CardRow.TYPE_SECTION_HEADER -> return SectionRow(HeaderItem(cardRow.title))
            CardRow.TYPE_DIVIDER -> return DividerRow()
            CardRow.TYPE_DEFAULT -> {
                // Build main row using the ImageCardViewPresenter.
                val presenterSelector = CardPresenterSelector(activity)
                val listRowAdapter = ArrayObjectAdapter(presenterSelector)
                for (card in cardRow.cards!!) {
                    listRowAdapter.add(card)
                }
                return CardListRow(HeaderItem(cardRow.title), listRowAdapter, cardRow)
            }
            else -> {
                val presenterSelector = CardPresenterSelector(activity)
                val listRowAdapter = ArrayObjectAdapter(presenterSelector)
                for (card in cardRow.cards!!) {
                    listRowAdapter.add(card)
                }
                return CardListRow(HeaderItem(cardRow.title), listRowAdapter, cardRow)
            }
        }
    }

}
