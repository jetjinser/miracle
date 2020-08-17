package plugins

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.id
import utils.data.MessageCacheData.append

fun Bot.recode() {
    subscribeGroupMessages {
        always {
            append(message.id, message.toString())
        }
    }
}