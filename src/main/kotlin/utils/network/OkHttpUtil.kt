package utils.network

import okhttp3.OkHttpClient


object OkHttpUtil {
    private var singleton: OkHttpClient? = null

    fun getInstance(): OkHttpClient? {
        if (singleton == null) {
            synchronized(OkHttpUtil::class.java) {
                if (singleton == null) {
                    singleton = OkHttpClient()
                }
            }
        }
        return singleton
    }
}