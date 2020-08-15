package utils.network.model

import com.google.gson.annotations.SerializedName


data class BiliCoverModel(
    @SerializedName("data")
    val `data`: Data
) {
    data class Data(
        @SerializedName("pic")
        val pic: String
    )
}

