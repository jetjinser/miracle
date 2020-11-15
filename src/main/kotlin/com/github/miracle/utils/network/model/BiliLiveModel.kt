package com.github.miracle.utils.network.model

import kotlinx.serialization.Serializable

import kotlinx.serialization.SerialName

@Serializable
data class BiliLiveModel(
    @SerialName("data")
    val `data`: Data
) {
    @Serializable
    data class Data(
        @SerialName("anchor_info")
        val anchorInfo: AnchorInfo,
        @SerialName("room_info")
        val roomInfo: RoomInfo
    )

    @Serializable
    data class AnchorInfo(
        @SerialName("base_info")
        val baseInfo: BaseInfo
    )

    @Serializable
    data class RoomInfo(
        @SerialName("keyframe")
        val keyframe: String,
        @SerialName("live_start_time")
        val liveStartTime: Int,
        @SerialName("title")
        val title: String,
        @SerialName("live_status")
        val liveStatus: Int
    )

    @Serializable
    data class BaseInfo(
        @SerialName("uname")
        val uname: String
    )
}
