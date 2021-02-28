package com.github.miracle.utils.data

import com.github.miracle.utils.database.BotDataBase.Platform.SUPER
import com.github.miracle.utils.database.BotDataBase.Platform.WEIBO

object SubWeiboCache {
    private val superCache = mutableMapOf<String, Long>()
    private val userCache = mutableMapOf<String, Long>()
    private var superQueue = SubscribeData.getSubQueue(SUPER) //  Map<订阅id，订阅该id的所有群号>，从数据库获取
    private var userQueue = SubscribeData.getSubQueue(WEIBO) //  Map<订阅id，订阅该id的所有群号>，从数据库获取
    private var superIter = superQueue.iterator()   // nid, list(gid)
    private var userIter = userQueue.iterator()   // uid, list(gid)
    fun nextSubSuper(): MutableMap.MutableEntry<String, MutableList<Long>> =
        if (superIter.hasNext()) {
            superIter.next()
        } else {
            superIter = superQueue.iterator()
            superIter.next()
        }

    fun nextSubUser(): MutableMap.MutableEntry<String, MutableList<Long>> =
        if (userIter.hasNext()) {
            userIter.next()
        } else {
            userIter = userQueue.iterator()
            userIter.next()
        }

    fun refreshSuperCache() {
        superQueue = SubscribeData.getSubQueue(SUPER)
    }

    fun refreshUserCache() {
        userQueue = SubscribeData.getSubQueue(WEIBO)
    }

    fun setLastSuperUpdateTime(sId: String, time: Long) {
        superCache[sId] = time
    }

    fun getLastSuperUpdateTime(sId: String): Long {
        return superCache[sId] ?: 0
    }

    fun setLastUserUpdateTime(uId: String, time: Long) {
        userCache[uId] = time
    }

    fun getLastUserUpdateTime(uId: String): Long {
        return userCache[uId] ?: 0
    }
}