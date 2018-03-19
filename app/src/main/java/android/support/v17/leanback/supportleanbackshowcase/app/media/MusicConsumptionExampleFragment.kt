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

package android.support.v17.leanback.supportleanbackshowcase.app.media

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.support.v17.leanback.app.PlaybackFragment
import android.support.v17.leanback.app.PlaybackFragmentGlueHost
import android.support.v17.leanback.supportleanbackshowcase.R
import android.support.v17.leanback.supportleanbackshowcase.models.Song
import android.support.v17.leanback.supportleanbackshowcase.models.SongList
import android.support.v17.leanback.supportleanbackshowcase.utils.Constants
import android.support.v17.leanback.supportleanbackshowcase.utils.Utils
import android.support.v17.leanback.widget.AbstractMediaItemPresenter
import android.support.v17.leanback.widget.AbstractMediaListHeaderPresenter
import android.support.v17.leanback.widget.ArrayObjectAdapter
import android.support.v17.leanback.widget.BaseOnItemViewClickedListener
import android.support.v17.leanback.widget.ClassPresenterSelector
import android.support.v17.leanback.widget.MultiActionsProvider
import android.support.v17.leanback.widget.Presenter
import android.support.v17.leanback.widget.PresenterSelector
import android.support.v17.leanback.widget.RowPresenter
import android.util.Log
import android.widget.TextView

import com.google.gson.Gson

import java.util.ArrayList

/**
 * This example shows how to play music files and build a simple track list.
 */
