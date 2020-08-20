package utils.data

import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.dsl.*
import net.mamoe.mirai.contact.Group
import utils.database.BotDataBase
import utils.database.BotDataBase.GroupSetting
import utils.logger.BotLogger

object GroupSettingDataMap {
    private val groupSettingDataMap = mutableMapOf<Long, GroupSettingData>()

    fun getInstance(group: Group): GroupSettingData {
        val map = groupSettingDataMap[group.id]
        return if (map == null) {
            val data = GroupSettingData(group)
            groupSettingDataMap[group.id] = data
            data
        } else map
    }


    class GroupSettingData(private val group: Group) {
        private var query: Query? = null
        private val dataBase: Database? = BotDataBase.getInstance()
        private val logger = BotLogger.logger("GSD")

        init {
            query = dataBase?.from(GroupSetting)?.select(GroupSetting.qaProbability)?.where {
                GroupSetting.groupId eq group.id
            }

            if (query?.totalRecords == 0) {
                logger.info(
                    "新增设置 ${group.name}(${group.id}), ${GroupSetting.qaProbability.name}: $qaProbability 已录入数据库"
                )

                dataBase?.insert(GroupSetting) {
                    it.groupId to group.id
                    it.qaProbability to 0.7
                }
            }
            logger.info("GroupSettingData initialized")

            query?.forEach { queryRowSet ->
                queryRowSet[GroupSetting.qaProbability]?.let {
                    _qaProbability = it
                }
            }
        }

        private var _qaProbability: Double? = null

        var qaProbability: Double
            get() = _qaProbability ?: 0.7
            set(value) {
                require(value < 0.7 && value >= 0) { logger.error("用户设置机率大于70%, 抛出异常") }
                dataBase?.update(GroupSetting) {
                    it.qaProbability to value
                    where {
                        it.groupId eq group.id
                    }
                }
            }
    }
}