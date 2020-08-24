package utils.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class TraceMoeModel(
    @SerialName("docs")
    val docs: List<Doc>,
    @SerialName("limit")
    val limit: Int,
    @SerialName("limit_ttl")
    val limitTtl: Int,
    @SerialName("quota")
    val quota: Int,
    @SerialName("quota_ttl")
    val quotaTtl: Int
) {
    @Serializable
    data class Doc(
        @SerialName("at")
        val at: Double,
        @SerialName("from")
        val from: Double,
        @SerialName("to")
        val to: Double,
        @SerialName("is_adult")
        val isAdult: Boolean,
        @SerialName("anilist_id")
        val aniListId: Int,
        @SerialName("mal_id")
        val malId: Int,
        @SerialName("season")
        val season: String,
        @SerialName("similarity")
        val similarity: Double,
        @SerialName("title")
        val title: String,
        @SerialName("title_chinese")
        val titleChinese: String,
        @SerialName("tokenthumb")
        val tokenThumb: String
    )
}