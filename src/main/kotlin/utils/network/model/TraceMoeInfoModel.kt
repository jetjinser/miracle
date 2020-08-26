package utils.network.model

import kotlinx.serialization.Serializable

import kotlinx.serialization.SerialName


@Serializable
class TraceMoeInfoModel(
    @SerialName("coverImage")
    val coverImage: CoverImage,
    @SerialName("description")
    val description: String
) {
    @Serializable
    data class CoverImage(
        @SerialName("large")
        val large: String
    )
}