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
import android.os.AsyncTask
import android.support.v17.leanback.widget.PlaybackSeekDataProvider
import android.support.v4.util.LruCache
import android.util.Log
import android.util.SparseArray

/**
 *
 * Base class that implements PlaybackSeekDataProvider using AsyncTask.THREAD_POOL_EXECUTOR with
 * prefetching.
 */
abstract class PlaybackSeekAsyncDataProvider @JvmOverloads constructor(cacheSize: Int = 16, prefetchCacheSize: Int = 24) : PlaybackSeekDataProvider() {

    lateinit internal var mSeekPositions: LongArray
    // mCache is for the bitmap requested by user
    internal val mCache: LruCache<Int, Bitmap>
    // mPrefetchCache is for the bitmap not requested by user but prefetched by heuristic
    // estimation. We use a different LruCache so that items in mCache will not be evicted by
    // prefeteched items.
    internal val mPrefetchCache: LruCache<Int, Bitmap>
    internal val mRequests = SparseArray<LoadBitmapTask>()
    internal var mLastRequestedIndex = -1

    protected fun isCancelled(task: Any): Boolean {
        return (task as AsyncTask<*, *, *>).isCancelled
    }

    protected abstract fun doInBackground(task: Any, index: Int, position: Long): Bitmap?

    internal inner class LoadBitmapTask(var mIndex: Int, var mResultCallback: PlaybackSeekDataProvider.ResultCallback?) : AsyncTask<Any, Any, Bitmap>() {

        override fun doInBackground(params: Array<Any>): Bitmap? {
            return this@PlaybackSeekAsyncDataProvider
                    .doInBackground(this, mIndex, mSeekPositions[mIndex])
        }

        override fun onPostExecute(bitmap: Bitmap) {
            mRequests.remove(mIndex)
            Log.d(TAG, "thumb Loaded " + mIndex)
            if (mResultCallback != null) {
                mCache.put(mIndex, bitmap)
                mResultCallback!!.onThumbnailLoaded(bitmap, mIndex)
            } else {
                mPrefetchCache.put(mIndex, bitmap)
            }
        }

    }

    init {
        mCache = LruCache(cacheSize)
        mPrefetchCache = LruCache(prefetchCacheSize)
    }

    fun setSeekPositions(positions: LongArray) {
        mSeekPositions = positions
    }

    override fun getSeekPositions(): LongArray {
        return mSeekPositions
    }

    override fun getThumbnail(index: Int, callback: PlaybackSeekDataProvider.ResultCallback?) {
        var bitmap: Bitmap? = mCache.get(index)
        if (bitmap != null) {
            callback!!.onThumbnailLoaded(bitmap, index)
        } else {
            bitmap = mPrefetchCache.get(index)
            if (bitmap != null) {
                mCache.put(index, bitmap)
                mPrefetchCache.remove(index)
                callback!!.onThumbnailLoaded(bitmap, index)
            } else {
                var task: LoadBitmapTask? = mRequests.get(index)
                if (task == null || task.isCancelled) {
                    // no normal task or prefetch for the position, create a new task
                    task = LoadBitmapTask(index, callback)
                    mRequests.put(index, task)
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                } else {
                    // update existing ResultCallback which might be normal task or prefetch
                    task.mResultCallback = callback
                }
            }
        }
        if (mLastRequestedIndex != index) {
            if (mLastRequestedIndex != -1) {
                prefetch(mLastRequestedIndex, index > mLastRequestedIndex)
            }
            mLastRequestedIndex = index
        }
    }

    protected fun prefetch(hintIndex: Int, forward: Boolean) {
        val it = mPrefetchCache.snapshot().entries.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            if (if (forward) entry.key < hintIndex else entry.key > hintIndex) {
                mPrefetchCache.remove(entry.key)
            }
        }
        val inc = if (forward) 1 else -1
        var i = hintIndex
        while (mRequests.size() + mPrefetchCache.size() < mPrefetchCache.maxSize() && if (inc > 0) i < mSeekPositions.size else i >= 0) {
            val key = i
            if (mCache.get(key) == null && mPrefetchCache.get(key) == null) {
                var task: LoadBitmapTask? = mRequests.get(i)
                if (task == null) {
                    task = LoadBitmapTask(key, null)
                    mRequests.put(i, task)
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                }
            }
            i += inc
        }
    }

    override fun reset() {
        for (i in 0 until mRequests.size()) {
            val task = mRequests.valueAt(i)
            task.cancel(true)
        }
        mRequests.clear()
        mCache.evictAll()
        mPrefetchCache.evictAll()
        mLastRequestedIndex = -1
    }

    override fun toString(): String {
        val b = StringBuilder()
        b.append("Requests<")
        for (i in 0 until mRequests.size()) {
            b.append(mRequests.keyAt(i))
            b.append(",")
        }
        b.append("> Cache<")
        run {
            val it = mCache.snapshot().keys.iterator()
            while (it.hasNext()) {
                val key = it.next()
                if (mCache.get(key!!) != null) {
                    b.append(key)
                    b.append(",")
                }
            }
        }
        b.append(">")
        b.append("> PrefetchCache<")
        val it = mPrefetchCache.snapshot().keys.iterator()
        while (it.hasNext()) {
            val key = it.next()
            if (mPrefetchCache.get(key!!) != null) {
                b.append(key)
                b.append(",")
            }
        }
        b.append(">")
        return b.toString()
    }

    companion object {

        internal val TAG = "SeekAsyncProvider"
    }
}
