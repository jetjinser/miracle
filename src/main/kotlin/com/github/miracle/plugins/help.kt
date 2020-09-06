package com.github.miracle.plugins

import com.github.miracle.utils.tools.help.Helper
import com.github.miracle.utils.tools.help.Plugin
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.Listener
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.PlainText

fun Bot.help() {
    val pluginNameArray = arrayOf(
        "反小程序", "bilibili", "内置回复", "按钮", "签到",
        "帮助", "信息", "音乐", "随机", "响应", "提醒", "内置提醒", "设置", "图灵", "词库", "WoPay"
    )

    subscribeGroupMessages(priority = Listener.EventPriority.LOW) {
        Regex(""".*(帮助|菜单|(?i)help).*""") matching regex@{
            if (message[At]?.target != bot.id) return@regex

            val pluginName = message[PlainText]?.content?.split(" ")?.filter { it.isNotEmpty() }?.getOrNull(1)

            if (pluginName == null) {
                // 直接发送 help
                reply(
                    """我目前的功能有:
                        |${pluginNameArray.joinToString("\n")}
                        |可发送 帮助 <功能名> 来查看详情
                        |readme: 待填
                    """.trimMargin()
                )
                intercept()
            } else {
                // 发送 plugin 描述
                reply(Helper(pluginName).getDesc() ?: "没有找到匹配的功能")
                intercept()
            }
        }
    }
}