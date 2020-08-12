package plugins

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import utils.process.checkIn.CheckInModel
import utils.process.checkIn.CheckInPicture


fun Bot.checkIn() {
    subscribeGroupMessages {
        case("签到") {
            val checkInModel = CheckInModel(this).also {
                it.checkIn()
            }
            CheckInPicture(sender.avatarUrl, checkInModel).generate().send()
        }
    }
}
