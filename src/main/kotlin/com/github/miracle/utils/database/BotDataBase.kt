package com.github.miracle.utils.database

import com.github.miracle.utils.database.BotDataBase.NovelSubscription.primaryKey
import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.schema.*
import com.github.miracle.utils.network.KtorClient

object BotDataBase {
    private var singleton: Database? = null

    fun getInstance(): Database? {
        if (singleton == null) {
            synchronized(KtorClient::class.java) {
                if (singleton == null) {
                    singleton =
//                        Database.connect("jdbc:sqlite:C:/Users/cmdrj/Desktop/archived/miracle/src/main/resources/data.db")
                        Database.connect("jdbc:sqlite:data.db")
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

        /**
         * - **null**: 未审核
         * - **0**: 不通过
         * - **1**: 通过
         */
        val review = int("review")
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

    // woPay region
    object Deadline : Table<Nothing>("Deadline") {
        val deadline = varchar("deadline").primaryKey()
        val groupId = long("group_id")
    }

    object Token : Table<Nothing>("Token") {
        val token = varchar("token").primaryKey()
        val day = int("day")
    }
    // end region

    object BiliSubscription : Table<Nothing>("BiliSubscription") {
        val groupId = long("group_id").primaryKey()
        val roomId = long("room_id").primaryKey()
        val uname = varchar("uname")
    }
    object NovelSubscription : Table<Nothing>("NovelSubscription") {
        val groupId = long("group_id").primaryKey()
        val novelId = long("novel_id").primaryKey()
        val title = varchar("title")
    }
    object Flomo: Table<Nothing>("flomo") {
        val qqId = long("qq_id").primaryKey()
        val flomoKey = varchar("flomo_key")
    }
}