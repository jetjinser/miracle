package utils.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class SauceNaoModel(
    @SerialName("results")
    val results: List<Result>
) {
    @Serializable
    data class Result(
        @SerialName("data")
        val `data`: Data,
        @SerialName("header")
        val header: Header
    )

    @Serializable
    data class Data(
        @SerialName("ext_urls")
        val extUrls: List<String>,
        @SerialName("member_id")
        val memberId: Int,
        @SerialName("member_name")
        val memberName: String,
        @SerialName("pixiv_id")
        val pixivId: Int,
        @SerialName("title")
        val title: String
    )

    @Serializable
    data class Header(
        @SerialName("similarity")
        val similarity: String
    )
}