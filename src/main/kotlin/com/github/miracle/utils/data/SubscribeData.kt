package com.github.miracle.utils.data

import com.github.miracle.utils.database.BotDataBase
import com.github.miracle.utils.database.BotDataBase.Subscription
import com.github.miracle.utils.database.BotDataBase.Platform.JJWXC
import com.github.miracle.utils.logger.BotLogger
import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.dsl.*
import org.sqlite.SQLiteException

/**
 * 各个平台的订阅统一一下
 */
object SubscribeData {
    private val dataBase: Database? = BotDataBase.getInstance()
    private val logger = BotLogger.logger("BSD")
    val novelSubQueue by lazy {
        loadSubQueue(JJWXC)
    }

    init {
        logger.info("SubData initialized")
    }

    fun getQueue(platform: Int): MutableMap<Long, MutableList<Long>> {
        return when (platform) {
            0 -> mutableMapOf<Long, MutableList<Long>>()
            1 -> novelSubQueue
            else -> mutableMapOf<Long, MutableList<Long>>()
        }
    }

    /**
     * 根据platform取该平台的订阅列表：
     * Return: Map<订阅id，订阅该id的所有群号>
     */
    fun loadSubQueue(platform: Int): MutableMap<Long, MutableList<Long>> {
        val result = mutableMapOf<Long, MutableList<Long>>()
        if (dataBase == null) {
            throw NullPointerException()
        } else {
            dataBase.from(BotDataBase.Subscription)
                .select(BotDataBase.Subscription.objectId, BotDataBase.Subscription.groupId).where {
                    BotDataBase.Subscription.platform eq platform
                }
                .forEach { queryRowSet ->
                    val objId = queryRowSet[BotDataBase.Subscription.objectId]
                    val groupIds = queryRowSet[BotDataBase.Subscription.groupId]
                    if (objId != null && groupIds != null) {
                        result[objId] = mutableListOf(groupIds)
//                        novelUpdateCache[objId] = 0
                    }
                }
            return result
        }
    }

    /**
     * 添加新订阅
     */
    fun subscribe(groupId: Long, objectId: Long, title: String, platform: Int): Boolean {
        return if (dataBase == null) {
            false
        } else {
            try {
                dataBase.insert(BotDataBase.Subscription) {
                    it.groupId to groupId
                    it.objectId to objectId
                    it.title to title
                    it.platform to platform
                }
                // 将第一个订阅该obj的群号加入queue中
                val mutableList = getQueue(platform)[objectId]
                if (mutableList == null) {
                    getQueue(platform)[objectId] = mutableListOf(groupId)
                } else {
                    mutableList.add(groupId)
                }
                true
            } catch (e: SQLiteException) {
                false
            }
        }
    }

    /**
     * 取消订阅
     */
    fun unsubscribe(groupId: Long, objId: Long, platform: Int): Boolean {
        println("groupid:$groupId, objId:$objId, platform:$platform")
        val effects = dataBase?.delete(Subscription) {
            it.platform eq platform
            it.groupId eq groupId
            it.objectId eq objId
        }
        println(effects)
        return if (effects != null) effects != 0 else false
    }

    /**
     * 获取一个群的所有订阅
     */
    fun getAllSubList(groupId: Long): MutableList<Pair<Long, String>>? {
        val query = dataBase?.from(Subscription)?.select(Subscription.objectId, Subscription.title)
            ?.where {
                Subscription.groupId eq groupId
            }
        val result = mutableListOf<Pair<Long, String>>()
        query?.forEach { queryRowSet ->
            val objId = queryRowSet[Subscription.objectId]
            val title = queryRowSet[Subscription.title]
            if (objId != null && title != null) result.add(objId to title)
        }
        return if (result.isEmpty()) null else result
    }

    /**
     * 获取一个群单平台订阅
     */
    fun getPlatformSubList(groupId: Long, platform: Int): MutableList<Pair<Long, String>>? {
        val query = dataBase?.from(Subscription)?.select(Subscription.objectId, Subscription.title)
            ?.where {
                Subscription.groupId eq groupId
                Subscription.platform eq platform
            }
        val result = mutableListOf<Pair<Long, String>>()
        query?.forEach { queryRowSet ->
            val objId = queryRowSet[Subscription.objectId]
            val title = queryRowSet[Subscription.title]
            if (objId != null && title != null) result.add(objId to title)
        }
        return if (result.isEmpty()) null else result
    }
}