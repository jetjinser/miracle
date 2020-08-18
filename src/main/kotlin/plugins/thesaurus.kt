package plugins

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.code.parseMiraiCode
import net.mamoe.mirai.message.data.buildMessageChain
import utils.data.ThesaurusData

fun Bot.thesaurus() {
    subscribeGroupMessages {
        Regex(""".*添加问.*答.*""") matching {
            val event = this
            Regex(""".*](?<global>(?:全局)?)添加问(?<question>.*)答(?<answer>.*)""").matchEntire(message.toString())?.apply {
                println(groupValues[1].trim())
                val globalToken = groupValues[1].trim() == "全局"
                val global = if (globalToken) 1 else 0
                val question = groupValues[2]
                val answer = groupValues[3]
                val thesaurusDate = ThesaurusData(event)
                thesaurusDate.add(question, answer, global)
                buildMessageChain {
                    add("${if (globalToken) "全局" else ""}添加成功\n问: [")
                    add(question.parseMiraiCode())
                    add("]\n答: [")
                    add(answer.parseMiraiCode())
                    add("]\n")
                    add("via thesaurus")
                }.send()
            }
        }

        always {
            val thesaurusData = ThesaurusData(this)
            if (thesaurusData.answerList.size != 0) {
                // TODO 用户分群设置 草 忘记加字段了
                if (thesaurusData.random(0.75)) {
                    thesaurusData.answerList.random().parseMiraiCode().send()
                }
            }
        }
    }
}