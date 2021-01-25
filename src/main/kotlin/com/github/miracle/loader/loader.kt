package com.github.miracle.loader

import com.github.miracle.SecretConfig
import com.github.miracle.plugins.*
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.alsoLogin


suspend fun main() {
    val bot = BotFactory.newBot(
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
        remind()
//        scheduler()
        woPay()
        help()
        manage()
        translate()
        saveMsg()
        jjwxc()
        flomo()
    }



    bot.join()
}