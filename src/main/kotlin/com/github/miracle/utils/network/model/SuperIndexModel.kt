package com.github.miracle.utils.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SuperIndexModel(
    val status: Int,
    val result: List<SuperIndex>
) {
    @Serializable
    data class SuperIndex(
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