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

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.MediaPlayer
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.support.v17.leanback.supportleanbackshowcase.R
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.KeyEvent

import java.io.IOException
import java.util.ArrayList

/**
 * Music service that handles all the interactions between the app and the media player. It receives
 * media list from an app and starts playing the media items one after another. Apps can also directly
 * interact with the service for specific operations such as playing or pausing or getting different
 * info about the current media item.
 */
class MusicPlaybackService : Service() {

    // The ID used for for the notification. This is purely for making the service run as a
    // foreground service
    internal val NOTIFICATION_ID = 1
    internal var mNotificationBuilder: Notification.Builder? = null

    private var mAudioManager: AudioManager? = null
    var repeatState = MEDIA_ACTION_NO_REPEAT

    private var mPlayer: MediaPlayer? = null
    // MediaSession created for communication between NowPlayingCard in the launcher and the current MediaPlayer state
    private var mMediaSession: MediaSessionCompat? = null
    internal var mCurrentMediaPosition = -1
    internal var mCurrentMediaState = -1
    internal var mCurrentMediaItem: MediaMetaData? = null
    internal var mMediaItemList: MutableList<MediaMetaData> = ArrayList()
    private var mInitialized = false // true when the MediaPlayer is prepared/initialized


    private val mMediaPlayerHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                FOCUS_CHANGE -> when (msg.arg1) {
                    AudioManager.AUDIOFOCUS_LOSS -> {
                        if (mAudioManager!!.abandonAudioFocus(mOnAudioFocusChangeListener) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                            Log.w(TAG, "abandonAudioFocus after AudioFocus_LOSS failed!")
                        }
                        pause()
                        Log.d(TAG, "AudioFocus: Received AUDIOFOCUS_LOSS.")
                    }
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> Log.d(TAG, "AudioFocus: Received AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK.")
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> Log.d(TAG, "AudioFocus: Received AUDIOFOCUS_LOSS_TRANSIENT.")
                    AudioManager.AUDIOFOCUS_GAIN -> Log.d(TAG, "AudioFocus: Received AUDIOFOCUS_GAIN.")
                }
            }
        }
    }

    private val mOnAudioFocusChangeListener = AudioManager.OnAudioFocusChangeListener {
//        focusChange -> mMediaPlayerHandler.obtainMessage(FOCUS_CHANGE, focusChange, 0).sendToTarget()
    }

    private val mServiceCallbacks = ArrayList<ServiceCallback>()

    // Binder given to clients of this service
    private val mBinder = LocalBinder()

    /**
     * @return The available set of actions for the media session. These actions should be provided
     * for the MediaSession PlaybackState in order for
     * [MediaSessionCompat.Callback.onMediaButtonEvent] to call relevant methods of onPause() or
     * onPlay().
     */
    private val playbackStateActions: Long
        get() = PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS

    val isPlaying: Boolean
        get() = mPlayer != null && mPlayer!!.isPlaying

    /**
     * @return The current playback position in milliseconds.
     */
    val currentPosition: Int
        get() = if (mInitialized && mPlayer != null) {
            mPlayer!!.currentPosition
        } else 0

    /**
     * @return The duration of the current media item in milliseconds
     */
    val duration: Int
        get() = if (mInitialized && mPlayer != null) mPlayer!!.duration else 0

    val currentMediaItem: MediaMetaData?
        get() = if (mInitialized) mCurrentMediaItem else null

    interface ServiceCallback {
        fun onMediaStateChanged(currentMediaState: Int)
        fun onCurrentItemChanged(currentMediaItem: MediaMetaData?)
    }

    fun registerServiceCallback(serviceCallback: ServiceCallback?) {
        if (serviceCallback == null) {
            throw IllegalArgumentException("The provided service callback is null.")
        }
        mServiceCallbacks.add(serviceCallback)
        if (mCurrentMediaItem != null) {
            // Calling onMediaStateChangedByPlaybackService on the callback to update UI on the
            // activity if they are out of sync with the playback service state.
            serviceCallback.onCurrentItemChanged(mCurrentMediaItem)
            serviceCallback.onMediaStateChanged(mCurrentMediaState)
        }
    }

    fun unregisterServiceCallback(serviceCallback: ServiceCallback?) {
        if (serviceCallback == null) {
            throw IllegalArgumentException("The provided service callback is null.")
        }
        mServiceCallbacks.remove(serviceCallback)
        stopServiceIfNeeded()
    }

    fun unregisterAll() {
        mServiceCallbacks.clear()
        stopServiceIfNeeded()
    }

    private fun stopServiceIfNeeded() {
        if (mServiceCallbacks.size == 0 && mCurrentMediaState == MediaUtils.MEDIA_STATE_MEDIALIST_COMPLETED) {
            Log.d(TAG, "stop " + this@MusicPlaybackService)
            stopSelf()
        }
    }

    private fun notifyMediaStateChanged(currentMediaState: Int) {
        mCurrentMediaState = currentMediaState
        for (i in mServiceCallbacks.indices.reversed()) {
            mServiceCallbacks[i].onMediaStateChanged(currentMediaState)
        }
    }

    private fun notifyMediaItemChanged(currentMediaItem: MediaMetaData) {
        mCurrentMediaItem = currentMediaItem
        for (i in mServiceCallbacks.indices.reversed()) {
            mServiceCallbacks[i].onCurrentItemChanged(mCurrentMediaItem)
        }
    }

    inner class LocalBinder : Binder() {
        internal val service: MusicPlaybackService
            get() = this@MusicPlaybackService
    }

    override fun onCreate() {
        super.onCreate()
        if (mPlayer == null) {
            mPlayer = MediaPlayer()
        }
        if (mMediaSession == null) {
            mMediaSession = MediaSessionCompat(this, "MusicPlayer Session")
            mMediaSession!!.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
            mMediaSession!!.setCallback(MediaSessionCallback())
            updateMediaSessionIntent()
        }
        mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        setUpAsForeground("This Awesome Music Service :)")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return Service.START_NOT_STICKY
    }

    fun setMediaItemList(mediaItemList: List<MediaMetaData>, isQueue: Boolean) {
        if (!isQueue) {
            mMediaItemList.clear()
        }
        mMediaItemList.addAll(mediaItemList)

    }

    fun playMediaItem(mediaItemToPlay: MediaMetaData?) {
        if (mediaItemToPlay == null) {
            throw IllegalArgumentException("mediaItemToPlay is null!")
        }
        val mediaItemPos = findMediaItemPosition(mediaItemToPlay)
        if (mediaItemPos == -1) {
            throw IllegalArgumentException("mediaItemToPlay not found in the media item list!")
        }

        if (mAudioManager!!.requestAudioFocus(mOnAudioFocusChangeListener, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.e(TAG, "playMediaItem cannot obtain audio focus!")
            return
        }

        if (!mMediaSession!!.isActive) {
            mMediaSession!!.isActive = true
        }

        if (mCurrentMediaItem != null && mInitialized &&
                mCurrentMediaItem!!.mediaSourceUri == mediaItemToPlay.mediaSourceUri) {
            if (!isPlaying) {
                // This media item had been already playing but is being paused. Will resume the player.
                // No need to reset the player
                play()
            }
        } else {
            mCurrentMediaPosition = mediaItemPos
            notifyMediaItemChanged(mediaItemToPlay)
            prepareNewMedia()
        }
    }

    internal fun findMediaItemPosition(mediaItem: MediaMetaData): Int {
        for (i in mMediaItemList.indices) {
            if (mMediaItemList[i].mediaSourceUri == mediaItem.mediaSourceUri) {
                return i
            }
        }
        return -1
    }

    private fun prepareNewMedia() {
        reset()
        try {
            mPlayer!!.setDataSource(applicationContext,
                    mCurrentMediaItem!!.mediaSourceUri!!)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        mPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mPlayer!!.setOnPreparedListener {
            updateMediaSessionMetaData()
            mInitialized = true
            play()
        }
        mPlayer!!.setOnErrorListener { mediaPlayer, what, extra ->
            Log.e(TAG, "Error: what=" + what.toString() + ", extra=" +
                    extra.toString())
            true
        }

        mPlayer!!.setOnCompletionListener {
            updateMediaSessionPlayState()
            if (repeatState == MEDIA_ACTION_REPEAT_ALL && mCurrentMediaPosition == mMediaItemList.size - 1) {
                // The last media item is played and repeatAll action is enabled;
                // start over the media list from the beginning
                mCurrentMediaPosition = 0
                notifyMediaItemChanged(mMediaItemList[mCurrentMediaPosition])
                prepareNewMedia()
            } else if (repeatState == MEDIA_ACTION_REPEAT_ONE) {
                // repeat playing the same media item
                prepareNewMedia()
            } else if (mCurrentMediaPosition < mMediaItemList.size - 1) {
                // Move on to playing the next media item in the list
                mCurrentMediaPosition++
                notifyMediaItemChanged(mMediaItemList[mCurrentMediaPosition])
                prepareNewMedia()
            } else {
                // Last media item is reached, and the service is no longer necessary;
                // Stop the service after some delay, since the service might need to stay alive
                // for some time for the cleanup (such as updating the progress bar during the
                // final seconds).
                notifyMediaStateChanged(MediaUtils.MEDIA_STATE_MEDIALIST_COMPLETED)
                stopServiceIfNeeded()

            }
        }
        mPlayer!!.prepareAsync()
        notifyMediaStateChanged(MediaUtils.MEDIA_STATE_PREPARING)
    }

    private fun updateMediaSessionMetaData() {
        if (mCurrentMediaItem == null) {
            throw IllegalArgumentException(
                    "mCurrentMediaItem is null in updateMediaSessionMetaData!")
        }
        val metaDataBuilder = MediaMetadataCompat.Builder()
        if (mCurrentMediaItem!!.mediaTitle != null) {
            metaDataBuilder.putString(MediaMetadata.METADATA_KEY_TITLE,
                    mCurrentMediaItem!!.mediaTitle)
        }
        if (mCurrentMediaItem!!.mediaAlbumName != null) {
            metaDataBuilder.putString(MediaMetadata.METADATA_KEY_ALBUM,
                    mCurrentMediaItem!!.mediaAlbumName)
        }
        if (mCurrentMediaItem!!.mediaArtistName != null) {
            metaDataBuilder.putString(MediaMetadata.METADATA_KEY_ARTIST,
                    mCurrentMediaItem!!.mediaArtistName)
        }
        if (mCurrentMediaItem!!.mediaAlbumArtResId != 0) {
            val albumArtBitmap = BitmapFactory.decodeResource(resources,
                    mCurrentMediaItem!!.mediaAlbumArtResId)
            metaDataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, albumArtBitmap)
        }
        mMediaSession!!.setMetadata(metaDataBuilder.build())
    }

    private fun updateMediaSessionPlayState() {
        val playbackStateBuilder = PlaybackStateCompat.Builder()
        val playState: Int
        if (isPlaying) {
            playState = PlaybackStateCompat.STATE_PLAYING
        } else {
            playState = PlaybackStateCompat.STATE_PAUSED
        }
        val currentPosition = currentPosition.toLong()
        playbackStateBuilder.setState(playState, currentPosition, 1.0.toFloat()).setActions(
                playbackStateActions
        )
        mMediaSession!!.setPlaybackState(playbackStateBuilder.build())
    }

    /**
     * Sets the media session's activity launched when clicking on NowPlayingCard. This returns to
     * the media screen that is playing or paused; the launched activity corresponds to the
     * currently shown media session in the NowPlayingCard on TV launcher.
     */
    private fun updateMediaSessionIntent() {
        if (mMediaSession == null) {
            return
        }
        val nowPlayIntent = Intent(applicationContext, MusicExampleActivity::class.java)
        val pi = PendingIntent.getActivity(applicationContext, 0, nowPlayIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)
        mMediaSession!!.setSessionActivity(pi)
    }

    internal fun reset() {
        if (mPlayer != null) {
            mPlayer!!.reset()
            mInitialized = false
        }
    }

    /**
     * starts playback of the previously opened media file
     */
    fun play() {
        if (mPlayer != null && mInitialized && !isPlaying) {
            mPlayer!!.start()
            updateMediaSessionPlayState()
            notifyMediaStateChanged(MediaUtils.MEDIA_STATE_PLAYING)
        }
    }

    /**
     * pauses playback (call play() to resume)
     */
    fun pause() {
        if (mPlayer != null && mPlayer!!.isPlaying) {
            mPlayer!!.pause()
            updateMediaSessionPlayState()
            notifyMediaStateChanged(MediaUtils.MEDIA_STATE_PAUSED)
        }
    }

    /**
     * skip to next item
     */
    operator fun next() {
        if (mMediaItemList.size == 0) {
            return
        }
        if (mCurrentMediaPosition == mMediaItemList.size - 1) {
            // The last media item is played and repeatAll action is enabled;
            // start over the media list from the beginning
            mCurrentMediaPosition = 0
        } else {
            // Move on to playing the next media item in the list
            mCurrentMediaPosition++
        }
        notifyMediaItemChanged(mMediaItemList[mCurrentMediaPosition])
        prepareNewMedia()
    }

    /**
     * skip to previous item
     */
    fun previous() {
        if (mMediaItemList.size == 0) {
            return
        }
        if (mCurrentMediaPosition == 0) {
            // The last media item is played and repeatAll action is enabled;
            // start over the media list from the beginning
            mCurrentMediaPosition = mMediaItemList.size - 1
        } else {
            // Move on to playing the next media item in the list
            mCurrentMediaPosition--
        }
        notifyMediaItemChanged(mMediaItemList[mCurrentMediaPosition])
        prepareNewMedia()
    }

    /**
     * Seeks to the given new position in milliseconds of the current media item
     * @param newPosition The new position of the current media item in milliseconds
     */
    fun seekTo(newPosition: Int) {
        if (mPlayer != null) {
            mPlayer!!.seekTo(newPosition)
        }
    }

    /**
     * Configures service as a foreground service.
     */
    internal fun setUpAsForeground(text: String) {
        val notificationIntent = Intent(this, MusicExampleActivity::class.java)
        val pi = PendingIntent.getActivity(applicationContext, 0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)

        // Build the notification object.
        mNotificationBuilder = Notification.Builder(applicationContext)
                .setSmallIcon(R.drawable.ic_favorite_border_white_24dp)
                .setTicker(text)
                .setWhen(System.currentTimeMillis())
                .setContentTitle("RandomMusicPlayer")
                .setContentText(text)
                .setContentIntent(pi)
                .setOngoing(true)

        startForeground(NOTIFICATION_ID, mNotificationBuilder!!.build())
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
        mAudioManager!!.abandonAudioFocus(mOnAudioFocusChangeListener)
        mMediaPlayerHandler.removeCallbacksAndMessages(null)
        if (mPlayer != null) {
            // stop and release the media player since it's no longer in use
            mPlayer!!.reset()
            mPlayer!!.release()
            mPlayer = null
        }
        if (mMediaSession != null) {
            mMediaSession!!.release()
            mMediaSession = null
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    private inner class MediaSessionCallback : MediaSessionCompat.Callback() {

        override fun onMediaButtonEvent(mediaButtonIntent: Intent): Boolean {
            val keyEvent = mediaButtonIntent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
            Log.d(TAG, "onMediaButtonEvent in MediaSessionCallback called with event: " + keyEvent)
            return super.onMediaButtonEvent(mediaButtonIntent)
        }

        override fun onPlay() {
            play()
        }

        override fun onPause() {
            pause()
        }

        override fun onSkipToNext() {
            next()
        }

        override fun onSkipToPrevious() {
            previous()
        }

        override fun onFastForward() {
            super.onFastForward()
        }

        override fun onRewind() {
            super.onRewind()
        }

        override fun onStop() {
            super.onStop()
        }

        override fun onSeekTo(pos: Long) {
            seekTo(pos.toInt())
        }
    }

    companion object {

        val MEDIA_ACTION_NO_REPEAT = 0
        val MEDIA_ACTION_REPEAT_ONE = 1
        val MEDIA_ACTION_REPEAT_ALL = 2

        private val TAG = "MusicPlaybackService"

        private val FOCUS_CHANGE = 2
    }
}
