package com.github.miracle.utils.data

import com.github.miracle.utils.database.BotDataBase.Platform.BILI

object SubBiliCache {

    private val liveRoomCache = mutableMapOf<String, Boolean>()
    private var liveQueue = SubscribeData.getSubQueue(BILI)
    private var liveIter = liveQueue.iterator()
    fun nextSub(): Pair<MutableMap.MutableEntry<String, MutableList<Long>>, MutableMap<String, Boolean>> =
        if (liveIter.hasNext()) {
            liveIter.next()
        } else {
            liveIter = liveQueue.iterator()
            liveIter.next()
        } to liveRoomCache

    fun refreshCache(){
        liveQueue = SubscribeData.getSubQueue(BILI)
    }

    fun markLiving(bid: String) {
        liveRoomCache[bid] = true
    }

    fun markUnliving(bid: String) {
        liveRoomCache[bid] = false
    }
}