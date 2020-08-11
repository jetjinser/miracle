package utils.model

import kotlinx.serialization.*

@Serializable
data class ActivityModel(
    @SerialName("activity")
    val activity: String,

    @SerialName("type")
    val type: String,

    @SerialName("participants")
    val participants: Int,

    @SerialName("price")
    val price: Float,

    @SerialName("link")
    val link: String,

    @SerialName("key")
    val key: String,

    @SerialName("accessibility")
    val accessibility: Float
)