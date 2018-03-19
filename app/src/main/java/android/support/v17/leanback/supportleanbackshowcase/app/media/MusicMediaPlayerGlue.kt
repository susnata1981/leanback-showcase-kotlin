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
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.IBinder
import android.support.v17.leanback.widget.Action
import android.support.v17.leanback.widget.ArrayObjectAdapter
import android.support.v17.leanback.widget.PlaybackControlsRow
import android.util.Log

import java.util.ArrayList

/**
 *
 *>
 * This glue extends the [MediaPlayerGlue] and handles all the heavy-lifting of the
 * interactions between the fragment, playback controls, and the music service. It starts and
 * connects to a music service which will be running in the background. The music service notifies
 * the listeners set in this glue upon any playback status changes, and this glue will in turn
 * notify listeners passed from the fragment.
 *
 *
 */
class MusicMediaPlayerGlue(private val mContext: Context) : MediaPlayerGlue(mContext), MusicPlaybackService.ServiceCallback {

    private val mMediaMetaDataList = ArrayList<MediaMetaData>()
    internal var mPendingServiceListUpdate = false // flag indicating that mMediaMetaDataList is changed and
    // the media item list in the service needs to be updated
    // next time one of its APIs is used
    private var mPlaybackService: MusicPlaybackService? = null
    private var mServiceCallbackRegistered = false
    private var mOnBindServiceHasBeenCalled = false
    private var mStartPlayingAfterConnect = true

    private val mPlaybackServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            val binder = iBinder as MusicPlaybackService.LocalBinder
            mPlaybackService = binder.service

            if (mPlaybackService!!.currentMediaItem == null) {
                if (mStartPlayingAfterConnect && mMediaMetaData != null) {
                    prepareAndPlay(mMediaMetaData)
                }
            }

            mPlaybackService!!.registerServiceCallback(this@MusicMediaPlayerGlue)
            mServiceCallbackRegistered = true

