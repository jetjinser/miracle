package com.github.miracle.utils.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 超话和用户共用一套
 */
@Serializable
data class WeiboResponseModel(
    val status: Int,
    @SerialName("weibo_title")
    val weiboTitle: String,
    val result: List<WeiboModel>
) {
    @Serializable
    data class WeiboModel(
        val title: String,
        val content: String,
        @SerialName("img_urls")
        val imgUrls: List<String>,
        @SerialName("ttarticle_link")
        val ttarticleLink: String,
        val extra: List<String>,
        val time: String,
        val time_unix: Long,
        val author: String,
        val link: String,
    )
}