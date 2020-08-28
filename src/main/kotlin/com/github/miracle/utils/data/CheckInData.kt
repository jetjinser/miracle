package com.github.miracle.utils.data

import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.dsl.*
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.message.GroupMessageEvent
import com.github.miracle.utils.database.BotDataBase
import com.github.miracle.utils.database.BotDataBase.User
import com.github.miracle.utils.logger.BotLogger

/**
 * 处理签到的数据
 * @param event 机器人收到的群消息的事件
 * @author jinser
 * @see GroupMessageEvent
 */
class CheckInData(private val event: GroupMessageEvent) {
    private var query: Query? = null
    private val dataBase: Database? = BotDataBase.getInstance()
    private val logger = BotLogger.logger("CID")

    private var _card: String? = null
    private var _checkInDays: Int? = null
    private var _lastCheckInDay: String? = null
    private var _cuprum: Int? = null
    private var _favor: Int? = null

    fun consumeCuprum(amount: Int): Pair<Boolean, Int?> {
        val cuprum = this.cuprum
        if (cuprum != null) {
            if (cuprum < amount) {
                logger.info("${event.sender.nameCardOrNick} 当前铜币 $cuprum 枚, 不足消费 $amount")
                return false to cuprum
            }
            this.cuprum = cuprum - amount
            logger.info("${event.sender.nameCardOrNick} 消费 $amount 枚铜币")
            return true to cuprum
        }
        return false to cuprum
    }

    /**
     * 注册 / 初始化 User 到数据库
     */
    private fun registeredUser() {
        dataBase?.insert(User) {
            it.favor to 0
            it.cuprum to 0
            it.QQId to event.sender.id
            it.nickname to event.senderName
            it.card to event.sender.nameCard
            it.checkInDays to 0
            it.lastCheckInDay to "1970-01-01"
        }
    }

    init {
        query = dataBase?.from(User)?.select()?.where { User.QQId eq event.sender.id }

        if (query?.totalRecords == 0) {
            registeredUser()
            logger.info("新增用户 ${event.senderName}, 已录入数据库")

            _favor = 0
            _cuprum = 0
            _card = event.sender.nameCard
            _checkInDays = 0
            _lastCheckInDay = "1970-01-01"
        } else {
            query?.forEach {
                _card = it[User.card]
                _checkInDays = it[User.checkInDays]
                _lastCheckInDay = it[User.lastCheckInDay]
                _cuprum = it[User.cuprum]
                _favor = it[User.favor]
            }
        }
    }

    /**
     * 设置 / update 数据库值的抽象方法
     */
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


    var card: String?
        get() = _card
        set(value) {
            setValue(value, "card")
            _card = value
        }

    var checkInDays: Int?
        get() = _checkInDays
        set(value) {
            setValue(value, "check_in_days")
            _checkInDays = value
        }

    var lastCheckInDay: String?
        get() = _lastCheckInDay
        set(value) {
            setValue(value, "last_check_in_day")
            _lastCheckInDay = value
        }

    var cuprum: Int?
        get() = _cuprum
        set(value) {
            setValue(value, "cuprum")
            _cuprum = value
        }

    var favor: Int?
        get() = _favor
        set(value) {
            setValue(value, "favor")
            _favor = value
        }
}