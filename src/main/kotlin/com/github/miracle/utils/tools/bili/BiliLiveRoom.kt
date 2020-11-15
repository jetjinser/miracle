package com.github.miracle.utils.tools.bili

import com.github.miracle.utils.network.KtorClient
import com.github.miracle.utils.network.model.BiliCheckModel
import com.github.miracle.utils.network.model.BiliLiveModel
import io.ktor.client.request.*
import kotlinx.serialization.SerializationException

object BiliLiveRoom {
    private const val liveUrl = "https://api.live.bilibili.com/xlive/web-room/v1/index/getInfoByRoom?room_id="
    private const val unameUrl = "https://api.live.bilibili.com/room_ex/v1/RoomNews/get?roomid="

    suspend fun getBiliLive(bid: Long): BiliLiveModel? {
        val url = liveUrl + bid
        val client = KtorClient.getInstance() ?: return null

        return try {
            client.get<BiliLiveModel>(url)
        } catch (e: SerializationException) {
            null
        }
    }

    suspend fun getBiliLiveUname(bid: Long): BiliCheckModel? {
        val url = unameUrl + bid
        val client = KtorClient.getInstance() ?: return null

        return try {
            client.get<BiliCheckModel>(url)
        } catch (e: SerializationException) {
            null
        }
    }

    suspend fun getUname(bid: Long): String? {
        val cLive = getBiliLiveUname(bid) ?: return null
        return cLive.data.uname
    }
}