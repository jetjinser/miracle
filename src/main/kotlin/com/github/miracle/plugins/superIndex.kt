package com.github.miracle.plugins

import com.github.miracle.utils.network.KtorClient
import com.github.miracle.utils.network.model.NovelModel
import com.github.miracle.utils.network.model.SuperIndexModel
import io.ktor.client.request.*
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import net.mamoe.mirai.Bot
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.message.data.sendTo
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule

private const val url = "http://fun.zhufree.fun/super-index-rss/"
private const val lizzieBar = 637538362L
fun Bot.superIndex() {
    Timer().schedule(Date(), period = TimeUnit.MINUTES.toMillis(5)) {
        val url = url + "1008087da64813703d2d3e10ac7e5814c27d65"
        launch {
            val client = KtorClient.getInstance()
            val superModel = client?.get<SuperIndexModel>(url)
            val contact = getGroupOrFail(lizzieBar)
            superModel?.let { it ->
                if (it.status == 0) {
                    it.result.forEach { model ->
                        // 是在五分钟内发的
                        if (System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5) < model.time_unix) {
                            buildMessageChain {
                                add("${model.content}\n")
                                if (model.ttarticleLink.isNotEmpty()) {
                                    add("${model.ttarticleLink}\n")
                                }
                                if (model.imgUrls.isNotEmpty()) {
                                    model.imgUrls.forEach {
                                        val byteArray = client.get<ByteArray>(it)
                                        add(byteArray.inputStream().uploadAsImage(contact))
                                    }
                                }
                                add("by ${model.author} at ${model.time}\n")
                            }.sendTo(contact)
                        }
                    }
                }
            }
        }
    }
}
