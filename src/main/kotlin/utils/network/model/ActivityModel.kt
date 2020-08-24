package utils.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class ActivityModel(
    @SerialName("accessibility")
    val accessibility: Double,
    @SerialName("activity")
    val activity: String,
    @SerialName("key")
    val key: String,
    @SerialName("link")
    val link: String,
    @SerialName("participants")
    val participants: Int,
    @SerialName("price")
    val price: Double,
    @SerialName("type")
    val type: String
)