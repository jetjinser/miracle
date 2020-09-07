package com.github.miracle.plugins

import com.github.miracle.utils.data.CheckInData
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import com.github.miracle.utils.tools.music.MusicProvider

fun Bot.music() {
    subscribeGroupMessages {
        contains("点歌", trim = true) {
            val songName = it.substringAfter("点歌").trim()
            if (songName.isEmpty()) {
                reply("歌名不能为空")
                return@contains
            }

            val lightApp = when (it.substringBefore("点歌").trim()) {
                "" -> MusicProvider.netEaseMusicGen(songName)
                "网易云" -> MusicProvider.netEaseMusicGen(songName)
                "酷狗" -> MusicProvider.kuGouMusicGen(songName)
                else -> return@contains
            }

            if (lightApp == null) {
                reply("失败, 请尝试更换关键词重试")
            } else {
                val pair = CheckInData(this).consumeCuprum(50)
                if (pair.first) {
                    lightApp.send()
                } else reply("铜币不足 50 , 点歌取消, 铜币可由签到获得\n当前铜币: ${pair.second}")
            }
        }
    }
}