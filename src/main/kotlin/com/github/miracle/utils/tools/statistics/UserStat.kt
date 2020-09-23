package com.github.miracle.utils.tools.statistics

import com.github.miracle.utils.database.BotDataBase
import com.github.miracle.utils.database.BotDataBase.User
import me.liuwj.ktorm.dsl.*

object UserStatHandle {
    private val querySource = BotDataBase.getInstance()?.from(User) ?: throw ExceptionInInitializerError("未初始化")

    data class UserStat(
        val count: Int,
        val mostDays: Int?,
        val mostCuprum: Int?,
        val mostFavor: Int?,
    )

    private fun getCount() = querySource
        .select(count())
        .iterator().next()
        .getInt(1)

    private fun getMostDays() = querySource
        .select(User.checkInDays)
        .orderBy(User.checkInDays.desc())
        .limit(0, 1)
        .iterator().next()[User.checkInDays]

    private fun getMostCuprum() = querySource
        .select(User.cuprum)
        .orderBy(User.cuprum.desc())
        .limit(0, 1)
        .iterator().next()[User.cuprum]

    private fun getMostFavor() = querySource
        .select(User.favor)
        .orderBy(User.favor.desc())
        .limit(0, 1)
        .iterator().next()[User.favor]

    val stat: UserStat?
        get() = UserStat(
            getCount(),
            getMostDays(),
            getMostCuprum(),
            getMostFavor()
        )
}