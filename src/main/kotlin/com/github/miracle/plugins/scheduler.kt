package com.github.miracle.plugins

import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule


fun Bot.scheduler() {
    suspend fun sendToEveryGroup(message: String) {
        this.groups.forEach {
            it.sendMessage(message)
        }
    }

    fun calendarGen(hourOfDay: Int): Calendar {
        return Calendar.getInstance().also {
            it.set(Calendar.HOUR_OF_DAY, hourOfDay)
            it.set(Calendar.MINUTE, 0)
            it.set(Calendar.SECOND, 0)
            if (it.get(Calendar.HOUR_OF_DAY) > hourOfDay) it.add(Calendar.DATE, 1)
        }
    }

    Timer().apply {
        schedule(calendarGen(6).time, TimeUnit.DAYS.toMillis(1)) {
            launch { sendToEveryGroup("早") }
        }
        schedule(calendarGen(0).time, TimeUnit.DAYS.toMillis(1)) {
            launch { sendToEveryGroup("🕛") }
        }
    }
}