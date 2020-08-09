package plugins

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.content
import utils.module.ActivityModel
import java.net.URL

@OptIn(UnstableDefault::class)
fun Bot.random() {
    subscribeGroupMessages {
        startsWith("随机数") {
            reply(randomNumber(message.content))
        }

        startsWith("打乱") {
            reply(
                message.content
                    .split(" ")
                    .filter { it.isNotEmpty() }
                    .drop(1).shuffled().joinToString(" ")
            )
        }

        startsWith("抽签") {
            reply(
                message.content
                    .split(" ")
                    .filter { it.isNotEmpty() }
                    .drop(1)
                    .random()
            )
        }

        Regex("找点乐子|没事找事|找点事做") matchingReply {
            Json.parse(ActivityModel.serializer(), URL("http://www.boredapi.com/api/activity/").readText()).let {
                "你可以\n\t${it.activity}\n可行性:\t${it.accessibility}\n" +
                        "类型:\t${it.type}\n参与人数:\t${it.participants}\n花费:\t${it.price}\n${it.link}"
            }
        }
    }
}

fun randomNumber(message: String): String {
    var start = 1
    var end = 100

    val msg = message.split(" ").filter { it.isNotEmpty() }

    try {
        start = msg[1].toInt()
        end = msg[2].toInt()
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
