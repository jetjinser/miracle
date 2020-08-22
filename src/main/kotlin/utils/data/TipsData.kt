package utils.data

import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.dsl.*
import me.liuwj.ktorm.expression.SqlExpression
import org.sqlite.SQLiteException
import utils.database.BotDataBase
import utils.database.BotDataBase.Tip
import utils.logger.BotLogger
import java.time.LocalDate

object TipsData {
    private val dataBase: Database? = BotDataBase.getInstance()
    private val querySource = dataBase?.from(Tip)
    private val logger = BotLogger.logger("TPD")

    private val tips = mutableListOf<String>()
    private var id = 0

    init {
        querySource?.apply {
            select(Tip.tip).where { Tip.review eq 1 }.forEach {
                it[Tip.tip]?.let { tip -> tips.add(tip) }
            }

            select(Tip.id).orderBy(Tip.id.desc()).limit(0, 1).forEach {
                val queryId = it[Tip.id]
                if (queryId != null) id = queryId
            }
        }

        logger.info("TipsData initialized. Current id: $id")
    }

    val tip: String?
        get() = if (tips.isEmpty()) null else tips.random()


    fun add(tip: String, qqId: Long): Boolean {
        if (tip.isEmpty()) return false
        try {
            dataBase?.insert(Tip) {
                it.qqId to qqId
                it.tip to tip
                it.date to LocalDate.now().toString()
                it.review to null
            }
        } catch (e: SQLiteException) {
            return false
        }
        logger.info("新增待审核 tip: [$tip], Added by $qqId")
        return true
    }

    fun review(id: Int, pass: Boolean): String? {
        var tip: String? = null
        querySource?.select(Tip.tip)?.where { Tip.id eq id }?.forEach {
            tip = it[Tip.tip]
        }
        val review: Int = if (pass) {
            tip?.let { tips.add(it) }
            1
        } else 0
        dataBase?.update(Tip) {
            it.review to review
            where { it.id eq id }
        }
        return tip
    }

    data class StoreTip(
        val tip: String,
        val id: Int,
        val date: String,
        val qqId: Long
    )

    private fun userTipGen(queryRowSet: QueryRowSet): UserTip? {
        val tip = queryRowSet[Tip.tip]
        val id = queryRowSet[Tip.id]
        val review = queryRowSet[Tip.review]
        if (
            tip != null &&
            id != null
        ) {
            return if (review == null) {
                UserTip(
                    tip, id, review
                )
            } else {
                UserTip(
                    tip, id, review == 1
                )
            }
        }
        return null
    }

    fun getReviewingList(qqId: Long): List<UserTip>? {
        val tempList = mutableListOf<UserTip>()
        querySource?.select()?.where { Tip.qqId eq qqId and (Tip.review.isNull()) }?.forEach { queryRowSet ->
            userTipGen(queryRowSet)?.let { tempList.add(it) }
        }
        return if (tempList.isEmpty()) null else tempList
    }

    fun getPendingTips(): StoreTip? {
        querySource?.select()?.where { Tip.review.isNull() }?.limit(0, 1)?.also {
            if (it.totalRecords == 0) return null
        }?.forEach { queryRowSet ->
            val tip = queryRowSet[Tip.tip]
            val id = queryRowSet[Tip.id]
            val date = queryRowSet[Tip.date]
            val qqId = queryRowSet[Tip.qqId]
            if (
                tip != null &&
                id != null &&
                date != null &&
                qqId != null
            ) {
                return StoreTip(
                    tip, id, date, qqId
                )
            }
        }
        return null
    }

    data class UserTip(
        val tip: String,
        val id: Int,
        val review: Boolean?
    )

    fun getHistory(qqId: Long): List<UserTip>? {
        val tempList = mutableListOf<UserTip>()
        querySource?.select(Tip.tip, Tip.review, Tip.id)?.where { Tip.qqId eq qqId }?.forEach { queryRowSet ->
            userTipGen(queryRowSet)?.let { tempList.add(it) }
        }
        return if (tempList.isEmpty()) null else tempList
    }
}
