package android.support.v17.leanback.supportleanbackshowcase.models

import com.google.gson.annotations.SerializedName

/**
 * Created by susnata on 3/17/18.
 */
class VideoRow {
    @SerializedName("category")
    var category = ""
    @SerializedName("videos")
    var videos: List<VideoCard>? = null
}
