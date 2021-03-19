package com.github.miracle.utils.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 超话和用户共用一套
 */
@Serializable
data class LofterResponseModel(
    val status: Int,
    @SerialName("lofter_title")
    val title: String,
    val result: List<LofterArticleModel>
) {
    @Serializable
    data class LofterArticleModel(
        val title: String,
        val content: String,
        val time: String,
        val time_unix: Long,
        val author: String,
        val url: String,
        @SerialName("img_urls")
        val imgUrls: List<String>,
    )
}