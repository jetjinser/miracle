package utils.network.model

import com.google.gson.annotations.SerializedName

data class NetEaseMusicLightApp(
    @SerializedName("app")
    val app: String,
    @SerializedName("config")
    val config: Config,
    @SerializedName("desc")
    val desc: String,
    @SerializedName("extra")
    val extra: Extra,
    @SerializedName("meta")
    val meta: Meta,
    @SerializedName("prompt")
    val prompt: String,
    @SerializedName("ver")
    val ver: String,
    @SerializedName("view")
    val view: String
) {
    data class Config(
        @SerializedName("autosize")
        val autosize: Boolean,
        @SerializedName("ctime")
        val ctime: Long,
        @SerializedName("forward")
        val forward: Boolean,
        @SerializedName("token")
        val token: String,
        @SerializedName("type")
        val type: String
    )

    data class Extra(
        @SerializedName("app_type")
        val appType: Int,
        @SerializedName("appid")
        val appid: Int,
        @SerializedName("msg_seq")
        val msgSeq: Long
    )

    data class Meta(
        @SerializedName("music")
        val music: MusicX
    )

    data class MusicX(
        @SerializedName("action")
        val action: String,
        @SerializedName("android_pkg_name")
        val androidPkgName: String,
        @SerializedName("app_type")
        val appType: Int,
        @SerializedName("appid")
        val appid: Int,
        @SerializedName("desc")
        val desc: String,
        @SerializedName("jumpUrl")
        val jumpUrl: String,
        @SerializedName("musicUrl")
        val musicUrl: String,
        @SerializedName("preview")
        val preview: String,
        @SerializedName("source_icon")
        val sourceIcon: String,
        @SerializedName("sourceMsgId")
        val sourceMsgId: String,
        @SerializedName("source_url")
        val sourceUrl: String,
        @SerializedName("tag")
        val tag: String,
        @SerializedName("title")
        val title: String
    )
}