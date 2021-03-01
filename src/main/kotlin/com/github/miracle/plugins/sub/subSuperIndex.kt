package com.github.miracle.plugins.sub

import com.github.miracle.MiracleConstants
import com.github.miracle.utils.data.SubWeiboCache
import com.github.miracle.utils.data.SubscribeData
import com.github.miracle.utils.database.BotDataBase
import com.github.miracle.utils.database.BotDataBase.Platform.SUPER
import com.github.miracle.utils.network.KtorClient
import com.github.miracle.utils.network.model.WeiboResponseModel
import io.ktor.client.request.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.message.data.sendTo
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule

private const val lizzieBar = 637538362L

/**
 * 获取novel信息
 */
suspend fun getSuperInfo(sid: String): WeiboResponseModel? {
    val client = KtorClient.getInstance() ?: return null
    val url = MiracleConstants.SUB_API_URL + "/super-index-rss/"
    return try {
        client.get<WeiboResponseModel>(url + sid)
    } catch (e: SerializationException) {
        null
    }
}

fun Bot.subSuperIndex() {
    eventChannel.subscribeGroupMessages {
        Regex("""\s*超话订阅 +\w+\s*""") matching regex@{
            val sid = it.substringAfter("超话订阅").trim()
            if (sid.isEmpty()) {
                return@regex
            } else {
                val superModel = getSuperInfo(sid)
                if (superModel == null) {
                    // 不存在
                    subject.sendMessage("没有查询到信息, 超话id为网页端超话链接即https://weibo.com/p/[id]/super_index中间的id部分")
                    return@regex
                } else {
                    when (superModel.status) {
                        0 -> {
                            if (SubscribeData.subscribe(
                                    group.id, sid, superModel.weiboTitle,
                                    BotDataBase.Platform.SUPER
                                )
                            ) {
                                SubWeiboCache.refreshSuperCache()
                                SubWeiboCache.setLastSuperUpdateTime(sid, System.currentTimeMillis())
                                subject.sendMessage(
                                    "${superModel.weiboTitle} : \n订阅成功"
                                )
                            } else {
                                subject.sendMessage("你已经订阅过了: $sid")
                            }
                        }
                        else -> {
                            subject.sendMessage("订阅失败, 请确认超话id正确")
                        }
                    }
                }
            }
        }

        case("超话订阅列表") {
            val list = SubscribeData.getPlatformSubList(group.id, SUPER)
            if (list == null) {
                subject.sendMessage("本群还没有订阅超话")
            } else {
                subject.sendMessage(
                    list.joinToString("\n") {
                        "${it.first} - ${it.second}"
                    }
                )
            }
        }

        Regex("""\s*超话取订 +\w+\s*""") matching regex@{
            val sid = it.substringAfter("超话取订").trim()
            if (sid.isEmpty()) {
                return@regex
            } else {
                val success = SubscribeData.unsubscribe(group.id, sid, BotDataBase.Platform.SUPER)
                SubWeiboCache.refreshSuperCache()
                if (success) subject.sendMessage("取订成功: $sid") else subject.sendMessage("本群没有订阅该超话")
            }
        }
    }

    Timer().schedule(Date(), period = TimeUnit.MINUTES.toMillis(1)) {
        val superItem = SubWeiboCache.nextSubSuper()
        launch {
            val sid = superItem.key // nid
            val groupIdList = superItem.value

            val model = getSuperInfo(sid) ?: return@launch
            if (SubWeiboCache.getLastSuperUpdateTime(sid) != 0L) {
                sendWeiboUpdate(SUPER, sid, groupIdList, model)
                SubWeiboCache.setLastSuperUpdateTime(sid, System.currentTimeMillis())
            } else{
                SubWeiboCache.setLastSuperUpdateTime(sid, System.currentTimeMillis())
            }
        }
    }
}
