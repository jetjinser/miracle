package com.github.miracle.plugins

import com.github.miracle.utils.data.CheckInData
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.buildMessageChain
import com.github.miracle.utils.data.GroupSettingDataMap
import com.github.miracle.utils.data.ThesaurusData
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.sendTo

fun Bot.thesaurus() {
    eventChannel.subscribeGroupMessages {
        Regex(""".*添加问.*答.*""") matching regex@{
            Regex(""".*](?<global>(?:全局)?)添加问(?<question>.*)答(?<answer>.*)""").matchEntire(message.toString())?.apply {
                val globalToken = groupValues[1].trim() == "全局"
                val question = groupValues[2]
                val answer = groupValues[3]
                val thesaurusDate = ThesaurusData(this@regex)
                CheckInData(this@regex).consumeCuprum(200) {
                    if (it.first) {
                        val success = thesaurusDate.add(question, answer, globalToken)
                        return@consumeCuprum if (success) {
                            buildMessageChain {
                                add("${if (globalToken) "全局" else ""}添加成功\nⓆ: [")
                                add(question.deserializeMiraiCode())
                                add("]\nⒶ: [")
                                add(answer.deserializeMiraiCode())  // TODO
                                add("]\n")
                                add("via thesaurus")
                            }.sendTo(subject)
                            true
                        } else {
                            subject.sendMessage("添加失败, 已存在相同的问答\nⓆ: [$question]\nⒶ: [$answer]\nvia thesaurus")
                            false
                        }
                    } else {
                        subject.sendMessage("铜币不足 200 , 添加取消, 铜币可由签到获得\n当前铜币: ${it.second}")
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
                    answerList.random().deserializeMiraiCode(subject).sendTo(subject)
                }
            }
        }
    }
}