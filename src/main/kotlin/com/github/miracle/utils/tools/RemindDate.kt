package com.github.miracle.utils.tools

import java.util.*
import java.util.concurrent.TimeUnit

object RemindDate {
    enum class Unit(val unitArray: Array<String>) {
        Sec(arrayOf("秒", "秒钟")),
        Min(arrayOf("分", "分钟")),
        Hou(arrayOf("时", "小时")),
        Day(arrayOf("天", "日")),
        Wee(arrayOf("周", "星期")),
    }

    fun getDate(sentence: String): Date? {
        return Calendar.getInstance().also {
            val mills = toMills(sentence)?.toInt()
            if (mills == null) return null else it.add(Calendar.SECOND, mills)
        }.time
    }

    private fun toMills(sentence: String): Long? {
        val unit = findStringThatEndsWith(sentence) ?: return null
        val num = CnNum2ArabNum.parseNumber(sentence).toLong()
        if (num == 0L) return null

        return when (unit) {
            Unit.Sec -> TimeUnit.SECONDS.toSeconds(num)
            Unit.Min -> TimeUnit.MINUTES.toSeconds(num)
            Unit.Hou -> TimeUnit.HOURS.toSeconds(num)
            Unit.Day -> TimeUnit.DAYS.toSeconds(num)
            Unit.Wee -> TimeUnit.DAYS.toSeconds(1) * num
        }
    }

    private fun findStringThatEndsWith(sentence: String): Unit? {
        for (u in Unit.values()) {
            for (uu in u.unitArray) {
                return if (sentence.endsWith(uu)) u else continue
            }
        }
        return null
    }
}