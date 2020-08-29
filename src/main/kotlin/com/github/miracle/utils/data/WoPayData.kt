package com.github.miracle.utils.data

import com.github.miracle.utils.database.BotDataBase
import com.github.miracle.utils.database.BotDataBase.Token
import com.github.miracle.utils.database.BotDataBase.Deadline
import com.github.miracle.utils.logger.BotLogger
import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.dsl.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.NoSuchElementException

object WoPayData {
    private val dataBase: Database? = BotDataBase.getInstance()
    private val tokenQuerySource = dataBase?.from(Token)
    private val deadlineQuerySource = dataBase?.from(Deadline)
    private val logger = BotLogger.logger("WPD")

    init {
        logger.info("WoPayData initialized")
    }

    /**
     * 生成 token
     * @param days token 可以续费的日期 [Date]
     * @return token [UUID] null 时表示失败: database 未初始化
     */
    fun genToken(days: Int): String? {
        val token = UUID.randomUUID().toString().replace("-", "")

        dataBase?.insert(Token) {
            it.token to token
            it.day to days
        }?.run {
            return token
        }
        return null
    }

    fun register(groupId: Long, deadline: String) {
        dataBase?.insert(Deadline) {
            it.groupId to groupId
            it.deadline to deadline
        }
    }

    /**
     * 续期
     * @param token 凭借 token 查询对应的天数来续期
     * @param groupId 续期的群号码
     * @return 续期后的日期, null 时表示失败: token 不存在
     */
    fun renew(token: String, groupId: Long): String? {
        val daysQuery = tokenQuerySource
            ?.select(Token.day)
            ?.where { Token.token eq token }

        if (daysQuery?.totalRecords == 0) return null

        val days = daysQuery?.iterator()?.next()
            ?.get(Token.day) ?: return null

        var deadline = LocalDate.now()

        val query = deadlineQuerySource
            ?.select(Deadline.deadline)
            ?.where { Deadline.groupId eq groupId }
        if (query?.totalRecords == 0) {
            // 此时 dateNow 是 deadline
            register(groupId, deadline.toString())
        } else {
            // 此时 select, deadline 为 deadline
            if (query != null) {
                deadline = LocalDate.parse(
                    query.iterator().next()[Deadline.deadline],
                    DateTimeFormatter.ISO_DATE
                )
            } else return null
        }

        val newDeadline = deadline.plusDays(days.toLong()).toString()
        dataBase?.apply {
            update(Deadline) {
                it.deadline to newDeadline
                where {
                    Deadline.groupId eq groupId
                }
            }

            delete(Token) {
                it.token eq token
            }
        }

        return newDeadline
    }

    /**
     * 查询到期
     */
    fun inquire(groupId: Long) = try {
        deadlineQuerySource?.select(Deadline.deadline)?.where { Deadline.groupId eq groupId }?.iterator()?.next()
            ?.get(Deadline.deadline)
    } catch (e: NoSuchElementException) {
        register(groupId, LocalDate.now().toString())
        logger.info("Registered a new group deadline: $groupId")
        deadlineQuerySource?.select(Deadline.deadline)?.where { Deadline.groupId eq groupId }?.iterator()?.next()
            ?.get(Deadline.deadline)
    }
}