package com.github.miracle.plugins

import io.ktor.client.request.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.content
import com.github.miracle.utils.network.KtorClient
import com.github.miracle.utils.network.model.ActivityModel

fun Bot.random() {
    subscribeGroupMessages {
        startsWith("随机数", removePrefix = true, trim = true) {
            reply(randomNumber(message.content))
             
        }

        startsWith("打乱", removePrefix = true, trim = true) {
            reply(
                message.content
                    .split(" ")
                    .filter { it.isNotEmpty() }
                    .shuffled().joinToString(" ")
            )
             
        }

        startsWith("抽签") {
            if (message.content.isEmpty()) {
                reply("格式错误，需要提供要抽签的内容")
                return@startsWith
            }
            reply(
                message.content
                    .split(" ")
                    .filter { it.isNotEmpty() }
                    .drop(1)
                    .random()
            )
             
        }

        Regex("找点乐子|没事找事|找点事做") matching regex@{
            val client = KtorClient.getInstance() ?: return@regex

            val url = "http://www.boredapi.com/api/activity/"
            val model = client.get<ActivityModel>(url)

            reply(
                "你可以\n\t${model.activity}\n可行性:\t${model.accessibility}\n" +
                        "类型:\t${model.type}\n参与人数:\t${model.participants}\n花费:\t${model.price}" +
                        if (model.link.isNotEmpty()) "\n${model.link}" else ""
            )
             
        }
    }
}

fun randomNumber(message: String): String {
    var start = 1
    var end = 100

    val msg = message.split(" ").filter { it.isNotEmpty() }

    try {
        start = msg[0].toInt()
        end = msg[1].toInt()
    } catch (e: NumberFormatException) {
        return "需要是数字"
    } catch (e: IndexOutOfBoundsException) {
    }

    start = minOf(start, end)
    end = maxOf(start, end)
    if (start < -10000 || end > 10000) {
        return "范围过大"
    }

    return (start..end).random().toString()
}
