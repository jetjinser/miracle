package com.github.miracle.utils.expand

import com.github.miracle.SecretConfig.owner
import kotlinx.coroutines.delay
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.BotIsBeingMutedException
import net.mamoe.mirai.getFriendOrNull
import java.util.concurrent.TimeUnit

suspend fun Bot.sendToEveryGroup(message: String): Int {
    groups.forEach {
        delay(TimeUnit.SECONDS.toMillis(7))
        try {
            it.sendMessage(message)
        } catch (e: BotIsBeingMutedException) {
            getFriendOrNull(owner)?.sendMessage("BotIsBeingMutedException: ${it.name} (${it.id})")
            logger.info("BotIsBeingMutedException: ${it.name} (${it.id})")
        } catch (e: Exception) {
            e.printStackTrace()
            getFriendOrNull(owner)?.sendMessage("在发送到群 [${it.name} (${it.id})] 时发生了未知错误:\n${e.stackTrace}")
        }
    }
    return groups.size
}