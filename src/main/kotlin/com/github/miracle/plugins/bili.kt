package com.github.miracle.plugins

import com.github.miracle.utils.network.KtorClient
import com.github.miracle.utils.network.model.BiliCvModel
import com.github.miracle.utils.network.model.BiliViewModel
import com.github.miracle.utils.tools.bili.AvBv
import io.ktor.client.request.*
import io.ktor.client.statement.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.message.nextMessage
import net.mamoe.mirai.utils.ExternalResource.Companion.sendAsImageTo
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import java.util.*

fun Bot.bili() {
    suspend fun MessageEvent.biliView(message: String) {
        val avOrBv =
            Regex(""".*(?i)((?<av>av\d{5,13})|(?<bv>BV\w{10}))(?-i).*""").matchEntire(message)?.groupValues?.get(1)
        val aid = avOrBv?.drop(2)?.toIntOrNull() ?: avOrBv?.let { bv -> AvBv.bvToAv(bv)?.toInt() }
        if (aid != null) {
            val url = "https://api.bilibili.com/x/web-interface/view?aid=$aid"

            logger.info("Request $url")

            val client = KtorClient.getInstance() ?: return

            val model = client.get<BiliViewModel>(url)
            if (model.code == 0) {
                val biliUrl = "https://www.bilibili.com/video/$avOrBv"

                val data = model.data
                val stat = data.stat
                val imageStream = client.get<ByteArray>(model.data.pic).inputStream()

                subject.sendMessage(buildMessageChain {
                    add(imageStream.uploadAsImage(subject))
                    add(
                        "${data.title} / ${data.owner.name}\n$biliUrl\n"
                    )
                    add(
                        "⌘ ${stat.view} 👍 ${stat.like} ⓒ ${stat.coin} ⮬ ${stat.share}\n"
                    )
                    add("via antiBv")
                })
            } else logger.info("视频不存在")
        }
    }

    bot.eventChannel.subscribeGroupMessages {
        startsWith("提取封面", removePrefix = true, trim = true) { m ->
            var aid = m.toIntOrNull() ?: AvBv.bvToAv(m)?.toInt()
            var mg: String? = null
            while (aid == null) {
                subject.sendMessage("请告诉我av号或者bv号 [取消]")
                val msg = nextMessage(timeoutMillis = 3000) {
                    mg = message.content
                    message.content.toIntOrNull() != null || AvBv.bvToAv(message.content)?.toInt() != null
                }
                if (mg in listOf("算了", "取消")) {
                    return@startsWith
                }
                aid = msg.content.trim().toIntOrNull() ?: AvBv.bvToAv(msg.content)?.toInt()
            }
            val url = "https://api.bilibili.com/x/web-interface/view?aid=$aid"
            logger.info("Request $url")

            val client = KtorClient.getInstance() ?: return@startsWith
            val model = client.get<BiliViewModel>(url)

            logger.info("提取到图片url: ${model.data.pic}")
            client.get<ByteArray>(model.data.pic).inputStream().sendAsImageTo(getGroupOrFail(group.id))


        }

        Regex(""".*(?i)((av\d{5,13})|(BV\w{10}\??))(?-i).*""") matching regex@{
            biliView(it)
        }

        Regex(""".*(?i)cv(?-i)\d{5,7}.*""") matching regex@{ msg ->
            val cid = msg.substringAfter("cv").substringBefore("\"").toIntOrNull() ?: return@regex

            val client = KtorClient.getInstance() ?: return@regex

            val url = "https://api.bilibili.com/x/article/viewinfo?id=$cid"
            val model = client.get<BiliCvModel>(url)

            val data = model.data
            val stats = data.stats
            val biliUrl = "https://www.bilibili.com/read/cv$cid"
            subject.sendMessage(
                "${data.title}\n✍ ${data.authorName}\n👍 ${stats.like}  \uD83D\uDC4E ${stats.dislike}" +
                        "   ⓒ ${stats.coin}  ⮬ ${stats.share}\n$biliUrl\nvia antiCv"
            )
        }

        Regex(""".*b23\.tv/.*""") matching regex@{
            val result = Regex(""".*(https://b23\.tv/\w{6}).*""").matchEntire(it) ?: return@regex
            val b23Url = result.groupValues.getOrNull(1) ?: return@regex

            val client = KtorClient.getInstance() ?: return@regex
            val response = client.head<HttpResponse>(b23Url)
            val location = response.headers["location"] ?: return@regex

            biliView(location)
        }
    }
}