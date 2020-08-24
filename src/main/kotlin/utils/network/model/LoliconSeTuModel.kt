package utils.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class LoliconSeTuModel(
    @SerialName("code")
    val code: Int,
    @SerialName("count")
    val count: Int,
    @SerialName("data")
    val `data`: List<Data>,
    @SerialName("msg")
    val msg: String,
    @SerialName("quota")
    val quota: Int,
    @SerialName("quota_min_ttl")
    val quotaMinTtl: Int
) {
    @Serializable
    data class Data(
        @SerialName("author")
        val author: String,
        @SerialName("height")
        val height: Int,
        @SerialName("p")
        val p: Int,
        @SerialName("pid")
        val pid: Int,
        @SerialName("r18")
        val r18: Boolean,
        @SerialName("tags")
        val tags: List<String>,
        @SerialName("title")
        val title: String,
        @SerialName("uid")
        val uid: Int,
        @SerialName("url")
        val url: String,
        @SerialName("width")
        val width: Int
    )
}