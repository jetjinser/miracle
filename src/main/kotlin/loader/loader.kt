package loader

import net.mamoe.mirai.Bot
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.join
import plugins.builtInReply
import plugins.checkIn
import plugins.random


suspend fun main() {
    val bot = Bot(
        Config.qq,
        Config.password
    ) {
        fileBasedDeviceInfo("device.json")
    }.alsoLogin()

    bot.builtInReply()
    bot.random()
    bot.checkIn()

    bot.join()
}