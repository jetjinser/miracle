package plugins

import Config.loliconSeTuApiKey
import Config.sauceNaoApiKey
import com.google.gson.Gson
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.code.parseMiraiCode
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.nextMessage
import net.mamoe.mirai.utils.minutesToMillis
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
import java.math.BigInteger
import java.net.URL
import java.security.MessageDigest

@Suppress("BlockingMethodInNonBlockingContext")  // 哭了
fun Bot.seTu() {
    suspend fun GroupMessageEvent.getSeTu() {
        reply("少女祈祷中")
        Requests.get(
            "https://api.lolicon.app/setu/?r18=0&apikey=$loliconSeTuApiKey",
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    logger.error("色图 api onFailure")
                    launch { reply("api 请求失败") }
                }

                override fun onResponse(call: Call, response: Response) {
                    Gson().fromJson(
                        response.body?.string(), LoliconSeTuModel::class.java
                    ).let {
                        if (response.code == 429) {
                            logger.info("达到请求上限")
                            launch { reply("达到请求上限, 距离下一次调用额度恢复还有 ${it.quotaMinTtl}s") }
                            return
                        }
                        val data = it.data.first()
                        val url = data.url
                        Requests.get(
                            url,
                            object : Callback {
                                override fun onFailure(call: Call, e: IOException) {
                                    logger.warning("色图 onFailure")
                                    launch {
                                        reply("图片下载失败\n這個作品可能已被刪除，或無法取得。\n該当作品は削除されたか、存在しない作品IDです。")
                                    }
                                }

                                override fun onResponse(call: Call, response: Response) {
                                    launch {
                                        if (response.isSuccessful) {
                                            buildMessageChain {
                                                response.body?.byteStream()?.uploadAsImage()
                                                    ?.let { image -> add(image) }
                                                add("\npid: ${data.pid}\nuid: ${data.uid}\n")
                                                add("via seTu")
                                            }.send()
                                        } else {
                                            logger.warning("色图下载失败")
                                            reply("网络请求失败")
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        )
    }

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
        Regex("(?:来一?[点张份]?)?[色瑟涩]图来?") matching { getSeTu() }

        Regex("""\s*[pP][识搜]图\s*""") matching {
            reply("请发送图片")
            val messageChain = nextMessage(timeoutMillis = 3.minutesToMillis) {
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
                    val cuprum = CheckInData(this).consumeCuprum(100)
                    if (cuprum != null) {
                        this.searchImage(image)
                    } else {
                        reply("铜币不足 100 , 识图取消, 铜币可由签到获得\n当前铜币: $cuprum")
                    }
                } else {
                    reply("无法获取到图片, 请直接使用指令 [p识图]")
                }
            }
        }

        val seTuCome = "{B407F708-A2C6-A506-3420-98DF7CAC4A57}.mirai"
        has<Image> {
            if (message[Image]?.imageId == seTuCome) getSeTu()
        }

        Regex("""\s*动[画漫][识搜]图\s*""") matching {

        }
    }
}