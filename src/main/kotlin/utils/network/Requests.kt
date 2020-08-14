package utils.network


import okhttp3.Callback
import okhttp3.Request


object Requests {
    private val client = OkHttpUtil.getInstance()

    fun get(url: String, callback: Callback) {
        val request = Request.Builder()
            .get()
            .url(url)
            .build()
        client?.newCall(request)?.enqueue(callback)
    }
}