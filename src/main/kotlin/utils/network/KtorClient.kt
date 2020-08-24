package utils.network

import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import utils.logger.BotLogger


object KtorClient {
    private var singleton: HttpClient? = null
    private val logger = BotLogger.logger("OkHttpUtil")

    fun getInstance(): HttpClient? {
        if (singleton == null) {
            synchronized(KtorClient::class.java) {
                if (singleton == null) {
                    logger.info("Ktor Client Instantiate")
                    singleton = HttpClient {
                        install(JsonFeature) {
                            serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
                                ignoreUnknownKeys = true
                            })
                        }
                    }
                }
            }
        }
        return singleton
    }
}