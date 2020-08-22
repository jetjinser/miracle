package plugins

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import utils.process.music.MusicProvider

fun Bot.music() {
    subscribeGroupMessages {
        startsWith("点歌", removePrefix = true, trim = true) {
            MusicProvider.netEaseMusicGen(it).send()
        }
    }
}