            Log.d("MusicPlaybackService", "mPlaybackServiceConnection connected!")
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            Log.d("MusicPlaybackService", "mPlaybackServiceConnection disconnected!")
            mOnBindServiceHasBeenCalled = false
            mPlaybackService = null
            // update UI before the service disconnects. This should presumably happen after the
            // activity has called onStop. If the playback service finishes and stops, and when the user
            // returns to the playback from home screen, the play status and progress bar UIs could
            // be outdated and the activity may not connect to the service for a while. So we update
            // UI here for the playback state to be up-to-date once the user returns to the activity.
            onMediaStateChanged(-1)
        }
    }

    val isPlaybackServiceConnected: Boolean
        get() = if (mPlaybackService != null) true else false

    init {

        startAndBindToServiceIfNeeded()
    }

    private fun startAndBindToServiceIfNeeded() {
        if (mOnBindServiceHasBeenCalled) {
            return
        }
        // setting this flag to true so that media item list is repopulated once this activity
        // connects to a fresh copy of the playback service
        mPendingServiceListUpdate = true
        // Bind to MusicPlaybackService
        val serviceIntent = Intent(mContext, MusicPlaybackService::class.java)
        mContext.startService(serviceIntent)
        mContext.bindService(serviceIntent, mPlaybackServiceConnection, 0)
        mOnBindServiceHasBeenCalled = true
    }

    override fun onCurrentItemChanged(currentMediaItem: MediaMetaData?) {
        if (mPlaybackService == null) {
            // onMetadataChanged updates both the metadata info on the player as well as the progress bar
            onMetadataChanged()
            return
        }
        mediaMetaData = currentMediaItem
        if (mPlaybackService != null) {
            val repeatState = mPlaybackService!!.repeatState
            val mappedActionIndex = mapServiceRepeatStateToActionIndex(repeatState)
            // if the activity's current repeat state differs from the service's, update it with the
            // repeatState of the service
            if (mRepeatAction.index != mappedActionIndex) {
                mRepeatAction.index = mappedActionIndex
                MediaPlayerGlue.Companion.notifyItemChanged(controlsRow.secondaryActionsAdapter as ArrayObjectAdapter, mRepeatAction)
            }
        }
    }

    override fun onMediaStateChanged(currentMediaState: Int) {
        onStateChanged()
    }

    fun openServiceCallback() {
        if (mPlaybackService != null && !mServiceCallbackRegistered) {
            mPlaybackService!!.registerServiceCallback(this)
            mServiceCallbackRegistered = true
        }
    }

    fun releaseServiceCallback() {
        if (mPlaybackService != null && mServiceCallbackRegistered) {
            mPlaybackService!!.unregisterAll()
            mServiceCallbackRegistered = false
        }
    }

    /**
     * Unbinds glue from the playback service. Called when the fragment is destroyed (pressing back)
     */
    fun close() {
        Log.d("MusicPlaybackService", "MusicMediaPlayerGlue closed!")
        mContext.unbindService(mPlaybackServiceConnection)
    }

    override fun onActionClicked(action: Action) {
        // If either 'Shuffle' or 'Repeat' has been clicked we need to make sure the acitons index
        // is incremented and the UI updated such that we can display the new state.
        super.onActionClicked(action)
        if (action is PlaybackControlsRow.RepeatAction) {
            val index = action.index
            if (mPlaybackService != null) {
                mPlaybackService!!.repeatState = mapActionIndexToServiceRepeatState(index)
            }
        }
    }

    override fun isMediaPlaying(): Boolean {
        return mPlaybackService != null && mPlaybackService!!.isPlaying
    }

    override fun getMediaDuration(): Int {
        return if (mPlaybackService != null) mPlaybackService!!.duration else 0
    }

    override fun getCurrentPosition(): Int {
        return if (mPlaybackService != null) mPlaybackService!!.currentPosition else 0
    }

    override fun play(speed: Int) {
        prepareAndPlay(mMediaMetaData)
    }

    override fun pause() {
        if (mPlaybackService != null) {
            mPlaybackService!!.pause()
        }
    }

    override fun next() {
        if (mPlaybackService != null) {
            mPlaybackService!!.next()
        }
    }

    override fun previous() {
        if (mPlaybackService != null) {
            mPlaybackService!!.previous()
        }
    }

    fun setMediaMetaDataList(mediaMetaDataList: List<MediaMetaData>) {
        mMediaMetaDataList.clear()
        mMediaMetaDataList.addAll(mediaMetaDataList)
        mPendingServiceListUpdate = true
        if (mPlaybackService != null) {
            mPlaybackService!!.setMediaItemList(mMediaMetaDataList, false)
            mPendingServiceListUpdate = false
        }
    }

    fun prepareAndPlay(uri: Uri) {
        for (i in mMediaMetaDataList.indices) {
            val mediaData = mMediaMetaDataList[i]
            if (mediaData.mediaSourceUri == uri) {
                prepareAndPlay(mediaData)
                return
            }
        }
    }

    fun prepareAndPlay(mediaMetaData: MediaMetaData?) {
        if (mediaMetaData == null) {
            throw RuntimeException("Provided uri is null!")
        }
        startAndBindToServiceIfNeeded()
        mMediaMetaData = mediaMetaData
        if (mPlaybackService == null) {
            // This media item is saved (mMediaMetaData) and later played when the
            // connection channel is established.
            mStartPlayingAfterConnect = true
            return
        }
        if (mPendingServiceListUpdate) {
            mPlaybackService!!.setMediaItemList(mMediaMetaDataList, false)
            mPendingServiceListUpdate = false
        }
        mPlaybackService!!.playMediaItem(mediaMetaData)
    }

    companion object {

        private val TAG = "MusicMediaPlayerGlue"

        fun mapActionIndexToServiceRepeatState(index: Int): Int {
            return if (index == PlaybackControlsRow.RepeatAction.ONE) {
                MusicPlaybackService.MEDIA_ACTION_REPEAT_ONE
            } else if (index == PlaybackControlsRow.RepeatAction.ALL) {
                MusicPlaybackService.MEDIA_ACTION_REPEAT_ALL
            } else {
                MusicPlaybackService.MEDIA_ACTION_NO_REPEAT
            }
        }

        fun mapServiceRepeatStateToActionIndex(serviceRepeatState: Int): Int {
            return if (serviceRepeatState == MusicPlaybackService.MEDIA_ACTION_REPEAT_ONE) {
                PlaybackControlsRow.RepeatAction.ONE
            } else if (serviceRepeatState == MusicPlaybackService.MEDIA_ACTION_REPEAT_ALL) {
                PlaybackControlsRow.RepeatAction.ALL
            } else {
                PlaybackControlsRow.RepeatAction.NONE
            }
        }
    }

}
