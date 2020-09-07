package com.github.miracle.utils.network.model.music

import kotlinx.serialization.Serializable

import kotlinx.serialization.SerialName


@Serializable
data class KuGouMusicInfoApiModel(
    @SerialName("data")
    val `data`: Data
) {
    @Serializable
    data class Data(
        @SerialName("author_name")
        val authorName: String,
        @SerialName("img")
        val img: String,
        @SerialName("play_url")
        val playUrl: String,
        @SerialName("song_name")
        val songName: String,
    )
}