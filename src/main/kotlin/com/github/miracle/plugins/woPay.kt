package com.github.miracle.plugins

import com.github.miracle.utils.data.WoPayData
import com.github.miracle.utils.expand.subscribeOwnerMessage
import com.github.miracle.utils.tools.timer.calendarGen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.events.BotJoinGroupEvent
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.message.nextMessage
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule

fun Bot.woPay() {
    subscribeAlways<BotJoinGroupEvent> {
        WoPayData.register(group.id, LocalDate.now().toString())
    }

    Timer().schedule(calendarGen(4).time) {
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

    subscribeGroupMessages(priority = EventPriority.LOWEST) {
        contains("token", ignoreCase = true, trim = true) {
            if (message[At]?.isBot() != true) return@contains
            var msg = message[PlainText]?.content?.substringAfter("token")?.trim()
            if (msg.isNullOrEmpty()) {
                reply("请发送token")
                msg = nextMessage { message[PlainText] != null }.content.trim()
            }
            val success = WoPayData.renewByToken(msg, group.id)
            if (success == null) {
                reply("失败, token不存在")
                intercept()
                return@contains
            }
            reply("成功! 续期到 $success")
            intercept()
        }

        Regex("""\s*查询到期(日期)?\s*""") matching {
            val date = WoPayData.inquire(group.id)
            if (date == null) reply("::bug") else reply(date)
        }
    }

    subscribeOwnerMessage {
        startsWith("get_token", removePrefix = true, trim = true) {
            var days = it.toIntOrNull()
            if (days == null) {
                reply("几日?")
                days = nextMessage { message.content.toIntOrNull() != null }.content.toInt()
            }
            reply(
                WoPayData.genToken(days) ?: "Nil"
            )
        }

        startsWith("pay", removePrefix = true, trim = true) { s ->
            val list = s.split(" ").filter { it.isNotEmpty() }
            var days = list.getOrNull(0)?.toIntOrNull()
            var groupId = list.getOrNull(1)?.toIntOrNull()

            if (days == null) {
                reply("几日")
                days = nextMessage { message.content.toIntOrNull() != null }.content.toInt()
            }
            if (groupId == null) {
                reply("什么群?")
                groupId = nextMessage { message.content.toIntOrNull() != null }.content.toInt()
            }

            reply(
                WoPayData.renewDirect(days!!, groupId.toLong())?.toString() ?: "Failure"
            )
        }
    }
}