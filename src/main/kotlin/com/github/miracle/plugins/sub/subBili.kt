package com.github.miracle.plugins.sub

import com.github.miracle.utils.data.SubBiliCache
import com.github.miracle.utils.data.SubscribeData
import com.github.miracle.utils.database.BotDataBase
import com.github.miracle.utils.network.KtorClient
import com.github.miracle.utils.network.model.BiliLiveModel
import com.github.miracle.utils.tools.bili.BiliLiveRoom
import io.ktor.client.request.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.message.data.sendTo
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import java.io.InputStream
import java.time.Duration
import java.time.Instant
import java.util.*
import kotlin.concurrent.schedule

fun Bot.subBili() {
    eventChannel.subscribeGroupMessages {
        Regex("""\s*b订阅 +\w+\s*""") matching regex@{
            val msg = it.substringAfter("b订阅").trim()
            if (msg.isEmpty()) return@regex
            val bid = msg.toLongOrNull()
            if (bid == null) {
                subject.sendMessage("房间号是数字喔")
                return@regex
            } else {
                val live = BiliLiveRoom.getBiliLiveUname(bid)
                if (live == null) {
                    // 不存在
                    subject.sendMessage("订阅失败, 请确认房间号正确")
                    return@regex
                }
                val data = live.data
                if (SubscribeData.subscribe(group.id, bid.toString(), data.uname, BotDataBase.Platform.BILI)) {
                    // 已经插入到数据库里了
                    subject.sendMessage(
                        "${data.uname} :: ${data.roomId}\n订阅成功"
                    )
                } else {
                    subject.sendMessage("你已经订阅过了: $bid")
                }
            }
        }

        case("b订阅列表") {
            val list = SubscribeData.getPlatformSubList(group.id, BotDataBase.Platform.BILI)
            if (list == null) {
                subject.sendMessage("本群还没有订阅")
            } else {
                subject.sendMessage(
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
                subject.sendMessage("房间号是数字喔")
                return@regex
            } else {
                val success = SubscribeData.unsubscribe(group.id, bid.toString(), BotDataBase.Platform.BILI)
                if (success) subject.sendMessage("取订成功: $bid") else subject.sendMessage("你没有订阅过这个房间")
            }
        }
    }
    suspend fun Bot.sendBiliLive(bid: String, groupId: List<Long>, model: BiliLiveModel) {
        groupId.forEach {
            val bi = model.data.anchorInfo.baseInfo
            val ri = model.data.roomInfo
            coroutineScope {
                launch {
                    val client = KtorClient.getInstance() ?: return@launch
                    val stream = client.get<InputStream>(ri.keyframe)
                    val contact = getGroupOrFail(it)

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
    Timer().schedule(Date(), 20000) {
        val pair = SubBiliCache.nextSub()
        launch {
            val map = pair.first
            val cache = pair.second

            val bid = map.key
            val groupId = map.value

            val model = BiliLiveRoom.getBiliLive(bid.toLong()) ?: return@launch
            val status = model.data.roomInfo.liveStatus

            val live = if (status == 0) false else status == 1
            if (live) {
                if (cache[bid] != true) {
                    SubBiliCache.markLiving(bid)
                    sendBiliLive(bid, groupId, model)
                }
            } else {
                if (cache[bid] != false) SubBiliCache.markUnliving(bid)
            }
        }
    }
}

