package com.github.miracle.plugins

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import com.github.miracle.utils.data.GroupSettingDataMap
import java.lang.NumberFormatException

fun Bot.setting() {
    eventChannel.subscribeGroupMessages {
        // 临时 懒得写
    }
}