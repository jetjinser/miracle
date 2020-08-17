package utils.data

import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.dsl.*
import utils.database.BotDataBase
import utils.database.BotDataBase.Cache
import utils.logger.BotLogger

object MessageCacheData {
    private var query: Query? = null
    private val dataBase: Database? = BotDataBase.getInstance()
    private val logger = BotLogger.logger("MCD")

    private var privateMessageCache: MutableMap<Int, String> = mutableMapOf()

    private fun update() {
        query = dataBase?.from(Cache)?.select()
        query?.forEach {
            it[Cache.messageId]?.let { messageId ->
                it[Cache.content]?.let { content ->
                    privateMessageCache[messageId] = content
                }
            }
        }
    }

    init {
        update()
        logger.info("MessageCacheData initialized")
    }

    val messageCache: MutableMap<Int, String>?
        get() = privateMessageCache

    fun append(messageId: Int, content: String) {
        dataBase?.insert(Cache) {
            it.messageId to messageId
            it.content to content
        }
        this@MessageCacheData.privateMessageCache.apply {
            set(messageId, content)
            putAll(this)
        }
        update()
    }
}
