package plugins

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages

fun Bot.builtInReply() {
    subscribeGroupMessages {
        Regex("""\s*来?嘤(一[个下])?\s*""") matchingReply { intercept(); "嘤嘤嘤" }
        Regex("""\s*来?喵(一[个下])?\s*""") matchingReply { intercept(); "喵喵喵" }
        Regex("""\s*((wei)?[,， ])? *zaima\??\s*""") matchingReply { intercept(); "buzai,cnm" }
        Regex("""\s*你好|泥嚎\s*""") matching {
            listOf("泥嚎,我很阔爱,请给我钱", "").random().let {
                if (it != "") {
                    reply(it)
                    intercept()
                }
            }
        }

        "草" reply {
            listOf("草", "草", "草", "草", "").random()
            intercept()
        }

        contains("机屑人") {
            listOf("你才是机屑人", "").random().let {
                if (it != "") {
                    reply(it)
                }
            }
            intercept()
        }
    }
}
