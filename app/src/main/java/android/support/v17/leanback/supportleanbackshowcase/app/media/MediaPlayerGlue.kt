/*
 * Copyright (C) 2016 The Android Open Source Project
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

package android.support.v17.leanback.supportleanbackshowcase.app.media

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Message
import android.support.v17.leanback.media.PlaybackControlGlue
import android.support.v17.leanback.supportleanbackshowcase.R
import android.support.v17.leanback.widget.Action
import android.support.v17.leanback.widget.ArrayObjectAdapter
import android.support.v17.leanback.widget.PlaybackControlsRow
import android.support.v17.leanback.widget.PlaybackControlsRowPresenter
import android.util.Log

/**
 * This glue extends the [PlaybackControlGlue] with a [MediaMetaData] support.
 * It supports 7 actions:
 *
 *  * [android.support.v17.leanback.widget.PlaybackControlsRow.SkipNextAction]
 *  * [android.support.v17.leanback.widget.PlaybackControlsRow.SkipPreviousAction]
 *  * [android.support.v17.leanback.widget.PlaybackControlsRow.PlayPauseAction]
 *  * [android.support.v17.leanback.widget.PlaybackControlsRow.ShuffleAction]
 *  * [android.support.v17.leanback.widget.PlaybackControlsRow.RepeatAction]
 *  * [android.support.v17.leanback.widget.PlaybackControlsRow.ThumbsDownAction]
 *  * [android.support.v17.leanback.widget.PlaybackControlsRow.ThumbsUpAction]
 *
 *
 *
 */
