package utils.network


import okhttp3.*


object Requests {
    private val client = OkHttpUtil.getInstance()

    fun get(url: String, callback: Callback) {
        val request = Request.Builder()
            .get()
            .url(url)
            .build()
        client?.newCall(request)?.enqueue(callback)
    }

    fun post(url: String, requestBody: RequestBody? = null): Response? {
        var rb = requestBody
        if (rb == null) rb = FormBody.Builder().build()
        val request = Request.Builder()
            .post(rb)
            .url(url)
            .build()
        return client?.newCall(request)?.execute()
    }

    fun head(url: String): Response? {
        val request = Request.Builder()
            .head()
            .url(url)
            .build()
        return client?.newCall(request)?.execute()
    }
}