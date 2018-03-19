package android.support.v17.leanback.supportleanbackshowcase.models

import com.google.gson.annotations.SerializedName

/**
 * This class represents a row of cards. In a real world application you might want to store more
 * data than in this example.
 */
class CardRow {

    @SerializedName("type")
    val type = TYPE_DEFAULT
    // Used to determine whether the row shall use shadows when displaying its cards or not.
    @SerializedName("shadow") private val mShadow = true
    @SerializedName("title")
    val title: String? = null
    @SerializedName("cards")
    val cards: List<Card>? = null

    fun useShadow(): Boolean {
        return mShadow
    }

    companion object {

        // default is a list of cards
        val TYPE_DEFAULT = 0
        // section header
        val TYPE_SECTION_HEADER = 1
        // divider
        val TYPE_DIVIDER = 2
    }
}