abstract class MediaPlayerGlue(context: Context) : PlaybackControlGlue(context, intArrayOf(1)) {
    private val mHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                REFRESH_PROGRESS -> {
                    updateProgress()
                    queueNextRefresh()
                }
            }
        }
    }

    protected var mShuffleAction: PlaybackControlsRow.MultiAction
    protected var mRepeatAction: PlaybackControlsRow.MultiAction
    protected var mThumbsUpAction: PlaybackControlsRow.MultiAction
    protected var mThumbsDownAction: PlaybackControlsRow.MultiAction
    private val mLastKeyDownEvent = 0L // timestamp when the last DPAD_CENTER KEY_DOWN occurred

    protected var mMediaMetaData: MediaMetaData? = null

    var mediaMetaData: MediaMetaData?
        get() = mMediaMetaData
        set(mediaMetaData) {
            mMediaMetaData = mediaMetaData
            onMetadataChanged()
        }

    init {
        mShuffleAction = PlaybackControlsRow.ShuffleAction(getContext())
        mRepeatAction = PlaybackControlsRow.RepeatAction(getContext())
        mThumbsDownAction = PlaybackControlsRow.ThumbsDownAction(getContext())
        mThumbsUpAction = PlaybackControlsRow.ThumbsUpAction(getContext())
        mThumbsDownAction.index = PlaybackControlsRow.ThumbsAction.OUTLINE
        mThumbsUpAction.index = PlaybackControlsRow.ThumbsAction.OUTLINE
    }

    override fun onCreateSecondaryActions(secondaryActionsAdapter: ArrayObjectAdapter?) {
        // Instantiate secondary actions
        secondaryActionsAdapter!!.add(mShuffleAction)
        secondaryActionsAdapter.add(mRepeatAction)
        secondaryActionsAdapter.add(mThumbsDownAction)
        secondaryActionsAdapter.add(mThumbsUpAction)
    }

    override fun onCreateControlsRowAndPresenter() {
        super.onCreateControlsRowAndPresenter()
        val presenter = playbackRowPresenter as PlaybackControlsRowPresenter
        presenter.progressColor = context.resources.getColor(
                R.color.player_progress_color)
        presenter.backgroundColor = context.resources.getColor(
                R.color.player_background_color)
    }

    override fun enableProgressUpdating(enabled: Boolean) {
        Log.d(TAG, "enableProgressUpdating: " + enabled)
        if (!enabled) {
            mHandler.removeMessages(REFRESH_PROGRESS)
            return
        }
        queueNextRefresh()
    }

    override fun getUpdatePeriod(): Int {
        return 16
    }

    private fun queueNextRefresh() {
        val refreshMsg = mHandler.obtainMessage(REFRESH_PROGRESS)
        mHandler.removeMessages(REFRESH_PROGRESS)
        mHandler.sendMessageDelayed(refreshMsg, updatePeriod.toLong())
    }

    override fun onActionClicked(action: Action) {
        if (action is PlaybackControlsRow.ShuffleAction || action is PlaybackControlsRow.RepeatAction) {
            (action as PlaybackControlsRow.MultiAction).nextIndex()
            notifySecondaryActionChanged(action)
        } else if (action === mThumbsUpAction) {
            if (mThumbsUpAction.index == PlaybackControlsRow.ThumbsAction.SOLID) {
                mThumbsUpAction.index = PlaybackControlsRow.ThumbsAction.OUTLINE
                notifySecondaryActionChanged(mThumbsUpAction)
            } else {
                mThumbsUpAction.index = PlaybackControlsRow.ThumbsAction.SOLID
                mThumbsDownAction.index = PlaybackControlsRow.ThumbsAction.OUTLINE
                notifySecondaryActionChanged(mThumbsUpAction)
                notifySecondaryActionChanged(mThumbsDownAction)
            }
        } else if (action === mThumbsDownAction) {
            if (mThumbsDownAction.index == PlaybackControlsRow.ThumbsAction.SOLID) {
                mThumbsDownAction.index = PlaybackControlsRow.ThumbsAction.OUTLINE
                notifySecondaryActionChanged(mThumbsDownAction)
            } else {
                mThumbsDownAction.index = PlaybackControlsRow.ThumbsAction.SOLID
                mThumbsUpAction.index = PlaybackControlsRow.ThumbsAction.OUTLINE
                notifySecondaryActionChanged(mThumbsUpAction)
                notifySecondaryActionChanged(mThumbsDownAction)
            }
        } else {
            super.onActionClicked(action)
        }
    }

    internal fun notifySecondaryActionChanged(act: Action) {
        notifyItemChanged(controlsRow.secondaryActionsAdapter as ArrayObjectAdapter, act)
    }

    override fun hasValidMedia(): Boolean {
        return mMediaMetaData != null
    }


    override fun getMediaTitle(): CharSequence? {
        return if (hasValidMedia()) mMediaMetaData!!.mediaTitle else "N/a"
    }

    override fun getMediaSubtitle(): CharSequence? {
        return if (hasValidMedia()) mMediaMetaData!!.mediaArtistName else "N/a"
    }

    override fun getMediaArt(): Drawable? {
        return if (hasValidMedia() && mMediaMetaData!!.mediaAlbumArtResId != 0)
            context.resources.getDrawable(mMediaMetaData!!.mediaAlbumArtResId, null)
        else
            null
    }

    override fun getSupportedActions(): Long {
        return (PlaybackControlGlue.ACTION_PLAY_PAUSE
                or PlaybackControlGlue.ACTION_SKIP_TO_NEXT
                or PlaybackControlGlue.ACTION_SKIP_TO_PREVIOUS).toLong()
    }

    override fun getCurrentSpeedId(): Int {
        return if (isMediaPlaying) PlaybackControlGlue.PLAYBACK_SPEED_NORMAL else PlaybackControlGlue.PLAYBACK_SPEED_PAUSED
    }

    companion object {

        private val TAG = "MusicMediaPlayerGlue"
        private val REFRESH_PROGRESS = 1

        internal fun notifyItemChanged(adapter: ArrayObjectAdapter, `object`: Any) {
            val index = adapter.indexOf(`object`)
            if (index >= 0) {
                adapter.notifyArrayItemRangeChanged(index, 1)
            }
        }
    }

}
