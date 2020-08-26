package plugins

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import utils.process.music.MusicProvider

fun Bot.music() {
    subscribeGroupMessages {
        startsWith("点歌", removePrefix = true, trim = true) {
            val netEaseMusicLightApp = MusicProvider.netEaseMusicGen(it)
            if (netEaseMusicLightApp == null) {
                reply("失败")
            } else netEaseMusicLightApp.send()
            intercept()
        }
    }
}