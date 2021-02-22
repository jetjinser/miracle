package com.github.miracle.plugins.sub

import com.github.miracle.MiracleConstants
import com.github.miracle.utils.data.SubBiliCache
import com.github.miracle.utils.data.SubNovelCache
import com.github.miracle.utils.data.SubscribeData
import com.github.miracle.utils.database.BotDataBase.Platform.JJWXC
import com.github.miracle.utils.network.KtorClient
import com.github.miracle.utils.network.model.NovelModel
import io.ktor.client.request.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.message.data.sendTo
import java.util.*
import kotlin.concurrent.schedule


/**
 * 获取novel信息
 */
suspend fun getNovelInfo(nid: String): NovelModel? {
    val client = KtorClient.getInstance() ?: return null
    val url = MiracleConstants.SUB_API_URL + "/get_novel_info/"
    return try {
        client.get<NovelModel>(url + nid)
    } catch (e: SerializationException) {
        null
    }
}

fun Bot.subJjwxc() {
    eventChannel.subscribeGroupMessages {
        Regex("""\s*j订阅 +\w+\s*""") matching regex@{
            val msg = it.substringAfter("j订阅").trim()
            if (msg.isEmpty()) return@regex
            val nid = msg.toLongOrNull()
            if (nid == null) {
                subject.sendMessage("小说id是数字喔")
                return@regex
            } else {
                val novel = getNovelInfo(nid.toString())
                if (novel == null) {
                    // 不存在
                    subject.sendMessage("没有查询到信息, 请确认小说id正确")
                    return@regex
                } else {
                    // 0: 连载中 1: 已完结 2: 不存在
                    when (novel.status) {
                        0 -> {
                            if (SubscribeData.subscribe(group.id, nid.toString(), novel.title, JJWXC)) {
                                // 已经插入到数据库里了
                                // 标记最新一章
                                SubBiliCache.refreshCache()
                                SubNovelCache.markLastChapter(nid.toString(), novel.chapterId)
                                subject.sendMessage(
                                    "${novel.title} : ${nid}\n订阅成功\n最新章节：第${novel.chapterId}章\n" +
                                            "${novel.chapterTitle}:${novel.chapterDesc}"
                                )
                            } else {
                                subject.sendMessage("你已经订阅过了: $nid")
                            }
                        }
                        1 -> {
                            subject.sendMessage("该小说已完结")
                        }
                        2 -> {
                            subject.sendMessage("订阅失败, 请确认小说id正确")
                        }
                    }
                }
            }
        }

        case("j订阅列表") {
            val list = SubscribeData.getPlatformSubList(group.id, JJWXC)
            if (list == null) {
                subject.sendMessage("本群还没有订阅晋江小说")
            } else {
                subject.sendMessage(
                    list.joinToString("\n") {
                        "${it.first.toString().padEnd(8, ' ')} - ${it.second}"
                    }
                )
            }
        }

        Regex("""\s*j取订 +\w+\s*""") matching regex@{
            val msg = it.substringAfter("j取订").trim()
            if (msg.isEmpty()) return@regex
            val nid = msg.toLongOrNull()
            if (nid == null) {
                subject.sendMessage("小说id是数字喔")
                return@regex
            } else {
                val success = SubscribeData.unsubscribe(group.id, nid.toString(), JJWXC)
                SubNovelCache.refreshCache()
                if (success) subject.sendMessage("取订成功: $nid") else subject.sendMessage("你没有订阅过这本小说")
            }
        }
    }

    suspend fun Bot.sendNovelUpdate(groupId: List<Long>, model: NovelModel) {
        groupId.forEach {
            coroutineScope {
                launch {
                    val contact = getGroupOrFail(it)
                    buildMessageChain {
                        add("${model.title} 更新了第${model.chapterId}章\n")
                        add("${model.chapterTitle}:${model.chapterDesc}\n")
                    }.sendTo(contact)
                    delay(2000)
                }
            }
        }
    }

    Timer().schedule(Date(), 20000) {
        val pair = SubNovelCache.nextSub()
        launch {
            val novelMap = pair.first // nid: list(gid)
            val chapterCache = pair.second // {novelId:chapterId}

            val nid = novelMap.key // nid
            val groupIdList = novelMap.value

            val model = getNovelInfo(nid) ?: return@launch

            if (chapterCache[nid] != 0 && chapterCache[nid] != null && model.chapterId > chapterCache[nid] ?: 0) {
                sendNovelUpdate(groupIdList, model)
            }
            SubNovelCache.markLastChapter(nid, model.chapterId)
        }
    }
}