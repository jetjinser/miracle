package loader

import net.mamoe.mirai.Bot
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.join
import plugins.*


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
    bot.seTu()
    bot.reaction()

    bot.join()
}