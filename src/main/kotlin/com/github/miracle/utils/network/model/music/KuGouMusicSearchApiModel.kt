package com.github.miracle.utils.network.model.music

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName


@Serializable
data class KuGouMusicSearchApiModel(
    @SerialName("data")
    val `data`: Data
) {
    @Serializable
    data class Data(
        @SerialName("info")
        val info: List<Info>
    )

    @Serializable
    data class Info(
        @SerialName("hash")
        val hash: String,
    )
}