package plugins

import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.message.nextMessage
import net.mamoe.mirai.utils.minutesToMillis
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import utils.network.OkHttpUtil
import utils.network.Requests
import utils.network.model.BiliCvModel
import utils.network.model.BiliViewModel
import utils.process.bili.AvBv
import java.io.IOException
import java.net.URL

@Suppress("BlockingMethodInNonBlockingContext")  // 哭了
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

            Requests.get(
                url,
                object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        logger.error("提取封面 api onFailure")
                        launch { reply("请求失败, 请确认av号") }
                    }

                    override fun onResponse(call: Call, response: Response) {
                        OkHttpUtil.gson.fromJson(
                            response.body?.string(),
                            BiliViewModel::class.java
                        ).let {
                            logger.info("提取到图片url: ${it.data.pic}")
                            val pic = URL(it.data.pic)
                            launch { pic.sendAsImage() }
                        }
                    }
                }
            )
        }

        Regex(""".*(?i)((av\d{5,13})|(BV\w{10}\??))(?-i).*""") matching {
            val avOrBv =
                Regex(""".*(?i)((?<av>av\d{5,13})|(?<bv>BV\w{10}))(?-i).*""").matchEntire(it)?.groupValues?.get(1)
            val aid = avOrBv?.drop(2)?.toIntOrNull() ?: avOrBv?.let { bv -> AvBv.bvToAv(bv)?.toInt() }
            if (aid != null) {
                val url = "https://api.bilibili.com/x/web-interface/view?aid=$aid"
                logger.info("Request $url")
                Requests.get(
                    url,
                    object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            logger.error("bilibili information onFailure")
                        }

                        override fun onResponse(call: Call, response: Response) {
                            OkHttpUtil.gson.fromJson(
                                response.body?.string(),
                                BiliViewModel::class.java
                            ).let { model ->
                                if (model.code == 0) {
                                    val data = model.data
                                    val stat = data.stat
                                    Requests.get(
                                        model.data.pic,
                                        object : Callback {
                                            override fun onFailure(call: Call, e: IOException) {
                                                logger.error("AvBv 下载图片 onFailure")
                                            }

                                            override fun onResponse(call: Call, response: Response) {
                                                logger.info("图片下载完成")
                                                val biliUrl = "https://www.bilibili.com/video/$it"
                                                launch {
                                                    buildMessageChain {
                                                        response.body?.byteStream()?.uploadAsImage()
                                                            ?.let { image -> add(image) }
                                                        add(
                                                            "${data.title} / ${data.owner.name}\n${data.desc}\n$biliUrl\n"
                                                        )
                                                        add(
                                                            "⌘ ${stat.view} 👍 ${stat.like} ⓒ ${stat.coin} ⮬ ${stat.share}\n"
                                                        )
                                                        add("via antiBv")
                                                    }.send()
                                                }
                                            }
                                        }
                                    )
                                } else {
                                    logger.info("视频不存在")
                                }
                            }
                        }
                    }
                )
            }
        }

        Regex(""".*(?i)cv(?-i)\d{5,7}.*""") matching { msg ->
            val cid = msg.substringAfter("cv").substringBefore("\"").toIntOrNull()
            cid?.let {
                Requests.get("https://api.bilibili.com/x/article/viewinfo?id=$cid",
                    object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            logger.error("bili cv api onFailure")
                        }

                        override fun onResponse(call: Call, response: Response) {
                            OkHttpUtil.gson.fromJson(
                                response.body?.string(),
                                BiliCvModel::class.java
                            ).let {
                                val data = it.data
                                val stats = data.stats
                                val biliUrl = "https://www.bilibili.com/read/cv$cid"
                                launch {
                                    reply(
                                        "${data.title}\n✍ ${data.authorName}\n👍 ${stats.like}  \uD83D\uDC4E ${stats.dislike}" +
                                                "   ⓒ ${stats.coin}  ⮬ ${stats.share}\n$biliUrl\nvia antiCv"
                                    )
                                }
                            }
                        }

                    })
            }
        }

        Regex("""\s*新番(时间表)?\s*""") matching {
            // TODO 新番 / 新番时间表
        }
    }
}