package com.github.miracle.utils.data

import com.github.miracle.utils.database.BotDataBase
import com.github.miracle.utils.database.BotDataBase.NovelSubscription
import com.github.miracle.utils.logger.BotLogger
import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.dsl.*
import me.liuwj.ktorm.schema.Column
import org.sqlite.SQLiteException

object NovelSubData {
    private val dataBase: Database? = BotDataBase.getInstance()
    private val logger = BotLogger.logger("BSD")


    init {
        logger.info("NovelSubData initialized")
    }

    //                                         {novelId:chapterId}
    private val novelUpdateCache = mutableMapOf<Long, Int>()
    private val novelQueue = getNovelQueue()
    private var iter = novelQueue.iterator()   // nid, list(gid)
    fun nextSub(): Pair<MutableMap.MutableEntry<Long, MutableList<Long>>, MutableMap<Long, Int>> =
        if (iter.hasNext()) {
            iter.next()
        } else {
            iter = novelQueue.iterator()
            iter.next()
        } to novelUpdateCache


    fun markLastChapter(nid: Long, cid: Int) {
        novelUpdateCache[nid] = cid
    }

//    fun markUnliving(nid: Long) {
//        novelUpdateCache[nid] = false
//    }

    private fun getNovelQueue(): MutableMap<Long, MutableList<Long>> {
        val ret = mutableMapOf<Long, MutableList<Long>>()
        if (dataBase == null) {
            throw NullPointerException()
        } else {
            dataBase.from(NovelSubscription)
                .select(NovelSubscription.novelId, NovelSubscription.groupId)
                .forEach { queryRowSet ->
                    val ni = queryRowSet[NovelSubscription.novelId]
                    val gi = queryRowSet[NovelSubscription.groupId]
                    if (ni != null && gi != null) {
                        ret[ni] = mutableListOf(gi)
                        novelUpdateCache[ni] = 0
                    }
                }
            return ret
        }
    }

    suspend fun subscribe(groupId: Long, nid: Long, title: String): Boolean {
        return if (dataBase == null) {
            false
        } else {
            try {
                dataBase.insert(NovelSubscription) {
                    it.groupId to groupId
                    it.novelId to nid
                    it.title to title
                }
                val mutableList = novelQueue[nid]
                if (mutableList == null) {
                    novelQueue[nid] = mutableListOf(groupId)
                } else {
                    mutableList.add(groupId)
                }
                true
            } catch (e: SQLiteException) {
                false
            }
        }
    }

    fun unsubscribe(groupId: Long, nid: Long): Boolean {
        val effects = dataBase?.delete(NovelSubscription) {
            it.groupId eq groupId
            it.novelId eq nid
        }
        return if (effects != null) effects != 0 else false
    }

    private fun selectFromBS(vararg column: Column<*>, block: Query.() -> Query?): Query? {
        return if (dataBase == null) {
            null
        } else {
            return dataBase.from(NovelSubscription).select(*column).block()
        }
    }

    fun getSubList(groupId: Long): MutableList<Pair<Long, String>>? {
        val query = selectFromBS(NovelSubscription.novelId, NovelSubscription.title) {
            where {
                NovelSubscription.groupId eq groupId
            }
        }
        val ret = mutableListOf<Pair<Long, String>>()
        query?.forEach { queryRowSet ->
            val ri = queryRowSet[NovelSubscription.novelId]
            val un = queryRowSet[NovelSubscription.title]
            if (ri != null && un != null) ret.add(ri to un)
        }
        return if (ret.isEmpty()) null else ret
    }
}