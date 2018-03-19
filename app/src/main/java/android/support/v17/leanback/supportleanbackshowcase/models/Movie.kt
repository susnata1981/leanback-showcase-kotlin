package android.support.v17.leanback.supportleanbackshowcase.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Movie : Serializable {

    @SerializedName("title")
    val title = ""
    @SerializedName("price_hd")
    val priceHd = "n/a"
    @SerializedName("price_sd")
    val priceSd = "n/a"
    @SerializedName("breadcrump")
    val breadcrump = ""

    companion object {
        private const val serialVersionUID = 133742L
    }

}
