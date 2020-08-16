package plugins

import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.message.nextMessage
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import utils.process.bili.AvBv
import utils.network.OkHttpUtil
import utils.network.Requests
import utils.network.model.BiliCoverModel
import java.io.IOException

@Suppress("BlockingMethodInNonBlockingContext")  // 哭了
fun Bot.bili() {
    // TODO 新番 / 新番时间表 | 根据链接(av/bv/cv)自动返回相关信息
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
                            BiliCoverModel::class.java
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
    }
}