package com.github.miracle.utils.tools

import com.github.miracle.utils.network.KtorClient
import com.github.miracle.utils.network.model.NovelModel
import io.ktor.client.request.*
import kotlinx.serialization.SerializationException

object Jjwxc {
    private const val url = "http://fun.zhufree.fun/get_novel_info/"

    suspend fun getNovelInfo(nid: Long): NovelModel? {
        val url = url + nid
        val client = KtorClient.getInstance() ?: return null

        return try {
            client.get<NovelModel>(url)
        } catch (e: SerializationException) {
            null
        }
    }
}