package com.github.miracle.utils.data

import com.github.miracle.utils.database.BotDataBase.Platform.JJWXC

object SubNovelCache {
    private val novelChapterCache = mutableMapOf<String, Int>() // 每个小说的章节信息缓存Map<nid, lastChapId>
    private var novelQueue = SubscribeData.getSubQueue(JJWXC) //  Map<订阅id，订阅该id的所有群号>，从数据库获取
    private var novelIter = novelQueue.iterator()   // nid, list(gid)
    fun nextSub(): Pair<MutableMap.MutableEntry<String, MutableList<Long>>, MutableMap<String, Int>> =
        if (novelIter.hasNext()) {
            novelIter.next()
        } else {
            novelIter = novelQueue.iterator()
            novelIter.next()
        } to novelChapterCache

    fun refreshCache(){
        novelQueue = SubscribeData.getSubQueue(JJWXC)
    }
    fun markLastChapter(nid: String, cid: Int) {
        novelChapterCache[nid] = cid
    }
}