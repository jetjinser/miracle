package com.github.miracle.plugins.base

import com.github.miracle.SecretConfig.tkk
import com.github.miracle.utils.network.KtorClient
import com.github.miracle.utils.network.model.MorseModel
import com.github.miracle.utils.tools.translate.ScriptInvocable
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.nextMessage
import java.util.concurrent.TimeUnit

private fun CharSequence.isContainChinese(): Boolean {
    return Regex("[\u4e00-\u9fa5]").containsMatchIn(this)
}

fun Bot.translate() {
    eventChannel.subscribeGroupMessages {
        Regex("""(?i).*(翻译|fanyi|translate).*""") matching regex@{
            if (message.firstIsInstanceOrNull<At>()?.target == bot.id) {
                val first = message.first { it is PlainText }.content
                var original =
                    Regex("""(?i)翻译|fanyi|translate""").replace(first.trim(), "").trim()

                original = if (original.isEmpty()) {
                    subject.sendMessage("你要翻译什么?")
                    intercept()
                    nextMessage(
                        timeoutMillis = TimeUnit.MINUTES.toMillis(3),
                        priority = EventPriority.HIGH
                    ) {
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
        Regex("""(?i).*(morse|摩斯电码|摩斯).*""") matching regex@{
            if (message.firstIsInstanceOrNull<At>()?.target == bot.id) {
                val msg = message.first { it is PlainText }.content
                var original = Regex("""(?i).*(摩斯电码|摩斯|morse)""").replace(msg.trim(), "").trim()
                original = if (original.isEmpty()) {
                    subject.sendMessage("你要编码或解码什么?")
                    intercept()
                    nextMessage(
                        timeoutMillis = TimeUnit.MINUTES.toMillis(3),
                        priority = EventPriority.HIGH
                    ) {
                        message[MessageSource.Key] != null
                    }.content
                } else original
                val client = KtorClient.getInstance() ?: return@regex
                val translateType = if (original.contains(Regex("""\.|-|/""""))) {
                    "decode"
                } else {
                    "encode"
                }
                val resp = client.post<MorseModel> {
                    url("https://tool.lu/morse/ajax.html")
                    header("origin", "https://tool.lu")
                    header("referer", "https://tool.lu/morse/")
                    body = MultiPartFormDataContent(formData {
                        append("code", original)
                        append("operate", translateType)
                    })
                }
                subject.sendMessage(resp.text)
            }
        }
    }
}