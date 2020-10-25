package com.github.miracle.utils.tools.music

import com.github.miracle.SecretConfig
import com.github.miracle.utils.network.KtorClient
import com.github.miracle.utils.network.model.music.KuGouMusicInfoApiModel
import com.github.miracle.utils.network.model.music.KuGouMusicSearchApiModel
import com.github.miracle.utils.network.model.music.MusicLightApp
import com.github.miracle.utils.network.model.music.NetEaseMusicApiModel
import io.ktor.client.request.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.mamoe.mirai.message.data.LightApp
import java.net.URLEncoder

@Suppress("BlockingMethodInNonBlockingContext")  // 哭了
object MusicProvider {
    suspend fun netEaseMusicGen(searchMusicName: String): LightApp? {
        val preFormData =
            """{"hlpretag":"<span class=\"s-fc7\">","hlposttag":"</span>","s":"$searchMusicName","type":"1","offset":"0","total":"true","limit":"1","csrf_token":""}"""

        val params = URLEncoder.encode( // 阻塞方法
            NetEaseFormData.encrypt(preFormData),
            "utf-8"
        )
        val url =
            """https://music.163.com/weapi/cloudsearch/get/web?csrf_token=&params=${params}&encSecKey=${NetEaseFormData.encSecKey}"""

        val client = KtorClient.getInstance() ?: return null

        val model = client.post<NetEaseMusicApiModel>(url)

        val ctime = System.currentTimeMillis() / 1000
        val song = model.result.songs.first()
        val musicName = song.name
        val musicId = song.id
        val preview = song.al.picUrl
        val desc = song.ar.first().name

        val music = MusicLightApp(
            "com.tencent.structmsg",
            MusicLightApp.Config(
                true,
                ctime,
                true,
                "93cb506574a4af2ab61c3df05df204f9",
                "normal"
            ),
            "音乐",
//            MusicLightApp.Extra(
//                1,
//                100495085,
//                6863003740196404000
//            ),
            MusicLightApp.Meta(
                MusicLightApp.MusicX(
                    "",
                    "",
                    1,
                    100495085,
                    desc, // singer
                    "https://y.music.163.com/m/song/$musicId",
                    "http://music.163.com/song/media/outer/url?id=$musicId",
                    preview,
                    "",
                    "0",
                    "",
                    "网易云音乐 via SongOrder",
                    musicName
                )
            ),
            "[分享]$musicName via SongOrder",
            "0.0.0.1",
            "music"
        )
        val json = KtorClient.json.encodeToString(music)
        return LightApp(json)
    }

    suspend fun kuGouMusicGen(searchMusicName: String): LightApp? {
        val searchUrl =
            "http://mobilecdn.kugou.com/api/v3/search/song?format=json&keyword=$searchMusicName&page=1&pagesize=1&showtype=1"

        val client = KtorClient.getInstance() ?: return null

        val resp = client.get<String>(searchUrl)
        val searchApiModel = KtorClient.json.decodeFromString<KuGouMusicSearchApiModel>(resp)

        val hash = searchApiModel.data.info.firstOrNull()?.hash ?: return null

        val infoUrl = "https://www.kugou.com/yy/index.php?r=play/getdata&hash=$hash"

        val infoResp = client.get<String>(infoUrl) {
            headers {
                set("Cookie", "kg_mid=${SecretConfig.kgMid}")
            }
        }

        val infoModel = KtorClient.json.decodeFromString<KuGouMusicInfoApiModel>(infoResp)

        val ctime = System.currentTimeMillis() / 1000
        val data = infoModel.data

        val music = MusicLightApp(
            "com.tencent.structmsg",
            MusicLightApp.Config(
                true,
                ctime,
                true,
                "114514",
                "normal"
            ),
            "音乐",
//            MusicLightApp.Extra(
//                1,
//                100495085,
//                6863003740196404000
//            ),
            MusicLightApp.Meta(
                MusicLightApp.MusicX(
                    "",
                    "",
                    1,
                    100495085,
                    data.authorName, // singer
                    "",
                    data.playUrl,
                    data.img,
                    "",
                    "0",
                    "",
                    "酷狗音乐 via SongOrder",
                    data.songName
                )
            ),
            "[分享]${data.songName} via SongOrder",
            "0.0.0.1",
            "music"
        )
        val json = KtorClient.json.encodeToString(music)
        return LightApp(json)
    }
}
