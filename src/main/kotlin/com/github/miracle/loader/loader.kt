package com.github.miracle.loader

import com.github.miracle.SecretConfig
import com.github.miracle.plugins.*
import com.github.miracle.plugins.base.*
import com.github.miracle.plugins.sub.*
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
//        builtInReply()
//        random()
//        checkIn()
//        seTu()
//        reaction()
//        bili()
//        subBili()
//        button()
//        antiLightApp()
//        recode()
//        thesaurus()
//        setting()
        music()
//        information()
//        tuling()
//        remind()
//        scheduler()
//        woPay()
//        help()
//        manage()
//        translate()
//        saveMsg()
//        subJjwxc()
//        subSuperIndex()
//        flomo()
//        shotSender()
    }
    bot.join()
}