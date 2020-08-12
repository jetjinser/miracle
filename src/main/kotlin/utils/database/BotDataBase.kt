package utils.database

import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.schema.Table
import me.liuwj.ktorm.schema.int
import me.liuwj.ktorm.schema.varchar
import utils.network.OkHttpUtil

object BotDataBase {
    private var singleton: Database? = null

    fun getInstance(): Database? {
        if (singleton == null) {
            synchronized(OkHttpUtil::class.java) {
                if (singleton == null) {
                    singleton = Database.connect("jdbc:sqlite:/src/main/resource/data.db")
                }
            }
        }
        return singleton
    }

    object User : Table<Nothing>("User") {
        val QQId = int("qq_id").primaryKey()
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
        val qqId = varchar("qq_id")
    }
}