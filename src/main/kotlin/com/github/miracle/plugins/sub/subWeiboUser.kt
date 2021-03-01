package com.github.miracle.plugins.sub

import com.github.miracle.MiracleConstants
import com.github.miracle.utils.data.SubWeiboCache
import com.github.miracle.utils.data.SubscribeData
import com.github.miracle.utils.database.BotDataBase
import com.github.miracle.utils.database.BotDataBase.SubPlatform.SUPER
import com.github.miracle.utils.database.BotDataBase.SubPlatform.WEIBO
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


/**
 * 获取novel信息
 */
suspend fun getUserWeibo(uid: String): WeiboResponseModel? {
    val client = KtorClient.getInstance() ?: return null
    val url = MiracleConstants.SUB_API_URL + "/weibo-rss/"
    return try {
        client.get<WeiboResponseModel>(url + uid)
    } catch (e: SerializationException) {
        null
    }
}

fun Bot.subWeiboUser() {
    val uIds = SubscribeData.getAllSubObject(WEIBO)
    uIds?.forEach {
        it?.let {
            SubWeiboCache.setLastUserUpdateTime(it, System.currentTimeMillis())
        }
    }
    eventChannel.subscribeGroupMessages {
        Regex("""\s*微博订阅 +\w+\s*""") matching regex@{
            val uid = it.substringAfter("微博订阅").trim()
            if (uid.isEmpty()) {
                return@regex
            } else {
                val weiboRes = getUserWeibo(uid)
                if (weiboRes == null) {
                    // 不存在
                    subject.sendMessage("没有查询到信息, 微博id为网页端用户主页即https://weibo.com/u/[id]的id部分")
                    return@regex
                } else {
                    when (weiboRes.status) {
                        0 -> {
                            if (SubscribeData.subscribe(
                                    group.id, uid, weiboRes.weiboTitle, WEIBO
                                )
                            ) {
                                SubWeiboCache.refreshUserCache()
                                SubWeiboCache.setLastUserUpdateTime(uid, System.currentTimeMillis())
                                subject.sendMessage(
                                    "${weiboRes.weiboTitle} : \n订阅成功"
                                )
                            } else {
                                subject.sendMessage("你已经订阅过了: $uid")
                            }
                        }
                        else -> {
                            subject.sendMessage("订阅失败, 请确认微博用户id正确，仅支持数字id，博主主页打开控制台执行 \$CONFIG.oid 获取")
                        }
                    }
                }
            }
        }

        case("微博订阅列表") {
            val list = SubscribeData.getPlatformSubList(group.id, WEIBO)
            if (list == null) {
                subject.sendMessage("本群还没有订阅微博用户")
            } else {
                subject.sendMessage(
                    list.joinToString("\n") {
                        "${it.first} - ${it.second}"
                    }
                )
            }
        }

        Regex("""\s*微博取订 +\w+\s*""") matching regex@{
            val sid = it.substringAfter("微博取订").trim()
            if (sid.isEmpty()) {
                return@regex
            } else {
                val success = SubscribeData.unsubscribe(group.id, sid, WEIBO)
                SubWeiboCache.refreshUserCache()
                if (success) subject.sendMessage("取订成功: $sid") else subject.sendMessage("本群没有订阅该微博用户")
            }
        }
    }


    Timer().schedule(Date(), period = TimeUnit.SECONDS.toMillis(30)) {
        val userItem = SubWeiboCache.nextSubUser()
        launch {
            val uid = userItem.key // nid
            val groupIdList = userItem.value
            val model = getUserWeibo(uid) ?: return@launch
            if (SubWeiboCache.getLastUserUpdateTime(uid) != 0L) {
                sendWeiboUpdate(WEIBO.value, uid, groupIdList, model)
                SubWeiboCache.setLastUserUpdateTime(uid, System.currentTimeMillis())
            } else {
                SubWeiboCache.setLastUserUpdateTime(uid, System.currentTimeMillis())
            }
        }
    }
}

suspend fun Bot.sendWeiboUpdate(
    type: Int,
    objId: String,
    groupId: List<Long>,
    model: WeiboResponseModel
) {
    coroutineScope {
        launch {
            val client = KtorClient.getInstance() ?: return@launch
            if (model.status != 0) {
                return@launch
            }
            val lastTime = if (type == SUPER.value) SubWeiboCache.getLastSuperUpdateTime(objId)
            else SubWeiboCache.getLastUserUpdateTime(objId)
            model.result.forEach { model ->
                if (model.time_unix > lastTime) {
                    groupId.forEach {
                        val contact = getGroupOrFail(it)
                        buildMessageChain {
                            add("${model.content}\n")
                            if (model.ttarticleLink.isNotEmpty()) {
                                add("头条文章：${model.ttarticleLink}\n")
                            }
                            if (model.imgUrls.isNotEmpty()) {
                                model.imgUrls.forEach { img ->
                                    val byteArray = client.get<ByteArray>(img)
                                    add(byteArray.inputStream().uploadAsImage(contact))
                                }
                            }
                            if (model.extra.isNotEmpty()) {
                                model.extra.forEach { ext ->
                                    add("$ext\n")
                                }
                            }
                            add("by ${model.author} at ${model.time}\n")
                            add(model.link) // 原微博链接
                        }.sendTo(contact)
                    }
                }
            }
            delay(2000)
        }
    }
}
