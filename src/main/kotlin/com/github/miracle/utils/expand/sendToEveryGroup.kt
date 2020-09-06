package com.github.miracle.utils.expand

import kotlinx.coroutines.delay
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.BotIsBeingMutedException
import kotlin.random.Random

suspend fun Bot.sendToEveryGroup(message: String): Int {
    groups.forEach {
        delay(Random.nextLong(0, 20))
        try {
            it.sendMessage(message)
        } catch (e: BotIsBeingMutedException) {
            logger.info("BotIsBeingMutedException: ${it.name} (${it.id})")
        }
    }
    return groups.size
}