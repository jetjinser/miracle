package plugins

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.code.parseMiraiCode
import net.mamoe.mirai.message.data.asMessageChain
import net.mamoe.mirai.message.data.buildMessageChain
import utils.data.GroupSettingDataMap
import utils.data.ThesaurusData

fun Bot.thesaurus() {
    subscribeGroupMessages {
        Regex(""".*添加问.*答.*""") matching {
            val event = this
            Regex(""".*](?<global>(?:全局)?)添加问(?<question>.*)答(?<answer>.*)""").matchEntire(message.toString())?.apply {
                val globalToken = groupValues[1].trim() == "全局"
                val question = groupValues[2]
                val answer = groupValues[3]
                val thesaurusDate = ThesaurusData(event)
                val success = thesaurusDate.add(question, answer, globalToken)
                if (success) {
                    buildMessageChain {
                        add("${if (globalToken) "全局" else ""}添加成功\nⓆ: [")
                        add(question.parseMiraiCode())
                        add("]\nⒶ: [")
                        add(answer.parseMiraiCode())
                        add("]\n")
                        add("via thesaurus")
                    }.send()
                } else {
                    reply("添加失败, 已存在相同的问答\nⓆ: [$question]\nⒶ: [$answer]\nvia thesaurus")
                }
            }
        }

        always {
            val thesaurusData = ThesaurusData(this)
            val answerList = thesaurusData.answerList
            if (answerList.isNotEmpty()) {
                if (thesaurusData.random(GroupSettingDataMap.getInstance(this.group).qaProbability)) {
                    answerList.random().parseMiraiCode().send()
                }
            }
        }
    }
}