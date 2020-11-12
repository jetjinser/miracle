package com.github.miracle.utils.tools

import java.util.*
import java.util.concurrent.TimeUnit

object RemindDate {
    enum class Unit(val unitArray: Array<String>) {
        Sec(arrayOf("秒", "秒钟")),
        Min(arrayOf("分", "分钟")),
        Hou(arrayOf("小时", "时")), // endWith判断结果一样，把小时放前面先判断
        Day(arrayOf("天", "日")),
        Wee(arrayOf("周", "星期")),
    }

    fun getDate(sentence: String): Date? {
        return Calendar.getInstance().also {
            val mills = toMills(sentence)?.toInt()
            if (mills == null) return null else it.add(Calendar.SECOND, mills)
        }.time
    }
    private var endString = ""

    /**
     * 转换秒数
     */
    private fun toMills(sentence: String): Long? {
        val unit = findStringThatEndsWith(sentence) ?: return null
        var num = sentence.replace(endString, "").toLongOrNull()
        if (num == null) {
            num = CnNum2ArabNum.parseNumber(sentence).toLong()
        }
        if (num == 0L) return null

        return when (unit) {
            Unit.Sec -> TimeUnit.SECONDS.toSeconds(num)
            Unit.Min -> TimeUnit.MINUTES.toSeconds(num)
            Unit.Hou -> TimeUnit.HOURS.toSeconds(num)
            Unit.Day -> TimeUnit.DAYS.toSeconds(num)
            Unit.Wee -> TimeUnit.DAYS.toSeconds(1) * num
        }
    }

    /**
     * 判断分秒时
     */
    private fun findStringThatEndsWith(sentence: String): Unit? {
        for (u in Unit.values()) {
            for (uu in u.unitArray) {
                if (sentence.endsWith(uu)){
                    endString = uu
                    return  u
                } else continue
            }
        }
        return null
    }
}