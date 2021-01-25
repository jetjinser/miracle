package com.github.miracle.plugins

import com.github.miracle.SecretConfig
import com.github.miracle.utils.expand.sendToEveryGroup
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeFriendMessages

fun Bot.manage() {
    eventChannel.subscribeFriendMessages {
        sentBy(SecretConfig.owner) {
            startsWith("广播", removePrefix = true, trim = true) {
                val num = sendToEveryGroup(it)
                subject.sendMessage("Successfully sent messages to $num groups")
            }
        }
    }
}