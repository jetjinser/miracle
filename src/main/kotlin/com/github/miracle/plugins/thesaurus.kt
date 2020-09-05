package com.github.miracle.plugins

import com.github.miracle.utils.data.CheckInData
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.code.parseMiraiCode
import net.mamoe.mirai.message.data.buildMessageChain
import com.github.miracle.utils.data.GroupSettingDataMap
import com.github.miracle.utils.data.ThesaurusData

fun Bot.thesaurus() {
    subscribeGroupMessages {
        Regex(""".*添加问.*答.*""") matching regex@{
            Regex(""".*](?<global>(?:全局)?)添加问(?<question>.*)答(?<answer>.*)""").matchEntire(message.toString())?.apply {
                val globalToken = groupValues[1].trim() == "全局"
                val question = groupValues[2]
                val answer = groupValues[3]
                val thesaurusDate = ThesaurusData(this@regex)
                CheckInData(this@regex).consumeCuprum(200) {
                    val success = thesaurusDate.add(question, answer, globalToken)
                    return@consumeCuprum if (success) {
                        buildMessageChain {
                            add("${if (globalToken) "全局" else ""}添加成功\nⓆ: [")
                            add(question.parseMiraiCode())
                            add("]\nⒶ: [")
                            add(answer.parseMiraiCode())
                            add("]\n")
                            add("via thesaurus")
                        }.send()
                        true
                    } else {
                        reply("添加失败, 已存在相同的问答\nⓆ: [$question]\nⒶ: [$answer]\nvia thesaurus")
                        false
                    }
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