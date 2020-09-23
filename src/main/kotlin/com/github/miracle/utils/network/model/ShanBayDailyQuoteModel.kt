package com.github.miracle.utils.network.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName


@Serializable
data class ShanBayDailyQuoteModel(
    @SerialName("author")
    val author: String,
    @SerialName("content")
    val content: String,
    @SerialName("translation")
    val translation: String
)