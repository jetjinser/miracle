package plugins

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages

fun Bot.builtInReply() {
    subscribeGroupMessages {
        Regex("""\s*来?嘤(一[个下])?\s*""") matchingReply { "嘤嘤嘤" }
        Regex("""\s*来?喵(一[个下])?\s*""") matchingReply { "喵喵喵" }
        Regex("""\s*((wei)?[,， ])? *zaima\??\s*""") matchingReply { "buzai,cnm" }
        Regex("""\s*你好|泥嚎\s*""") matchingReply {
            listOf("泥嚎,我很阔爱,请给我钱", "").random().let {
                if (it != "") {
                    reply(it)
                }
            }
        }

        "草" reply listOf("草", "草", "草", "草", "").random()

        "机屑人" containsReply {
            listOf("你才是机屑人", "").random().let {
                if (it != "") {
                    reply(it)
                }
            }
        }
    }
}
