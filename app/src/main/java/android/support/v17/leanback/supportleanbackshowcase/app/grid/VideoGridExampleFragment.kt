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

package android.support.v17.leanback.supportleanbackshowcase.app.grid

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.support.v17.leanback.app.VerticalGridFragment
import android.support.v17.leanback.supportleanbackshowcase.R
import android.support.v17.leanback.supportleanbackshowcase.app.media.MediaMetaData
import android.support.v17.leanback.supportleanbackshowcase.app.media.VideoExampleActivity
import android.support.v17.leanback.supportleanbackshowcase.cards.presenters.CardPresenterSelector
import android.support.v17.leanback.supportleanbackshowcase.cards.presenters.VideoCardViewPresenter
import android.support.v17.leanback.supportleanbackshowcase.models.VideoCard
import android.support.v17.leanback.supportleanbackshowcase.models.VideoRow
import android.support.v17.leanback.widget.ArrayObjectAdapter
import android.support.v17.leanback.widget.FocusHighlight
import android.support.v17.leanback.widget.OnItemViewClickedListener
import android.support.v17.leanback.widget.OnItemViewSelectedListener
import android.support.v17.leanback.widget.Presenter
import android.support.v17.leanback.widget.PresenterSelector
import android.support.v17.leanback.widget.Row
import android.support.v17.leanback.widget.RowPresenter
import android.support.v17.leanback.widget.VerticalGridPresenter
import android.util.Log
import android.widget.Toast

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

import org.json.JSONException
import org.json.JSONObject

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.ArrayList
import java.util.HashMap

/**
 * Example fragment displaying videos in a vertical grid using [VerticalGridFragment].
 * It fetches the videos from the the url in [R.string.videos_url] and displays the metadata
 * fetched from each video in an ImageCardView (using [VideoCardViewPresenter]).
 * On clicking on each one of these video cards, a fresh instance of the
 * VideoExampleActivity starts which plays the video item.
 */
class VideoGridExampleFragment : VerticalGridFragment(), OnItemViewSelectedListener, OnItemViewClickedListener {
    // Hashmap mapping category names to the list of videos in that category. This is fetched from
    // the url
    private val categoryVideosMap:MutableMap<String, MutableList<VideoCard>> = hashMapOf();

