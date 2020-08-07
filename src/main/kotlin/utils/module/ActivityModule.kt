package utils.module

import kotlinx.serialization.*

@Serializable
data class ActivityModule(
    @SerialName("activity")
    val activity: String,

    @SerialName("type")
    val type: String,

    @SerialName("participants")
    val participants: Int,

    @SerialName("price")
    val price: Int,

    @SerialName("link")
    val link: String,

    @SerialName("key")
    val key: String,

    @SerialName("accessibility")
    val accessibility: Float
)