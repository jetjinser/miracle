package com.github.miracle.utils.data

import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.dsl.*
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.data.asMessageChain
import org.sqlite.SQLiteException
import com.github.miracle.utils.database.BotDataBase
import com.github.miracle.utils.database.BotDataBase.Thesaurus
import com.github.miracle.utils.logger.BotLogger
import net.mamoe.mirai.message.data.isPlain
import kotlin.random.Random.Default.nextDouble

class ThesaurusData(private val event: GroupMessageEvent) {
    private var query: Query? = null
    private val dataBase: Database? = BotDataBase.getInstance()
    private val logger = BotLogger.logger("TRD")

    private var _answerList: MutableList<String> = mutableListOf()

    private val contentStringMiraiCode = event.message.filter { it.isPlain() }.joinToString("")

    init {
        query = dataBase?.from(Thesaurus)?.select(Thesaurus.answer)?.where {
            (Thesaurus.question eq contentStringMiraiCode) and (Thesaurus.groupId eq event.group.id or (Thesaurus.groupId.isNull()))
        }
        _answerList = mutableListOf()
        query?.forEach { queryRowSet ->
            queryRowSet[Thesaurus.answer]?.let { answer ->
                _answerList.add(answer)
            }
        }
    }

    fun add(question: String, answer: String, global: Boolean = false): Boolean {
        return try {
            var groupId: Long? = event.group.id
            if (global) groupId = null
            dataBase?.insert(Thesaurus) {
                it.qqId to event.sender.id
                it.groupId to groupId
                it.question to question
                it.answer to answer
            }
            logger.info("已${if (global) "全局" else ""}添加问 [$question] 答 [$answer] Added by ${event.sender.id} in ${event.group.id}")
            true
        } catch (e: SQLiteException) {
            logger.info("问答 [$question] => [$answer] 已存在")
            false
        }
    }

    fun random(probability: Double): Boolean {
        val double = nextDouble()
        return double < probability
    }

    val answerList: MutableList<String>
        get() {
            if (_answerList.isNotEmpty()) logger.verbose("answerList: $_answerList")
            return _answerList
        }
}