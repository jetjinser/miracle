package utils.data

import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.dsl.*
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.data.asMessageChain
import utils.database.BotDataBase
import utils.database.BotDataBase.Thesaurus
import utils.logger.BotLogger
import kotlin.random.Random.Default.nextDouble

class ThesaurusData(private val event: GroupMessageEvent) {
    private var query: Query? = null
    private val dataBase: Database? = BotDataBase.getInstance()
    private val logger = BotLogger.logger("TRD")


    private fun update() {
        query = dataBase?.from(Thesaurus)?.select()?.where {
            (Thesaurus.question eq event.message.drop(1).asMessageChain().toString()
                    or (Thesaurus.global eq 1)) and (Thesaurus.groupId eq event.group.id)
        }
    }

    init {
        update()
//        logger.verbose("ThesaurusDate initialized")
    }

    fun add(question: String, answer: String, global: Int = 0) {
        require(global in 0..1) { logger.error("global 必须为 0(false) 或 1(true)") }
        // TODO 数据库 Thesaurus unique 约束 成功 return true
        dataBase?.insert(Thesaurus) {
            it.qqId to event.sender.id
            it.groupId to event.group.id
            it.global to global
            it.question to question
            it.answer to answer
            logger.info("已${if (global == 1) "全局" else ""}添加问 $question 答 $answer Added by ${event.sender.id} in ${event.group.id}")
        }
    }

    fun random(probability: Double): Boolean {
        val double = nextDouble()
        return probability < double
    }

    val answerList: MutableList<String>
        get() {
            val answerList = mutableListOf<String>()
            query?.forEach { queryRowSet ->
                queryRowSet[Thesaurus.answer]?.let {
                    answerList.add(it)
                }
            }
            return answerList
        }
}