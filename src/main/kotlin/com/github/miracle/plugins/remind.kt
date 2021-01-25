package com.github.miracle.plugins

import com.github.miracle.utils.tools.RemindDate
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.content
import java.util.*
import kotlin.concurrent.schedule

fun Bot.remind() {
    val timer = Timer()

    eventChannel.subscribeGroupMessages {
        contains("后提醒我") {
            val msg = message[MessageSource.Key]?.content ?: return@contains

            val result = Regex("""(?<date>.*后)\s*提醒我(?<something>.*)""").matchEntire(msg)
            if (result == null) {
                subject.sendMessage("格式错误")
                return@contains
            }

            val preDate = result.groups[1]?.value?.trim()
            val date = preDate?.dropLast(1) ?: return@contains
            val something = result.groups[2]?.value?.trim() ?: return@contains

            val delayDate = RemindDate.getDate(date)
            if (delayDate == null) {
                subject.sendMessage("单位不支持或时间为0, 请注意, 过大的数字会导致溢出, 使时间错误")
            }

            val delay = delayDate?.time?.minus(System.currentTimeMillis()) ?: return@contains

            subject.sendMessage("好的, 我会在 $preDate 提醒你\n[$something]")
            timer.schedule(delay) {
                launch {
                    subject.sendMessage(At(sender) + something)
                }
            }
        }
    }
}