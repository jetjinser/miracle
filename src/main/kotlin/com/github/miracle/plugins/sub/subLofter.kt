package com.github.miracle.plugins.sub

import com.github.miracle.MiracleConstants
import com.github.miracle.utils.data.SubLofterCache
import com.github.miracle.utils.data.SubscribeData
import com.github.miracle.utils.database.BotDataBase
import com.github.miracle.utils.network.KtorClient
import com.github.miracle.utils.network.model.LofterResponseModel
import io.ktor.client.request.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.message.data.sendTo
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule

/**
 * 获取Lofter Tag信息
 */
suspend fun getLofterInfo(tid: String): LofterResponseModel? {
    val client = KtorClient.getInstance() ?: return null
    val url = MiracleConstants.SUB_API_URL + "/lofter-rss/"
    return try {
        client.get<LofterResponseModel>(url + tid)
    } catch (e: SerializationException) {
        null
    }
}

fun Bot.subLofter() {
    val tIds = SubscribeData.getAllSubObject(BotDataBase.SubPlatform.LOFTER)
    tIds?.forEach {
        it?.let {
            SubLofterCache.setLastTagUpdateTime(it, System.currentTimeMillis())
        }
    }
    eventChannel.subscribeGroupMessages {
        Regex("""\s*lof订阅 .*""") matching regex@{
            val tid = it.substringAfter("lof订阅").trim()
            if (tid.isEmpty()) {
                return@regex
            } else {
                val lofRes = getLofterInfo(tid)
                if (lofRes == null) {
                    // 不存在
                    subject.sendMessage("没有查询到信息, 请输入准确的tag名称")
                    return@regex
                } else {
                    when (lofRes.status) {
                        0 -> {
                            if (SubscribeData.subscribe(
                                    group.id, tid, lofRes.title,
                                    BotDataBase.SubPlatform.LOFTER
                                )
                            ) {
                                SubLofterCache.refreshLofTagCache()
                                SubLofterCache.setLastTagUpdateTime(tid, System.currentTimeMillis())
                                subject.sendMessage(
                                    "${lofRes.title} : \n订阅成功"
                                )
                            } else {
                                subject.sendMessage("你已经订阅过了: $tid")
                            }
                        }
                        else -> {
                            subject.sendMessage("订阅失败, 请输入准确的tag名称")
                        }
                    }
                }
            }
        }

        case("lof订阅列表") {
            val list = SubscribeData.getPlatformSubList(group.id, BotDataBase.SubPlatform.LOFTER)
            if (list == null) {
                subject.sendMessage("本群还没有订阅lofter TAG")
            } else {
                subject.sendMessage(
                    list.joinToString("\n") {
                        "${it.first} - ${it.second}"
                    }
                )
            }
        }

        Regex("""\s*lof取订 .*""") matching regex@{
            val tName = it.substringAfter("lof取订").trim()
            if (tName.isEmpty()) {
                return@regex
            } else {
                val success =
                    SubscribeData.unsubscribe(group.id, tName, BotDataBase.SubPlatform.LOFTER)
                SubLofterCache.refreshLofTagCache()
                if (success) subject.sendMessage("取订成功: $tName") else subject.sendMessage("本群没有订阅该TAG")
            }
        }
    }
    suspend fun sendLofterUpdate(groupId: List<Long>, model: LofterResponseModel.LofterArticleModel) {
        launch {
            val client = KtorClient.getInstance() ?: return@launch
            groupId.forEach {
                val contact = getGroupOrFail(it)
                buildMessageChain {
                    add("${model.title}\n")
                    add("${model.content}...\n")
                    if (model.imgUrls.isNotEmpty()) {
                        model.imgUrls.forEach { img ->
                            val byteArray = client.get<ByteArray>(img)
                            add(byteArray.inputStream().uploadAsImage(contact))
                        }
                    }
                    add("${model.url}...\n")
                    add("by ${model.author} at ${model.time}")
                }.sendTo(contact)
                delay(2000)
            }
        }
    }
    Timer().schedule(Date(), period = TimeUnit.MINUTES.toMillis(5)) {
        val lofItem = SubLofterCache.nextSubLofTag()
        launch {
            val tName = lofItem.key // nid
            val groupIdList = lofItem.value
            val lastTime = SubLofterCache.getLastTagUpdateTime(tName)
            if (lastTime == 0L) {
                SubLofterCache.setLastTagUpdateTime(tName, System.currentTimeMillis())
                return@launch
            }
            val model = getLofterInfo(tName) ?: return@launch
            if (model.status != 0) return@launch
            model.result.forEach { article ->
                if (article.time_unix > lastTime) {
                    sendLofterUpdate(groupIdList, article)
                }
            }
            SubLofterCache.setLastTagUpdateTime(tName, System.currentTimeMillis())
        }
    }
}