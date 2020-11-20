package com.github.miracle.plugins

import com.github.miracle.utils.data.BiliSubData
import com.github.miracle.utils.network.KtorClient
import com.github.miracle.utils.network.model.BiliCvModel
import com.github.miracle.utils.network.model.BiliLiveModel
import com.github.miracle.utils.network.model.BiliViewModel
import com.github.miracle.utils.tools.bili.AvBv
import com.github.miracle.utils.tools.bili.BiliLiveRoom
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.message.data.sendTo
import net.mamoe.mirai.message.nextMessage
import net.mamoe.mirai.message.uploadAsImage
import net.mamoe.mirai.utils.minutesToMillis
import net.mamoe.mirai.utils.secondsToMillis
import java.io.InputStream
import java.time.Duration
import java.time.Instant
import java.util.*
import kotlin.concurrent.schedule

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

                buildMessageChain {
                    add(imageStream.uploadAsImage())
                    add(
                        "${data.title} / ${data.owner.name}\n$biliUrl\n"
                    )
                    add(
                        "⌘ ${stat.view} 👍 ${stat.like} ⓒ ${stat.coin} ⮬ ${stat.share}\n"
                    )
                    add("via antiBv")
                }.send()
            } else logger.info("视频不存在")
        }
    }

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
            reply(
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


        // bilibili 订阅 region
        Regex("""\s*b订阅 +\w+\s*""") matching regex@{
            val msg = it.substringAfter("b订阅").trim()
            if (msg.isEmpty()) return@regex
            val bid = msg.toLongOrNull()
            if (bid == null) {
                reply("房间号是数字喔")
                return@regex
            } else {
                val live = BiliLiveRoom.getBiliLiveUname(bid)
                if (live == null) {
                    // 不存在
                    reply("订阅失败, 请确认房间号正确")
                    return@regex
                }

                if (BiliSubData.subscribe(group.id, bid)) {
                    // 已经插入到数据库里了
                    val data = live.data
                    reply(
                        "${data.uname} :: ${data.roomId}\n订阅成功"
                    )
                } else {
                    reply("你已经订阅过了: $bid")
                }
            }
        }

        case("b订阅列表") {
            val list = BiliSubData.getSubList(group.id)
            if (list == null) {
                reply("本群还没有订阅")
            } else {
                reply(
                    list.joinToString("\n") {
                        "${it.first.toString().padEnd(8, ' ')} - ${it.second}"
                    }
                )
            }
        }

        Regex("""\s*b取订 +\w+\s*""") matching regex@{
            val msg = it.substringAfter("b取订").trim()
            if (msg.isEmpty()) return@regex
            val bid = msg.toLongOrNull()
            if (bid == null) {
                reply("房间号是数字喔")
                return@regex
            } else {
                val success = BiliSubData.unsubscribe(group.id, bid)
                if (success) reply("取订成功: $bid") else reply("你没有订阅过这个房间")
            }
        }
    }

    suspend fun Bot.sendBiliLive(bid: Long, groupId: List<Long>, model: BiliLiveModel) {
        groupId.forEach {
            val bi = model.data.anchorInfo.baseInfo
            val ri = model.data.roomInfo
            coroutineScope {
                launch {
                    val client = KtorClient.getInstance() ?: return@launch
                    val stream = client.get<InputStream>(ri.keyframe)
                    val contact = getGroup(it)

                    val liveStartInstant = Instant.ofEpochSecond(ri.liveStartTime.toLong())
                    val now = Instant.now()
                    val timeElapsed = Duration.between(liveStartInstant, now)

                    buildMessageChain {
                        add(stream.uploadAsImage(contact))
                        add("${ri.title} / ${bi.uname}\n")
                        add("https://live.bilibili.com/$bid\n")
                        add("直播开始时间: ${timeElapsed.seconds}秒 前")
                    }.sendTo(contact)
                    delay(2000)
                }
            }
        }
    }

    Timer().schedule(Date(), 15.secondsToMillis) {
        val pair = BiliSubData.nextSub()
        launch {
            val map = pair.first
            val cache = pair.second

            val bid = map.key
            val groupId = map.value

            val model = BiliLiveRoom.getBiliLive(bid) ?: return@launch
            val status = model.data.roomInfo.liveStatus

            val live = if (status == 0) false else status == 1
            if (live) {
                if (cache[bid] != true) {
                    BiliSubData.markLiving(bid)
                    sendBiliLive(bid, groupId, model)
                }
            } else {
                if (cache[bid] != false) BiliSubData.markUnliving(bid)
            }
        }
    }
    // end region
}