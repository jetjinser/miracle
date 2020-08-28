package com.github.miracle.plugins

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.PlainText

fun Bot.remind() {
    subscribeGroupMessages {
        contains("提醒我") {
            val msg = message[PlainText]?.content ?: return@contains


        }
    }
}