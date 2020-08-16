package plugins

import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.message.nextMessage
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import utils.network.OkHttpUtil
import utils.network.Requests
import utils.network.model.BiliViewModel
import utils.process.bili.AvBv
import java.io.IOException

@Suppress("BlockingMethodInNonBlockingContext")  // 哭了
fun Bot.bili() {
    // TODO 新番 / 新番时间表 | cv
    subscribeGroupMessages {
        startsWith("提取封面", removePrefix = true, trim = true) { m ->
            var aid = m.toIntOrNull() ?: AvBv.bvToAv(m)?.toInt()
            while (aid == null) {
                reply("请告诉我av号或者bv号")
                val msg = nextMessage {
                    message.content.toIntOrNull() != null || AvBv.bvToAv(message.content)?.toInt() != null
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
                            Requests.get(
                                it.data.pic,
                                object : Callback {
                                    override fun onFailure(call: Call, e: IOException) {
                                        logger.error("提取封面 下载图片 onFailure")
                                        launch { reply("图片请求失败") }
                                    }

                                    override fun onResponse(call: Call, response: Response) {
                                        logger.info("发送图片")
                                        launch {
                                            response.body?.byteStream()?.sendAsImage()
                                                ?: reply("没有下载到图片")
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            )
        }

        Regex("""\s*(?i)((av\d{5,13})|(BV\w{10}))\s*""") matching {
            val aid = it.drop(2).toIntOrNull() ?: AvBv.bvToAv(it)?.toInt()
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

        Regex("""\s*(?i)cv(?-i)\d{5,7}\s*""") matching {
            println("cv $it")
        }
    }
}