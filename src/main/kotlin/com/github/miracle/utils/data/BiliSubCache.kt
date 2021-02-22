package com.github.miracle.utils.data

import com.github.miracle.utils.database.BotDataBase.Platform.BILI

object BiliSubCache {

    private val liveRoomCache = mutableMapOf<Long, Boolean>()
    private val liveQueue = SubscribeData.getSubQueue(BILI)
    private var liveIter = liveQueue.iterator()
    fun nextSub(): Pair<MutableMap.MutableEntry<Long, MutableList<Long>>, MutableMap<Long, Boolean>> =
        if (liveIter.hasNext()) {
            liveIter.next()
        } else {
            liveIter = liveQueue.iterator()
            liveIter.next()
        } to liveRoomCache


    fun markLiving(bid: Long) {
        liveRoomCache[bid] = true
    }

    fun markUnliving(bid: Long) {
        liveRoomCache[bid] = false
    }
}