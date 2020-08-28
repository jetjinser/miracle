package com.github.miracle.plugins

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.id
import com.github.miracle.utils.data.MessageCacheData.append

fun Bot.recode() {
    subscribeGroupMessages {
        always {
            append(message.id, message.toString())
        }
    }
}