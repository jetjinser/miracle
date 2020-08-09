package plugins

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import utils.process.CheckInPicture


fun Bot.checkIn() {
    subscribeGroupMessages {
        case("签到") {
            val url = "http://q1.qlogo.cn/g?b=qq&nk=${this.sender.id}&s=5"
            CheckInPicture.produce(url).send()
        }
    }
}
