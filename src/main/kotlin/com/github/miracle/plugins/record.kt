package com.github.miracle.plugins

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import com.github.miracle.utils.data.MessageCacheData.append

fun Bot.recode() {
    eventChannel.subscribeGroupMessages {
        always {
            append(message.hashCode(), message.toString())
        }
    }
}