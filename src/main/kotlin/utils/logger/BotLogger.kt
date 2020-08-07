package utils.logger

import net.mamoe.mirai.utils.DefaultLogger

object BotLogger {
    fun logger(identity: String) = DefaultLogger.invoke(identity)
}