package com.github.miracle.plugins

import com.github.miracle.utils.data.CheckInData
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import com.github.miracle.utils.tools.music.MusicProvider

fun Bot.music() {
    subscribeGroupMessages {
        startsWith("点歌 ", removePrefix = true, trim = true) {
            val netEaseMusicLightApp = MusicProvider.netEaseMusicGen(it)
            if (netEaseMusicLightApp == null) {
                reply("失败")
            } else {
                val pair = CheckInData(this).consumeCuprum(50)
                if (pair.first) {
                    netEaseMusicLightApp.send()
                } else reply("铜币不足 50 , 点歌取消, 铜币可由签到获得\n当前铜币: ${pair.second}")
            }
        }
    }
}