package com.github.miracle.plugins.sub

import com.github.miracle.utils.data.SubRedditCache
import com.github.miracle.utils.data.SubscribeData
import com.github.miracle.utils.database.BotDataBase
import com.github.miracle.utils.network.KtorClient
import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import io.ktor.client.request.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.message.data.sendTo
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import org.jsoup.Jsoup
import java.net.URL
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule


/**
 * 获取Lofter Tag信息
 */
suspend fun getRedditInfo(rUrl: String): SyndFeed? {
    return try {
        val feed: SyndFeed = SyndFeedInput().build(XmlReader(URL(rUrl)))
        feed
    } catch (e: SerializationException) {
        null
    }
}

fun Bot.subReddit() {
    val rUrl = SubscribeData.getAllSubObject(BotDataBase.SubPlatform.REDDIT)
    rUrl?.forEach {
        it?.let {
            SubRedditCache.setLastRedditRssUpdateTime(it, System.currentTimeMillis())
        }
    }
    eventChannel.subscribeGroupMessages {
        Regex("""\s*r订阅 .*""") matching regex@{
            val rUrl = it.substringAfter("r订阅").trim()
            if (rUrl.isEmpty()) {
                return@regex
            } else {
                val rdRes = getRedditInfo(rUrl)
                if (rdRes == null) {
                    // 不存在
                    subject.sendMessage("没有查询到rss信息, 订阅reddit频道请查看详细指南：\nhttps://shimo.im/docs/6cKr6jrwCQhTxDXC/") // TODO
                    return@regex
                } else {
                    if (SubscribeData.subscribe(
                            group.id, rUrl, rdRes.title,
                            BotDataBase.SubPlatform.REDDIT
                        )
                    ) {
                        SubRedditCache.refreshRedditRssCache()
                        SubRedditCache.setLastRedditRssUpdateTime(rUrl, System.currentTimeMillis())
                        subject.sendMessage(
                            "${rdRes.title} : \n订阅成功"
                        )
                    } else {
                        subject.sendMessage("你已经订阅过了: $rUrl")
                    }
                }
            }
        }
//
        case("r订阅列表") {
            val list = SubscribeData.getPlatformSubList(group.id, BotDataBase.SubPlatform.REDDIT)
            if (list == null) {
                subject.sendMessage("本群还没有订阅reddit rss")
            } else {
                subject.sendMessage(
                    list.joinToString("\n") {
                        "${it.first} - ${it.second}"
                    }
                )
            }
        }

        Regex("""\s*r取订 .*""") matching regex@{
            val rUrl = it.substringAfter("r取订").trim()
            if (rUrl.isEmpty()) {
                return@regex
            } else {
                val success =
                    SubscribeData.unsubscribe(group.id, rUrl, BotDataBase.SubPlatform.REDDIT)
                SubRedditCache.refreshRedditRssCache()
                if (success) subject.sendMessage("取订成功: $rUrl") else subject.sendMessage("本群没有订阅该rss")
            }
        }
    }
    suspend fun sendRedditUpdate(groupId: List<Long>, model: SyndEntry) {
        val doc = Jsoup.parse(model.contents[0].value)
        val content = doc.text();
        print(content);
        val imgs = doc.select("img");
        print(imgs);
        launch {
            val client = KtorClient.getInstance() ?: return@launch
            groupId.forEach {
                val contact = getGroupOrFail(it)

                buildMessageChain {
                    add("${model.title}\n")
                    add("${content}\n")
                    if (imgs.size > 0) {
                        imgs.forEachIndexed { index, img ->
                            if (index < 5) {
                                val byteArray = client.get<ByteArray>(img.absUrl("src"))
                                add(byteArray.inputStream().uploadAsImage(contact))
                            } else {
                                add("[图片]")
                            }
                        }
                    }
                    add("${model.link}\n")
                    add("by ${model.authors[0].uri} at ${model.updatedDate}")
                }.sendTo(contact)
                delay(2000)
            }
        }
    }

    Timer().schedule(Date(), period = TimeUnit.MINUTES.toMillis(5)) {
        val rssItem = SubRedditCache.nextSubRedditRss()
        print(rssItem)
        launch {
            val rUrl = rssItem.key // nid
            val groupIdList = rssItem.value
            val lastTime = SubRedditCache.getLastRedditRssUpdateTime(rUrl)
            if (lastTime == 0L) {
                SubRedditCache.setLastRedditRssUpdateTime(rUrl, System.currentTimeMillis())
                return@launch
            }
            val rssFeed = getRedditInfo(rUrl) ?: return@launch
            rssFeed.entries.forEach { entry ->
                val timeUnix = entry.updatedDate.time
                if (timeUnix > lastTime) {
                    sendRedditUpdate(groupIdList, entry)
                }
            }
            SubRedditCache.setLastRedditRssUpdateTime(rUrl, System.currentTimeMillis())
        }
    }
}