package plugins

import com.google.gson.JsonSyntaxException
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.LightApp
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.launch
import net.mamoe.mirai.message.data.buildMessageChain
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import utils.network.OkHttpUtil
import utils.network.Requests
import java.io.IOException


fun Bot.antiLightApp() {
    subscribeGroupMessages {
        has<LightApp> {
            OkHttpUtil.gson.fromJson(
                it.content,
                LightAppModel::class.java
            ).let { model ->
                var url = model.meta.detail.preview
                "https://".let { scheme ->
                    if (!url.startsWith(scheme) && !url.startsWith("http://")) {
                        url = scheme + url
                    }
                }
                logger.info("Request $url")
                Requests.get(
                    url,
                    object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            logger.error("antiLightApp.bili 请求 preview 图片时失败")
                        }

                        override fun onResponse(call: Call, response: Response) {
                            val doCurl: String? = try {
                                model.meta.detail.qqDoCurl?.split("?")?.first()
                            } catch (e: JsonSyntaxException) {
                                null
                            }
                            val detail = model.meta.detail
                            launch {
                                buildMessageChain {
                                    add("@${detail.title}\n")
                                    response.body?.byteStream()?.let { image -> uploadImage(image) }
                                        ?.let { imageMessage -> add(imageMessage) }
                                    add("${detail.desc}\n${doCurl ?: "无法获取链接: ${detail.title}不支持或版本过低"}\nvia antiLightApp")
                                }.send()
                            }
                        }
                    }
                )
            }
        }
    }
}

data class LightAppModel(
    @SerializedName("meta")
    val meta: Meta
) {
    data class Meta(
        @SerializedName("detail_1")
        val detail: Detail
    )

    data class Detail(
        @SerializedName("desc")
        val desc: String,
        @SerializedName("preview")
        val preview: String,
        @SerializedName("qqdocurl")
        val qqDoCurl: String?,
        @SerializedName("title")
        val title: String
    )
}



