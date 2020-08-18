package utils.network


import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response


object Requests {
    private val client = OkHttpUtil.getInstance()

    fun get(url: String, callback: Callback) {
        val request = Request.Builder()
            .get()
            .url(url)
            .build()
        client?.newCall(request)?.enqueue(callback)
    }

    fun head(url: String): Response? {
        val request = Request.Builder()
            .head()
            .url(url)
            .build()
        return client?.newCall(request)?.execute()
    }
}