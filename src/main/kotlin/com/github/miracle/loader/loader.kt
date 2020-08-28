package com.github.miracle.loader

import com.github.miracle.SecretConfig
import com.github.miracle.plugins.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.join


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
        remind()
        scheduler()
    }

    bot.join()
}