package com.github.miracle.utils.network.model

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
        val memberId: Int = -1,
        @SerialName("member_name")
        val memberName: String = "未知",
        @SerialName("pixiv_id")
        val pixivId: Int = -1,
        @SerialName("title")
        val title: String = "未知"
    )

    @Serializable
    data class Header(
        @SerialName("similarity")
        val similarity: String,
        @SerialName("thumbnail")
        val thumbnail: String
    )
}