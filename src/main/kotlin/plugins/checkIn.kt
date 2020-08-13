package plugins

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import utils.process.checkIn.CheckInModel
import utils.process.checkIn.CheckInPicture


fun Bot.checkIn() {
    subscribeGroupMessages {
        case("签到") {
            CheckInModel(this).also {
                if (it.checkIn()) {
                    CheckInPicture(sender.avatarUrl, it).generate().send()
                } else {
                    reply("您今天已经签到过了")
                }
            }
        }
    }
}
