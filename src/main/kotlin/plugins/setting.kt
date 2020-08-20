package plugins

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import utils.data.GroupSettingDataMap
import java.lang.NumberFormatException

fun Bot.setting() {
    subscribeGroupMessages {
        // 临时 懒得写
        startsWith(".set", removePrefix = true, trim = true) {
            val msgList = it.split(Regex("""\s*"""))
            val setting = msgList.getOrElse(0) {
                reply("[设置]获取错误\n请发送正确的格式: `.set [设置] [值]`")
            }
            var value = msgList.getOrElse(1) {
                reply("[值]获取错误\n请发送正确的格式: `.set [设置] [值]`")
            }
            val settingList = listOf("问答机率", "机率.问答")
            if (setting in settingList) {
                try {
                    value = value.toString().toDouble()
                    if (value > 0.7 && value < 0) {
                        GroupSettingDataMap.getInstance(this.group).qaProbability = value
                        reply("$setting 设置成功, 现在是 $value")
                    }
                } catch (e: NumberFormatException) {
                    reply("请确认输入的[值]: $value 正确")
                }
            } else {
                reply("[设置]错误, 你是不是想说:\n${settingList.joinToString("\n")}")
            }
        }
    }
}