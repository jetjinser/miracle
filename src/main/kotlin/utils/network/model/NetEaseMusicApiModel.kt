package utils.network.model

import com.google.gson.annotations.SerializedName

data class NetEaseMusicApiModel(
    @SerializedName("result")
    val result: Result
) {
    data class Result(
        @SerializedName("songs")
        val songs: List<Song>
    )

    data class Song(
        @SerializedName("al")
        val al: Al,
        @SerializedName("ar")
        val ar: List<Ar>,
        @SerializedName("id")
        val id: Int,
        @SerializedName("name")
        val name: String
    )

    data class Al(
        @SerializedName("picUrl")
        val picUrl: String
    )

    data class Ar(
        @SerializedName("name")
        val name: String
    )
}
