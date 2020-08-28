package com.github.miracle.utils.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TulingRecvModel(
    @SerialName("results")
    val results: List<Result>
) {
    @Serializable
    data class Result(
        @SerialName("groupType")
        val groupType: Int,
        @SerialName("resultType")
        val resultType: String,
        @SerialName("values")
        val values: Values
    ) {
        @Serializable
        data class Values(
            @SerialName("text")
            val text: String? = null,
            @SerialName("url")
            val url: String? = null,
            @SerialName("voice")
            val voice: String? = null,
            @SerialName("image")
            val image: String? = null,
            @SerialName("news")
            val news: String? = null
        )
    }
}