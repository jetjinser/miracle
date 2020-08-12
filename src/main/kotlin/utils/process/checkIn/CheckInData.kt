package utils.process.checkIn

import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.dsl.*
import net.mamoe.mirai.message.GroupMessageEvent
import utils.database.BotDataBase
import utils.database.BotDataBase.User
import utils.logger.BotLogger

/**
 * 处理签到的数据
 * @param event 机器人收到的群消息的事件
 * @see GroupMessageEvent
 * @author jinser
 */
class CheckInData(private val event: GroupMessageEvent) {
    private var query: Query?
    private val dataBase: Database? = BotDataBase.getInstance()
    private val logger = BotLogger.logger("CheckInData")

    /**
     * java.sql.SQLException: Invalid cursor position.
     * at me.liuwj.ktorm.database.CachedRowSet.next(CachedRowSet.kt:211)
     * at me.liuwj.ktorm.database.JdbcExtensionsKt$iterator$1.hasNext(JdbcExtensions.kt:72)
     * at utils.process.checkIn.CheckInData$_checkInDays$2.invoke(CheckInData.kt:194)
     * at utils.process.checkIn.CheckInData$_checkInDays$2.invoke(CheckInData.kt:16)
     * at kotlin.SynchronizedLazyImpl.getValue(LazyJVM.kt:74)
     * at utils.process.checkIn.CheckInData.get_checkInDays(CheckInData.kt)
     * at utils.process.checkIn.CheckInData.getCheckInDays(CheckInData.kt:135)
     * at utils.process.checkIn.CheckInModel.checkIn(CheckInModel.kt:31)
     * at plugins.CheckInKt$checkIn$1$1.invokeSuspend(checkIn.kt:13)
     * at plugins.CheckInKt$checkIn$1$1.invoke(checkIn.kt)
     */

    init {
        query = dataBase?.from(User)?.select()?.where { User.QQId eq event.sender.id }

        if (query?.totalRecords == 0) {
            registeredUser()
            logger.info("新增用户 ${event.senderName}, 已录入数据库")
            query = dataBase?.from(User)?.select()?.where { User.QQId eq event.sender.id }
        }
    }

    private val _card: String? by lazy {
        var value: String? = null
        query?.forEach {
            value = it[User.card]
        }
        value
    }
    private val _checkInDays: Int? by lazy {
        var value: Int? = null
        query?.forEach {
            value = it[User.checkInDays]
        }
        value
    }
    private val _lastCheckInDay: String? by lazy {
        var value: String? = null
        query?.forEach {
            value = it[User.lastCheckInDay]
        }
        value
    }
    private val _cuprum: Int? by lazy {
        var value: Int? = null
        query?.forEach {
            value = it[User.cuprum]
        }
        value
    }
    private val _favor: Int? by lazy {
        var value: Int? = null
        query?.forEach {
            value = it[User.favor]
        }
        value
    }


    /**
     * 注册 / 初始化 User 到数据库
     */
    private fun registeredUser() {
        dataBase?.insert(User) {
            it.favor to 0
            it.cuprum to 0
            it.QQId to event.sender.id
            it.nickname to event.sender.nick
            it.card to event.sender.nameCard
            it.checkInDays to 0
            it.lastCheckInDay to "1970-01-01"
        }
    }

    /**
     * 从数据里取值的抽象方法
     */
    private fun getValue(column: String): Any? {
        query?.forEach {
            val value = it[User[column]]
            if (value is String?) {
                if (!value.isNullOrEmpty()) return value
            } else if (value is Int?) {
                return value
            }
        }
        return null
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


//    var nickname: String
//        get() = getValue(User.nickname) as String
//        set(value) = setValue(value, "nickname")

    var card: String?
        get() = _card
        set(value) {
            if (dataBase != null) {
                dataBase.update(User) {
                    it.card to value
                    where {
                        it.QQId.toLong() eq event.sender.id
                    }
                }
            } else {
                logger.error("database has not been initialized")
            }
        }

    var checkInDays: Int?
        get() = _checkInDays
        set(value) {
            if (dataBase != null) {
                dataBase.update(User) {
                    it.checkInDays to value
                    where {
                        it.QQId.toLong() eq event.sender.id
                    }
                }
            } else {
                logger.error("database has not been initialized")
            }
        }

    var lastCheckInDay: String?
        get() = _lastCheckInDay
        set(value) {
            if (dataBase != null) {
                dataBase.update(User) {
                    it.lastCheckInDay to value
                    where {
                        it.QQId.toLong() eq event.sender.id
                    }
                }
            } else {
                logger.error("database has not been initialized")
            }
        }

    var cuprum: Int?
        get() = _cuprum
        set(value) {
            if (dataBase != null) {
                dataBase.update(User) {
                    it.cuprum to value
                    where {
                        it.QQId.toLong() eq event.sender.id
                    }
                }
            } else {
                logger.error("database has not been initialized")
            }
        }

    var favor: Int?
        get() = _favor
        set(value) {
            if (dataBase != null) {
                dataBase.update(User) {
                    it.favor to value
                    where {
                        it.QQId.toLong() eq event.sender.id
                    }
                }
            } else {
                logger.error("database has not been initialized")
            }
        }
}