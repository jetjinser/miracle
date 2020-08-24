package plugins

import io.ktor.client.request.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.LightApp
import net.mamoe.mirai.message.data.buildMessageChain
import utils.network.KtorClient
import utils.network.model.AntiLightAppModel


fun Bot.antiLightApp() {
    subscribeGroupMessages {
        has<LightApp> {
            val client = KtorClient.getInstance() ?: return@has
            val model: AntiLightAppModel
            try {
                model = Json { ignoreUnknownKeys = true }.decodeFromString(it.content)
            } catch (e: Exception) {
                logger.info("JsonDecodingException 网 易 云")
                return@has
            }


            var url = model.meta.detail.preview
            "https://".let { scheme ->
                if (!url.startsWith(scheme) && !url.startsWith("http://")) {
                    url = scheme + url
                }
            }
            logger.info("Request $url")

            val byteArray = client.get<ByteArray>(url)

            val detail = model.meta.detail
            val doCurl: String? = model.meta.detail.qqDoCurl?.split("?")?.first()

            buildMessageChain {
                add("@${detail.title}\n")
                add(byteArray.inputStream().uploadAsImage())
                add("${detail.desc}\n${doCurl ?: "无法获取链接: ${detail.title}不支持或版本过低"}\nvia antiLightApp")
            }.send()
        }
    }
}
