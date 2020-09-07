package com.github.miracle.utils.network

import com.github.miracle.utils.logger.BotLogger
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import kotlinx.serialization.json.Json


object KtorClient {
    private var singleton: HttpClient? = null
    private val logger = BotLogger.logger("KCI")

    fun getInstance(): HttpClient? {
        if (singleton == null) {
            synchronized(KtorClient::class.java) {
                if (singleton == null) {
                    logger.info("Ktor Client Instantiate")
                    singleton = HttpClient {
                        install(JsonFeature) {
                            serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
                                ignoreUnknownKeys = true
                                isLenient = true
                            })
                        }
                    }
                }
            }
        }
        return singleton
    }

    val json: Json by lazy { Json { ignoreUnknownKeys = true; isLenient = true } }
}