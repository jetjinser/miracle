package com.github.miracle.plugins

import com.github.miracle.SecretConfig
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.getFriendOrNull
import net.mamoe.mirai.message.data.PlainText

fun Bot.saveMsg(){
    var recordId = 0L
    var recordName = ""
    var startRecord = false
    var tempMergeString = ""
    subscribeGroupMessages {
        always {
            if (startRecord && sender.id == recordId && sender.nick == recordName) {
                val msg = message[PlainText]?.content ?: return@always
                if (msg != "end") {
                    tempMergeString += msg + "\n"
                    reply("√")
                }
            }
            return@always
        }
        case("start", trim = true) {
            if (startRecord) {
                reply("当前记录未结束")
            } else {
                recordId = sender.id // 记录id
                recordName = sender.nick
                startRecord = true
                reply("开始记录")
            }
            return@case
        }
        case("end", trim = true) {
            if (sender.id == recordId && sender.nick == recordName) {
                recordId = 0L
                recordName = ""
                startRecord = false
                reply(tempMergeString)
                getFriendOrNull(SecretConfig.owner)?.sendMessage(tempMergeString)
                tempMergeString = ""
            }
            return@case
        }
    }
}