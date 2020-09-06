package com.github.miracle.plugins

import com.github.miracle.utils.expand.sendToEveryGroup
import com.github.miracle.utils.expand.subscribeOwnerMessage
import net.mamoe.mirai.Bot

fun Bot.manage() {
    subscribeOwnerMessage {
        startsWith("广播", removePrefix = true, trim = true) {
            val num = sendToEveryGroup(it)
            reply("Successfully sent messages to $num groups")
        }
    }
}