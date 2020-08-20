package utils.database

import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.schema.*
import utils.network.OkHttpUtil

object BotDataBase {
    private var singleton: Database? = null

    fun getInstance(): Database? {
        if (singleton == null) {
            synchronized(OkHttpUtil::class.java) {
                if (singleton == null) {
                    singleton =
                        Database.connect("jdbc:sqlite:C:/Users/cmdrj/Desktop/archived/miracle/src/main/resources/data.db")
                }
            }
        }
        return singleton
    }

    object User : Table<Nothing>("User") {
        val QQId = long("qq_id").primaryKey()
        val nickname = varchar("nickname")
        val card = varchar("card")
        val checkInDays = int("check_in_days")
        val lastCheckInDay = varchar("last_check_in_day")
        val cuprum = int("cuprum")
        val favor = int("favor")
    }

    object Tip : Table<Nothing>("Tip") {
        val id = int("id").primaryKey()
        val tip = varchar("tip")
        val date = varchar("date")
        val qqId = long("qq_id")
    }

    object Cache : Table<Nothing>("Cache") {
        val id = int("id").primaryKey()
        val messageId = int("message_id")
        val content = varchar("content")
    }

    object Thesaurus : Table<Nothing>("Thesaurus") {
        val qqId = long("qq_id")
        val question = varchar("question").primaryKey()
        val answer = varchar("answer").primaryKey()
        val groupId = long("group_id").primaryKey()
        val global = int("global") // 0 or 1
    }

    object GroupSetting : Table<Nothing>("GroupSetting") {
        val id = int("id").primaryKey()
        val groupId = long("group_id")
        val qaProbability = double("qa_probability")
    }
}