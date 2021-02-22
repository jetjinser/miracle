package com.github.miracle.plugins.sub

import com.github.miracle.MiracleConstants
import com.github.miracle.utils.data.SubNovelCache
import com.github.miracle.utils.data.SubSuperCache
import com.github.miracle.utils.data.SubscribeData
import com.github.miracle.utils.database.BotDataBase
import com.github.miracle.utils.network.KtorClient
import com.github.miracle.utils.network.model.SuperIndexModel
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
suspend fun getSuperInfo(sid: String): SuperIndexModel? {
    val client = KtorClient.getInstance() ?: return null
    val url = MiracleConstants.SUB_API_URL + "/super-index-rss/"
    return try {
        client.get<SuperIndexModel>(url + sid)
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
                    // 0: 连载中 1: 已完结 2: 不存在
                    when (superModel.status) {
                        0 -> {
                            if (SubscribeData.subscribe(group.id, sid, superModel.superTitle,
                                    BotDataBase.Platform.SUPER
                                )) {
                                // 已经插入到数据库里了
//                                NovelSubCache.markLastChapter(nid, superModel.chapterId)
                                subject.sendMessage(
                                    "${superModel.superTitle} : \n订阅成功"
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
            val list = SubscribeData.getPlatformSubList(group.id, BotDataBase.Platform.SUPER)
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
                if (success) subject.sendMessage("取订成功: $sid") else subject.sendMessage("本群没有订阅该超话")
            }
        }
    }

    suspend fun Bot.sendSuperUpdate(sId:String, groupId: List<Long>, model: SuperIndexModel) {
        groupId.forEach {
            coroutineScope {
                launch {
                    val contact = getGroupOrFail(it)
                    val client = KtorClient.getInstance()
                    client?.let {
                        if (model.status == 0) {
                            model.result.forEach { model ->
                                println(model)
                                if (model.time_unix > SubSuperCache.getLastUpdateTime(sId)) {
                                    buildMessageChain {
                                        add("${model.content}\n")
                                        if (model.ttarticleLink.isNotEmpty()) {
                                            add("${model.ttarticleLink}\n")
                                        }
                                        if (model.imgUrls.isNotEmpty()) {
                                            model.imgUrls.forEach {
                                                val byteArray = client.get<ByteArray>(it)
                                                add(byteArray.inputStream().uploadAsImage(contact))
                                            }
                                        }
                                        if (model.extra.isNotEmpty()) {
                                            add("${model.extra}\n")
                                        }
                                        add("by ${model.author} at ${model.time}\n")
                                    }.sendTo(contact)
                                }
                            }
                        }
                    }
                    delay(2000)
                }
            }
        }
    }
    Timer().schedule(Date(), period = TimeUnit.MINUTES.toMillis(1)) {
        val superItem = SubSuperCache.nextSub()
        launch {
            val sid = superItem.key // nid
            val groupIdList = superItem.value

            val model = getSuperInfo(sid) ?: return@launch
            if (SubSuperCache.getLastUpdateTime(sid) != 0L) {
                sendSuperUpdate(sid, groupIdList, model)
            }
            SubSuperCache.setLastUpdateTime(sid, System.currentTimeMillis())
        }
    }
}
