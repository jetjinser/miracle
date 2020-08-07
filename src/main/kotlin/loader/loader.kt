package loader

import net.mamoe.mirai.Bot
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.join


suspend fun main() {
    val bot = Bot(
        Config.qq,
        Config.password
    ) {
        fileBasedDeviceInfo("device.json")
    }.alsoLogin()

    bot.join()
}