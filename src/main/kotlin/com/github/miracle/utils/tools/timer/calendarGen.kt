package com.github.miracle.utils.tools.timer

import java.util.*

fun calendarGen(hourOfDay: Int): Calendar {
    return Calendar.getInstance().also {
        it.set(Calendar.MINUTE, 0)
        it.set(Calendar.SECOND, 0)
        if (it.get(Calendar.HOUR_OF_DAY) > hourOfDay) it.add(Calendar.DATE, 1)
        it.set(Calendar.HOUR_OF_DAY, hourOfDay)
    }
}