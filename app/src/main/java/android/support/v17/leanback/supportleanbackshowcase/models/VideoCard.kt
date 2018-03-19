package android.support.v17.leanback.supportleanbackshowcase.models

import com.google.gson.annotations.SerializedName

/**
 * Created by susnata on 3/17/18.
 */
class VideoCard : Card() {

    @SerializedName("sources")
    var videoSource:Array<String> = arrayOf("")
    @SerializedName("background")
    var background = ""
    @SerializedName("studio")
    var studio = ""

    init {
        type = Type.VIDEO_GRID
    }
}
