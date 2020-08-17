package loader

import net.mamoe.mirai.Bot
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.event.subscribe
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.join
import plugins.*
import utils.data.MessageCacheData
import utils.data.MessageCacheData.append


suspend fun main() {
    val bot = Bot(
        Config.qq,
        Config.password
    ) {
        fileBasedDeviceInfo("device.json")
    }.alsoLogin()

    bot.apply {
        builtInReply()
        random()
        checkIn()
        seTu()
        reaction()
        bili()
        button()
        antiLightApp()
        recode()
    }

    bot.join()
}