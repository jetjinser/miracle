package com.github.miracle.plugins

import com.github.miracle.SecretConfig.tkk
import com.github.miracle.utils.network.KtorClient
import com.github.miracle.utils.tools.translate.ScriptInvocable
import io.ktor.client.request.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.Listener
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.nextMessage
import java.util.concurrent.TimeUnit

private fun CharSequence.isContainChinese(): Boolean {
    return Regex("[\u4e00-\u9fa5]").containsMatchIn(this)
}

fun Bot.translate() {
    eventChannel.subscribeGroupMessages(priority = EventPriority.LOWEST) {
        Regex("""(?i).*(翻译|fanyi|translate).*""") matching regex@{
            val at = message[MessageSource.Key]
            if (at != null) if (at.targetId != bot.id) return@regex

//            val m = message.filter { it.contentToString() } TODO
            val first = message.content

            var original = Regex("""(?i)翻译|fanyi|translate""").replace(first.trim(), "").trim()

            original = if (original.isEmpty()) {
                subject.sendMessage("你要翻译什么?")
                intercept()
                nextMessage(timeoutMillis = TimeUnit.MINUTES.toMillis(3), priority = EventPriority.HIGH) {
                    message[MessageSource.Key] != null // TODO
                }.content
            } else original
            val tl = if (original.isContainChinese()) "auto" else "zh-CN"

            val tk = ScriptInvocable.invocable.invokeFunction("tk", original, tkk)
            val gTranslateUrl =
                "https://translate.google.cn/translate_a/single" +
                        "?client=webapp&sl=auto&tl=$tl&hl=zh-CN&dt=at&dt=bd&dt=ex&dt=ld&dt=md&dt=qca&dt=rw&dt=rm&dt=ss&dt=t" +
                        "&source=bh&ssel=0&tsel=0&kc=1&tk=$tk&q=$original"

            val client = KtorClient.getInstance() ?: return@regex
            val resp = client.get<String>(gTranslateUrl)
            subject.sendMessage(resp.substringAfter("\"").substringBefore("\""))
        }
    }
}