package plugins

import Config.sauceNaoApiKey
import com.google.gson.Gson
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.code.parseMiraiCode
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.nextMessage
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import utils.data.MessageCacheData.messageCache
import utils.network.OkHttpUtil
import utils.network.Requests
import utils.network.model.LoliconSeTuModel
import utils.network.model.SauceNaoModel
import java.io.IOException
import java.lang.IllegalStateException
import java.net.URL

@Suppress("BlockingMethodInNonBlockingContext")  // 哭了
fun Bot.seTu() {
    subscribeGroupMessages {
        Regex("(?:来一?[点张份]?)?[色瑟涩]图来?") matching {
            reply("少女祈祷中")
            Requests.get("https://api.lolicon.app/setu/",
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
                                Requests.get(url,
                                    object : Callback {
                                        override fun onFailure(call: Call, e: IOException) {
                                            logger.warning("色图 onFailure")
                                            launch { reply("图片获取失败") }
                                        }

                                        override fun onResponse(call: Call, response: Response) {
                                            launch {
                                                if (response.isSuccessful) {
                                                    response.body?.byteStream()?.sendAsImage()
                                                } else {
                                                    "网络请求失败".let { info ->
                                                        logger.warning("色图 $info")
                                                        reply(info)
                                                    }
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
                reply("少女祈祷中")
                val imageUrl = URL(it)
                val url = "https://saucenao.com/search.php?output_type=2&api_key=$sauceNaoApiKey&numres=1&url=$imageUrl"
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
                                    val catPixiv = "https://pixiv.cat/${result.data.pixivId}.jpg"
                                    Requests.get(
                                        catPixiv,
                                        object : Callback {
                                            override fun onFailure(call: Call, e: IOException) {
                                                logger.error("pixiv.cat 图片下载失败")
                                            }

                                            override fun onResponse(call: Call, response: Response) {
                                                logger.info("pixiv.cat 图片下载成功")
                                                launch {
                                                    buildMessageChain {
                                                        response.body?.byteStream()?.uploadAsImage()?.let { image ->
                                                            add(
                                                                image
                                                            )
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
        }

        has<QuoteReply> { reply ->
            if (Regex(""".*[pP][识搜]图\s*""").matches(message.content)) {
                val msg = messageCache?.get(reply.source.id)
                println(messageCache)
                println(msg)
                val messageChain = msg?.parseMiraiCode()
                println(messageChain?.get(Image))
                messageChain?.get(Image)?.queryUrl()?.let {
                    reply("少女祈祷中")
                    val imageUrl = URL(it)
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
                                        val catPixiv = "https://pixiv.cat/${result.data.pixivId}.jpg"
                                        Requests.get(
                                            catPixiv,
                                            object : Callback {
                                                override fun onFailure(call: Call, e: IOException) {
                                                    logger.error("pixiv.cat 图片下载失败")
                                                }

                                                override fun onResponse(call: Call, response: Response) {
                                                    logger.info("pixiv.cat 图片下载成功")
                                                    launch {
                                                        buildMessageChain {
                                                            try {
                                                                response.body?.byteStream()?.uploadAsImage()
                                                                    ?.let { image ->
                                                                        add(
                                                                            image
                                                                        )
                                                                    }
                                                            } catch (e: IllegalStateException) {
                                                                add(catPixiv)
                                                                // TODO 不止一张图片 [catPixiv] 需要对应修改
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
            }
        }

        Regex("""\s*动[画漫][识搜]图\s*""") matching {
            // TODO trace.moe
        }
    }
}