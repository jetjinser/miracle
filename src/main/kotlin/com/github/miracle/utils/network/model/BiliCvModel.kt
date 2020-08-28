package com.github.miracle.utils.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class BiliCvModel(
    @SerialName("data")
    val `data`: Data
) {
    @Serializable
    data class Data(
        @SerialName("author_name")
        val authorName: String,
        @SerialName("stats")
        val stats: Stats,
        @SerialName("title")
        val title: String
    )

    @Serializable
    data class Stats(
        @SerialName("coin")
        val coin: Int,
        @SerialName("dislike")
        val dislike: Int,
        @SerialName("favorite")
        val favorite: Int,
        @SerialName("like")
        val like: Int,
        @SerialName("reply")
        val reply: Int,
        @SerialName("share")
        val share: Int,
        @SerialName("view")
        val view: Int
    )
}