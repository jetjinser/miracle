package utils.network.model

import com.google.gson.annotations.SerializedName


data class TraceMoeModel(
    @SerializedName("docs")
    val docs: List<Doc>,
    @SerializedName("limit")
    val limit: Int,
    @SerializedName("limit_ttl")
    val limitTtl: Int,
    @SerializedName("quota")
    val quota: Int,
    @SerializedName("quota_ttl")
    val quotaTtl: Int
) {
    data class Doc(
        @SerializedName("at")
        val at: Double,
        @SerializedName("from")
        val from: Double,
        @SerializedName("to")
        val to: Double,
        @SerializedName("is_adult")
        val isAdult: Boolean,
        @SerializedName("anilist_id")
        val aniListId: Int,
        @SerializedName("mal_id")
        val malId: Int,
        @SerializedName("season")
        val season: String,
        @SerializedName("similarity")
        val similarity: Double,
        @SerializedName("title")
        val title: String,
        @SerializedName("title_chinese")
        val titleChinese: String,
        @SerializedName("tokenthumb")
        val tokenThumb: String
    )
}