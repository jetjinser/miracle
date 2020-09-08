package com.github.miracle.plugins

import com.github.miracle.SecretConfig.tkk
import com.github.miracle.utils.network.KtorClient
import com.github.miracle.utils.tools.translate.ScriptInvocable
import io.ktor.client.request.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages

private fun CharSequence.isContainChinese(): Boolean {
    return Regex("[\u4e00-\u9fa5]").containsMatchIn(this)
}

fun Bot.translate() {
    subscribeGroupMessages {
        Regex("""(?i)\s*(翻译|fanyi|translate).*""") matching regex@{
            val original = Regex("""(?i)翻译|fanyi|translate""").replace(it.trim(), "").trim()

            if (original.isEmpty()) return@regex

            val tl = if (original.isContainChinese()) "auto" else "zh-CN"

            val tk = ScriptInvocable.invocable.invokeFunction("tk", original, tkk)
            val gTranslateUrl =
                "https://translate.google.cn/translate_a/single" +
                        "?client=webapp&sl=auto&tl=$tl&hl=zh-CN&dt=at&dt=bd&dt=ex&dt=ld&dt=md&dt=qca&dt=rw&dt=rm&dt=ss&dt=t" +
                        "&source=bh&ssel=0&tsel=0&kc=1&tk=$tk&q=$original"

            val client = KtorClient.getInstance() ?: return@regex
            val resp = client.get<String>(gTranslateUrl)
            reply(resp.substringAfter("\"").substringBefore("\""))
        }
    }
}