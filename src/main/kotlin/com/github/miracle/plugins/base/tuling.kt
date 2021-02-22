package com.github.miracle.plugins.base

import com.github.miracle.SecretConfig.tulingApiKeyArray
import io.ktor.client.request.*
import kotlinx.serialization.decodeFromString
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.subscribeFriendMessages
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.content
import com.github.miracle.utils.network.KtorClient
import com.github.miracle.utils.network.model.TulingPostModel
import com.github.miracle.utils.network.model.TulingRecvModel
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.toMessageChain

fun Bot.tuling() {
    suspend fun MessageEvent.tulingReply(s: String) {
        val url = "http://openapi.tuling123.com/openapi/api/v2"
        val client = KtorClient.getInstance() ?: return

        while (true) {
            val apiKey = tulingApiKeyArray[Store.index]
            // 直接借用 ktor 序列化就会报错, 怪事
            val resp = client.post<String>(url) {
                header("Content-Type", "application/json")
                body = TulingPostModel.newModel(
                    s,
                    sender.id.toString(),
                    apiKey
                )
            }

            val model = KtorClient.json.decodeFromString<TulingRecvModel>(resp)

            if (model.results.first().values.text == "请求次数超限制!") {
                Store.index += 1
                logger.info("api index + 1")
                continue
            }

            var msg = ""
            model.results.forEach { result ->
                result.values.let { value ->
                    value.image?.let { msg += "$it\n" }
                    value.text?.let { msg += "$it\n" }
                    value.url?.let { msg += "$it\n" }
                    value.news?.let { msg += "$it\n" }
                    value.voice?.let { msg += "$it\n" }
                }
            }

            if ("http" in msg) {
                logger.info("图灵广告url")
                continue
            }

            subject.sendMessage(if (msg.isNotEmpty()) msg.dropLast(1) else "?")
            break
        }
    }

    eventChannel.subscribeGroupMessages(priority = EventPriority.MONITOR) {
        has<At> { at ->
            if (at.target != bot.id) return@has
            tulingReply(message.drop(2).toMessageChain().content)
        }
    }

    eventChannel.subscribeFriendMessages {
        always {
            tulingReply(message.content)
        }
    }
}

private object Store {
    var index: Int = 0
        get() {
            if (field == 10) field = 0
            return field
        }
}
