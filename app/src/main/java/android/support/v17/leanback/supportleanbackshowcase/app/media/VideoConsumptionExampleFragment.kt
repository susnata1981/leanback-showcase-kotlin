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
 */

package android.support.v17.leanback.supportleanbackshowcase.app.media

import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.support.v17.leanback.app.PlaybackFragment
import android.support.v17.leanback.app.VideoFragment
import android.support.v17.leanback.app.VideoFragmentGlueHost
import android.support.v17.leanback.media.MediaPlayerAdapter
import android.support.v17.leanback.media.PlaybackGlue
import android.support.v17.leanback.widget.PlaybackControlsRow
import android.util.Log


class VideoConsumptionExampleFragment : VideoFragment() {
    private var mMediaPlayerGlue: VideoMediaPlayerGlue<MediaPlayerAdapter>? = null
    internal val mHost = VideoFragmentGlueHost(this)

    internal var mOnAudioFocusChangeListener: AudioManager.OnAudioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mMediaPlayerGlue = VideoMediaPlayerGlue(activity,
                MediaPlayerAdapter(activity))
        mMediaPlayerGlue!!.host = mHost
        val audioManager = activity
                .getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (audioManager.requestAudioFocus(mOnAudioFocusChangeListener, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.w(TAG, "video player cannot obtain audio focus!")
        }

        mMediaPlayerGlue!!.setMode(PlaybackControlsRow.RepeatAction.NONE)
        val intentMetaData = activity.intent.getParcelableExtra<MediaMetaData>(
                VideoExampleActivity.TAG)
        if (intentMetaData != null) {
            mMediaPlayerGlue!!.title = intentMetaData.mediaTitle
            mMediaPlayerGlue!!.subtitle = intentMetaData.mediaArtistName
            mMediaPlayerGlue!!.playerAdapter.setDataSource(
                    Uri.parse(intentMetaData.mediaSourcePath))
        } else {
            mMediaPlayerGlue!!.title = "Diving with Sharks"
            mMediaPlayerGlue!!.subtitle = "A Googler"
            mMediaPlayerGlue!!.playerAdapter.setDataSource(Uri.parse(URL))
        }
        PlaybackSeekDiskDataProvider.setDemoSeekProvider(mMediaPlayerGlue!!)
        playWhenReady(mMediaPlayerGlue!!)
        backgroundType = PlaybackFragment.BG_LIGHT
    }

    override fun onPause() {
        if (mMediaPlayerGlue != null) {
            mMediaPlayerGlue!!.pause()
        }
        super.onPause()
    }

    companion object {

        private val URL = "https://storage.googleapis.com/android-tv/Sample videos/" + "April Fool's 2013/Explore Treasure Mode with Google Maps.mp4"
        val TAG = "VideoConsumption"

        internal fun playWhenReady(glue: PlaybackGlue) {
            if (glue.isPrepared) {
                glue.play()
            } else {
                glue.addPlayerCallback(object : PlaybackGlue.PlayerCallback() {
                    override fun onPreparedStateChanged(glue: PlaybackGlue) {
                        if (glue.isPrepared) {
                            glue.removePlayerCallback(this)
                            glue.play()
                        }
                    }
                })
            }
        }
    }

}
