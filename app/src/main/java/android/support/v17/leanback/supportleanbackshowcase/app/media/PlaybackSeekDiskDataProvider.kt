/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.support.v17.leanback.supportleanbackshowcase.app.media

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.support.v17.leanback.media.PlaybackGlue
import android.support.v17.leanback.media.PlaybackTransportControlGlue

import java.io.File

/**
 * Sample PlaybackSeekDataProvider that reads bitmaps stored on disk.
 * e.g. new PlaybackSeekDiskDataProvider(duration, 1000, "/sdcard/frame_%04d.jpg")
 * Expects the seek positions are 1000ms interval, snapshots are stored at
 * /sdcard/frame_0001.jpg, ...
 */
class PlaybackSeekDiskDataProvider internal constructor(duration: Long, interval: Long, internal val mPathPattern: String) : PlaybackSeekAsyncDataProvider() {

    internal val mPaint: Paint

    init {
        val size = (duration / interval).toInt() + 1
        val pos = LongArray(size)
        for (i in pos.indices) {
            pos[i] = i * duration / pos.size
        }
        seekPositions = pos
        mPaint = Paint()
        mPaint.textSize = 16f
        mPaint.color = Color.BLUE
    }

    override fun doInBackground(task: Any, index: Int, position: Long): Bitmap? {
        try {
            Thread.sleep(100)
        } catch (ex: InterruptedException) {
            // Thread might be interrupted by cancel() call.
        }

        if (isCancelled(task)) {
            return null
        }
        val path = String.format(mPathPattern, index + 1)
        if (File(path).exists()) {
            return BitmapFactory.decodeFile(path)
        } else {
            val bmp = Bitmap.createBitmap(160, 160, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            canvas.drawColor(Color.YELLOW)
            canvas.drawText(path, 10f, 80f, mPaint)
            canvas.drawText(Integer.toString(index), 10f, 150f, mPaint)
            return bmp
        }
    }

    companion object {

        /**
         * Helper function to set a demo seek provider on PlaybackTransportControlGlue based on
         * duration.
         */
        fun setDemoSeekProvider(glue: PlaybackTransportControlGlue<*>) {
            if (glue.isPrepared) {
                glue.setSeekProvider(PlaybackSeekDiskDataProvider(
                        glue.duration,
                        glue.duration / 100,
                        "/sdcard/seek/frame_%04d.jpg"))
            } else {
                glue.addPlayerCallback(object : PlaybackGlue.PlayerCallback() {
                    override fun onPreparedStateChanged(glue: PlaybackGlue) {
                        if (glue.isPrepared) {
                            glue.removePlayerCallback(this)
                            val transportControlGlue = glue as PlaybackTransportControlGlue<*>
                            transportControlGlue.seekProvider = PlaybackSeekDiskDataProvider(
                                    transportControlGlue.duration,
                                    transportControlGlue.duration / 100,
                                    "/sdcard/seek/frame_%04d.jpg")
                        }
                    }
                })
            }
        }
    }

}
