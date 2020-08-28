package com.github.miracle.plugins

import com.github.miracle.SecretConfig.loliconSeTuApiKey
import com.github.miracle.SecretConfig.sauceNaoApiKey
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.code.parseMiraiCode
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.nextMessage
import net.mamoe.mirai.utils.minutesToMillis
import com.github.miracle.utils.data.CheckInData
import com.github.miracle.utils.data.MessageCacheData.messageCache
import com.github.miracle.utils.network.KtorClient
import com.github.miracle.utils.network.model.LoliconSeTuModel
import com.github.miracle.utils.network.model.SauceNaoModel
import com.github.miracle.utils.network.model.TraceMoeInfoModel
import com.github.miracle.utils.network.model.TraceMoeModel
import java.io.InputStream
import java.text.DecimalFormat

fun Bot.seTu() {
    suspend fun GroupMessageEvent.getSeTu() {
        reply("少女祈祷中")

        val client = KtorClient.getInstance() ?: return

        // TODO 缓存
        val url = "https://api.lolicon.app/setu/?r18=0&apikey=$loliconSeTuApiKey"
        val response = client.get<HttpResponse>(url)

        val model = Json.decodeFromString<LoliconSeTuModel>(response.readText())

        if (response.status.value == 429) {
            logger.info("达到请求上限: ${model.quotaMinTtl}s 后恢复")
            reply("达到请求上限, 距离下一次调用额度恢复还有 ${model.quotaMinTtl}s")
            return
        }

        val data = model.data.first()

        val imageResponse = client.get<HttpResponse>(data.url)

        if (imageResponse.status.value == 200) {
            buildMessageChain {
                add(imageResponse.receive<InputStream>().uploadAsImage())
                add("\npid: ${data.pid}\nuid: ${data.uid}\n")
                add("via seTu")
            }.send()
        } else {
            logger.warning("色图下载失败")
            reply("网络请求失败")
        }
    }

    suspend fun GroupMessageEvent.searchSeTu(imageUrl: String) {
        reply("少女祈祷中")
        val url =
            "https://saucenao.com/search.php?output_type=2&api_key=$sauceNaoApiKey&numres=1&url=$imageUrl"

        val client = KtorClient.getInstance() ?: return

        val model = client.get<SauceNaoModel>(url)

        val result = model.results.first()
        val data = result.data
        var catPixiv = "https://pixiv.cat/${result.data.pixivId}.jpg"

        val headResponse = client.head<HttpResponse>(catPixiv)
        if (headResponse.status.value == 404) {
            logger.info("$catPixiv 不止一张, 重新构造")
            catPixiv = "https://pixiv.cat/${result.data.pixivId}-1.jpg"
        } else {
            logger.info("$catPixiv 只有一张, 继续请求")
        }

        val imageResponse = client.get<HttpResponse>(catPixiv)

        buildMessageChain {
            if (imageResponse.status.value == 404) {
                reply("這個作品可能已被刪除，或無法取得。\n該当作品は削除されたか、存在しない作品IDです。")
                logger.info("图片不存在")
                add(catPixiv)
            } else {
                add(imageResponse.receive<InputStream>().uploadAsImage())
                logger.info("pixiv.cat 图片下载成功")
            }
            add(
                "\n${data.title}\n${data.extUrls.first()}\nid: ${data.pixivId}" +
                        "\n${data.memberName}: ${data.memberId}\n相似度: ${result.header.similarity}%\n"
            )
            add("via SauceNao")
        }.send()
    }

    suspend fun GroupMessageEvent.searchAnimation(imageUrl: String) {
        reply("少女祈祷中")
        val client = KtorClient.getInstance() ?: return

        val url = "https://trace.moe/api/search?url=$imageUrl"
        val response = client.get<HttpResponse>(url)

        val searchModel = KtorClient.json.decodeFromString<TraceMoeModel>(response.readText())

        if (response.status.value == 429) {
            logger.info("达到请求上限: ${searchModel.limitTtl}s 后恢复")
            reply(
                "达到请求上限, 距离下一次调用额度恢复还有 ${searchModel.limit}s, 恢复后有10次请求额度\n" +
                        "总共还有 ${searchModel.quota} 次调用额度, ${searchModel.quotaTtl}s 后恢复"
            )
            return
        }

        val doc = searchModel.docs.first()

        val infoUrl = "https://trace.moe/info?anilist_id=${doc.aniListId}"
        val infoModel = client.get<List<TraceMoeInfoModel>>(infoUrl).first()

        val descWithHtml = infoModel.description
        val desc = Regex("<[^>]+>").replace(descWithHtml, "").substring(0..144)

        buildMessageChain {
            if (!doc.isAdult) {
                val imageStream = client.get<ByteArray>(infoModel.coverImage.large)
                add(imageStream.inputStream().uploadAsImage())
            } else add("成人向, 不显示图片\n")

            add("${doc.title} / ${doc.titleChinese} #${doc.episode}\n")
            add("相似度: ${DecimalFormat("#.00").format(doc.similarity * 100) + "%"}\n")
            add("$desc...")
            add("\nhttps://anilist.co/anime/${doc.aniListId}\n")
            add("via TraceMoe")
        }.send()

    }

    subscribeGroupMessages {
        Regex("(?:来一?[点张份]?)?[色瑟涩]图来?") matching { getSeTu(); intercept() }

        Regex("""\s*[pP][识搜]图\s*""") matching {
            reply("请发送你要搜索的二次元图片")
            val messageChain = nextMessage(timeoutMillis = 3.minutesToMillis) {
                message[Image] != null
            }
            messageChain[Image]?.queryUrl()?.let {
                this.searchSeTu(it)
            }
            intercept()
        }

        has<QuoteReply> { reply ->
            if (Regex(""".*[pP][识搜]图\s*""").matches(message.content)) {
                val msg = messageCache?.get(reply.source.id)
                val messageChain = msg?.parseMiraiCode()
                val image = messageChain?.get(Image)?.queryUrl()
                if (image != null) {
                    val pair = CheckInData(this).consumeCuprum(100)
                    if (pair.first) {
                        this.searchSeTu(image)
                    } else {
                        reply("铜币不足 100 , 识图取消, 铜币可由签到获得\n当前铜币: ${pair.second}")
                    }
                } else {
                    reply("无法获取到图片, 请直接使用指令 [p识图]")
                }
            } else if (Regex(""".*动[画漫][识搜]图\s*""").matches(message.content)) {
                val msg = messageCache?.get(reply.source.id)
                val messageChain = msg?.parseMiraiCode()
                val image = messageChain?.get(Image)?.queryUrl()
                if (image != null) {
                    val pair = CheckInData(this).consumeCuprum(100)
                    if (pair.first) {
                        this.searchAnimation(image)
                    } else {
                        reply("铜币不足 100 , 识图取消, 铜币可由签到获得\n当前铜币: ${pair.second}")
                    }
                } else {
                    reply("无法获取到图片, 请直接使用指令 [动画识图]")
                }
            }
            intercept()
        }

        val seTuCome = "{B407F708-A2C6-A506-3420-98DF7CAC4A57}.mirai"
        has<Image> {
            if (message[Image]?.imageId == seTuCome) getSeTu()
            intercept()
        }

        Regex("""\s*动[画漫][识搜]图\s*""") matching {
            reply("请发送你要搜索的动画截图")
            val messageChain = nextMessage(timeoutMillis = 3.minutesToMillis) {
                message[Image] != null
            }
            messageChain[Image]?.queryUrl()?.let {
                searchAnimation(it)
            }
            intercept()
        }
    }
}