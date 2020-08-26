package loader

import SecretConfig
import net.mamoe.mirai.Bot
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.join
import plugins.*


suspend fun main() {
    val bot = Bot(
        SecretConfig.qq,
        SecretConfig.password
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
        thesaurus()
        setting()
        music()
        information()
        tuling()
    }

    bot.join()
}