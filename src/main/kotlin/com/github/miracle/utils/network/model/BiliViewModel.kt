package com.github.miracle.utils.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class BiliViewModel(
    @SerialName("data")
    val `data`: Data,
    @SerialName("code")
    val code: Int
) {
    @Serializable
    data class Data(
        @SerialName("pic")
        val pic: String,
        @SerialName("title")
        val title: String,
        @SerialName("desc")
        val desc: String,
        @SerialName("owner")
        val owner: Owner,
        @SerialName("stat")
        val stat: Stat
    )

    @Serializable
    data class Owner(
        @SerialName("name")
        val name: String
    )

    @Serializable
    data class Stat(
        @SerialName("view")
        val view: Int,
        @SerialName("like")
        val like: Int,
        @SerialName("share")
        val share: Int,
        @SerialName("coin")
        val coin: Int
    )
}

