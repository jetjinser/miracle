package utils.network.model

import com.google.gson.annotations.SerializedName


data class BiliCvModel(
    @SerializedName("data")
    val `data`: Data
) {
    data class Data(
        @SerializedName("author_name")
        val authorName: String,
        @SerializedName("stats")
        val stats: Stats,
        @SerializedName("title")
        val title: String
    )

    data class Stats(
        @SerializedName("coin")
        val coin: Int,
        @SerializedName("dislike")
        val dislike: Int,
        @SerializedName("favorite")
        val favorite: Int,
        @SerializedName("like")
        val like: Int,
        @SerializedName("reply")
        val reply: Int,
        @SerializedName("share")
        val share: Int,
        @SerializedName("view")
        val view: Int
    )
}