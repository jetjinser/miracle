package plugins

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.buildXmlMessage

fun Bot.music() {
    subscribeGroupMessages {
        startsWith("点歌", removePrefix = true, trim = true) {
            buildXmlMessage(1) {

            }
        }
    }
}