package android.support.v17.leanback.supportleanbackshowcase.models

import android.content.Context
import com.google.gson.annotations.SerializedName

class DetailedCard {

    @SerializedName("title")
    val title = ""
    @SerializedName("description")
    val description = ""
    @SerializedName("text")
    val text = ""
    @SerializedName("localImageResource")
    val localImageResource: String? = null
    @SerializedName("price")
    val price: String? = null
    @SerializedName("characters")
    val characters: Array<Card>? = null
    @SerializedName("recommended")
    val recommended: Array<Card>? = null
    @SerializedName("year")
    val year = 0
    @SerializedName("trailerUrl")
    val trailerUrl: String? = null
    @SerializedName("videoUrl")
    val videoUrl: String? = null

    fun getLocalImageResourceId(context: Context): Int {
        return context.resources
                .getIdentifier(localImageResource, "drawable", context.packageName)
    }
}
