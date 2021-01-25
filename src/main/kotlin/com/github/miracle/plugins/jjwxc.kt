package com.github.miracle.plugins

import com.github.miracle.utils.data.NovelSubData
import com.github.miracle.utils.network.model.NovelModel
import com.github.miracle.utils.tools.Jjwxc
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.message.data.sendTo
import java.util.*
import kotlin.concurrent.schedule

fun Bot.jjwxc() {
    eventChannel.subscribeGroupMessages { // bilibili 订阅 region
        Regex("""\s*j订阅 +\w+\s*""") matching regex@{
            val msg = it.substringAfter("j订阅").trim()
            if (msg.isEmpty()) return@regex
            val nid = msg.toLongOrNull()
            if (nid == null) {
                subject.sendMessage("小说id是数字喔")
                return@regex
            } else {
                val novel = Jjwxc.getNovelInfo(nid)
                if (novel == null) {
                    // 不存在
                    subject.sendMessage("订阅失败, 请确认小说id正确")
                    return@regex
                } else {
                    // 0: 连载中 1: 已完结 2: 不存在
                    when (novel.status) {
                        0 -> {
                            if (NovelSubData.subscribe(group.id, nid, novel.title)) {
                                // 已经插入到数据库里了
                                // 标记最新一章
                                NovelSubData.markLastChapter(nid, novel.chapterId)
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
            val list = NovelSubData.getSubList(group.id)
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

        Regex("""\s*j取订 +\w+\s*""") matching regex@{
            val msg = it.substringAfter("j取订").trim()
            if (msg.isEmpty()) return@regex
            val nid = msg.toLongOrNull()
            if (nid == null) {
                subject.sendMessage("小说id是数字喔")
                return@regex
            } else {
                val success = NovelSubData.unsubscribe(group.id, nid)
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
        val pair = NovelSubData.nextSub()
        launch {
            val novelMap = pair.first // nid: list(gid)
            val chapterCache = pair.second // {novelId:chapterId}

            val nid = novelMap.key // nid
            val groupIdList = novelMap.value

            val model = Jjwxc.getNovelInfo(nid) ?: return@launch

            if (chapterCache[nid] != 0 && model.chapterId > chapterCache[nid] ?: 0) {
                sendNovelUpdate(groupIdList, model)
            }
            NovelSubData.markLastChapter(nid, model.chapterId)
        }
    }
}