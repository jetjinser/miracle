package com.github.miracle.plugins

import com.github.miracle.SecretConfig
import com.github.miracle.utils.tools.GenerateTextPic
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.message.data.sendTo
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage

fun Bot.saveMsg() {
    var recordId = 0L
    var recordName = ""
    var startRecord = false
    var tempMergeString = ""
    eventChannel.subscribeGroupMessages {
        always {
            if (startRecord && sender.id == recordId && sender.nick == recordName) {
                val msg = message.content
                if (msg != "end") {
                    tempMergeString += msg + "\n"
                    subject.sendMessage("√")
                }
            }
            return@always
        }
        case("start", trim = true) {
            if (startRecord) {
                subject.sendMessage("当前记录未结束")
            } else {
                recordId = sender.id // 记录id
                recordName = sender.nick
                startRecord = true
                tempMergeString = ""
                subject.sendMessage("开始记录")
            }
            return@case
        }
        case("end", trim = true) {
            if (sender.id == recordId && sender.nick == recordName) {
                if (tempMergeString.length < 4000) {
                    subject.sendMessage(tempMergeString)
                } else {
                    // 文字太长，进行截取
                    val partCount = tempMergeString.length / 4000 + 1
                    for (i in 0 until partCount) {
                        val endIndex =
                            if ((i + 1) * 4000 < tempMergeString.length - 1) (i + 1) * 4000 else tempMergeString.length - 1
                        subject.sendMessage(tempMergeString.substring(i * 4000, endIndex))
                    }
                }
                buildMessageChain {
                    add(GenerateTextPic(tempMergeString, recordName).createTextPic().toExternalResource().uploadAsImage(subject))
                }.sendTo(subject)
                recordId = 0L
                recordName = ""
                startRecord = false
                tempMergeString = ""
                getFriendOrFail(SecretConfig.owner).sendMessage("有车了")
            }
            return@case
        }
    }
}