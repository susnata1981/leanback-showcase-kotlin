package android.support.v17.leanback.supportleanbackshowcase.models

import android.content.Context
import android.support.v17.leanback.widget.MultiActionsProvider
import com.google.gson.annotations.SerializedName


class Song : MultiActionsProvider {

    @SerializedName("title")
    var title = ""
    @SerializedName("description")
    var description = ""
    @SerializedName("text")
    val text = ""
    @SerializedName("image") private val mImage: String? = null
    @SerializedName("file") private val mFile: String? = null
    @SerializedName("duration")
    var duration: String? = null
    @SerializedName("number")
    val number = 0
    @SerializedName("favorite")
    var isFavorite = false

    var mediaRowActions: Array<MultiActionsProvider.MultiAction?>? = null

    fun getFileResource(context: Context): Int {
        return context.resources
                .getIdentifier(mFile, "raw", context.packageName)
    }

    fun getImageResource(context: Context): Int {
        return context.resources
                .getIdentifier(mImage, "drawable", context.packageName)
    }

    override fun getActions(): Array<MultiActionsProvider.MultiAction?>? {
        return mediaRowActions
    }

}
