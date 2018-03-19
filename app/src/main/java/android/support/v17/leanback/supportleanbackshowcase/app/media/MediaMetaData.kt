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

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

/**
 * Holds the meta data such as media title, artist and cover art. It'll be used by the [ ] for passing media item information between app and [MediaPlayerGlue], and
 * between [MusicMediaPlayerGlue] and [MusicPlaybackService].
 */
class MediaMetaData : Parcelable {

    var mediaSourceUri: Uri? = null
    var mediaSourcePath: String? = null
    var mediaTitle: String? = null
    var mediaArtistName: String? = null
    var mediaAlbumName: String? = null
    var mediaAlbumArtResId: Int = 0
    var mediaAlbumArtUrl: String? = null

    internal constructor(mediaSourceUri: Uri, mediaSourcePath: String, mediaTitle: String,
                         mediaArtistName: String, mediaAlbumName: String, mediaAlbumArtResId: Int,
                         mediaAlbumArtUrl: String) {
        this.mediaSourceUri = mediaSourceUri
        this.mediaSourcePath = mediaSourcePath
        this.mediaTitle = mediaTitle
        this.mediaArtistName = mediaArtistName
        this.mediaAlbumName = mediaAlbumName
        this.mediaAlbumArtResId = mediaAlbumArtResId
        this.mediaAlbumArtUrl = mediaAlbumArtUrl
    }

    constructor() {}

    constructor(`in`: Parcel) {
        mediaSourceUri = `in`.readParcelable(null)
        mediaSourcePath = `in`.readString()
        mediaTitle = `in`.readString()
        mediaArtistName = `in`.readString()
        mediaAlbumName = `in`.readString()
        mediaAlbumArtResId = `in`.readInt()
        mediaAlbumArtUrl = `in`.readString()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(mediaSourceUri, flags)
        dest.writeString(mediaSourcePath)
        dest.writeString(mediaTitle)
        dest.writeString(mediaArtistName)
        dest.writeString(mediaAlbumName)
        dest.writeInt(mediaAlbumArtResId)
        dest.writeString(mediaAlbumArtUrl)
    }

    companion object {

       @JvmField val CREATOR: Parcelable.Creator<MediaMetaData> = object : Parcelable.Creator<MediaMetaData> {
            override fun createFromParcel(parcel: Parcel): MediaMetaData {
                return MediaMetaData(parcel)
            }

            override fun newArray(size: Int): Array<MediaMetaData?> {
                return arrayOfNulls<MediaMetaData>(size)
            }
        }
    }

}
