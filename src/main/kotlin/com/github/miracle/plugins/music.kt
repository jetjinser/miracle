package com.github.miracle.plugins

import com.github.miracle.utils.data.CheckInData
import com.github.miracle.utils.tools.music.MusicProvider
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.sendTo

fun Bot.music() {
    eventChannel.subscribeGroupMessages {
        contains("点歌", trim = true) {
            val songName = it.substringAfter("点歌").trim()
            if (songName.isEmpty()) {
                subject.sendMessage("歌名不能为空")
                return@contains
            }

            val share = when (it.substringBefore("点歌").trim().toLowerCase()) {
                "" -> MusicProvider.netEaseMusicGen(songName)
                "网易云" -> MusicProvider.netEaseMusicGen(songName)
                "咪咕" -> return@contains
                "qq" -> return@contains // TODO add more
                else -> return@contains // TODO reply
            }

            if (share == null) {
                subject.sendMessage("失败, 请尝试更换关键词重试")
            } else {
                val pair = CheckInData(this).consumeCuprum(50)
                if (pair.first) {
                    share.sendTo(subject)
                } else subject.sendMessage("铜币不足 50 , 点歌取消, 铜币可由签到获得\n当前铜币: ${pair.second}")
            }
        }
    }
}