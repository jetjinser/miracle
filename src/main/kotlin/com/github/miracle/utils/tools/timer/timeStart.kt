package com.github.miracle.utils.tools.timer

import java.util.*

fun timeStart(hourOfDay: Int): Date {
    fun addOneDay(date: Date): Date {
        val startDT = Calendar.getInstance()
        startDT.time = date
        startDT.add(Calendar.DAY_OF_MONTH, 1)
        return startDT.time
    }

    val calendar = Calendar.getInstance()
    calendar.apply {
        set(Calendar.HOUR_OF_DAY, hourOfDay)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }
    var date = calendar.time
    if (date.before(Date())) {
        date = addOneDay(date)
    }
    return date
}