package com.github.miracle.utils.data

import com.github.miracle.utils.database.BotDataBase.SubPlatform.REDDIT

object SubRedditCache {
    private val rdRssCache = mutableMapOf<String, Long>()
//    private val userCache = mutableMapOf<String, Long>()
    private var rdRssQueue = SubscribeData.getSubQueue(REDDIT) //  Map<订阅id，订阅该id的所有群号>，从数据库获取
//    private var userQueue = SubscribeData.getSubQueue(WEIBO) //  Map<订阅id，订阅该id的所有群号>，从数据库获取
    private var rdRssIter = rdRssQueue.iterator()   // nid, list(gid)
//    private var userIter = userQueue.iterator()   // uid, list(gid)
    fun nextSubRedditRss(): MutableMap.MutableEntry<String, MutableList<Long>> =
        if (rdRssIter.hasNext()) {
            rdRssIter.next()
        } else {
            rdRssIter = rdRssQueue.iterator()
            rdRssIter.next()
        }

//    fun nextSubUser(): MutableMap.MutableEntry<String, MutableList<Long>> =
//        if (userIter.hasNext()) {
//            userIter.next()
//        } else {
//            userIter = userQueue.iterator()
//            userIter.next()
//        }

    fun refreshRedditRssCache() {
        rdRssQueue = SubscribeData.getSubQueue(REDDIT)
    }

//    fun refreshUserCache() {
//        userQueue = SubscribeData.getSubQueue(WEIBO)
//    }

    fun setLastRedditRssUpdateTime(rUrl: String, time: Long) {
        rdRssCache[rUrl] = time
    }

    fun getLastRedditRssUpdateTime(rUrl: String): Long {
        return rdRssCache[rUrl] ?: 0
    }

//    fun setLastUserUpdateTime(uId: String, time: Long) {
//        userCache[uId] = time
//    }
//
//    fun getLastUserUpdateTime(uId: String): Long {
//        return userCache[uId] ?: 0
//    }
}