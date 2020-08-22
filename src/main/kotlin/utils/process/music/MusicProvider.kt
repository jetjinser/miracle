package utils.process.music

import net.mamoe.mirai.message.data.LightApp
import utils.network.OkHttpUtil
import utils.network.Requests
import utils.network.model.NetEaseMusicApiModel
import utils.network.model.NetEaseMusicLightApp
import java.net.URLEncoder

object MusicProvider {
    fun netEaseMusicGen(searchMusicName: String): LightApp {
        val preFormData =
            """{"hlpretag":"<span class=\"s-fc7\">","hlposttag":"</span>","s":"$searchMusicName","type":"1","offset":"0","total":"true","limit":"1","csrf_token":""}"""

        val params = URLEncoder.encode(
            NetEaseFormData.encrypt(preFormData),
            "utf-8"
        )
        val url =
            """https://music.163.com/weapi/cloudsearch/get/web?csrf_token=&params=${params}&encSecKey=${NetEaseFormData.encSecKey}"""


        val response = Requests.post(url)
        val model = OkHttpUtil.gson.fromJson(
            response?.body?.string(),
            NetEaseMusicApiModel::class.java
        )

        val ctime = System.currentTimeMillis() / 1000
        val song = model.result.songs.first()
        val musicName = song.name
        val musicId = song.id
        val preview = song.al.picUrl
        val desc = song.ar.first().name

        val music = NetEaseMusicLightApp(
            "com.tencent.structmsg",
            NetEaseMusicLightApp.Config(
                true,
                ctime,
                true,
                "114514",
                "normal"
            ),
            "音乐",
            NetEaseMusicLightApp.Extra(
                1,
                100495085,
                6863003740196404000
            ),
            NetEaseMusicLightApp.Meta(
                NetEaseMusicLightApp.MusicX(
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
                    "网易云音乐",
                    musicName
                )
            ),
            "[分享]$musicName",
            "0.0.0.1",
            "music"
        )
        val json = OkHttpUtil.gson.toJson(music)
        return LightApp(json)
    }
}