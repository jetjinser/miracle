package com.github.miracle.plugins.base

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.content

fun Bot.random() {
    eventChannel.subscribeGroupMessages {
        startsWith("随机数", removePrefix = true, trim = true) { subject.sendMessage(randomNumber(message.content)) }

        startsWith("打乱", removePrefix = true, trim = true) {
            subject.sendMessage(
                message.content
                    .split(" ")
                    .filter { it.isNotEmpty() }
                    .drop(1) // FIX removePrefix 不起作用
                    .shuffled().joinToString(" ")
            )
        }

        startsWith("抽签") {
            if (message.content.isEmpty()) {
                subject.sendMessage("格式错误，需要提供要抽签的内容")
                return@startsWith
            }
            subject.sendMessage(
                message.content
                    .split(" ")
                    .filter { it.isNotEmpty() }
                    .drop(1)
                    .random()
            )
        }
    }
}

private fun randomNumber(message: String): String {
    var start = 1
    var end = 100

    val msg = message.split(" ").filter { it.isNotEmpty() }

    try {
        // FIX remove prefix 不起作用
        if (msg[0] == "随机数") {
            start = msg[1].toInt()
            end = msg[2].toInt()
        } else {
            start = msg[0].toInt()
            end = msg[1].toInt()
        }
    } catch (e: NumberFormatException) {
        return "需要是数字"
    } catch (e: IndexOutOfBoundsException) {
    }

    start = minOf(start, end)
    end = maxOf(start, end)
    if (start < -10000 || end > 10000) {
        return "范围过大"
    }

    return (start..end).random().toString()
}
