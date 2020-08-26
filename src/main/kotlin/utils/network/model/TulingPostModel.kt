package utils.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.mamoe.mirai.message.data.asMessageChain
import net.mamoe.mirai.message.data.content

@Serializable
data class TulingPostModel(
    @SerialName("perception")
    val perception: Perception,
    @SerialName("reqType")
    val reqType: Int,
    @SerialName("userInfo")
    val userInfo: UserInfo
) {
    companion object {
        fun newModel(text: String, qqId: String, apiKey: String): TulingPostModel {

            return TulingPostModel(
                Perception(
                    InputText(
                        text
                    ),
                ),
                0,
                UserInfo(
                    apiKey,
                    qqId
                )
            )
        }
    }

    @Serializable
    data class Perception(
        @SerialName("inputText")
        val inputText: InputText
    )

    @Serializable
    data class UserInfo(
        @SerialName("apiKey")
        val apiKey: String,
        @SerialName("userId")
        val userId: String
    )

    @Serializable
    data class InputText(
        @SerialName("text")
        val text: String
    )
}