package plugins

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages

fun Bot.builtInReply() {
    subscribeGroupMessages {
        Regex("嘤一下|嘤一个|来嘤") matchingReply { "嘤嘤嘤" }
        Regex("喵一下|喵一个|来喵") matchingReply { "喵喵喵" }
        Regex("zaima|wei,zaima|wei，zaima") matchingReply { "buzai,cnm" }
        Regex("你好|泥嚎") matchingReply { listOf("泥嚎,我很阔爱,请给我钱", "").random() }

        "草" reply listOf("草", "草", "草", "草", "").random()

        contains("机屑人") {
            listOf("你才是机屑人", "").random()
        }
    }
}
