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
 *
 */

package android.support.v17.leanback.supportleanbackshowcase.app.details

import android.content.Context
import android.support.v17.leanback.supportleanbackshowcase.R
import android.support.v17.leanback.supportleanbackshowcase.models.DetailedCard
import android.support.v17.leanback.supportleanbackshowcase.utils.ResourceCache
import android.support.v17.leanback.widget.Presenter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import javax.inject.Inject

/**
 * This presenter is used to render a [DetailedCard] in the [ ].
 */
class DetailsDescriptionPresenter @Inject
constructor(private val mContext: Context) : Presenter() {

    private val mResourceCache = ResourceCache()

    override fun onCreateViewHolder(parent: ViewGroup): Presenter.ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.detail_view_content, null)
        return Presenter.ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: Presenter.ViewHolder, item: Any) {
        val primaryText = mResourceCache.getViewById(viewHolder.view, R.id.primary_text) as TextView
        val sndText1 = mResourceCache.getViewById(viewHolder.view, R.id.secondary_text_first) as TextView
        val sndText2 = mResourceCache.getViewById(viewHolder.view, R.id.secondary_text_second) as TextView
        val extraText = mResourceCache.getViewById(viewHolder.view, R.id.extra_text) as TextView

        val card = item as DetailedCard
        primaryText.setText(card.title)
        sndText1.setText(card.description)
        sndText2.setText(card.year.toString() + "")
        extraText.setText(card.text)
    }

    override fun onUnbindViewHolder(viewHolder: Presenter.ViewHolder) {
        // Nothing to do here.
    }
}
