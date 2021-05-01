package com.github.miracle.utils.data

import com.github.miracle.utils.database.BotDataBase.SubPlatform.LOFTER

object SubLofterCache {
    private val lofTagCache = mutableMapOf<String, Long>()
//    private val userCache = mutableMapOf<String, Long>()
    private var lofTagQueue = SubscribeData.getSubQueue(LOFTER) //  Map<订阅id，订阅该id的所有群号>，从数据库获取
//    private var userQueue = SubscribeData.getSubQueue(WEIBO) //  Map<订阅id，订阅该id的所有群号>，从数据库获取
    private var lofTagIter = lofTagQueue.iterator()   // nid, list(gid)
//    private var userIter = userQueue.iterator()   // uid, list(gid)
    fun nextSubLofTag(): MutableMap.MutableEntry<String, MutableList<Long>> =
        if (lofTagIter.hasNext()) {
            lofTagIter.next()
        } else {
            lofTagIter = lofTagQueue.iterator()
            lofTagIter.next()
        }

//    fun nextSubUser(): MutableMap.MutableEntry<String, MutableList<Long>> =
//        if (userIter.hasNext()) {
//            userIter.next()
//        } else {
//            userIter = userQueue.iterator()
//            userIter.next()
//        }

    fun refreshLofTagCache() {
        lofTagQueue = SubscribeData.getSubQueue(LOFTER)
    }

//    fun refreshUserCache() {
//        userQueue = SubscribeData.getSubQueue(WEIBO)
//    }

    fun setLastTagUpdateTime(tId: String, time: Long) {
        lofTagCache[tId] = time
    }

    fun getLastTagUpdateTime(tId: String): Long {
        return lofTagCache[tId] ?: 0
    }

//    fun setLastUserUpdateTime(uId: String, time: Long) {
//        userCache[uId] = time
//    }
//
//    fun getLastUserUpdateTime(uId: String): Long {
//        return userCache[uId] ?: 0
//    }
}