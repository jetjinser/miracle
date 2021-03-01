package com.github.miracle.utils.data

import com.github.miracle.utils.database.BotDataBase
import com.github.miracle.utils.database.BotDataBase.Lottery
import com.github.miracle.utils.database.BotDataBase.JoinLottery
import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.dsl.*
import me.liuwj.ktorm.entity.Tuple4
import me.liuwj.ktorm.entity.Tuple5
import org.sqlite.SQLiteException

object LotteryData {
    private val dataBase: Database? = BotDataBase.getInstance()
    fun addNewLottery(
        groupId: Long,
        sponsorId: Long,
        prize: String,
        count: Int,
        deadline: Long,
        limit: Int
    ): Int {
        return if (dataBase == null) {
            -1
        } else {
            try {
                return dataBase.insertAndGenerateKey(Lottery) {
                    it.groupId to groupId
                    it.sponsorId to sponsorId
                    it.prize to prize
                    it.count to count
                    it.title to "$sponsorId 抽 $count 个 $prize"
                    it.limit to limit
                    it.deadline to deadline
                } as Int
            } catch (e: SQLiteException) {
                -1
            }
        }
    }

    fun joinLottery(qqId: Long, lotId: String): Boolean {
        return if (dataBase == null) {
            false
        } else {
            try {
                dataBase.insert(JoinLottery) {
                    it.lotId to lotId.toInt()
                    it.qqId to qqId
                }
                true
            } catch (e: SQLiteException) {
                false
            } catch (e: NumberFormatException) {
                false
            }
        }
    }

    fun getLotteryList(groupId: Long): List<Tuple4<Int, Int, String, Long>> {
        val result = emptyList<Tuple4<Int, Int, String, Long>>().toMutableList()
        val query = dataBase?.from(Lottery)
            ?.select(Lottery.title, Lottery.id, Lottery.limit, Lottery.deadline)
            ?.where {
                Lottery.groupId eq groupId
            }
        query?.forEach { queryRowSet ->
            val lotId = queryRowSet[Lottery.id]
            val title = queryRowSet[Lottery.title]
            val limit = queryRowSet[Lottery.limit] ?: 0
            val deadline = queryRowSet[Lottery.deadline] ?: 0L
            if (lotId != null && title != null) result.add(Tuple4(lotId, limit, title, deadline))
        }
        return result
    }

    fun isLotteryAble(lotId: Int, groupId: Long): Boolean {
        var limit = 0
        val limitQuery = dataBase?.from(Lottery)
            ?.select(Lottery.limit, Lottery.groupId, Lottery.deadline)
            ?.where {
                Lottery.id eq lotId
            }
        limitQuery?.forEach { queryRowSet ->
            limit = queryRowSet[Lottery.limit] ?: 0
            val ltGroupId = queryRowSet[Lottery.groupId] ?: 0
            val ddl = queryRowSet[Lottery.deadline] ?: 0
            if (ltGroupId != groupId) return false
            if (ddl < System.currentTimeMillis()) return false
        }
        if (limit == 0) return true
        val joinQ = dataBase?.from(JoinLottery)
            ?.select(JoinLottery.qqId)
            ?.where { JoinLottery.lotId eq lotId }
        return limit >= joinQ?.totalRecords ?: 0
    }


    fun getAllLotteryList(): List<Tuple5<Int, Long, Int, Long, Int>> { // id, groupId, limit, ddl, count
        val result = emptyList<Tuple5<Int, Long, Int, Long, Int>>().toMutableList()
        val q = dataBase?.from(Lottery)?.select(Lottery.id, Lottery.groupId, Lottery.limit,
            Lottery.deadline, Lottery.count)
        q?.forEach {
            val id = it[Lottery.id]
            val groupId = it[Lottery.groupId]
            val limit = it[Lottery.limit]
            val ddl = it[Lottery.deadline]
            val count = it[Lottery.count]
            if (id != null && ddl != null && groupId != null && limit != null&& count != null) {
                result.add(Tuple5(id, groupId, limit, ddl, count))
            }
        }
        return result
    }

    fun getLotteryInfoById(id: Int):String {
        val q = dataBase?.from(Lottery)?.select(Lottery.title)
            ?.where { Lottery.id eq id }
        var result = ""
        q?.forEach {
            val title = it[Lottery.title]
            result = "$title"
        }
        return result
    }

    fun getJoinId(id: Int):List<Long> {
        val q = dataBase?.from(JoinLottery)?.select(JoinLottery.qqId)
            ?.where { JoinLottery.lotId eq id }
        val result = mutableListOf<Long>()
        q?.forEach {
            val qqId = it[JoinLottery.qqId]
            qqId?.let {
                result.add(qqId)
            }
        }
        return result
    }

    fun deleteLottery(id: Int) {
        dataBase?.delete(Lottery) {
            it.id eq id
        }
    }
}