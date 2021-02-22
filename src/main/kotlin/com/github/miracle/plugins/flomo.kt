package com.github.miracle.plugins

import com.github.miracle.utils.database.BotDataBase
import com.github.miracle.utils.database.BotDataBase.Flomo
import com.github.miracle.utils.network.KtorClient
import io.ktor.client.request.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.dsl.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeFriendMessages
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.message.nextMessage

fun Bot.flomo() {
    val dataBase: Database? = BotDataBase.getInstance()
    eventChannel.subscribeFriendMessages {
        case("flomo") {
            // 绑定api key
            subject.sendMessage("请发送flomo的api key:https://flomoapp.com/{api-key}")
            val key = nextMessage { message[MessageSource.Key] != null }.content.trim()

            if (key.isNotEmpty()) {
                dataBase?.insert(Flomo) {
                    it.qqId to friend.id
                    it.flomoKey to key
                }
                // TODO update
                subject.sendMessage("绑定成功")
            }
        }
        Regex("""flomo .*""") matching {
            val query = dataBase?.from(Flomo)?.select(Flomo.qqId, Flomo.flomoKey)?.where {
                Flomo.qqId eq friend.id
            }
            val content = it.substringAfter("flomo").trim()
            query?.let {
                query.forEach {
                    val key = it[Flomo.flomoKey] ?: ""
                    // post
                    val client = KtorClient.getInstance()
                    val jsonString = "{\"content\":\"$content\"}"
                    val response = client?.post<String> {
                        url("https://flomoapp.com/$key")
                        header("Content-Type", "application/json")
                        body = Json.parseToJsonElement(jsonString)
                    }
                    response?.let {
                        val jsonObj = Json.decodeFromString(JsonObject.serializer(), response)
                        subject.sendMessage(jsonObj["message"].toString())
                    }
                }
            }?: kotlin.run {
                subject.sendMessage("未绑定flomo api key，回复[flomo]开始绑定")
            }
        }
    }
}