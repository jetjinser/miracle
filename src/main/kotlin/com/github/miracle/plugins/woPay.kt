package com.github.miracle.plugins

import com.github.miracle.utils.data.WoPayData
import com.github.miracle.utils.subscriber.subscribeOwnerMessage
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
import kotlin.concurrent.schedule
import kotlin.random.Random

fun Bot.woPay() {
    subscribeAlways<BotJoinGroupEvent> {
        WoPayData.register(group.id, LocalDate.now().toString())
    }

    Timer().schedule(calendarGen(4).time) {
        for (group in this@woPay.groups) {
            val date = LocalDate.parse(WoPayData.inquire(group.id), DateTimeFormatter.ISO_DATE)
            if (date.isBefore(LocalDate.now())) {
                launch {
                    delay(Random.nextLong(1000, 50000))
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
            val success = WoPayData.renew(msg, group.id)
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
    }
}