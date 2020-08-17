package utils.network.model
import com.google.gson.annotations.SerializedName


data class SauceNaoModel(
    @SerializedName("results")
    val results: List<Result>
) {
    data class Result(
        @SerializedName("data")
        val `data`: Data,
        @SerializedName("header")
        val header: Header
    )

    data class Data(
        @SerializedName("ext_urls")
        val extUrls: List<String>,
        @SerializedName("member_id")
        val memberId: Int,
        @SerializedName("member_name")
        val memberName: String,
        @SerializedName("pixiv_id")
        val pixivId: Int,
        @SerializedName("title")
        val title: String
    )

    data class Header(
        @SerializedName("similarity")
        val similarity: String
    )
}