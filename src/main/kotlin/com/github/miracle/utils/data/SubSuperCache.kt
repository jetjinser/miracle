package com.github.miracle.utils.data

import com.github.miracle.utils.database.BotDataBase.Platform.SUPER

object SubSuperCache {
    private val superCache = mutableMapOf<String, Long>()
    private var superQueue = SubscribeData.getSubQueue(SUPER) //  Map<订阅id，订阅该id的所有群号>，从数据库获取
    private var superIter = superQueue.iterator()   // nid, list(gid)
    fun nextSub(): MutableMap.MutableEntry<String, MutableList<Long>> =
        if (superIter.hasNext()) {
            superIter.next()
        } else {
            superIter = superQueue.iterator()
            superIter.next()
        }

    fun refreshCache(){
        superQueue = SubscribeData.getSubQueue(SUPER)
    }

    fun setLastUpdateTime(sId: String, time: Long) {
        superCache[sId] = time
    }

    fun getLastUpdateTime(sId: String): Long {
        return superCache[sId]?:0
    }
}