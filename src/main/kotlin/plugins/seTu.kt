package plugins

import Config.sauceNaoApiKey
import com.google.gson.Gson
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.code.parseMiraiCode
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.nextMessage
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import utils.data.CheckInData
import utils.data.MessageCacheData.messageCache
import utils.network.OkHttpUtil
import utils.network.Requests
import utils.network.model.LoliconSeTuModel
import utils.network.model.SauceNaoModel
import java.io.IOException
import java.net.URL

@Suppress("BlockingMethodInNonBlockingContext")  // 哭了
fun Bot.seTu() {
    suspend fun GroupMessageEvent.searchImage(message: String) {
        reply("少女祈祷中")
        val imageUrl = URL(message)
        val url =
            "https://saucenao.com/search.php?output_type=2&api_key=$sauceNaoApiKey&numres=1&url=$imageUrl"
        Requests.get(
            url,
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    logger.error("SauceNao api onFailure")
                }

                override fun onResponse(call: Call, response: Response) {
                    logger.info("SauceNao api 请求成功")
                    OkHttpUtil.gson.fromJson(
                        response.body?.string(),
                        SauceNaoModel::class.java
                    ).let { model ->
                        try {
                            val result = model.results.first()
                            val data = result.data
                            var catPixiv = "https://pixiv.cat/${result.data.pixivId}.jpg"
                            val head = Requests.head(catPixiv)
                            if (head?.code == 404) {
                                logger.info("$catPixiv 不止一张, 重新构造")
                                catPixiv = "https://pixiv.cat/${result.data.pixivId}-1.jpg"
                            } else {
                                logger.info("$catPixiv 只有一张, 继续请求")
                            }
                            Requests.get(
                                catPixiv,
                                object : Callback {
                                    override fun onFailure(call: Call, e: IOException) {
                                        logger.error("pixiv.cat 图片下载失败")
                                    }

                                    override fun onResponse(call: Call, response: Response) {
                                        launch {
                                            buildMessageChain {
                                                if (!response.isSuccessful) {
                                                    launch { reply("這個作品可能已被刪除，或無法取得。\n該当作品は削除されたか、存在しない作品IDです。") }
                                                    logger.info("图片不存在")
                                                    add(catPixiv)
                                                } else {
                                                    response.body?.byteStream()?.uploadAsImage()
                                                        ?.let { image ->
                                                            add(
                                                                image
                                                            )
                                                            logger.info("pixiv.cat 图片下载成功")
                                                        }
                                                }
                                                add(
                                                    "\n${data.title}\n${data.extUrls.first()}\nid: ${data.pixivId}" +
                                                            "\n${data.memberName}: ${data.memberId}\n相似度: ${result.header.similarity}%\n"
                                                )
                                                add("via SauceNao")
                                            }.send()
                                        }
                                    }

                                }
                            )
                        } catch (e: NoSuchElementException) {
                            logger.info("无结果 ${model.results}")
                            launch { reply("无结果") }
                        }
                    }
                }
            }
        )
    }

    subscribeGroupMessages {
        Regex("(?:来一?[点张份]?)?[色瑟涩]图来?") matching {
            reply("少女祈祷中")
            Requests.get(
                "https://api.lolicon.app/setu/",
                object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        logger.error("色图 api onFailure")
                        launch { reply("api 获取失败") }
                    }

                    override fun onResponse(call: Call, response: Response) {
                        Gson().fromJson(
                            response.body?.string(), LoliconSeTuModel::class.java
                        ).let {
                            try {
                                val url = it.data.first().url
                                Requests.get(
                                    url,
                                    object : Callback {
                                        override fun onFailure(call: Call, e: IOException) {
                                            logger.warning("色图 onFailure")
                                            launch { reply("图片获取失败, 也许是请求速度过快, 达到限制") }
                                        }

                                        override fun onResponse(call: Call, response: Response) {
                                            launch {
                                                if (response.isSuccessful) {
                                                    response.body?.byteStream()?.sendAsImage()
                                                } else {
                                                    logger.warning("色图下载失败")
                                                    reply("网络请求失败")
                                                }
                                            }
                                        }
                                    }
                                )
                            } catch (e: NoSuchElementException) {
                                launch { reply("请求速度过快, 达到限制") }
                            }
                        }
                    }
                }
            )
        }

        Regex("""\s*[pP][识搜]图\s*""") matching {
            reply("请发送图片")
            val messageChain = nextMessage(timeoutMillis = 120000) {
                message[Image] != null
            }
            messageChain[Image]?.queryUrl()?.let {
                this.searchImage(it)
            }
        }

        has<QuoteReply> { reply ->
            if (Regex(""".*[pP][识搜]图\s*""").matches(message.content)) {
                val msg = messageCache?.get(reply.source.id)
                val messageChain = msg?.parseMiraiCode()
                val image = messageChain?.get(Image)?.queryUrl()
                if (image != null) {
                    if(CheckInData(this).consumeCuprum(100)) {
                        this.searchImage(image)
                    } else {
                        reply("铜币不足, 识图取消, 铜币可由签到获得")
                    }
                } else {
                    reply("无法获取到图片, 请直接使用指令 [p识图]")
                }
            }
        }

        Regex("""\s*动[画漫][识搜]图\s*""") matching {

        }
    }
}