    private var mAdapter: ArrayObjectAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getString(R.string.video_grid_example_title)
        setupRowAdapter()
    }

    private fun setupRowAdapter() {
        val videoGridPresenter = VerticalGridPresenter(ZOOM_FACTOR)
        videoGridPresenter.numberOfColumns = COLUMNS
        // note: The click listeners must be called before setGridPresenter for the event listeners
        // to be properly registered on the viewholders.
        setOnItemViewSelectedListener(this)
        onItemViewClickedListener = this
        gridPresenter = videoGridPresenter

        val cardPresenterSelector = CardPresenterSelector(activity)
        // VideoCardViewPresenter videoCardViewPresenter = new VideoCardViewPresenter(getActivity());
        mAdapter = ArrayObjectAdapter(cardPresenterSelector)
        adapter = mAdapter

        prepareEntranceTransition()
        Handler().postDelayed({ createRows() }, 1000)
    }

    private fun createRows() {
        val urlToFetch = resources.getString(R.string.videos_url)
        fetchVideosInfo(urlToFetch)
    }

    /**
     * Called when videos metadata are fetched from the url. The result of this fetch is returned
     * in the form of a JSON object.
     * @param jsonObj The json object containing the information about all the videos.
     */
    private fun onFetchVideosInfoSuccess(jsonObj: JSONObject?) {
        try {
            val videoRowsJson = jsonObj!!.getString(TAG_CATEGORY)
            Log.d("XXX", "video = " + videoRowsJson);
//            val videoRows = Gson().fromJson(videoRowsJson, Array<VideoRow>::class.java)
            var videoRows = Gson().fromJson<List<VideoRow>>(videoRowsJson, object : TypeToken<List<VideoRow>>() {}.type)

            for (videoRow in videoRows) {
                if (!categoryVideosMap.containsKey(videoRow.category)) {
                    categoryVideosMap.put(videoRow.category, ArrayList())
                }
                categoryVideosMap[videoRow.category]!!.addAll(videoRow.videos!!)
                mAdapter!!.addAll(mAdapter!!.size(), videoRow.videos!!)
                startEntranceTransition()
            }
        } catch (ex: JSONException) {
            Log.e(TAG, "A JSON error occurred while fetching videos: " + ex.toString())
        }

    }

    /**
     * Called when an exception occurred while fetching videos meta data from the url.
     * @param ex The exception occurred in the asynchronous task fetching videos.
     */
    private fun onFetchVideosInfoError(ex: Exception?) {
        Log.e(TAG, "Error fetching videos from " + resources.getString(R.string.videos_url) +
                ", Exception: " + ex!!.toString())
        Toast.makeText(context, "Error fetching videos from json file",
                Toast.LENGTH_LONG).show()
    }

    /**
     * The result type of the background computation of the url fetcher
     */
    private class FetchResult {
        var isSuccess: Boolean = false
        var exception: Exception? = null
        internal var jsonObj: JSONObject? = null

        internal constructor(obj: JSONObject) {
            jsonObj = obj
            isSuccess = true
            exception = null
        }

        internal constructor(ex: Exception) {
            jsonObj = null
            isSuccess = false
            exception = ex
        }
    }

    /**
     * Fetches videos metadata from urlString on a background thread. Callback methods are invoked
     * upon success or failure of this fetching.
     * @param urlString The json file url to fetch from
     */
    private fun fetchVideosInfo(urlString: String) {

        object : AsyncTask<Void, Void, FetchResult>() {
            override fun onPostExecute(fetchResult: FetchResult) {
                if (fetchResult.isSuccess) {
                    onFetchVideosInfoSuccess(fetchResult.jsonObj)
                } else {
                    onFetchVideosInfoError(fetchResult.exception)
                }
            }

            override fun doInBackground(vararg params: Void): FetchResult {
                var reader: BufferedReader? = null
                var urlConnection: HttpURLConnection? = null
                try {
                    val url = URL(urlString)
                    urlConnection = url.openConnection() as HttpURLConnection
                    reader = BufferedReader(InputStreamReader(urlConnection.inputStream,
                            "utf-8"))
                    val sb = StringBuilder()
                    var line: String? = reader.readLine();
                    while (line != null) {
                        sb.append(line)
                        line = reader.readLine();
                    }
                    return FetchResult(JSONObject(sb.toString()))
                } catch (ex: JSONException) {
                    Log.e(TAG, "A JSON error occurred while fetching videos: " + ex.toString())
                    return FetchResult(ex)
                } catch (ex: IOException) {
                    Log.e(TAG, "An I/O error occurred while fetching videos: " + ex.toString())
                    return FetchResult(ex)
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect()
                    }
                    if (reader != null) {
                        try {
                            reader.close()
                        } catch (ex: IOException) {
                            Log.e(TAG, "JSON reader could not be closed! " + ex)
                        }

                    }
                }
            }
        }.execute()
    }

    override fun onItemClicked(itemViewHolder: Presenter.ViewHolder, item: Any,
                               rowViewHolder: RowPresenter.ViewHolder?, row: Row?) {
        if (item is VideoCard) {
            val metaData = MediaMetaData()
            metaData.mediaSourcePath = item.videoSource[0]
            metaData.mediaTitle = item.title
            metaData.mediaArtistName = item.description
            metaData.mediaAlbumArtUrl = item.imageUrl
            val intent = Intent(activity, VideoExampleActivity::class.java)
            intent.putExtra(VideoExampleActivity.TAG, metaData)
            intent.data = Uri.parse(metaData.mediaSourcePath)
            activity.startActivity(intent)
        }
    }

    override fun onItemSelected(itemViewHolder: Presenter.ViewHolder, item: Any,
                                rowViewHolder: RowPresenter.ViewHolder?, row: Row?) {

    }

    companion object {

        private val COLUMNS = 4
        private val ZOOM_FACTOR = FocusHighlight.ZOOM_FACTOR_MEDIUM
        private val TAG = "VGridExampleFragment"
        private val TAG_CATEGORY = "googlevideos"
    }
}
