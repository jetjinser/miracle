package plugins

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.content
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import utils.logger.BotLogger
import utils.module.ActivityModule
import utils.network.OkHttpUtil
import java.io.IOException

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
            getActivity()
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


@OptIn(UnstableDefault::class)
fun getActivity(): String {
    val url = "http://www.boredapi.com/api/activity/"

    lateinit var msg: String

    val okHttpClient = OkHttpUtil.getInstance()
    okHttpClient?.newCall(
        Request.Builder()
            .url(url)
            .get().build()
    )?.apply {
        enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                BotLogger.logger("okhttp").warning("failed: $url cause of ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let {
                    val data = Json.parse(ActivityModule.serializer(), it)
                    msg =
                        "你可以\n\t${data.activity}\n可行性:\t${data.accessibility}\n" +
                                "类型:\t${data.type}\n参与人数:\t${data.participants}\n花费:\t${data.price}\n${data.link}"
                }
            }
        })
    }
    return msg
}
