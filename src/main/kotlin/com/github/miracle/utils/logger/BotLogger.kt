package com.github.miracle.utils.logger

import net.mamoe.mirai.utils.SimpleLogger

object BotLogger {
    fun logger(identity: String) = SimpleLogger.invoke(identity) { _, _ ->
    }
}