package com.github.miracle.plugins

import io.ktor.client.request.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.message.nextMessage
import net.mamoe.mirai.utils.minutesToMillis
import com.github.miracle.utils.network.KtorClient
import com.github.miracle.utils.network.model.BiliCvModel
import com.github.miracle.utils.network.model.BiliViewModel
import com.github.miracle.utils.process.bili.AvBv

fun Bot.bili() {
    subscribeGroupMessages {
        startsWith("提取封面", removePrefix = true, trim = true) { m ->
            var aid = m.toIntOrNull() ?: AvBv.bvToAv(m)?.toInt()
            var mg: String? = null
            while (aid == null) {
                reply("请告诉我av号或者bv号 [取消]")
                val msg = nextMessage(timeoutMillis = 3.minutesToMillis) {
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
            client.get<ByteArray>(model.data.pic).inputStream().sendAsImage()

            intercept()
        }

        Regex(""".*(?i)((av\d{5,13})|(BV\w{10}\??))(?-i).*""") matching regex@{
            val avOrBv =
                Regex(""".*(?i)((?<av>av\d{5,13})|(?<bv>BV\w{10}))(?-i).*""").matchEntire(it)?.groupValues?.get(1)
            val aid = avOrBv?.drop(2)?.toIntOrNull() ?: avOrBv?.let { bv -> AvBv.bvToAv(bv)?.toInt() }
            if (aid != null) {
                val url = "https://api.bilibili.com/x/web-interface/view?aid=$aid"
                logger.info("Request $url")

                val client = KtorClient.getInstance() ?: return@regex

                val model = client.get<BiliViewModel>(url)
                if (model.code == 0) {
                    val data = model.data
                    val stat = data.stat
                    val biliUrl = "https://www.bilibili.com/video/$it"
                    val imageStream = client.get<ByteArray>(model.data.pic).inputStream()

                    buildMessageChain {
                        add(imageStream.uploadAsImage())
                        add(
                            "${data.title} / ${data.owner.name}\n${data.desc}\n$biliUrl\n"
                        )
                        add(
                            "⌘ ${stat.view} 👍 ${stat.like} ⓒ ${stat.coin} ⮬ ${stat.share}\n"
                        )
                        add("via antiBv")
                    }.send()

                } else {
                    logger.info("视频不存在")
                }
            }
            intercept()
        }

        Regex(""".*(?i)cv(?-i)\d{5,7}.*""") matching regex@{ msg ->
            val cid = msg.substringAfter("cv").substringBefore("\"").toIntOrNull() ?: return@regex

            val client = KtorClient.getInstance() ?: return@regex

            val url = "https://api.bilibili.com/x/article/viewinfo?id=$cid"
            val model = client.get<BiliCvModel>(url)

            val data = model.data
            val stats = data.stats
            val biliUrl = "https://www.bilibili.com/read/cv$cid"
            reply(
                "${data.title}\n✍ ${data.authorName}\n👍 ${stats.like}  \uD83D\uDC4E ${stats.dislike}" +
                        "   ⓒ ${stats.coin}  ⮬ ${stats.share}\n$biliUrl\nvia antiCv"
            )

            intercept()
        }

        Regex("""\s*新番(时间表)?\s*""") matching {
            // TODO 新番 / 新番时间表
        }
    }
}