class MusicConsumptionExampleFragment : PlaybackFragment(), BaseOnItemViewClickedListener<Any> {
    private var mRowsAdapter: ArrayObjectAdapter? = null
    private var mGlue: MusicMediaPlayerGlue? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Constants.LOCAL_LOGD) Log.d(TAG, "onCreate")

        mGlue = MusicMediaPlayerGlue(activity)
        mGlue!!.host = PlaybackFragmentGlueHost(this)

        val json = Utils.inputStreamToString(
                resources.openRawResource(R.raw.music_consumption_example))


        val songList = Gson().fromJson(json, SongList::class.java).songs

        val res = activity.resources

        // For each song add a playlist and favorite actions.
        for (song in songList!!) {
            val mediaRowActions = arrayOfNulls<MultiActionsProvider.MultiAction>(2)
            val playlistAction = MultiActionsProvider.MultiAction(PLAYLIST_ACTION_ID.toLong())
            val playlistActionDrawables = arrayOf(res.getDrawable(R.drawable.ic_playlist_add_white_24dp,
                    activity.theme), res.getDrawable(R.drawable.ic_playlist_add_filled_24dp,
                    activity.theme))
            playlistAction.drawables = playlistActionDrawables
            mediaRowActions[0] = playlistAction

            val favoriteAction = MultiActionsProvider.MultiAction(FAVORITE_ACTION_ID.toLong())
            val favoriteActionDrawables = arrayOf(res.getDrawable(R.drawable.ic_favorite_border_white_24dp,
                    activity.theme), res.getDrawable(R.drawable.ic_favorite_filled_24dp,
                    activity.theme))
            favoriteAction.drawables = favoriteActionDrawables
            mediaRowActions[1] = favoriteAction
            song.mediaRowActions = mediaRowActions
        }

        val songMetaDataList = ArrayList<MediaMetaData>()
        val songUriList = ArrayList<Uri>()
        for (song in songList) {
            val metaData = createMetaDataFromSong(song)
            songMetaDataList.add(metaData)
        }
        mGlue!!.setMediaMetaDataList(songMetaDataList)
        addPlaybackControlsRow(songList)
        mGlue!!.prepareAndPlay(getUri(songList[0]))
    }

    override fun onDestroy() {
        super.onDestroy()
        mGlue!!.close()
    }

    override fun onPause() {
        super.onPause()
        Log.d("MusicService", "onPause called.")

    }

    override fun onStart() {
        super.onStart()
        Log.d("MusicService", "onStart called.")
        mGlue!!.openServiceCallback()
    }

    override fun onStop() {
        Log.d("MusicService", "onStop called.")
        super.onStop()
        mGlue!!.enableProgressUpdating(false)
        mGlue!!.releaseServiceCallback()
    }

    internal class SongPresenter : AbstractMediaItemPresenter {

        constructor() : super() {}

        constructor(context: Context, themeResId: Int) : super(themeResId) {
            setHasMediaRowSeparator(true)
        }

        override fun onBindMediaDetails(vh: AbstractMediaItemPresenter.ViewHolder, item: Any) {

            val favoriteTextColor = vh.view.context.resources.getColor(
                    R.color.song_row_favorite_color)
            val song = item as Song
            if (song.number == 1 && firstRowView == null) {
                firstRowView = vh.mediaItemNameView
            }
            vh.mediaItemNumberView.text = "" + song.number

            val songTitle = song.title + " / " + song.description
            vh.mediaItemNameView.text = songTitle

            vh.mediaItemDurationView.text = "" + song.duration!!

            if (song.isFavorite) {
                vh.mediaItemNumberView.setTextColor(favoriteTextColor)
                vh.mediaItemNameView.setTextColor(favoriteTextColor)
                vh.mediaItemDurationView.setTextColor(favoriteTextColor)
            } else {
                val context = vh.mediaItemNumberView.context
                vh.mediaItemNumberView.setTextAppearance(context,
                        R.style.TextAppearance_Leanback_PlaybackMediaItemNumber)
                vh.mediaItemNameView.setTextAppearance(context,
                        R.style.TextAppearance_Leanback_PlaybackMediaItemName)
                vh.mediaItemDurationView.setTextAppearance(context,
                        R.style.TextAppearance_Leanback_PlaybackMediaItemDuration)
            }
        }
    }

    internal class SongPresenterSelector : PresenterSelector() {
        lateinit var mRegularPresenter: Presenter
        lateinit var mFavoritePresenter: Presenter

        /**
         * Adds a presenter to be used for the given class.
         */
        fun setSongPresenterRegular(presenter: Presenter): SongPresenterSelector {
            mRegularPresenter = presenter
            return this
        }

        /**
         * Adds a presenter to be used for the given class.
         */
        fun setSongPresenterFavorite(presenter: Presenter): SongPresenterSelector {
            mFavoritePresenter = presenter
            return this
        }

        override fun getPresenters(): Array<Presenter> {
            return arrayOf(mRegularPresenter, mFavoritePresenter)
        }

        override fun getPresenter(item: Any): Presenter {
            return if ((item as Song).isFavorite) mFavoritePresenter else mRegularPresenter
        }

    }

    internal class TrackListHeaderPresenter : AbstractMediaListHeaderPresenter() {

        override fun onBindMediaListHeaderViewHolder(vh: AbstractMediaListHeaderPresenter.ViewHolder, item: Any) {
            vh.headerView.text = "Tracklist"
        }
    }

    private fun addPlaybackControlsRow(songList: List<Song>) {
        mRowsAdapter = ArrayObjectAdapter(ClassPresenterSelector()
                .addClassPresenterSelector(Song::class.java, SongPresenterSelector()
                        .setSongPresenterRegular(SongPresenter(activity,
                                R.style.Theme_Example_LeanbackMusic_RegularSongNumbers))
                        .setSongPresenterFavorite(SongPresenter(activity,
                                R.style.Theme_Example_LeanbackMusic_FavoriteSongNumbers)))
                .addClassPresenter(TrackListHeader::class.java, TrackListHeaderPresenter()))
        mRowsAdapter!!.add(TrackListHeader())
        mRowsAdapter!!.addAll(mRowsAdapter!!.size(), songList)
        adapter = mRowsAdapter
        setOnItemViewClickedListener(this)
    }


    override fun onItemClicked(itemViewHolder: Presenter.ViewHolder, item: Any?,
                               rowViewHolder: RowPresenter.ViewHolder, row: Any) {

        if (row is Song) {
            // if a media item row is clicked
            val songRowVh = rowViewHolder as AbstractMediaItemPresenter.ViewHolder

            // if an action within a media item row is clicked
            if (item is MultiActionsProvider.MultiAction) {
                if (item.id == FAVORITE_ACTION_ID.toLong()) {
                    val favoriteAction = item as MultiActionsProvider.MultiAction?
                    val playlistAction = songRowVh.mediaItemRowActions[0]
                    favoriteAction!!.incrementIndex()
                    playlistAction.incrementIndex()

                    row.isFavorite = !row.isFavorite
                    songRowVh.notifyDetailsChanged()
                    songRowVh.notifyActionChanged(playlistAction)
                    songRowVh.notifyActionChanged(favoriteAction)
                }
            } else if (item == null) {
                // if a media item details is clicked, start playing that media item
                onSongDetailsClicked(row)
            }

        }
    }

    fun onSongDetailsClicked(song: Song) {
        mGlue!!.prepareAndPlay(getUri(song))
    }

    private fun getUri(song: Song): Uri {
        return Utils.getResourceUri(activity, song.getFileResource(activity))
    }

    private fun createMetaDataFromSong(song: Song): MediaMetaData {
        val mediaMetaData = MediaMetaData()
        mediaMetaData.mediaTitle = song.title
        mediaMetaData.mediaArtistName = song.description
        val uri = getUri(song)
        mediaMetaData.mediaSourceUri = uri
        mediaMetaData.mediaAlbumArtResId = song.getImageResource(activity)
        return mediaMetaData
    }

    companion object {

        private val TAG = "MusicConsumptionExampleFragment"
        private val PLAYLIST_ACTION_ID = 0
        private val FAVORITE_ACTION_ID = 1
        private var firstRowView: TextView? = null
    }
}
