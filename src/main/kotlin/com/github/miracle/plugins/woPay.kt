package com.github.miracle.plugins

import com.github.miracle.SecretConfig
import com.github.miracle.utils.data.WoPayData
import com.github.miracle.utils.tools.timer.timeStart
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.events.BotJoinGroupEvent
import net.mamoe.mirai.event.subscribeFriendMessages
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.nextMessage
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule

fun Bot.woPay() {
    eventChannel.subscribeAlways<BotJoinGroupEvent> {
        WoPayData.register(group.id, LocalDate.now().toString())
    }

    Timer().schedule(timeStart(4).time) {
        for (group in this@woPay.groups) {
            val date = LocalDate.parse(WoPayData.inquire(group.id), DateTimeFormatter.ISO_DATE)
            if (date.isBefore(LocalDate.now())) {
                launch {
                    delay(TimeUnit.SECONDS.toMillis(25))
                    group.sendMessage("bot已到期, 如有意愿续费请加群了解:\n117340135")
                    group.quit()
                }
            }
        }
    }

    eventChannel.subscribeGroupMessages(priority = EventPriority.LOWEST) {
        contains("token", ignoreCase = true, trim = true) {
            if (message.firstIsInstanceOrNull<At>()?.target == bot.id) {
                val content = message.first { it is PlainText }.content
                var msg = content.substringAfter("token").trim()
                if (msg.isEmpty()) {
                    subject.sendMessage("请发送token")
                    msg = nextMessage { message[MessageSource.Key] != null }.content.trim() // TODO
                }
                val success = WoPayData.renewByToken(msg, group.id)
                if (success == null) {
                    subject.sendMessage("失败, token不存在")
                    intercept()
                    return@contains
                }
                subject.sendMessage("成功! 续期到 $success")
                intercept()
            }
        }

        Regex("""\s*查询到期(日期)?\s*""") matching {
            val date = WoPayData.inquire(group.id)
            if (date == null) subject.sendMessage("::bug") else subject.sendMessage(date)
        }
    }

    eventChannel.subscribeFriendMessages {
        startsWith("get_token", removePrefix = true, trim = true) {
            if (sender.id == SecretConfig.owner) {
                var days = it.toIntOrNull()
                if (days == null) {
                    subject.sendMessage("几日?")
                    days = nextMessage { message.content.toIntOrNull() != null }.content.toInt()
                }
                subject.sendMessage(
                    WoPayData.genToken(days) ?: "Nil"
                )
            }
        }

        startsWith("pay", removePrefix = true, trim = true) { s ->
            if (sender.id == SecretConfig.owner) {
                val list = s.split(" ").filter { it.isNotEmpty() }
                var days = list.getOrNull(0)?.toIntOrNull()
                var groupId = list.getOrNull(1)?.toIntOrNull()

                if (days == null) {
                    subject.sendMessage("几日")
                    days = nextMessage { message.content.toIntOrNull() != null }.content.toInt()
                }
                if (groupId == null) {
                    subject.sendMessage("什么群?")
                    groupId =
                        nextMessage { message.content.toIntOrNull() != null }.content.toInt()
                }

                subject.sendMessage(
                    WoPayData.renewDirect(days!!, groupId.toLong())?.toString() ?: "Failure"
                )
            }
        }
    }
}