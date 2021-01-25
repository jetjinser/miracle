package com.github.miracle.plugins

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.LightApp

fun Bot.builtInReply() {
    eventChannel.subscribeGroupMessages {
        Regex("""\s*来?嘤(一[个下])?\s*""") matchingReply { "嘤嘤嘤" }
        Regex("""\s*来?喵(一[个下])?\s*""") matchingReply { "喵喵喵" }
        Regex("""\s*((wei)?[,， ])? *zaima\??\s*""") matchingReply { "buzai,cnm" }
        Regex("""\s*你好|泥嚎\s*""") matching {
            listOf("泥嚎,我很阔爱,请给我钱", "").random().let {
                if (it != "") {
                    subject.sendMessage(it)
                }
            }
        }

        "草" reply {
            listOf("草", "", "", "", "").random()
        }

        contains("机屑人") {
            listOf("你才是机屑人", "", "", "").random().let {
                if (it != "") {
                    subject.sendMessage(it)
                }
            }
        }

        startsWith(".json", removePrefix = true, trim = true) {
            subject.sendMessage(LightApp(it))
        }
    }
}
