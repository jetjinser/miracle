package utils.network

import com.google.gson.Gson
import okhttp3.OkHttpClient
import utils.logger.BotLogger


object OkHttpUtil {
    private var singleton: OkHttpClient? = null
    private val logger = BotLogger.logger("OkHttpUtil")

    fun getInstance(): OkHttpClient? {
        if (singleton == null) {
            synchronized(OkHttpUtil::class.java) {
                if (singleton == null) {
                    logger.info("OkHttpClient Instantiate")
                    singleton = OkHttpClient()
                }
            }
        }
        return singleton
    }

    val gson: Gson by lazy {
        logger.info("Gson Instantiate")
        Gson()
    }
}