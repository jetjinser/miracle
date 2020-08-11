package plugins

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import utils.process.checkIn.CheckInPicture


fun Bot.checkIn() {
    subscribeGroupMessages {
        case("签到") {
            CheckInPicture.generate(sender.avatarUrl).send()
        }
    }
}
