package utils.network.model

import com.google.gson.annotations.SerializedName


data class BiliViewModel(
    @SerializedName("data")
    val `data`: Data,
    @SerializedName("code")
    val code: Int
) {
    data class Data(
        @SerializedName("pic")
        val pic: String,
        @SerializedName("title")
        val title: String,
        @SerializedName("desc")
        val desc: String,
        @SerializedName("owner")
        val owner: Owner,
        @SerializedName("stat")
        val stat: Stat
    )
    data class Owner(
        @SerializedName("name")
        val name: String
    )
    data class Stat(
        @SerializedName("view")
        val view: Int,
        @SerializedName("like")
        val like: Int,
        @SerializedName("share")
        val share: Int,
        @SerializedName("coin")
        val coin: Int
    )
}

