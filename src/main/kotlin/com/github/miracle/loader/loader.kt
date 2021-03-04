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
        // base
        builtInReply()
        random()
        reaction()
        bili()
        subBili()
        button()
        antiLightApp()
        thesaurus()
        music()
        information()
        remind()
        scheduler()
        help()
        manage()
        translate()
//        woPay() 付费系统废弃
        // 五十弦
        checkIn()
        seTu()
        tuling()
        // 腹肌
        saveMsg()
        subJjwxc()
        subSuperIndex()
        subWeiboUser()
        lottery()
    }
    bot.join()
}