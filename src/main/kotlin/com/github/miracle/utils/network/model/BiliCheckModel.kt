package com.github.miracle.utils.network.model

import kotlinx.serialization.Serializable

import kotlinx.serialization.SerialName


@Serializable
data class BiliCheckModel(
    @SerialName("code")
    val code: Int,
    @SerialName("data")
    val `data`: Data,
    @SerialName("message")
    val message: String,
    @SerialName("msg")
    val msg: String
) {
    @Serializable
    data class Data(
        @SerialName("content")
        val content: String,
        @SerialName("ctime")
        val ctime: String,
        @SerialName("roomid")
        val roomId: String,
        @SerialName("status")
        val status: String,
        @SerialName("uid")
        val uid: String,
        @SerialName("uname")
        val uname: String
    )
}
