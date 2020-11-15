package com.github.miracle.utils.data

import com.github.miracle.utils.database.BotDataBase
import com.github.miracle.utils.database.BotDataBase.BiliSubscription
import com.github.miracle.utils.logger.BotLogger
import com.github.miracle.utils.tools.bili.BiliLiveRoom
import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.dsl.*
import me.liuwj.ktorm.schema.Column
import org.sqlite.SQLiteException

object BiliSubData {
    private val dataBase: Database? = BotDataBase.getInstance()
    private val logger = BotLogger.logger("BSD")


    init {
        logger.info("BiliSubData initialized")
    }

    private val liveCache = mutableMapOf<Long, Boolean>()
    private val liveQueue = getLiveQueue()
    private var iter = liveQueue.iterator()
    fun nextSub(): Pair<MutableMap.MutableEntry<Long, MutableList<Long>>, MutableMap<Long, Boolean>> =
        if (iter.hasNext()) {
            iter.next()
        } else {
            iter = liveQueue.iterator()
            iter.next()
        } to liveCache


    fun markLiving(bid: Long) {
        liveCache[bid] = true
    }

    fun markUnliving(bid: Long) {
        liveCache[bid] = false
    }

    private fun getLiveQueue(): MutableMap<Long, MutableList<Long>> {
        val ret = mutableMapOf<Long, MutableList<Long>>()
        if (dataBase == null) {
            throw NullPointerException()
        } else {
            dataBase.from(BiliSubscription)
                .select(BiliSubscription.roomId, BiliSubscription.groupId)
                .forEach { queryRowSet ->
                    val ri = queryRowSet[BiliSubscription.roomId]
                    val gi = queryRowSet[BiliSubscription.groupId]
                    if (ri != null && gi != null) {
                        ret[ri] = mutableListOf(gi)
                        liveCache[gi] = false
                    }
                }
            return ret
        }
    }

    suspend fun subscribe(groupId: Long, bid: Long): Boolean {
        return if (dataBase == null) {
            false
        } else {
            val uname = BiliLiveRoom.getUname(bid) ?: return false
            try {
                dataBase.insert(BiliSubscription) {
                    it.groupId to groupId
                    it.roomId to bid
                    it.uname to uname
                }
                val mutableList = liveQueue[bid]
                if (mutableList == null) {
                    liveQueue[bid] = mutableListOf(groupId)
                } else {
                    mutableList.add(groupId)
                }
                true
            } catch (e: SQLiteException) {
                false
            }
        }
    }

    fun unsubscribe(groupId: Long, bid: Long): Boolean {
        val effects = dataBase?.delete(BiliSubscription) {
            it.groupId eq groupId
            it.roomId eq bid
        }
        return if (effects != null) effects != 0 else false
    }

    private fun selectFromBS(vararg column: Column<*>, block: Query.() -> Query?): Query? {
        return if (dataBase == null) {
            null
        } else {
            return dataBase.from(BiliSubscription).select(*column).block()
        }
    }

    fun getSubList(groupId: Long): MutableList<Pair<Long, String>>? {
        val query = selectFromBS(BiliSubscription.roomId, BiliSubscription.uname) {
            where {
                BiliSubscription.groupId eq groupId
            }
        }
        val ret = mutableListOf<Pair<Long, String>>()
        query?.forEach { queryRowSet ->
            val ri = queryRowSet[BiliSubscription.roomId]
            val un = queryRowSet[BiliSubscription.uname]
            if (ri != null && un != null) ret.add(ri to un)
        }
        return if (ret.isEmpty()) null else ret
    }
}