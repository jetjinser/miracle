package com.github.miracle.utils.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AntiLightAppModel(
    @SerialName("meta")
    val meta: Meta
) {
    @Serializable
    data class Meta(
        @SerialName("detail_1")
        val detail: Detail
    )

    @Serializable
    data class Detail(
        @SerialName("desc")
        val desc: String,
        @SerialName("preview")
        val preview: String,
        @SerialName("qqdocurl")
        val qqDoCurl: String?,
        @SerialName("title")
        val title: String
    )
}