package utils.process.checkIn

import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.dsl.*
import me.liuwj.ktorm.schema.Column
import net.mamoe.mirai.message.GroupMessageEvent
import utils.database.BotDataBase
import utils.database.BotDataBase.User
import utils.logger.BotLogger
import java.text.SimpleDateFormat
import java.util.*

/**
 * 处理签到的数据
 * @param event 机器人收到的群消息的事件
 * @see GroupMessageEvent
 */
class CheckInData(private val event: GroupMessageEvent) {
    private var query: Query?
    private val dataBase: Database? = BotDataBase.getInstance()
    private val logger = BotLogger.logger("CheckInData")

    init {
        query = dataBase?.from(User)?.select()?.where { User.QQId.toLong() eq event.sender.id }
        if (query?.totalRecords == 0) {
            registeredUser()
            query = dataBase?.from(User)?.select()?.where { User.QQId.toLong() eq event.sender.id }
        }
    }

    private fun registeredUser() {
        dataBase?.insert(User) {
            it.QQId to event.sender.id
            it.nickname to event.sender.nick
            it.card to event.sender.nameCard
            it.checkInDays to 0
            it.lastCheckInDay to SimpleDateFormat("YYYY-MM-d").format(Date())
        }
    }

    private fun <T : Any> getValue(column: Column<T>): Any? {
        query?.forEach {
            val value = it[column]
            if (value is String?) {
                if (!value.isNullOrEmpty()) return value
            } else if (value is Int?) {
                return value
            }
        }
        return null
    }

    private fun <T> setValue(value: T, key: String) {
        if (value != null) {
            dataBase?.update(User) {
                it[key] to value
                where {
                    it.QQId.toLong() eq event.sender.id
                }
            }
        } else {
            logger.error("database has not been initialized")
        }
    }


    var nickname: String?
        get() = getValue(User.nickname) as String?
        set(value) = setValue(value, "nickname")

    var card: String?
        get() = getValue(User.card) as String?
        set(value) = setValue(value, "card")

    var checkInDays: Int?
        get() = getValue(User.checkInDays) as Int?
        set(value) = setValue(value, "checkInDays")

    var lastCheckInDay: String?
        get() = getValue(User.lastCheckInDay) as String?
        set(value) = setValue(value, "lastCheckInDay")

    var cuprum: Int?
        get() = getValue(User.cuprum) as Int?
        set(value) = setValue(value, "cuprum")
}