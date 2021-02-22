package com.github.miracle.utils.data

import com.github.miracle.utils.database.BotDataBase.Platform.JJWXC

object NovelSubCache {
    private val novelChapterCache = mutableMapOf<Long, Int>() // 每个小说的章节信息缓存Map<nid, lastChapId>
    private val novelQueue = SubscribeData.getQueue(JJWXC) //  Map<订阅id，订阅该id的所有群号>，从数据库获取
    private var novelIter = novelQueue.iterator()   // nid, list(gid)
    fun nextSub(): Pair<MutableMap.MutableEntry<Long, MutableList<Long>>, MutableMap<Long, Int>> =
        if (novelIter.hasNext()) {
            novelIter.next()
        } else {
            novelIter = novelQueue.iterator()
            novelIter.next()
        } to novelChapterCache

    fun markLastChapter(nid: Long, cid: Int) {
        novelChapterCache[nid] = cid
    }
}